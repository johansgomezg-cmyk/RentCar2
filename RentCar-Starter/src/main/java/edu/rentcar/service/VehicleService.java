package edu.rentcar.service;

import edu.rentcar.model.Vehicle;
import edu.rentcar.repository.VehicleRepository;

import java.time.Year;
import java.util.List;

public class VehicleService {
    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public int getVehicleCount() {
        return vehicleRepository.count();
    }

    public Vehicle registerVehicle(String brand, String model, String category,
                                   String modelYearText, String dailyRateText) {
        String cleanBrand = requireText(brand, "Brand");
        String cleanModel = requireText(model, "Model");
        String cleanCategory = requireText(category, "Category");

        int modelYear;
        try {
            modelYear = Integer.parseInt(modelYearText.trim());
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Model year must be a valid number.");
        }

        int currentYear = Year.now().getValue();
        if (modelYear < 1990 || modelYear > currentYear + 1) {
            throw new IllegalArgumentException(
                    "Model year must be between 1990 and " + (currentYear + 1) + "."
            );
        }

        double dailyRate;
        try {
            dailyRate = Double.parseDouble(dailyRateText.trim());
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Daily rate must be a valid number.");
        }

        if (!Double.isFinite(dailyRate) || dailyRate <= 0) {
            throw new IllegalArgumentException("Daily rate must be greater than zero.");
        }

        return vehicleRepository.save(
                cleanBrand, cleanModel, cleanCategory, modelYear, dailyRate
        );
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
    }
}
