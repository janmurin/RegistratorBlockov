/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package home.blockova;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Janco1
 */
public class MakroFromErrorLog extends javax.swing.JFrame {

    /**
     * Creates new form MakroFromErrorLog
     */
    public MakroFromErrorLog() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        makroErrorLoguButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        makroErrorLoguButton.setText("Generuj Makro z ErrorLogu");
        makroErrorLoguButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                makroErrorLoguButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(70, Short.MAX_VALUE)
                .addComponent(makroErrorLoguButton)
                .addGap(143, 143, 143))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(111, 111, 111)
                .addComponent(makroErrorLoguButton)
                .addContainerGap(164, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void makroErrorLoguButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_makroErrorLoguButtonActionPerformed

        PrintWriter out = null;
        try {
            BufferedReader f = new BufferedReader(new FileReader("C:\\Users\\Janco1\\Documents\\iMacros\\Macros\\errorlog2.txt"));
            List<Blocek> blocky = new ArrayList<Blocek>();
            while (true) {
                StringTokenizer st = null;
                try {
                    String line = f.readLine();
                    if (line == null) {
                        break;
                    }
                    line=line.substring(1, line.length()-1);
                    String[] zlozky=line.split(" ");
                    Blocek novy=new Blocek(zlozky[0], zlozky[1]+" "+zlozky[2], Double.parseDouble(zlozky[3]), 0);
                    blocky.add(novy);
                    System.out.println(novy);
                } catch (Exception exception) {
                    break;
                }
            }
            
            out = new PrintWriter(new BufferedWriter(new FileWriter("C:\\Users\\Janco1\\Documents\\iMacros\\Macros\\autoRegisterMakro.js")));
            StringBuilder dkps = new StringBuilder("[");
            StringBuilder datums = new StringBuilder("[");
            StringBuilder sums = new StringBuilder("[");
            int count = 0;
            for (Blocek b : blocky) {
                if (b.pocet < 3) {
                    count++;
                    dkps.append("\"" + b.dkp + "\",");
                    datums.append("\"" + b.datum.substring(0, 16) + "\",");
                    sums.append("\"" + b.suma + "\",");
                }
            }
            String dkpcka = dkps.toString().substring(0, dkps.length() - 1) + "];";
            String datumy = datums.toString().substring(0, datums.length() - 1) + "];";
            String sumy = sums.toString().substring(0, sums.length() - 1) + "];";
            System.out.println(dkpcka);
            System.out.println(datumy);
            System.out.println(sumy);
            out.println("var dkps=" + dkpcka);
            out.println("var datums=" + datumy);
            out.println("var sums=" + sumy);

            f = new BufferedReader(new FileReader("C:\\Users\\Janco1\\Documents\\iMacros\\Macros\\rawAutoRegisterMakro.txt"));

            while (true) {
                StringTokenizer st = null;
                try {
                    String line = f.readLine();
                    if (line == null) {
                        break;
                    }
                    out.println(line);
                } catch (Exception exception) {
                    break;
                }
            }

            out.close();

            System.out.println("VYTVORENE MAKRO S " + count + " BLOCKAMI PRE FORMULAR");
        } catch (IOException ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
        }

        out.close();
    }//GEN-LAST:event_makroErrorLoguButtonActionPerformed

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
            java.util.logging.Logger.getLogger(MakroFromErrorLog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MakroFromErrorLog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MakroFromErrorLog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MakroFromErrorLog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MakroFromErrorLog().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton makroErrorLoguButton;
    // End of variables declaration//GEN-END:variables
}
