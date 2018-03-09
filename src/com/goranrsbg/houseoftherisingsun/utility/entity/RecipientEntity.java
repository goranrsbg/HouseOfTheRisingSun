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
public class RecipientEntity {
    
    private final IntegerProperty recipientId;
    private final StringProperty lastName;
    private final StringProperty firstName;
    private final StringProperty details;
    private final IntegerProperty locationId;
    private final LongProperty idNumber;
    private final BooleanProperty retire;

    public RecipientEntity(int recipientId, String lastName, String firstName, String details, int locationId, long idNumber, boolean retire) {
        this.recipientId = new SimpleIntegerProperty(recipientId);
        this.lastName = new SimpleStringProperty(lastName);
        this.firstName = new SimpleStringProperty(firstName);
        this.details = new SimpleStringProperty(details);
        this.locationId = new SimpleIntegerProperty(locationId);
        this.idNumber = new SimpleLongProperty(idNumber);
        this.retire = new SimpleBooleanProperty(retire);
    }

    public int getRecipientId() {
        return recipientId.get();
    }

    public String getLastName() {
        return lastName.get();
    }
    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    public String getFirstName() {
        return firstName.get();
    }
    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }
    
    public String getDetails() {
        return details.get();
    }
    public void setDetails(String details) {
        this.details.set(details);
    }
    
    public int getLocationId() {
        return locationId.get();
    }
    public void setLocationId(int locationId) {
        this.locationId.set(locationId);
    }

    public long getIdNumber() {
        return idNumber.get();
    }
    public void setIdNumber(int idNumber) {
        this.idNumber.set(idNumber);
    }
    
    public boolean getRetire() {
        return retire.get();
    }
    public void setRetire(boolean retire) {
        this.retire.set(retire);
    }
    
}
