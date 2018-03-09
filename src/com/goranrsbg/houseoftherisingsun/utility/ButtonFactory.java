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

import com.jfoenix.controls.JFXButton;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.control.Tooltip;

/**
 *
 * @author Goran
 */
public class ButtonFactory {

    private JFXButton button;
    private Tooltip tooltip;
    private FontAwesomeIconView icon;

    public ButtonFactory() {
    }
    public JFXButton getButton() {
        JFXButton button1 = button;
        free();
        return button1;
    }

    private void free() {
        button = null;
        tooltip = null;
        icon = null;
    }

    public ButtonFactory createNewJFXTextButton(String text, String tooltipText) {
        createNewJFXButton(text).addNewTooltip(tooltipText);
        button.getStyleClass().add(DEFAULT_TEXT_BUTTON_CSS_CLASS);
        return this;
    }

    public ButtonFactory createNewJFXGlyphButton(int buttonCssSubclass, String glyphIconCssClass, String tooltipText) {
        createNewJFXButton(null).addNewGraphic(glyphIconCssClass).addNewTooltip(tooltipText);
        button.getStyleClass().add(DEFAULT_BUTTON_CSS_SUBCLASS + buttonCssSubclass);
        return this;
    }

    private ButtonFactory createNewJFXButton(String text) {
        button = new JFXButton(text);
        button.getStyleClass().add(DEFAULT_BUTTON_CSS_CLASS);
        return this;
    }

    public ButtonFactory addNewTooltip(String text) {
        tooltip = new Tooltip(text);
        tooltip.setStyle(DEFAULT_TOOLTIP_STYLE);
        if (button != null) {
            button.setTooltip(tooltip);
        }
        return this;
    }

    public ButtonFactory addNewGraphic(String glyphIconCssClass) {
        icon = new FontAwesomeIconView();
        icon.setStyleClass(glyphIconCssClass);
        if (button != null) {
            button.setGraphic(icon);
        }
        return this;
    }

    private static final String DEFAULT_TEXT_BUTTON_CSS_CLASS = "settlement";
    private static final String DEFAULT_BUTTON_CSS_SUBCLASS = "animated-option-sub-button";
    private static final String DEFAULT_BUTTON_CSS_CLASS = "animated-option-button";
    private static final String DEFAULT_TOOLTIP_STYLE = "-fx-font: normal bold 15px 'Oxygen'; -fx-base: #AE3522; -fx-text-fill: orange;";
    public static final int BUTTON_ZERO_SUBCLASS   = 0;
    public static final int BUTTON_FIRST_SUBCLASS  = 1;
    public static final int BUTTON_SECOND_SUBCLASS = 2;
    public static final int BUTTON_THIRD_SUBCLASS  = 3;
    public static final int BUTTON_FOURTH_SUBCLASS = 4;

}
