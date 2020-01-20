package com.technion.doggyguide.users;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.technion.doggyguide.Adapters.UsersAdapter;
import com.technion.doggyguide.Adapters.UsersCustomAdapter;
import com.technion.doggyguide.R;
import com.technion.doggyguide.dataElements.DogOwnerElement;
import com.technion.doggyguide.dataElements.Users;

import android.os.Bundle;
import android.view.MenuItem;


public class UsersActivity extends AppCompatActivity {

    private CollectionReference mUsersRef;
    private UsersCustomAdapter mAdapter;
    String mDogOwners = "dogOwners";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        String filter = getIntent().getStringExtra("filter");
        getSupportActionBar().setTitle("Search results for " + filter);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_up_button);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        mUsersRef = db.collection(mDogOwners);

        setUpRecyclerView(filter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpRecyclerView(final String filter) {
        Query query = mUsersRef.orderBy("mName", Query.Direction.ASCENDING);
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        mAdapter = new UsersCustomAdapter(task.getResult().toObjects(DogOwnerElement.class));
                        mAdapter.getFilter().filter(filter);
                        RecyclerView mUsersListRecycleView = findViewById(R.id.recyclerView_id);
                        mUsersListRecycleView.setHasFixedSize(true);
                        mUsersListRecycleView.setLayoutManager(new LinearLayoutManager(UsersActivity.this));
                        mUsersListRecycleView.setAdapter(mAdapter);
                    }
                });


    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
