package edu.birzeit.encs5150.exp2;

import java.util.ArrayList;

public class Customer {

    // Shared by both activities. It lives in memory only, so it is
    // emptied when Android kills the app (Exp 4 stores it in SQLite).
    public static final ArrayList<Customer> customersArrayList = new ArrayList<>();

    private long customerId;
    private String name;
    private String phone;
    private String gender;

    public Customer() {
    }

    public Customer(long customerId, String name, String phone, String gender) {
        this.customerId = customerId;
        this.name = name;
        this.phone = phone;
        this.gender = gender;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "ID: " + customerId
                + "\nName: " + name
                + "\nPhone: " + phone
                + "\nGender: " + gender;
    }
}
