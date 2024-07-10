package com.example.inventorymanagement1;

import static androidx.core.app.ActivityCompat.invalidateOptionsMenu;

import android.content.ClipData;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProjectAdapter projectAdapter;
    private List<Project> projectList;
    private SearchView searchView;
    private ProjectAdapter.OnProjectSelectedListener listener;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        searchView = view.findViewById(R.id.search_view_project);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        /*
        itemsList = new ArrayList<>();
        itemsAdapter = new ItemsAdapter(getContext(), itemsList);
        recyclerView.setAdapter(itemsAdapter);
         */

        projectList = new ArrayList<>();
        projectAdapter = new ProjectAdapter(getActivity(), projectList, this::onProjectSelected);
        recyclerView.setAdapter(projectAdapter);

        loadProjectFromFirebase();
        setupSearchView();

        return view;
    }

    private void onProjectSelected(String projectName) {
        ProjectDetailsFragment fragment = new ProjectDetailsFragment();
        Bundle args = new Bundle();
        args.putString("project_name", projectName);
        fragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
        requireActivity().invalidateOptionsMenu();
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                projectAdapter.filter(newText);
                return true;
            }
        });
    }

    private void loadProjectFromFirebase() {
        String emailHeader = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",");
        DatabaseReference projectRef = FirebaseDatabase.getInstance().getReference().child("Users").child(emailHeader).child("Project");

        projectRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                projectList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Project project = snapshot.getValue(Project.class);
                    projectList.add(project);
                    /*
                    if (items != null) {
                        itemsList.add(items);
                    }
                     */
                }
                projectAdapter.updateProjectList(projectList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load items", Toast.LENGTH_SHORT).show();
            }
        });
    }
}