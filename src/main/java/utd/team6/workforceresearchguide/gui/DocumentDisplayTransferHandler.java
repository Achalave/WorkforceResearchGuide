package utd.team6.workforceresearchguide.gui;

import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import javax.swing.JComponent;
import javax.swing.TransferHandler;



//@author Michael Haertling

public class DocumentDisplayTransferHandler extends TransferHandler{
    
    @Override
    public int getSourceActions(JComponent c){
        return COPY;
    }
    
    @Override
    public Transferable createTransferable(JComponent c){
        System.out.println("CREATING TRANSFERABLE: "+((DocumentDisplay)c).getDocumentData().getPath());
        return new StringSelection(((DocumentDisplay)c).getDocumentData().getPath());
    }
    
}
