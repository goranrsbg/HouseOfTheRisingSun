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

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author Goran
 */
public final class LocationEntity {

    private final SimpleIntegerProperty locationId;
    private final SimpleDoubleProperty x;
    private final SimpleDoubleProperty y;
    private final SimpleIntegerProperty streetPak;
    private final SimpleStringProperty number;
    private final SimpleStringProperty note;

    public LocationEntity(double x, double y, int streetPak, String number, String note) {
        this(EMPTY_ADDRESS_ID, x, y, streetPak, number, note);
    }

    public LocationEntity(int locationId, double x, double y, int streetPak, String number, String note) {
        this.locationId = new SimpleIntegerProperty(locationId);
        this.x = new SimpleDoubleProperty(x);
        this.y = new SimpleDoubleProperty(y);
        this.streetPak = new SimpleIntegerProperty(streetPak);
        this.number = new SimpleStringProperty(number);
        this.note = new SimpleStringProperty(note);
    }

    public int getLocationId() {
        return locationId.get();
    }

    public void setLocationId(int locationId) {
        this.locationId.set(locationId);
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

    public int getStreetPak() {
        return streetPak.get();
    }

    public void setStreetPak(int streetPak) {
        this.streetPak.set(streetPak);
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

    public String toAddressString() {
        return getStreetPak() + " br. " + getNumber() + ".";
    }
    
    public String toTooltipString() {
        return " br. " + getNumber() + ((note.get() == null) ? "" : ("\nNapomena:\n" + note.get()));
    }

    @Override
    public String toString() {
        return "Address{" + "Id=" + locationId.get() + ", x=" + x.get() + ", y="
                + y.get() + ", streetPak=" + getStreetPak() + ", br="
                + number.get() + ", note=" + note.get() + '}';
    }

    public static final int EMPTY_ADDRESS_ID = -1;

}
