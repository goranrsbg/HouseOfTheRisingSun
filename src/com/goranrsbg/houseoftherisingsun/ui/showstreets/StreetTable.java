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

/**
 *
 * @author Goran
 */
public class StreetTable {
    
    private final int id;
    private final int pak;
    private final String name;
    private final String settlementInitial;

    public StreetTable(int id, int pak, String name, String settlementInitial) {
        this.id = id;
        this.pak = pak;
        this.name = name;
        this.settlementInitial = settlementInitial;
    }

    public int getId() {
        return id;
    }

    public int getPak() {
        return pak;
    }

    public String getName() {
        return name;
    }

    public String getSettlementInitial() {
        return settlementInitial;
    }

}
