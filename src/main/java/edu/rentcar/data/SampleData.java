package edu.rentcar.data;

import edu.rentcar.model.Customer;
import edu.rentcar.model.Vehicle;

import java.util.List;

public final class SampleData {
    private SampleData() {
    }

    public static List<Customer> customers() {
        return List.of(
                new Customer("CUS-1001", "Sofia Martinez", "sofia.martinez@example.com", true),
                new Customer("CUS-1002", "Daniel Rojas", "daniel.rojas@example.com", true),
                new Customer("CUS-1003", "Valentina Torres", "valentina.torres@example.com", false)
        );
    }

    public static List<Vehicle> vehicles() {
        return List.of(
                new Vehicle("VEH-1001", "Toyota", "Corolla", "Economy", 2024, 150000, "Available"),
                new Vehicle("VEH-1002", "Mazda", "CX-5", "Executive", 2025, 230000, "Available"),
                new Vehicle("VEH-1003", "BMW", "320i", "Premium", 2024, 380000, "Available"),
                new Vehicle("VEH-1004", "Renault", "Duster", "Executive", 2023, 210000, "Available")
        );
    }
}
