/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utd.team6.workforceresearchguide.gui.repscan;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import utd.team6.workforceresearchguide.main.FileSyncManager;
import utd.team6.workforceresearchguide.main.issues.AddedFileIssue;
import utd.team6.workforceresearchguide.main.issues.FileSyncIssue;
import utd.team6.workforceresearchguide.main.issues.MissingFileIssue;
import utd.team6.workforceresearchguide.main.issues.MovedFileIssue;
import utd.team6.workforceresearchguide.main.issues.OutdatedFileIssue;
import utd.team6.workforceresearchguide.sqlite.ConnectionNotStartedException;
import utd.team6.workforceresearchguide.sqlite.DatabaseFileDoesNotExistException;

/**
 *
 * @author Michael
 */
public class RepositoryScanDialog extends javax.swing.JFrame {

    private static final String ANIMATION_PANEL = "scan";
    private static final String ISSUES_PANEL = "issues";

    private final CardLayout cards;
    private final RepositoryScanWaitPanel waitScreen;
    private final RepositoryScanIssuesPanel issueScreen;

    private FileSyncManager sync;

    boolean scanningComplete = false;

    Timer animationTimer;

    /**
     * Creates new form RepositoryScanDialog
     */
    public RepositoryScanDialog() {
        initComponents();
        cards = new CardLayout();
        mainPanel.setLayout(cards);

        waitScreen = new RepositoryScanWaitPanel(new ActionListener() {
            //Stop the scan process
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelFromWait();
            }
        });
        issueScreen = new RepositoryScanIssuesPanel(new ActionListener() {
            //Begin the issue resolution
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        }, new ActionListener() {
            //Cancel the set of issues
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        }) {
        };

        mainPanel.add(ANIMATION_PANEL, waitScreen);
        mainPanel.add(ISSUES_PANEL, issueScreen);

        animationTimer = new Timer(RepositoryScanWaitPanel.SCAN_ANIMATION_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                waitScreen.updateAnimation(sync.getWaitMessage());
            }
        });
    }

    /**
     * Creates a new RepositoryScanDialog object.
     *
     * @param fsm
     */
    public RepositoryScanDialog(FileSyncManager fsm) {
        this();
        sync = fsm;
    }

    /**
     * Displays this dialog to the user.
     *
     * @throws ConnectionNotStartedException
     */
    public void showDialog() throws ConnectionNotStartedException {
        this.setVisible(true);
        startSync();
    }

    public void startAnimation() {
        waitScreen.resetAnimation(sync.getWaitMessage());
        animationTimer.start();
    }

    public void stopAnimation() {
        animationTimer.stop();
    }

    /**
     * Begins the syncing process starting with the initial examination of the
     * repository.
     *
     */
    public void startSync() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    startAnimation();
                    FileSyncIssue[] issues = sync.examineDifferences();
                    System.out.println(Arrays.toString(issues));
                    scanningComplete = true;
                    processIssues(issues);
                    stopAnimation();
                    processIssues(issues);
                    showIssuePanel();

                } catch (SQLException | DatabaseFileDoesNotExistException | IOException | ParseException | IssueTypeNotSupportedException | ConnectionNotStartedException ex) {
                    Logger.getLogger(RepositoryScanDialog.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(rootPane, "An exception was encountered. Please see the logs for more information.");
                    close();
                }
            }
        };
        thread.start();
    }

    /**
     * Processes the provided issues and fills out the issue panel.
     *
     * @param issues
     * @throws
     * utd.team6.workforceresearchguide.gui.repscan.IssueTypeNotSupportedException
     */
    public void processIssues(FileSyncIssue[] issues) throws IssueTypeNotSupportedException {
        for (FileSyncIssue iss : issues) {
            if (iss instanceof AddedFileIssue) {
                issueScreen.importAddedFileIssue((AddedFileIssue) iss);
            } else if (iss instanceof MovedFileIssue) {

            } else if (iss instanceof MissingFileIssue) {

            } else if (iss instanceof OutdatedFileIssue) {

            } else {
                throw new IssueTypeNotSupportedException();
            }
        }

        //Send each category into the issue panel
    }

    /**
     * Presents the issue panel.
     */
    public void showIssuePanel() {
        issueScreen.finalizeView();
        cards.show(mainPanel, ISSUES_PANEL);
    }

    /**
     * Presents the animation panel.
     */
    public void showAnimationPanel() {
        cards.show(mainPanel, ANIMATION_PANEL);
    }

    /**
     * Cancels the process from the waiting screen.
     */
    public void cancelFromWait() {
        if (!scanningComplete) {
            //The scanning of the repository is in progress

        } else {
            //The resolving of issues is in progress

        }
    }

    /**
     * Cancels the resolution process from the issue screen.
     */
    public void cancelResolution() {
        //There is no onging process, just close this GUI
        close();
    }

    /**
     * Closes this GUI.
     */
    public void close() {
        this.setVisible(false);
    }

    /**
     * Begins the process of resolving the set of issues.
     */
    public void beginResolution() {
        try {
            sync.startResolutionProcess(1);
        } catch (IOException | SQLException | DatabaseFileDoesNotExistException ex) {
            Logger.getLogger(RepositoryScanDialog.class.getName()).log(Level.SEVERE, null, ex);
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

        mainPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 428, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 456, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
            java.util.logging.Logger.getLogger(RepositoryScanDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(RepositoryScanDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(RepositoryScanDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(RepositoryScanDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new RepositoryScanDialog().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel mainPanel;
    // End of variables declaration//GEN-END:variables

}