package com.example.myapplication;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Product;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class SearchActivity extends AppCompatActivity {

    private SearchView searchView;
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;

    private AmazonApiService amazonApiService;
    private NoonApiService noonApiService;
    private MockApiService mockApiService;
    private List<Product> amazonProducts;
    private List<Product> noonProducts;
    private List<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchView = findViewById(R.id.searchView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(SearchActivity.this, new ArrayList<>());
        recyclerView.setAdapter(productAdapter);

        amazonProducts = new ArrayList<>();
        noonProducts = new ArrayList<>();
        productList = new ArrayList<>();

        // Hide the action bar
        getSupportActionBar().hide();

        // Initialize the Retrofit instances
        Retrofit amazonRetrofit = new Retrofit.Builder()
                .baseUrl("https://amazon.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Retrofit noonRetrofit = new Retrofit.Builder()
                .baseUrl("https://noon.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Retrofit mockRetrofit = new Retrofit.Builder()
                .baseUrl("https://zeyad-hamouda.github.io/apitest")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create API service interfaces
        amazonApiService = amazonRetrofit.create(AmazonApiService.class);
        noonApiService = noonRetrofit.create(NoonApiService.class);
        mockApiService = mockRetrofit.create(MockApiService.class);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Get the search query entered by the user
                String searchQuery = searchView.getQuery().toString();

                // Make API requests to Amazon, Noon, and Mock API
                Call<List<Product>> amazonCall = amazonApiService.searchProducts(searchQuery);
                Call<List<Product>> noonCall = noonApiService.searchProducts(searchQuery);
                Call<List<Product>> mockCall = mockApiService.searchProducts(searchQuery);

                // Execute the API requests asynchronously
                amazonCall.enqueue(new Callback<List<Product>>() {
                    @Override
                    public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                        // Handle successful Amazon API response
                        List<Product> amazonProducts = response.body();
                        // Merge the Amazon products with Noon and mock products and update UI
                        mergeResultsAndUpdateUI(amazonProducts);
                    }

                    @Override
                    public void onFailure(Call<List<Product>> call, Throwable t) {
                        // Handle Amazon API failure
                    }
                });

                noonCall.enqueue(new Callback<List<Product>>() {
                    @Override
                    public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                        // Handle successful Noon API response
                        List<Product> noonProducts = response.body();
                        mergeResultsAndUpdateUI(noonProducts);
                    }

                    @Override
                    public void onFailure(Call<List<Product>> call, Throwable t) {
                        // Handle Noon API failure
                        Log.e(TAG, "Failed to fetch data from Noon API", t);
                    }
                });

                mockCall.enqueue(new Callback<List<Product>>() {
                    @Override
                    public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                        // Handle successful mock API response
                        List<Product> mockProducts = response.body();
                        // Merge the Amazon and Noon products with the mock products and update the UI
                        // Combine the results from all APIs
                        List<Product> combinedResults = new ArrayList<>();
                        combinedResults.addAll(amazonProducts);
                        combinedResults.addAll(noonProducts);
                        combinedResults.addAll(mockProducts);

                        // Update the UI with the combined results
                        updateUI(combinedResults);
                    }

                    @Override
                    public void onFailure(Call<List<Product>> call, Throwable t) {
                        // Handle mock API failure
                        Log.e(TAG, "Failed to fetch data from mock API", t);
                    }
                });
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    // Show the full list of products if the search query is empty
                    productList.clear();
                    productList.addAll(allProducts);
                    adapter.notifyDataSetChanged();
                } else {
                    // Filter the list of products based on the search query
                    String searchQuery = newText.toLowerCase();
                    List<Product> filteredProducts = new ArrayList<>();
                    for (Product product : allProducts) {
                        if (product.getName().toLowerCase().contains(searchQuery) || product.getDescription().toLowerCase().contains(searchQuery)) {
                            filteredProducts.add(product);
                        }
                    }
                    productList.clear();
                    productList.addAll(filteredProducts);
                    adapter.notifyDataSetChanged();
                }
                return true;
            }



            private void mergeResultsAndUpdateUI(List<Product> products) {
                // Merge the API results with the existing product list
                productList.addAll(products);

                // Remove duplicate products
                removeDuplicateProducts();

                // Sort the product list by price
                sortProductsByPrice();

                // Update the RecyclerView with the new product list
                productAdapter.notifyDataSetChanged();
            }

            private void updateUI(List<Product> products) {
                // Clear the existing product list and add the new products
                productList.clear();
                productList.addAll(products);

                // Remove duplicate products
                removeDuplicateProducts();

                // Sort the product list by price
                sortProductsByPrice();

                // Update the RecyclerView with the new product list
                productAdapter.notifyDataSetChanged();
            }

            private void removeDuplicateProducts() {
                // Remove any duplicate products from the product list
                List<Product> uniqueProducts = new ArrayList<>();
                for (Product product : productList) {
                    boolean isDuplicate = false;
                    for (Product uniqueProduct : uniqueProducts) {
                        if (product.getId().equals(uniqueProduct.getId())) {
                            isDuplicate = true;
                            break;
                        }
                    }
                    if (!isDuplicate) {
                        uniqueProducts.add(product);
                    }
                }
                productList = uniqueProducts;
            }

            private void sortProductsByPrice() {
                // Sort the product list by price
                Collections.sort(productList, new Comparator<Product>() {
                    @Override
                    public int compare(Product p1, Product p2) {
                        if (p1.getPrice() < p2.getPrice()) {
                            return -1;
                        } else if (p1.getPrice() > p2.getPrice()) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                });
            }

            // Interface for the Amazon API service
            interface AmazonApiService {
                @GET("search")
                Call<List<Product>> searchProducts(@Query("q") String query);
            }

            // Interface for the Noon API service
            interface NoonApiService {
                @GET("search")
                Call<List<Product>> searchProducts(@Query("q") String query);
            }

            // Interface for the mock API service
            interface MockApiService {
                @GET("search")
                Call<List<Product>> searchProducts(@Query("q") String query);
            }
        });
    }
}
