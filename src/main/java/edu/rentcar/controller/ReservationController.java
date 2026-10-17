
package edu.rentcar.controller;

import edu.rentcar.app.ApplicationServices;
import edu.rentcar.data.SampleData;
import edu.rentcar.model.*;
import edu.rentcar.service.RentalFacade;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

public class ReservationController {

    @FXML private ComboBox<Customer> customerCombo;
    @FXML private ComboBox<Vehicle> vehicleCombo;
    @FXML private ComboBox<String> modeCombo;
    @FXML private ComboBox<String> tokenCombo;

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    @FXML private CheckBox gpsCheck;
    @FXML private CheckBox childSeatCheck;
    @FXML private CheckBox extraDriverCheck;
    @FXML private CheckBox familyPackCheck;
    @FXML private CheckBox completePackCheck;

    @FXML private Button quoteButton;
    @FXML private Button payButton;
    @FXML private Button clearButton;

    @FXML private Label quoteLabel;
    @FXML private Label resultLabel;

    @FXML private TableView<Reservation> reservationTable;
    @FXML private TableColumn<Reservation, String> idColumn;
    @FXML private TableColumn<Reservation, String> customerColumn;
    @FXML private TableColumn<Reservation, String> vehicleColumn;
    @FXML private TableColumn<Reservation, LocalDate> startColumn;
    @FXML private TableColumn<Reservation, LocalDate> endColumn;
    @FXML private TableColumn<Reservation, String> statusColumn;

    private RentalEstimate currentEstimate;

    private final NumberFormat money =
            NumberFormat.getCurrencyInstance(
                    Locale.forLanguageTag("es-CO")
            );

    @FXML
    public void initialize() {

        customerCombo.getItems().setAll(
                SampleData.customers()
        );

        vehicleCombo.getItems().setAll(
                ApplicationServices.vehicleService().getAllVehicles()
        );

        customerCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Customer customer) {
                if (customer == null) return "";

                return customer.getFullName()
                        + " (" + customer.getId() + ")"
                        + (customer.isAuthorized()
                        ? " - Autorizado"
                        : " - No autorizado");
            }

