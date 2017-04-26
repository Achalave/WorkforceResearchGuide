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
    
    public ArrayList<String> getDocumentTags(String docPath) throws ConnectionNotStartedException;
    public ArrayList<String> getSuggestedDocumentTags(String docPath,int numTags) throws ConnectionNotStartedException;
    public void addTag(String tag);
    public void removeTag(String tag);
    public void addDocumentTag(String docPath,String tag) throws DatabaseTagDoesNotExistException, DatabaseFileDoesNotExistException;
    public void removeDocumentTag(String docPath, String tag) throws DatabaseTagDoesNotExistException, DatabaseFileDoesNotExistException;
    
    public ArrayList<String> getTagList();
    
}
