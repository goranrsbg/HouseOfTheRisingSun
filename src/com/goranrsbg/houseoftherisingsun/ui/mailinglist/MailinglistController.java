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
package com.goranrsbg.houseoftherisingsun.ui.mailinglist;

import com.goranrsbg.houseoftherisingsun.ui.main.MainController;
import com.goranrsbg.houseoftherisingsun.ui.main.SearchRecipient;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;

/**
 * FXML Controller class
 *
 * @author Goran
 */
public class MailinglistController implements Initializable {

    @FXML
    private TableView<SearchRecipient> mailTable;
    @FXML
    private TableColumn<SearchRecipient, Integer> numCol;
    @FXML
    private TableColumn<SearchRecipient, Integer> ppCol;
    @FXML
    private TableColumn<SearchRecipient, String> snCol;
    @FXML
    private TableColumn<SearchRecipient, String> nCol;
    @FXML
    private TableColumn<SearchRecipient, String> dCol;
    @FXML
    private TableColumn<SearchRecipient, String> aCol;

    private ObservableList<SearchRecipient> data;
    private MainController mc;
    
    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        data = FXCollections.observableArrayList();
        mc = MainController.getInstance();
        initTable();
    }

    private void initTable() {
        numCol.setCellValueFactory((param) -> {
            return new ReadOnlyObjectWrapper<>(mailTable.getItems().indexOf(param.getValue()) + 1);
        });
        numCol.setStyle("-fx-alignment: CENTER;");
        ppCol.setCellValueFactory(new PropertyValueFactory<>("postmanPathStep"));
        ppCol.setStyle("-fx-alignment: CENTER;");
        snCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        snCol.setStyle("-fx-alignment: CENTER-LEFT");
        nCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        nCol.setStyle("-fx-alignment: CENTER-LEFT");
        dCol.setCellValueFactory(new PropertyValueFactory<>("details"));
        dCol.setStyle("-fx-alignment: CENTER-LEFT");
        aCol.setCellValueFactory(new PropertyValueFactory<>("locationAddress"));
        aCol.setStyle("-fx-alignment: CENTER-LEFT");
        mailTable.setRowFactory((tableView) -> {
            TableRow<SearchRecipient> row = new TableRow<>();
            row.itemProperty().addListener((obs, previous, current) -> {
                if(current != null) {
                    row.pseudoClassStateChanged(RETIRE_PSEUDO_CLASS, current.getIsRetire());
                } else if(previous != null) {
                    row.pseudoClassStateChanged(RETIRE_PSEUDO_CLASS, false);
                }
            });
            row.setOnMouseClicked((event) -> {
                if(event.getClickCount() == 2) {
                    mc.centerLocationOnTheScreen(row.getItem().getLocationId() + "");
                }
            });
            return row;
        });
        mailTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue != null) {
                mc.selectLocation(oldValue.getLocationId() + "", false);
            }
            if(newValue != null) {
                mc.selectLocation(newValue.getLocationId() + "", true);
            }
        });
        mailTable.setOnKeyPressed((event) -> {
            if(event.getCode() == KeyCode.DELETE) {
                    ObservableList<SearchRecipient> selectedItems = mailTable.getSelectionModel().getSelectedItems();
                    data.removeAll(selectedItems);
                }
        });
        mailTable.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue) {
                mailTable.getSelectionModel().clearSelection();
            }
        });
        mailTable.setItems(data);
    }

    public boolean addRecipient(SearchRecipient recipient) {
        boolean success = false;
        if (!data.contains(recipient)) {
            data.add(recipient);
            success = true;
        }
        return success;
    }

    private final PseudoClass RETIRE_PSEUDO_CLASS = PseudoClass.getPseudoClass("retire");
}
