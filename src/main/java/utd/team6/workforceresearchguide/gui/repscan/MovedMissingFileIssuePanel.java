/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utd.team6.workforceresearchguide.gui.repscan;

import java.io.File;
import javax.swing.JFileChooser;
import utd.team6.workforceresearchguide.gui.DocumentDetailsDialog;
import utd.team6.workforceresearchguide.gui.DocumentInfoDialogFactory;
import utd.team6.workforceresearchguide.main.DocumentData;
import utd.team6.workforceresearchguide.main.issues.FileSyncIssue;
import utd.team6.workforceresearchguide.main.issues.MissingFileIssue;
import utd.team6.workforceresearchguide.main.issues.MovedFileIssue;

/**
 *
 * @author Michael
 */
public class MovedMissingFileIssuePanel extends javax.swing.JPanel {

    /**
     * Represents the user's choice to relocate the file.
     */
    public static final int RELOCATE_OPTION = 0;
    /**
     * Represents the user's choice to remove the file.
     */
    public static final int REMOVE_OPTION = 1;
    /**
     * Represents the user's choice to keep the file.
     */
    public static final int KEEP_OPTION = 2;

    FileSyncIssue issue;
    JFileChooser chooser;

    int selection;

    private final String startPath;
    private final DocumentInfoDialogFactory info;

    DocumentDetailsDialog info1;
    DocumentDetailsDialog info2;

    /**
     * Creates new form DoubleFileIssuePanel
     *
     * @param issue
     * @param info
     */
    public MovedMissingFileIssuePanel(MovedFileIssue issue, DocumentInfoDialogFactory info) {
        initComponents();
        this.issue = issue;
        this.oldFilePathLabel.setText(issue.getMissingFile().getPath());
        this.newFilePathLabel.setText(issue.getNewFile().getPath());
        notifyLabel.setVisible(false);

        startPath = issue.getNewFile().getPath();
        this.info = info;

        buttonGroup.add(relocateRadioButton);
        buttonGroup.add(removeRadioButton);
        buttonGroup.add(keepRadioButton);
        buttonGroup.setSelected(relocateRadioButton.getModel(), true);

        selection = RELOCATE_OPTION;
    }

    /**
     * Creates new form DoubleFileIssuePanel
     *
     * @param issue
     * @param info
     */
    public MovedMissingFileIssuePanel(MissingFileIssue issue, DocumentInfoDialogFactory info) {
        initComponents();
        this.issue = issue;
        this.oldFilePathLabel.setText(issue.getDocumentData().getPath());
        this.newFilePathLabel.setText("");
        this.infoButton2.setEnabled(false);
        notifyLabel.setVisible(false);

        startPath = issue.getDocumentData().getPath();
        this.info = info;

        buttonGroup.add(relocateRadioButton);
        buttonGroup.add(removeRadioButton);
        buttonGroup.add(keepRadioButton);
        buttonGroup.setSelected(removeRadioButton.getModel(), true);

        selection = REMOVE_OPTION;
    }

    /**
     *
     * @return The file that was removed.
     */
    public DocumentData getOldFile() {
        return new DocumentData(oldFilePathLabel.getText());
    }

    /**
     *
     * @return The file that should take the place of the replaced file, if any.
     */
    public DocumentData getNewFile() {
        return new DocumentData(newFilePathLabel.getText());
    }

    /**
     *
     * @return The integer corresponding to the radio selection made by the
     * user.
     */
    public int getSelection() {
        return selection;
    }

    /**
     *
     * @return The issue this panel is centered around;
     */
    public FileSyncIssue getIssue() {
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

        buttonGroup = new javax.swing.ButtonGroup();
        infoButton = new javax.swing.JButton();
        infoButton2 = new javax.swing.JButton();
        relocateButton = new javax.swing.JButton();
        notifyLabel = new javax.swing.JLabel();
        relocateRadioButton = new javax.swing.JRadioButton();
        removeRadioButton = new javax.swing.JRadioButton();
        keepRadioButton = new javax.swing.JRadioButton();
        oldFilePathLabel = new javax.swing.JTextField();
        newFilePathLabel = new javax.swing.JTextField();

        infoButton.setText("Info");
        infoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                infoButtonActionPerformed(evt);
            }
        });

        infoButton2.setText("Info");
        infoButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                infoButton2ActionPerformed(evt);
            }
        });

        relocateButton.setText("Set Relocation Path");
        relocateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                relocateButtonActionPerformed(evt);
            }
        });

        notifyLabel.setForeground(java.awt.Color.red);
        notifyLabel.setText("The suggested file will be added to the system.");

        relocateRadioButton.setText("Relocate");
        relocateRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                relocateRadioButtonActionPerformed(evt);
            }
        });

        removeRadioButton.setText("Remove");
        removeRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeRadioButtonActionPerformed(evt);
            }
        });

        keepRadioButton.setText("Keep");
        keepRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keepRadioButtonActionPerformed(evt);
            }
        });

        oldFilePathLabel.setEditable(false);

        newFilePathLabel.setEditable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(notifyLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(relocateRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(keepRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(relocateButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(newFilePathLabel)
                            .addComponent(oldFilePathLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(infoButton, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(infoButton2, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(infoButton)
                    .addComponent(oldFilePathLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(infoButton2)
                    .addComponent(newFilePathLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(notifyLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(relocateButton)
                    .addComponent(relocateRadioButton)
                    .addComponent(removeRadioButton)
                    .addComponent(keepRadioButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void relocateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_relocateButtonActionPerformed
        if (chooser == null) {
            chooser = new JFileChooser();
            chooser.setSelectedFile(new File(startPath));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        }
        int result = chooser.showDialog(this, "Set File");
        if (result == JFileChooser.APPROVE_OPTION && !newFilePathLabel.getText().equals(chooser.getSelectedFile().getAbsolutePath())) {
            newFilePathLabel.setText(chooser.getSelectedFile().getAbsolutePath());
            if (issue instanceof MovedFileIssue) {
                notifyLabel.setVisible(true);
            }
            infoButton2.setEnabled(true);
        }
    }//GEN-LAST:event_relocateButtonActionPerformed

    private void relocateRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_relocateRadioButtonActionPerformed
        selection = RELOCATE_OPTION;
    }//GEN-LAST:event_relocateRadioButtonActionPerformed

    private void removeRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeRadioButtonActionPerformed
        selection = REMOVE_OPTION;
    }//GEN-LAST:event_removeRadioButtonActionPerformed

    private void keepRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keepRadioButtonActionPerformed
        selection = KEEP_OPTION;
    }//GEN-LAST:event_keepRadioButtonActionPerformed

    private void infoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_infoButtonActionPerformed
        if (info1 == null) {
            info1 = info.getDetailsDialog(this.oldFilePathLabel.getText(), true);
        }
        info1.setVisible(true);
    }//GEN-LAST:event_infoButtonActionPerformed

    private void infoButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_infoButton2ActionPerformed
        if (info1 == null) {
            info1 = info.getDetailsDialog(this.newFilePathLabel.getText(), false);
        }
        info1.setVisible(true);
    }//GEN-LAST:event_infoButton2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JButton infoButton;
    private javax.swing.JButton infoButton2;
    private javax.swing.JRadioButton keepRadioButton;
    private javax.swing.JTextField newFilePathLabel;
    private javax.swing.JLabel notifyLabel;
    private javax.swing.JTextField oldFilePathLabel;
    private javax.swing.JButton relocateButton;
    private javax.swing.JRadioButton relocateRadioButton;
    private javax.swing.JRadioButton removeRadioButton;
    // End of variables declaration//GEN-END:variables
}
