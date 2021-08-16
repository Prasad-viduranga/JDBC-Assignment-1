package lk.ijse.jdbc_assignment1.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lk.ijse.jdbc_assignment1.tm.ProviderTM;

import java.io.IOException;
import java.sql.*;

public class ManageProvideFormController {
    public TextField txtProviderID;
    public TextField txtProviderName;
    public TableView<ProviderTM> tblProviders;
    private Connection connection;
    private PreparedStatement pstmSaveProvider;
    private PreparedStatement pstmDeleteProvider;

    public void initialize() {
        tblProviders.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblProviders.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<ProviderTM, Button> colDelete = (TableColumn<ProviderTM, Button>) tblProviders.getColumns().get(2);

        txtProviderName.setOnAction(this::btnSave_OnAction);

        colDelete.setCellValueFactory(param -> {
            Button btnDelete = new Button("Remove");
            btnDelete.setOnAction(event -> {
                int id = param.getValue().getId();
                try {
                    pstmDeleteProvider.setInt(1, id);
                    int affectedRows = pstmDeleteProvider.executeUpdate();
                    if (affectedRows != 0) {
                        new Alert(Alert.AlertType.CONFIRMATION, "Provider deleted").show();
                        tblProviders.getItems().remove(param.getValue());
                    } else {
                        throw new RuntimeException("Unable to delete that provider");
                    }

                } catch (SQLException | RuntimeException e) {
                    new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
                    e.printStackTrace();
                }

            });
            return new ReadOnlyObjectWrapper<>(btnDelete);
        });
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dep7", "root", "prasad");
            pstmSaveProvider = connection.prepareStatement("INSERT INTO provider(id,provider) VALUES (?,?);");
            pstmDeleteProvider = connection.prepareStatement("DELETE FROM provider WHERE id=?;");

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        loadAllProviders();
    }

    private void loadAllProviders() {
        tblProviders.getItems().clear();
        try {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM provider");
            while (resultSet.next()) {
                tblProviders.getItems().add(new ProviderTM(resultSet.getInt(1), resultSet.getString(2)));

            }
            tblProviders.refresh();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void btnBack_OnAction(ActionEvent actionEvent) throws IOException {
        HomeFormController.navigate(HomeFormController.NavigationMenu.HOME);
    }

    public void btnSave_OnAction(ActionEvent actionEvent) {
        String id = txtProviderID.getText();
        String providerName = txtProviderName.getText();
        if (id.trim().isEmpty() || providerName.trim().isEmpty() || !id.matches("\\d+")) {
            new Alert(Alert.AlertType.ERROR, "Please enter valid id").show();
            return;
        }
        try {
            pstmSaveProvider.setInt(1, Integer.parseInt(id));
            pstmSaveProvider.setString(2, providerName);
            if (pstmSaveProvider.executeUpdate() == 1) {
                new Alert(Alert.AlertType.CONFIRMATION, "Provider has been saved successfully").show();
                tblProviders.getItems().add(new ProviderTM(Integer.parseInt(id), providerName));
                tblProviders.refresh();
                txtProviderID.clear();
                txtProviderName.clear();
                txtProviderID.requestFocus();
            } else {
                throw new RuntimeException("Failed to save the provider");
            }

        } catch (SQLException | RuntimeException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }

    }
}