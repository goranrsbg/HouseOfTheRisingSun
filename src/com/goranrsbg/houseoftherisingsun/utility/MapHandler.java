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
package com.goranrsbg.houseoftherisingsun.utility;

import com.goranrsbg.houseoftherisingsun.LocatorApp;
import com.goranrsbg.houseoftherisingsun.ui.main.MainController;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author Goran
 */
public class MapHandler {

    private static MapHandler instance;

    private final ArrayList<Settlement> settlementsList;
    private final ImageView imageView;
    private int currentSettlementIndex;
    private Image theMapImage;

    public MapHandler(ImageView imageView, ArrayList<Settlement> settlements) {
        this.imageView = imageView;
        this.settlementsList = settlements;
        this.currentSettlementIndex = NONE_MAP_INDEX;
        instance = this;
    }

    public static MapHandler getInstance() {
        return instance;
    }

    public Iterator<Settlement> getSettlementsIterator() {
        return settlementsList.iterator();
    }

    public MapHandler loadMap(int settlementIndex) {
        if (settlementIndex != currentSettlementIndex) {
            if (settlementIndex >= 0 && settlementIndex < settlementsList.size()
                    && readImage(settlementsList.get(settlementIndex).getMapName())) {
                currentSettlementIndex = settlementIndex;
            } else {
                loadDefaultMap();
            }
        }
        return this;
    }

    public MapHandler loadDefaultMap() {
        if (readImage(DEFAULT_MAP_FILE_NAME)) {
            currentSettlementIndex = DEFAULT_MAP_INDEX;
        } else {
            imageView.setImage(null);
            currentSettlementIndex = NONE_MAP_INDEX;
        }
        return this;
    }

    private boolean readImage(final String fileName) {
        boolean result = true;
        try {
            theMapImage = new Image(new FileInputStream(PATH + fileName));
            LocatorApp.setSubTitle(fileName);
            imageView.setImage(theMapImage);
        } catch (FileNotFoundException ex) {
            result = false;
        }
        final boolean result1 = result;
        Platform.runLater(() -> {
            sendMessage(fileName, result1, !result1);
        });
        return result;
    }

    public boolean isMapLoaded() {
        return currentSettlementIndex >= 0;
    }

    public double getMapWidth() {
        return theMapImage.getWidth();
    }

    public double getMapHeight() {
        return theMapImage.getHeight();
    }

    public int getCurrentMapId() {
        return settlementsList.get(currentSettlementIndex).getId();
    }

    private void sendMessage(String title, boolean message1, boolean type) {
        MainController.notifyWithMsg(title, message1 ? MESSAGE1 : MESSAGE2, type);
    }

    private final String MESSAGE1 = "Karta je učitana.";
    private final String MESSAGE2 = "Karta nije dostupna.";
    private final String PATH = "res/maps/";
    private final String DEFAULT_MAP_FILE_NAME = "default.bmp";
    private final int NONE_MAP_INDEX = -2;
    public final int DEFAULT_MAP_INDEX = -1;

}
