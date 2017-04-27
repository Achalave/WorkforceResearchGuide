package gentest;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import utd.team6.workforceresearchguide.main.DocumentData;
import utd.team6.workforceresearchguide.sqlite.ConnectionAlreadyActiveException;
import utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException;
import utd.team6.workforceresearchguide.sqlite.DatabaseController;
import utd.team6.workforceresearchguide.sqlite.DatabaseFileDoesNotExistException;
import utd.team6.workforceresearchguide.sqlite.DatabaseTagDoesNotExistException;

//@author Michael Haertling

/**
 *
 * @author Michael
 */
public class DatabaseControllerTest {

    private static DatabaseController db;

    /**
     *
     * @throws ConnectionNotStartedException
     * @throws ConnectionAlreadyActiveException
     */
    public DatabaseControllerTest() throws ConnectionNotStartedException, ConnectionAlreadyActiveException {
        new File("test.db").delete();
        db = new DatabaseController("test.db");
        db.startConnection();
        db.updateDatabaseSchema();
        db.stopConnection();
    }

    /**
     *
     * @param args
     * @throws SQLException
     * @throws ConnectionAlreadyActiveException
     * @throws ConnectionNotStartedException
     */
    public static void main(String[] args) throws SQLException, ConnectionAlreadyActiveException, ConnectionNotStartedException {
        DatabaseControllerTest dbt = new DatabaseControllerTest();
//        dbt.testAddDocument();
//        dbt.testRollback();
//        dbt.testUpdateDocumentPath();
        dbt.testTagDocument();
//        dbt.testGroups();
    }

    /**
     * Tests the groups function of the DatabaseController.
     */
    public void testGroups(){
        try {
            db.startConnection();
            db.addGroup("testGroup");
            DocumentData data = new DocumentData(TestUtils.PATH_TO_RESOURCES+"\\test1.txt");
            data.fillFromFile();
            db.addDocument(data);
            db.addFileToGroup("testGroup", data.getPath());
            db.printQuery("SELECT * FROM GROUPS");
            db.printQuery("SELECT * FROM FILES");
            db.printQuery("SELECT * FROM GROUP_FILES");
            
            System.out.println("\nPHASE 2");
            db.deleteFileFromGroup("testGroup", data.getPath());
            db.printQuery("SELECT * FROM GROUPS");
            db.printQuery("SELECT * FROM FILES");
            db.printQuery("SELECT * FROM GROUP_FILES");
            
            System.out.println("\nPHASE 3");
            db.addFileToGroup("testGroup", data.getPath());
            db.deleteGroup("testGroup");
            db.printQuery("SELECT * FROM GROUPS");
            db.printQuery("SELECT * FROM FILES");
            db.printQuery("SELECT * FROM GROUP_FILES");
            
        } catch (ConnectionNotStartedException | IOException | ConnectionAlreadyActiveException ex) {
            Logger.getLogger(DatabaseControllerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Tests the adding of documents in the DatabaseController.
     */
    public void testAddDocument() {
        try {
            DocumentData doc = new DocumentData(TestUtils.PATH_TO_RESOURCES + "\\test1.txt");
            doc.fillFromFile();
            db.startConnection();
            db.addDocument(doc);
            db.printQuery("SELECT * FROM FILES");
            db.stopConnection();
        } catch (ConnectionNotStartedException | ConnectionAlreadyActiveException | IOException ex) {
            Logger.getLogger(DatabaseControllerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Tests the rollback function of the DatabaseController.
     */
    public void testRollback() {
        try {
            DocumentData doc = new DocumentData(TestUtils.PATH_TO_RESOURCES + "\\test1.txt");
            doc.fillFromFile();
            db.startConnection();
            db.addDocument(doc);
            db.stopConnection();

            doc = new DocumentData(TestUtils.PATH_TO_RESOURCES + "\\test2.txt");
            doc.fillFromFile();
            db.startConnection();
            db.addDocument(doc);
            db.rollbackConnection();

            db.startConnection();
            db.printQuery("SELECT * FROM FILES");
            db.stopConnection();
        } catch (ConnectionNotStartedException | ConnectionAlreadyActiveException | IOException ex) {
            Logger.getLogger(DatabaseControllerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Tests the updating of document paths in the DatabaseController.
     */
    public void testUpdateDocumentPath() {
        try {
            String path = TestUtils.PATH_TO_RESOURCES + "\\test1.txt";
            DocumentData doc = new DocumentData(path);
            doc.fillFromFile();
            db.startConnection();
            db.addDocument(doc);
            db.updateDocumentPath(path, "fake/new/path");
            db.printQuery("SELECT * FROM FILES");
            db.stopConnection();
        } catch (ConnectionNotStartedException | ConnectionAlreadyActiveException | IOException | DatabaseFileDoesNotExistException ex) {
            Logger.getLogger(DatabaseControllerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Tests the tagging of documents in the DatabaseController.
     */
    public void testTagDocument() {
        try {
            String path = TestUtils.PATH_TO_RESOURCES + "\\test1.txt";
            DocumentData doc = new DocumentData(path);
            doc.fillFromFile();
            db.startConnection();
            db.addDocument(doc);
            db.addTag("tag1");
            db.tagDocument(path, "tag1");
            db.printQuery("SELECT * FROM FILES");
            db.printQuery("SELECT * FROM FILE_TAGS");
            db.printQuery("SELECT * FROM TAGS");
            db.stopConnection();
        } catch (ConnectionNotStartedException | ConnectionAlreadyActiveException | IOException | DatabaseFileDoesNotExistException | DatabaseTagDoesNotExistException ex) {
            Logger.getLogger(DatabaseControllerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
