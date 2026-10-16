package edu.rentcar.model;

public class Economy implements Rentalmode {
    @Override
    public TariffPolicy createTariffPolicy() {
        return new EconomyTariff(); }
    @Override
    public CoveragePolicy createCoveragePolicy() {
        return new EconomyCoverage(); }
    @Override
    public AssistancePolicy createAssistancePolicy() {
        return new EconomyAssistance(); }
}

class EconomyTariff implements TariffPolicy {
    @Override
    public double calculateCost(double baseDaily, int totalDays) {
        return baseDaily * totalDays;
    }
}

class EconomyCoverage implements CoveragePolicy {
    @Override
    public String gettextcoverage() {
        return "Seguro básico";
    }
}

class EconomyAssistance implements AssistancePolicy {
    @Override
    public String gettext() {
        return "Asistencia en horario";
    }
}