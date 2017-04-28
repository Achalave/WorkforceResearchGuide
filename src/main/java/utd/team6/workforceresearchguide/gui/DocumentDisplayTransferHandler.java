package utd.team6.workforceresearchguide.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
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
        return new Transferable(){
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
    }
    
}
