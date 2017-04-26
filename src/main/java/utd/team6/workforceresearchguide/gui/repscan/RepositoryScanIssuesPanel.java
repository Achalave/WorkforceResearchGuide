/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utd.team6.workforceresearchguide.gui.repscan;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import utd.team6.workforceresearchguide.main.issues.AddedFileIssue;
import utd.team6.workforceresearchguide.main.issues.MissingFileIssue;
import utd.team6.workforceresearchguide.main.issues.MovedFileIssue;
import utd.team6.workforceresearchguide.main.issues.OutdatedFileIssue;

/**
 *
 * @author Michael
 */
public class RepositoryScanIssuesPanel extends javax.swing.JPanel {

    ActionListener resolve;
    ActionListener cancel;

    boolean addedFiles, movedFiles, outdatedFiles, removedFiles = false;

    /**
     * Creates new form RepositoryScanIssuesPanel
     *
     * @param resolve
     * @param cancel
     */
    public RepositoryScanIssuesPanel(ActionListener resolve, ActionListener cancel) {
        initComponents();
        this.resolve = resolve;
        this.cancel = cancel;

        setupAddedFilesPanel();
        setupMissingFilesPanel();
        setupOutdatedFilesPanel();
        setupMovedFilesPanel();
    }

    /**
     * Performs the setup procedure for the specified panel.
     */
    private void setupAddedFilesPanel() {

        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        labelPanel.add(new JLabel("Selected files will be added to the system."));
        addedFilesPanel.add(labelPanel);

        JPanel selectPanel = new JPanel();
        selectPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        final JCheckBox jcb = new JCheckBox();
        jcb.setText("Select All");
        jcb.setSelected(true);

        selectPanel.add(jcb);

        jcb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean selected = jcb.isSelected();
                for (Component c : addedFilesPanel.getComponents()) {
                    if (c instanceof AddedFileIssuePanel) {
                        ((AddedFileIssuePanel) c).setSelected(selected);
                    }
                }
            }
        });

        addedFilesPanel.add(selectPanel);
    }

    /**
     * Performs the setup procedure for the specified panel.
     */
    private void setupMissingFilesPanel() {
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        labelPanel.add(new JLabel("Selected files will be removed from the system."));
        removedFilesPanel.add(labelPanel);

        JPanel selectPanel = new JPanel();
        selectPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        final JCheckBox jcb = new JCheckBox();
        jcb.setText("Select All");
        jcb.setSelected(true);

        selectPanel.add(jcb);

        jcb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean selected = jcb.isSelected();
                for (Component c : removedFilesPanel.getComponents()) {
                    if (c instanceof AddedFileIssuePanel) {
                        ((AddedFileIssuePanel) c).setSelected(selected);
                    }
                }
            }
        });

        removedFilesPanel.add(selectPanel);
    }

    /**
     * Performs the setup procedure for the specified panel.
     */
    private void setupOutdatedFilesPanel() {
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        labelPanel.add(new JLabel("Selected files will be re-indexed."));
        outdatedFilesPanel.add(labelPanel);

        JPanel selectPanel = new JPanel();
        selectPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        final JCheckBox jcb = new JCheckBox();
        jcb.setText("Select All");
        jcb.setSelected(true);

        selectPanel.add(jcb);

        jcb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean selected = jcb.isSelected();
                for (Component c : outdatedFilesPanel.getComponents()) {
                    if (c instanceof AddedFileIssuePanel) {
                        ((AddedFileIssuePanel) c).setSelected(selected);
                    }
                }
            }
        });

        outdatedFilesPanel.add(selectPanel);
    }

    /**
     * Performs the setup procedure for the specified panel.
     */
    private void setupMovedFilesPanel() {

    }

    public void finalizeView() {
        tabPane.setEnabledAt(0, addedFiles);
        tabPane.setEnabledAt(1, removedFiles);
        tabPane.setEnabledAt(2, outdatedFiles);
        tabPane.setEnabledAt(3, movedFiles);
    }

    /**
     * Adds an AddedFileIssue to the GUI
     *
     * @param issue
     */
    public void importAddedFileIssue(AddedFileIssue issue) {
        addedFiles = true;
        AddedFileIssuePanel pan = new AddedFileIssuePanel(issue);
        addedFilesPanel.add(pan);
    }

    /**
     * Adds an MovedFileIssue to the GUI
     *
     * @param issue
     */
    public void importMovedFileIssue(MovedFileIssue issue) {
        movedFiles = true;
    }

    /**
     * Adds an MissingFileIssue to the GUI
     *
     * @param issue
     */
    public void importMissingFileIssue(MissingFileIssue issue) {
        removedFiles = true;
    }

    /**
     * Adds an OutdatedFileIssue to the GUI
     *
     * @param issue
     */
    public void importOutdatedFileIssue(OutdatedFileIssue issue) {
        outdatedFiles = true;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabPane = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        addedFilesPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        removedFilesPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        outdatedFilesPanel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        movedFilesPanel = new javax.swing.JPanel();
        resolveButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        addedFilesPanel.setLayout(new javax.swing.BoxLayout(addedFilesPanel, javax.swing.BoxLayout.Y_AXIS));
        jScrollPane1.setViewportView(addedFilesPanel);

        tabPane.addTab("Added Files", jScrollPane1);

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        removedFilesPanel.setLayout(new javax.swing.BoxLayout(removedFilesPanel, javax.swing.BoxLayout.Y_AXIS));
        jScrollPane2.setViewportView(removedFilesPanel);

        tabPane.addTab("Removed Files", jScrollPane2);

        jScrollPane3.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        outdatedFilesPanel.setLayout(new javax.swing.BoxLayout(outdatedFilesPanel, javax.swing.BoxLayout.Y_AXIS));
        jScrollPane3.setViewportView(outdatedFilesPanel);

        tabPane.addTab("Outdated Files", jScrollPane3);

        jScrollPane4.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        movedFilesPanel.setLayout(new javax.swing.BoxLayout(movedFilesPanel, javax.swing.BoxLayout.Y_AXIS));
        jScrollPane4.setViewportView(movedFilesPanel);

        tabPane.addTab("Moved Files", jScrollPane4);

        resolveButton.setText("Resolve");
        resolveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resolveButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.setPreferredSize(new java.awt.Dimension(71, 23));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabPane)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(resolveButton, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(cancelButton, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(tabPane, javax.swing.GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(resolveButton)
                    .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void resolveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resolveButtonActionPerformed
        resolve.actionPerformed(evt);
    }//GEN-LAST:event_resolveButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        cancel.actionPerformed(evt);
    }//GEN-LAST:event_cancelButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel addedFilesPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JPanel movedFilesPanel;
    private javax.swing.JPanel outdatedFilesPanel;
    private javax.swing.JPanel removedFilesPanel;
    private javax.swing.JButton resolveButton;
    private javax.swing.JTabbedPane tabPane;
    // End of variables declaration//GEN-END:variables
}
