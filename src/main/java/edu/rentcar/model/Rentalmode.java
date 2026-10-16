package edu.rentcar.model;

public interface Rentalmode {
    TariffPolicy createTariffPolicy();

    CoveragePolicy createCoveragePolicy();

    AssistancePolicy createAssistancePolicy();

}
