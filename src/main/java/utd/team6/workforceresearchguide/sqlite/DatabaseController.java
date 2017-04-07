package utd.team6.workforceresearchguide.sqlite;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import utd.team6.workforceresearchguide.main.DocumentData;

//@author Michael Haertling
public class DatabaseController {

    private static final String DB_BUILD_FILE_PATH = "DatabaseBuild.sql";
    private static final String DB_URL_PREFIX = "jdbc:sqlite:";

    String dbPath;
    String dbURL;
    Connection dbConnect;

    public DatabaseController(String dbPath) throws SQLException {
        this.dbPath = dbPath;
        this.dbURL = DB_URL_PREFIX + dbPath;
    }

    public static void main(String[] args) throws SQLException, DatabaseTagDoesNotExistException, DatabaseFileDoesNotExistException {
    }

    /**
     * Starts a new SQL connection.
     *
     * @throws SQLException
     * @throws ConnectionAlreadyActiveException
     */
    public void startConnection() throws SQLException, ConnectionAlreadyActiveException {
        if (dbConnect != null) {
            throw new ConnectionAlreadyActiveException();
        }
        dbConnect = DriverManager.getConnection(dbURL);
        dbConnect.setAutoCommit(false);
    }

    /**
     * Closes the active SQL connection and commits all the changes to the
     * database. This function does nothing if there is no active connection.
     *
     * @throws SQLException
     */
    public void stopConnection() throws SQLException {
        if (dbConnect != null) {
            dbConnect.commit();
            dbConnect.close();
        }
    }

    public void rollbackConnection() throws SQLException {
        if (dbConnect != null) {
            dbConnect.rollback();
            dbConnect.close();
        }
    }

    /**
     * This method ensures that the database has all the appropriate tables and
     * relations created. This should be called upon startup.
     *
     * @throws java.sql.SQLException
     * @throws
     * utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
     */
    public void updateDatabaseSchema() throws SQLException, ConnectionNotStartedException {
        //Load the query file into a string
        String query = "";
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(DB_BUILD_FILE_PATH).getFile());
        try (Scanner in = new Scanner(file)) {
            while (in.hasNextLine()) {
                query += in.nextLine() + "\n";
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            //Execute the setup query
            this.startConnection();
        } catch (ConnectionAlreadyActiveException ex) {
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }

        executeUpdate(query);
        this.stopConnection();

    }

    /**
     * Executes a SQL query. This function should only be used on queries that
     * will return some data.
     *
     * @param con This parameter cannot be null.
     * @param query
     * @return A ResultSet object containing an iterable connection to the
     * result data.
     * @throws java.sql.SQLException
     */
    private ResultSet executeQuery(String query) throws SQLException, ConnectionNotStartedException {
        if (dbConnect == null) {
            throw new ConnectionNotStartedException();
        }
        ResultSet result = null;
        Statement stat = dbConnect.createStatement();
        result = stat.executeQuery(query);
        stat.closeOnCompletion();
        return result;
    }

    /**
     * Executes an SQL insert statement. This function should only be used on
     * queries that will not return any data.
     *
     * @param con This parameter cannot be null.
     * @param insert
     * @throws java.sql.SQLException
     */
    private void executeUpdate(String insert) throws SQLException, ConnectionNotStartedException {
        if (dbConnect == null) {
            throw new ConnectionNotStartedException();
        }
        Statement stat = dbConnect.createStatement();
        stat.executeUpdate(insert);
        stat.closeOnCompletion();
    }

    /**
     * Associates the given tag with the document denoted by the path. It is
     * assumed that the tag and the document already reside in the database. If
     * this is not so, an SQLException will be thrown.
     *
     * @param docPath
     * @param tag
     * @throws SQLException
     * @throws
     * utd.team6.workforceresearchguide.sqlite.DatabaseTagDoesNotExistException
     * @throws
     * utd.team6.workforceresearchguide.sqlite.DatabaseFileDoesNotExistException
     * @throws
     * utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
     */
    public void tagDocument(String docPath, String tag) throws
            SQLException, DatabaseTagDoesNotExistException, DatabaseFileDoesNotExistException, ConnectionNotStartedException {
        try {
            executeUpdate("INSERT INTO FILE_TAGS(FileID,TagID) VALUES("
                    + "(SELECT rowid FROM FILES WHERE FilePath=\'" + docPath + "\'),"
                    + "(SELECT rowid FROM TAGS WHERE TagText=\'" + tag + "\'))");

        } catch (SQLException ex) {
            //Checks the top level exception to see if it was caused by a tag or file missing
            if (ex.getErrorCode() == 19) {
                if (ex.toString().contains("NOT NULL constraint failed: FILE_TAGS.TagID")) {
                    throw new DatabaseTagDoesNotExistException();
                } else if (ex.toString().contains("NOT NULL constraint failed: FILE_TAGS.FileID")) {
                    throw new DatabaseFileDoesNotExistException();
                }
            } else {
                throw ex;
            }
        }
    }

