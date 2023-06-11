package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailsActivity extends AppCompatActivity {
    private ImageView productImageView;
    private TextView productNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        productImageView = findViewById(R.id.productImageView);
        productNameTextView = findViewById(R.id.productNameTextView);

        // Get the intent data
        Intent intent = getIntent();
        String productName = intent.getStringExtra("productName");
        String imageUrl = intent.getStringExtra("imageUrl");
        String productId = intent.getStringExtra("productId");

        // Set the product details in the views
        productNameTextView.setText(productName);
        Glide.with(this).load(imageUrl).into(productImageView);

        // Get current user's email
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Replace dot with comma to make a valid Firebase path
        String userEmailPath = userEmail.replace('.', ',');

        // Add this product to the user's viewed products list
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
        mDatabase.child(userEmailPath).addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User currentUser = snapshot.getValue(User.class);
                if (currentUser != null) {
                    List<String> viewedProductIds = currentUser.getViewedProductIds();

                    // Handle the case where getViewedProductIds() returns null
                    if (viewedProductIds == null) {
                        viewedProductIds = new ArrayList<>();
                    }

                    // Check if the product is already in the viewed list
                    if (!viewedProductIds.contains(productId)) {
                        viewedProductIds.add(productId);

                        // Update the user in the database
                        currentUser.setViewedProductIds(viewedProductIds);
                        mDatabase.child(userEmailPath).setValue(currentUser);
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }
}
