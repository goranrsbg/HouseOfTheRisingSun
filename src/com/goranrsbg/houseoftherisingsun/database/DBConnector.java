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

import com.goranrsbg.houseoftherisingsun.ui.main.MainController;
import com.goranrsbg.houseoftherisingsun.utility.entity.LocationEntity;
import com.goranrsbg.houseoftherisingsun.utility.LocationsHandler;
import com.goranrsbg.houseoftherisingsun.utility.entity.SettlementEntity;
import com.goranrsbg.houseoftherisingsun.utility.entity.StreetEntyty;
import com.goranrsbg.houseoftherisingsun.utility.entity.StreetTableEntity;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Creates and manages database connection, performs all database manipulation
 * calls.
 *
 * @author Goran
 */
public class DBConnector {

    /**
     * Default logger for this class.
     */
    private static final Logger LOGGER = Logger.getLogger("locator");
    /**
     * Represent single and only instance of this object.
     */
    private static DBConnector instance;

    private LocationsHandler locationHandler;
    /**
     * Main connection to database instance.
     */
    private Connection connection;
    /**
     * Statement object for executing all queries.
     */
    private Statement statement;
    /**
     * Properties object that stores values from database.properties file.
     */
    private final Properties props;

    private PreparedStatement ps_insertLocation;
    private PreparedStatement ps_selectStreetsFromSettlement;
    private PreparedStatement ps_selectLocationsWithSettlementId;
    private PreparedStatement ps_selectLocationsWithPak;
    private PreparedStatement ps_updateLocation;
    private String SELECT_ALL_STREETS_QUERY;
    private String SELECT_ALL_SETTLEMENTS_QUERY;

    private DBConnector() {
        props = new Properties();

    }

    public void setLocationHandler(LocationsHandler locationHandler) {
        this.locationHandler = locationHandler;
    }

    public boolean isConnected() {
        return connection != null;
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

    /**
     * Initialize LOGGER, properties, connection, prepared statements.
     */
    private void init() {
        setLog();
        try {
            props.load(getClass().getResourceAsStream("database.properties"));
        } catch (IOException ex) {
            DBConnector.LOGGER.log(Level.SEVERE, null, ex);
        }
        connect();
    }

    /**
     * Creates default logger that stores logs into locator%u.log file.
     */
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
        DBConnector.LOGGER.info("Started.");
    }

    /**
     * Creates connection to database if database exists or creates database by
     * parsing <code>default.sql</code> file and then creates connection.
     * this.prepareStatements() method if connection i created.
     */
    private void connect() {
        String url = null;
        String username = null;
        String password = null;
        String defaultShema = null;
        try {
            String driver = props.getProperty("jdbc.drivers");
            url = props.getProperty("jdbc.url");
            username = props.getProperty("jdbc.username");
            password = props.getProperty("jdbc.password");
            defaultShema = props.getProperty("jdbc.default.shema");
            if (driver != null) {
                System.setProperty("jdbc.drivers", driver);
            }
            connection = DriverManager.getConnection(url, username, password);
            connection.setSchema(defaultShema);
            statement = connection.createStatement();
        } catch (SQLException e) {
            if (e.getSQLState().equals("XJ004")) {
                try {
                    connection = DriverManager.getConnection(url + ";create=true", username, password);
                    connection.setSchema(defaultShema);
                    statement = connection.createStatement();
                    loadSetupDefaults();
                } catch (SQLException ex) {
                    DBConnector.LOGGER.log(Level.SEVERE, null, ex);
                }
            } else {
                DBConnector.LOGGER.info(e.getSQLState());
            }
        } finally {
            if (isConnected()) {
                prepareStatements();
            }
        }
    }

