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
package com.goranrsbg.houseoftherisingsun.ui.showlocation;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Goran
 */
public class Recipient {
    
    private final IntegerProperty id;
    private final StringProperty lastName;
    private final StringProperty firstName;
    private final StringProperty details;
    private final BooleanProperty isRetire;
    private final LongProperty idCardNumber;
    private final StringProperty policeDepartment;

    public Recipient(int id, String lastName, String firstName, String details, boolean isRetire, long idCardNumber, String policeDepartment) {
        this.id = new SimpleIntegerProperty(id);
        this.lastName = new SimpleStringProperty(lastName);
        this.firstName = new SimpleStringProperty(firstName);
        this.details = new SimpleStringProperty(details);
        this.isRetire = new SimpleBooleanProperty(isRetire);
        this.idCardNumber = new SimpleLongProperty(idCardNumber);
        this.policeDepartment = new SimpleStringProperty(policeDepartment);
    }

    public int getId() {
        return id.get();
    }
    public void setId(int id) {
        this.id.set(id);
    }

    public String getFirstName() {
        return firstName.get();
    }
    public void setFirsName(String firstName) {
        this.firstName.set(firstName);
    }

    public String getLastName() {
        return lastName.get();
    }
    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }
    
    public String getDetails() {
        return details.get();
    }
    public void setDetails(String details) {
        this.details.set(details);
    }

    public boolean getIsRetire() {
        return isRetire.get();
    }
    public void setIsRetire(boolean isRetire) {
        this.isRetire.set(isRetire);
    }

    public long getIdCardNumber() {
        return idCardNumber.get();
    }
    public void setIdCardNumber(long idCardNumber) {
        this.idCardNumber.set(idCardNumber);
    }

    public String getPoliceDepartment() {
        return policeDepartment.get();
    }
    public void setPoliceDepartment(String policeDepartment) {
        this.policeDepartment.set(policeDepartment);
    }
    
}
