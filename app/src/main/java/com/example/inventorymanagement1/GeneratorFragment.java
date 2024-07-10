package com.example.inventorymanagement1;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import com.journeyapps.barcodescanner.BarcodeEncoder;

import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class GeneratorFragment extends Fragment {

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private EditText edit_text_input;
    private Button button_generate, button_save_qr;
    private ImageView image_view_qr_code;
    private Bitmap qr_code_bitmap;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_generator, container, false);

        edit_text_input = view.findViewById(R.id.edit_text_input);
        button_generate = view.findViewById(R.id.button_generate);
        button_save_qr = view.findViewById(R.id.button_save_qr);
        image_view_qr_code = view.findViewById(R.id.image_view_qr_code);

        button_generate.setOnClickListener(v -> generateQRCode());

        button_save_qr.setOnClickListener(v -> {
            if (qr_code_bitmap != null) {
                checkPermissionsAndSaveQRCode();
            } else {
                Toast.makeText(requireActivity(), "Generate a QR code first", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void generateQRCode() {
        String text = edit_text_input.getText().toString().toLowerCase().trim()
                .replace(".","")
                .replace("#","")
                .replace("$","")
                .replace("[","")
                .replace("]","")
                .replace("/","");
        if (text.isEmpty()) {
            Toast.makeText(requireActivity(), "Please enter text", Toast.LENGTH_SHORT).show();
            return;
        }

        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        try {
            BitMatrix bitMatrix = barcodeEncoder.encode(text, BarcodeFormat.QR_CODE, 400, 400);
            qr_code_bitmap = barcodeEncoder.createBitmap(bitMatrix);
            image_view_qr_code.setImageBitmap(qr_code_bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(requireActivity(), "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkPermissionsAndSaveQRCode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                saveQRCode();
            } else {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                } catch (Exception e) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(intent);
                }
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                saveQRCode();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveQRCode();
            } else {
                Toast.makeText(requireActivity(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveQRCode() {
        if (qr_code_bitmap == null) {
            Toast.makeText(requireActivity(), "Generate a QR code first", Toast.LENGTH_SHORT).show();
            return;
        }

        OutputStream fos;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, "qrcode_" + System.currentTimeMillis() + ".png");
                values.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MyAppQR");

                Uri uri = requireActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    fos = requireActivity().getContentResolver().openOutputStream(uri);
                } else {
                    throw new Exception("Failed to create new MediaStore record.");
                }
            } else {
                String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/MyAppQR";
                File directory = new File(path);
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                File file = new File(directory, "qrcode_" + System.currentTimeMillis() + ".png");
                fos = new FileOutputStream(file);
            }

            qr_code_bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            Toast.makeText(requireActivity(), "QR code saved successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireActivity(), "Failed to save QR code", Toast.LENGTH_SHORT).show();
        }
    }
}