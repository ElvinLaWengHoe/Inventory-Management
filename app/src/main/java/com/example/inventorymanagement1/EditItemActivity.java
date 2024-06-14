package com.example.inventorymanagement1;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class EditItemActivity extends AppCompatActivity {

    private EditText edit_item_count;
    private TextView item_name;
    private Button button_update, button_delete;
    private ImageView edit_minus, edit_plus;
    private int item_quantity;
    private String qrcode, itemName;
    private Context context;
    private List<Items> itemsList;
    private ItemsAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_item);

        item_name = findViewById(R.id.item_name);
        edit_item_count = findViewById(R.id.edit_item_count);
        button_update = findViewById(R.id.button_update);
        button_delete = findViewById(R.id.button_delete);
        edit_plus = findViewById(R.id.edit_plus);
        edit_minus = findViewById(R.id.edit_minus);

        Intent intent = getIntent();
        itemName = intent.getStringExtra("item_name");
        String item_count = getIntent().getStringExtra("item_count");

        if (item_name != null && edit_item_count != null) {
            item_name.setText(itemName);
            item_quantity = Integer.parseInt(item_count);
            edit_item_count.setText(String.valueOf(item_quantity));
        } else {
            Toast.makeText(this, "Invalid item data", Toast.LENGTH_SHORT).show();
            finish();
        }

        edit_plus.setOnClickListener(v -> {
            item_quantity++;
            edit_item_count.setText(String.valueOf(item_quantity));
        });

        edit_minus.setOnClickListener(v -> {
            if (item_quantity > 0) {
                item_quantity--;
                edit_item_count.setText(String.valueOf(item_quantity));
            }
        });

        edit_item_count.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    item_quantity = Integer.parseInt(s.toString());
                } catch (NumberFormatException e) {
                    item_quantity = 0;
                }
            }
        });

        /*login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
         */

        button_update.setOnClickListener(v -> {
            String itemCount = edit_item_count.getText().toString().trim();
            if (!itemCount.isEmpty()) {
                updateItemInFirebase(itemName, itemCount);
            } else {
                Toast.makeText(this, "Please enter a quantity", Toast.LENGTH_SHORT).show();
            }
        });

        button_delete.setOnClickListener( v -> {
            showDeleteConfirmationDialog2(itemName);
        });
    }

    private void updateItemInFirebase(String qrcode, String itemCount) {
        String emailHeader = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
        DatabaseReference userDbRef = FirebaseDatabase.getInstance().getReference().child("Users").child(emailHeader).child("Items").child(qrcode);

        userDbRef.child("count").setValue(itemCount).addOnCompleteListener(task -> {
           if (task.isSuccessful()) {
               Toast.makeText(EditItemActivity.this, "Item updated successfully", Toast.LENGTH_SHORT).show();
               finish();
           } else {
               Toast.makeText(EditItemActivity.this, "Failed to update item", Toast.LENGTH_SHORT).show();
           }
        });
    }

    private void showDeleteConfirmationDialog2(String itemName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditItemActivity.this);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this item?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            deleteItemFromFirebase(itemName);
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteItemFromFirebase(String itemName) {
        if (itemName == null || itemName.isEmpty()) {
            Log.e("ItemsAdapter", "Item or Item ID is null or empty");
            Toast.makeText(this, "Failed to delete item: Invalid item data", Toast.LENGTH_SHORT).show();
            return;
        }

        String emailHeader = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",");
        DatabaseReference userDbRef = FirebaseDatabase.getInstance().getReference().child("Users").child(emailHeader).child("Items").child(itemName);

        userDbRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                removeItemFromList(itemName);
            } else {
                Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeItemFromList(String itemName) {
        if (itemsList != null && adapter != null) {
            for (int i = 0; i < itemsList.size(); i++) {
                if (itemsList.get(i).getName().equals(itemName)) {
                    itemsList.remove(i);
                    adapter.notifyItemRemoved(i);
                    break;
                }
            }
        }
    }
}