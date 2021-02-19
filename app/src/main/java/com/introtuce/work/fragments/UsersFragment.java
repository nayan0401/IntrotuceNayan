package com.introtuce.work.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.introtuce.work.Model.UserModel;
import com.introtuce.work.R;
import com.introtuce.work.UsersAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class UsersFragment extends Fragment {

    private TextView noUsersText;
    private RecyclerView usersRecyclerView;
    public static List<UserModel> usersList = new ArrayList<>();
    ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        noUsersText = view.findViewById(R.id.no_users_text);
        usersRecyclerView = view.findViewById(R.id.users_recycler_view);
        usersRecyclerView.setHasFixedSize(true);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading users...");

        getUsers();
        return view;
    }

    private void getUsers() {
        usersList.clear();
        progressDialog.show();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                UserModel model = new UserModel();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    model = snapshot.getValue(UserModel.class);
                    usersList.add(model);
                }
                if (usersList.size() == 0) {
                    noUsersText.setVisibility(View.VISIBLE);
                    usersRecyclerView.setVisibility(View.GONE);
                } else {
                    noUsersText.setVisibility(View.GONE);
                    usersRecyclerView.setVisibility(View.VISIBLE);
                    UsersAdapter adapter = new UsersAdapter(getContext(), usersList);
                    Collections.reverse(usersList);
                    usersRecyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
                progressDialog.dismiss();
            }
        });
    }
}