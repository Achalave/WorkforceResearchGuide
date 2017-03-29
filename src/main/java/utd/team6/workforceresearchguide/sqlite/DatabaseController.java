package utd.team6.workforceresearchguide.sqlite;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

//@author Michael Haertling
public class DatabaseController {

    private static final String DB_BUILD_FILE_PATH = "DatabaseBuild.sql";
    
    String dbPath;
    Connection dbConnect;

    public DatabaseController(String dbPath) throws SQLException {
        this.dbPath = dbPath;
        //dbConnect = connect("jdbc:sqlite:" + dbPath);
    }

    private static Connection connect(String url) throws SQLException {
        Connection con = null;
        con = DriverManager.getConnection(url);
        return con;
    }

    public static void main(String[] args) throws SQLException {
        DatabaseController dbc = new DatabaseController("test.db");
        dbc.updateDatabase();
    }

    /**
     * This method ensures that the database has all the appropriate tables and
     * relations created. This should be called upon startup.
     */
    private void updateDatabase() {
        String query = "";
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(DB_BUILD_FILE_PATH).getFile());
        try (Scanner in = new Scanner(file)) {
            while (in.hasNextLine()) {
                query += in.nextLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
        executeInsert(query);
    }

    /**
     * Executes a SQL query.
     *
     * @param query
     * @return
     */
    public ResultSet executeQuery(String query) {
        ResultSet result = null;
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
            Statement stat = con.createStatement();
            result = stat.executeQuery(query);
            stat.closeOnCompletion();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * Executes an SQL insert statement.
     *
     * @param insert
     */
    public void executeInsert(String insert) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
            Statement stat = con.createStatement();
            stat.executeUpdate(insert);
            stat.closeOnCompletion();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
