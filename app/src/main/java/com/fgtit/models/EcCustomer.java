package com.fgtit.models;

public class EcCustomer {

    int customer_id, id;
    String name;

    public EcCustomer() {
    }

    public EcCustomer(int customer_id, int id, String name) {
        this.customer_id = customer_id;
        this.id = id;
        this.name = name;
       // this.address = address;
    }

    public EcCustomer(int id, String name) {
        this.id = id;
        this.name = name;
       // this.address = address;
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

  /*  public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }*/
}
