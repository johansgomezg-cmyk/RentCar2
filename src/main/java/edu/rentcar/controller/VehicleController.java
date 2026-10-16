package edu.rentcar.controller;

import edu.rentcar.app.ApplicationServices;
import edu.rentcar.model.Vehicle;
import edu.rentcar.service.VehicleService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class VehicleController {
    private final VehicleService vehicleService = ApplicationServices.vehicleService();

    @FXML private TableView<Vehicle> vehicleTable;
    @FXML private TableColumn<Vehicle, String> vehicleIdColumn;
    @FXML private TableColumn<Vehicle, String> vehicleBrandColumn;
    @FXML private TableColumn<Vehicle, String> vehicleModelColumn;
    @FXML private TableColumn<Vehicle, String> vehicleCategoryColumn;
    @FXML private TableColumn<Vehicle, Integer> vehicleYearColumn;
    @FXML private TableColumn<Vehicle, Number> vehicleRateColumn;
    @FXML private TableColumn<Vehicle, String> vehicleStatusColumn;
    @FXML private TextField vehicleBrandField;
    @FXML private TextField vehicleModelField;
    @FXML private ComboBox<String> vehicleCategoryCombo;
    @FXML private TextField vehicleYearField;
    @FXML private TextField vehicleRateField;

    @FXML
    public void initialize() {
        vehicleIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        vehicleBrandColumn.setCellValueFactory(new PropertyValueFactory<>("brand"));
        vehicleModelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        vehicleCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        vehicleYearColumn.setCellValueFactory(new PropertyValueFactory<>("modelYear"));
        vehicleRateColumn.setCellValueFactory(new PropertyValueFactory<>("dailyRate"));
        vehicleStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        vehicleCategoryCombo.getItems().setAll("Economy", "Executive", "Premium");
        vehicleCategoryCombo.getSelectionModel().selectFirst();
        refreshVehicleData();
    }

    @FXML
    private void registerVehicle() {
        try {
            Vehicle vehicle = vehicleService.registerVehicle(
                    vehicleBrandField.getText(),
                    vehicleModelField.getText(),
                    vehicleCategoryCombo.getValue(),
                    vehicleYearField.getText(),
                    vehicleRateField.getText()
            );
            refreshVehicleData();
            clearVehicleForm();
            showAlert(Alert.AlertType.INFORMATION, "Vehicle registered",
                    vehicle.getDisplayName() + " was registered successfully.");
        } catch (IllegalArgumentException exception) {
            showAlert(Alert.AlertType.WARNING, "Check vehicle information", exception.getMessage());
        }
    }

    @FXML
    private void clearVehicleForm() {
        vehicleBrandField.clear();
        vehicleModelField.clear();
        vehicleYearField.clear();
        vehicleRateField.clear();
        vehicleCategoryCombo.getSelectionModel().selectFirst();
    }

    private void refreshVehicleData() {
        vehicleTable.getItems().setAll(vehicleService.getAllVehicles());
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
