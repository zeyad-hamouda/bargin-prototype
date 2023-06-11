package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

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

        // Set the product details in the views
        productNameTextView.setText(productName);
        // Load the product image using an image loading library like Picasso or Glide

        Glide.with(this).load(imageUrl).into(productImageView);    }
}
