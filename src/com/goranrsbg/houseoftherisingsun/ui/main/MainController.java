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
package com.goranrsbg.houseoftherisingsun.ui.main;

import com.goranrsbg.houseoftherisingsun.database.DBConnector;
import com.goranrsbg.houseoftherisingsun.ui.addAddress.AddAddressController;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXNodesList;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.controlsfx.control.Notifications;

/**
 *
 * @author Goran
 */
public class MainController implements Initializable {

    private static MainController instance;
    private AddAddressController addAddressController;

    private final DBConnector db;

    private final Path pathToTheMaps;
    private double imgHeight;
    private double imgWidth;

    private int currentMap;

    private final List<Settlement> settlements;

    // stages buttons
    JFXButton buttonAddRecipient;
    JFXButton buttonAddLocation;
    JFXButton buttonShowStreets;
    JFXButton buttonShowSettlements;

    @FXML
    private Label output;
    @FXML
    private ScrollPane theMapPane;
    @FXML
    private ImageView theMapImage;
    @FXML
    private JFXNodesList jFXNodeList;

    public MainController() {
        db = DBConnector.getInstance();
        settlements = new ArrayList<>();
        collectSettlements();
        pathToTheMaps = Paths.get("", "res", "maps");
        instance = this;
    }

    private void collectSettlements() {
        final String query = "SELECT * FROM SETTLEMENTS";
        ResultSet rs = db.executeQuery(query);
        try {
            while (rs.next()) {
                settlements.add(new Settlement(rs.getInt("ID"), rs.getString("NAME"), rs.getString("INITIAL")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static MainController getInstance() {
        return instance;
    }

    public Settlement getCurrentMap() {
        return settlements.get(currentMap);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        db.setMc(this);
        theMapPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        theMapPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        currentMap = 0;

        loadTheMap();

        buttonAddLocation = createGlyphJFXButton(2, "marker-icon", "Dadaj lokaciju.");
        buttonAddLocation.setOnAction(this::addWindowLoadOnActionEvent);
        buttonAddLocation.setUserData(StagesNames.ADD_LOCATION);

        buttonAddRecipient = createGlyphJFXButton(1, "addrecip-icon", "Dodaj primaoca.");
        buttonShowStreets = createGlyphJFXButton(3, "roadt-icon", "Prikaži spisak ulica.");
        buttonShowSettlements = createGlyphJFXButton(3, "listol-icon", "Prikaži spisak naselja.");

        jFXNodeList.addAnimatedNode(createGlyphJFXButton(0, "start-icon", "Start"));

        JFXNodesList recipientsJFXNodesList = new JFXNodesList();
        recipientsJFXNodesList.addAnimatedNode(createGlyphJFXButton(1, "recip-icon", "Primaoci:"));
        recipientsJFXNodesList.addAnimatedNode(buttonAddRecipient);

        JFXNodesList locationsJFXNodesList = new JFXNodesList();
        locationsJFXNodesList.addAnimatedNode(createGlyphJFXButton(2, "loc-icon", "Lokacije:"));
        locationsJFXNodesList.addAnimatedNode(buttonAddLocation);

        JFXNodesList streetsJFXNodesList = new JFXNodesList();
        streetsJFXNodesList.addAnimatedNode(createGlyphJFXButton(3, "road-icon", "Ulice:"));
        streetsJFXNodesList.addAnimatedNode(buttonShowStreets);

        JFXNodesList settlementsJFXNodesList = new JFXNodesList();
        settlementsJFXNodesList.addAnimatedNode(createGlyphJFXButton(3, "map-icon", "Naselja:"));
        JFXNodesList settlementsMapChooserList = new JFXNodesList();
        settlementsMapChooserList.setRotate(90d);
        settlementsMapChooserList.addAnimatedNode(createGlyphJFXButton(3, "settchooser-icon", "Izaberi mapu naselja."));
        for (int i = 0; i < settlements.size(); i++) {
            Settlement s = settlements.get(i);
            JFXButton button = createTextJFXButton(s.getINITIALS(), s.getNAME(), "settlement");
            button.setOnAction(this::addMapChangeOnActionEvent);
            button.setUserData(i);
            settlementsMapChooserList.addAnimatedNode(button);
        }
        settlementsJFXNodesList.addAnimatedNode(settlementsMapChooserList);
        settlementsJFXNodesList.addAnimatedNode(buttonShowSettlements);

        jFXNodeList.addAnimatedNode(recipientsJFXNodesList);
        jFXNodeList.addAnimatedNode(locationsJFXNodesList);
        jFXNodeList.addAnimatedNode(streetsJFXNodesList);
        jFXNodeList.addAnimatedNode(settlementsJFXNodesList);
    }

    private JFXButton createTextJFXButton(String text, String toolTip, String cssClass) {
        String defaultButtonCssClass = "animated-option-button";
        JFXButton button = new JFXButton(text);
        button.getStyleClass().addAll(defaultButtonCssClass, cssClass);
        button.setTooltip(createTooltip(toolTip));
        return button;
    }

    private JFXButton createGlyphJFXButton(int buttonCssClass, String glyphIconCssClass, String toolTip) {
        String defaultButtonCssClass = "animated-option-button";
        String buttonClass = "animated-option-sub-button";
        FontAwesomeIconView icon = new FontAwesomeIconView();
        icon.setStyleClass(glyphIconCssClass);
        JFXButton button = new JFXButton(null, icon);
        button.getStyleClass().addAll(defaultButtonCssClass, buttonClass + buttonCssClass);
        button.setTooltip(createTooltip(toolTip));
        return button;
    }

    private Tooltip createTooltip(String text) {
        String defaultToolTipStyle = "-fx-font: normal bold 15px 'Oxygen'; -fx-base: #AE3522; -fx-text-fill: orange;";
        Tooltip tt = new Tooltip();
        tt.setText(text);
        tt.setStyle(defaultToolTipStyle);
        return tt;
    }

    public void loadTheMap() {
        String mapName = settlements.get(currentMap).getNAME();
        String uri = pathToTheMaps.resolve(mapName + ".bmp").toUri().toString();
        Image img = new Image(uri);
        imgHeight = img.getHeight();
        imgWidth = img.getWidth();
        theMapImage.setImage(img);
        theMapPane.setContent(theMapImage);
    }

    public void notifyWithMsg(String title, String message) {
        Notifications.create()
                .title(title)
                .text(message)
                .showInformation();
    }

    private void addWindowLoadOnActionEvent(ActionEvent event) {
        switch ((StagesNames) ((JFXButton) event.getSource()).getUserData()) {
            case ADD_LOCATION:
                loadWindow("/com/goranrsbg/houseoftherisingsun/ui/addAddress/addaddress.fxml", "Dodaj lokaciju", ((JFXButton) event.getSource()).getUserData());
                buttonAddLocation.setDisable(true);
                break;
        }
    }

    private void loadWindow(String location, String title, Object userData) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(location));
            Parent parent = loader.load();
            addAddressController = (AddAddressController) loader.getController();
            Stage stage = new Stage(StageStyle.UTILITY);
            Scene newScene = new Scene(parent);
            stage.setScene(newScene);
            stage.setAlwaysOnTop(true);
            stage.setTitle(title);
            stage.setResizable(false);
            stage.setOnCloseRequest(this::addClosingEvent);
            stage.setUserData(userData);
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addClosingEvent(WindowEvent event) {
        switch ((StagesNames) ((Stage) event.getSource()).getUserData()) {
            case ADD_LOCATION:
                buttonAddLocation.setDisable(false);
                break;
        }
    }

    private void addMapChangeOnActionEvent(ActionEvent event) {
        int id = (int) ((JFXButton) event.getSource()).getUserData();
        if (currentMap != id) {
            currentMap = id;
            loadTheMap();
            if (buttonAddLocation.isDisabled()) {
                addAddressController.comboBoxAddStreets();
                addAddressController.clearLocationXY();
            }
        }
    }

    @FXML
    private void mouseClicked(MouseEvent event) {
        MouseButton mb = event.getButton();
        final double x = event.getX();
        final double y = event.getY();
        if (mb == MouseButton.SECONDARY) {
            final double w = theMapPane.getWidth();
            final double h = theMapPane.getHeight();
            output.setText("X: " + x + " Y: " + y + "      W: " + w + " H: " + h + " iW: " + imgWidth + " iH: " + imgHeight);
            // move right click point to center of the screen if possible
            if (imgWidth > w) {
                theMapPane.setHvalue((x - w / 2) / (imgWidth - w));
            }
            if (imgHeight > h) {
                theMapPane.setVvalue((y - h / 2) / (imgHeight - h));
            }
        } else if (mb == MouseButton.PRIMARY) {
            if (buttonAddLocation.isDisabled()) {
                addAddressController.setLocationXY(x, y);
            }
        }
    }

}
