
package edu.rentcar.app;

import edu.rentcar.data.SampleData;
import edu.rentcar.payment.InMemoryCustomerVerificationService;
import edu.rentcar.payment.PaymentApiAdapter;
import edu.rentcar.payment.PaymentGateway;
import edu.rentcar.payment.PaymentVerificationProxy;
import edu.rentcar.repository.VehicleRepository;
import edu.rentcar.service.RentalFacade;
import edu.rentcar.service.RentalPricingService;
import edu.rentcar.service.RentalServiceCatalog;
import edu.rentcar.service.ReservationService;
import edu.rentcar.service.VehicleAvailabilityService;
import edu.rentcar.service.VehicleService;

public final class ApplicationServices {

    private static final VehicleService VEHICLE_SERVICE =
            new VehicleService(new VehicleRepository());

    private static final ReservationService RESERVATION_SERVICE =
            new ReservationService();

    private static final PaymentGateway PAYMENT_GATEWAY =
            new PaymentVerificationProxy(
                    new InMemoryCustomerVerificationService(
                            SampleData.customers()
                    ),
                    new PaymentApiAdapter("http://localhost:8080")
            );

    private ApplicationServices() {
    }

    public static VehicleService vehicleService() {
        return VEHICLE_SERVICE;
    }

    public static ReservationService reservationService() {
        return RESERVATION_SERVICE;
    }

    public static RentalFacade rentalFacade() {
        return new RentalFacade(
                VEHICLE_SERVICE.getAllVehicles(),
                RESERVATION_SERVICE,
                new RentalPricingService(),
                new VehicleAvailabilityService(),
                RentalServiceCatalog.standardServices(),
                PAYMENT_GATEWAY
        );
    }
}
