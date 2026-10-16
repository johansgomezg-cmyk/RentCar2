package edu.rentcar.model;

public class Vehicle {
    private final String id;
    private final String brand;
    private final String model;
    private final String category;
    private final int modelYear;
    private final double dailyRate;
    private final String status;

    public Vehicle(String id, String brand, String model, String category, int modelYear) {
        this(id, brand, model, category, modelYear, 0.0, "Available");
    }

    public Vehicle(String id, String brand, String model, String category,
                   int modelYear, double dailyRate, String status) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.category = category;
        this.modelYear = modelYear;
        this.dailyRate = dailyRate;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getCategory() {
        return category;
    }

    public int getModelYear() {
        return modelYear;
    }

    public double getDailyRate() {
        return dailyRate;
    }

    public String getStatus() {
        return status;
    }

    public String getDisplayName() {
        return brand + " " + model;
    }
}
