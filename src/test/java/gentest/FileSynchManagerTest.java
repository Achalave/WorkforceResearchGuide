package gentest;

import com.google.common.io.Files;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tika.exception.TikaException;
import org.codehaus.plexus.util.FileUtils;
import utd.team6.workforceresearchguide.lucene.IndexingSessionNotStartedException;
import utd.team6.workforceresearchguide.lucene.LuceneController;
import utd.team6.workforceresearchguide.lucene.ReadSessionNotStartedException;
import utd.team6.workforceresearchguide.main.DocumentData;
import utd.team6.workforceresearchguide.main.FileSyncManager;
import utd.team6.workforceresearchguide.main.Utils;
import utd.team6.workforceresearchguide.main.issues.AddedFileIssue;
import utd.team6.workforceresearchguide.main.issues.FileSyncIssue;
import utd.team6.workforceresearchguide.main.issues.InvalidResponseException;
import utd.team6.workforceresearchguide.sqlite.ConnectionAlreadyActiveException;
import utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException;
import utd.team6.workforceresearchguide.sqlite.DatabaseController;
import utd.team6.workforceresearchguide.sqlite.DatabaseFileDoesNotExistException;

//@author Michael Haertling

/**
 *
 * @author Michael
 */
public class FileSynchManagerTest {

    DatabaseController db;
    LuceneController lucene;
    FileSyncManager sync;

    /**
     *
     * @param files
     * @param renew
     * @throws IOException
     * @throws SQLException
     * @throws ConnectionNotStartedException
     * @throws ConnectionAlreadyActiveException
     */
    public FileSynchManagerTest(String[] files, boolean renew) throws IOException, SQLException, ConnectionNotStartedException, ConnectionAlreadyActiveException {
        if (renew) {
            new File("test.db").delete();
            FileUtils.forceDelete("lucene_test_files");
        }
        db = new DatabaseController("test.db");
        lucene = new LuceneController("lucene_files");
        sync = new FileSyncManager(lucene, db, files);
        db.startConnection();
        db.updateDatabaseSchema();
        db.stopConnection();
    }

    /**
     *
     * @return
     */
    public static String[] getPaths(){
        ArrayList<String> paths = Utils.extractAllPaths(TestUtils.PATH_TO_RESOURCES);
        String[] p = new String[paths.size()];
        return paths.toArray(p);
    }
    
    /**
     *
     * @param args
     * @throws IOException
     * @throws SQLException
     * @throws ParseException
     * @throws ConnectionNotStartedException
     * @throws ConnectionAlreadyActiveException
     */
    public static void main(String[] args) throws IOException, SQLException, ParseException, ConnectionNotStartedException, ConnectionAlreadyActiveException {
//        FileSynchManagerTest.generateFile(TestUtils.PATH_TO_RESOURCES + "/tmp.txt");
//        System.out.println("ORIGINAL LMD: "+new File(TestUtils.PATH_TO_RESOURCES + "/tmp.txt").lastModified());
//        
//        String[] p = getPaths();
//        FileSynchManagerTest fsmt = new FileSynchManagerTest(p,true);
//        fsmt.testDocumentMoveP1();
//        
//        
//        p = getPaths();
//        fsmt = new FileSynchManagerTest(p,false);
//        fsmt.testDocumentMoveP2();
        FileSynchManagerTest test = new FileSynchManagerTest(null,true);
        test.testAddedFileResolution();
    }

