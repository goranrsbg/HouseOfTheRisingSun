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

import com.goranrsbg.houseoftherisingsun.database.DBHandler;
import com.goranrsbg.houseoftherisingsun.ui.addrecipient.AddRecipientController;
import com.goranrsbg.houseoftherisingsun.ui.main.MainController;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
    private Pattern pattern;

    private Address address;
    private String note;

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
        recipientsTableView.setRowFactory((e) -> {
            TableRow<Recipient> row = new TableRow<>();
            row.setOnMouseClicked((event) -> {
                if (event.getClickCount() == 2) {
                    if (!changeRecipient.isDisable()) {
                        changeRecipient.fire();
                    }
                }
            });
            row.setOnDragDetected((event) -> {
                TableRow<?> source = (TableRow<?>) event.getSource();
                Dragboard sourceStartDragAndDrop = source.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent clipboardContent = new ClipboardContent();
                if (source.getItem() != null) {
                    clipboardContent.putString("RID:" + ((Recipient) source.getItem()).getId() + ",LID:" + idLabel.getText());
                    sourceStartDragAndDrop.setContent(clipboardContent);
                    sourceStartDragAndDrop.setDragView(source.snapshot(null, null), source.getLayoutBounds().getWidth() / 2, source.getLayoutBounds().getHeight() / 2);
                }
                event.consume();
            });
            return row;
        });
        recipientsTableView.setOnDragOver((event) -> {
            if (event.getDragboard().hasString() && pattern.matcher(event.getDragboard().getString()).matches()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        recipientsTableView.setOnDragDropped((event) -> {
            boolean success = false;
            String[] split = event.getDragboard().getString().split(",");
            int recipientId = Integer.parseInt(split[0].split(":")[1]);
            int fromLocation = Integer.parseInt(split[1].split(":")[1]);
            int toLocation = Integer.parseInt(idLabel.getText());
            if (fromLocation != toLocation) {
                try {
                    PreparedStatement ps = db.getStatement(DBHandler.StatementType.UPDATE_RECIPIENT_LOCATION);
                    ps.setInt(1, toLocation);
                    ps.setInt(2, recipientId);
                    ps.executeUpdate();
                    ps.clearParameters();
                    loadRecipients();
                    MainController.getInstance().refreshRecipients(fromLocation);
                    MainController.getInstance().showMessage(TITLE, "Primalac uspešno premešten.", MainController.MessageType.INFORMATION);
                    success = true;
                } catch (SQLException ex) {
                    MainController.getInstance().showMessage(TITLE, "Greška pri premeštanju primaoca.\nError: " + ex.getMessage(), MainController.MessageType.ERROR);
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public boolean setLocation(int locationId) {
        boolean result = false;
        if (!addRecipient.isDisabled()) {
            try {
                PreparedStatement ps = db.getStatement(DBHandler.StatementType.SELECT_STREET_NAME_LOCATION_NUMBER_LOCATION_PPSTEP_LOCATION_NOTE);
                ps.setInt(1, locationId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String streetName = rs.getString("STREET_NAME");
                        String locationNumber = rs.getString("LOCATION_ADDRESS_NO");
                        int postman_path_step = rs.getInt("LOCATION_POSTMAN_PATH_STEP");
                        this.note = rs.getString("LOCATION_NOTE");
                        this.address = new Address(streetName, locationNumber, postman_path_step);
                        idLabel.setText(String.format("%06d", locationId));
                        addressNameLabel.setText(address.toString());
                        setTitle(address.toString());
                        result = true;
                        MainController.getInstance().showMessage(TITLE, "Lokacija " + address.toString() + " je prikazana.", MainController.MessageType.INFORMATION);
                    } else {
                        setTitle("prazna");
                        MainController.getInstance().showMessage(TITLE, "Lokacija sa identifikacionim brojem #" + locationId + " ne postoji.", MainController.MessageType.ERROR);
                    }
                    ps.clearParameters();
                }
            } catch (SQLException ex) {
                MainController.getInstance().showMessage(TITLE, "Greška pri selektovanju lokacije #" + locationId + "\nError: " + ex.getMessage(), MainController.MessageType.ERROR);
            }
        } else {
            MainController.getInstance().showMessage(TITLE, "Potrebno je zatvoriti pomoćne prozore da bi bila prikazana nova lokacija.", MainController.MessageType.ERROR);
        }
        return result;
    }

    /**
     * Changes title of the window to location - <italic>title</italic>
     *
     * @param title
     */
    private void setTitle(String title) {
        Platform.runLater(() -> {
            ((Stage) addressNameLabel.getScene().getWindow()).setTitle("Lokacija - " + title);
        });
    }

    /**
     * Returns location ID if location is loaded, or 0 if none location is
     * loaded.
     *
     * @return
     */
    public int getLocationID() {
        return idLabel.getText().isEmpty() ? 0 : Integer.parseInt(idLabel.getText());
    }

    public void requestFocus() {
        idLabel.getScene().getWindow().requestFocus();
    }

    public void loadRecipients() {
        if (idLabel.getText().isEmpty()) {
            MainController.getInstance().showMessage(TITLE, "Selektovanje primalaca nije moguće jer lokacija nije izabrana.", MainController.MessageType.INFORMATION);
        } else {
            try {
                PreparedStatement ps = db.getStatement(DBHandler.StatementType.SELECT_RECIPIENTS_ON_LOCATION_ID);
                ps.setInt(1, Integer.parseInt(idLabel.getText()));
                try (ResultSet rs = ps.executeQuery()) {
                    recipients.clear();
                    while (rs.next()) {
                        recipients.add(new Recipient(rs.getInt("RECIPIENT_ID"), rs.getString("RECIPIENT_LAST_NAME"), rs.getString("RECIPIENT_FIRST_NAME"),
                                rs.getString("RECIPIENT_DETAILS"), rs.getBoolean("RECIPIENT_IS_RETIREE"), rs.getLong("RECIPIENT_ID_CARD_NUMBER"),
                                rs.getString("RECIPIENT_ID_CARD_POLICE_DEPARTMENT")));
                    }
                    ps.clearParameters();
                }
                if (recipients.isEmpty()) {
                    MainController.getInstance().showMessage(TITLE, "Izabrana lokacija nema primalaca.", MainController.MessageType.INFORMATION);
                }
            } catch (SQLException ex) {
                MainController.getInstance().showMessage(TITLE, "Greška prilikom učitavanja primalaca.\n" + ex.getMessage(), MainController.MessageType.ERROR);
            }
        }
    }

    @FXML
    private void deleteLocationOnAction(ActionEvent event) {
        if (!recipients.isEmpty()) {
            sendMessage("Lokacija mora da bude bez primaoca da bi brisanje bilo moguće.", MainController.MessageType.INFORMATION);
            return;
        }
        ButtonType yes = new ButtonType("Da", ButtonBar.ButtonData.YES);
        ButtonType no = new ButtonType("Ne", ButtonBar.ButtonData.NO);
        Alert alert = new Alert(Alert.AlertType.NONE, "Da li si siguran?", yes, no);
        alert.showAndWait().ifPresent((t) -> {
            if (t == yes) {
                try {
                    PreparedStatement ps = db.getStatement(DBHandler.StatementType.DELETE_LOCATION);
                    ps.setInt(1, getLocationID());
                    ps.executeUpdate();
                    MainController.getInstance().clearLocationFromLocationPane(getLocationID() + "");
                    sendMessage("Lokacije " + addressNameLabel.getText() + " uspešno obrisana.", MainController.MessageType.INFORMATION);
                    idLabel.getScene().getWindow().hide();
                } catch (SQLException ex) {
                    sendMessage("Neuspelo brisanje lokacije.\nError: " + ex.getMessage(), MainController.MessageType.ERROR);
                }
            }
        });
    }

    @FXML
    private void addRecipientOnAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/goranrsbg/houseoftherisingsun/ui/addrecipient/addrecipient.fxml"));
            loader.load();
            Stage stage = new Stage(StageStyle.UTILITY);
            stage.setScene(new Scene(loader.getRoot()));
            stage.initOwner(idLabel.getScene().getWindow());
            AddRecipientController controller = (AddRecipientController) loader.getController();
            controller.setLocationID(Integer.parseInt(idLabel.getText())).setShowLocationController(this);
            controller.setTitle("Novi primalac - " + addressNameLabel.getText());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException ex) {
            MainController.getInstance().showMessage(TITLE, "Greška pri učitavanju .fxml faja.\n" + ex.getMessage(), MainController.MessageType.ERROR);
        }
    }

    @FXML
    private void changeRecipientOnAction(ActionEvent event) {
        Recipient recipient = recipientsTableView.getSelectionModel().getSelectedItem();
        if (recipient != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/goranrsbg/houseoftherisingsun/ui/addrecipient/addrecipient.fxml"));
                loader.load();
                Stage stage = new Stage(StageStyle.UTILITY);
                stage.setScene(new Scene(loader.getRoot()));
                stage.initOwner(idLabel.getScene().getWindow());
                AddRecipientController controller = (AddRecipientController) loader.getController();
                controller.setLocationID(Integer.parseInt(idLabel.getText())).setShowLocationController(this);
                controller.setRecipient(recipient);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();
            } catch (IOException ex) {
                MainController.getInstance().showMessage(TITLE, "Greška pri učitavanju .fxml faja.\n" + ex.getMessage(), MainController.MessageType.ERROR);
            }
        } else {
            recipientsTableView.requestFocus();
            MainController.getInstance().showMessage(TITLE, "Primalac nije izabran.", MainController.MessageType.INFORMATION);
        }
    }

    @FXML
    private void deleteRecipientOnAction(ActionEvent event) {
        Recipient recipient = recipientsTableView.getSelectionModel().getSelectedItem();
        if (recipient != null) {
            ButtonType da = new ButtonType("Da", ButtonBar.ButtonData.YES);
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Primalac " + recipient.getFullName() + " biće trajno uklonjen. \n\nDa li ste sigurni?", da, new ButtonType("Ne", ButtonBar.ButtonData.NO));
            alert.showAndWait();
            if (alert.getResult() == da) {
                try {
                    PreparedStatement ps = db.getStatement(DBHandler.StatementType.DELETE_RECIPIENT_WITH_ID);
                    ps.setInt(1, recipient.getId());
                    ps.executeUpdate();
                    recipients.remove(recipient);
                    ps.clearParameters();
                    MainController.getInstance().showMessage(TITLE, "Primalac " + recipient.getFullName() + " uspešno obrisan.", MainController.MessageType.INFORMATION);
                } catch (SQLException ex) {
                    MainController.getInstance().showMessage(TITLE, "Greška pri brisanju primaoca " + recipient.getFullName() + ".\n" + ex.getMessage(), MainController.MessageType.ERROR);
                }
            }
        } else {
            MainController.getInstance().showMessage(TITLE, "Primalac nije izabran.", MainController.MessageType.INFORMATION);
        }
    }

    @FXML
    private void changeLocationNumberOnAction(ActionEvent event) {
        Dialog dialog = createDialog(address.getAddressNumber(), address.getPostmanPathStep() + "");
        Optional locUpdate = dialog.showAndWait();
        if (locUpdate.isPresent()) {
            try {
                LocationUpdate lu = (LocationUpdate) locUpdate.get();
                PreparedStatement ps = db.getStatement(DBHandler.StatementType.UPDATE_LOCATION_NUMBER_PPSTEP);
                ps.setString(1, lu.getNumber());
                ps.setInt(2, lu.getPpStep());
                ps.setString(3, lu.getNote());
                ps.setInt(4, Integer.parseInt(idLabel.getText()));
                ps.executeUpdate();
                ps.clearParameters();
                address.setPostmanPathStep(lu.getPpStep());
                address.setAddressNumber(lu.getNumber());
                addressNameLabel.setText(address.toString());
                this.note = lu.getNote();
                MainController.getInstance().updateLocationText(idLabel.getText().replaceFirst("0*", ""), lu.getNumber(), lu.getNote());
                sendMessage("Adresa lokacije uspešno promenjena.", MainController.MessageType.INFORMATION);
            } catch (SQLException ex) {
                sendMessage("Greška prilikom promene adrese.\nError: " + ex.getMessage(), MainController.MessageType.ERROR);
            }
        }
    }

    /**
     * Dialog for updating location number and postman path step.
     *
     * @param addressNumber Current number.
     * @param ppStep Current postman path step.
     * @return
     */
    private Dialog createDialog(String addressNumber, String ppStep) {
        Dialog<LocationUpdate> dialog = new Dialog<>();
        dialog.setTitle("Ažuriranje adrese.");
        TextFormatter<String> formatterNumber = new TextFormatter<>((t) -> {
            String textNew = t.getControlNewText();
            if (!textNew.isEmpty()) {
                if (textNew.length() > 23) {
                    t = null;
                    sendMessage("Vrednost polja mora da bude do 23 znaka.", MainController.MessageType.INFORMATION);
                }
            }
            return t;
        });
        TextFormatter<String> formatterPostmanPath = new TextFormatter<>((t) -> {
            String textNew = t.getControlNewText();
            if (!textNew.isEmpty()) {
                try {
                    Integer.valueOf(textNew);
                } catch (NumberFormatException e) {
                    t = null;
                    sendMessage("Vrednost polja mora da bude ceo broj.", MainController.MessageType.INFORMATION);
                }
            }
            return t;
        });
        JFXTextField number = new JFXTextField();
        JFXTextField pStep = new JFXTextField();
        JFXTextArea noteArea = new JFXTextArea(this.note);
        noteArea.setPrefColumnCount(19);
        noteArea.setPrefRowCount(3);
        number.setLabelFloat(true);
        pStep.setLabelFloat(true);
        number.setPromptText("Broj adrese");
        pStep.setPromptText("Broj poštarevog puta");
        number.setText(addressNumber);
        pStep.setText(ppStep);
        number.setTextFormatter(formatterNumber);
        pStep.setTextFormatter(formatterPostmanPath);
        GridPane pane = new GridPane();
        pane.add(new Label("Broj:"), 0, 0);
        pane.add(new Label("Poštarev put:"), 0, 1);
        pane.add(new Label("Detalj:"), 0, 2);
        pane.add(number, 1, 0);
        pane.add(pStep, 1, 1);
        pane.add(noteArea, 1, 2);
        pane.setHgap(13d);
        pane.setVgap(13d);
        dialog.getDialogPane().setContent(pane);
        dialog.initStyle(StageStyle.UTILITY);
        ButtonType save = new ButtonType("Sačuvaj", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Otkaži", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(save, cancel);
        dialog.setResultConverter((param) -> {
            if (param == save && !number.getText().isEmpty()) {
                return new LocationUpdate(number.getText(), Integer.parseInt(pStep.getText()), noteArea.getText());
            }
            return null;
        });
        return dialog;
    }

    private void sendMessage(String message, MainController.MessageType type) {
        MainController.getInstance().showMessage("Lokacija: " + addressNameLabel.getText(), message, type);
    }

}
