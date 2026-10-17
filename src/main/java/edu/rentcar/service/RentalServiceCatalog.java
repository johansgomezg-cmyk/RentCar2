
package edu.rentcar.service;

import edu.rentcar.model.IndividualService;
import edu.rentcar.model.RentalServiceItem;
import edu.rentcar.model.ServicePackage;

import java.util.Map;

public final class RentalServiceCatalog {

    private RentalServiceCatalog() {
    }

    public static Map<String, RentalServiceItem> standardServices() {

        IndividualService gps = new IndividualService(
                "GPS", "GPS", 12000
        );

        IndividualService childSeat = new IndividualService(
                "CHILD_SEAT", "Child seat", 8000
        );

        IndividualService extraDriver = new IndividualService(
                "EXTRA_DRIVER", "Extra driver", 18000
        );

        ServicePackage family = new ServicePackage("Family pack")
                .add(childSeat)
                .add(extraDriver);

        ServicePackage complete = new ServicePackage("Complete pack")
                .add(gps)
                .add(family);

        return Map.of(
                "GPS", gps,
                "CHILD_SEAT", childSeat,
                "EXTRA_DRIVER", extraDriver,
                "FAMILY_PACK", family,
                "COMPLETE_PACK", complete
        );
    }
}
