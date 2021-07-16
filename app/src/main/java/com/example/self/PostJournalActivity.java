package com.example.self;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;

import model.Journal;
import utils.JournalApi;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {

    private Button saveButton;
    private ProgressBar progressBar;
    private EditText postTitle, postDesc;
    private TextView postUserName, postDate;
    private ImageView postImage, userPostImage;

    private Uri imageUri;

    private String currentUid;
    private String currentUserName;
    private String title, thought;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private CollectionReference collectionReference = db.collection("Journal");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);

        Toolbar toolbar = findViewById(R.id.postJournal_toolbar);
        setSupportActionBar(toolbar);

        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        saveButton = findViewById(R.id.post_save_button);
        userPostImage = findViewById(R.id.user_post_image);

        progressBar = findViewById(R.id.post_progressBar);
        postImage = findViewById(R.id.post_camera_button);
        postUserName = findViewById(R.id.post_username);
        postDate = findViewById(R.id.post_date);
        postDesc = findViewById(R.id.post_desc);
        postTitle = findViewById(R.id.post_title);

        saveButton.setOnClickListener(this);
        postImage.setOnClickListener(this);

        if (JournalApi.getInstance() != null){
            currentUserName = JournalApi.getInstance().getUserName();
            currentUid = JournalApi.getInstance().getUserId();

            postUserName.setText(currentUserName);
        }

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null){

                }
                else
                {

                }
            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();

        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.post_camera_button:
                galleryIntent();
                break;
            case R.id.post_save_button:
                saveInfo();
                break;
        }
    }

    private void saveInfo() {

        title = postTitle.getText().toString().trim();
        thought = postDesc.getText().toString();

        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(thought) && imageUri != null){
            saveImageToStorage();
        }
        else
        {
            Toast.makeText(this, "Ensure you've filled and selected a photo", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageToStorage() {
        progressBar.setVisibility(View.VISIBLE);
        StorageReference filePath = storageReference.child("Journal Images")
                .child("my_image_" + Timestamp.now().getSeconds());

        filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String imageUrl = uri.toString();

                        Journal journal = new Journal();
                        journal.setUserName(currentUserName);
                        journal.setUserId(currentUid);
                        journal.setImageUrl(imageUrl);
                        journal.setTitle(title);
                        journal.setThought(thought);
                        journal.setTimeAdded(new Timestamp(new Date()));

                        collectionReference.add(journal).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                progressBar.setVisibility(View.INVISIBLE);

                                startActivity(new Intent(PostJournalActivity.this, JournalListActivity.class));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.INVISIBLE);

            }
        });
    }

    private void galleryIntent() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();
            userPostImage.setImageURI(imageUri);
        }
    }
}