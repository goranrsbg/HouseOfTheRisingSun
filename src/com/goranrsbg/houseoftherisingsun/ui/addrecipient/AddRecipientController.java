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

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;

/**
 * FXML Controller class
 *
 * @author Goran
 */
public class AddRecipientController implements Initializable {

    @FXML
    private JFXTextField lastNameTextField;
    @FXML
    private JFXTextField firstNameTextField;
    @FXML
    private JFXTextField detaiilsTextField;
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

    public AddRecipientController() {
    }

    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        isRetireCheckBox.setOnAction(this::isRetireActionEvent);
    }    
    
    private void isRetireActionEvent(ActionEvent event) {
        if(isRetireCheckBox.isSelected()) {
            retireDetails.setDisable(false);
        } else {
            retireDetails.setDisable(true);
        }
    }
    
}
