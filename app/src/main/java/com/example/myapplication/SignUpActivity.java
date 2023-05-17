package com.example.myapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.Query;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;
import android.content.SharedPreferences;

import java.util.concurrent.TimeUnit;

public class SignUpActivity extends AppCompatActivity {

    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private EditText mConfirmPasswordEditText;
    private EditText mPhoneNumberEditText;
    private EditText mFirstNameEditText;
    private EditText mLastNameEditText;
    private EditText mDobEditText;
    private Button mSignUpButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Button mLoginButton;
    private EditText mDisplayNameEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mEmailEditText = findViewById(R.id.email_edit_text);
        mPasswordEditText = findViewById(R.id.password_edit_text);
        mConfirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        mPhoneNumberEditText = findViewById(R.id.phone_number_edit_text);
        mFirstNameEditText = findViewById(R.id.first_name_edit_text);
        mLastNameEditText = findViewById(R.id.last_name_edit_text);
        mDobEditText = findViewById(R.id.date_of_birth_edit_text);
        mSignUpButton = findViewById(R.id.sign_up_button);
        mDisplayNameEditText = findViewById(R.id.display_name_edit_text);



        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmailEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();
                String confirmPassword = mConfirmPasswordEditText.getText().toString();
                String phoneNumber = mPhoneNumberEditText.getText().toString();
                String firstName = mFirstNameEditText.getText().toString();
                String lastName = mLastNameEditText.getText().toString();
                String dob = mDobEditText.getText().toString();
                String displayName = mDisplayNameEditText.getText().toString();

                if (TextUtils.isEmpty(firstName)) {
                    Toast.makeText(SignUpActivity.this, "Please enter your first name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(lastName)) {
                    Toast.makeText(SignUpActivity.this, "Please enter your last name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(dob)) {
                    Toast.makeText(SignUpActivity.this, "Please enter your date of birth", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(SignUpActivity.this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(SignUpActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(displayName)) {
                    Toast.makeText(SignUpActivity.this, "Please enter a display name", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if display name already exists
                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                Query query = usersRef.orderByChild("displayName").equalTo(displayName);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Check if display name already exists in the database
                        if (snapshot.exists()) {
                            // Display name is already taken, display error message
                            Toast.makeText(getApplicationContext(), "Display name is already taken!", Toast.LENGTH_SHORT).show();
                        } else {
                            // Display name is unique, continue with email validation
                            if (TextUtils.isEmpty(email)) {
                                Toast.makeText(SignUpActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                                return;
                            }

                // Check if email already exists
                mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        boolean isNewUser = task.getResult().getSignInMethods().isEmpty();

                        if (!isNewUser) {
                            Toast.makeText(SignUpActivity.this, "Email already exists, please use a different email", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            // Email is unique, continue with phone number validation
                            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                            Query query = usersRef.orderByChild("phoneNumber").equalTo(phoneNumber);

                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    // Check if phone number already exists in the database
                                    if (snapshot.exists()) {
                                        // Phone number is already registered, display error message
                                        Toast.makeText(getApplicationContext(), "Phone number already exists!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Phone number is unique, continue with account registration
                                        User newUser = new User(email, phoneNumber, password, firstName, lastName, dob, displayName);
                                        DatabaseReference newUserRef = FirebaseDatabase.getInstance().getReference("users").push();
                                        newUserRef.setValue(newUser);
                                        // Display success message and go to login activity
                                        Toast.makeText(getApplicationContext(), "Account created successfully!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();

                                        mAuth.createUserWithEmailAndPassword(email, password)
                                                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                                        FirebaseUser user = mAuth.getCurrentUser(); // Move the declaration here
                                                        if (task.isSuccessful()) {
                                                            // Step 2: Send a verification email
                                                            if (user != null) {
                                                                user.sendEmailVerification()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    // Verification email sent successfully
                                                                                    Toast.makeText(SignUpActivity.this, "Registration successful! Please check your email for verification.", Toast.LENGTH_LONG).show();                                                                    } else {
                                                                                    // Failed to send verification email
                                                                                    // Handle the error or show an error message to the user
                                                                                    Toast.makeText(SignUpActivity.this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
                                                                                    // Cancel the registration
                                                                                    if (user != null) {
                                                                                        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                // Account deleted, show appropriate message or take necessary action
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }
                                                                            }
                                                                        });
                                                            }

                                                            // Step 3: Phone number verification
                                                            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                                                    phoneNumber, // e.g., +1234567890
                                                                    60, // Timeout duration
                                                                    TimeUnit.SECONDS,
                                                                    SignUpActivity.this,
                                                                    new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                                                        @Override
                                                                        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                                                            // Auto-retrieval or instant verification is successful
                                                                            // Proceed with signing in the user
                                                                        }

                                                                        @Override
                                                                        public void onVerificationFailed(@NonNull FirebaseException e) {
                                                                            // Verification failed
                                                                            // Handle the error or show an error message to the user
                                                                            Toast.makeText(SignUpActivity.this, "Phone number verification failed", Toast.LENGTH_SHORT).show();
                                                                            // Cancel the registration
                                                                            if (user != null) {
                                                                                user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        // Account deleted, show appropriate message or take necessary action
                                                                                    }
                                                                                });
                                                                            }
                                                                        }

                                                                        @Override
                                                                        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                                                            Toast.makeText(SignUpActivity.this, "Verification code sent to your phone number. Please check your phone.", Toast.LENGTH_LONG).show();                                                                // Save the verification ID and token for later use
                                                                            // Save the verification ID and token for later use
                                                                            // You can store them in SharedPreferences or in global variables
                                                                            // For example:
                                                                            // Save the verification ID
                                                                            SharedPreferences preferences = getSharedPreferences("VerificationPrefs", MODE_PRIVATE);
                                                                            SharedPreferences.Editor editor = preferences.edit();
                                                                            editor.putString("verificationId", verificationId);
                                                                            editor.apply();

                                                                            // Proceed to the verification screen
                                                                            Intent intent = new Intent(SignUpActivity.this, VerificationActivity.class);
                                                                            startActivity(intent);
                                                                        }
                                                                    });
                                                        } else {
                                                            // Failed to create a new user account
                                                            // Handle the error or show an error message to the user
                                                            Toast.makeText(SignUpActivity.this, "Failed to create a new user account", Toast.LENGTH_SHORT).show();

                                                            // Cancel the registration
                                                            user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        // Account deleted successfully
                                                                        // Show appropriate message or take necessary action
                                                                    } else {
                                                                        // Failed to delete the account
                                                                        // Show appropriate message or take necessary action
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                    }

                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    // Handle the error or show an error message to the user
                                    Toast.makeText(getApplicationContext(), "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });


                            mLoginButton = findViewById(R.id.login_button);
                            mLoginButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Start the login activity
                                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                }
                            });
                        }
                    }
                });
            }
        }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(),
                                "Failed to update data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
});
    }}
