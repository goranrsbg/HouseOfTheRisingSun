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
package com.goranrsbg.houseoftherisingsun;

import com.airhacks.afterburner.injection.Injector;
import com.goranrsbg.houseoftherisingsun.mainboard.MainboardView;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Goran
 */
public class App extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        
        Map<String, String> customProperties = new HashMap<>();
        customProperties.put("title", "BB-BB-locator");
        customProperties.put("author", "Goran Cvijanović");
        customProperties.put("date", LocalDateTime.now().toString());
        
        Injector.setConfigurationSource(customProperties::get);
        
        MainboardView appView = new MainboardView();
        Scene scene = new Scene(appView.getView());
        stage.setFullScreen(true);
        stage.setTitle(customProperties.get("title"));
        String uri = getClass().getResource("app.css").toExternalForm();
        scene.getStylesheets().add(uri);
        stage.setScene(scene);
        stage.show();
    }
    
    @Override
    public void stop() throws Exception {
        Injector.forgetAll();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
}
