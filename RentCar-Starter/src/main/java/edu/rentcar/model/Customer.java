package edu.rentcar.model;

public class Customer {
    private final String id;
    private final String fullName;
    private final String email;
    private final boolean authorized;

    public Customer(String id, String fullName, String email, boolean authorized) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.authorized = authorized;
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public boolean isAuthorized() {
        return authorized;
    }
}
