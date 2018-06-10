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
package com.goranrsbg.houseoftherisingsun.ui.showstreets;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Goran
 */
public class StreetTable {
    
    private final IntegerProperty id;
    private final IntegerProperty pak;
    private final StringProperty name;
    private final StringProperty settlementInitial;

    public StreetTable(int id, int pak, String name, String settlementInitial) {
        this.id = new SimpleIntegerProperty(id);
        this.pak = new SimpleIntegerProperty(pak);
        this.name = new SimpleStringProperty(name);
        this.settlementInitial = new SimpleStringProperty(settlementInitial);
    }

    public int getId() {
        return id.get();
    }

    public int getPak() {
        return pak.get();
    }

    public String getName() {
        return name.get();
    }

    public String getSettlementInitial() {
        return settlementInitial.get();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + this.id.get();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StreetTable other = (StreetTable) obj;
        return this.id == other.id;
    }
    
}
