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

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author Goran
 */
public class StreetEntyty {

    private final SimpleIntegerProperty pak;
    private final SimpleStringProperty name;

    public StreetEntyty(int pak, String name) {
        this.pak = new SimpleIntegerProperty(pak);
        this.name = new SimpleStringProperty(name);
    }

    public int getPak() {
        return pak.get();
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }
    
    @Override
    public String toString() {
        return this.getPak() + " ~ " + this.getName();
    }

}
