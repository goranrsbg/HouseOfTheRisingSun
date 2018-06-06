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

/**
 *
 * @author Goran
 */
public class LocationUpdate {
    
    private final String number;
    private final int ppStep;

    public LocationUpdate(String number, int ppStep) {
        this.number = number;
        this.ppStep = ppStep;
    }

    public String getNumber() {
        return number;
    }

    public int getPpStep() {
        return ppStep;
    }

    @Override
    public String toString() {
        return "LocationUpdate{" + "number=" + number + ", ppStep=" + ppStep + '}';
    }
    
}
