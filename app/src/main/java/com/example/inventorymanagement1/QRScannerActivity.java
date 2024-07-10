package com.example.inventorymanagement1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
public class QRScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 0;

    private ZXingScannerView scannerView;

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

    @Override
    public void handleResult(com.google.zxing.Result result) {
        String qrcode = result.getText();
        fetchItemFromFirebase(qrcode);
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
        /*scannerView.setResultHandler(this);
        scannerView.startCamera();*/
    }

    /*protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        }
    }

    private void startCamera() {
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }*/

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

    private void fetchItemFromFirebase(String qrcode) {
        String emailHeader = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
        DatabaseReference projectRef = FirebaseDatabase.getInstance().getReference().child("Users").child(emailHeader).child("Project");

        projectRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean itemFound = false;
                for (DataSnapshot projectSnapshot : snapshot.getChildren()) {
                    String itemQrCode = projectSnapshot.child("qrcode").getValue(String.class);
                    if (qrcode.equals(itemQrCode)) {
                        String projectName = projectSnapshot.getKey();
                        if (projectName != null) {
                            Intent intent = new Intent(QRScannerActivity.this, HomeActivity.class);
                            intent.putExtra("project_name", projectName);
                            intent.putExtra("qrcode", itemQrCode);
                            startActivity(intent);
                        }
                        itemFound = true;
                        break;
                    }
                }
                if (!itemFound) {
                    Toast.makeText(QRScannerActivity.this, "Item not found", Toast.LENGTH_SHORT).show();
                    scannerView.resumeCameraPreview(QRScannerActivity.this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QRScannerActivity.this, "Failed to fetch item data", Toast.LENGTH_SHORT).show();
                scannerView.resumeCameraPreview(QRScannerActivity.this);
            }
        });
    }
}

