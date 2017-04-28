/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utd.team6.workforceresearchguide.gui;

import java.awt.Desktop;
import java.awt.FontMetrics;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.TransferHandler;
import utd.team6.workforceresearchguide.main.DocumentData;
import utd.team6.workforceresearchguide.main.SearchResult;

/**
 * Displays basic information over a document. Allows documents to be opened.
 *
 * @author Michael
 */
public class DocumentDisplay extends javax.swing.JPanel implements Comparable<DocumentDisplay>, Transferable, MouseMotionListener{
    private static final DataFlavor[] flavors = {DataFlavor.stringFlavor};
    private static final String EXTENDED_PATH_PREFIX = "...";

    DocumentData data;
    SearchResult result;
    
    HashSet<String> tags;
    
    boolean infoListener = false;
    

    /**
     * Creates new form DocumentDisplay
     *
     * @param data
     * @param result
     */
    public DocumentDisplay(DocumentData data, SearchResult result) {
        this();
        this.data = data;
        this.result = result;
        data.fillName();
        pathLabel.setText(data.getName());
    }

    /**
     * Creates new form DocumentDisplay
     *
     * @param result
     */
    public DocumentDisplay(SearchResult result) {
        this();
        this.result = result;
        data = new DocumentData(result.getFilePath());
        data.fillName();
        pathLabel.setText(data.getName());
    }

    /**
     * Creates a new form DocumentDisplay without any data to display.
     */
    public DocumentDisplay() {
        initComponents();
    }

    /**
     * 
     * @param tags 
     */
    public void setTags(HashSet<String> tags){
        this.tags = tags;
    }
    
    /**
     * 
     * @return True if the tags have been filled in this object.
     */
    public boolean tagsFilled(){
        return tags != null;
    }
    
    /**
     * 
     * @param tag
     * @return True if the specified tag is associated with this document.
     */
    public boolean releventTag(String tag){
        return tags.contains(tag);
    }
    
    /**
     * Adds an ActionListener to the info button of this panel.
     * @param act 
     */
    public void setInfoListener(ActionListener act){
        infoListener = true;
        infoButton.addActionListener(act);
    }
    
    /**
     * 
     * @return True if an info listener has been added to the info button.
     */
    public boolean hasInfoListener(){
        return infoListener;
    }
    
    /**
     *
     * @return
     */
    public SearchResult getSearchResult() {
        return result;
    }

    /**
     *
     * @return
     */
    public DocumentData getDocumentData() {
        return data;
    }

    /**
     * Adjusted the displayed text in the pathLabel such that the latter end of
     * the path string is always shown.
     */
    public void resizePathLabel() {
        if (data != null) {
            FontMetrics metrics = pathLabel.getFontMetrics(pathLabel.getFont());
            int width = metrics.stringWidth(TOOL_TIP_TEXT_KEY);
            if (pathLabel.getWidth() > 0 && width > pathLabel.getWidth()) {
                System.out.println(width + "\t" + pathLabel.getWidth());
                int prefixWidth = metrics.stringWidth(EXTENDED_PATH_PREFIX);
                String path = data.getPath();
                while (width > pathLabel.getWidth() && path.length() > 1) {
                    path = path.substring(1);
                    width = metrics.stringWidth(path) + prefixWidth;
                }
                pathLabel.setText(EXTENDED_PATH_PREFIX + path);
            } else {
                pathLabel.setText(data.getPath());
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        openButton = new javax.swing.JButton();
        openFolderButton = new javax.swing.JButton();
        pathLabel = new javax.swing.JTextField();
        infoButton = new javax.swing.JButton();

        openButton.setText("Open");
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });

        openFolderButton.setText("Open Folder");
        openFolderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openFolderButtonActionPerformed(evt);
            }
        });

        pathLabel.setEditable(false);

        infoButton.setText("Info");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pathLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(openFolderButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(openButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(infoButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(openButton)
                    .addComponent(openFolderButton)
                    .addComponent(pathLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(infoButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed
        try {
            Desktop.getDesktop().open(new File(data.getPath()));
        } catch (IOException ex) {
            Logger.getLogger(DocumentDisplay.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_openButtonActionPerformed

    private void openFolderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openFolderButtonActionPerformed
        try {
            Runtime.getRuntime().exec("explorer.exe /select," + data.getPath());
        } catch (IOException ex) {
            Logger.getLogger(DocumentDisplay.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_openFolderButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton infoButton;
    private javax.swing.JButton openButton;
    private javax.swing.JButton openFolderButton;
    private javax.swing.JTextField pathLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public int compareTo(DocumentDisplay o) {
        if (result == null || o.getSearchResult() == null) {
            return 0;
        } else {
            return result.compareTo(o.getSearchResult());
        }
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return (flavor.equals(DataFlavor.stringFlavor));
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        return data.getPath();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        this.getTransferHandler().exportAsDrag(this, e, TransferHandler.COPY);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }


}
