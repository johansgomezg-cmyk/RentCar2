
package edu.rentcar.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class ServicePackage implements RentalServiceItem {

    private final String name;

    private final List<RentalServiceItem> components =
            new ArrayList<>();

    public ServicePackage(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Package name is required.");
        }

        this.name = name.trim();
    }

    public ServicePackage add(RentalServiceItem component) {

        if (component == null) {
            throw new IllegalArgumentException(
                    "Component is required.");
        }

        if (component == this ||
                (component instanceof ServicePackage group
                        && group.contains(this))) {

            throw new IllegalArgumentException(
                    "Package cycles are not allowed.");
        }

        if (components.contains(component)) {
            throw new IllegalArgumentException(
                    "Component is already included.");
        }

        components.add(component);

        return this;
    }

    public List<RentalServiceItem> getComponents() {
        return List.copyOf(components);
    }

    @Override
    public String description() {

        String details = components.stream()
                .map(RentalServiceItem::description)
                .collect(Collectors.joining(", "));

        return name + " [" + details + "]";
    }

    @Override
    public double costForDays(int rentalDays) {

        if (rentalDays <= 0) {
            throw new IllegalArgumentException(
                    "Rental days must be positive.");
        }

        if (components.isEmpty()) {
            throw new IllegalStateException(
                    "Service package cannot be empty.");
        }

        double total = 0;

        for (RentalServiceItem component : components) {
            total += component.costForDays(rentalDays);
        }

        if (!Double.isFinite(total)) {
            throw new IllegalStateException(
                    "Package cost is too large.");
        }

        return total;
    }

    private boolean contains(ServicePackage target) {

        if (this == target) {
            return true;
        }

        for (RentalServiceItem component : components) {

            if (component instanceof ServicePackage group
                    && group.contains(target)) {
                return true;
            }
        }

        return false;
    }
}
