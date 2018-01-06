/*
 * Copyright 2017 Goran.
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
package com.goranrsbg.houseoftherisingsun.ui.addAddress;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

/**
 *
 * @author Goran
 */
public class AddAddressController implements Initializable {

    @FXML
    private JFXTextField jFXtfX;
    @FXML
    private JFXTextField jFXtfY;
    @FXML
    private JFXTextField jFXtfBR;
    @FXML
    private JFXTextArea jFXtaNote;
    @FXML
    private JFXButton jFXbtSave;

    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
    }
    
}
