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
public class Address {

    private String streetName;
    private String addressNumber;
    private int postmanPathStep;

    public Address(String streetName, String addressNumber, int postmanPathStep) {
        this.streetName = streetName;
        this.addressNumber = addressNumber;
        this.postmanPathStep = postmanPathStep;
    }

    public String getStreetName() {
        return streetName;
    }

    public String getAddressNumber() {
        return addressNumber;
    }

    public int getPostmanPathStep() {
        return postmanPathStep;
    }

    public void setPostmanPathStep(int postmanPathStep) {
        this.postmanPathStep = postmanPathStep;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public void setAddressNumber(String addressNumber) {
        this.addressNumber = addressNumber;
    }

    @Override
    public String toString() {
        return streetName + " " + addressNumber;
    }

}
