package com.technion.doggyguide;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class OrganizationSignUp extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText emailtxt;
    private EditText pwdtxt;
    private EditText pwdconfirmtxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_sign_up);
        getSupportActionBar().setTitle("Sign Up");
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_up_button);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Initialize buttons
        emailtxt = findViewById(R.id.organizationemail);
        pwdtxt = findViewById(R.id.organizationpassword);
        pwdconfirmtxt = findViewById(R.id.organizationpasswordconfirmation);
        //Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    public void signUpbtnHandler(View view) {
        String email = emailtxt.getText().toString();
        String pwd = pwdtxt.getText().toString();
        String pwdconfirm = pwdconfirmtxt.getText().toString();
        if (!validateSignup(email, pwd, pwdconfirm))
            return;
        signUpWithEmailAndPassword(email, pwd);
    }

    private void signUpWithEmailAndPassword(String email, String pwd) {
        mAuth.createUserWithEmailAndPassword(email, pwd).
                addOnCompleteListener(OrganizationSignUp.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Sign up succeded
                            mAuth.getCurrentUser().sendEmailVerification().
                                    addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(OrganizationSignUp.this, "Please check your email for verification.",
                                                        Toast.LENGTH_SHORT).show();
//                                                FirebaseAuth.getInstance().signOut();
//                                                finish();
                                                //TODO: insert the user id to the organizations' database
                                                Intent intent = new Intent(OrganizationSignUp.this, MainActivity.class);
                                                startActivity(intent);
                                            } else {
                                                Toast.makeText(OrganizationSignUp.this, task.getException().getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(OrganizationSignUp.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("exception", e.getMessage());
            }
        });
    }

    private boolean validateSignup(String email, String pwd, String pwdconfirm) {
        if (email.isEmpty()) {
            emailtxt.setError("Please enter an email address.");
            emailtxt.requestFocus();
            return false;
        }
        if (pwd.isEmpty()) {
            pwdtxt.setError("Please enter a password.");
            pwdtxt.requestFocus();
            return false;
        }
        if (pwd.isEmpty()) {
            pwdconfirmtxt.setError("Please enter a password confirmation.");
            pwdconfirmtxt.requestFocus();
            return false;
        } else if (!pwd.equals(pwdconfirm)) {
            pwdconfirmtxt.setError("The confirmation password doesn't match.");
            pwdconfirmtxt.requestFocus();
            return false;
        }
        return true;
    }


    @Override
    public void onStart() {
        super.onStart();
    }
}
