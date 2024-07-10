package com.example.inventorymanagement1;

public class Items {

    private String name;
    private String count;

    public Items() {
    }

    public Items(String id, String name, String count, String qrcode) {
        this.name = name;
        this.count = count;
    }


    public String getName() {
        return name;
    }

    public String getCount() {
        return count;
    }

    public void setName(String name) { this.name = name; }

    public void setCount(String count) {
        this.count = count;
    }
}
