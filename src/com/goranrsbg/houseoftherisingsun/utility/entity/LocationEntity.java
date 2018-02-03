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
package com.goranrsbg.houseoftherisingsun.utility.entity;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.util.Objects;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

/**
 *
 * @author Goran
 */
public final class LocationEntity extends Label {

    private final SimpleIntegerProperty locationId;
    private final SimpleDoubleProperty x;
    private final SimpleDoubleProperty y;
    private final SimpleObjectProperty<StreetEntyty> street;
    private final SimpleStringProperty number;
    private final SimpleStringProperty note;

    public LocationEntity(double x, double y, StreetEntyty street, String number, String note) {
        this(EMPTY_ADDRESS_ID, x, y, street, number, note);
    }

    public LocationEntity(int locationId, double x, double y, StreetEntyty street, String number, String note) {
        super(null, new FontAwesomeIconView().setStyleClass(DEFAULT_ICON_STYLE));
        this.locationId = new SimpleIntegerProperty(locationId);
        this.x = new SimpleDoubleProperty(x);
        this.y = new SimpleDoubleProperty(y);
        this.street = new SimpleObjectProperty<>(street);
        this.number = new SimpleStringProperty(number);
        this.note = new SimpleStringProperty(note);
    }

    public int getLocationId() {
        return locationId.get();
    }

    public void setLocationId(int locationId) {
        this.locationId.set(locationId);
    }

    public boolean isLocationIdEmpty() {
        return getLocationId() == EMPTY_ADDRESS_ID;
    }

    public double getX() {
        return x.get();
    }

    public void setX(double x) {
        this.x.set(x);
    }

    public double getY() {
        return y.get();
    }

    public void setY(double y) {
        this.y.set(y);
    }

    public StreetEntyty getStreet() {
        return street.get();
    }

    public void setStreet(StreetEntyty street) {
        this.street.set(street);
    }

    public String getNumber() {
        return number.get();
    }

    public void setNumber(String br) {
        this.number.set(br);
    }

    public String getNote() {
        return note.get();
    }

    public void setNote(String note) {
        this.note.set(note);
    }

    public LocationEntity updateLayout() {
        this.setLayoutX(getX());
        this.setLayoutY(getY());
        if (getTooltip() == null) {
            Tooltip tt = new Tooltip();
            tt.setStyle(DEFAULT_TOOLTIP_STYLE);
            this.setTooltip(tt);
        }
        getTooltip().setText("Adresa:\n" + getStreet().getName() + " br. " + getNumber() + ((note.get() == null) ? "" : ("\nNapomena:\n" + note.get())));
        return this;
    }

    public void addTooltipRecipients(String[] r) {
        StringBuilder sb = new StringBuilder();
        Tooltip tt = getTooltip();
        sb.append("Primaoci:").append("\n");
        for (String s : r) {
            sb.append(s).append("\n");
        }
        sb.append(tt.getText());
        tt.setText(sb.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!Objects.equals(getClass(), obj.getClass())) {
            return false;
        }
        final LocationEntity other = (LocationEntity) obj;
        return Objects.equals(this.locationId, other.locationId);
    }
    
    public String toAddressString() {
        return getStreet().getName() + " br. " + getNumber() + ".";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.locationId.get());
        hash = 19 * hash + Objects.hashCode(this.x.get());
        hash = 19 * hash + Objects.hashCode(this.y.get());
        hash = 19 * hash + Objects.hashCode(this.street.get().getPak());
        hash = 19 * hash + Objects.hashCode(this.number.get());
        hash = 19 * hash + Objects.hashCode(this.note.get());
        return hash;
    }

    @Override
    public String toString() {
        return "Address{" + "Id=" + locationId.get() + ", x=" + x.get() + ", y="
                + y.get() + ", streetName=" + street.get().getName() + ", br="
                + number.get() + ", note=" + note.get() + '}';
    }

    private static final int EMPTY_ADDRESS_ID = -1;
    private static final double ICON_WIDTH_HALF = 11.5714282989502;
    private static final double ICON_HEIGHT_HALF = 14.52099609375;
    private static final String DEFAULT_ICON_STYLE = "location-icon";
    private static final String DEFAULT_TOOLTIP_STYLE = "-fx-font: normal normal 15px 'Oxygen'; -fx-base: #AE3522; -fx-text-fill: orange;";

}
