/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utd.team6.workforceresearchguide.gui.repscan;

import utd.team6.workforceresearchguide.gui.DocumentDetailsDialog;
import utd.team6.workforceresearchguide.gui.DocumentInfoDialogFactory;
import utd.team6.workforceresearchguide.main.issues.AddedFileIssue;
import utd.team6.workforceresearchguide.main.issues.SingleFileIssue;

/**
 *
 * @author Michael
 */
public class SingleFileIssuePanel extends javax.swing.JPanel {

    private final SingleFileIssue issue;
    private final DocumentInfoDialogFactory infoFactory;
    boolean fillFromDB;
    
    DocumentDetailsDialog docDetails;
    
    /**
     * Creates new form SingleFileIssue
     * @param infoFactory
     * @param iss
     * @param fillFromDB
     */
    public SingleFileIssuePanel(DocumentInfoDialogFactory infoFactory, SingleFileIssue iss, boolean fillFromDB) {
        initComponents();
        issue = iss;
        documentCheckbox.setText(issue.getDocumentData().getPath());
        this.infoFactory = infoFactory;
        this.fillFromDB = fillFromDB;
    }

    /**
     * Sets the internal checkbox selection state.
     * @param selected 
     */
    public void setSelected(boolean selected){
        documentCheckbox.setSelected(selected);
    }
    
    /**
     * 
     * @return The internal checkbox selection state.
     */
    public boolean isSelected(){
        return documentCheckbox.isSelected();
    }
    
    /**
     * 
     * @return The issue this panel represents.
     */
    public SingleFileIssue getIssue(){
        return issue;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        infoButton = new javax.swing.JButton();
        documentCheckbox = new javax.swing.JCheckBox();

        infoButton.setText("Info");
        infoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                infoButtonActionPerformed(evt);
            }
        });

        documentCheckbox.setSelected(true);
        documentCheckbox.setText("Document Path");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(infoButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(documentCheckbox, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(documentCheckbox)
                .addComponent(infoButton))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void infoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_infoButtonActionPerformed
        if(docDetails == null){
            docDetails = infoFactory.getDetailsDialog(issue.getDocumentData(),fillFromDB);
        }
        docDetails.setVisible(true);
    }//GEN-LAST:event_infoButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox documentCheckbox;
    private javax.swing.JButton infoButton;
    // End of variables declaration//GEN-END:variables
}
