/*
 * Copyright 2018 Goran.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.goranrsbg.houseoftherisingsun.ui.showlocation;

import com.goranrsbg.houseoftherisingsun.LocatorApp;
import com.goranrsbg.houseoftherisingsun.database.DBHandler;
import com.goranrsbg.houseoftherisingsun.ui.addrecipient.AddRecipientController;
import com.goranrsbg.houseoftherisingsun.ui.main.MainController;
import com.jfoenix.controls.JFXButton;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * FXML Controller class
 *
 * @author Goran
 */
public class ShowLocationController implements Initializable {

    @FXML
    private Label idLabel;
    @FXML
    private Label addressNameLabel;
    @FXML
    private JFXButton addRecipient;
    @FXML
    private JFXButton changeRecipient;
    @FXML
    private JFXButton deleteRecipient;
    @FXML
    private JFXButton updateLocation;
    @FXML
    private JFXButton deleteLocation;
    @FXML
    private TableView<Recipient> recipientsTableView;
    @FXML
    private TableColumn<Recipient, String> firstNameTableColumn;
    @FXML
    private TableColumn<Recipient, String> detailsTableColumn;
    @FXML
    private TableColumn<Recipient, String> lastNameTableColumn;

    private ObservableList<Recipient> recipients;
    private DBHandler db;
    private final String TITLE = "Prikaz lokacije.";

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        recipients = FXCollections.observableArrayList();
        db = DBHandler.getInstance();
        initColumns();
    }

    private void initColumns() {
        firstNameTableColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameTableColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        detailsTableColumn.setCellValueFactory(new PropertyValueFactory<>("details"));
        recipientsTableView.setItems(recipients);
    }

    public void setLocation(int locationId) {
        try {
            PreparedStatement ps = db.getStatement(DBHandler.StatementType.SELECT_STREET_NAME_AND_LOCATION_NUMBER);
            ps.setInt(1, locationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String streetName = rs.getString("STREET_NAME");
                    String locationNumber = rs.getString("LOCATION_ADDRESS_NO");
                    idLabel.setText(String.format("%06d", locationId));
                    addressNameLabel.setText(streetName + " " + locationNumber);
                    MainController.getInstance().showMessage(TITLE, "Lokacija " + streetName + " " + locationNumber + " je prikazana.", MainController.MessageType.INFORMATION);
                } else {
                    MainController.getInstance().showMessage(TITLE, "Lokacija sa identifikacionim brojem #" + locationId + " ne postoji.", MainController.MessageType.ERROR);
                }
                ps.clearParameters();
            }
        } catch (SQLException ex) {
            MainController.getInstance().showMessage(TITLE, "Greška pri selektovanju lokacije #" + locationId + "\nError: " + ex.getMessage(), MainController.MessageType.ERROR);
        }

    }

    public void loadRecipients() {

        // TO DO
        
    }

    @FXML
    private void deleteLocationOnAction(ActionEvent event) {
    }

    @FXML
    private void addRecipientOnAction(ActionEvent event) {
        try {
            JFXButton btn = (JFXButton)event.getSource();
            LocatorApp.getInstance().LoadSubWindow("/com/goranrsbg/houseoftherisingsun/ui/addrecipient/addrecipient.fxml", btn, false, "Dodaj primaoca.");
            btn.setDisable(true);
            ((AddRecipientController)btn.getUserData()).setLocationID(Integer.parseInt(idLabel.getText()));
        } catch (IOException ex) {
            MainController.getInstance().showMessage(TITLE, "Greška pri učitavanju .fxml faja.\n" + ex.getMessage(), MainController.MessageType.ERROR);
        }

    }

    @FXML
    private void changeRecipientOnAction(ActionEvent event) {
    }

    @FXML
    private void deleteRecipientOnAction(ActionEvent event) {
    }

}
