package utd.team6.workforceresearchguide.sqlite;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import utd.team6.workforceresearchguide.main.DocumentData;

/**
 * This class is used to interface with the SQLite database used by this
 * application. All necessary modifications and extractions from the database
 * can be performed through methods within this class.
 *
 * @author Michael
 */
public class DatabaseController {

    private static final String DB_BUILD_FILE_PATH = "DatabaseBuild.sql";
    private static final String DB_URL_PREFIX = "jdbc:sqlite:";

    String dbPath;
    String dbURL;
    Connection dbConnect;

    /**
     * Instantiates an object of this class using the provided path to an SQLite
     * database file. If the file does not exist, it will be created.
     *
     * @param dbPath
     */
    public DatabaseController(String dbPath) {
        this.dbPath = dbPath;
        this.dbURL = DB_URL_PREFIX + dbPath;
    }

    /**
     * Starts a new SQL connection.
     *
     * @throws ConnectionAlreadyActiveException
     */
    public void startConnection() throws ConnectionAlreadyActiveException {
        try {
            if (dbConnect != null) {
                throw new ConnectionAlreadyActiveException();
            }
            dbConnect = DriverManager.getConnection(dbURL);
            dbConnect.setAutoCommit(false);
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Closes the active SQL connection and commits all the changes to the
     * database. This function does nothing if there is no active connection.
     *
     */
    public void stopConnection() {
        if (dbConnect != null) {
            try {
                dbConnect.commit();
                dbConnect.close();
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
            }
            dbConnect = null;
        }
    }

    /**
     * Closes the active SQL connection and disregards all changes that were
     * made on the connection.
     *
     */
    public void rollbackConnection() {
        if (dbConnect != null) {
            try {
                dbConnect.rollback();
                dbConnect.close();
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
            }
            dbConnect = null;
        }
    }

    /**
     * This method ensures that the database has all the appropriate tables and
     * relations created. This should be called upon startup.
     *
     * @throws
     * utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
     */
    public void updateDatabaseSchema() throws ConnectionNotStartedException {
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
            executeUpdate(query);
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

    public PreparedStatement getPreparedStatement(String query) throws ConnectionNotStartedException, SQLException {
        if (dbConnect == null) {
            throw new ConnectionNotStartedException();
        }
        return dbConnect.prepareStatement(query);
    }

    public void executePreparedUpdate(String query, String... params) throws ConnectionNotStartedException, SQLException {
        PreparedStatement stmt = this.getPreparedStatement(query);
        for (int i = 0; i < params.length; i++) {
            stmt.setString(i+1, params[i]);
        }
        stmt.executeUpdate();
    }

    public ResultSet executePreparedQuery(String query, String... params) throws ConnectionNotStartedException, SQLException {
        PreparedStatement stmt = this.getPreparedStatement(query);
        for (int i = 0; i < params.length; i++) {
            stmt.setString(i+1, params[i]);
        }
        return stmt.executeQuery();
    }

    /**
     *
     * @return The names of all groups saved in the system.
     * @throws ConnectionNotStartedException
     */
    public ArrayList<String> getGroups() throws ConnectionNotStartedException {
        ArrayList<String> groups = new ArrayList<>();
        try (ResultSet result = this.executeQuery("SELECT GroupName FROM GROUPS")) {
            while (result.next()) {
                groups.add(result.getString(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return groups;
    }

    /**
     *
     * @param group
     * @return A list of files contained within the specified group.
     * @throws ConnectionNotStartedException
     */
    public ArrayList<String> getGroupFiles(String group) throws ConnectionNotStartedException {
        ArrayList<String> files = new ArrayList<>();
        String query = "SELECT f.FileName FROM GROUPS g, FILES f WHERE g.GroupName= ? AND f.FileID=g.FileID";
        try (ResultSet result = this.executePreparedQuery(query, group)) {
            while (result.next()) {
                files.add(result.getString(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return files;
    }

    /**
     * Adds a new group to the database.
     *
     * @param group
     * @throws ConnectionNotStartedException
     */
    public void addGroup(String group) throws ConnectionNotStartedException {
        try {
            this.executePreparedUpdate("INSERT INTO GROUPS(GroupName) VALUES (?)", group);
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Deletes the specified group.
     *
     * @param group
     * @throws ConnectionNotStartedException
     */
    public void deleteGroup(String group) throws ConnectionNotStartedException {
        try {
            //Delete all the file associations
            this.executePreparedUpdate("DELETE FROM GROUP_FILES WHERE GroupID=(SELECT GroupID FROM GROUPS WHERE GroupName=?)", group);
            //Delete the group
            this.executePreparedUpdate("DELETE FROM GROUPS WHERE GroupName=?", group);
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Adds the specified file to the group.
     *
     * @param group
     * @param filePath
     * @throws
     * utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
     */
    public void addFileToGroup(String group, String filePath) throws ConnectionNotStartedException {
        try {
            this.executePreparedUpdate("INSERT INTO GROUP_FILES VALUES ("
                    + "(SELECT GroupID FROM GROUPS WHERE GroupName=?),"
                    + "(SELECT FileID FROM FILES WHERE FilePath=?))", group, filePath);
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Deletes the specified file from the specified group.
     *
     * @param group
     * @param filePath
     * @throws ConnectionNotStartedException
     */
    public void deleteFileFromGroup(String group, String filePath) throws ConnectionNotStartedException {
        try {
            this.executePreparedUpdate("DELETE FROM GROUP_FILES WHERE "
                    + "GroupID=(SELECT GroupID FROM GROUPS WHERE GroupName=?) AND "
                    + "FileID=(SELECT FileID FROM FILES WHERE FilePath=?)", group, filePath);
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param oldPath
     * @param newPath
     * @throws DatabaseFileDoesNotExistException
     * @throws ConnectionNotStartedException
     */
    public void updateDocumentPath(String oldPath, String newPath) throws DatabaseFileDoesNotExistException, ConnectionNotStartedException {
        try {
            executeUpdate("UPDATE FILES SET FilePath=\'" + newPath + "\' WHERE FilePath=\'" + oldPath + "\'");
            this.executePreparedUpdate("UPDATE FILES SET FilePath= ? WHERE FilePath= ? ", newPath, oldPath);
        } catch (SQLException ex) {
            //Checks the top level exception to see if it was caused by a tag or file missing
            if (ex.getErrorCode() == 19) {
                if (ex.toString().contains("NOT NULL constraint failed: FILE_TAGS.FileID")) {
                    throw new DatabaseFileDoesNotExistException();
                }
            } else {
                Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Updates the document's data to match that described in the DocumentData
     * parameter.
     *
     * @param path
     * @param data
     * @throws
     * utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
     * @throws
     * utd.team6.workforceresearchguide.sqlite.DatabaseFileDoesNotExistException
     */
    public void updateDocument(String path, DocumentData data) throws ConnectionNotStartedException, DatabaseFileDoesNotExistException {
        try {
            this.executePreparedUpdate("UPDATE FILES SET FilePath=?, Hash=?, "
                    + "FileName=?, LastModDate=?, DateAdded=?, Hits=? WHERE FilePath=?",
                    data.getPath(), data.getHash(), this.dateToString(data.getLastModDate()),
                    this.dateToString(data.getDateAdded()), data.getHits() + "", path);
        } catch (SQLException ex) {
            //Checks the top level exception to see if it was caused by a tag or file missing
            if (ex.getErrorCode() == 19) {
                if (ex.toString().contains("NOT NULL constraint failed: FILE_TAGS.FileID")) {
                    throw new DatabaseFileDoesNotExistException();
                }
            } else {
                Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Associates the given tag with the document denoted by the path. It is
     * assumed that the tag and the document already reside in the database. If
     * this is not so, an SQLException will be thrown.
     *
     * @param docPath
     * @param tag
     * @throws
     * utd.team6.workforceresearchguide.sqlite.DatabaseTagDoesNotExistException
     * @throws
     * utd.team6.workforceresearchguide.sqlite.DatabaseFileDoesNotExistException
     * @throws
     * utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
     */
    public void tagDocument(String docPath, String tag) throws
            DatabaseTagDoesNotExistException, DatabaseFileDoesNotExistException, ConnectionNotStartedException {
        try {
            this.executePreparedUpdate("INSERT INTO FILE_TAGS(FileID,TagID) VALUES("
                    + "(SELECT rowid FROM FILES WHERE FilePath=?),"
                    + "(SELECT rowid FROM TAGS WHERE TagText=?))", docPath, tag);
        } catch (SQLException ex) {
            //Checks the top level exception to see if it was caused by a tag or file missing
            if (ex.getErrorCode() == 19) {
                if (ex.toString().contains("NOT NULL constraint failed: FILE_TAGS.TagID")) {
                    throw new DatabaseTagDoesNotExistException();
                } else if (ex.toString().contains("NOT NULL constraint failed: FILE_TAGS.FileID")) {
                    throw new DatabaseFileDoesNotExistException();
                }
            } else {
                Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void removeDocumentTag(String docPath, String tag) throws ConnectionNotStartedException {
        try {
            this.executePreparedUpdate("DELETE FROM FILE_TAGS WHERE FileID="
                    + "(SELECT FileID FROM FILES WHERE FilePath=?) AND TagID="
                    + "(SELECT TagID FROM TAGS WHERE TagName=?)", docPath, tag);
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Gets the integer ID for the specified document.
     *
     * @param path
     * @return
     * @throws
     * utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
     */
    public int getDocumentID(String path) throws ConnectionNotStartedException {
        try {
            ResultSet result = this.executePreparedQuery("SELECT FileID FROM FILES WHERE FilePath=?", path);
            if (result.next()) {
                return result.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    /**
     * Adds a document to the database.
     *
     * @param documentPath The full, unique path to the represented file.
     * @param lastModDate The last modified date associated with the file.
     * @param hash
     * @param hits
     * @throws
     * utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
     */
    public void addDocument(String documentPath, Date lastModDate, String hash, int hits) throws ConnectionNotStartedException {
        String documentName = new File(documentPath).getName();
        String query = "INSERT INTO FILES(FilePath, FileName, LastModDate, Hash, Hits) VALUES( ?, ?, ?, ?, ? );";
        try {
            this.executePreparedUpdate(query, documentPath, documentName, dateToString(lastModDate), hash, hits + "");
        } catch (SQLException ex) {
            System.err.println("SQL Exception: " + query);
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Adds a document to the database.
     *
     * @param doc
     * @throws
     * utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
     */
    public void addDocument(DocumentData doc) throws ConnectionNotStartedException {
        this.addDocument(doc.getPath(), doc.getLastModDate(), doc.getHash(), doc.getHits());
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
     * Converts a String to a Date object.
     *
     * @param date
     * @return
     * @throws ParseException
     */
    private Date stringToDate(String date) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.parse(date);
    }

    /**
     * Gets a list of all the file paths currently in the database.
     *
     * @return
     * @throws
     * utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
     */
    public String[] getAllKnownFiles() throws ConnectionNotStartedException {
        ArrayList<String> files = new ArrayList<>();
        try {
            ResultSet results = executeQuery("SELECT FilePath FROM FILES");
            while (results.next()) {
                files.add(results.getString(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
        String[] tmp = new String[files.size()];
        return files.toArray(tmp);
    }

    /**
     * Updates the last modified date recorded for the specified document.
     *
     * @param docID
     * @param date
     * @throws
     * utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
     */
    public void updateDocumentModifiedDate(int docID, Date date) throws ConnectionNotStartedException {
        try {
            executeUpdate("UPDATE FILES SET LastModDate=\'" + dateToString(date) + "\' WHERE FileID=\'" + docID + "\'");
            this.executePreparedUpdate("UPDATE FILES SET LastModDate=? WHERE FileID=?", dateToString(date), docID + "");
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Deletes a document and its associations.
     *
     * @param documentPath
     * @throws
     * utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
     */
    public void deleteDocument(String documentPath) throws ConnectionNotStartedException {
        try {
            //Delete the file from any groups it is in
            this.executePreparedUpdate("DELETE FROM GROUP_FILES WHERE FileID = (SELECT FileID FROM FILES WHERE FilePath=?)", documentPath);
            //Delete the file from all tag associations
            this.executePreparedUpdate("DELETE FROM FILE_TAGS WHERE FileID = (SELECT FileID FROM FILES WHERE FilePath=?)", documentPath);
            //Delete the file itself
            this.executePreparedUpdate("DELETE FROM FILES WHERE FilePath=?", documentPath);
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Collects and returns the integer ID of the specified tag.
     *
     * @param tag A string representation of a tag currently in the system.
     * @return The integer ID of the specified tag or -1 if not found.
     * @throws
     * utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
     */
    public int getTagID(String tag) throws ConnectionNotStartedException {
        String query = "SELECT rowid FROM TAGS WHERE TagText=?";
        try {
            Array value;
            try (ResultSet results = executePreparedQuery(query, tag)) {
                value = results.getArray(1);
            }
            return ((int[]) value.getArray())[0];
        } catch (NullPointerException | ArrayIndexOutOfBoundsException ex) {
            return -1;
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    /**
     * Adds a tag to the database.
     *
     * @param tag
     * @throws
     * utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
     */
    public void addTag(String tag) throws ConnectionNotStartedException {
        try {
            String query = "INSERT INTO TAGS(TagText) VALUES(\'" + tag + "\')";
            this.executeUpdate(query);
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Deletes a tag and its associations from the database.
     *
     * @param tag
     * @throws
     * utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
     */
    public void deleteTag(String tag) throws ConnectionNotStartedException {
        try {
            executePreparedUpdate("DELETE FROM FILE_TAGS WHERE TagID=(SELECT TagID FROM TAGS WHERE TagText=?)", tag);
            executePreparedUpdate("DELETE FROM TAGS WHERE TagText=?", tag);
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Gets all tags associated with tagText and the number of times they have
     * been associated with one another.
     *
     * @param tagText
     * @param numTop Sets the number of records to be returned. If less than or
     * equal to 0, the entire list of records will be returned.
     * @return A hash map with keys corresponding to tags associated with
     * tagText and values representing the number of associations present.
     * @throws
     * utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
     */
    public HashMap<String, Integer> getTagAssociation(String tagText, int numTop) throws ConnectionNotStartedException {
        String query = "SELECT tag1.TagText, tag2.TagText, ta.count "
                + "FROM TAG_ASSOCIATIONS ta, TAGS tag1, TAGS tag2 "
                + "WHERE tag1.TagText=? AND tag1.TagID=ta.tag1 AND tag2.tagID=ta.tag2 "
                + "ORDER BY ta.count DESC ";
        if (numTop > 0) {
            query += "TOP " + numTop;
        }
        HashMap<String, Integer> associations = new HashMap<>();
        try (ResultSet results = executePreparedQuery(query, tagText)) {
            while (results.next()) {
                associations.put(results.getString(2), results.getInt(3));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return associations;
    }

    /**
     *
     * @param fileID
     * @return
     * @throws DatabaseFileDoesNotExistException
     * @throws ConnectionNotStartedException
     * @throws java.text.ParseException
     */
    public DocumentData getDocumentData(int fileID) throws DatabaseFileDoesNotExistException, ConnectionNotStartedException, ParseException {
        String query = "SELECT(FilePath,FileName,LastModDate,Hits,Hash FROM FILES WHERE FileID=?)";
        try (ResultSet results = executePreparedQuery(query, fileID + "")) {
            if (!results.next()) {
                throw new DatabaseFileDoesNotExistException();
            }
            DocumentData docData = new DocumentData(results.getString(1), results.getString(2), this.stringToDate(results.getString(3)), results.getInt(4), results.getString(5));
            return docData;
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
        throw new DatabaseFileDoesNotExistException();
    }

    /**
     * Retrieves all data in the database pertaining to the specified document.
     *
     * @param docPath
     * @return
     * @throws DatabaseFileDoesNotExistException
     * @throws
     * utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
     * @throws java.text.ParseException
     */
    public DocumentData getDocumentData(String docPath) throws DatabaseFileDoesNotExistException, ConnectionNotStartedException, ParseException {
        String query = "SELECT FilePath,FileName,LastModDate,Hits,Hash FROM FILES WHERE FilePath=?";
        try (ResultSet results = executePreparedQuery(query, docPath)) {
            if (!results.next()) {
                throw new DatabaseFileDoesNotExistException();
            }
            DocumentData docData = new DocumentData(results.getString(1), results.getString(2), this.stringToDate(results.getString(3)), results.getInt(4), results.getString(5));
            return docData;
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
        throw new DatabaseFileDoesNotExistException();
    }

    /**
     * Returns a list of tags associated with the specified document.
     *
     * @param docPath
     * @return
     * @throws ConnectionNotStartedException
     */
    public ArrayList<String> getDocumentTags(String docPath) throws ConnectionNotStartedException {
        ArrayList<String> tags = new ArrayList<>();
        try {
            ResultSet results = this.executePreparedQuery("SELECT ft.TagID FROM FILES f, FILE_TAGS, TAGS t tf WHERE f.FilePath=? AND f.FileID=ft.FileID AND t.TagID=ft.TagID", docPath);
            if (results.next()) {
                String tag = results.getString(0);
                tags.add(tag);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tags;
    }

    public ArrayList<String> getTags() throws ConnectionNotStartedException {
        ArrayList<String> tags = new ArrayList<>();
        try {
            ResultSet results = this.executePreparedQuery("SELECT TagName FROM TAGS");
            if (results.next()) {
                String tag = results.getString(0);
                tags.add(tag);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tags;
    }

    /**
     * Prints the results of some query to the database. This function is for
     * debug use only.
     *
     * @param query An SQL query that will produce a ResultSet.
     * @throws
     * utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
     */
    public void printQuery(String query) throws ConnectionNotStartedException {
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
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
