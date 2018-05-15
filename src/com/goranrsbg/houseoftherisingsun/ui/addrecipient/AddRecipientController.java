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
package com.goranrsbg.houseoftherisingsun.ui.addrecipient;

import com.goranrsbg.houseoftherisingsun.database.DBHandler;
import com.goranrsbg.houseoftherisingsun.ui.main.MainController;
import com.goranrsbg.houseoftherisingsun.ui.showlocation.ShowLocationController;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * FXML Controller class
 *
 * @author Goran
 */
public class AddRecipientController implements Initializable {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private JFXTextField lastNameTextField;
    @FXML
    private JFXTextField firstNameTextField;
    @FXML
    private JFXTextField detailsTextField;
    @FXML
    private JFXCheckBox isRetireCheckBox;
    @FXML
    private HBox retireDetails;
    @FXML
    private JFXTextField idCardNuberTextField;
    @FXML
    private JFXTextField policeDepartmentTextField;
    @FXML
    private JFXButton saveButton;
    @FXML
    private JFXButton cancelButton;

    private final String TITLE = "Dodaj primaoca.";
    private int locationID;
    private DBHandler db;
    private ShowLocationController slc;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        db = DBHandler.getInstance();
        isRetireCheckBox.setOnAction(this::isRetireActionEvent);
        retireDetails.setDisable(true);
        TextFormatter<String> formatterIdCardNumber = new TextFormatter<>((t) -> {
            String textNew = t.getControlNewText();
            if (!textNew.isEmpty()) {
                try {
                    Integer.valueOf(textNew);
                } catch (NumberFormatException e) {
                    t = null;
                    MainController.getInstance().showMessage(TITLE, "Vrednost polja mora da bude ceo broj.", MainController.MessageType.INFORMATION);
                }
            }
            return t;
        });
        idCardNuberTextField.setTextFormatter(formatterIdCardNumber);
    }

    private void isRetireActionEvent(ActionEvent event) {
        if (isRetireCheckBox.isSelected()) {
            retireDetails.setDisable(false);
        } else {
            retireDetails.setDisable(true);
        }
    }

    public AddRecipientController setLocationID(int locationID) {
        this.locationID = locationID;
        return this;
    }
    
    public void setShowLocationController(ShowLocationController slc) {
        this.slc = slc;
    }

    @FXML
    private void saveButtonOnAction(ActionEvent event) {
        String lastName = nameToUpperCase(lastNameTextField.getText());
        String firstName = nameToUpperCase(firstNameTextField.getText());
        String details = detailsTextField.getText();
        boolean isRetire = isRetireCheckBox.isSelected();
        String idCardNumber = idCardNuberTextField.getText();
        String policeDepartment = policeDepartmentTextField.getText();
        if (validateFields(lastName, isRetire, idCardNumber, policeDepartment)) {
            try {
                PreparedStatement ps = db.getStatement(DBHandler.StatementType.INSERT_RECIPIENT);
                ps.setString(1, lastName);
                ps.setString(2, firstName);
                ps.setString(3, details);
                ps.setBoolean(4, isRetire);
                if (isRetire) {
                    ps.setLong(5, Long.parseLong(idCardNumber));
                    ps.setString(6, nameToUpperCase(policeDepartment));
                } else {
                    ps.setNull(5, Types.BIGINT);
                    ps.setNull(6, Types.VARCHAR);
                }
                ps.setInt(7, locationID);
                ps.executeUpdate();
                ps.clearParameters();
                if(slc != null) {
                    slc.loadRecipients();
                }
                MainController.getInstance().showMessage(TITLE, "Primalac " + lastName + " " + firstName + " uspešno dodan.", MainController.MessageType.INFORMATION);
            } catch (SQLException ex) {
                MainController.getInstance().showMessage(TITLE, "Neuspelo dodavanje primaoca.\nGreška: " + ex.getMessage(), MainController.MessageType.ERROR);
            }
        }
    }

    @FXML
    private void cancelButtonOnAction(ActionEvent event) {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    private boolean validateFields(String lastName, boolean isRetire, String idCardNumber, String policeDepartment) {
        boolean valid = true;
        if (locationID < 1) {
            MainController.getInstance().showMessage(TITLE, "Lokacija nije odabrana.", MainController.MessageType.INFORMATION);
            valid = false;
        }
        if (lastName.isEmpty()) {
            MainController.getInstance().showMessage(TITLE, "Polje prezime ili ime firme mora da bude popunjeno.", MainController.MessageType.INFORMATION);
            valid = false;
        }
        if (isRetire && (idCardNumber.isEmpty() || policeDepartment.isEmpty())) {
            MainController.getInstance().showMessage(TITLE, "Polja broj lične karte i policijske uprave moraju da bude popunjena.", MainController.MessageType.INFORMATION);
            valid = false;
        }
        return valid;
    }

    /**
     * Changes name to start with uppercase letter. 
     * goran -> Goran
     * smederevo -> Smederevo
     *
     * @param name name to be changed.
     * @return
     */
    private String nameToUpperCase(String name) {
        if (!name.isEmpty()) {
            char charAt0 = name.charAt(0);
            if (Character.isLowerCase(charAt0)) {
                name = Character.toUpperCase(charAt0) + name.substring(1, name.length());
            }
        }
        return name;
    }

}
