package utd.team6.workforceresearchguide.gui;

import utd.team6.workforceresearchguide.main.ApplicationController;
import utd.team6.workforceresearchguide.main.DocumentData;

/**
 * This is a factory class for creating DocumentDetails panels and dialogs. This
 * is used so caching can be performed easily.
 *
 * @author Michael
 */
public class DocumentInfoDialogFactory {

    ApplicationController app;

    /**
     * Creates a new DocumentInfoDialogFactory.
     * @param app 
     */
    public DocumentInfoDialogFactory(ApplicationController app) {
        this.app = app;
    }

    /**
     * 
     * @param docPath
     * @param fillFromDB
     * @return
     */
    public DocumentDetailsPanel getDetailsPanel(String docPath, boolean fillFromDB) {
        return this.getDetailsPanel(new DocumentData(docPath), fillFromDB);
    }

    /**
     * 
     * @param data
     * @param fillFromDB
     * @return 
     */
    public DocumentDetailsPanel getDetailsPanel(DocumentData data, boolean fillFromDB) {
        return new DocumentDetailsPanel(data, app, fillFromDB);
    }

    /**
     * 
     * @param docPath
     * @param fillFromDB
     * @return 
     */
    public DocumentDetailsDialog getDetailsDialog(String docPath, boolean fillFromDB) {
        return new DocumentDetailsDialog(getDetailsPanel(docPath, fillFromDB));
    }

    /**
     * 
     * @param data
     * @param fillFromDB
     * @return 
     */
    public DocumentDetailsDialog getDetailsDialog(DocumentData data, boolean fillFromDB) {
        return new DocumentDetailsDialog(getDetailsPanel(data, fillFromDB));
    }
}