    /**
     * Gets the integer ID for the specified document.
     *
     * @param path
     * @return
     * @throws SQLException
     */
    public int getDocumentID(String path) throws SQLException, ConnectionNotStartedException {
        ResultSet result = this.executeQuery("SELECT FileID FROM FILES WHERE FilePath=\'" + path + "\'");
        if (result.next()) {
            return result.getInt(1);
        } else {
            return -1;
        }
    }

    /**
     * Adds a document to the database.
     *
     * @param con
     * @param documentPath The full, unique path to the represented file.
     * @param lastModDate The last modified date associated with the file.
     * @param hash
     * @throws SQLException
     */
    public void addDocument(Connection con, String documentPath, Date lastModDate, String hash) throws SQLException, ConnectionNotStartedException {
        String documentName = new File(documentPath).getName();
        executeUpdate("INSERT INTO FILES(FilePath, FileName, LastModDate, Hash) VALUES(\'"
                + documentPath + "\',\'" + documentName + "\', \'"
                + dateToString(lastModDate)
                + "\', \'" + hash + "\')");
    }

    /**
     * Converts a Date object to a string that will be accepted by a SQLite
     * query.
     *
     * @param date
     * @return
     */
    private String dateToString(Date date) {
        return new SimpleDateFormat("yyy-MM-dd HH:mm:ss").format(date);
    }

    /**
     * Gets a list of all the file paths currently in the database.
     *
     * @return
     * @throws SQLException
     * @throws
     * utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
     */
    public String[] getAllKnownFiles() throws SQLException, ConnectionNotStartedException {
        ArrayList<String> files = new ArrayList<>();
        ResultSet results = executeQuery("SELECT FilePath FROM FILES");
        while (results.next()) {
            files.add(results.getString(0));
        }
        String[] tmp = new String[files.size()];
        return files.toArray(tmp);
    }

    /**
     * Updates the last modified date recorded for the specified document.
     *
     * @param docID
     * @param date
     * @throws SQLException
     * @throws
     * utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
     */
    public void updateDocumentModifiedDate(int docID, Date date) throws SQLException, ConnectionNotStartedException {
        executeUpdate("UPDATE FILES SET LastModDate=\'" + dateToString(date) + "\' WHERE FileID=\'" + docID + "\'");
    }

    /**
     * Deletes a document and its associations.
     *
     * @param documentPath
     * @throws java.sql.SQLException
     * @throws
     * utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
     */
    public void deleteDocument(String documentPath) throws SQLException, ConnectionNotStartedException {
        executeUpdate("DELETE FROM FILE_TAGS WHERE FileID = (SELECT FileID FROM FILES WHERE FilePath=\'" + documentPath + "\')");
        executeUpdate("DELETE FROM FILES WHERE FilePath=\'" + documentPath + "\'");
    }

    /**
     * Collects and returns the integer ID of the specified tag.
     *
     * @param tag A string representation of a tag currently in the system.
     * @return The integer ID of the specified tag or -1 if not found.
     * @throws SQLException
     * @throws
     * utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
     */
    public int getTagID(String tag) throws SQLException, ConnectionNotStartedException {
        String query = "SELECT rowid FROM TAGS WHERE TagText=\'" + tag + "\'";
        try {
            Array value;
            try (ResultSet results = executeQuery(query)) {
                value = results.getArray(1);
            }
            return ((int[]) value.getArray())[0];
        } catch (NullPointerException | ArrayIndexOutOfBoundsException ex) {
            return -1;
        }
    }

