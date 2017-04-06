package utd.team6.workforceresearchguide.main;

import utd.team6.workforceresearchguide.main.issues.FileSynchIssue;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tika.exception.TikaException;
import utd.team6.workforceresearchguide.lucene.IndexingSessionNotStartedException;
import utd.team6.workforceresearchguide.lucene.LuceneController;
import utd.team6.workforceresearchguide.lucene.ReadSessionNotStartedException;
import utd.team6.workforceresearchguide.sqlite.DatabaseController;
import utd.team6.workforceresearchguide.sqlite.DatabaseFileDoesNotExistException;

//@author Michael Haertling
public class FileSyncManager {

    private final LuceneController lucene;
    private final DatabaseController db;
    private final String[] files;

    private final ArrayList<String> missingFiles;
    private final ArrayList<String> addedFiles;
    private final ArrayList<String> existingFiles;
    private final ArrayList<String> outdatedFiles;
    private final HashMap<String, DocumentData> movedFiles;

    BlockingQueue<IndexReadyFile> indexReadyFiles;

    DatabaseThread databaseThread;
    LuceneThread luceneThread;

    private class IndexReadyFile {

        String path, hash;

        public IndexReadyFile(String path, String hash) {
            this.path = path;
            this.hash = hash;
        }
    }

    public FileSyncManager(LuceneController lucene, DatabaseController db, String[] files) {
        this.lucene = lucene;
        this.db = db;
        this.files = files;
        missingFiles = new ArrayList<>();
        addedFiles = new ArrayList<>();
        existingFiles = new ArrayList<>();
        outdatedFiles = new ArrayList<>();
        movedFiles = new HashMap<>();
    }

    /**
     * This function finds all files that are missing from and added to the
     * physical repository by comparing the list of files to those recorded in
     * the database.
     */
    private void scanRepository() throws SQLException, IOException, DatabaseFileDoesNotExistException {
        Collections.addAll(addedFiles, files);
        Collections.sort(addedFiles);

        //Get a list of the files in the SQL database
        String[] dbFiles = db.getAllKnownFiles();
        Collections.addAll(missingFiles, dbFiles);

        Iterator<String> fileIterator = missingFiles.iterator();
        while (fileIterator.hasNext()) {
            int index = Collections.binarySearch(addedFiles, fileIterator.next());
            if (index >= 0) {
                fileIterator.remove();
                String file = addedFiles.remove(index);
                existingFiles.add(file);
            }
        }

        //For the files that exist, check if they are up to date
        for (String file : existingFiles) {
            if (fileIsOutdated(file)) {
                outdatedFiles.add(file);
            }
        }

        //Check if any of the new files are simply relocated ones
        fileIterator = missingFiles.iterator();
        while (fileIterator.hasNext()) {
            String file = fileIterator.next();
            DocumentData relFile = this.identifyRelocatedFile(file, addedFiles);
            if (relFile != null) {
                movedFiles.put(file, relFile);
                fileIterator.remove();
                //Remove from added files using binary search, since it should still be sorted
                int index = Collections.binarySearch(addedFiles, relFile.getPath());
                existingFiles.remove(index);
            }
        }

    }

    /**
     *
     * @param file
     * @return True if the specified file is out of date with regards to the
     * last indexed version.
     * @throws SQLException
     * @throws DatabaseFileDoesNotExistException
     * @throws IOException
     */
    private boolean fileIsOutdated(String file) throws SQLException, DatabaseFileDoesNotExistException, IOException {
        int fileID = db.getDocumentID(file);
        File f = new File(file);
        Date lmd = new Date(f.lastModified());
        //Check if the file has been modified
        DocumentData data = db.getDocumentData(fileID);
        if (data.getLastModDate().compareTo(lmd) != 0) {
            //The file may be modified
            //Hash it and compare the hash values
            data.fillHash();
            String hash = data.getHash();
            if (!hash.equals(data.getHash())) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param file
     * @param addedFiles This is a list of the files that are new to the system
     * or candidates for being a relocated file.
     * @return The file path to the possible moved file location or null if no
     * replacement potential was found.
     */
    private DocumentData identifyRelocatedFile(String file, List<String> addedFiles) throws SQLException, DatabaseFileDoesNotExistException, IOException {
        File f = new File(file);
        for (String pf : addedFiles) {
            //Compare names
            if (pf.endsWith(f.getName())) {
                Date newLMD = new Date(f.lastModified());
                DocumentData data = db.getDocumentData(file);
                //Compare last modification dates
                if (newLMD.compareTo(data.getLastModDate()) == 0) {
                    //Compare hash values
                    data.fillHash();
                    String hash = data.getHash();
                    if (hash.equals(data.getHash())) {
                        //This is probably the same file
                        return new DocumentData(pf, data.getName(), data.getLastModDate(), data.getHits(), hash);
                    }
                }
            }
        }
        return null;
    }

    /**
     * This function compares the database records with the list of files
     * currently in the repository and compiles any issues to be resolved into a
     * set of issue objects that should be displayed to the user.
     *
     * @return 
     * @throws java.sql.SQLException
     * @throws
     * utd.team6.workforceresearchguide.sqlite.DatabaseFileDoesNotExistException
     * @throws java.io.IOException
     */
    public FileSynchIssue[] examineDifferences() throws SQLException, DatabaseFileDoesNotExistException, IOException {
        scanRepository();
        //Generate a list of FileSynchIssue(s)
        
        //Look at missing files
        
        //Look at moved files
        
        //Look at outdated files
        
        return null;
    }

    /**
     * Begins the indexing process between the application files and the
     * repository files. This function should only be called after the
     * examineDifferences function has been called and all issues have been
     * resolved with the user.
     *
     * @throws IOException
     * @throws SQLException
     * @throws DatabaseFileDoesNotExistException
     */
    public void startIndexingProcess() throws IOException, SQLException, DatabaseFileDoesNotExistException {
        scanRepository();

        lucene.startIndexingSession();
        lucene.startReadSession();

        indexReadyFiles = new LinkedBlockingQueue<>();

        databaseThread = new DatabaseThread();
        luceneThread = new LuceneThread();

        databaseThread.start();
        luceneThread.start();
    }

    public void cancelSynch() throws IOException {
        lucene.rollbackIndexingSession();
        lucene.stopIndexingSession();
    }

    class LuceneThread extends Thread {

        boolean expectInputs = true;

        @Override
        public void run() {

        }

        public void inputsComplete() {
            expectInputs = false;
        }
    }

    class DatabaseThread extends Thread {

        @Override
        public void run() {

            //Check if any of the new files are simply relocated ones
            //Add the file to the databse
//                        String hash = Utils.hashFile(file);
//                        db.addDocument(file, lmd, hash);
//                        //Add the file to the queue
//                        indexReadyFiles.put(new IndexReadyFile(file, hash));
            luceneThread.inputsComplete();
        }
    }

}
