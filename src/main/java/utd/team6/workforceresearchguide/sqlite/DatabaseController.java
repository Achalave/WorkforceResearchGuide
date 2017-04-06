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
        /*
            This does some basic testing of the functions provided by this class.
         */
        new File("test.db").delete();

        DatabaseController dbc = new DatabaseController("test.db");
        dbc.updateDatabase();
        try (Connection con = dbc.getConnection()) {
            Date testDate = new Date(System.currentTimeMillis());

            System.out.println("----->" + new SimpleDateFormat("yyy-MM-dd HH:mm:ss").format(testDate));
            dbc.addDocument(con, "test/home/file1.txt", new Date(System.currentTimeMillis()), "A");
            dbc.addDocument(con, "test/home/file2.txt", new Date(System.currentTimeMillis()), "B");
            dbc.addDocument(con, "test/home/file3.txt", new Date(System.currentTimeMillis()), "C");

            dbc.addTag(con, "tag1");
            dbc.addTag(con, "tag2");
            dbc.addTag(con, "tag3");

            dbc.tagDocument(con, "test/home/file1.txt", "tag1");
            dbc.tagDocument(con, "test/home/file1.txt", "tag2");
            dbc.tagDocument(con, "test/home/file1.txt", "tag3");
            dbc.tagDocument(con, "test/home/file2.txt", "tag2");
            dbc.tagDocument(con, "test/home/file2.txt", "tag3");

//            dbc.deleteDocument("test/home/file2.txt");
            dbc.deleteTag("tag2");

            dbc.printQuery(con, "SELECT * FROM FILES");
            dbc.printQuery(con, "SELECT * FROM FILE_TAGS");
//            dbc.printQuery(con, "SELECT * FROM TAG_ASSOCIATIONS");
//            dbc.printQuery(con, "SELECT tag1.TagText, tag2.TagText, ta.count "
//                    + "FROM TAG_ASSOCIATIONS ta, TAGS tag1, TAGS tag2 "
//                    + "WHERE tag1.TagID=ta.tag1 AND tag2.tagID=ta.tag2");
//            System.out.println(dbc.getTagAssociation(con, "tag3").get("tag2"));
        }

        new File("test.db").delete();
    }

    /**
     * Creates and returns a new connection to the DatabaseController's database
     * file.
     *
     * @return
     * @throws SQLException
     */
    private Connection getConnection() throws SQLException {
        Connection con = DriverManager.getConnection(dbURL);
        return con;
    }

    /**
     * This method ensures that the database has all the appropriate tables and
     * relations created. This should be called upon startup.
     */
    private void updateDatabase() {
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

        //Execute the setup query
        try (Connection con = getConnection()) {
            executeUpdate(con, query);
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
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
    private ResultSet executeQuery(Connection con, String query) throws SQLException {
        ResultSet result = null;
        Statement stat = con.createStatement();
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
    private void executeUpdate(Connection con, String insert) throws SQLException {
        Statement stat = con.createStatement();
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
     * @throws DatabaseTagDoesNotExistException
     * @throws DatabaseFileDoesNotExistException
     */
    public void tagDocument(String docPath, String tag) throws SQLException,
            DatabaseTagDoesNotExistException, DatabaseFileDoesNotExistException {
        try (Connection con = getConnection()) {
            this.tagDocument(con, docPath, tag);
        }
    }

    /**
     * Associates the given tag with the document denoted by the path. It is
     * assumed that the tag and the document already reside in the database. If
     * this is not so, an SQLException will be thrown.
     *
     * @param con An optional Connection object. If left null, a database
     * connection will be created internally.
     * @param docPath
     * @param tag
     * @throws SQLException
     * @throws
     * utd.team6.workforceresearchguide.sqlite.DatabaseTagDoesNotExistException
     * @throws
     * utd.team6.workforceresearchguide.sqlite.DatabaseFileDoesNotExistException
     */
    public void tagDocument(Connection con, String docPath, String tag) throws
            SQLException, DatabaseTagDoesNotExistException, DatabaseFileDoesNotExistException {
        try {
            executeUpdate(con, "INSERT INTO FILE_TAGS(FileID,TagID) VALUES("
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
    public int getDocumentID(String path) throws SQLException {
        try (Connection con = getConnection()) {
            return this.getDocumentID(con, path);
        }
    }

    /**
     * Gets the integer ID for the specified document.
     *
     * @param con
     * @param path
     * @return
     * @throws SQLException
     */
    private int getDocumentID(Connection con, String path) throws SQLException {
        ResultSet result = this.executeQuery(con, "SELECT FileID FROM FILES WHERE FilePath=\'" + path + "\'");
        if (result.next()) {
            return result.getInt(1);
        } else {
            return -1;
        }
    }

    /**
     * Adds a document to the database.
     *
     * @param documentPath
     * @param lastModDate
     * @param hash
     * @throws SQLException
     */
    public void addDocument(String documentPath, Date lastModDate, String hash) throws SQLException {
        try (Connection con = getConnection()) {
            this.addDocument(con, documentPath, lastModDate, hash);
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
    protected void addDocument(Connection con, String documentPath, Date lastModDate, String hash) throws SQLException {
        String documentName = new File(documentPath).getName();
        executeUpdate(con, "INSERT INTO FILES(FilePath, FileName, LastModDate, Hash) VALUES(\'"
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
     */
    public String[] getAllKnownFiles() throws SQLException {
        try (Connection con = getConnection()) {
            return this.getAllKnownFiles(con);
        }
    }

    /**
     * Gets a list of all the file paths currently in the database.
     *
     * @param con
     * @return
     * @throws SQLException
     */
    private String[] getAllKnownFiles(Connection con) throws SQLException {
        ArrayList<String> files = new ArrayList<>();
        ResultSet results = executeQuery(con, "SELECT FilePath FROM FILES");
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
     */
    public void updateDocumentModifiedDate(int docID, Date date) throws SQLException {
        try (Connection con = getConnection()) {
            this.updateDocumentModifiedDate(con, docID, date);
        }
    }

    /**
     * Updates the last modified date recorded for the specified document.
     *
     * @param con
     * @param docID
     * @param date
     * @throws SQLException
     */
    private void updateDocumentModifiedDate(Connection con, int docID, Date date) throws SQLException {
        executeUpdate(con, "UPDATE FILES SET LastModDate=\'" + dateToString(date) + "\' WHERE FileID=\'" + docID + "\'");
    }

    /**
     * Deletes a document and its associations.
     *
     * @param documentPath
     * @throws SQLException
     */
    public void deleteDocument(String documentPath) throws SQLException {
        try (Connection con = getConnection()) {
            this.deleteDocument(con, documentPath);
        }
    }

    /**
     * Deletes a document and its associations.
     *
     * @param con
     * @param documentPath
     */
    private void deleteDocument(Connection con, String documentPath) throws SQLException {
        executeUpdate(con, "DELETE FROM FILE_TAGS WHERE FileID = (SELECT FileID FROM FILES WHERE FilePath=\'" + documentPath + "\')");
        executeUpdate(con, "DELETE FROM FILES WHERE FilePath=\'" + documentPath + "\'");
    }

    /**
     * Collects and returns the integer ID of the specified tag.
     *
     * @param tag
     * @return
     * @throws SQLException
     */
    public int getTagID(String tag) throws SQLException {
        try (Connection con = getConnection()) {
            return this.getTagID(con, tag);
        }
    }

    /**
     * Collects and returns the integer ID of the specified tag.
     *
     * @param con An optional Connection object. If left null, a database
     * connection will be created internally.
     * @param tag A string representation of a tag currently in the system.
     * @return The integer ID of the specified tag or -1 if not found.
     * @throws SQLException
     */
    protected int getTagID(Connection con, String tag) throws SQLException {
        String query = "SELECT rowid FROM TAGS WHERE TagText=\'" + tag + "\'";
        try {
            Array value;
            try (ResultSet results = executeQuery(con, query)) {
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
     * @throws SQLException
     */
    public void addTag(String tag) throws SQLException {
        try (Connection con = getConnection()) {
            this.addTag(con, tag);
        }
    }

    /**
     * Adds a tag to the database.
     *
     * @param con
     * @param tag
     * @throws java.sql.SQLException
     */
    protected void addTag(Connection con, String tag) throws SQLException {
        String query = "INSERT INTO TAGS(TagText) VALUES(\'" + tag + "\')";
        this.executeUpdate(con, query);
    }

    /**
     * Deletes a tag and its associations from the database.
     *
     * @param tag
     * @throws SQLException
     */
    public void deleteTag(String tag) throws SQLException {
        try (Connection con = getConnection()) {
            this.deleteTag(con, tag);
        }
    }

    /**
     * Deletes a tag and its associations from the database.
     *
     * @param con
     * @param tag
     * @throws SQLException
     */
    protected void deleteTag(Connection con, String tag) throws SQLException {
        executeUpdate(con, "DELETE FROM FILE_TAGS WHERE TagID=(SELECT TagID FROM TAGS WHERE TagText=\'" + tag + "\')");
        executeUpdate(con, "DELETE FROM TAGS WHERE TagText=\'" + tag + "\'");
    }

    /**
     * Gets all tags associated with tagText and the number of times they have
     * been associated with one another.
     *
     * @param tagText
     * @return
     * @throws SQLException
     */
    public HashMap<String, Integer> getTagAssociation(String tagText) throws SQLException {
        try (Connection con = getConnection()) {
            return this.getTagAssociation(con, tagText);
        }
    }

    /**
     * Gets all tags associated with tagText and the number of times they have
     * been associated with one another.
     *
     * @param con
     * @param tagText
     * @return A hash map with keys corresponding to tags associated with
     * tagText and values representing the number of associations present.
     * @throws SQLException
     */
    protected HashMap<String, Integer> getTagAssociation(Connection con, String tagText) throws SQLException {
        String query = "SELECT tag1.TagText, tag2.TagText, ta.count "
                + "FROM TAG_ASSOCIATIONS ta, TAGS tag1, TAGS tag2 "
                + "WHERE tag1.TagText=\'" + tagText + "\' AND tag1.TagID=ta.tag1 AND tag2.tagID=ta.tag2";
        HashMap<String, Integer> associations;
        try (ResultSet results = executeQuery(con, query)) {
            associations = new HashMap<>();
            while (results.next()) {
                associations.put(results.getString(2), results.getInt(3));
            }
        }
        return associations;
    }

    public DocumentData getDocumentData(int fileID) throws SQLException, DatabaseFileDoesNotExistException {
        try (Connection con = getConnection()) {
            return this.getDocumentData(con, fileID);
        }
    }

    protected DocumentData getDocumentData(Connection con, int fileID) throws SQLException, DatabaseFileDoesNotExistException {
        String query = "SELECT(FilePath,FileName,LastModDate,Hits,Hash FROM FILES WHERE FileID=\'" + fileID + "\')";
        try (ResultSet results = executeQuery(con, query)) {
            if (!results.next()) {
                throw new DatabaseFileDoesNotExistException();
            }
            DocumentData docData = new DocumentData(results.getString(1), results.getString(2), results.getDate(3), results.getInt(4), results.getString(5));
            return docData;
        }
    }

    /**
     *
     * Retrieves all data in the database pertaining to the specified document.
     *
     * @param docPath
     * @return
     * @throws SQLException
     * @throws DatabaseFileDoesNotExistException
     */
    public DocumentData getDocumentData(String docPath) throws SQLException, DatabaseFileDoesNotExistException {
        try (Connection con = getConnection()) {
            return this.getDocumentData(con, docPath);
        }
    }

    /**
     * Retrieves all data in the database pertaining to the specified document.
     *
     * @param con
     * @param docPath
     * @return
     * @throws SQLException
     * @throws DatabaseFileDoesNotExistException
     */
    protected DocumentData getDocumentData(Connection con, String docPath) throws SQLException, DatabaseFileDoesNotExistException {
        String query = "SELECT(FilePath,FileName,LastModDate,Hits,Hash FROM FILES WHERE FilePath=\'" + docPath + "\')";
        try (ResultSet results = executeQuery(con, query)) {
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
     * @param con This must be a non-null, valid SQL connection.
     * @param query An SQL query that will produce a ResultSet.
     * @throws SQLException
     */
    protected void printQuery(Connection con, String query) throws SQLException {
        System.out.println(query);
        try (ResultSet results = this.executeQuery(con, query)) {
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
