/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utd.team6.workforceresearchguide.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import utd.team6.workforceresearchguide.main.ApplicationController;
import utd.team6.workforceresearchguide.main.DocumentData;
import utd.team6.workforceresearchguide.sqlite.DatabaseFileDoesNotExistException;
import utd.team6.workforceresearchguide.sqlite.DatabaseTagDoesNotExistException;

/**
 *
 * @author Michael
 */
public class DocumentDetailsPanel extends javax.swing.JPanel {

    final DocumentData data;
    final ApplicationController app;
    DefaultListModel tagListModel;
    DefaultListModel suggestedTagListModel;

    /**
     * Creates new form DocumentDetailsPanel
     *
     * @param d
     * @param a
     * @param fillFromDB
     */
    public DocumentDetailsPanel(DocumentData d, ApplicationController a, boolean fillFromDB) {
        initComponents();
        this.data = d;
        this.app = a;
        if (fillFromDB) {
            try {
                d.fillFromDatabase(app);
            } catch (DatabaseFileDoesNotExistException ex) {
                Logger.getLogger(DocumentDetailsPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                data.fillFromFile();
            } catch (IOException ex) {
                Logger.getLogger(DocumentDetailsPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            tabPane.setEnabledAt(1, false);
            tabPane.setEnabledAt(2, false);
        }
        if (data != null) {
            documentNameLabel.setText(data.getName());
            documentPathArea.setText(data.getPath());
            lastModDateLabel.setText(data.getLastModDate().toString());
            Date date = data.getDateAdded();
            if (date != null) {
                dateAddedLabel.setText(date.toString());
            } else {
                dateAddedLabel.setText("");
            }
            hitsLabel.setText(data.getHits() + "");
            hashLabel.setText(data.getHash());
        }

        tabPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (tabPane.getSelectedIndex() == 1) {
                    //The tabs tab is selected
                    //The tabs need to be loaded
                    ArrayList<String> tags = app.getDocumentTags(data.getPath());
                    tagListModel.removeAllElements();
                    for (String tag : tags) {
                        tagListModel.addElement(tag);
                    }
                } else if (tabPane.getSelectedIndex() == 2) {
                    //The suggested tabs tab is selected
                    //The suggested tabs need to be loaded
                    ArrayList<String> sugTags = app.getSuggestedDocumentTags(data.getPath(), 0);
                    ArrayList<String> existingTags = app.getDocumentTags(data.getPath());
                    suggestedTagListModel.removeAllElements();
                    for (String tag : sugTags) {
                        if (!existingTags.contains(tag)) {
                            suggestedTagListModel.addElement(tag);
                        }
                    }
                }
            }
        });

        tagListModel = new DefaultListModel();
        tagList.setModel(tagListModel);

        suggestedTagListModel = new DefaultListModel();
        suggestedTagList.setModel(suggestedTagListModel);
    }

    /**
     * Adds the selected tags to the document represented by this panel.
     */
    public void addSelectedSuggestedTags() {
        //Get the selected suggestion tags
        int numAdded = 0;
        for (int index : suggestedTagList.getSelectedIndices()) {
            try {
                String tag = (String) suggestedTagListModel.get(index - numAdded);
                numAdded++;
                tagListModel.add(0, tag);
                //Add the actual tag to the document
                app.addDocumentTag(data.getPath(), tag);
            } catch (DatabaseTagDoesNotExistException | DatabaseFileDoesNotExistException ex) {
                Logger.getLogger(DocumentDetailsPanel.class.getName()).log(Level.SEVERE, null, ex);
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

        tabPane = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        documentNameLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        documentPathArea = new javax.swing.JTextArea();
        lastModDateLabel = new javax.swing.JLabel();
        dateAddedLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        hitsLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        hashLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tagList = new javax.swing.JList<>();
        removeTags = new javax.swing.JButton();
        addTag = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        suggestedTagList = new javax.swing.JList<>();
        sAddTags = new javax.swing.JButton();

        documentNameLabel.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        documentNameLabel.setText("Document Name");

        jLabel3.setText("Last Modified Date:");

        jLabel4.setText("Date Added to System:");

        documentPathArea.setEditable(false);
        documentPathArea.setColumns(20);
        documentPathArea.setLineWrap(true);
        documentPathArea.setRows(5);
        jScrollPane1.setViewportView(documentPathArea);

        lastModDateLabel.setText("jLabel1");

        dateAddedLabel.setText("jLabel2");

        jLabel1.setText("Hits:");

        hitsLabel.setText("jLabel2");

        jLabel2.setText("Hash:");

        hashLabel.setText("jLabel5");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(documentNameLabel)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lastModDateLabel))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(dateAddedLabel))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(hitsLabel))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(hashLabel)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(documentNameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(lastModDateLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(dateAddedLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(hitsLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(hashLabel))
                .addContainerGap(183, Short.MAX_VALUE))
        );

        tabPane.addTab("Details", jPanel1);

        jScrollPane3.setViewportView(tagList);

        removeTags.setText("Remove Selected Tags");
        removeTags.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeTagsActionPerformed(evt);
            }
        });

        addTag.setText("Add Tag");
        addTag.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTagActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(removeTags, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(addTag, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(removeTags)
                    .addComponent(addTag))
                .addContainerGap())
        );

        tabPane.addTab("Tags", jPanel2);

        jScrollPane2.setViewportView(suggestedTagList);

        sAddTags.setText("Add Selected Tags");
        sAddTags.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sAddTagsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sAddTags, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(sAddTags)
                .addContainerGap())
        );

        tabPane.addTab("Suggested Tags", jPanel3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabPane)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addTagActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTagActionPerformed
        AddTagDialog addDialog = new AddTagDialog(data.getPath(), app);
        addDialog.setLocationRelativeTo(this);
        addDialog.showDialog();
        //Add any added tags to the list
        if (addDialog.getCloseState() == AddTagDialog.TAGS_ADDED) {
            for (String addedTag : addDialog.getAddedTags()) {
                tagListModel.add(0, addedTag);
            }
        }
    }//GEN-LAST:event_addTagActionPerformed

    private void removeTagsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeTagsActionPerformed
        //Get the selected tags
        int numRemoved = 0;
        for (int index : tagList.getSelectedIndices()) {
            try {
                String tag = (String) tagListModel.get(index - numRemoved);
                app.removeDocumentTag(data.getPath(), tag);
                tagListModel.removeElementAt(index);
                numRemoved++;
            } catch (DatabaseTagDoesNotExistException | DatabaseFileDoesNotExistException ex) {
                Logger.getLogger(DocumentDetailsPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }//GEN-LAST:event_removeTagsActionPerformed

    private void sAddTagsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sAddTagsActionPerformed
        addSelectedSuggestedTags();
    }//GEN-LAST:event_sAddTagsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addTag;
    private javax.swing.JLabel dateAddedLabel;
    private javax.swing.JLabel documentNameLabel;
    private javax.swing.JTextArea documentPathArea;
    private javax.swing.JLabel hashLabel;
    private javax.swing.JLabel hitsLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lastModDateLabel;
    private javax.swing.JButton removeTags;
    private javax.swing.JButton sAddTags;
    private javax.swing.JList<String> suggestedTagList;
    private javax.swing.JTabbedPane tabPane;
    private javax.swing.JList<String> tagList;
    // End of variables declaration//GEN-END:variables
}
