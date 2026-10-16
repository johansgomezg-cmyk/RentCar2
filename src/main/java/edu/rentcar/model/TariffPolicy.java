package edu.rentcar.model;

public interface TariffPolicy {
    double calculateCost (double baseDaily, int totalDays);
}
