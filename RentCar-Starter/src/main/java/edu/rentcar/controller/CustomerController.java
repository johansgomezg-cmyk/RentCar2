package edu.rentcar.controller;

import edu.rentcar.data.SampleData;
import edu.rentcar.model.Customer;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class CustomerController {
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> customerIdColumn;
    @FXML private TableColumn<Customer, String> customerNameColumn;
    @FXML private TableColumn<Customer, String> customerEmailColumn;
    @FXML private TableColumn<Customer, Boolean> customerAuthorizedColumn;

    @FXML
    public void initialize() {
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        customerEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        customerAuthorizedColumn.setCellValueFactory(new PropertyValueFactory<>("authorized"));
        customerTable.getItems().setAll(SampleData.customers());
    }
}
