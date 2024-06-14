package com.example.inventorymanagement1;

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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ItemsAdapter itemsAdapter;
    private List<Items> itemsList;
    private SearchView searchView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        searchView = view.findViewById(R.id.search_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        itemsList = new ArrayList<>();
        itemsAdapter = new ItemsAdapter(getContext(), itemsList);
        recyclerView.setAdapter(itemsAdapter);

        loadItemsFromFirebase();
        setupSearchView();

        return view;
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                itemsAdapter.filter(newText);
                return true;
            }
        });
    }

    private void loadItemsFromFirebase() {
        String emailHeader = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",");
        DatabaseReference userDbRef = FirebaseDatabase.getInstance().getReference().child("Users").child(emailHeader).child("Items");

        userDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemsList.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    Items items = itemSnapshot.getValue(Items.class);
                    if (items != null) {
                        itemsList.add(items);
                    }
                }
                itemsAdapter.updateItemsList(itemsList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load items", Toast.LENGTH_SHORT).show();
            }
        });
    }
}