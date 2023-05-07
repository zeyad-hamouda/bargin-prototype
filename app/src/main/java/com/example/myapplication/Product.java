package com.example.myapplication;

public class Product {
    private String name;
    private String description;
    private String image;
    private String id;
    private double price;
    // Add any additional fields that the APIs return

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id = id;
    }
    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
    public String getImage() {
        // Add code to get the image URL of the product
        return image;
    }
    public void setImage(){
        this.image = image;
    }

    // Add getters and setters for any additional fields
}

