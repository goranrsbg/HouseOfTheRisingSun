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
package com.goranrsbg.houseoftherisingsun.ui.login;

import com.goranrsbg.houseoftherisingsun.LocatorApp;
import com.goranrsbg.houseoftherisingsun.database.DBHandler;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * FXML Controller class
 *
 * @author Goran
 */
public class LoginController implements Initializable {

    @FXML
    private JFXTextField userName;
    @FXML
    private JFXPasswordField password;
    @FXML
    private VBox root;
    @FXML
    private HBox buttonsBox;
    @FXML
    private JFXButton buttonOk;
    @FXML
    private JFXButton buttonBack;
    
    private boolean validateUname;
    private int validUserId;
    
    private Window window;
    private MessageDigest generator;
    private DBHandler db;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        buttonBack.setText(null);
        buttonBack.setGraphic(new FontAwesomeIconView().setStyleClass("back-icon"));
        root.getChildren().remove(password);
        buttonsBox.getChildren().remove(buttonBack);
        validateUname = true;
        db = DBHandler.getInstance();
        try {
            generator = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            System.err.println(ex.getMessage());
        }
        Platform.runLater(() -> {
            window = root.getScene().getWindow();
            window.sizeToScene();
        });
    }

    @FXML
    private void onKeyReleased(KeyEvent event) {
        if(event.getCode() == KeyCode.ENTER) {
            buttonOk.fire();
        } else if(event.getCode() == KeyCode.ESCAPE && !validateUname) {
            buttonBack.fire();
        }
    }

    @FXML
    private void onOKAction() throws IOException {
        if (validateUname) {
            if (validateUserName(userName.getText())) {
                root.getChildren().remove(userName);
                root.getChildren().add(0, password);
                buttonsBox.getChildren().add(buttonBack);
                password.requestFocus();
                validateUname = false;
                window.sizeToScene();
            } else {
                userName.setText("");
            }
        } else if (validateUserPassword(password.getText(), validUserId)) {
            LocatorApp.getInstance().loadMain();
        } else {
            password.setText("");
        }
    }

    @FXML
    private void onBackAction() {
        root.getChildren().remove(password);
        root.getChildren().add(0, userName);
        buttonsBox.getChildren().remove(buttonBack);
        validateUname = true;
        validUserId = 0;
        window.sizeToScene();
    }

    private boolean validateUserName(String uname) {
        try {
            PreparedStatement ps = db.getStatement(DBHandler.StatementType.SELECT_USER_ID_WITH_NAME);
            ps.setString(1, toSha256String(uname));
            try (ResultSet rs = ps.executeQuery()) {
                ps.clearParameters();
                if(rs.next()) {
                    validUserId = rs.getInt("USER_ID");
                    return true;
                }
            }
            userName.setText("");
            userName.requestFocus();
        } catch (SQLException ex) {
            System.err.println(ex.getSQLState());
        }
        return false;
    }

    private boolean validateUserPassword(String pword, int userId) {
        try {
            PreparedStatement ps = db.getStatement(DBHandler.StatementType.SELECT_USER_PASSWORD_WITH_ID);
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if(rs.getString("USER_PASSWORD").equals(toSha256String(pword))) {
                    rs.close();
                    return true;
                }
            }
        } catch (SQLException ex) {
            System.err.println(ex.getSQLState());
        }
        return false;
    }

    private String toSha256String(String text) {
        byte[] digestedText = generator.digest(text.getBytes());
        // digestedText.toFullSizeHexString()
        StringBuilder sb = new StringBuilder(64);
        for (int i = 0; i < digestedText.length; i++) {
            String val = Integer.toHexString(digestedText[i] & 0xff);
            if (val.length() == 1) {
                sb.append('0');
            }
            sb.append(val);
        }
        return sb.toString();
    }

}
