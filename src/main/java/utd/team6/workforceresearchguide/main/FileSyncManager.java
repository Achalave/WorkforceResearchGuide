package utd.team6.workforceresearchguide.main;

import utd.team6.workforceresearchguide.main.issues.FileSynchIssue;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import utd.team6.workforceresearchguide.lucene.IndexingSessionNotStartedException;
import utd.team6.workforceresearchguide.lucene.LuceneController;
import utd.team6.workforceresearchguide.lucene.ReadSessionNotStartedException;
import utd.team6.workforceresearchguide.main.issues.FailedFileSynchIssue;
import utd.team6.workforceresearchguide.main.issues.InvalidResponseException;
import utd.team6.workforceresearchguide.main.issues.InvalidResponseFaliure;
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

    IssueResolutionThread[] resolutionThreads;

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
     * @throws
     * utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException
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
     * Begins the issue resolution process between the application files and the
     * repository files. This function should only be called after the
     * examineDifferences function has been called and all issues have been
     * resolved with the user.
     *
     * @param numThreads The number of threads to perform this operation with.
     * Must be greater than 0.
     * @throws IOException
     * @throws SQLException
     * @throws DatabaseFileDoesNotExistException
     */
    public void startResolutionProcess(int numThreads) throws IOException, SQLException, DatabaseFileDoesNotExistException {
        if (numThreads <= 0) {
            throw new IllegalArgumentException("The argument numThreads must be >= 0.");
        }
        lucene.startIndexingSession();
        lucene.startReadSession();

        int startIndex = 0;
        int numIssues = issues.length;
        int div = numIssues / numThreads;
        int rem = numIssues % numThreads;

        resolutionThreads = new IssueResolutionThread[numThreads];

        //Create the issue threads
        for (int i = resolutionThreads.length - 1; i > -1; i--) {
            if (i > 0) {
                resolutionThreads[i] = new IssueResolutionThread(issues, startIndex, div);
                resolutionThreads[i].start();
                //Increment the start index
                startIndex += div;
            } else {
                resolutionThreads[i] = new IssueResolutionThread(issues, startIndex, div + rem);
                resolutionThreads[i].start();
                //The start index does not need to be incremented because the loop will close
            }
        }

    }

    /**
     *
     * @return The ratio of completion for an ongoing resolution process. This
     * is will return 0 if there is no ongoing resolution process.
     */
    public double getCompletionRatio() {
        if (resolutionThreads == null) {
            return 0;
        }
        double sum = 0;
        for (IssueResolutionThread t : resolutionThreads) {
            sum += t.getCompletionRatio();
        }
        return sum / resolutionThreads.length;
    }

    /**
     *
     * @return True if there is an ongoing resolution process and false
     * otherwise.
     */
    public boolean isResolutionActive() {
        for (IssueResolutionThread t : resolutionThreads) {
            if (t.isAlive()) {
                return true;
            }
        }
        return false;
    }

    /**
     * This function should only be called when the resolution process is not
     * running.
     *
     * @return A list of all failed file issues. Returns null if the resolution
     * process was never started.
     */
    public List<FailedFileSynchIssue> getFileSynchIssues() {
        ArrayList<FailedFileSynchIssue> faliures = new ArrayList<>();
        for (IssueResolutionThread t : resolutionThreads) {
            faliures.addAll(t.faliures);
        }
        return faliures;
    }

    /**
     * Stops the resolution process.
     *
     * @throws IOException
     */
    public void cancelResolutionProcess() throws IOException {
        //Stop the threads
        for (IssueResolutionThread t : resolutionThreads) {
            t.interrupt();
        }
        lucene.rollbackIndexingSession();
        lucene.stopIndexingSession();
    }

    class IssueResolutionThread extends Thread {

        int index;
        FileSynchIssue[] issues;
        ArrayList<FailedFileSynchIssue> faliures;

        /**
         * Instantiates this thread object with num FileSynchIssue(s) from the
         * start position of the provided array of issues.
         *
         * @param isus
         * @param start
         * @param num
         */
        public IssueResolutionThread(FileSynchIssue[] isus, int start, int num) {
            issues = new FileSynchIssue[num];
            for (int i = 0; i < issues.length; i++) {
                issues[i] = isus[i + start];
            }
            index = 0;
            faliures = new ArrayList<>();
        }

        public IssueResolutionThread(FileSynchIssue[] issues) {
            this.issues = issues;
            index = 0;
            faliures = new ArrayList<>();
        }

        @Override
        public void run() {
            while (index < issues.length) {
                if (this.isInterrupted()) {
                    break;
                }
                try {
                    issues[index].resolve(db, lucene);
                } catch (InvalidResponseException ex) {
                    //Create a new file error issue
                    faliures.add(new InvalidResponseFaliure(issues[index]));
                } catch (IndexingSessionNotStartedException | ReadSessionNotStartedException ex) {
                    if (this.isInterrupted()) {
                        break;
                    } else {
                        Logger.getLogger(FileSyncManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(FileSyncManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                index++;
            }
        }

        public double getCompletionRatio() {
            return (double) index / issues.length;
        }
    }

}
