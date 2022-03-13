package lk.ijse.dep8.controller;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lk.ijse.dep8.Customer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class ManageCustomerFormController {
    private final Path dbPath = Paths.get("database/customers.dep8db");
    public TextField txtID;
    public TextField txtName;
    public TextField txtAddress;
    public TableView<Customer> tblCustomers;

    public void initialize() {
        tblCustomers.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblCustomers.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblCustomers.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("address"));

        TableColumn<Customer, Button> lastCol = (TableColumn<Customer, Button>) tblCustomers.getColumns().get(3);
        lastCol.setCellValueFactory(param -> {
            Button btnDelete = new Button("Delete");
            btnDelete.setOnAction(event -> {
                tblCustomers.getItems().remove(param.getValue());
                saveCustomers();
            });
            return new ReadOnlyObjectWrapper<>(btnDelete);
        });

        initDatabase();
    }

    public void btnSaveCustomer_OnAction(ActionEvent actionEvent) {

        if (!txtID.getText().matches("C\\d{3}") ||
                tblCustomers.getItems().stream().anyMatch(c -> c.getId().equalsIgnoreCase(txtID.getText()))) {
            txtID.requestFocus();
            txtID.selectAll();
            return;
        } else if (txtName.getText().trim().isEmpty()) {
            txtName.requestFocus();
            txtName.selectAll();
            return;
        } else if (txtAddress.getText().trim().isEmpty()) {
            txtAddress.requestFocus();
            txtAddress.selectAll();
            return;
        }

//        boolean b = tblCustomers.getItems().stream().anyMatch(c -> c.getId().equalsIgnoreCase(txtID.getText()));
//
//        for (Customer customer : tblCustomers.getItems()) {
//            if (customer.getId().matches(txtID.getText())){
//                txtID.requestFocus();
//                txtID.selectAll();
//                return;
//            }
//        }

        Customer newCustomer = new Customer(
                txtID.getText(),
                txtName.getText(),
                txtAddress.getText());
        tblCustomers.getItems().add(newCustomer);

        boolean result = saveCustomers();

        if (!result) {
            new Alert(Alert.AlertType.ERROR, "Failed to save the customer, try again").show();
            tblCustomers.getItems().remove(newCustomer);
        } else {
            txtID.clear();
            txtName.clear();
            txtAddress.clear();
        }

        txtID.requestFocus();
    }

    private void initDatabase() {
        try {

            if (!Files.exists(dbPath)) {
                Files.createDirectories(dbPath.getParent());
                Files.createFile(dbPath);
            }

            loadAllCustomers();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to initialize the database").showAndWait();
            Platform.exit();
        }
    }

    private boolean saveCustomers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(dbPath, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING))) {
            oos.writeObject(new ArrayList<Customer>(tblCustomers.getItems()));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void loadAllCustomers() {
        try (InputStream is = Files.newInputStream(dbPath, StandardOpenOption.READ);
             ObjectInputStream ois = new ObjectInputStream(is)) {
            tblCustomers.getItems().clear();
            tblCustomers.setItems(FXCollections.observableArrayList((ArrayList<Customer>) ois.readObject()));
        } catch (IOException | ClassNotFoundException e) {
            if (!(e instanceof EOFException)) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Failed to load customers").showAndWait();
            }
        }
    }

}
