/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utd.team6.workforceresearchguide.main;

import java.util.ArrayList;
import utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException;
import utd.team6.workforceresearchguide.sqlite.DatabaseFileDoesNotExistException;
import utd.team6.workforceresearchguide.sqlite.DatabaseTagDoesNotExistException;

/**
 *
 * @author Michael
 */
public interface DocumentTagSource {

    /**
     *
     * @param docPath
     * @return A list of the tags associated with a document.
     */
    public ArrayList<String> getDocumentTags(String docPath);

    /**
     *
     * @param docPath
     * @param numTags
     * @return A list of the suggested document tags associated with a document.
     */
    public ArrayList<String> getSuggestedDocumentTags(String docPath, int numTags);

    /**
     * Adds the specified tag to the database.
     *
     * @param tag
     */
    public void addTag(String tag);

    /**
     * Removes the specified tag from the database.
     *
     * @param tag
     */
    public void removeTag(String tag);

    /**
     * Adds the specified tag to the specified document.
     *
     * @param docPath
     * @param tag
     * @throws DatabaseTagDoesNotExistException
     * @throws DatabaseFileDoesNotExistException
     */
    public void addDocumentTag(String docPath, String tag) throws DatabaseTagDoesNotExistException, DatabaseFileDoesNotExistException;

    /**
     * Removes the specified tag from the specified document.
     *
     * @param docPath
     * @param tag
     * @throws DatabaseTagDoesNotExistException
     * @throws DatabaseFileDoesNotExistException
     */
    public void removeDocumentTag(String docPath, String tag) throws DatabaseTagDoesNotExistException, DatabaseFileDoesNotExistException;

    /**
     *
     * @return A complete list of the tags in the system.
     */
    public ArrayList<String> getTagList();

}
