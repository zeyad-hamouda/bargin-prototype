package com.example.myapplication;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProductBot {
    public interface SearchListener {
        void onSearchComplete(List<ProductInfo> products);
        void onSearchError(String errorMessage);
    }

    public static void searchProducts(String searchQuery, SearchListener listener) {
        new ProductSearchTask(listener).execute(searchQuery);
    }

    private static class ProductSearchTask extends AsyncTask<String, Void, List<ProductInfo>> {
        private final SearchListener listener;

        public ProductSearchTask(SearchListener listener) {
            this.listener = listener;
        }

        @Override
        protected List<ProductInfo> doInBackground(String... params) {
            String searchQuery = params[0];
            Log.d("ProductBot", "Performing search for query: " + searchQuery);

            List<ProductInfo> amazonProducts = searchAmazon(searchQuery);
            List<ProductInfo> noonProducts = searchNoon(searchQuery);
            List<ProductInfo> comparedProducts = comparePrices(amazonProducts, noonProducts);

            return comparedProducts;
        }

        @Override
        protected void onPostExecute(List<ProductInfo> products) {
            if (products != null) {
                Log.d("ProductBot", "Search complete. Found " + products.size() + " products.");
                listener.onSearchComplete(products);
            } else {
                listener.onSearchError("Error performing search");
            }
        }
    }

    private static List<ProductInfo> searchAmazon(String query) {
        List<ProductInfo> products = new ArrayList<>();

        try {
            String url = "https://www.amazon.ae/s?k=" + query.replace(" ", "+");
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                String html = response.body().string();
                Document document = Jsoup.parse(html);
                Elements elements = document.select(".s-result-item");

                for (Element element : elements) {
                    String name = element.select(".a-size-medium").text();
                    String description = element.select(".a-section a-spacing-medium a-spacing-top-small").text();
                    String category = element.attr("data-category");
                    String price = element.select(".a-price-whole").text();

                    products.add(new ProductInfo(name, description, category, parsePrice(price)));
                }
            } else {
                Log.e("ProductBot", "Error searching Amazon. Response code: " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("ProductBot", "Error searching Amazon: " + e.getMessage());
        }

        return products;
    }

    private static List<ProductInfo> searchNoon(String query) {
        List<ProductInfo> products = new ArrayList<>();

        try {
            String url = "https://www.noon.com/uae-en/search?q=" + query.replace(" ", "%20");
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request originalRequest = chain.request();
                        Request requestWithUserAgent = originalRequest.newBuilder()
                                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                                .build();
                        return chain.proceed(requestWithUserAgent);
                    })
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                String html = response.body().string();
                Document document = Jsoup.parse(html);
                Elements elements = document.select(".product");

                for (Element element : elements) {
                    String name = element.select(".sc-7983c5c-16 guILIl").text();
                    String description = element.select(".sc-3482472f-1 tbrvZ").text();
                    String category = element.select(".categoryLink").text();
                    String price = element.select(".priceNow").text();

                    products.add(new ProductInfo(name, description, category, parsePrice(price)));
                }
            } else {
                Log.e("ProductBot", "Error searching Noon. Response code: " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("ProductBot", "Error searching Noon: " + e.getMessage());
        }

        return products;
    }


    private static List<ProductInfo> comparePrices(List<ProductInfo> amazonProducts, List<ProductInfo> noonProducts) {
        List<ProductInfo> comparedProducts = new ArrayList<>();

        for (ProductInfo amazonProduct : amazonProducts) {
            for (ProductInfo noonProduct : noonProducts) {
                if (amazonProduct.getName().equalsIgnoreCase(noonProduct.getName())) {
                    ProductInfo comparedProduct = new ProductInfo(
                            amazonProduct.getName(),
                            amazonProduct.getDescription(),
                            amazonProduct.getCategory(),
                            amazonProduct.getAmazonPrice()
                    );
                    comparedProducts.add(comparedProduct);
                    break;
                }
            }
        }

        return comparedProducts;
    }

    private static double parsePrice(String price) {
        // Remove currency symbols and convert to double
        price = price.replaceAll("[^\\d.]", "");
        if (price.isEmpty()) {
            return 0.0; // or any other default value
        } else {
            try {
                return Double.parseDouble(price);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return 0.0; // or any other default value
            }
        }
    }

    public static class ProductInfo {
        private String name;
        private String description;
        private String category;
        private double amazonPrice;

        public ProductInfo(String name, String description, String category, double amazonPrice) {
            this.name = name;
            this.description = description;
            this.category = category;
            this.amazonPrice = amazonPrice;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getCategory() {
            return category;
        }

        public double getAmazonPrice() {
            return amazonPrice;
        }
    }
}

