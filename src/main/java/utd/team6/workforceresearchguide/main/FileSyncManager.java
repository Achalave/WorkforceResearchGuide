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
import utd.team6.workforceresearchguide.main.issues.MissingFileIssue;
import utd.team6.workforceresearchguide.main.issues.MovedFileIssue;
import utd.team6.workforceresearchguide.main.issues.OutdatedFileIssue;
import utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException;
import utd.team6.workforceresearchguide.sqlite.DatabaseController;
import utd.team6.workforceresearchguide.sqlite.DatabaseFileDoesNotExistException;

//@author Michael Haertling
public class FileSyncManager {

    private final LuceneController lucene;
    private final DatabaseController db;
    private final String[] files;

    private FileSynchIssue[] issues;

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
    }

    /**
     * Note: The DocumentData file provided is filled out completely if it is
     * determined to be outdated.
     *
     * @param file
     * @return True if the specified file is out of date with regards to the
     * last indexed version.
     * @throws SQLException
     * @throws DatabaseFileDoesNotExistException
     * @throws IOException
     */
    private boolean fileIsOutdated(DocumentData file) throws SQLException, DatabaseFileDoesNotExistException, IOException, ConnectionNotStartedException {
        file.fillLastModDate();
        //Check if the file has been modified
        DocumentData data = db.getDocumentData(file.getPath());
        if (data.getLastModDate().compareTo(file.getLastModDate()) != 0) {
            //The file may be modified
            //Hash it and compare the hash values
            file.fillHash();
            String hash = data.getHash();
            if (!hash.equals(file.getHash())) {
                file.setHits(data.getHits());
                file.setName(data.getName());
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param oldDoc
     * @param addedFiles This is a list of the files that are new to the system
     * or candidates for being a relocated file.
     * @return The file path to the possible moved file location or null if no
     * replacement potential was found.
     */
    private DocumentData identifyRelocatedFile(DocumentData oldDoc, List<String> addedFiles) throws SQLException, DatabaseFileDoesNotExistException, IOException {
        for (String pf : addedFiles) {
            //Compare names
            if (pf.endsWith(oldDoc.getName())) {
                DocumentData newDoc = new DocumentData(pf);
                newDoc.fillLastModDate();
                //Compare last modification dates
                if (newDoc.getLastModDate().compareTo(oldDoc.getLastModDate()) == 0) {
                    //Compare hash values
                    newDoc.fillHash();
                    if (oldDoc.getHash().equals(newDoc.getHash())) {
                        //This is probably the same file
                        return newDoc;
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
     * @throws utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
     */
    public FileSynchIssue[] examineDifferences() throws SQLException, DatabaseFileDoesNotExistException, IOException, ConnectionNotStartedException {
        ArrayList<FileSynchIssue> isus = new ArrayList<>();

        ArrayList<String> missingFiles = new ArrayList<>();
        ArrayList<String> addedFiles = new ArrayList<>();
        ArrayList<String> existingFiles = new ArrayList<>();

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
            DocumentData f = new DocumentData(file);
            if (fileIsOutdated(f)) {
                isus.add(new OutdatedFileIssue(f));
            }
        }

        //Check if any of the new files are simply relocated ones
        fileIterator = missingFiles.iterator();

        while (fileIterator.hasNext()) {
            String file = fileIterator.next();
            DocumentData oldFile = db.getDocumentData(file);
            DocumentData relFile = this.identifyRelocatedFile(oldFile, addedFiles);
            if (relFile != null) {
                isus.add(new MovedFileIssue(oldFile, relFile));
            } else {
                isus.add(new MissingFileIssue(oldFile));
            }
        }
        this.issues = new FileSynchIssue[isus.size()];
        this.issues = isus.toArray(this.issues);
        return this.issues;
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
        lucene.startIndexingSession();
        lucene.startReadSession();

        indexReadyFiles = new LinkedBlockingQueue<>();

        databaseThread = new DatabaseThread();
        luceneThread = new LuceneThread();

        databaseThread.start();
        luceneThread.start();
    }

    /**
     * Stops the synchronization process.
     *
     * @throws IOException
     */
    public void cancelSynch() throws IOException {
        lucene.rollbackIndexingSession();
        lucene.stopIndexingSession();
        //Stop the threads

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
