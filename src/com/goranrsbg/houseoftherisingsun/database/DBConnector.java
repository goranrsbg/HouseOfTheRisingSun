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
import com.goranrsbg.houseoftherisingsun.utility.Address;
import com.goranrsbg.houseoftherisingsun.utility.AddressHandler;
import com.goranrsbg.houseoftherisingsun.utility.Settlement;
import com.goranrsbg.houseoftherisingsun.utility.Street;
import com.goranrsbg.houseoftherisingsun.utility.StreetInitial;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
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

    private AddressHandler addressHandler;
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
    private final String SELECT_ALL_STREETS_QUERY;

    private DBConnector() {
        props = new Properties();
        SELECT_ALL_STREETS_QUERY = "SELECT S.PAK, S.NAME, T.INITIAL FROM STREETS AS S \n"
                + "JOIN SETTLEMENTS AS T ON S.SETTLEMENT_ID = T.ID";
    }

    public void setAddressHandler(AddressHandler addressHandler) {
        this.addressHandler = addressHandler;
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
     * Initializes all prepared statements.
     */
    private void prepareStatements() {
        try {
            ps_insertLocation = connection.prepareStatement("INSERT INTO LOCATIONS(X,Y,HOUSE_NUMBER,STREET_PAK,NOTE) VALUES(?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps_selectStreetsFromSettlement = connection.prepareStatement("SELECT * FROM STREETS WHERE SETTLEMENT_ID = ?");
            ps_selectLocationsWithSettlementId = connection.prepareStatement(
                    "SELECT L.ID, L.X, L.Y, S.NAME, L.HOUSE_NUMBER, L.NOTE FROM LOCATIONS AS L\n"
                    + "JOIN STREETS AS S ON L.STREET_PAK = S.PAK\n"
                    + "WHERE S.SETTLEMENT_ID = ?");
            ps_selectLocationsWithPak = connection.prepareStatement(
                    "SELECT L.ID, L.X, L.Y, S.NAME ,L.HOUSE_NUMBER, L.NOTE FROM LOCATIONS AS L\n"
                    + "JOIN STREETS AS S ON L.STREET_PAK = S.PAK\n"
                    + "WHERE L.STREET_PAK = ?");
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
    public ArrayList<Settlement> executeSellectAllSettlements() {
        final String query = "SELECT * FROM SETTLEMENTS";
        ArrayList<Settlement> list = new ArrayList<>();
        ResultSet rs;
        try {
            rs = statement.executeQuery(query);
            while (rs.next()) {
                list.add(new Settlement(rs.getInt("ID"), rs.getString("NAME"), rs.getString("INITIAL")));
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
     * @param x Coordinate X on the map.
     * @param y Coordinate Y on the map.
     * @param houseNo House number.
     * @param s Location street.
     * @param note Can be null.
     */
    public void executeInsertLocation(double x, double y, String houseNo, final Street s, String note) {
        try {
            ps_insertLocation.setDouble(1, x);
            ps_insertLocation.setDouble(2, y);
            ps_insertLocation.setString(3, houseNo);
            ps_insertLocation.setInt(4, s.getPak());
            if (note.isEmpty()) {
                ps_insertLocation.setNull(5, Types.VARCHAR);
            } else {
                ps_insertLocation.setString(5, note);
            }
            ps_insertLocation.executeUpdate();
            ResultSet generatedKeys = ps_insertLocation.getGeneratedKeys();
            if (generatedKeys != null && generatedKeys.next()) {
                int result = generatedKeys.getInt(1);
                if (result > 0 && addressHandler.isOnFlagOn()) {
                    addressHandler.addLocation(new Address(result, x, y, s.getName(), houseNo,
                            note, new FontAwesomeIconView().setStyleClass("location-icon")).updateLayout());
                }
            }
            ps_insertLocation.clearParameters();
            sendMessage("Podatak uspešno dodat.", false);
        } catch (SQLException ex) {
            int ec = ex.getErrorCode();
            if (ec == 30000) {
                sendMessage("Dodavalje podatka nije uspelo.\nLokacija već postoji.", true);
            } else {
                DBConnector.LOGGER.log(Level.SEVERE, null, ex);
                sendMessage("Dodavalje podatka nije uspelo.\nGreška: " + ec, true);
            }
        }
    }

    /**
     * Selects rows from streets table where
     * <code>streets.settlement_id = settlementId</code>.
     *
     * @param settlementId Value for compare. Must exists in settlements table.
     *
     * @return ResultSet object.
     */
    public List<Street> executeSelectStreets(int settlementId) {
        ArrayList<Street> list = new ArrayList<>();
        try {
            ps_selectStreetsFromSettlement.setInt(1, settlementId);
            ps_selectStreetsFromSettlement.executeQuery();
            ResultSet rs = ps_selectStreetsFromSettlement.getResultSet();
            ps_selectStreetsFromSettlement.clearParameters();
            if (rs != null) {
                while (rs.next()) {
                    list.add(new Street(rs.getInt("PAK"), rs.getString("NAME")));
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
     * @return List of StreetInitial objects.
     */
    public List<StreetInitial> executeSelectAllStreets() {
        ArrayList<StreetInitial> list = new ArrayList<>(100);
        try {
            statement.executeQuery(SELECT_ALL_STREETS_QUERY);
            ResultSet rs = statement.getResultSet();
            if (rs != null) {
                while (rs.next()) {
                    list.add(new StreetInitial(rs.getInt("PAK"), rs.getString("NAME"), rs.getString("INITIAL")));
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
     * @param byId If true then value maust be from settlement.id, if false then
     * value must be from street.pak
     * @return ArrayLisat of selected addresses.
     */
    public List<Address> executeSelectLocations(int value, boolean byId) {
        ResultSet rs;
        ArrayList<Address> list = new ArrayList<>(200);
        try {
            if (byId) {
                ps_selectLocationsWithSettlementId.setInt(1, value);
                rs = ps_selectLocationsWithSettlementId.executeQuery();
                ps_selectLocationsWithSettlementId.clearParameters();
            } else {
                ps_selectLocationsWithPak.setInt(1, value);
                rs = ps_selectLocationsWithPak.executeQuery();
                ps_selectLocationsWithPak.clearParameters();
            }
            if (rs != null) {
                while (rs.next()) {
                    list.add(new Address(rs.getInt("ID"), rs.getDouble("X"), rs.getDouble("Y"), rs.getString("NAME"),
                            rs.getString("HOUSE_NUMBER"), rs.getString("NOTE"),
                            new FontAwesomeIconView().setStyleClass("location-icon")).updateLayout());
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
