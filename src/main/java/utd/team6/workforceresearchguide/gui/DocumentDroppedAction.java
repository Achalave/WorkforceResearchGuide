/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utd.team6.workforceresearchguide.gui;

/**
 * This is used by the GroupPanelTransferHandler to notify the application of a
 * drop event.
 *
 * @author Michael
 */
public interface DocumentDroppedAction {

    /**
     * This is called when a document has been dropped.
     *
     * @param docPath
     */
    public void drop(String docPath);
}
