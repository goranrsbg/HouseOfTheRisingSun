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

import com.goranrsbg.houseoftherisingsun.database.DBHandler;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javafx.collections.ObservableList;

/**
 *
 * @author Goran
 */
public class SearchRunnable implements Runnable {

    private final long DELAY_IN_MILLISECONDS = 2011;
    private final String DEFAULT_SEARCH_VALUE = "Seach is about to begin.";
    private String valueSearched;
    private String valueToSearchFor;

    private final ObservableList<SearchRecipient> showRecipientsData;
    private Thread currentThread;
    
    private final MainController mc;
    private final ReentrantReadWriteLock rwl;
    private final Lock readLock;
    private final Lock writeLock;
    private final SearchRunnable lock;
    
    private PreparedStatement ps;
    private final PreparedStatement ps_selectAll;
    private final PreparedStatement ps_selectName;
    private final PreparedStatement ps_selectNameX2;
    
    /**
     *
     * @param showRecipientsData Data to be displayed in the recipients table.
     */
    public SearchRunnable(ObservableList<SearchRecipient> showRecipientsData) {
        this.showRecipientsData = showRecipientsData;
        this.valueSearched = DEFAULT_SEARCH_VALUE;
        this.valueToSearchFor = DEFAULT_SEARCH_VALUE;
        this.mc = MainController.getInstance();
        this.rwl = new ReentrantReadWriteLock();
        readLock = rwl.readLock();
        writeLock = rwl.writeLock();
        ps_selectAll = DBHandler.getInstance().getStatement(DBHandler.StatementType.SEARCH_RECIPIENTS);
        ps_selectName = DBHandler.getInstance().getStatement(DBHandler.StatementType.SEARCH_RECIPIENTS_WITH_NAME);
        ps_selectNameX2 = DBHandler.getInstance().getStatement(DBHandler.StatementType.SEARCH_RECIPIENTS_WITH_NAME_X2);
        lock = this;
    }

    public void setValueToSearchFor(String valueToSearchFor) {
        writeLock.lock();
        try {
            this.valueToSearchFor = valueToSearchFor;
        } finally {
            writeLock.unlock();
        }
    }

    public void start() {
        if (currentThread == null) {
            currentThread = new Thread(this);
            currentThread.setDaemon(true);
            currentThread.start();
        }
    }

    public void stop() {
        if (currentThread != null) {
            currentThread.interrupt();
        }
    }

    public void resume() {
        synchronized(lock) {
            lock.notify();
        }    
    }

    @Override
    public void run() {

        // Lock or not lock readlock and writelock how to do it...
        while (!currentThread.isInterrupted()) {
            if (!valueToSearchFor.equals(valueSearched)) {
                try {
                    //search valuetoSearchFor
                    System.out.println("Search for: " + valueToSearchFor);
                    showRecipientsData.clear();
                    String value = nameFirstLetterToUpperCase(valueToSearchFor.trim());
                    if(value.isEmpty()) {
                        ps_selectAll.setInt(1, mc.getLoadedMapId());
                        ps = ps_selectAll;
                    } else if(value.indexOf(' ') > 0) {
                        int ind = value.indexOf(' ');
                        String value_one = value.substring(0, ind);
                        String value_two = nameFirstLetterToUpperCase(value.substring(ind + 1, value.length()).trim());
                        ps_selectNameX2.setInt(1, mc.getLoadedMapId());
                        ps_selectNameX2.setString(2, value_one + '%');
                        ps_selectNameX2.setString(3, value_two + '%');
                        ps_selectNameX2.setString(4, value_one + '%');
                        ps_selectNameX2.setString(5, value_two + '%');
                        ps = ps_selectNameX2;
                    } else {
                        ps_selectName.setInt(1, mc.getLoadedMapId());
                        ps_selectName.setString(2, value + '%');
                        ps_selectName.setString(3, value + '%');
                        ps = ps_selectName;
                    }
                    try(ResultSet rs = ps.executeQuery()) {
                        while(rs.next()) {
                            int rid = rs.getInt("RECIPIENT_ID");
                            String lname = rs.getString("RECIPIENT_LAST_NAME");
                            String fname = rs.getString("RECIPIENT_FIRST_NAME");
                            String details = rs.getString("RECIPIENT_DETAILS");
                            boolean isRetiree = rs.getBoolean("RECIPIENT_IS_RETIREE");
                            long cnumber = rs.getLong("RECIPIENT_ID_CARD_NUMBER");
                            String pdepartment = rs.getString("RECIPIENT_ID_CARD_POLICE_DEPARTMENT");
                            int lid = rs.getInt("LOCATION_ID");
                            String sname = rs.getString("STREET_NAME");
                            String lno = rs.getString("LOCATION_ADDRESS_NO");
                            int pps = rs.getInt("LOCATION_POSTMAN_PATH_STEP");
                            showRecipientsData.add(new SearchRecipient(rid, lname, fname, details, isRetiree, cnumber, pdepartment, lid, sname + " " + lno, pps));
                        }
                    }
                    valueSearched = valueToSearchFor;
                } catch (SQLException ex) {
                    mc.showMessage("Pretraživanje primalaca.", "Greška prilikom pokušaja pretrage.\nError: " + ex.getMessage(), MainController.MessageType.ERROR);
                }
            } else {
                System.out.println("Do not search.");
            }
            try {
                synchronized (lock) {
                    lock.wait(DELAY_IN_MILLISECONDS);
                }
            } catch (InterruptedException ex) {
                currentThread.interrupt();
            }
        }
    }
    
    /**
     * Changes name to start with uppercase letter. goran -> Goran kolari ->
     * Kolari
     *
     * @param name name to be changed.
     * @return
     */
    private String nameFirstLetterToUpperCase(String name) {
        if (!name.isEmpty()) {
            char charAt0 = name.charAt(0);
            if (Character.isLowerCase(charAt0)) {
                name = Character.toUpperCase(charAt0) + name.substring(1, name.length());
            }
        }
        return name;
    }

}
