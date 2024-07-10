package com.example.inventorymanagement1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class AddProjectScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 0;

    private ZXingScannerView scannerView;
    private static final String SPECIAL_CHARACTERS = "[.#$/\\[\\]]";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            startCamera();
        }
    }

    public static boolean containSpecialCharacters(com.google.zxing.Result result) {
        String text_result = result.getText();
        Pattern pattern = Pattern.compile(SPECIAL_CHARACTERS);
        Matcher matcher = pattern.matcher(text_result);
        return matcher.find();
    }

    @Override
    public void handleResult(com.google.zxing.Result result) {
        if(AddProjectFragment.qrCodeNumber != null) {
            boolean hasSpecialChars = containSpecialCharacters(result);
            if (hasSpecialChars) {
                Toast.makeText(getApplicationContext(), "Special Characters (., #, $, [, ], /) are not allowed", Toast.LENGTH_SHORT).show();
            } else {
                AddProjectFragment.qrCodeNumber.setText(result.getText());
            }
        }
        onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    protected void onPostResume() {
        super.onPostResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
           startCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCamera() {
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }
}