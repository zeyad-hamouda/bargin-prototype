package com.example.myapplication;

import android.app.SearchManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private boolean isLoggedIn = false;

    private DatabaseReference mDatabaseRef; // Add this line

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView welcomeMessage = findViewById(R.id.welcome_message);
        Spinner userSpinner = findViewById(R.id.user_spinner);
        ImageButton dropdownButton = findViewById(R.id.logout_button);
        Button loginButton = findViewById(R.id.login_button);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users");
            mDatabaseRef.orderByChild("email").equalTo(email).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<String> userNames = new ArrayList<>();
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String firstName = userSnapshot.child("firstName").getValue(String.class);
                        String lastName = userSnapshot.child("lastName").getValue(String.class);
                        if (!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName)) {
                            String userName = firstName + " " + lastName;
                            userNames.add(userName);
                        }
                    }
                    userNames.add("Logout");
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, userNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    userSpinner.setAdapter(adapter);
                    userSpinner.setVisibility(View.VISIBLE);
                    dropdownButton.setVisibility(View.VISIBLE);
                    loginButton.setVisibility(View.GONE);
                }


                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("MainActivity", "Error reading user's data", error.toException());
                }
            });
        } else {
            userSpinner.setVisibility(View.GONE);
            dropdownButton.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
        }

        dropdownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userSpinner.performClick();
            }
        });

        userSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String userName = adapterView.getItemAtPosition(position).toString();
                if (userName.equals("Logout")) {
                    mAuth.signOut();
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    welcomeMessage.setText("Welcome " + userName);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                welcomeMessage.setText("Welcome");
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch the login activity
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
                searchIntent.putExtra(SearchManager.QUERY, query);
                startActivity(searchIntent);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

}




