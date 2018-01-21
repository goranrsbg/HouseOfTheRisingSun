package com.goranrsbg.houseoftherisingsun;

import com.goranrsbg.houseoftherisingsun.database.DBConnector;
import com.sun.javafx.application.LauncherImpl;
import java.nio.file.Paths;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author Goran
 */
public class LocatorApp extends Application {

    public static final String TITLE = "BB-BB-lokator";
    
    private static Stage primaryStage;
    
    public static void setSubTitle(String subtitle) {
        primaryStage.setTitle(TITLE + " - " + subtitle);
    }
    
    @Override
    public void init() {
        DBConnector.ceateInstance();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        LocatorApp.primaryStage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("ui/main/main.fxml"));
        final String uriCss = getClass().getResource("locator.css").toExternalForm();
        final String uriIco = Paths.get("", "res", "img").resolve("three.png").toUri().toString();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(uriCss);
        primaryStage.getIcons().add(new Image(uriIco));
        primaryStage.setMaximized(true);
        primaryStage.setTitle(TITLE);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        DBConnector.getInstance().closeConnection();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LauncherImpl.launchApplication(LocatorApp.class, LocatorPreloader.class, args);
    }

}
