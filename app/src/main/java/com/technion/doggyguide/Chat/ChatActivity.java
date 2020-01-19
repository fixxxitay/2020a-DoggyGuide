package com.technion.doggyguide.Chat;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.net.InternetDomainName;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.technion.doggyguide.R;
import com.technion.doggyguide.profile.UserProfileActivity;
import com.theartofdev.edmodo.cropper.CropImage;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;


public class ChatActivity extends AppCompatActivity {
    private TextView mTitleView;
    private TextView mStatusView;
    private CircleImageView mProfileImage;
    private ImageButton mChatAddBtn;
    private ImageButton mChatSendBtn;
    private EditText mChatMessageView;
    private String mChatUser;
    private String mChatUserId;
    private String mUserStatus;
    private String mUserImage;
    private String mCurrentUserId;
    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference chatRef = db.collection("Chat");
    private CollectionReference messagesRef = db.collection("messages");
    private static final int GALLERY_PICK = 1;
    FirebaseAuth users = FirebaseAuth.getInstance();
    private String mCurrentUserUid = users.getCurrentUser().getUid();
    CollectionReference mFriendsRef = db.collection("messages")
            .document(mCurrentUserUid)
            .collection("friends");
    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mMessagesListRecycleView;
    private MessageAdapter mAdapter;
    // Storage Firebase
    private StorageReference mImageStorage;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initToolBar();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        // ---- Custom Action bar Items ----
        mTitleView = findViewById(R.id.custom_bar_title);
        mStatusView = findViewById(R.id.custom_bar_status);
        mProfileImage = findViewById(R.id.custom_bar_image);
        mRefreshLayout = findViewById(R.id.message_swipe_layout);
        setInformationForToolBar();

        mChatSendBtn = findViewById(R.id.chat_send_btn);
        mChatMessageView = findViewById(R.id.chat_message_view);
        mChatAddBtn = findViewById(R.id.chat_add_btn);


        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        initChatForThisUser();
        setUpRecyclerView();
        loadMessages();

        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = mChatMessageView.getText().toString();
                sendMessage(message, "text");
            }
        });
        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

            }
        });


        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mMessagesListRecycleView.
                        smoothScrollToPosition(mAdapter.getItemCount());
                mRefreshLayout.setRefreshing(false);


            }
        });
    }

    private void initChatForThisUser() {
        chatRef.document(mCurrentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot != null){
                    chatRef.document(mCurrentUserId)
                            .collection("friends")
                            .document(mChatUserId).get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (!document.exists()) {
                                    Map<String, Object> chatAddMap = new HashMap<>();
                                    chatAddMap.put("seen", false);
                                    chatAddMap.put("time", FieldValue.serverTimestamp());
                                    chatRef.document(mCurrentUserId)
                                            .collection("friends")
                                            .document(mChatUserId)
                                            .set(chatAddMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Map<String, Object> chatAddMap = new HashMap<>();
                                            chatAddMap.put("seen", false);
                                            chatAddMap.put("time", FieldValue.serverTimestamp());
                                            chatRef.document(mChatUserId)
                                                    .collection("friends")
                                                    .document(mCurrentUserId )
                                                    .set(chatAddMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) { }                                            });
                                        }
                                    });
                                } else { }
                            } else { }
                        }
                    });
                }
            }
        });
    }

    private void setInformationForToolBar() {
        mChatUserId = getIntent().getStringExtra("user_id");
        mChatUser  = getIntent().getStringExtra("user_name");
        mTitleView.setText(mChatUser );
        mUserStatus = getIntent().getStringExtra("user_status");
        mStatusView.setText(mUserStatus);
        mUserImage = getIntent().getStringExtra("user_image");
        Picasso.get().load(mUserImage).into(mProfileImage);
    }

    private void initToolBar() {
        Toolbar mChatToolbar = findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);
        actionBar.setDisplayShowTitleEnabled(false);
    }

    private void sendMessage(String message, String type){
        if(!TextUtils.isEmpty(message)){
            final Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", type);
            messageMap.put("time", FieldValue.serverTimestamp());
            messageMap.put("from", mCurrentUserId);

            messagesRef.document(mCurrentUserId)
                    .collection("friends")
                    .document(mChatUserId).
                    collection("messages")
                    .document().set(messageMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    messagesRef.document(mChatUserId )
                            .collection("friends")
                            .document(mCurrentUserId).
                            collection("messages")
                            .document().set(messageMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mChatMessageView.setText("");
                        }
                    });
                }
            });
            chatRef.document(mCurrentUserId)
                    .collection("friends")
                    .document(mChatUserId).update("time", FieldValue.serverTimestamp()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    chatRef.document(mCurrentUserId)
                            .collection("friends")
                            .document(mChatUserId).update("seen", true).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            chatRef.document(mChatUserId )
                                    .collection("friends")
                                    .document(mCurrentUserId).update("time", FieldValue.serverTimestamp()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    chatRef.document(mChatUserId )
                                            .collection("friends")
                                            .document(mCurrentUserId).update("seen", false).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            loadMessages();
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            });
        }
    }


    private void setUpRecyclerView() {
        Query query = mFriendsRef.document(mChatUserId)
                .collection("messages")
                .orderBy("time", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<Messages> options = new FirestoreRecyclerOptions.Builder<Messages>()
                .setQuery(query, Messages.class)
                .build();

        mAdapter = new MessageAdapter(options);
        mMessagesListRecycleView = findViewById(R.id.MessagesRecyclerView_id);
        mMessagesListRecycleView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mMessagesListRecycleView.setLayoutManager(linearLayoutManager);
        mMessagesListRecycleView.setAdapter(mAdapter);
        mAdapter.startListening();

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private void loadMessages() {
        messagesRef.document(mCurrentUserId)
                .collection("friends")
                .document(mChatUserId)
                .collection("messages").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                mMessagesListRecycleView.
                        smoothScrollToPosition(mAdapter.getItemCount());
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                Uri resultUri = result.getUri();
                //compressedImage
                File filePath = new File(resultUri.getPath());
                final byte[] data_;
                data_ = compressedImage(filePath);
                long res = generateRandom(10);
                final StorageReference thumbs_filepath = mStorageRef.child("uploads")
                        .child(mCurrentUserId + mChatUserId + (res) + ".jpg");

                thumbs_filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            UploadTask uploadTask = thumbs_filepath.putBytes(data_);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> compressed_task) {
                                    if (compressed_task.isSuccessful()) {
                                        if (compressed_task.getResult() != null) {
                                            thumbs_filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    sendMessage(uri.toString(), "image");
                                                }
                                            });
                                        }
                                    } else {

                                    }
                                }
                            });
                        } else {

                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public static long generateRandom(int length) {
        Random random = new Random();
        char[] digits = new char[length];
        digits[0] = (char) (random.nextInt(9) + '1');
        for (int i = 1; i < length; i++) {
            digits[i] = (char) (random.nextInt(10) + '0');
        }
        return Long.parseLong(new String(digits));
    }

    private byte[] compressedImage(File filePath) {
        byte[] data_;
        Bitmap compressedImageBitmap = null;
        try {
            compressedImageBitmap = new Compressor(this)
                    .setMaxWidth(200)
                    .setMaxHeight(200)
                    .setQuality(75)
                    .compressToBitmap(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        data_ = baos.toByteArray();
        return data_;
    }

}