            @Override
            public Customer fromString(String text) {
                return null;
            }
        });

        vehicleCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Vehicle vehicle) {
                if (vehicle == null) return "";

                return vehicle.getId() + " - "
                        + vehicle.getDisplayName()
                        + " (" + money.format(
                        vehicle.getDailyRate()
                ) + "/día)";
            }

            @Override
            public Vehicle fromString(String text) {
                return null;
            }
        });

        modeCombo.getItems().setAll(
                "Economy", "Executive", "Premium"
        );

        tokenCombo.getItems().setAll(
                "tok_approved",
                "tok_declined",
                "tok_api_error"
        );

        customerCombo.getSelectionModel().selectFirst();
        vehicleCombo.getSelectionModel().selectFirst();
        modeCombo.getSelectionModel().selectFirst();
        tokenCombo.getSelectionModel().selectFirst();

        startDatePicker.setValue(
                LocalDate.now().plusDays(1)
        );
        endDatePicker.setValue(
                LocalDate.now().plusDays(4)
        );

        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );
        customerColumn.setCellValueFactory(
                new PropertyValueFactory<>("customerId")
        );
        vehicleColumn.setCellValueFactory(
                new PropertyValueFactory<>("vehicleId")
        );
        startColumn.setCellValueFactory(
                new PropertyValueFactory<>("startDate")
        );
        endColumn.setCellValueFactory(
                new PropertyValueFactory<>("endDate")
        );
        statusColumn.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );

        installChangeListeners();
        invalidateQuote();
        refreshReservations();
    }

    private void installChangeListeners() {

        customerCombo.valueProperty().addListener(
                (obs, oldValue, newValue) -> invalidateQuote()
        );

        vehicleCombo.valueProperty().addListener(
                (obs, oldValue, newValue) -> invalidateQuote()
        );

        modeCombo.valueProperty().addListener(
                (obs, oldValue, newValue) -> invalidateQuote()
        );

        startDatePicker.valueProperty().addListener(
                (obs, oldValue, newValue) -> invalidateQuote()
        );

        endDatePicker.valueProperty().addListener(
                (obs, oldValue, newValue) -> invalidateQuote()
        );

        gpsCheck.selectedProperty().addListener(
                (obs, oldValue, newValue) -> invalidateQuote()
        );

        childSeatCheck.selectedProperty().addListener(
                (obs, oldValue, newValue) -> invalidateQuote()
        );

        extraDriverCheck.selectedProperty().addListener(
                (obs, oldValue, newValue) -> invalidateQuote()
        );

        familyPackCheck.selectedProperty().addListener(
                (obs, oldValue, newValue) -> invalidateQuote()
        );

        completePackCheck.selectedProperty().addListener(
                (obs, oldValue, newValue) -> invalidateQuote()
        );
    }

    private void invalidateQuote() {
        currentEstimate = null;
        payButton.setDisable(true);
        quoteLabel.setText(
                "Calcula una cotización para continuar."
        );
    }

    private RentalRequest buildRequest() {

        Customer customer = customerCombo.getValue();
        Vehicle vehicle = vehicleCombo.getValue();

        if (customer == null || vehicle == null
                || modeCombo.getValue() == null) {
            throw new IllegalArgumentException(
                    "Selecciona cliente, vehículo y modalidad."
            );
        }

        Rentalmode mode = switch (modeCombo.getValue()) {
            case "Economy" -> new Economy();
            case "Executive" -> new Executive();
            case "Premium" -> new Premium();
            default -> throw new IllegalArgumentException(
                    "Modalidad desconocida."
            );
        };

        RentalRequestBuilder builder =
                new RentalRequestBuilder()
                        .customerId(customer.getId())
                        .vehicleId(vehicle.getId())
                        .dates(
                                startDatePicker.getValue(),
                                endDatePicker.getValue()
                        )
                        .rentalMode(mode);

        if (gpsCheck.isSelected()) {
            builder.addService("GPS");
        }

        if (childSeatCheck.isSelected()) {
            builder.addService("CHILD_SEAT");
        }

        if (extraDriverCheck.isSelected()) {
            builder.addService("EXTRA_DRIVER");
        }

        if (familyPackCheck.isSelected()) {
            builder.addService("FAMILY_PACK");
        }

        if (completePackCheck.isSelected()) {
            builder.addService("COMPLETE_PACK");
        }

        return builder.build();
    }

    @FXML
    private void quoteRental() {
        try {
            RentalRequest request = buildRequest();

            currentEstimate =
                    ApplicationServices.rentalFacade()
                            .quote(request);

            quoteLabel.setText(
                    "Total: "
                            + money.format(currentEstimate.totalCost())
                            + "\nDías: "
                            + currentEstimate.rentalDays()
                            + "\nDetalle: "
                            + currentEstimate.description()
                            + "\nCobertura: "
                            + currentEstimate.coverageDescription()
                            + "\nAsistencia: "
                            + currentEstimate.assistanceDescription()
            );

            payButton.setDisable(false);

            resultLabel.setText(
                    "Cotización lista. Confirma si deseas pagar."
            );

        } catch (RuntimeException exception) {
            invalidateQuote();
            showAlert(
                    Alert.AlertType.WARNING,
                    "No se pudo cotizar",
                    exception.getMessage()
            );
        }
    }

    @FXML
    private void checkoutRental() {

        if (currentEstimate == null) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Cotización requerida",
                    "Primero calcula la cotización."
            );
            return;
        }

        String token = tokenCombo.getValue();

        if (token == null || token.isBlank()) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Token requerido",
                    "Selecciona un token de prueba."
            );
            return;
        }

        final RentalRequest request;

        try {
            request = buildRequest();
        } catch (RuntimeException exception) {
            invalidateQuote();
            showAlert(
                    Alert.AlertType.WARNING,
                    "Datos inválidos",
                    exception.getMessage()
            );
            return;
        }

        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        confirmation.setTitle("Confirmar pago");
        confirmation.setHeaderText(
                "¿Deseas procesar esta reservación?"
        );

        confirmation.setContentText(
                "Cliente: " + request.getCustomerId()
                        + "\nVehículo: " + request.getVehicleId()
                        + "\nTotal: "
                        + money.format(currentEstimate.totalCost())
                        + "\nToken de prueba: " + token
        );

        if (confirmation.showAndWait()
                .filter(button -> button == ButtonType.OK)
                .isEmpty()) {
            return;
        }

        setBusy(true);
        resultLabel.setText(
                "Procesando pago. Espera el resultado..."
        );

        Task<CheckoutResult> task = new Task<>() {
            @Override
            protected CheckoutResult call() {
                RentalFacade facade =
                        ApplicationServices.rentalFacade();

                return facade.checkout(request, token);
            }
        };

        task.setOnSucceeded(event -> {
            setBusy(false);

            CheckoutResult result = task.getValue();

            refreshReservations();
            invalidateQuote();

            if (result.isConfirmed()) {

                resultLabel.setText(
                        "RESERVA CONFIRMADA: "
                                + result.reservation().getId()
                                + "\nTotal pagado: "
                                + money.format(
                                result.estimate().totalCost()
                        )
                );

                showAlert(
                        Alert.AlertType.INFORMATION,
                        "Reservación confirmada",
                        "Pago aprobado y reserva confirmada."
                );

            } else {

                resultLabel.setText(
                        "NO CONFIRMADA: "
                                + result.reservation().getId()
                                + "\nEstado: "
                                + result.reservation().getStatus()
                                + "\nDetalle: "
                                + result.payment().message()
                );

                showAlert(
                        Alert.AlertType.WARNING,
                        "Reservación no confirmada",
                        result.payment().message()
                );
            }
        });

        task.setOnFailed(event -> {
            setBusy(false);
            refreshReservations();
            invalidateQuote();

            resultLabel.setText(
                    "No se pudo completar la operación. "
                            + "Revisa el estado de las reservas."
            );

            showAlert(
                    Alert.AlertType.ERROR,
                    "Error durante checkout",
                    "La operación terminó con un error inesperado."
            );
        });

        Thread worker = new Thread(
                task, "rentcar-checkout"
        );

        worker.setDaemon(true);
        worker.start();
    }

    private void setBusy(boolean busy) {
        customerCombo.setDisable(busy);
        vehicleCombo.setDisable(busy);
        modeCombo.setDisable(busy);
        tokenCombo.setDisable(busy);

        startDatePicker.setDisable(busy);
        endDatePicker.setDisable(busy);

        gpsCheck.setDisable(busy);
        childSeatCheck.setDisable(busy);
        extraDriverCheck.setDisable(busy);
        familyPackCheck.setDisable(busy);
        completePackCheck.setDisable(busy);

        quoteButton.setDisable(busy);
        clearButton.setDisable(busy);

        payButton.setDisable(
                busy || currentEstimate == null
        );
    }

    private void refreshReservations() {
        reservationTable.getItems().setAll(
                ApplicationServices.reservationService()
                        .getAllReservations()
        );
    }

    @FXML
    private void clearForm() {

        gpsCheck.setSelected(false);
        childSeatCheck.setSelected(false);
        extraDriverCheck.setSelected(false);
        familyPackCheck.setSelected(false);
        completePackCheck.setSelected(false);

        startDatePicker.setValue(
                LocalDate.now().plusDays(1)
        );

        endDatePicker.setValue(
                LocalDate.now().plusDays(4)
        );

        customerCombo.getSelectionModel().selectFirst();
        vehicleCombo.getSelectionModel().selectFirst();
        modeCombo.getSelectionModel().selectFirst();
        tokenCombo.getSelectionModel().selectFirst();

        resultLabel.setText("");
        invalidateQuote();
        refreshReservations();
    }

    private void showAlert(
            Alert.AlertType type,
            String title,
            String message) {

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
