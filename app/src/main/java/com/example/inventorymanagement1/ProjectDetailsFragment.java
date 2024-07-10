package com.example.inventorymanagement1;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import kotlin.collections.ArrayDeque;

public class ProjectDetailsFragment extends Fragment {

    private RecyclerView recyclerView;
    private SearchView searchView;
    private ItemsAdapter itemsAdapter;
    private List<Items> itemsList;
    private String projectName;

    public ProjectDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            projectName = getArguments().getString("project_name");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_details, container, false);

        searchView = view.findViewById(R.id.search_view_items);

        recyclerView = view.findViewById(R.id.recyclerView_items);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        itemsList = new ArrayList<>();

        itemsAdapter = new ItemsAdapter(getActivity(), itemsList, projectName);
        recyclerView.setAdapter(itemsAdapter);

        loadItemsFromFirebase();
        setupSearchView();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() != null && getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setTitle(projectName);
            }
        }
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (itemsAdapter != null) {
                    itemsAdapter.filter(newText);
                }
                return true;
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_item, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_add_item) {
            AddItemFragment fragment = new AddItemFragment();

            Bundle args = new Bundle();
            args.putString("project_name", projectName);
            fragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadItemsFromFirebase() {
        String emailHeader = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference().child("Users").child(emailHeader)
                .child("Project").child(projectName).child("Items");

        itemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                itemsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Items item = snapshot.getValue(Items.class);
                    Log.d("ProjectDetailsFragment", "Item: " + item.getName() + ", Count: " + item.getCount());
                    itemsList.add(item);
                }
                itemsAdapter.updateItemsList(itemsList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}