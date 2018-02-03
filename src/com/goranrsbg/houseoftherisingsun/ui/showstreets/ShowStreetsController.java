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
package com.goranrsbg.houseoftherisingsun.ui.showstreets;

import com.goranrsbg.houseoftherisingsun.database.DBConnector;
import com.goranrsbg.houseoftherisingsun.ui.main.MainController;
import com.goranrsbg.houseoftherisingsun.utility.entity.StreetTableEntity;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * FXML Controller class
 *
 * @author Goran
 */
public class ShowStreetsController implements Initializable {

    public static final String TITLE = "Spisak ulica";

    private final DBConnector db;

    private final ObservableList<StreetTableEntity> data;

    @FXML
    private TableView<StreetTableEntity> tableStreets;
    @FXML
    private TableColumn<StreetTableEntity, Integer> colPak;
    @FXML
    private TableColumn<StreetTableEntity, String> colName;
    @FXML
    private TableColumn<StreetTableEntity, String> colInitial;

    public ShowStreetsController() {
        db = DBConnector.getInstance();
        data = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initColumns();
    }

    private void initColumns() {
        colPak.setCellValueFactory(new PropertyValueFactory<>("pak"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colInitial.setCellValueFactory(new PropertyValueFactory<>("initial"));
        tableStreets.setItems(data);
        data.addAll(db.executeSelectAllStreets());
    }
    
    public void sendMessage(String message, boolean type) {
        MainController.notifyWithMsg(TITLE, message, type);
    }

}
