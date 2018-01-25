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

import com.goranrsbg.houseoftherisingsun.database.DBConnector;
import com.goranrsbg.houseoftherisingsun.ui.main.MainController;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Goran
 */
public class AddressHandler implements Iterable<Address> {
    
    public static final String TITLE = "AddtessHandler";
    
    private static AddressHandler instance;
    private DBConnector db;
    private MainController mc;
    
    private final List<Address> data;
    
    private AddressHandler() {
        data = new ArrayList<>();
    }
    
    public static AddressHandler getInstance() {
        if (instance == null) {
            instance = new AddressHandler();
            instance.db = DBConnector.getInstance();
            instance.mc = MainController.getInstance();
        }
        return instance;
    }
    
    public AddressHandler fetchDataByID(int settlementID) {
        ResultSet rs = db.execugeSelectLocationsByID(settlementID);
        readResultSet(rs);
        return this;
    }
    
    public AddressHandler fetchDataByPAK(int streetPAK) {
        ResultSet rs = db.executeSelectLocatonsByPak(streetPAK);
        readResultSet(rs);
        return this;
    }
    
    private void readResultSet(ResultSet rs) {
        try {
            data.clear();
            Address adr;
            while (rs.next()) {
                int id = rs.getInt("ID");
                double x = rs.getDouble("X");
                double y = rs.getDouble("Y");
                String nameStreet = rs.getString("NAME");
                String houseNo = rs.getString("HOUSE_NUMBER");
                String note = rs.getString("NOTE");
                adr = new Address(id, x, y, nameStreet, houseNo, note);
                data.add(adr);
            }
            rs.close();
        } catch (SQLException ex) {
            mc.notifyWithMsg(TITLE, "Greška: " + ex.getErrorCode(), true);
        }
    }
    
    @Override
    public Iterator<Address> iterator() {
        return new Iterator<Address>() {
            
            private final Iterator<Address> iter = data.iterator();
            
            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }
            
            @Override
            public Address next() {
                return iter.next();
            }
            
            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported.");
            }
        };
    }
    
}
