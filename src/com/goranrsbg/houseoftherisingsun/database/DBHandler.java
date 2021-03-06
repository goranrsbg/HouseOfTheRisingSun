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

import com.goranrsbg.houseoftherisingsun.LocatorApp;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates and manages database connection, performs all database manipulation
 * calls.
 *
 * @author Goran
 */
public class DBHandler {

    public enum StatementType {
        SELECT_MAP_WITH_ID(0),
        SELECT_USER_ID_WITH_NAME(1),
        SELECT_USER_PASSWORD_WITH_ID(2),
        SELECT_STREETS_WITH_SETTLEMENT_ID(3),
        INSERT_LOCATION(4),
        SELECT_LOCATONS_WITH_SETTLEMENT_ID(5),
        SELECT_ALL_STREETS(6),
        UPDATE_LOCATON_XY(7),
        SELECT_STREET_NAME_LOCATION_NUMBER_LOCATION_PPSTEP_LOCATION_NOTE(8),
        INSERT_RECIPIENT(9),
        SELECT_RECIPIENTS_ON_LOCATION_ID(10),
        DELETE_RECIPIENT_WITH_ID(11),
        UPDATE_RECIPIENT(12),
        UPDATE_RECIPIENT_LOCATION(13),
        SEARCH_RECIPIENTS_WITH_NAME(14),
        SEARCH_RECIPIENTS_WITH_NAME_X2(15),
        SEARCH_RECIPIENTS(16),
        UPDATE_LOCATION_NUMBER_PPSTEP(17),
        INSERT_USER(18),
        DELETE_USER(19),
        DELETE_LOCATION(20),
        SELECT_RETIRE_RECIPIENTS(21);
        public final int I;

        private StatementType(int I) {
            this.I = I;
        }
    }
    /**
     * Default logger for this class.
     */
    private final Logger LOGGER;
    /**
     * Represents single and only instance of this class.
     */
    private static DBHandler instance;
    /**
     * Main connection to database instance.
     */
    private Connection connection;
    /**
     * All prepared statements.
     */
    private final ArrayList<PreparedStatement> statements;

    private DBHandler() {
        this.LOGGER = Logger.getLogger(LocatorApp.class.getName());
        this.statements = new ArrayList<>();
    }

    /**
     * Is not connection null.
     *
     * @return
     */
    public boolean isConnected() {
        return connection != null;
    }

    /**
     * Create then connect.
     */
    public static void ceateInstance() {
        if (instance == null) {
            instance = new DBHandler();
            instance.connect();
        }
    }

    /**
     * One and only instance.
     *
     * @return
     */
    public static DBHandler getInstance() {
        return instance;
    }

