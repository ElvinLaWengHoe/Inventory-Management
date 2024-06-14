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
public class EditItemActivityTest extends AppCompatActivity {

    private EditText edit_item_count;
    private TextView item_name, initial_quantity, new_quantity;
    private Button button_update, button_delete;
    private ImageView edit_minus, edit_plus;
    private int item_quantity, item_quantity_2, item_added_quantity, item_initial_quantity, item_final_quantity;
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
        initial_quantity = findViewById(R.id.initial_quantity);
        new_quantity = findViewById(R.id.new_quantity);
        edit_item_count = findViewById(R.id.edit_item_count);
        button_update = findViewById(R.id.button_update);
        button_delete = findViewById(R.id.button_delete);
        edit_plus = findViewById(R.id.edit_plus);
        edit_minus = findViewById(R.id.edit_minus);

        Intent intent = getIntent();
        itemName = intent.getStringExtra("item_name");
        String item_count = getIntent().getStringExtra("item_count");

        if (item_name != null) {
            item_name.setText(itemName);
            item_quantity = Integer.parseInt(item_count);
            initial_quantity.setText(String.valueOf(item_quantity));
            new_quantity.setText(String.valueOf(item_quantity));
        } else {
            Toast.makeText(this, "Invalid item data", Toast.LENGTH_SHORT).show();
            finish();
        }

        edit_plus.setOnClickListener(v -> {
            item_quantity_2++;
            edit_item_count.setText(String.valueOf(item_quantity_2));
            updateQuantities();
        });

        edit_minus.setOnClickListener(v -> {
            item_quantity_2--;
            edit_item_count.setText(String.valueOf(item_quantity_2));
            updateQuantities();
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
                String edit_item_count_text = s.toString().trim();

                try {
                    if (!edit_item_count_text.isEmpty()) {
                        item_quantity_2 = Integer.parseInt(edit_item_count_text);
                        updateQuantities();
                    } else {
                        new_quantity.setText(initial_quantity.getText().toString().trim());
                    }
                } catch (NumberFormatException e) {
                    item_quantity_2 = 0;
                    new_quantity.setText(initial_quantity.getText().toString().trim());
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
            try {
                item_added_quantity = Integer.parseInt(edit_item_count.getText().toString().trim());
                item_initial_quantity = Integer.parseInt(initial_quantity.getText().toString().trim());
                item_final_quantity = item_initial_quantity + item_added_quantity;
                if (item_final_quantity >= 0) {
                    new_quantity.setText(String.valueOf(item_final_quantity));
                    String itemCount = String.valueOf(item_final_quantity);
                    if (!itemCount.isEmpty()) {
                        updateItemInFirebase(itemName, itemCount);
                    } else {
                        Toast.makeText(this, "Please enter a quantity", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Item Quantity cannot be negative", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show();
            }
        });

        button_delete.setOnClickListener( v -> {
            showDeleteConfirmationDialog2(itemName);
        });
    }
    private void updateQuantities() {
        String edit_item_count_text = edit_item_count.getText().toString().trim();
        String initial_quantity_text = initial_quantity.getText().toString().trim();

        if (!edit_item_count_text.isEmpty()) {
            item_added_quantity = Integer.parseInt(edit_item_count_text);
            item_initial_quantity = Integer.parseInt(initial_quantity_text);
            item_final_quantity = item_initial_quantity + item_added_quantity;
            new_quantity.setText(String.valueOf(item_final_quantity));
        } else {
            new_quantity.setText(initial_quantity_text);
        }
    }

    private void updateItemInFirebase(String qrcode, String itemCount) {
        String emailHeader = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
        DatabaseReference userDbRef = FirebaseDatabase.getInstance().getReference().child("Users").child(emailHeader).child("Items").child(qrcode);

        userDbRef.child("count").setValue(itemCount).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EditItemActivityTest.this, "Item updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(EditItemActivityTest.this, "Failed to update item", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmationDialog2(String itemName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditItemActivityTest.this);
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


