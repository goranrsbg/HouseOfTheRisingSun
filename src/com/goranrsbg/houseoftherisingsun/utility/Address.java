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

import de.jensd.fx.glyphs.GlyphIcon;
import java.util.Objects;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

/**
 *
 * @author Goran
 */
public final class Address extends Label {

    private final SimpleIntegerProperty addressId;
    private final SimpleDoubleProperty x;
    private final SimpleDoubleProperty y;
    private final SimpleStringProperty streetName;
    private final SimpleStringProperty br;
    private final SimpleStringProperty note;

    public Address(final int addressId, double x, double y, String streetName, String br, String note, GlyphIcon icon) {
        super(null, icon);
        this.addressId = new SimpleIntegerProperty(addressId);
        this.x = new SimpleDoubleProperty(x);
        this.y = new SimpleDoubleProperty(y);
        this.streetName = new SimpleStringProperty(streetName);
        this.br = new SimpleStringProperty(br);
        this.note = new SimpleStringProperty(note);
    }

    public int getAddressId() {
        return addressId.get();
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

    public String getStreetName() {
        return streetName.get();
    }

    public void setStreetName(String streetName) {
        this.streetName.set(streetName);
    }

    public String getBr() {
        return br.get();
    }

    public void setBr(String br) {
        this.br.set(br);
    }

    public String getNote() {
        return note.get();
    }

    public void setNote(String note) {
        this.note.set(note);
    }

    public Address updateLayout() {
        this.setLayoutX(getX());
        this.setLayoutY(getY());
        if (getTooltip() == null) {
            Tooltip tt = new Tooltip();
            tt.setStyle(DEFAULT_TOOLTIP_STYLE);
            this.setTooltip(tt);
        }
        getTooltip().setText("Adresa:\n" + getStreetName() + " " + getBr() + ((note.get() == null) ? "" : ("\nNapomena:\n" + note.get())));
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

    public void update(Address other) {
        boolean updated = false;
        if (this.getX() != other.getX()) {
            this.setX(other.getX());
            updated = true;
        }
        if (this.getY() != other.getY()) {
            this.setY(other.getY());
            updated = true;
        }
        if (!this.getStreetName().equals(other.getStreetName())) {
            this.setStreetName(other.getStreetName());
            updated = true;
        }
        if (!this.getBr().equals(other.getBr())) {
            this.setBr(other.getBr());
            updated = true;
        }
        if (!this.getNote().equals(other.getNote())) {
            this.setNote(other.getNote());
            updated = true;
        }
        if (updated) {
            updateLayout();
        }
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
        final Address other = (Address) obj;
        return Objects.equals(this.addressId, other.addressId);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.addressId.get());
        hash = 19 * hash + Objects.hashCode(this.x.get());
        hash = 19 * hash + Objects.hashCode(this.y.get());
        hash = 19 * hash + Objects.hashCode(this.streetName.get());
        hash = 19 * hash + Objects.hashCode(this.br.get());
        hash = 19 * hash + Objects.hashCode(this.note.get());
        return hash;
    }

 
    @Override
    public String toString() {
        return "Address{" + "Id=" + addressId.get() + ", x=" + x.get() + ", y="
                + y.get() + ", streetName=" + streetName.get() + ", br="
                + br.get() + ", note=" + note.get() + '}';
    }

    private static final String DEFAULT_TOOLTIP_STYLE = "-fx-font: normal normal 15px 'Oxygen'; -fx-base: #AE3522; -fx-text-fill: orange;";

}
