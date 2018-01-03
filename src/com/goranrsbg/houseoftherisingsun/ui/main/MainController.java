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

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author Goran
 */
public class MainController implements Initializable {

    private Path pathToTheMaps;
    private double imgHeight;
    private double imgWidth;

    @FXML
    private Label output;
    @FXML
    private ScrollPane theMapPane;
    @FXML
    private ImageView theMapImage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pathToTheMaps = Paths.get("", "res", "maps");
        theMapPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        theMapPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        loadTheMap("Kolari.bmp");
    }

    public void loadTheMap(String mapName) {
        String uri = pathToTheMaps.resolve(mapName).toUri().toString();

        Image img = new Image(uri);
        imgHeight = img.getHeight();
        imgWidth = img.getWidth();

        theMapImage.setImage(img);

        theMapPane.setContent(theMapImage);

        output.setText(uri);
    }

    @FXML
    private void mouseClicked(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            final double x = event.getX();
            final double y = event.getY();
            final double w = theMapPane.getWidth();
            final double h = theMapPane.getHeight();

            output.setText("X: " + x + " Y: " + y + " W: " + w + " H: " + h + "iW: " + imgWidth + "iH: " + imgHeight);
            // move right click point to center of the screen if possible
            if (imgWidth > w) {
                theMapPane.setHvalue((x - w / 2) / (imgWidth - w));
            }
            if (imgHeight > h) {
                theMapPane.setVvalue((y - h / 2) / (imgHeight - h));
            }
        }
    }

}
