package com.example.inventorymanagement1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inventorymanagement1.databinding.ActivityAddItemScanBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class AddFragment extends Fragment {

    private ImageView minus;
    private ImageView plus;
    private EditText numbers;
    private TextInputEditText itemName;
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
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        plus = view.findViewById(R.id.plus);
        minus = view.findViewById(R.id.minus);
        numbers = view.findViewById(R.id.numbers);
        itemName = view.findViewById(R.id.item_name);
        qrCodeNumber = view.findViewById(R.id.qrCodeNumber);
        qrScanBtn = view.findViewById(R.id.scan_qr_button);
        saveBtn = view.findViewById(R.id.save_button);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        itemDbRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        final int[] num = {0};

        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                num[0]++;
                updateEditText(num[0]);
            }
        });
        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (num[0] > 0) {
                    num[0]--;
                    updateEditText(num[0]);
                }
            }
        });

        numbers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    num[0] = Integer.parseInt(s.toString());
                } catch (NumberFormatException e ){
                    num[0] = 0;
                }
            }
        });

        qrScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), AddItemScanActivity.class);
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

    private void updateEditText(int value) {
        numbers.setText(String.valueOf(value));
    }

    private void insertData() {
        String name = itemName.getText().toString().trim().toLowerCase()
                .replace(".","")
                .replace("$","")
                .replace("#","")
                .replace("[","")
                .replace("]","")
                .replace("/","");
        String count = numbers.getText().toString().trim();
        String qrcode = qrCodeNumber.getText().toString().trim();
        String emailHeader = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        DatabaseReference userDbRef = FirebaseDatabase.getInstance().getReference().child("Users").child(emailHeader);


        userDbRef.child("Items").orderByChild("name").equalTo(name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot nameSnapshot) {
                if (name.isEmpty() || qrcode.isEmpty() || count.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }
                else if (nameSnapshot.exists()) {
                    Toast.makeText(getContext(), "Item with the same name already exists", Toast.LENGTH_SHORT).show();
                    return;
                }
                else {
                    userDbRef.child("Items").orderByChild("qrcode").equalTo(qrcode).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot qrSnapshot) {
                            if (qrSnapshot.exists()) {
                                Toast.makeText(getContext(), "Item with the same QR code already exists", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            DatabaseReference itemsDbRef = userDbRef.child("Items").child(name);

                            itemsDbRef.child("name").setValue(name);
                            itemsDbRef.child("count").setValue(count);
                            itemsDbRef.child("qrcode").setValue(qrcode);

                            Toast.makeText(getContext(), "Item Added", Toast.LENGTH_SHORT).show();

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