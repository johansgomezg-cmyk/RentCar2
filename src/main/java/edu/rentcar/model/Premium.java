package edu.rentcar.model;

public class Premium implements Rentalmode {
    @Override
    public TariffPolicy createTariffPolicy() { return new PremiumTariff(); }
    @Override
    public CoveragePolicy createCoveragePolicy() { return new PremiumCoverage(); }
    @Override
    public AssistancePolicy createAssistancePolicy() { return new PremiumAssistance(); }
}

class PremiumTariff implements TariffPolicy {
    @Override
    public double calculateCost(double baseDaily, int totalDays) {
        return (baseDaily * totalDays) * 1.50;
    }
}

class PremiumCoverage implements CoveragePolicy {
    @Override
    public String gettextcoverage() {
        return "Seguro a todo riesgo";
    }
}

class PremiumAssistance implements AssistancePolicy {
    @Override
    public String gettext() {
        return "Asistencia inmediata con vehículo de reemplazo";
    }
}