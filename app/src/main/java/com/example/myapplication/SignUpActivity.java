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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.Query;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

public class SignUpActivity extends AppCompatActivity {

    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private EditText mConfirmPasswordEditText;
    private CountryCodePicker mCountryCodePicker;
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


        mCountryCodePicker = findViewById(R.id.countryCodePicker);

        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmailEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();
                String confirmPassword = mConfirmPasswordEditText.getText().toString();
                String phoneCode = mCountryCodePicker.getSelectedCountryCode();
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

                if (TextUtils.isEmpty(phoneCode)) {
                    Toast.makeText(SignUpActivity.this, "Please enter your phone code", Toast.LENGTH_SHORT).show();
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
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    // Failed to read value, display error message
                                    Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                            mAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                // User created successfully, proceed to add user data to database
                                                FirebaseUser user = mAuth.getCurrentUser();
                                                // Add user data to database ...
                                            } else {
                                                // User creation failed, display a message to the user
                                                Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                                        Toast.LENGTH_SHORT).show();
                                            }
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
