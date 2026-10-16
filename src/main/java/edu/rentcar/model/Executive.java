package edu.rentcar.model;

public class Executive implements Rentalmode {
    @Override
    public TariffPolicy createTariffPolicy() { return new ExecutiveTariff(); }
    @Override
    public CoveragePolicy createCoveragePolicy() { return new ExecutiveCoverage(); }
    @Override
    public AssistancePolicy createAssistancePolicy() { return new ExecutiveAssistance(); }
}

class ExecutiveTariff implements TariffPolicy {
    @Override
    public double calculateCost(double baseDaily, int totalDays) {
        return (baseDaily * totalDays) * 1.10;
    }
}

class ExecutiveCoverage implements CoveragePolicy {
    @Override
    public String gettextcoverage() {
        return "Seguro amplio";
    }
}

class ExecutiveAssistance implements AssistancePolicy {
    @Override
    public String gettext() {
        return "Asistencia 24/7";
    }
}