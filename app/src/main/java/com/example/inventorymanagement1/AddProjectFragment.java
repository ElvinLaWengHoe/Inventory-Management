package com.example.inventorymanagement1;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddProjectFragment extends Fragment {

    private TextInputEditText projectName;
    private DatabaseReference itemDbRef;
    public static TextView qrCodeNumber;
    private Button qrScanBtn;
    private Button saveBtn;
    Context context;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        context = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_project, container, false);

        projectName = view.findViewById(R.id.project_name);
        qrCodeNumber = view.findViewById(R.id.qrCodeNumber);
        qrScanBtn = view.findViewById(R.id.scan_qr_button);
        saveBtn = view.findViewById(R.id.save_button);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        itemDbRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);


        qrScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), AddProjectScanActivity.class);
                startActivity(intent);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertData();
            }
        });

        return view;
    }

    private void insertData() {
        String project_name = projectName.getText().toString().trim().toLowerCase()
                .replace(".","")
                .replace("$","")
                .replace("#","")
                .replace("[","")
                .replace("]","")
                .replace("/","");
        String qrcode = qrCodeNumber.getText().toString().trim();
        String emailHeader = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        DatabaseReference userDbRef = FirebaseDatabase.getInstance().getReference().child("Users").child(emailHeader);


        userDbRef.child("Project").orderByChild("project_name").equalTo(project_name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot nameSnapshot) {
                if (project_name.isEmpty() || qrcode.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }
                else if (nameSnapshot.exists()) {
                    Toast.makeText(getContext(), "Project with the same name already exists", Toast.LENGTH_SHORT).show();
                    return;
                }
                else {
                    userDbRef.child("Project").orderByChild("qrcode").equalTo(qrcode).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot qrSnapshot) {
                            if (qrSnapshot.exists()) {
                                Toast.makeText(getContext(), "Project with the same QR code already exists", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            DatabaseReference itemsDbRef = userDbRef.child("Project").child(project_name);

                            itemsDbRef.child("project_name").setValue(project_name);
                            itemsDbRef.child("qrcode").setValue(qrcode);

                            Toast.makeText(getContext(), "Project Added", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(requireContext(), HomeActivity.class);
                            startActivity(intent);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("Firebase", "Error checking for existing item by QR code: " + error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error checking for existing item by name: " + error.getMessage());
            }
        });

    }
}