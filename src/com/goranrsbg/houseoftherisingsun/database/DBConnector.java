/*
 * Copyright 2017 Goran.
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
package com.goranrsbg.houseoftherisingsun.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Goran
 */
public class DBConnector {
    
    private static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String URL = "jdbc:derby:db/data";
    private static final String USER = "posta";
    private static final String PASSWORD = "11431";
    
    private static final Logger LOGGER = Logger.getLogger("locator");
    
    private static DBConnector instance;
    
    private static Connection conn;
    
    private Statement statement;
    
    private DBConnector() {}
    
    public static void ceateInstance() {
        if(instance == null) {
            instance = new DBConnector();
            instance.init();
        }
    }
    
    public DBConnector getInstance() {
        return instance;
    }
    
    private void init() {
        new Thread(() -> {
            setLog();
            connect();
        }).start();
    }
    
    private void setLog() {
        String uri = System.getProperty("user.dir") + File.separator + "locator.log";
        try {    
            FileHandler fh = new FileHandler(uri);
            fh.setFormatter(new SimpleFormatter());
            DBConnector.LOGGER.addHandler(fh);
            DBConnector.LOGGER.setUseParentHandlers(false);
        } catch (IOException | SecurityException ex) {
            DBConnector.LOGGER.log(Level.SEVERE, null, ex);
        }
        DBConnector.LOGGER.info("Started.");
    }
    
    private void connect() {
        try {
            Class.forName(DBConnector.DRIVER).newInstance();
            conn = DriverManager.getConnection(DBConnector.URL, DBConnector.USER, DBConnector.PASSWORD);
            conn.setSchema("APP");
        } catch (SQLException e) {
            if(e.getSQLState().equals("XJ004")) {
                try {
                    conn = DriverManager.getConnection(DBConnector.URL + ";create=true", DBConnector.USER, DBConnector.PASSWORD);
                    conn.setSchema("APP");
                    
                    // Prepare statement for create tables, insert into tables
                    loadSetupDefaults();
                    
                } catch (SQLException ex) {
                    DBConnector.LOGGER.log(Level.SEVERE, null, ex);
                }
            } else {
                DBConnector.LOGGER.info(e.getSQLState());
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            DBConnector.LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean isConnected() {
        return conn != null;
    }
    
    public ResultSet executeQuery(String query) {
        ResultSet rs;
        try {
            statement = conn.createStatement();
            rs = statement.executeQuery(query);
        } catch (SQLException ex) {
            DBConnector.LOGGER.log(Level.SEVERE, null, ex);
            rs = null;
        }
        return rs;
    }
    
    public boolean execute(String query) {
        try {
            statement = conn.createStatement();
            System.out.println(query);
            statement.execute(query);
            return true;
        } catch (SQLException ex) {
            DBConnector.LOGGER.log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private void loadSetupDefaults() {
        String fname = "db_init.sql";
        String nl = System.getProperty("line.separator");
        String uri = System.getProperty("user.dir") + File.separator + "res" + File.separator + "sql" + File.separator + fname;
        File f = new File(uri);
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            String s;
            while((s = reader.readLine()) != null) {
                sb.append(s).append(nl);
            }
            for(String q : sb.toString().split(";")) {
                boolean res = execute(q);
                if(res) 
                    DBConnector.LOGGER.info("Done.");
                else 
                    DBConnector.LOGGER.info("Fail.");
            }
        } catch (FileNotFoundException ex) {
            DBConnector.LOGGER.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            DBConnector.LOGGER.log(Level.SEVERE, null, ex);
        }
    }
}
