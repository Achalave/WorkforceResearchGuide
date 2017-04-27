package utd.team6.workforceresearchguide.gui;

import utd.team6.workforceresearchguide.main.ApplicationController;
import utd.team6.workforceresearchguide.main.DocumentData;



//@author Michael Haertling

public class DocumentInfoDialogFactory {
    
    ApplicationController app;
    
    public DocumentInfoDialogFactory(ApplicationController app){
        
    }
    
    public DocumentDetailsPanel getDetailsPanel(String docPath){
        return this.getDetailsPanel(new DocumentData(docPath));
    }
    
    public DocumentDetailsPanel getDetailsPanel(DocumentData data){
        return new DocumentDetailsPanel(data, app);
    }
    
    public DocumentDetailsDialog getDetailsDialog(String docPath){
        return new DocumentDetailsDialog(getDetailsPanel(docPath));
    }
    
    public DocumentDetailsDialog getDetailsDialog(DocumentData data){
        return new DocumentDetailsDialog(getDetailsPanel(data));
    }
}
