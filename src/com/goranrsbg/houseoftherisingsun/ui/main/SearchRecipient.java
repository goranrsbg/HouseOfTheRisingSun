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
package com.goranrsbg.houseoftherisingsun.ui.main;

import com.goranrsbg.houseoftherisingsun.ui.showlocation.Recipient;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Goran
 */
public class SearchRecipient extends Recipient {
    
    private final IntegerProperty locationId;
    private final StringProperty locationAddress;

    public SearchRecipient(int id, String lastName, String firstName, String details, boolean isRetire, long idCardNumber, String policeDepartment, int locationId, String locationAddress) {
        super(id, lastName, firstName, details, isRetire, idCardNumber, policeDepartment);
        this.locationId = new SimpleIntegerProperty(locationId);
        this.locationAddress = new SimpleStringProperty(locationAddress);
    }
    
    public int getLocationId() {
        return locationId.get();
    }

    public void setLocationId(int locationId) {
        this.locationId.set(locationId);
    }

    public String getLocationAddress() {
        return locationAddress.get();
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress.set(locationAddress);
    }
    
}