    /**
     * Runs stage 1 of the SyncManager and outputs the issues found. There
     * should be some add issues.
     *
     * @throws ParseException
     */
    public void testStage1() throws ParseException {
        try {
            db.startConnection();
            System.out.println("PASS 1");
            FileSyncIssue[] issues = sync.examineDifferences();
            System.out.println(Arrays.toString(issues));
            db.stopConnection();
        } catch (SQLException | DatabaseFileDoesNotExistException | IOException | ConnectionNotStartedException | ConnectionAlreadyActiveException ex) {
            Logger.getLogger(FileSynchManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Runs stage 1 of the SyncManager, prints issues found, resolves all
     * issues, then runs again. The second run should reveal no issues.
     *
     * @throws ParseException
     */
    public void testStage1Extended() throws ParseException {
        try {
            db.startConnection();
            lucene.startIndexingSession(false);
            System.out.println("PASS 1");
            FileSyncIssue[] issues = sync.examineDifferences();
            System.out.println(Arrays.toString(issues));

            //Resolve the issues
            System.out.println("RESOLVING ISSUES");
            for (FileSyncIssue iss : issues) {
                iss.setUserResponse(AddedFileIssue.RESPONSE_ADD_FILE);
                iss.resolve(db, lucene);
            }

            System.out.println("PASS 2");
            issues = sync.examineDifferences();
            System.out.println(Arrays.toString(issues));

            lucene.stopIndexingSession();
            db.stopConnection();
        } catch (SQLException | DatabaseFileDoesNotExistException | IOException | ConnectionNotStartedException | ConnectionAlreadyActiveException | InvalidResponseException | IndexingSessionNotStartedException | ReadSessionNotStartedException | TikaException ex) {
            Logger.getLogger(FileSynchManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Part 1 of testing document moves.
     */
    public void testDocumentMoveP1() {
        try {
            db.startConnection();
            lucene.startIndexingSession(false);

            System.out.println("PASS 1");
            FileSyncIssue[] issues = sync.examineDifferences();
            System.out.println(Arrays.toString(issues));

            //Resolve the issues
            System.out.println("RESOLVING ISSUES");
            for (FileSyncIssue iss : issues) {
                System.out.println("RESOLVING: " + iss);
                iss.setUserResponse(AddedFileIssue.RESPONSE_ADD_FILE);
                iss.resolve(db, lucene);
            }

            //Move the temp file\
            System.out.println("MOVING THE TEMP FILE");
            new File(TestUtils.PATH_TO_RESOURCES + "/tmpFolder/").mkdir();
            Files.copy(new File(TestUtils.PATH_TO_RESOURCES + "/tmp.txt"), new File(TestUtils.PATH_TO_RESOURCES + "/tmpFolder/tmp.txt"));
            new File(TestUtils.PATH_TO_RESOURCES + "/tmp.txt").delete();

            db.stopConnection();
            lucene.stopIndexingSession();
        } catch (InvalidResponseException | IndexingSessionNotStartedException | ReadSessionNotStartedException | IOException | TikaException | ConnectionNotStartedException | SQLException | ConnectionAlreadyActiveException | DatabaseFileDoesNotExistException | ParseException ex) {
            Logger.getLogger(FileSynchManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Part 2 of testing document moves.
     */
    public void testDocumentMoveP2() {
        try {
            db.startConnection();
            
            System.out.println("PASS 2");
            FileSyncIssue[] issues = sync.examineDifferences();
            System.out.println(Arrays.toString(issues));
            
            //Delete the temp file
            FileUtils.forceDelete(TestUtils.PATH_TO_RESOURCES+"/tmpFolder");

            db.stopConnection();
        } catch (IOException | ConnectionNotStartedException | SQLException | DatabaseFileDoesNotExistException | ParseException | ConnectionAlreadyActiveException ex) {
            Logger.getLogger(FileSynchManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Tests the resolution of file added issues.
     */
    public void testAddedFileResolution(){
        try {
            db.startConnection();
            lucene.startIndexingSession(false);
            AddedFileIssue iss = new AddedFileIssue(new DocumentData(TestUtils.PATH_TO_RESOURCES+"/test1.txt"));
            iss.addFile();
            iss.resolve(db, lucene);
            
            System.out.println(Arrays.toString(db.getAllKnownFiles()));
            
            db.stopConnection();
            lucene.stopIndexingSession();
        } catch (InvalidResponseException | IndexingSessionNotStartedException | ReadSessionNotStartedException | IOException | TikaException | ConnectionNotStartedException | SQLException | ConnectionAlreadyActiveException ex) {
            Logger.getLogger(FileSynchManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Tests the moved file issues.
     */
    public void testMovedFileResolution(){
        try {
            db.startConnection();
            lucene.startIndexingSession(false);
            AddedFileIssue iss = new AddedFileIssue(new DocumentData(TestUtils.PATH_TO_RESOURCES+"/test1.txt"));
            iss.addFile();
            iss.resolve(db, lucene);
            db.stopConnection();
            lucene.stopIndexingSession();
        } catch (InvalidResponseException | IndexingSessionNotStartedException | ReadSessionNotStartedException | IOException | TikaException | ConnectionNotStartedException | SQLException | ConnectionAlreadyActiveException ex) {
            Logger.getLogger(FileSynchManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Tests missing file issues.
     */
    public void testMissingFileResolution(){
        
    }
    
    /**
     * Tests outdated file issues.
     */
    public void testOutdatedFileResolution(){
        
    }
    
    /**
     *
     * @param path
     */
    public static void generateFile(String path) {
        try (PrintWriter write = new PrintWriter(path)) {
            write.print("this is just a test file");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileSynchManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
