package utd.team6.workforceresearchguide.main;

import utd.team6.workforceresearchguide.main.issues.FileSyncIssue;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.tika.exception.TikaException;
import utd.team6.workforceresearchguide.lucene.IndexingSessionNotStartedException;
import utd.team6.workforceresearchguide.lucene.LuceneController;
import utd.team6.workforceresearchguide.lucene.ReadSessionNotStartedException;
import utd.team6.workforceresearchguide.main.issues.AddedFileIssue;
import utd.team6.workforceresearchguide.main.issues.FailedFileSyncIssue;
import utd.team6.workforceresearchguide.main.issues.InvalidResponseException;
import utd.team6.workforceresearchguide.main.issues.InvalidResponseFailure;
import utd.team6.workforceresearchguide.main.issues.MissingFileIssue;
import utd.team6.workforceresearchguide.main.issues.MovedFileIssue;
import utd.team6.workforceresearchguide.main.issues.OutdatedFileIssue;
import utd.team6.workforceresearchguide.sqlite.ConnectionAlreadyActiveException;
import utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException;
import utd.team6.workforceresearchguide.sqlite.DatabaseController;
import utd.team6.workforceresearchguide.sqlite.DatabaseFileDoesNotExistException;

/**
 * This class is used to synchronize the file repository with the system. This
 * is done in two phases. First the repository is compared with the system
 * representation and any discrepancies are converted into issue object. Then,
 * these issues are resolved after a particular option is selected by either the
 * user or the application.
 *
 * @author Michael
 */
public class FileSyncManager {

    private final LuceneController lucene;
    private final DatabaseController db;
    private final ArrayList<String> files;

    private FileSyncIssue[] issues;

    IssueResolutionThread[] resolutionThreads;

    SessionManager sess;

    String waitMessage = "";

