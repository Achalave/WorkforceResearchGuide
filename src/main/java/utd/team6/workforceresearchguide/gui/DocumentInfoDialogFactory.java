package utd.team6.workforceresearchguide.gui;

import utd.team6.workforceresearchguide.main.ApplicationController;
import utd.team6.workforceresearchguide.main.DocumentData;



//@author Michael Haertling

public class DocumentInfoDialogFactory {
    
    ApplicationController app;
    
    public DocumentInfoDialogFactory(ApplicationController app){
        this.app  = app;
    }
    
    public DocumentDetailsPanel getDetailsPanel(String docPath, boolean fillFromDB){
        return this.getDetailsPanel(new DocumentData(docPath),fillFromDB);
    }
    
    public DocumentDetailsPanel getDetailsPanel(DocumentData data, boolean fillFromDB){
        return new DocumentDetailsPanel(data, app,fillFromDB);
    }
    
    public DocumentDetailsDialog getDetailsDialog(String docPath, boolean fillFromDB){
        return new DocumentDetailsDialog(getDetailsPanel(docPath,fillFromDB));
    }
    
    public DocumentDetailsDialog getDetailsDialog(DocumentData data, boolean fillFromDB){
        return new DocumentDetailsDialog(getDetailsPanel(data,fillFromDB));
    }
}
