package com.example.self;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import utils.JournalApi;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText usernameTxt, emailTxt, passwordTxt, confirmPassTxt;
    private Button createAcctBtn;
    private ProgressBar progressBar;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");
    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        firebaseAuth = FirebaseAuth.getInstance();

        usernameTxt = findViewById(R.id.create_acct_username);
        emailTxt = findViewById(R.id.create_acct_email);
        passwordTxt = findViewById(R.id.create_acct_password);
        confirmPassTxt = findViewById(R.id.confirm_create_acct_password);
        createAcctBtn = findViewById(R.id.create_acct_button);
        progressBar = findViewById(R.id.create_acct_progress);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();


            }
        };

        createAcctBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyUserInfo();
            }
        });
    }
    private void verifyUserInfo() {
        String username = usernameTxt.getText().toString();
        String email = emailTxt.getText().toString();
        String pass= passwordTxt.getText().toString();
        String confPass = confirmPassTxt.getText().toString();

        if (TextUtils.isEmpty(username)){
            Toast.makeText(this, "Please enter your Username", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(pass)){
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(confPass)){
            Toast.makeText(this, "Please confirm your password", Toast.LENGTH_SHORT).show();
        }
        else if (!pass.equals(confPass)){
            Toast.makeText(this, "Please ensure your password match", Toast.LENGTH_SHORT).show();
        }
        else
        {
            createUserAccount(username, email, pass);
        }

    }

    private void createUserAccount(String username, String email, String pass) {

        progressBar.setVisibility(View.VISIBLE);

        firebaseAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            currentUser = firebaseAuth.getCurrentUser();
                            String currentUid = currentUser.getUid();

                            HashMap<String, Object> userMap = new HashMap<>();
                            userMap.put("userId", currentUid);
                            userMap.put("userName", username);

                            collectionReference.add(userMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.getResult().exists()){
                                                progressBar.setVisibility(View.INVISIBLE);
                                                String name = task.getResult().getString("userName");

                                                JournalApi journalApi = JournalApi.getInstance();
                                                journalApi.setUserId(currentUid);
                                                journalApi.setUserName(name);



                                                Intent postIntent = new Intent(CreateAccountActivity.this, PostJournalActivity.class);
                                                postIntent.putExtra("userName", name);
                                                postIntent.putExtra("userId", currentUid);
                                                startActivity(postIntent);
                                            }
                                            else
                                            {
                                                progressBar.setVisibility(View.INVISIBLE);
                                            }
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
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);

    }
}