package com.example.inventorymanagement1;

import java.util.List;

public class Project {
    private String project_name;
    private String qrcode;


    public Project() {
    }

    public Project(String project_name, String qrcode) {
        this.project_name = project_name;
        this.qrcode = qrcode;

    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public String getQrCode() { return qrcode; }

    public void setQrCode(String qrcode) {
        this.qrcode = qrcode;
    }
}
