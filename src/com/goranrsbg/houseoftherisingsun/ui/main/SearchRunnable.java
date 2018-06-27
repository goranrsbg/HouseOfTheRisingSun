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
 * Runnable class used for query database for recipient. Database query is execute
 * in intervals. Interval can be interrupted and call search early.
 * @author Goran
 */
public class SearchRunnable implements Runnable {
    /**
     * Default delay between searches.
     */
    private final long DELAY_IN_MILLISECONDS = 2011;
    /**
     * Starting searching value;
     */
    private final String DEFAULT_SEARCH_VALUE = "Seach is about to begin.";
    /**
     * Value that is searched last time.
     */
    private String valueSearched;
    /**
     * Next value to search for.
     */
    private String valueToSearchFor;
    /**
    * Collection for storing last database query results.
    */
    private final ObservableList<SearchRecipient> showRecipientsData;
    /**
    * Thread that is running instance of this class.
    */
    private Thread currentThread;
    /**
     * Main controller instance.
     */
    private final MainController mc;
    /**
     * Lock for concurrent access to this class.
     */
    private final ReentrantReadWriteLock rwl;
    /**
     * Read lock of the lock. Do not have purpose yet.
     */
    private final Lock readLock;
    /**
     * Lock method for write.
     */
    private final Lock writeLock;
    /**
     * One and only instance of this class.
     */
    private final SearchRunnable lock;
    /**
     * Prepared statement pointer that changes value based of the search needed.
     */
    private PreparedStatement ps;
    /**
     * Select all recipients the settlement.
     */
    private final PreparedStatement ps_selectAll;
    /**
     * Select recipients whose first name of last name starts with given value.
     */
    private final PreparedStatement ps_selectName;
    /**
     * Select recipients whose first name and last name or last name and first name
     * starts with given value.
     */
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
    /**
     * Setter for the next search value.
     * @param valueToSearchFor Next value to search for.
     */
    public void setValueToSearchFor(String valueToSearchFor) {
        writeLock.lock();
        try {
            this.valueToSearchFor = valueToSearchFor;
        } finally {
            writeLock.unlock();
        }
    }
    /**
     * Start this class in a separate thread.
     */
    public void start() {
        if (currentThread == null) {
            currentThread = new Thread(this);
            currentThread.setDaemon(true);
            currentThread.start();
        }
    }
    /**
     * Interrupt this thread if running.
     */
    public void stop() {
        if (currentThread != null) {
            currentThread.interrupt();
        }
    }
    /**
     * Notify thread if it is waiting.
     */
    public void resume() {
        synchronized(lock) {
            lock.notify();
        }    
    }
    /**
     * Search database for recipients if value to search for is different then
     * previous one. Search can be executed with three different prepared 
     * statements. First one is search all recipients if value to search for
     * is empty. Second is search for first name or second name if value is 
     * single word (without space). Third is chosen if value to search for 
     * contains more than one word, value is split into two words by the first 
     * space in the value, then search is performed comparing first name and 
     * second name with those values.
     * After database query thread is waiting for the given wait time.
     */
    @Override
    public void run() {
        while (!currentThread.isInterrupted()) {
            if (!valueToSearchFor.equals(valueSearched)) {
                try {
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
     * Changes name to start with uppercase letter. 
     * <code>goran -> Goran 
     * kolari -> Kolari</code>
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
