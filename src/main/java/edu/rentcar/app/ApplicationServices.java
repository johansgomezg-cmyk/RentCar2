package edu.rentcar.app;

import edu.rentcar.repository.VehicleRepository;
import edu.rentcar.service.VehicleService;

/**
 * ApplicationServices es un Service Locator: los controllers consultan una clase global para obtener los servicios.
 */
public final class ApplicationServices {
    private static final VehicleService VEHICLE_SERVICE = new VehicleService(new VehicleRepository());

    private ApplicationServices() {
    }

    public static VehicleService vehicleService() {
        return VEHICLE_SERVICE;
    }
}