    /**
     * Creates a new FileSyncManager object.
     *
     * @param sess
     * @param lucene
     * @param db
     * @param files
     */
    public FileSyncManager(SessionManager sess, LuceneController lucene, DatabaseController db, ArrayList<String> files) {
        this.lucene = lucene;
        this.db = db;
        this.files = files;
        this.sess = sess;
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
    private boolean fileIsOutdated(DocumentData file) throws SQLException, DatabaseFileDoesNotExistException, IOException, ConnectionNotStartedException, ParseException {
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
     * @return The index of the added file that is suspected to the the
     * relocated version of the oldDoc parameter. Returns -1 if no such document
     * is found.
     */
    private int identifyRelocatedFile(DocumentData oldDoc, List<DocumentData> addedFiles) throws SQLException, DatabaseFileDoesNotExistException, IOException {
        for (int i = 0; i < addedFiles.size(); i++) {
            DocumentData newDoc = addedFiles.get(i);
            newDoc.fillName();
            //Compare names
            System.out.println("COMPARE\n" + newDoc.getName() + "\n" + oldDoc.getName());
            if (newDoc.getName().equals(oldDoc.getName())) {
                newDoc.fillLastModDate();
                //Compare last modification dates
                System.out.println("COMPARE LMD\n" + newDoc.getLastModDate().getTime() + "\n" + oldDoc.getLastModDate().getTime() + "\n" + newDoc.getLastModDate().compareTo(oldDoc.getLastModDate()));
                if (Utils.equalDates(newDoc.getLastModDate(), oldDoc.getLastModDate())) {
                    //Compare hash values
                    newDoc.fillHash();
                    System.out.println("COMPARE HASH\n" + newDoc.getHash() + "\n" + oldDoc.getHash());
                    if (oldDoc.getHash().equals(newDoc.getHash())) {
                        //This is probably the same file
                        return i;
                    }
                }
            }
        }
        return -1;
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
     * @throws java.text.ParseException
     */
    public FileSyncIssue[] examineDifferences() throws SQLException, DatabaseFileDoesNotExistException, IOException, ConnectionNotStartedException, ParseException {
        waitMessage = "Waiting for Files to be Unused";
        sess.getSessionPermission();
        waitMessage = "Scanning Repository";

        sess.startDBConnection();

        //Get a list of the files in the SQL database
        String[] dbFiles = db.getAllKnownFiles();

        ArrayList<FileSyncIssue> isus = new ArrayList<>();

        ArrayList<String> missingFiles = new ArrayList<>();
        ArrayList<String> addedFiles = new ArrayList<>();
        ArrayList<String> existingFiles = new ArrayList<>();

        addedFiles.addAll(files);

        Collections.sort(addedFiles);

        Collections.addAll(missingFiles, dbFiles);

        Iterator<String> fileIterator = missingFiles.iterator();

//        System.out.println("INTERNAL SCANNED FILES: " + addedFiles);
//        System.out.println("INTERNAL DB FILES: " + missingFiles);
        while (fileIterator.hasNext()) {
            int index = Collections.binarySearch(addedFiles, fileIterator.next());
            if (index >= 0) {
                fileIterator.remove();
                String file = addedFiles.remove(index);
                existingFiles.add(file);
            }
        }

//        System.out.println("INTERNAL: " + missingFiles);
        //For the files that exist, check if they are up to date
        for (String file : existingFiles) {
            DocumentData f = new DocumentData(file);
            if (fileIsOutdated(f)) {
                isus.add(new OutdatedFileIssue(f));
            }
        }

        //Create DocumentData objects for all added files.
        ArrayList<DocumentData> addedFileData = new ArrayList<>();
        for (String addFile : addedFiles) {
            addedFileData.add(new DocumentData(addFile));
        }

        //Check if any of the new files are simply relocated ones
        fileIterator = missingFiles.iterator();

        while (fileIterator.hasNext()) {
            String file = fileIterator.next();
            DocumentData missingFile = db.getDocumentData(file);
            int relocatedFile = this.identifyRelocatedFile(missingFile, addedFileData);
//            System.out.println("RELOCATION: " + missingFile.getPath() + "\t" + relocatedFile);
            if (relocatedFile >= 0) {
                isus.add(new MovedFileIssue(missingFile, addedFileData.remove(relocatedFile)));
            } else {
                isus.add(new MissingFileIssue(missingFile));
            }
        }

        //Create issues for the remaining added files
        for (DocumentData add : addedFileData) {
            isus.add(new AddedFileIssue(add));
        }

        this.issues = new FileSyncIssue[isus.size()];
        this.issues = isus.toArray(this.issues);

        sess.stopDBConnection();
        sess.releaseSessionPermission();

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
        waitMessage = "Resolving Issues";
        if (numThreads <= 0) {
            throw new IllegalArgumentException("The argument numThreads must be >= 0.");
        }

        sess.getSessionPermission();
        sess.startLuceneIndexingSession();
        sess.startLuceneReadSession();
        sess.startDBConnection();

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
     * Finalizes the resolution process. This includes closing all sessions
     * used. This should be called after the resolution threads have completed.
     */
    public void finalizeResolution() {
        sess.stopDBConnection();
        sess.stopLuceneIndexingSession();
        sess.stopLuceneReadSession();
        sess.releaseSessionPermission();
    }

    /**
     * Cancels the scan process.
     */
    public void cancelScan() {

    }

    /**
     *
     * @return The current wait message that should be displayed.
     */
    public String getWaitMessage() {
        return waitMessage;
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
    public List<FailedFileSyncIssue> getFileSynchIssues() {
        ArrayList<FailedFileSyncIssue> faliures = new ArrayList<>();
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
        sess.rollbackDBConnection();
        sess.rollbackIndexingSession();
        sess.stopLuceneReadSession();
        sess.releaseSessionPermission();
    }

    class IssueResolutionThread extends Thread {

        int index;
        FileSyncIssue[] issues;
        ArrayList<FailedFileSyncIssue> faliures;

        /**
         * Instantiates this thread object with num FileSynchIssue(s) from the
         * start position of the provided array of issues.
         *
         * @param isus
         * @param start
         * @param num
         */
        public IssueResolutionThread(FileSyncIssue[] isus, int start, int num) {
            issues = new FileSyncIssue[num];
            for (int i = 0; i < issues.length; i++) {
                issues[i] = isus[i + start];
            }
            index = 0;
            faliures = new ArrayList<>();
        }

        public IssueResolutionThread(FileSyncIssue[] issues) {
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
                    faliures.add(new InvalidResponseFailure(issues[index]));
                } catch (IndexingSessionNotStartedException | ReadSessionNotStartedException ex) {
                    if (this.isInterrupted()) {
                        break;
                    } else {
                        Logger.getLogger(FileSyncManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (IOException | TikaException | ConnectionNotStartedException | SQLException ex) {
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
