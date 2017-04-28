/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utd.team6.workforceresearchguide.gui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import utd.team6.workforceresearchguide.main.DocumentTagSource;
import utd.team6.workforceresearchguide.sqlite.DatabaseFileDoesNotExistException;
import utd.team6.workforceresearchguide.sqlite.DatabaseTagDoesNotExistException;

/**
 *
 * @author Michael
 */
public class AddTagDialog extends javax.swing.JDialog {

    /**
     * Indicated that the dialog was canceled.
     */
    public static final int CANCELED = 0;

    /**
     * Indicates that the user selected to add tags.
     */
    public static final int TAGS_ADDED = 1;

    DocumentTagSource tagSource;
    HashSet<String> newTags;
    HashSet<String> addedTags;
    HashSet<String> allTags;
    HashSet<String> docTags;
    HashMap<String,JCheckBox> validTags;
    String docPath;

    private int closeState = CANCELED;

    /**
     * Creates new form AddTagDialog
     *
     * @param docPath
     * @param tagSource
     */
    public AddTagDialog(String docPath, DocumentTagSource tagSource) {
        initComponents();

        this.docPath = docPath;

        newTags = new HashSet<>();
        addedTags = new HashSet<>();
        
        this.tagSource = tagSource;

        ArrayList<String> tags = tagSource.getTagList();
        allTags = new HashSet<>(tags);
        
        ArrayList<String> dt = tagSource.getDocumentTags(docPath);
        docTags = new HashSet<>(dt);
        
        validTags = new HashMap<>();
        for(String tag:allTags){
            if(!docTags.contains(tag)){
                JCheckBox box = new JCheckBox(tag);
                validTags.put(tag,box);
                tagListPanel.add(box);
            }
        }
        pack();
         
        newTagTextBox.getDocument().addDocumentListener(new DocumentListener(){
            @Override
            public void insertUpdate(DocumentEvent e) {
                populateTags();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                populateTags();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
            
        });
        
        populateTags();
    }

    public final void populateTags(){
        System.out.println("Populate Tags");
        String constraint = newTagTextBox.getText();
//        tagListPanel.removeAll();
        for (Entry<String,JCheckBox> ent:validTags.entrySet()) {
            if(constraint.isEmpty() || ent.getKey().startsWith(constraint)){
                ent.getValue().setVisible(true);
//                tagListPanel.add(ent.getValue());
            }else{
                ent.getValue().setVisible(false);
            }
        }
//        tagListPanel.revalidate();
//        pack();
    }
    
    /**
     *
     * @return The user action taken that caused this dialog to close.
     */
    public int getCloseState() {
        return closeState;
    }

    /**
     * Displays this dialog.
     */
    public void showDialog() {
        setModal(true);
        this.setVisible(true);
    }

    /**
     *
     * @return A list of all tags selected to be added.
     */
    public HashSet<String> getAddedTags() {
        return addedTags;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addButton = new javax.swing.JButton();
        newTagTextBox = new javax.swing.JTextField();
        createTagButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tagListPanel = new javax.swing.JPanel();

        addButton.setText("Add Selected");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        createTagButton.setText("Create New Tag");
        createTagButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createTagButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        tagListPanel.setLayout(new javax.swing.BoxLayout(tagListPanel, javax.swing.BoxLayout.Y_AXIS));
        jScrollPane2.setViewportView(tagListPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(newTagTextBox, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(createTagButton))
                    .addComponent(cancelButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addComponent(jScrollPane2)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(newTagTextBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(createTagButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(addButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cancelButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void createTagButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createTagButtonActionPerformed
        String tag = newTagTextBox.getText();
        if (!tag.isEmpty()) {
            if(allTags.contains(tag)){
                JOptionPane.showMessageDialog(rootPane, "Tag already exists.");
                return;
            }
            newTagTextBox.setText("");
            JCheckBox box = new JCheckBox(tag, true);
            validTags.put(tag,box);
            newTags.add(tag);
            allTags.add(tag);
            tagListPanel.add(box);
            pack();
            populateTags();
        }
    }//GEN-LAST:event_createTagButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        //Add the new tags
        for (String tag : newTags) {
            System.out.println("Adding Tag: "+tag);
            tagSource.addTag(tag);
        }
        //Apply the selected tags to the document
        for (Component c : tagListPanel.getComponents()) {
            if (c instanceof JCheckBox && ((JCheckBox) c).isSelected()) {
                try {
                    String tag = ((JCheckBox) c).getText();
                    System.out.println("Adding Tag to Document: "+tag);
                    tagSource.addDocumentTag(docPath, tag);
                    addedTags.add(tag);
                } catch (DatabaseTagDoesNotExistException | DatabaseFileDoesNotExistException ex) {
                    Logger.getLogger(AddTagDialog.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        closeState = TAGS_ADDED;
        setVisible(false);
    }//GEN-LAST:event_addButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        closeState = CANCELED;
        setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AddTagDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AddTagDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AddTagDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AddTagDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AddTagDialog(null, null).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton createTagButton;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField newTagTextBox;
    private javax.swing.JPanel tagListPanel;
    // End of variables declaration//GEN-END:variables
}
