package edu.rentcar.service;

import edu.rentcar.model.Vehicle;
import edu.rentcar.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VehicleServiceTest {

    private VehicleService vehicleService;

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleService(new VehicleRepository());
    }

    @Test
    void registerVehicleWithValidDataShouldAddVehicle() {
        int initialCount = vehicleService.getVehicleCount();

        Vehicle vehicle = vehicleService.registerVehicle(
                "Honda", "Civic", "Economy", "2024", "175000"
        );

        assertNotNull(vehicle);
        assertEquals("Honda", vehicle.getBrand());
        assertEquals("Civic", vehicle.getModel());
        assertEquals("Economy", vehicle.getCategory());
        assertEquals(2024, vehicle.getModelYear());
        assertEquals(175000.0, vehicle.getDailyRate());
        assertEquals("Available", vehicle.getStatus());
        assertEquals(initialCount + 1, vehicleService.getVehicleCount());
    }

    @Test
    void registerVehicleWithBlankBrandShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> vehicleService.registerVehicle(
                        " ", "Civic", "Economy", "2024", "175000"
                )
        );

        assertEquals("Brand is required.", exception.getMessage());
    }

    @Test
    void registerVehicleWithInvalidYearShouldThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> vehicleService.registerVehicle(
                        "Honda", "Civic", "Economy", "not-a-year", "175000"
                )
        );
    }

    @Test
    void registerVehicleWithYearOutOfRangeShouldThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> vehicleService.registerVehicle(
                        "Honda", "Civic", "Economy", "1800", "175000"
                )
        );
    }

    @Test
    void registerVehicleWithZeroDailyRateShouldThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> vehicleService.registerVehicle(
                        "Honda", "Civic", "Economy", "2024", "0"
                )
        );
    }

    @Test
    void registerVehicleWithInvalidDailyRateShouldThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> vehicleService.registerVehicle(
                        "Honda", "Civic", "Economy", "2024", "not-a-rate"
                )
        );
    }

    @Test
    void getAllVehiclesShouldReturnExistingAndNewVehicles() {
        int initialCount = vehicleService.getAllVehicles().size();

        vehicleService.registerVehicle(
                "Honda", "Civic", "Economy", "2024", "175000"
        );

        assertEquals(initialCount + 1, vehicleService.getAllVehicles().size());
    }
}
