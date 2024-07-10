package com.example.inventorymanagement1;

import static androidx.core.app.ActivityCompat.invalidateOptionsMenu;

import android.content.Context;
import android.content.Intent;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {
    private List<Project> projectList;
    private List<Project> filteredProjectList;
    private Context context;
    private OnProjectSelectedListener listener;


    public ProjectAdapter(Context context, List<Project> projectList, OnProjectSelectedListener listener) {
        this.projectList = projectList;
        this.filteredProjectList = new ArrayList<>(projectList);
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_layout, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = filteredProjectList.get(position);
        holder.projectName.setText(project.getProject_name());
        holder.projectQrCode.setText(project.getQrCode());

        Log.d("ProjectAdapter", "Project Name: " + project.getProject_name());
        Log.d("ProjectAdapter", "QR Code: " + project.getQrCode());

        holder.itemView.setOnClickListener(v -> listener.onProjectSelected(project.getProject_name()));
        holder.popup_menu_project.setOnClickListener(v -> showPopupMenu_project(v, project, position ));

        /*
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProjectDetailsFragment.class);
            intent.putExtra("project_name", project.getName());
            context.startActivity(intent);
        });

         */
    }

    private void showPopupMenu_project(View view, Project project, int position) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.inflate(R.menu.popup_project_menu);
        PopUpMenuHelper.enablePopupMenuIcons(popupMenu);

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.rename_project) {
                showRenameDialog(project, position);
            } else if (id == R.id.delete_project) {
                showDeleteConfirmationDialog(project);
            }
            return false;
        });
        popupMenu.show();
    }

    private void showDeleteConfirmationDialog(Project project) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this item?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            deleteProjectFromFirebase(project);
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showRenameDialog(Project project, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_rename_project, null);
        builder.setView(dialogView);

        TextInputEditText rename_project = dialogView.findViewById(R.id.rename_project);
        Button button_save_rename = dialogView.findViewById(R.id.button_save_rename);

        rename_project.setText(project.getProject_name());

        AlertDialog dialog = builder.create();

        button_save_rename.setOnClickListener(view -> {
            String newName = rename_project.getText().toString().trim().toLowerCase();
            if (!newName.isEmpty() && !newName.equals(project.getProject_name().toLowerCase())) {
                renameProjectInFirebase(project, newName, position);
                dialog.dismiss();
            } else {
                Toast.makeText(context, "Invalid Project Name", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void renameProjectInFirebase(Project project, String newName, int position) {
        String emailHeader = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
        DatabaseReference userDbRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(emailHeader).child("Project");

        DatabaseReference oldProjectRef = userDbRef.child(project.getProject_name());

        DatabaseReference newProjectRef = userDbRef.child(newName);

        oldProjectRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Object> projectData = (Map<String, Object>) dataSnapshot.getValue();
                    if (projectData != null) {
                        projectData.put("project_name", newName);

                        newProjectRef.setValue(projectData).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                oldProjectRef.removeValue().addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        project.setProject_name(newName);
                                        projectList.set(position, project);
                                        notifyDataSetChanged();
                                        Toast.makeText(context, "Name saved successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context, "Failed to remove old project", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(context, "Failed to create new project entry", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(context, "Failed to retrieve project data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Old project data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to read old project data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteProjectFromFirebase(Project project) {
        if (project == null || project.getProject_name() == null || project.getProject_name().isEmpty()) {
            Log.e("ProjectAdapter", "Item or Item ID is null or empty");
            Toast.makeText(context, "Failed to delete project: Invalid project data", Toast.LENGTH_SHORT).show();
            return;
        }

        String emailHeader = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",");
        DatabaseReference userDbRef = FirebaseDatabase.getInstance().getReference().child("Users").child(emailHeader)
                .child("Project").child(project.getProject_name());

        userDbRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                projectList.remove(project);
                notifyDataSetChanged();
            } else {
                Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredProjectList.size();
    }

    public void updateProjectList(List<Project> newProjectList) {
        this.projectList = newProjectList;
        this.filteredProjectList = new ArrayList<>(newProjectList);
        notifyDataSetChanged();


    }

    public interface OnProjectSelectedListener {
        void onProjectSelected(String projectName);
    }

    public void filter(String query) {
        filteredProjectList.clear();
        if (query.isEmpty()) {
            filteredProjectList.addAll(projectList);
        } else {
            for (Project project : projectList) {
                if (project.getProject_name().toLowerCase().contains(query.toLowerCase())) {
                    filteredProjectList.add(project);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView projectName, projectQrCode;
        ImageView popup_menu_project;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            projectName = itemView.findViewById(R.id.project_name);
            projectQrCode = itemView.findViewById(R.id.project_qr_code);
            popup_menu_project = itemView.findViewById(R.id.popup_menu_project);
        }
    }
}
