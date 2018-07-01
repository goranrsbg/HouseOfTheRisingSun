package com.goranrsbg.houseoftherisingsun;

import com.goranrsbg.houseoftherisingsun.database.DBHandler;
import com.jfoenix.controls.JFXButton;
import com.sun.javafx.application.LauncherImpl;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author Goran
 */
public class LocatorApp extends Application {

    /**
     * The application title.
     */
    private final String TITLE = "BB-BB-lokator";
    /**
     * The main logger.
     */
    private static final Logger LOGGER = Logger.getLogger(LocatorApp.class.getName());
    /**
     * Instance of the application to be shared.
     */
    private static LocatorApp instance;
    /**
     * Main stage.
     */
    private Stage stage;

    public LocatorApp() {
        instance = this;
        initLogger();
    }

    /**
     * Initializes the main logger.
     */
    private void initLogger() {
        String uri = System.getProperty("user.dir") + File.separator + "locator%u.log";
        try {
            FileHandler fh = new FileHandler(uri, 5000000, 1, true);
            fh.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fh);
            LOGGER.setUseParentHandlers(false);
        } catch (IOException | SecurityException ex) {
            System.err.println("Failed to initialize logger.\n" + ex.getMessage());
        }
    }

    /**
     * Instance of the applications starting class.
     *
     * @return Instance of the LocatorApp class.
     */
    public static LocatorApp getInstance() {
        return instance;
    }

    /**
     * Creates the application title in form of <code>TITLE - subtitle</code>
     *
     * @param subtitle String to be added to the main title.
     */
    public void setSubTitle(String subtitle) {
        stage.setTitle(TITLE + " - " + subtitle);
    }

    @Override
    public void init() {
        DBHandler.ceateInstance();
    }

    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        final String uriCss = getClass().getResource("/com/goranrsbg/houseoftherisingsun/locatorapp.css").toExternalForm();
        final String uriIco = Paths.get("", "res", "img").resolve("three.png").toUri().toString();
        Parent parent = FXMLLoader.load(getClass().getResource("/com/goranrsbg/houseoftherisingsun/ui/login/login.fxml"));
        Scene scene = new Scene(parent);
        scene.getStylesheets().add(uriCss);
        stage.getIcons().add(new Image(uriIco));
        setSubTitle("Prijava");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
        LOGGER.info("Login started.");
    }

    @Override
    public void stop() {
        DBHandler.getInstance().closeConnection();
        LOGGER.info("App closed.");
    }

    /**
     * Loads main window after successful logging.
     *
     * @throws IOException
     */
    public void loadMain() throws IOException {
        Parent parent = FXMLLoader.load(getClass().getResource("/com/goranrsbg/houseoftherisingsun/ui/main/main.fxml"));
        Scene scene = stage.getScene();
        scene.rootProperty().setValue(parent);
        stage.setMaximized(true);
        stage.setResizable(true);
        LOGGER.info("App started.");
    }

    /**
     * Creates the applications sub window that is called with the button press.
     *
     * @param pathToFxml Path to sub windows <code>.fxml</code> file.
     * @param btn Button that was cause for opening sub window.
     * @param resizable Can window be resized by the user.
     * @param title Sub window title.
     * @throws IOException
     */
    public void LoadSubWindow(String pathToFxml, JFXButton btn, boolean resizable, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(pathToFxml));
        Parent parent = loader.load();
        Scene newScene = new Scene(parent);
        Stage newStage = new Stage(StageStyle.UTILITY);
        newStage.setUserData(btn);
        btn.setUserData(loader.getController());
        newStage.initOwner(btn.getScene().getWindow());
        newStage.setScene(newScene);
        newStage.setResizable(resizable);
        newStage.setTitle(title);
        newStage.setOnCloseRequest((e) -> {
            Stage s = ((Stage) e.getSource());
            JFXButton b = (JFXButton) s.getUserData();
            b.setDisable(false);
            b.setUserData(null);
        });
        newStage.show();
    }

    /**
     * Launches the application with <code>preloader</code> class.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LauncherImpl.launchApplication(LocatorApp.class, LocatorPreloader.class, args);
    }

}
