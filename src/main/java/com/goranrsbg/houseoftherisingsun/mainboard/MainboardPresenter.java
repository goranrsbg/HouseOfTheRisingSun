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
package com.goranrsbg.houseoftherisingsun.mainboard;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

/**
 *
 * @author Goran
 */
public class MainboardPresenter implements Initializable {

    private ResourceBundle resources = null;
    
    @FXML
    TextArea textArea;
    
    @FXML
    Button okButton;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
    }
    
    public void click_OK() {
        textArea.appendText("click! ");
    }
    
}
