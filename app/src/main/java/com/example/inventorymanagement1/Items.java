package com.example.inventorymanagement1;

public class Items {

    private String id;
    private String name;
    private String count;
    private String qrcode;

    public Items() {
    }

    public Items(String id, String name, String count, String qrcode) {
        this.id = id;
        this.name = name;
        this.count = count;
        this.qrcode = qrcode;

    }

    public String getId() { return id; }

    public String getName() {
        return name;
    }

    public String getCount() {
        return count;
    }
    public String getQrcode() { return qrcode;}

    public void setId(String id) { this.id = id; }

    public void setName(String name) { this.name = name; }

    public void setCount(String count) {
        this.count = count;
    }

    public void setQrcode(String qrcode) { this.qrcode = qrcode; }
}
