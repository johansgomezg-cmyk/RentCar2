
package edu.rentcar.model;

public interface RentalServiceItem {

    String description();

    double costForDays(int rentalDays);
}
