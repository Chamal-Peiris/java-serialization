package lk.ijse.dep8.controller;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
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
    public TextField txtImage;
    public int selectedIndex;
    public Button btnUpdateDetails;
    public Button brnNewCustomer;
    public Button btnBrowse;
    public Button btnSaveCustomer;

    public void initialize() {
        tblCustomers.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblCustomers.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblCustomers.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("address"));

        TableColumn<Customer, ImageView> customerTableColumn = (TableColumn<Customer, ImageView>) tblCustomers.getColumns().get(3);
        customerTableColumn.setCellValueFactory(param -> {
            byte[] image = param.getValue().getImage();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(image);
            ImageView imageView = new ImageView(new Image(byteArrayInputStream));
            imageView.setFitHeight(75);
            imageView.setFitWidth(75);
            return new ReadOnlyObjectWrapper<>(imageView);

        });



        TableColumn<Customer, Button> lastCol = (TableColumn<Customer, Button>) tblCustomers.getColumns().get(4);
        lastCol.setCellValueFactory(param -> {
            Button btnDelete = new Button("Delete");
            btnDelete.setStyle("-fx-background-color: red;-fx-text-fill: white;");
            btnDelete.setOnAction(event -> {
                tblCustomers.getItems().remove(param.getValue());
                saveCustomers();
            });
            return new ReadOnlyObjectWrapper<>(btnDelete);
        });

        tblCustomers.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            disableControls(false);
            btnSaveCustomer.setDisable(true);
            txtName.setText(newValue.getName());
            txtImage.setText("[Picture]");              //add select customer feature
            txtAddress.setText(newValue.getAddress());
            txtID.setText(newValue.getId());
            txtID.setEditable(false);
            selectedIndex = tblCustomers.getSelectionModel().getSelectedIndex();
            // System.out.println(selectedIndex);


        });

        initDatabase();
    }
    private void disableControls(boolean disable) {
        txtID.setDisable(disable);
        txtName.setDisable(disable);
        txtAddress.setDisable(disable);
        txtImage.setDisable(disable);
        btnBrowse.setDisable(disable);
        btnSaveCustomer.setDisable(disable);
        btnUpdateDetails.setDisable(disable);
    }

    public void btnSaveCustomer_OnAction(ActionEvent actionEvent) throws IOException {

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
        else if (txtImage.getText().trim().isEmpty()) {
            txtImage.requestFocus();
            txtImage.selectAll();
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
                txtID.getText(),generateBuffer(),
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
            txtImage.clear();
            new Alert(Alert.AlertType.INFORMATION,"Customer details added Successfully!").show();
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

    public void btnBrowseOnAction(ActionEvent actionEvent) {
        FileChooser fs = new FileChooser();
        fs.setTitle("Select an Image");
        fs.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image File", "*.jpg"));
        File file = fs.showOpenDialog(null);
        txtImage.setText(file.getAbsolutePath());




    }

    public void btnNewCustomer_OnAction(ActionEvent actionEvent) {
        disableControls(false);
        btnUpdateDetails.setDisable(true);
        txtImage.clear();
        txtID.clear();
        txtAddress.clear();
        txtName.clear();
    }

    public void btnUpdateDetails_OnActiion(ActionEvent actionEvent) throws IOException {

        Customer updateCus = new Customer(
                txtID.getText(),
                generateBuffer(), txtName.getText(),
                txtAddress.getText());
        tblCustomers.getItems().remove(selectedIndex);
        tblCustomers.getItems().add(updateCus);
        boolean result = saveCustomers();

        if (!result) {
            new Alert(Alert.AlertType.ERROR, "Failed to Update the customer, try again").show();
            tblCustomers.getItems().remove(updateCus);
        } else {
            txtID.clear();
            txtName.clear();
            txtAddress.clear();
            txtImage.clear();
            new Alert(Alert.AlertType.INFORMATION,"Customer details Updated Successfully!").show();
        }

        txtID.requestFocus();
        disableControls(true);
        brnNewCustomer.setDisable(false);
    }
    //generate image buffer
    private byte[] generateBuffer() throws IOException {

        //converting image to byte array
        Path path = Paths.get(txtImage.getText());
        InputStream is = Files.newInputStream(path);
        byte[] buffer= new byte[is.available()];
        is.read(buffer);
        is.close();
        return buffer;
    }
}
