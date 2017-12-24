package com.goranrsbg.houseoftherisingsun;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Goran
 */
public class LocatorApp extends Application {
    
    public static final String TITLE = "BB-BB-locator";
    
    @Override
    public void start(Stage stage) throws Exception {
        
        Parent root = FXMLLoader.load(getClass().getResource("ui/main/main.fxml"));
        
        final String uri = getClass().getResource("locatorapp.css").toExternalForm();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(uri);
        
        stage.setFullScreen(true);
        stage.setTitle(TITLE);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
