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

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Goran
 */
public class SettlementEntity {

    private static final String EXTENSION = ".bmp";

    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty initials;

    public SettlementEntity(int id, String name, String initials) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.initials = new SimpleStringProperty(initials);
    }

    public int getId() {
        return id.get();
    }

    public String getName() {
        return name.get();
    }

    public String getInitials() {
        return initials.get();
    }
    
    public String getMapName() {
        return name.get() + EXTENSION;
    }

    @Override
    public String toString() {
        return "Settlement{" + "ID=" + id.get() + ", NAME=" + name.get() + ", INITIALS=" + initials.get() + '}';
    }

}
