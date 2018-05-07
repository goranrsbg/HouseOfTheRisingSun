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

import com.goranrsbg.houseoftherisingsun.database.DBHandler;
import com.goranrsbg.houseoftherisingsun.ui.main.MainController;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

    @FXML
    private TableView<StreetTable> tableStreets;
    @FXML
    private TableColumn<StreetTable, Integer> colPak;
    @FXML
    private TableColumn<StreetTable, String> colName;
    @FXML
    private TableColumn<StreetTable, String> colInitial;
    
    private final DBHandler db;
    private final ObservableList<StreetTable> data;
    private final MainController mc;

    public ShowStreetsController() {
        db = DBHandler.getInstance();
        data = FXCollections.observableArrayList();
        mc = MainController.getInstance();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initColumns();
    }

    private void initColumns() {
        colPak.setCellValueFactory(new PropertyValueFactory<>("pak"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colInitial.setCellValueFactory(new PropertyValueFactory<>("settlementInitial"));
        tableStreets.setItems(data);
        readStreets();
    }

    public void readStreets() {
        try {
            Statement stmt = db.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT ST.STREET_ID, ST.STREET_PAK, ST.STREET_NAME, SE.SETTLEMENT_INITIALS FROM STREETS AS ST " 
                    + "JOIN SETTLEMENTS AS SE ON ST.SETTLEMENT_ID = SE.SETTLEMENT_ID "
                    + "ORDER BY SE.SETTLEMENT_INITIALS");
            data.clear();
            while(rs.next()) {
                data.add(new StreetTable(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4)));
            }
        } catch (SQLException ex) {
            mc.showMessage(TITLE, "Greška pri učitavanju ulica.\nError: " + ex.getMessage(), MainController.MessageType.ERROR);
        }
    }

    public static final String TITLE = "Spisak ulica";

}
