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

/**
 *
 * @author Goran
 */
public class Address {

    private final int ID;
    private double x;
    private double y;
    private String streetName;
    private String br;
    private String note;

    public Address(int ID, double x, double y, String streetName, String br, String note) {
        this.ID = ID;
        this.x = x;
        this.y = y;
        this.streetName = streetName;
        this.br = br;
        this.note = note;
    }

    public int getID() {
        return ID;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getBr() {
        return br;
    }

    public void setBr(String br) {
        this.br = br;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "Address{" + "ID=" + ID + ", x=" + x + ", y=" + y + ", streetName=" + streetName + ", br=" + br + ", note=" + note + '}';
    }

}
