package com.goranrsbg.houseoftherisingsun;

import com.goranrsbg.houseoftherisingsun.database.DBConnector;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/**
 *
 * @author Goran
 */
public class LocatorApp extends Application {
    
    public static final String TITLE = "BB-BB-locator";
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        
        DBConnector.ceateInstance();
        
        Parent root = FXMLLoader.load(getClass().getResource("ui/main/main.fxml"));
        
        root.setOnKeyPressed((event) -> {
            if(event.isAltDown() && event.getCode() == KeyCode.ENTER) {
                primaryStage.setFullScreen(true);
            }
        });
        
        final String uri = getClass().getResource("locatorapp.css").toExternalForm();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(uri);
        
        primaryStage.setFullScreen(true);
        primaryStage.setTitle(TITLE);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
