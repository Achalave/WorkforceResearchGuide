package utd.team6.workforceresearchguide.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Date;
import org.apache.commons.codec.digest.DigestUtils;
import utd.team6.workforceresearchguide.sqlite.DatabaseController;
import utd.team6.workforceresearchguide.sqlite.DatabaseFileDoesNotExistException;

//@author Michael Haertling
/**
 * This class is used to store all the pertinent data on a single document. It's
 * intended use is to allow the transmission of such data as well as prevent the
 * need for re-calculating pieces of this data when files are being added,
 * moved, and removed. It is possible that some fields may not be filled out and
 * will need to be before the data can be entered into the system.
 *
 * @author Michael
 */
public class DocumentData {

    private String path;
    private String name;
    private Date lastModDate;
    /**
     * This is the number of times this document has been successfully returned
     * in search results. The object version of int is used in order to track if
     * the field has been set. The path variable should always be set as it is
     * the defining fields for any document.
     */
    private Integer hits;
    private String hash;

    /**
     * Instantiate the object with none of the meta fields filled out. A path
     * must be provided.
     *
     * @param path
     */
    public DocumentData(String path) {
        if (path == null) {
            throw new IllegalArgumentException();
        }
        this.path = path;
    }

    /**
     * Instantiate the object with one or more fields filled out. A path must be
     * provided
     *
     * @param path
     * @param name
     * @param date
     * @param hits
     * @param hash
     */
    public DocumentData(String path, String name, Date date, int hits, String hash) {
        this(path);
        this.name = name;
        this.lastModDate = date;
        this.hits = hits;
        this.hash = hash;
    }

    /**
     *
     * @return True if all data fields have been filled out and false otherwise.
     */
    public boolean dataComplete() {
        return path != null && name != null && lastModDate != null && hash != null && hits != null;
    }

    /**
     * This function attempts to fill out any fields that are empty upon being
     * called. The data is generated using the physical file specified in the
     * path.
     * @throws java.io.IOException
     */
    public void fillFromFile() throws IOException {
        File f = new File(path);
        if(!f.exists()){
            throw new FileNotFoundException();
        }
        if (name == null) {
            this.fillName(f);
        }
        if (lastModDate == null) {
            this.fillLastModDate(f);
        }
        if (hash == null) {
            this.fillHash();
        }
        if (hits == null) {
            this.fillHits();
        }
    }

    /**
     * Fills the name field whether it is empty or not. The data is generated
     * using the physical file specified in the path.
     */
    public void fillName() {
        fillName(new File(path));
    }

    /**
     * Fills the name field whether it is empty or not. The data is generated
     * using the physical file specified in the path.
     *
     * @param file
     */
    private void fillName(File file) {
        setName(file.getName());
    }

    /**
     * Fills the lastModDate field whether it is empty or not. The data is
     * generated using the physical file specified in the path.
     */
    public void fillLastModDate() {
        fillLastModDate(new File(path));
    }

    /**
     * Fills the lastModDate field whether it is empty or not. The data is
     * generated using the physical file specified in the path.
     *
     * @param file
     */
    private void fillLastModDate(File file) {
        setLastModDate(new Date(file.lastModified()));
    }

    /**
     * Fills the hash field whether it is empty or not. The data is generated
     * using the physical file specified in the path.
     *
     * @throws java.io.IOException
     */
    public void fillHash() throws IOException {
        setHash(hashFile(path));
    }

    /**
     * Sets the hits field to its default value of 0.
     */
    public void fillHits() {
        setHits(0);
    }

    /**
     * This function attempts to fill out any fields that are empty upon being
     * called. The data is pulled from the database.
     *
     * @param db
     * @throws java.sql.SQLException
     * @throws
     * utd.team6.workforceresearchguide.sqlite.DatabaseFileDoesNotExistException
     */
    public void fillFromDatabase(DatabaseController db) throws SQLException, DatabaseFileDoesNotExistException {
        copy(db.getDocumentData(path));
    }

    public void copy(DocumentData data) {
        this.path = data.path;
        this.name = data.name;
        this.lastModDate = data.lastModDate;
        this.hits = data.hits;
        this.hash = data.hash;
    }

    /**
     *
     * @param filePath
     * @return The md5 hash of the specified file in the form of a String.
     * @throws IOException
     */
    private static String hashFile(String filePath) throws IOException {
        String md5;
        try (InputStream is = Files.newInputStream(Paths.get(filePath))) {
            md5 = DigestUtils.md5Hex(is);
        }
        return md5;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLastModDate() {
        return lastModDate;
    }

    public void setLastModDate(Date lastModDate) {
        this.lastModDate = lastModDate;
    }

    public int getHits() {
        return hits;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

}