    /**
     * Adds a tag to the database.
     *
     * @param tag
     * @throws java.sql.SQLException
     * @throws
     * utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
     */
    public void addTag(String tag) throws SQLException, ConnectionNotStartedException {
        String query = "INSERT INTO TAGS(TagText) VALUES(\'" + tag + "\')";
        this.executeUpdate(query);
    }

    /**
     * Deletes a tag and its associations from the database.
     *
     * @param tag
     * @throws SQLException
     * @throws
     * utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
     */
    public void deleteTag(String tag) throws SQLException, ConnectionNotStartedException {
        executeUpdate("DELETE FROM FILE_TAGS WHERE TagID=(SELECT TagID FROM TAGS WHERE TagText=\'" + tag + "\')");
        executeUpdate("DELETE FROM TAGS WHERE TagText=\'" + tag + "\'");
    }

    /**
     * Gets all tags associated with tagText and the number of times they have
     * been associated with one another.
     *
     * @param tagText
     * @return A hash map with keys corresponding to tags associated with
     * tagText and values representing the number of associations present.
     * @throws SQLException
     * @throws
     * utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
     */
    public HashMap<String, Integer> getTagAssociation(String tagText) throws SQLException, ConnectionNotStartedException {
        String query = "SELECT tag1.TagText, tag2.TagText, ta.count "
                + "FROM TAG_ASSOCIATIONS ta, TAGS tag1, TAGS tag2 "
                + "WHERE tag1.TagText=\'" + tagText + "\' AND tag1.TagID=ta.tag1 AND tag2.tagID=ta.tag2";
        HashMap<String, Integer> associations;
        try (ResultSet results = executeQuery(query)) {
            associations = new HashMap<>();
            while (results.next()) {
                associations.put(results.getString(2), results.getInt(3));
            }
        }
        return associations;
    }

    /**
     *
     * @param fileID
     * @return
     * @throws SQLException
     * @throws DatabaseFileDoesNotExistException
     * @throws ConnectionNotStartedException
     */
    public DocumentData getDocumentData(int fileID) throws SQLException, DatabaseFileDoesNotExistException, ConnectionNotStartedException {
        String query = "SELECT(FilePath,FileName,LastModDate,Hits,Hash FROM FILES WHERE FileID=\'" + fileID + "\')";
        try (ResultSet results = executeQuery(query)) {
            if (!results.next()) {
                throw new DatabaseFileDoesNotExistException();
            }
            DocumentData docData = new DocumentData(results.getString(1), results.getString(2), results.getDate(3), results.getInt(4), results.getString(5));
            return docData;
        }
    }

    /**
     * Retrieves all data in the database pertaining to the specified document.
     *
     * @param docPath
     * @return
     * @throws SQLException
     * @throws DatabaseFileDoesNotExistException
     * @throws
     * utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
     */
    public DocumentData getDocumentData(String docPath) throws SQLException, DatabaseFileDoesNotExistException, ConnectionNotStartedException {
        String query = "SELECT(FilePath,FileName,LastModDate,Hits,Hash FROM FILES WHERE FilePath=\'" + docPath + "\')";
        try (ResultSet results = executeQuery(query)) {
            if (!results.next()) {
                throw new DatabaseFileDoesNotExistException();
            }
            DocumentData docData = new DocumentData(results.getString(1), results.getString(2), results.getDate(3), results.getInt(4), results.getString(5));
            return docData;
        }
    }

    /**
     * Prints the results of some query to the database. This function is for
     * debug use only.
     *
     * @param query An SQL query that will produce a ResultSet.
     * @throws SQLException
     * @throws
     * utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
     */
    public void printQuery(String query) throws SQLException, ConnectionNotStartedException {
        System.out.println(query);
        try (ResultSet results = this.executeQuery(query)) {
            ResultSetMetaData mdata = results.getMetaData();
            String tmp = "";
            for (int i = 1; i <= mdata.getColumnCount(); i++) {
                tmp += mdata.getColumnLabel(i) + ", ";
            }
            System.out.println("[" + tmp.substring(0, tmp.length() - 2) + "]");

            while (results.next()) {
                String line = "";
                for (int i = 1; i <= mdata.getColumnCount(); i++) {
                    line += results.getString(i) + ", ";
                }
                System.out.println("[" + line.substring(0, line.length() - 2) + "]");
            }
        }
    }
}