    /**
     * Reads file <code>res/sql/db_init.sql</code>. Creates all tables and
     * inserts all default data. All <code>sql</code> command are separated with
     * ';' character and executed one by one.
     */
    private void loadSetupDefaults() {
        try {
            String fname = props.getProperty("jdbc.default.sql");
            String nl = System.getProperty("line.separator");
            File file = new File(fname);
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
                            DBConnector.LOGGER.log(Level.INFO, "{0}{1}{2}", new Object[]{(ind > 0) ? s.substring(0, s.indexOf("\n")) : s, (res == 0) ? " Created. " : " Updated. ", res});
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

    /**
     * Initializes all prepared statements and queries.
     */
    private void prepareStatements() {
        SELECT_ALL_STREETS_QUERY = "SELECT S.STREET_PAK, S.STREET_NAME, T.SETTLEMENT_INITIAL FROM STREETS AS S \n"
                + "JOIN SETTLEMENTS AS T ON S.SETTLEMENT_ID = T.SETTLEMENT_ID";
        SELECT_ALL_SETTLEMENTS_QUERY = "SELECT * FROM SETTLEMENTS";
        try {
            ps_insertLocation = connection.prepareStatement("INSERT INTO LOCATIONS(X, Y, STREET_PAK, LOCATION_NUMBER, NOTE)\n"
                    + "VALUES(?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps_updateLocation = connection.prepareStatement("UPDATE LOCATIONS\n"
                    + "SET X = ?, Y = ?, STREET_PAK = ?, LOCATION_NUMBER = ?, NOTE = ?\n"
                    + "WHERE LOCATION_ID = ?");
            ps_selectLocationsWithSettlementId = connection.prepareStatement(
                    "SELECT L.LOCATION_ID, L.X, L.Y, S.STREET_PAK, S.STREET_NAME, L.LOCATION_NUMBER, L.NOTE FROM LOCATIONS AS L\n"
                    + "JOIN STREETS AS S ON L.STREET_PAK = S.STREET_PAK\n"
                    + "WHERE S.SETTLEMENT_ID = ?");
            ps_selectLocationsWithPak = connection.prepareStatement(
                    "SELECT L.LOCATION_ID, L.X, L.Y, S.STREET_PAK, S.STREET_NAME , L.LOCATION_NUMBER, L.NOTE FROM LOCATIONS AS L\n"
                    + "JOIN STREETS AS S ON L.STREET_PAK = S.STREET_PAK\n"
                    + "WHERE L.STREET_PAK = ?");
            ps_selectStreetsFromSettlement = connection.prepareStatement("SELECT STREET_PAK, STREET_NAME FROM STREETS WHERE SETTLEMENT_ID = ?");
        } catch (SQLException ex) {
            DBConnector.LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Executes the given SQL statement, which returns a single ResultSet
     * object.
     *
     * @return ResultSet object.
     */
    public ArrayList<SettlementEntity> executeSellectAllSettlements() {

        ArrayList<SettlementEntity> list = new ArrayList<>();
        ResultSet rs;
        try {
            rs = statement.executeQuery(SELECT_ALL_SETTLEMENTS_QUERY);
            while (rs.next()) {
                list.add(new SettlementEntity(rs.getInt("SETTLEMENT_ID"), rs.getString("SETTLEMENT_NAME"), rs.getString("SETTLEMENT_INITIAL")));
            }
            list.trimToSize();
            rs.close();
        } catch (SQLException ex) {
            DBConnector.LOGGER.log(Level.SEVERE, null, ex);
        }
        return list;
    }

    /**
     * Execute insert, update, delete or any query that returns nothing.
     *
     * @param query Any to execute.
     * @return number of affected rows.
     */
    private int executeUpdate(String query) {
        int updated = 0;
        try {
            updated = statement.executeUpdate(query);
        } catch (SQLException ex) {
            DBConnector.LOGGER.log(Level.SEVERE, null, ex);
        }
        return updated;
    }

    /**
     * Inserts new address data into location table. X and Y defines point on
     * the map. Should represent real object on the map.
     *
     * @param location The address to be added to the LocationEntity table
     */
    public void executeInsertUpdateLocation(LocationEntity location) {
        final boolean isInsert = location.isLocationIdEmpty();
        PreparedStatement ps = isInsert ? ps_insertLocation : ps_updateLocation;
        String message;
        boolean type = false;
        try {
            ps.setDouble(1, location.getX());
            ps.setDouble(2, location.getY());
            ps.setInt(3, location.getStreet().getPak());
            ps.setString(4, location.getNumber());
            if (location.getNote() == null) {
                ps.setNull(5, Types.VARCHAR);
            } else {
                ps.setString(5, location.getNote());
            }
            if (!isInsert) {
                ps.setInt(6, location.getLocationId());
            }
            ps.executeUpdate();
            if (isInsert) {
                int generatedId = -1;
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    generatedId = generatedKeys.getInt(1);
                }
                if (locationHandler.isOnFlagOn()) {
                    location.setLocationId(generatedId);
                    locationHandler.addLocation(location);
                }
                message = "Lokacija je uspešno dodata. #" + generatedId;
            } else {
                message = "Lokacija uspešno ažurirana.\n" + location.toAddressString();
            }
            ps.clearParameters();
        } catch (SQLException ex) {
            int ec = ex.getErrorCode();
            if (ec == 30000) {
                message = "Dodavalje podatka nije uspelo.\nLokacija već postoji.";
            } else {
                DBConnector.LOGGER.log(Level.SEVERE, null, ex);
                message = "Dodavalje podatka nije uspelo.\nGreška: " + ec;
            }
            type = true;
        }
        sendMessage(message, type);
    }

    /**
     * Selects rows from streets table where
     * <code>streets.settlement_id = settlementId</code>.
     *
     * @param settlementId Value for compare. Must exists in settlements table.
     *
     * @return ResultSet object.
     */
    public List<StreetEntyty> executeSelectStreetsById(int settlementId) {
        ArrayList<StreetEntyty> list = new ArrayList<>();
        try {
            ps_selectStreetsFromSettlement.setInt(1, settlementId);
            ps_selectStreetsFromSettlement.executeQuery();
            ResultSet rs = ps_selectStreetsFromSettlement.getResultSet();
            ps_selectStreetsFromSettlement.clearParameters();
            if (rs != null) {
                while (rs.next()) {
                    list.add(new StreetEntyty(rs.getInt("STREET_PAK"), rs.getString("STREET_NAME")));
                }
                rs.close();
            }
        } catch (SQLException ex) {
            DBConnector.LOGGER.log(Level.SEVERE, null, ex);
            sendMessage("Selektovanje ulica nije uspelo.\nGreška: " + ex.getErrorCode(), true);
        }
        list.trimToSize();
        return list;
    }

    /**
     * Selects all rows from streets table with settlement initial instead of
     * settlement id.
     *
     * @return List of StreetTableEntity objects.
     */
    public List<StreetTableEntity> executeSelectAllStreets() {
        ArrayList<StreetTableEntity> list = new ArrayList<>(100);
        try {
            statement.executeQuery(SELECT_ALL_STREETS_QUERY);
            ResultSet rs = statement.getResultSet();
            if (rs != null) {
                while (rs.next()) {
                    list.add(new StreetTableEntity(rs.getInt("STREET_PAK"), rs.getString("STREET_NAME"), rs.getString("SETTLEMENT_INITIAL")));
                }
                rs.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnector.class.getName()).log(Level.SEVERE, null, ex);
            sendMessage("Selektovanje svih ulica nije uspelo.\nGreška: " + ex.getErrorCode(), true);
        }
        list.trimToSize();
        return list;
    }

    /**
     * Selects rows from locations table where row id or pak values equals
     * value. <code>locations.id = value</code>.
     * <code>locations.pak = value</code>.
     *
     * @param value Value of settlement id or street pak.
     * @param byId If true then value mast be from settlement.id, if false then
     * value must be from street.pak
     * @return ArrayLisat of selected addresses.
     */
    public List<LocationEntity> executeSelectLocations(int value, boolean byId) {
        ResultSet rs;
        ArrayList<LocationEntity> list = new ArrayList<>(200);
        PreparedStatement ps = (byId) ? ps_selectLocationsWithSettlementId : ps_selectLocationsWithPak;
        try {
            ps.setInt(1, value);
            rs = ps.executeQuery();
            ps.clearParameters();
            if (rs != null) {
                while (rs.next()) {
                    list.add(new LocationEntity(rs.getInt("LOCATION_ID"), rs.getDouble("X"), rs.getDouble("Y"),
                            new StreetEntyty(rs.getInt("STREET_PAK"), rs.getString("STREET_NAME")),
                            rs.getString("LOCATION_NUMBER"), rs.getString("NOTE")).updateLayout());
                }
                rs.close();
                list.trimToSize();
            }
        } catch (SQLException ex) {
            DBConnector.LOGGER.log(Level.SEVERE, null, ex);
            sendMessage("Selektovanje ulica nije uspelo.\nGreška: " + ex.getErrorCode(), true);
        }
        return list;
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
            if (statement != null) {
                statement.close();
                statement = null;
            }
            if (connection != null) {
                connection.close();
                connection = null;
            }
            String url = "jdbc:derby:;shutdown=true";
            String username = props.getProperty("jdbc.username");
            String password = props.getProperty("jdbc.password");
            DriverManager.getConnection(url, username, password);
        } catch (SQLException ex) {
            if (!ex.getSQLState().equals("XJ015")) {
                DBConnector.LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Sends message to main controller and shows it as pop up notification.
     *
     * @param message Massage to be send.
     * @param type Notification type true for error type or false for
     * information type
     */
    private void sendMessage(String message, boolean type) {
        MainController.notifyWithMsg(props.getProperty("title"), message, type);
    }

}
