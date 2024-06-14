package com.example.inventorymanagement1;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextThemeWrapper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.protobuf.ApiOrBuilder;
import com.example.inventorymanagement1.PopUpMenuHelper;

import java.util.ArrayList;
import java.util.List;
public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ItemViewHolder> {

    private List<Items> itemsList;
    private List<Items> filteredItemsList;
    private Context context;
    private int item_added_quantity, item_initial_quantity, item_final_quantity;


    public ItemsAdapter(Context context, List<Items> itemsList) {
        this.itemsList = itemsList;
        this.filteredItemsList = new ArrayList<>(itemsList);
        this.context = context;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Items items = filteredItemsList.get(position);
        holder.item_name.setText(items.getName());
        holder.item_count.setText(items.getCount());
        holder.item_qr_code.setText(items.getQrcode());

        holder.popup_menu.setOnClickListener(v -> showPopupMenu(v, items, position ));
    }

    private void showPopupMenu(View view, Items items, int position) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.inflate(R.menu.popup_menu);
        PopUpMenuHelper.enablePopupMenuIcons(popupMenu);

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.edit_item) {
                showEditDialog(items, position);
            } else if (id == R.id.delete_item) {
                showDeleteConfirmationDialog(items);
            }
            return false;
        });
        popupMenu.show();
    }

    private void showDeleteConfirmationDialog(Items items) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this item?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            deleteItemFromFirebase(items);
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void showEditDialog(Items items, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_edit_item, null);
        builder.setView(dialogView);

        TextView edit_item_name = dialogView.findViewById(R.id.edit_item_name);
        TextView initial_quantity = dialogView.findViewById(R.id.initial_quantity);
        TextView new_quantity = dialogView.findViewById(R.id.new_quantity);
        EditText edit_item_count = dialogView.findViewById(R.id.edit_item_count);
        //EditText edit_item_qr_code = dialogView.findViewById(R.id.edit_item_qr_code);
        Button button_update = dialogView.findViewById(R.id.button_update);

        ImageView edit_minus = dialogView.findViewById(R.id.edit_minus);
        ImageView edit_plus = dialogView.findViewById(R.id.edit_plus);

        edit_item_name.setText(items.getName());
        initial_quantity.setText(items.getCount());
        new_quantity.setText(items.getCount());
        final int[] num = {0};

        edit_plus.setOnClickListener(v -> {
            num[0]++;
            edit_item_count.setText(String.valueOf(num[0]));
            String edit_item_count_text = edit_item_count.getText().toString().trim();
            String initial_quantity_text = initial_quantity.getText().toString().trim();

            item_added_quantity = Integer.parseInt(edit_item_count_text);
            item_initial_quantity = Integer.parseInt(initial_quantity_text);
            item_final_quantity = item_initial_quantity + item_added_quantity;
            new_quantity.setText(String.valueOf(item_final_quantity));

        });

        edit_minus.setOnClickListener(v -> {
            num[0]--;
            edit_item_count.setText(String.valueOf(num[0]));
            String edit_item_count_text = edit_item_count.getText().toString().trim();
            String initial_quantity_text = initial_quantity.getText().toString().trim();

            item_added_quantity = Integer.parseInt(edit_item_count_text);
            item_initial_quantity = Integer.parseInt(initial_quantity_text);
            item_final_quantity = item_initial_quantity + item_added_quantity;
            new_quantity.setText(String.valueOf(item_final_quantity));

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
                        num[0] = Integer.parseInt(edit_item_count_text);
                        String initial_quantity_text = initial_quantity.getText().toString().trim();

                        if (!edit_item_count_text.isEmpty()) {
                            item_added_quantity = Integer.parseInt(edit_item_count_text);
                            item_initial_quantity = Integer.parseInt(initial_quantity_text);
                            item_final_quantity = item_initial_quantity + item_added_quantity;
                            new_quantity.setText(String.valueOf(item_final_quantity));
                        } else {
                            new_quantity.setText(initial_quantity_text);
                        }
                    } else {
                        new_quantity.setText(initial_quantity.getText().toString().trim());
                    }
                } catch (NumberFormatException e ){
                    num[0] = 0;
                    new_quantity.setText(initial_quantity.getText().toString().trim());
                }
            }
        });


        AlertDialog dialog = builder.create();

        button_update.setOnClickListener(v -> {
            try {
            //String itemName = edit_item_name.getText().toString().trim().toLowerCase();
            item_added_quantity = Integer.parseInt(edit_item_count.getText().toString().trim());
            item_initial_quantity = Integer.parseInt(initial_quantity.getText().toString().trim());
            item_final_quantity = item_initial_quantity + item_added_quantity;
                if (item_final_quantity >= 0){
                    new_quantity.setText(String.valueOf(item_final_quantity));
                    String itemCount = String.valueOf(item_final_quantity);
                    if (!itemCount.isEmpty()) {
                        items.setCount(itemCount);

                        updateItemInFirebase(items.getName(), itemCount);
                        dialog.dismiss();
                    } else {
                        Toast.makeText(context, "Please enter a quantity", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Item Quantity cannot be negative", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Please enter a valid quantity", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }


    private void deleteItemFromFirebase(Items items) {
        if (items == null || items.getQrcode() == null || items.getQrcode().isEmpty()) {
            Log.e("ItemsAdapter", "Item or Item ID is null or empty");
            Toast.makeText(context, "Failed to delete item: Invalid item data", Toast.LENGTH_SHORT).show();
            return;
        }

        String emailHeader = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",");
        DatabaseReference userDbRef = FirebaseDatabase.getInstance().getReference().child("Users").child(emailHeader).child("Items").child(items.getName());

        userDbRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                itemsList.remove(items);
                notifyDataSetChanged();
            } else {
                Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateItemInFirebase(String qrcode, String itemCount) {

        String emailHeader = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
        DatabaseReference userDbRef = FirebaseDatabase.getInstance().getReference().child("Users").child(emailHeader).child("Items").child(qrcode);

        userDbRef.child("count").setValue(itemCount).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Item updated successfully", Toast.LENGTH_SHORT).show();
                notifyDataSetChanged();
            } else {
                Toast.makeText(context, "Failed to update item", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return filteredItemsList.size();
    }

    public void updateItemsList(List<Items> newItemsList) {
        this.itemsList = newItemsList;
        this.filteredItemsList = new ArrayList<>(newItemsList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredItemsList.clear();
        if (query.isEmpty()) {
            filteredItemsList.addAll(itemsList);
        } else {
            for (Items items : itemsList) {
                if (items.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredItemsList.add(items);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView item_name, item_count, item_qr_code;
        ImageView popup_menu;

        public ItemViewHolder(@NonNull View itemsView) {
            super(itemsView);
            item_name = itemsView.findViewById(R.id.item_name);
            item_count = itemsView.findViewById(R.id.item_count);
            item_qr_code = itemsView.findViewById(R.id.item_qr_code);
            popup_menu = itemsView.findViewById(R.id.popup_menu);
        }
    }
}
