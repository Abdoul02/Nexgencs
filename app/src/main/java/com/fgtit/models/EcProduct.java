package com.fgtit.models;

public class EcProduct {

    int product_id,id;
    String name, price;

public EcProduct(){}

    public EcProduct(int product_id, int id, String name, String price) {
        this.product_id = product_id;
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public EcProduct(int id, String name, String price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
