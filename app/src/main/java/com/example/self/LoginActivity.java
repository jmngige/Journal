package com.example.self;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import utils.JournalApi;

public class LoginActivity extends AppCompatActivity{

    private Button loginButton;
    private Button createAcctButton;
    private EditText emailTxt;
    private EditText passswordTxt;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        loginButton = findViewById(R.id.login_button);
        createAcctButton = findViewById(R.id.login_create_button);
        emailTxt = findViewById(R.id.email_login);
        passswordTxt = findViewById(R.id.login_password);
        progressBar = findViewById(R.id.login_progress);

        createAcctButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));

            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailTxt.getText().toString();
                String pwd = passswordTxt.getText().toString();

                loginEmailPassword(email, pwd);
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loginEmailPassword(String email, String pwd) {

        if (TextUtils.isEmpty(email) && TextUtils.isEmpty(pwd)){
            Toast.makeText(this, "Please fill the email and password", Toast.LENGTH_SHORT).show();
        }
        else {

            firebaseAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    String currentUid = user.getUid();

                    collectionReference.whereEqualTo("userId", currentUid).addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (!value.isEmpty()){
                                for (QueryDocumentSnapshot snapshot : value){
                                    JournalApi journalApi = new JournalApi();
                                    journalApi.setUserName(snapshot.getString("userName"));
                                    journalApi.setUserId(snapshot.getString("userId"));
                                    progressBar.setVisibility(View.INVISIBLE);
                                    startActivity(new Intent(LoginActivity.this, JournalListActivity.class));
                                }
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
}