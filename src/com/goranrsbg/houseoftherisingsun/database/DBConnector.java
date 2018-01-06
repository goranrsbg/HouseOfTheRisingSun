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
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Goran
 */
public class DBConnector {

    private static final Logger LOGGER = Logger.getLogger("locator");

    private static DBConnector instance;

    private static Connection conn;

    private Statement statement;

    private DBConnector() {
    }

    public static void ceateInstance() {
        if (instance == null) {
            instance = new DBConnector();
            instance.init();
        }
    }

    public static DBConnector getInstance() {
        return instance;
    }

    private void init() {
        new Thread(() -> {
            setLog();
            connect();
        }).start();
    }

    private void setLog() {
        String uri = System.getProperty("user.dir") + File.separator + "locator%u.log";
        try {
            FileHandler fh = new FileHandler(uri, 50000, 1, true);
            fh.setFormatter(new SimpleFormatter());
            DBConnector.LOGGER.addHandler(fh);
            DBConnector.LOGGER.setUseParentHandlers(false);
        } catch (IOException | SecurityException ex) {
            DBConnector.LOGGER.log(Level.SEVERE, null, ex);
        }
        info("Started.");
    }

    private void connect() {
        Properties props = new Properties();
        String url = null;
        String username = null;
        String password = null;
        try {
            props.load(getClass().getResourceAsStream("database.properties"));
            String driver = props.getProperty("jdbc.drivers");
            url = props.getProperty("jdbc.url");
            username = props.getProperty("jdbc.username");
            password = props.getProperty("jdbc.password");
            if (driver != null) {
                System.setProperty("jdbc.drivers", driver);
            }
            conn = DriverManager.getConnection(url, username, password);
            conn.setSchema("APP");
        } catch (SQLException e) {
            if (e.getSQLState().equals("XJ004")) {
                try {
                    conn = DriverManager.getConnection(url + ";create=true", username, password);
                    conn.setSchema("APP");
                    loadSetupDefaults();
                } catch (SQLException ex) {
                    DBConnector.LOGGER.log(Level.SEVERE, null, ex);
                }
            } else {
                DBConnector.LOGGER.info(e.getSQLState());
            }
        } catch (IOException ex) {
            DBConnector.LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public boolean isConnected() {
        return conn != null;
    }

    public ResultSet executeQuery(String query) {
        ResultSet rs = null;
        try {
            statement = conn.createStatement();
            rs = statement.executeQuery(query);
        } catch (SQLException ex) {
            DBConnector.LOGGER.log(Level.SEVERE, null, ex);
        }
        return rs;
    }

    public int executeUpdate(String query) {
        int updated = 0;
        try {
            statement = conn.createStatement();
            updated = statement.executeUpdate(query);
        } catch (SQLException ex) {
            DBConnector.LOGGER.log(Level.SEVERE, null, ex);
        }
        return updated;
    }

    private void loadSetupDefaults() {
        try {
            Properties props = new Properties();
            props.load(getClass().getResourceAsStream("database.properties"));
            String fname = props.getProperty("jdbc.default.sql");
            String nl = System.getProperty("line.separator");
            File file = new File(Paths.get("", "res", "sql", fname).toUri());
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String s;
            int pom;
            int res;
            while ((s = reader.readLine()) != null) {
                if (!s.equals("")) {
                    sb.append(s);
                    pom = sb.lastIndexOf(";");
                    if (pom > 0) {
                        sb.setLength(pom);
                        s = sb.toString();
                        res = executeUpdate(s);
                        sb.setLength(0);
                        int ind = s.indexOf("\n");
                        if (res >= 0) {
                            info(((ind > 0) ? s.substring(0, s.indexOf("\n")) : s) + ((res == 0) ? " Created. " : " Updated. ") + res);
                        }
                    } else {
                        sb.append(nl);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            DBConnector.LOGGER.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            DBConnector.LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private void info(String msg) {
        DBConnector.LOGGER.info(msg);
    }

    public void closeConnection() {
        try {
            if (statement != null) {
                statement.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ex) {
            DBConnector.LOGGER.log(Level.SEVERE, null, ex);
        }
    }

}
