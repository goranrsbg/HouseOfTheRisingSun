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
package com.goranrsbg.houseoftherisingsun.ui.retireonly;

import com.goranrsbg.houseoftherisingsun.database.DBHandler;
import com.goranrsbg.houseoftherisingsun.ui.main.MainController;
import com.goranrsbg.houseoftherisingsun.ui.main.SearchRecipient;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
public class RetireonlyController implements Initializable {

    @FXML
    private TableView<SearchRecipient> table;
    @FXML
    private TableColumn<SearchRecipient, Integer> noCol;
    @FXML
    private TableColumn<SearchRecipient, String> lastNameCol;
    @FXML
    private TableColumn<SearchRecipient, String> firstNameCol;
    @FXML
    private TableColumn<SearchRecipient, Long> idCardNumberCol;
    @FXML
    private TableColumn<SearchRecipient, String> policeDepartmentCol;
    @FXML
    private TableColumn<SearchRecipient, String> detailsCol;
    /**
     * The table items.
     */
    private ObservableList<SearchRecipient> data;
    private final String TITLE;

    public RetireonlyController() {
        TITLE = "Prikaz pen/neg/pom";
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        data = FXCollections.observableArrayList();
        noCol.setCellValueFactory((param) -> {
            return new ReadOnlyObjectWrapper<>(data.indexOf(param.getValue()) + 1);
        });
        noCol.setStyle("-fx-alignment: CENTER;");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lastNameCol.setStyle("-fx-alignment: CENTER-LEFT;");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        firstNameCol.setStyle("-fx-alignment: CENTER-LEFT;");
        idCardNumberCol.setCellValueFactory(new PropertyValueFactory<>("idCardNumber"));
        idCardNumberCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        policeDepartmentCol.setCellValueFactory(new PropertyValueFactory<>("policeDepartment"));
        policeDepartmentCol.setStyle("-fx-alignment: CENTER;");
        detailsCol.setCellValueFactory(new PropertyValueFactory<>("details"));
        detailsCol.setStyle("-fx-alignment: CENTER-LEFT;");
        table.setItems(data);
        loadData();
    }

    private void loadData() {
        if(!MainController.getInstance().isMapLoaded()) {
            return;
        }
        int id = MainController.getInstance().getLoadedMapId();
        PreparedStatement ps = DBHandler.getInstance().getStatement(DBHandler.StatementType.SELECT_RETIRE_RECIPIENTS);
        try {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int rid = rs.getInt("RECIPIENT_ID");
                    String lname = rs.getString("RECIPIENT_LAST_NAME");
                    String fname = rs.getString("RECIPIENT_FIRST_NAME");
                    String details = rs.getString("RECIPIENT_DETAILS");
                    boolean isRetiree = rs.getBoolean("RECIPIENT_IS_RETIREE");
                    long cnumber = rs.getLong("RECIPIENT_ID_CARD_NUMBER");
                    String pdepartment = rs.getString("RECIPIENT_ID_CARD_POLICE_DEPARTMENT");
                    int lid = rs.getInt("LOCATION_ID");
                    String sname = rs.getString("STREET_NAME");
                    String lno = rs.getString("LOCATION_ADDRESS_NO");
                    int pps = rs.getInt("LOCATION_POSTMAN_PATH_STEP");
                    data.add(new SearchRecipient(rid, lname, fname, details, isRetiree, cnumber, pdepartment, lid, sname + " " + lno, pps));
                }
            }
        } catch (SQLException ex) {
            MainController.getInstance().showMessage(TITLE, "Neuspelo čitanje iz baze.\nError: " + ex.getMessage(), MainController.MessageType.ERROR);
        }
    }

}
