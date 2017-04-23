/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utd.team6.workforceresearchguide.gui;

import java.awt.Desktop;
import java.awt.FontMetrics;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import utd.team6.workforceresearchguide.main.DocumentData;

/**
 * Displays basic information over a document. Allows documents to be opened.
 * @author Michael
 */
public class DocumentDisplay extends javax.swing.JPanel {

    private static final String EXTENDED_PATH_PREFIX = "...";

    DocumentData data;

    /**
     * Creates new form DocumentDisplay
     *
     * @param data
     */
    public DocumentDisplay(DocumentData data) {
        this();
        this.data = data;
    }

    /**
     * Creates a new form DocumentDisplay without any data to display.
     */
    public DocumentDisplay() {
        initComponents();
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

        pathLabel = new javax.swing.JLabel();
        openButton = new javax.swing.JButton();
        openFolderButton = new javax.swing.JButton();

        pathLabel.setText("Document Path");
        pathLabel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                pathLabelComponentResized(evt);
            }
        });
        pathLabel.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                pathLabelPropertyChange(evt);
            }
        });

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pathLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(openFolderButton)
                .addGap(10, 10, 10)
                .addComponent(openButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pathLabel)
                    .addComponent(openButton)
                    .addComponent(openFolderButton))
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

    private void pathLabelComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_pathLabelComponentResized
        resizePathLabel();
    }//GEN-LAST:event_pathLabelComponentResized

    private void pathLabelPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_pathLabelPropertyChange
        resizePathLabel();
    }//GEN-LAST:event_pathLabelPropertyChange

    private void openFolderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openFolderButtonActionPerformed
        try {
            Runtime.getRuntime().exec("explorer.exe /select,"+data.getPath());
        } catch (IOException ex) {
            Logger.getLogger(DocumentDisplay.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_openFolderButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton openButton;
    private javax.swing.JButton openFolderButton;
    private javax.swing.JLabel pathLabel;
    // End of variables declaration//GEN-END:variables
}
