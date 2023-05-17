package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerificationActivity extends AppCompatActivity {

    private static final String TAG = "VerificationActivity";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        mAuth = FirebaseAuth.getInstance();

        // Check if the user is already signed in
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            if (user.isEmailVerified()) {
                // User's email is already verified
                redirectToMainActivity();
            } else {
                // User's email is not verified, send another verification email
                sendVerificationEmail(user);
            }
        } else {
            // No user is currently signed in
            redirectToLoginActivity();
        }
    }

    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Verification email sent");
                            // Show a message to the user or perform any other desired action
                        } else {
                            Log.e(TAG, "Failed to send verification email", task.getException());
                            // Show an error message to the user or perform any other desired action
                        }
                    }
                });
    }

    private void redirectToMainActivity() {
        // Redirect the user to the main activity
        Intent intent = new Intent(VerificationActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void redirectToLoginActivity() {
        // Redirect the user to the login activity
        Intent intent = new Intent(VerificationActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
