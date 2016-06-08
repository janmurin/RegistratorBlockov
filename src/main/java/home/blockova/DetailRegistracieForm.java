/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package home.blockova;

import java.awt.Frame;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Janco1
 */
public class DetailRegistracieForm extends javax.swing.JDialog {

    private Database database;
    private int column;
    private Blocek blocek;
    private RegisterLog rl;

    /**
     * Creates new form DetailRegistracieForm
     */
    public DetailRegistracieForm(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    public DetailRegistracieForm(Frame owner, boolean b, Blocek blocek, Database database, int column) {
        this(owner, b);
        System.out.println("DetailRegistracieForm started constructor");
        dkpLabel.setText(blocek.dkp);
        this.column = column;
        this.blocek = blocek;

        System.out.println("datum na blocku: " + blocek.datum);
        datumLabel.setText(blocek.datum.substring(0, 16));
        sumaLabel.setText(Double.toString(blocek.suma));
        this.database = database;
        predajnaLabel.setText(database.getPredajca(blocek.dkp).meno);

        rl = database.getRegisterLog(blocek.id);
        if (column == 1) {
            nazovUctuLabel.setText(rl.login1);
            statusTextArea.setText(rl.status1);
        }
        if (column == 2) {
            nazovUctuLabel.setText(rl.login2);
            statusTextArea.setText(rl.status2);
        }
        if (column == 3) {
            nazovUctuLabel.setText(rl.login3);
            statusTextArea.setText(rl.status3);
        }
        if (blocek.timeMakroGenerated == null) {
            datumRegistracieLabel.setText("nezaregistrovany");
        } else {
            datumRegistracieLabel.setText(blocek.timeMakroGenerated.substring(0, 16));
        }
        setTitle("Detail Registrácie");
        if (statusTextArea.getText().equalsIgnoreCase("WRONG DKP")) {
            editButton.setEnabled(true);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        dkpLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        datumLabel = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        sumaLabel = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        predajnaLabel = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        nazovUctuLabel = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        datumRegistracieLabel = new javax.swing.JLabel();
        zavrietButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        statusTextArea = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Detail Registrácie");
        setResizable(false);

        jLabel1.setText("DKP:");

        dkpLabel.setText("jLabel2");

        jLabel3.setText("Dátum:");

        datumLabel.setText("jLabel2");

        jLabel5.setText("Suma:");

        sumaLabel.setText("jLabel2");

        jLabel7.setText("Predajňa:");

        predajnaLabel.setText("jLabel2");

        jLabel9.setText("Údaje o registrácii");

        jLabel10.setText("Názov účtu:");

        nazovUctuLabel.setText("jLabel2");

        jLabel12.setText("Status:");

        jLabel11.setText("Dátum registrácie:");

        datumRegistracieLabel.setText("jLabel2");

        zavrietButton.setText("Zavrieť");
        zavrietButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zavrietButtonActionPerformed(evt);
            }
        });

        editButton.setText("Edit");
        editButton.setEnabled(false);
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        statusTextArea.setEditable(false);
        statusTextArea.setColumns(20);
        statusTextArea.setLineWrap(true);
        statusTextArea.setRows(5);
        statusTextArea.setWrapStyleWord(true);
        jScrollPane1.setViewportView(statusTextArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel5)
                    .addComponent(jLabel7)
                    .addComponent(jLabel1))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(dkpLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
                    .addComponent(datumLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sumaLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(predajnaLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(170, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(79, 79, 79)
                        .addComponent(zavrietButton)
                        .addGap(18, 18, 18)
                        .addComponent(editButton, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel9))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel11)
                            .addComponent(jLabel12)
                            .addComponent(jLabel10))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(datumRegistracieLabel)
                                    .addComponent(nazovUctuLabel))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dkpLabel)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(datumLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(sumaLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(predajnaLabel))
                .addGap(32, 32, 32)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(nazovUctuLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addGap(82, 82, 82)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(datumRegistracieLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 90, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(zavrietButton)
                            .addComponent(editButton))
                        .addGap(21, 21, 21))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void zavrietButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zavrietButtonActionPerformed
        dispose();
    }//GEN-LAST:event_zavrietButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        EditBlocekForm ebf = new EditBlocekForm(null, true, blocek, database);
        ebf.setVisible(true);
        if (column == 1) {
            database.updateRegisterLogFromDB("login1", rl.login1, "status1", "TODO", (int) blocek.id);
        }
        if (column == 2) {
            database.updateRegisterLogFromDB("login2", rl.login2, "status2", "TODO", (int) blocek.id);
        }
        if (column == 3) {
            database.updateRegisterLogFromDB("login3", rl.login3, "status3", "TODO", (int) blocek.id);
        }
    }//GEN-LAST:event_editButtonActionPerformed

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
            java.util.logging.Logger.getLogger(DetailRegistracieForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DetailRegistracieForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DetailRegistracieForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DetailRegistracieForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                DetailRegistracieForm dialog = new DetailRegistracieForm(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel datumLabel;
    private javax.swing.JLabel datumRegistracieLabel;
    private javax.swing.JLabel dkpLabel;
    private javax.swing.JButton editButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel nazovUctuLabel;
    private javax.swing.JLabel predajnaLabel;
    private javax.swing.JTextArea statusTextArea;
    private javax.swing.JLabel sumaLabel;
    private javax.swing.JButton zavrietButton;
    // End of variables declaration//GEN-END:variables
}