    /**
     * Creates connection to database if database exists or creates database by
     * parsing <code>default.sql</code> file and then creates connection.
     * this.prepareStatements() method if connection i created.
     */
    private void connect() {
        Properties props = new Properties();
        try {
            props.load(getClass().getResourceAsStream("database.properties"));
            System.setProperty("jdbc.drivers", props.getProperty("jdbc.drivers"));
            connection = DriverManager.getConnection(props.getProperty("jdbc.protocol") + props.getProperty("jdbc.name"), props.getProperty("jdbc.user"), props.getProperty("jdbc.password"));
            connection.setSchema(props.getProperty("jdbc.default.shema"));
        } catch (SQLException e) {
            if (e.getSQLState().equals("XJ004")) {
                try {
                    connection = DriverManager.getConnection(props.getProperty("jdbc.protocol") + props.getProperty("jdbc.name") + ";create=true");
                    connection.setSchema(props.getProperty("jdbc.default.shema"));
                    try (Statement st = connection.createStatement()) {
                        // enable some type of security calls, userneme: posta password: 11431 can only access database.
                        st.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.user.posta', '11431')");
                        st.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.connection.requireAuthentication', 'true')");
                        st.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.authentication.provider', 'BUILTIN')");
                        st.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.database.defaultConnectionMode', 'noAccess')");
                        st.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.database.fullAccessUsers', 'posta')");
                    }
                    initTables();
                } catch (SQLException ex) {
                    LOGGER.log(Level.INFO, "Failed to create connection.\nError: {0}", ex.getSQLState());
                }
            } else {
                LOGGER.log(Level.INFO, "Failed to create connection.\nError: {0}", e.getSQLState());
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Failed to load database properties.\nError: {0}", ex.getMessage());
        } finally {
            if (isConnected()) {
                prepareStatements();
            }
            props.clear();
        }
    }

    /**
     * Reads file <code>res/sql/db_init.sql</code>. Creates all tables and
     * inserts all default data. All <code>sql</code> command are separated with
     * ';' character and executed one by one.
     */
    private void initTables() {
        Properties props = new Properties();
        try {
            props.load(getClass().getResourceAsStream("database.properties"));
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "File {0} is not accessible.\nError: {1}", new String[]{"database.properties", ex.getMessage()});
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(props.getProperty("jdbc.default.sql")), "UTF-8"))) {
            String s;
            StringBuilder sb = new StringBuilder();
            // reads entire file and puts all lines if not empty in to string builder
            while ((s = reader.readLine()) != null) {
                if (!s.isEmpty()) {
                    sb.append(s);
                }
            }
            // all substrings ending with comma should be valid sql query.
            for (String st : sb.toString().split(";")) {
                if (!st.isEmpty()) {
                    try {
                        executeUpdate(st);
                    } catch (SQLException e) {
                        LOGGER.log(Level.INFO, "{0} is not executed.\nError: {1}", new String[]{st, e.getMessage()});
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.INFO, "File {0} is not found.\nError: {1}", new String[]{props.getProperty("jdbc.default.sql"), ex.getMessage()});
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "File {0} is not accessible.\nError: {1}", new String[]{props.getProperty("jdbc.default.sql"), ex.getMessage()});
        }
    }

    /**
     * Initializes all prepared statements.
     */
    private void prepareStatements() {
        try {
            // 0 SELECT_MAP_WITH_ID
            statements.add(connection.prepareStatement("SELECT SETTLEMENT_NAME, SETTLEMENT_FILE_NAME FROM SETTLEMENTS WHERE SETTLEMENT_ID = ?"));
            // 1 SELECT_USER_ID_WITH_NAME
            statements.add(connection.prepareStatement("SELECT USER_ID FROM USERS WHERE USER_NAME = ?"));
            // 2 SELECT_USER_PASSWORD_WITH_ID
            statements.add(connection.prepareStatement("SELECT USER_PASSWORD FROM USERS WHERE USER_ID = ?"));
            // 3 SELECT_STREETS_WITH_SETTLEMENT_ID
            statements.add(connection.prepareStatement("SELECT STREET_ID, STREET_NAME FROM STREETS WHERE SETTLEMENT_ID = ?"));
            // 4 INSERT_LOCATION
            statements.add(connection.prepareStatement("INSERT INTO LOCATIONS VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS));
            // 5 SELECT_LOCATONS_WITH_SETTLEMENT_ID
            statements.add(connection.prepareStatement("SELECT L.LOCATION_ID, L.LOCATION_POINT_X, L.LOCATION_POINT_Y, L.LOCATION_ADDRESS_NO, L.LOCATION_NOTE FROM LOCATIONS AS L "
                    + "JOIN STREETS AS S ON L.STREET_ID = S.STREET_ID WHERE S.SETTLEMENT_ID = ?"));
            // 6 SELECT_ALL_STREETS
            statements.add(connection.prepareStatement("SELECT ST.STREET_ID, ST.STREET_PAK, ST.STREET_NAME, SE.SETTLEMENT_INITIALS FROM STREETS AS ST "
                    + "JOIN SETTLEMENTS AS SE ON ST.SETTLEMENT_ID = SE.SETTLEMENT_ID ORDER BY SE.SETTLEMENT_INITIALS"));
            // 7 UPDATE_LOCATON_XY
            statements.add(connection.prepareStatement("UPDATE LOCATIONS SET LOCATION_POINT_X = ?, LOCATION_POINT_Y = ? WHERE LOCATION_ID = ?"));
            // 8 SELECT_STREET_NAME_LOCATION_NUMBER_LOCATION_PPSTEP_LOCATION_NOTE
            statements.add(connection.prepareStatement("SELECT S.STREET_NAME, L.LOCATION_ADDRESS_NO, L.LOCATION_POSTMAN_PATH_STEP, L.LOCATION_NOTE FROM LOCATIONS AS L JOIN STREETS AS S ON L.STREET_ID = S.STREET_ID "
                    + "WHERE L.LOCATION_ID = ?"));
            // 9 INSERT_RECIPIENT
            statements.add(connection.prepareStatement("INSERT INTO RECIPIENTS VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS));
            // 10 SELECT_RECIPIENTS_ON_LOCATION_ID
            statements.add(connection.prepareStatement("SELECT R.RECIPIENT_ID, R.RECIPIENT_LAST_NAME, R.RECIPIENT_FIRST_NAME, R.RECIPIENT_DETAILS, "
                    + "R.RECIPIENT_IS_RETIREE, R.RECIPIENT_ID_CARD_NUMBER, R.RECIPIENT_ID_CARD_POLICE_DEPARTMENT FROM RECIPIENTS AS R WHERE R.LOCATION_ID = ?"));
            // 11 DELETE_RECIPIENT_WITH_ID
            statements.add(connection.prepareStatement("DELETE FROM RECIPIENTS AS R WHERE R.RECIPIENT_ID = ?"));
            // 12 UPDATE_RECIPIENT
            statements.add(connection.prepareStatement("UPDATE RECIPIENTS AS R SET R.RECIPIENT_LAST_NAME = ?, R.RECIPIENT_FIRST_NAME = ?, R.RECIPIENT_DETAILS = ?, R.RECIPIENT_IS_RETIREE = ?, "
                    + "R.RECIPIENT_ID_CARD_NUMBER = ?, R.RECIPIENT_ID_CARD_POLICE_DEPARTMENT = ? WHERE R.RECIPIENT_ID = ?"));
            // 13 UPDATE_RECIPIENT_LOCATION
            statements.add(connection.prepareStatement("UPDATE RECIPIENTS AS R SET R.LOCATION_ID = ? WHERE R.RECIPIENT_ID = ?"));
            // 14 SEARCH_RECIPIENTS_WITH_NAME
            statements.add(connection.prepareStatement("SELECT R.RECIPIENT_ID, R.RECIPIENT_LAST_NAME, R.RECIPIENT_FIRST_NAME, R.RECIPIENT_DETAILS, "
                    + "R.RECIPIENT_IS_RETIREE, R.RECIPIENT_ID_CARD_NUMBER, R.RECIPIENT_ID_CARD_POLICE_DEPARTMENT, L.LOCATION_ID, S.STREET_NAME, L.LOCATION_ADDRESS_NO, L.LOCATION_POSTMAN_PATH_STEP FROM RECIPIENTS AS R "
                    + "JOIN LOCATIONS AS L ON R.LOCATION_ID = L.LOCATION_ID "
                    + "JOIN STREETS AS S ON L.STREET_ID = S.STREET_ID "
                    + "WHERE S.SETTLEMENT_ID = ? AND (R.RECIPIENT_LAST_NAME LIKE ? OR R.RECIPIENT_FIRST_NAME LIKE ?)"));
            // 15 SEARCH_RECIPIENTS_WITH_NAME_X2
            statements.add(connection.prepareStatement("SELECT R.RECIPIENT_ID, R.RECIPIENT_LAST_NAME, R.RECIPIENT_FIRST_NAME, R.RECIPIENT_DETAILS, "
                    + "R.RECIPIENT_IS_RETIREE, R.RECIPIENT_ID_CARD_NUMBER, R.RECIPIENT_ID_CARD_POLICE_DEPARTMENT, L.LOCATION_ID, S.STREET_NAME, L.LOCATION_ADDRESS_NO, L.LOCATION_POSTMAN_PATH_STEP FROM RECIPIENTS AS R "
                    + "JOIN LOCATIONS AS L ON R.LOCATION_ID = L.LOCATION_ID "
                    + "JOIN STREETS AS S ON L.STREET_ID = S.STREET_ID "
                    + "WHERE S.SETTLEMENT_ID = ? AND ((R.RECIPIENT_LAST_NAME LIKE ? AND R.RECIPIENT_FIRST_NAME LIKE ?) OR (R.RECIPIENT_FIRST_NAME LIKE ? AND R.RECIPIENT_LAST_NAME LIKE ?))"));
            // 16 SEARCH_RECIPIENTS
            statements.add(connection.prepareStatement("SELECT R.RECIPIENT_ID, R.RECIPIENT_LAST_NAME, R.RECIPIENT_FIRST_NAME, R.RECIPIENT_DETAILS, "
                    + "R.RECIPIENT_IS_RETIREE, R.RECIPIENT_ID_CARD_NUMBER, R.RECIPIENT_ID_CARD_POLICE_DEPARTMENT, L.LOCATION_ID, S.STREET_NAME, L.LOCATION_ADDRESS_NO, L.LOCATION_POSTMAN_PATH_STEP FROM RECIPIENTS AS R "
                    + "JOIN LOCATIONS AS L ON R.LOCATION_ID = L.LOCATION_ID "
                    + "JOIN STREETS AS S ON L.STREET_ID = S.STREET_ID "
                    + "WHERE S.SETTLEMENT_ID = ?"));
            // 17 UPDATE_LOCATION_NUMBER_PPSTEP
            statements.add(connection.prepareStatement("UPDATE LOCATIONS SET LOCATION_ADDRESS_NO = ?, LOCATION_POSTMAN_PATH_STEP = ?, LOCATION_NOTE = ? WHERE LOCATION_ID = ?"));
            // 18 INSERT_USER
            statements.add(connection.prepareStatement("INSERT INTO USERS VALUES (DEFAULT, ?, ?)"));
            // 19 DELETE_USER
            statements.add(connection.prepareStatement("DELETE FROM USERS AS U WHERE U.USER_NAME = ? AND U.USER_PASSWORD = ?"));
            // 20 DELETE_LOCATION
            statements.add(connection.prepareStatement("DELETE FROM LOCATIONS AS L WHERE L.LOCATION_ID = ?"));
            // 21 SELECT_RETIRE_RECIPIENTS
            statements.add(connection.prepareStatement("SELECT R.RECIPIENT_ID, R.RECIPIENT_LAST_NAME, R.RECIPIENT_FIRST_NAME, R.RECIPIENT_DETAILS, "
                    + "R.RECIPIENT_IS_RETIREE, R.RECIPIENT_ID_CARD_NUMBER, R.RECIPIENT_ID_CARD_POLICE_DEPARTMENT, L.LOCATION_ID, S.STREET_NAME, L.LOCATION_ADDRESS_NO, L.LOCATION_POSTMAN_PATH_STEP FROM RECIPIENTS AS R "
                    + "JOIN LOCATIONS AS L ON R.LOCATION_ID = L.LOCATION_ID "
                    + "JOIN STREETS AS S ON L.STREET_ID = S.STREET_ID "
                    + "WHERE S.SETTLEMENT_ID = ? AND R.RECIPIENT_IS_RETIREE = true"));
        } catch (SQLException e) {
            LOGGER.log(Level.INFO, "Failed to initiate prepared statements.\nError: {0}", e.getSQLState());
        }
    }

    /**
     * Execute insert, update, delete or any query that returns nothing.
     *
     * @param query Any to execute.
     * @return number of affected rows.
     * @throws java.sql.SQLException
     */
    private int executeUpdate(String query) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            return stmt.executeUpdate(query);
        }
    }

    /**
     * The database connection.
     *
     * @return
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * This is the main way to communicate with database.
     *
     * @param type Type or one of many prepared statements design for
     * communication with the database.
     * @return Prepared statement for executing query to the database.
     */
    public PreparedStatement getStatement(StatementType type) {
        return statements.get(type.I);
    }

    /**
     * Closes statement object and connection object if they are not null and
     * sets them to null. DriverManager.getConnection is called with
     * <code>URL="jdbc:derby:;shutdown=true"</code> to shut down the system.
     * Default error "XJ015" that is thrown is ignored because it indicates
     * normal shut down. Other errors are logged.
     *
     */
    public void closeConnection() {
        try {
            Properties props = new Properties();
            props.load(getClass().getResourceAsStream("database.properties"));
            props.clear();
            DriverManager.getConnection("jdbc:derby:;shutdown=true", props.getProperty("jdbc.username"), props.getProperty("jdbc.password"));
            while (!statements.isEmpty()) {
                PreparedStatement ps = statements.remove(0);
                ps.close();
            }
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException ex) {
            if (!(ex.getErrorCode() == 50000 && ex.getSQLState().equals("XJ015"))) {
                LOGGER.log(Level.INFO, "Derby did not shut down normally.\nError: {0}", ex.getErrorCode());
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Database properties are not found.\nError: {0}", ex.getMessage());
        }
    }

}
