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
public class Settlement {

    public static final String EXTENSION = ".bmp";

    private final int ID;
    private final String NAME;
    private final String INITIALS;

    public Settlement(int ID, String NAME, String INITIALS) {
        this.ID = ID;
        this.NAME = NAME;
        this.INITIALS = INITIALS;
    }

    public int getID() {
        return ID;
    }

    public String getNAME() {
        return NAME;
    }

    public String getINITIALS() {
        return INITIALS;
    }
    
    public String getMapName() {
        return NAME + Settlement.EXTENSION;
    }

    @Override
    public String toString() {
        return "Settlement{" + "ID=" + ID + ", NAME=" + NAME + ", INITIALS=" + INITIALS + '}';
    }

}
