package utd.team6.workforceresearchguide.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.TransferHandler;

//@author Michael Haertling
/**
 * A TransferHAndler for the group panel drop site.
 *
 * @author Michael
 */
public class GroupPanelTransferHandler extends TransferHandler {

    DocumentDroppedAction drop;

    /**
     * Creates a new GroupPanelTransferHandler object.
     *
     * @param drop
     */
    public GroupPanelTransferHandler(DocumentDroppedAction drop) {
        this.drop = drop;
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        if (!support.isDrop()) {
            return false;
        }

        JPanel panel = (JPanel) support.getComponent();

        Transferable t = support.getTransferable();

        try {
            String docPath = (String) t.getTransferData(DataFlavor.stringFlavor);
            drop.drop(docPath);
            return true;
        } catch (UnsupportedFlavorException | IOException ex) {
            Logger.getLogger(GroupPanelTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

}
