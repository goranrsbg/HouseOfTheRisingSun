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
import com.goranrsbg.houseoftherisingsun.utility.Street;
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
    /**
     * Pointer to controller of main scene.
     */
    private MainController mc;

    private AddressHandler ah;
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

    private DBConnector() {
        props = new Properties();
    }

    public void setMc(MainController mc) {
        this.mc = mc;
    }

    public void setAh(AddressHandler ah) {
        this.ah = ah;
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
     * @param query An SQL statement to be sent to the database, typically a
     * static SQL SELECT statement
     *
     * @return ResultSet object.
     */
    public ResultSet executeQuery(String query) {
        ResultSet rs = null;
        try {
            rs = statement.executeQuery(query);
        } catch (SQLException ex) {
            DBConnector.LOGGER.log(Level.SEVERE, null, ex);
        }
        return rs;
    }

    /**
     * Execute insert, update, delete or any query that returns nothing.
     *
     * @param query Any to execute.
     * @return number of affected rows.
     */
    public int executeUpdate(String query) {
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
                if(result > 0 && ah.isOnFlagOn()) {
                    ah.addLocation(new Address(result, x, y, s.getName(), houseNo,
                            note, new FontAwesomeIconView().setStyleClass("location-icon")).updateLayout());
                }
            }
            ps_insertLocation.clearParameters();
            mc.notifyWithMsg(props.getProperty("title"), "Podatak uspešno dodat.", false);
        } catch (SQLException ex) {
            int ec = ex.getErrorCode();
            if (ec == 30000) {
                mc.notifyWithMsg(props.getProperty("title"), "Dodavalje podatka nije uspelo.\nLokacija već postoji.", true);
            } else {
                DBConnector.LOGGER.log(Level.SEVERE, null, ex);
                mc.notifyWithMsg(props.getProperty("title"), "Dodavalje podatka nije uspelo.\nGreška: " + ec, true);
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
        ResultSet rs;
        ArrayList<Street> list = new ArrayList<>();
        try {
            ps_selectStreetsFromSettlement.setInt(1, settlementId);
            rs = ps_selectStreetsFromSettlement.executeQuery();
            while (rs.next()) {
                list.add(new Street(rs.getInt("PAK"), rs.getString("NAME")));
            }
            ps_selectStreetsFromSettlement.clearParameters();
            rs.close();
        } catch (SQLException ex) {
            DBConnector.LOGGER.log(Level.SEVERE, null, ex);
            mc.notifyWithMsg(props.getProperty("title"), "Selektovanje ulica nije uspelo.\nGreška: " + ex.getErrorCode(), true);
        }
        list.trimToSize();
        return list;
    }

    /**
     * Selects single row from locations table where
     * <code>locations.id = id</code>.
     *
     * @param id Value for compare. Represents primary key in locations table.
     * @return Single row.
     */
    public List<Address> execugeSelectLocationsByID(int id) {
        ResultSet rs;
        List<Address> list = null;
        try {
            ps_selectLocationsWithSettlementId.setInt(1, id);
            rs = ps_selectLocationsWithSettlementId.executeQuery();
            list = readAddresses(rs);
            ps_selectLocationsWithSettlementId.clearParameters();
        } catch (SQLException ex) {
            DBConnector.LOGGER.log(Level.SEVERE, null, ex);
            mc.notifyWithMsg(props.getProperty("title"), "Selektovanje ulica nije uspelo.\nGreška: " + ex.getErrorCode(), true);
        }
        return list;
    }

    /**
     * Collects rows from locations table where
     * <code>locations.pak = pak</code>.
     *
     * @param pak Value for compare, must exists in streets table.
     *
     * @return ResultSet object from locations table.
     */
    public List<Address> executeSelectLocatonsByPak(int pak) {
        ResultSet rs;
        List<Address> list = null;
        try {
            ps_selectLocationsWithPak.setInt(1, pak);
            rs = ps_selectLocationsWithPak.executeQuery();
            list = readAddresses(rs);
            ps_selectLocationsWithPak.clearParameters();
        } catch (SQLException e) {
            DBConnector.LOGGER.log(Level.SEVERE, null, e);
        }
        return list;
    }

    /**
     * Creates list of addresses from given Result Set.
     *
     * @param rs Result Set from prepared statements that query locations table.
     *
     * @return List of Address objects.
     *
     * @throws SQLException
     */
    private List<Address> readAddresses(ResultSet rs) throws SQLException {
        ArrayList<Address> list = new ArrayList<>(200);
        while (rs.next()) {
            list.add(new Address(rs.getInt("ID"), rs.getDouble("X"), rs.getDouble("Y"), rs.getString("NAME"),
                    rs.getString("HOUSE_NUMBER"), rs.getString("NOTE"), new FontAwesomeIconView().setStyleClass("location-icon")).updateLayout());
        }
        list.trimToSize();
        return list;
    }

    /**
     * Closes statement object and connection object if they are not null and
     * sets them to null. DriverManager.getConnection is called with
     * <code>URL="jdbc:derby:;shutdown=true"</code> to shut down the system.
     * Default error "XJ015" that is thrown is ignored because it indicates
     * normal shut down. Other errors are logged.
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

}
