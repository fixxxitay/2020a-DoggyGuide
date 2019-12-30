package com.technion.doggyguide.homeScreen;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.technion.doggyguide.DatePickerFabFragment;
import com.technion.doggyguide.R;
import com.technion.doggyguide.TimePickerFabFragment;
import com.technion.doggyguide.dataElements.PostElement;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Map;


public class Fab extends AppCompatActivity implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {

    private final String TAG = "FAB POST";

    private int clicked_btn_id;

    private EditText postname;
    private TextView postdate;
    private TextView poststarttime;
    private TextView postendtime;
    private EditText postdescription;


    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String userID;

    private CollectionReference postsRef;
    private CollectionReference dogownersRef;
    private CollectionReference friendsRef;
    private CollectionReference userpostsRef;

    //start and end time for the post event
    private String start_time;
    private String end_time;
    private String posting_time;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fab);
        getSupportActionBar().setTitle("Post a request");
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_up_button);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        postname = findViewById(R.id.post_name);
        postdescription = findViewById(R.id.post_description);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userID = mAuth.getCurrentUser().getUid();

        postsRef = db.collection("posts");
        dogownersRef = db.collection("dog owners");
        friendsRef = db.collection("dog owners/" + userID + "/friends");
        userpostsRef = db.collection("dog owners/" + userID + "/posts");

    }

    public void datePickerHandler(View view) {
        clicked_btn_id = R.id.post_datepicker;
        DialogFragment datepicker = new DatePickerFabFragment();
        datepicker.show(getSupportFragmentManager(), "Date Picker");
    }

    public void startTimeHandler(View view) {
        clicked_btn_id = R.id.post_starttimepicker;
        DialogFragment starttimepicker = new TimePickerFabFragment();
        starttimepicker.show(getSupportFragmentManager(), "start time picker");
    }

    public void endTimeHandler(View view) {
        clicked_btn_id = R.id.post_endtimepicker;
        DialogFragment endtimepicker = new TimePickerFabFragment();
        endtimepicker.show(getSupportFragmentManager(), "end time picker");
    }

    public void postbtnHandler(View view) {
        //TODO: create a post and post it to all friends feed
        int startHour = Integer.parseInt(start_time.split(":")[0]);
        int startMinute = Integer.parseInt(start_time.split(":")[1]);
        int endHour = Integer.parseInt(end_time.split(":")[0]);
        int endMinute = Integer.parseInt(end_time.split(":")[1]);
        if (endHour < startHour) {
            Toast.makeText(this,
                    "End time cannot be later than start time!\nTry Again", Toast.LENGTH_LONG).show();
            return;
        } else if (endHour == startHour && endMinute < startMinute) {
            Toast.makeText(this,
                    "End time cannot be later than start time!\nTry Again", Toast.LENGTH_LONG).show();
            return;
        }
        posting_time = Calendar.getInstance().getTime().toString();
        final String postID = userID + posting_time;
        addPostToDatabase(postID);
        Toast.makeText(this,
                "You have successfully posted a request!", Toast.LENGTH_LONG).show();
        finish();
    }


    private void addPostToDatabase(String postID) {
        String name = postname.getText().toString();
        String description = postdescription.getText().toString();
        PostElement post = new PostElement(name, userID, start_time, end_time,
                postdate.getText().toString(), posting_time, description);
        postsRef.document(postID).set(post);
        addPostRefToUser(post, postID);
        addPostRefToFriends(post, postID);
    }

    private void addPostRefToUser(PostElement post, String postID) {
        userpostsRef.document(postID).set(post)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "added post to user successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.getMessage());
                    }
                });
    }

    private void addPostRefToFriends(final PostElement post, final String postID) {
        friendsRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            WriteBatch batch = db.batch();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> friendRef = document.getData();
                                DocumentReference friendDocRef = (DocumentReference) friendRef.get("reference");
                                batch.set(friendDocRef.collection("posts").document(postID), post);

                            }
                        }
                    }
                });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM dd, yyyy");
        String pickeddate = format.format(calendar.getTime());
        postdate = findViewById(R.id.post_date);
        postdate.setText(pickeddate);;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        switch (clicked_btn_id) {
            case R.id.post_starttimepicker:
                poststarttime = findViewById(R.id.post_starttime);
                if (minute < 10) {
                    poststarttime.setText("Starts at " + hourOfDay + ":0" + minute);
                    start_time = hourOfDay + ":0" + minute;
                } else {
                    poststarttime.setText("Starts at " + hourOfDay + ":" + minute);
                    start_time = hourOfDay + ":" + minute;
                }
                break;
            case R.id.post_endtimepicker:
                postendtime = findViewById(R.id.post_endtime);
                if (minute < 10) {
                    postendtime.setText("Ends at " + hourOfDay + ":0" + minute);
                    end_time = hourOfDay + ":0" + minute;
                } else {
                    postendtime.setText("Ends at " + hourOfDay + ":" + minute);
                    end_time = hourOfDay + ":" + minute;
                }
                break;
        }
    }
}
