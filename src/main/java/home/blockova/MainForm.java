/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package home.blockova;

import static home.blockova.ZaregistrovatForm.LIMIT_BLOCKOV;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import sun.util.locale.LocaleExtensions;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import org.jsoup.nodes.Document;

/**
 *
 * @author Janco1
 */
public class MainForm extends javax.swing.JFrame implements PropertyChangeListener {

    private BlockyTableModel blockyTableModel;
    private String[] blockyTableColumnNames = {"id", "DKP", "Datum", "Suma", "Pocet"};
    ExecutorService es = Executors.newCachedThreadPool();
    boolean beziCasovac = false;
    Database database;
    Timer timer = new Timer(0);
    private List<Blocek> blocky;
    private List<Predajca> predajcovia;
    private JPopupMenu Pmenu;
    private JMenuItem menuItem;
    private PredajcoviaTableModel predajcoviaTableModel;
    private String[] predajcoviaTableColumnNames = {"meno", "DKP", "pocet"};
    private long startPridavanieCas;
    private int pridanychSession;
    private int nezaregistrovanych;
    private int previouslySelectedRow;
    long poslednyCas;
    private int cas;
    private Authenticator authenticator = new Authenticator();
    private String datumText = "";
    // PARAMETRE
    public static final int LIMIT_BLOCKOV = 50;
    private boolean demo = false;
    private int APP_ID = 6;
    private boolean SYSOUT_ON = false;
    private static final String VERZIA = "2.0";

    /**
     * Creates new form MainForm
     */
    public MainForm() {
        SpustacDatabazy.execute();
        database = new Database();
        blocky = database.getBlocekList();
        Collections.reverse(blocky);
        predajcovia = database.getPredajcaList();
        blockyTableModel = new BlockyTableModel(blockyTableColumnNames, blocky);
        predajcoviaTableModel = new PredajcoviaTableModel(predajcoviaTableColumnNames, predajcovia);
        database.addPropertyChangeListener(this);
        timer.addPropertyChangeListener(this);

        initComponents();
        jDateChooser1.setDate(new Date(System.currentTimeMillis()));
        najblizsieZrebovanieLabel.setText(new SimpleDateFormat("dd.MM.yyyy").format(database.najblizsiaRegistracia));
        if (!demo) {
            setTitle("Registrátor Bločkov Profesional");
        } else {
            setTitle("Registrátor Bločkov DEMO");
        }
        verziaLabel.setText(VERZIA + "." + APP_ID);
        refreshBlockyTable(blocky);
        refreshBlockyTableColors();
        refreshPredajcoviaTable(predajcovia);
        //
        minuteSliderStateChanged(null);
        hourSliderStateChanged(null);
        pridanychSessionLabel.setText(Integer.toString(pridanychSession));

        blockyTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                BlockyTableModel model = (BlockyTableModel) table.getModel();
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(model.getRowColour(row));
                // Only for specific cell
                if (isSelected) {
                    c.setFont(new Font("Tahoma", 1, 12));
                    // you may want to address isSelected here too
                    c.setForeground(Color.BLACK);
                    //c.setBackground(/*special background color*/);
                }
                return c;
            }
        });
        try {
            // zistujeme pocet registracii
            int cisloApp = 1;
            if (!demo) {
                cisloApp = APP_ID;
            }
            if (SYSOUT_ON) {
                System.out.println("cisloapp: " + cisloApp);
            }
            int pocetRegistracii = 0;
            try {
                String macaddress = "unknownMac";
                Document authResponse = authenticator.increaseAndReturnPocet(getMacAddress(macaddress), cisloApp, 0); // 1 je pre demo verziu, 0 lebo len chceme zistit pocet registracii
                if (SYSOUT_ON) {
                    System.out.println(authResponse);
                    System.out.println("vypisany prve prihlasenie");
                }
                // naparsovat cislo
                String text = "deefault Text";
                text = authResponse.toString();
                if (!text.startsWith("<!DOCTYPE html>")) {
                    JOptionPane.showMessageDialog(rootPane, "Vyskytla sa chyba, skontrolujte či ste pripojení k internetu: ");
                    pridatButton.setEnabled(false);
                    casTextField.setEnabled(false);
                }
                System.out.println("AUTH RESPONSE\n" + text);
                String pocetText = "id=\"pocetInput\" class=\"txtBoxElem long\" value=";
                int prvaUvodzovkaPos = text.indexOf(pocetText) + pocetText.length() + 1;
                int druhaUvodzovkaPos = text.substring(prvaUvodzovkaPos).indexOf("\"") + prvaUvodzovkaPos;
                pocetRegistracii = Integer.parseInt(text.substring(prvaUvodzovkaPos, druhaUvodzovkaPos));
                // check ci sme dostali spravnu auth response
            } catch (HeadlessException headlessException) {
                JOptionPane.showMessageDialog(rootPane, "Vyskytla sa chyba, skontrolujte či ste pripojení k internetu: ");
                pridatButton.setEnabled(false);
                casTextField.setEnabled(false);
            } catch (NumberFormatException numberFormatException) {
                JOptionPane.showMessageDialog(rootPane, "Vyskytla sa chyba, skontrolujte či ste pripojení k internetu: ");
                pridatButton.setEnabled(false);
                casTextField.setEnabled(false);
            } catch (NullPointerException nullPointerException) {
                JOptionPane.showMessageDialog(rootPane, "Vyskytla sa chyba, skontrolujte či ste pripojení k internetu: ");
                pridatButton.setEnabled(false);
                casTextField.setEnabled(false);
            }
            if (SYSOUT_ON) {
                System.out.println("value: " + pocetRegistracii);
            }
            if (demo) {
                if (pocetRegistracii >= LIMIT_BLOCKOV * 3) {
                    JOptionPane.showMessageDialog(rootPane, "Ľutujeme, ale na tomto pc ste už zaregistrovali viac ako " + LIMIT_BLOCKOV + " bločkov. \n "
                            + "Pre zakúpenie plnej verzie navštívte stránku registratorblockov.sk");
                    pridatButton.setEnabled(false);
                    casTextField.setEnabled(false);
                }
            } else {
                // co sa stane ak sme riadny registrator- TODO obmedzit na 2000 blockov
                if (pocetRegistracii >= 60000) {
                    JOptionPane.showMessageDialog(rootPane, "Ľutujeme, ale na tomto pc ste už urobili viac ako 6000 registrácií. \n "
                            + "Aby ste mohli pokračovať v registrovaní bločkov, pošlite foto z vašich zaregistrovaných bločkov na registratorblockov@registratorblockov.sk, \n aby ste preukázali, že nezneužívate tento program!");
                    pridatButton.setEnabled(false);
                    casTextField.setEnabled(false);
                    System.exit(0);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(rootPane, "Vyskytla sa chyba, skontrolujte či ste pripojení k internetu: " + ex);
            pridatButton.setEnabled(false);
            casTextField.setEnabled(false);
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
        }

        checkUsersCount();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        manualMakroButton = new javax.swing.JButton();
        exportButton = new javax.swing.JButton();
        importButton = new javax.swing.JButton();
        zeleneToCerveneButton = new javax.swing.JButton();
        idTextField = new javax.swing.JTextField();
        minusDatumButton1 = new javax.swing.JButton();
        minusDKPButton = new javax.swing.JButton();
        plusDKPButton = new javax.swing.JButton();
        plusDatumButton1 = new javax.swing.JButton();
        vygenerujBOTMakroButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        blockyTable = new javax.swing.JTable();
        pridatBlockyPanel = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        timeLabel = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        minuteSlider = new javax.swing.JSlider();
        hourSlider = new javax.swing.JSlider();
        pridatButton = new javax.swing.JButton();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        sumaTextField = new javax.swing.JTextField();
        dkpTextField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        menoRegistratoraTextField = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        casTextField = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        extendedCheckBox = new javax.swing.JCheckBox();
        predajcoviaPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        predajcoviaTable = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        resetCasovacButton = new javax.swing.JButton();
        startPridavanieButton = new javax.swing.JButton();
        stopwatchLabel = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        pridanychSessionLabel = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        rychlostLabelLabel = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        nezaregistrovanychLabel = new javax.swing.JLabel();
        zaregistrovanychLabel = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        najblizsieZrebovanieLabel = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        aktualneRegistrovanychLabel = new javax.swing.JLabel();
        verziaLabel = new javax.swing.JLabel();
        zaregistrovatButton = new javax.swing.JButton();
        blockovVHreTextField = new javax.swing.JTextField();
        sancaNaVyhruLabel = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTable1);

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Iné"));

        jLabel14.setText("ID:");

        manualMakroButton.setText("Vygeneruj MANUAL makro");
        manualMakroButton.setEnabled(false);
        manualMakroButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manualMakroButtonActionPerformed(evt);
            }
        });

        exportButton.setText("Export ");
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        importButton.setText("Import");
        importButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importButtonActionPerformed(evt);
            }
        });

        zeleneToCerveneButton.setText("zelene => červene");
        zeleneToCerveneButton.setEnabled(false);
        zeleneToCerveneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zeleneToCerveneButtonActionPerformed(evt);
            }
        });

        idTextField.setText("IDlog");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(152, 152, 152)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel14))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(manualMakroButton)
                            .addComponent(zeleneToCerveneButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(exportButton)
                            .addComponent(importButton, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(162, Short.MAX_VALUE))
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(idTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jLabel14)
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(manualMakroButton)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(exportButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(importButton)
                            .addComponent(zeleneToCerveneButton))))
                .addGap(0, 14, Short.MAX_VALUE))
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(idTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        minusDatumButton1.setText("-");
        minusDatumButton1.setMaximumSize(new java.awt.Dimension(41, 25));
        minusDatumButton1.setMinimumSize(new java.awt.Dimension(41, 25));
        minusDatumButton1.setPreferredSize(new java.awt.Dimension(41, 25));
        minusDatumButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minusDatumButton1ActionPerformed(evt);
            }
        });

        minusDKPButton.setText("-");
        minusDKPButton.setMaximumSize(new java.awt.Dimension(41, 25));
        minusDKPButton.setMinimumSize(new java.awt.Dimension(41, 25));
        minusDKPButton.setPreferredSize(new java.awt.Dimension(41, 25));
        minusDKPButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minusDKPButtonActionPerformed(evt);
            }
        });

        plusDKPButton.setText("+");
        plusDKPButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plusDKPButtonActionPerformed(evt);
            }
        });

        plusDatumButton1.setText("+");
        plusDatumButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plusDatumButton1ActionPerformed(evt);
            }
        });

        vygenerujBOTMakroButton.setText("Vygeneruj BOT makro");
        vygenerujBOTMakroButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vygenerujBOTMakroButtonActionPerformed(evt);
            }
        });

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        setResizable(false);

        blockyTable.setModel(blockyTableModel);
        blockyTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        blockyTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                blockyTableMouseReleased(evt);
            }
        });
        blockyTable.addHierarchyListener(new java.awt.event.HierarchyListener() {
            public void hierarchyChanged(java.awt.event.HierarchyEvent evt) {
                blockyTableHierarchyChanged(evt);
            }
        });
        blockyTable.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                blockyTablePropertyChange(evt);
            }
        });
        jScrollPane1.setViewportView(blockyTable);

        pridatBlockyPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Pridať Bloček"));
        pridatBlockyPanel.setLayout(new java.awt.GridBagLayout());

        jLabel6.setText("Čas:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(16, 27, 0, 0);
        pridatBlockyPanel.add(jLabel6, gridBagConstraints);

        timeLabel.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        timeLabel.setText("00:00");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(13, 5, 0, 0);
        pridatBlockyPanel.add(timeLabel, gridBagConstraints);

        jLabel4.setText("Hodina:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 26, 0, 0);
        pridatBlockyPanel.add(jLabel4, gridBagConstraints);

        jLabel5.setText("Minúta:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 27, 0, 0);
        pridatBlockyPanel.add(jLabel5, gridBagConstraints);

        minuteSlider.setMaximum(59);
        minuteSlider.setValue(0);
        minuteSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                minuteSliderStateChanged(evt);
            }
        });
        minuteSlider.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                minuteSliderPropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 13;
        gridBagConstraints.ipadx = 83;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 18, 0, 0);
        pridatBlockyPanel.add(minuteSlider, gridBagConstraints);

        hourSlider.setMaximum(24);
        hourSlider.setValue(0);
        hourSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                hourSliderStateChanged(evt);
            }
        });
        hourSlider.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                hourSliderPropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 13;
        gridBagConstraints.ipadx = 83;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 18, 0, 0);
        pridatBlockyPanel.add(hourSlider, gridBagConstraints);

        pridatButton.setText("Pridať bloček");
        pridatButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pridatButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 10;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 21;
        gridBagConstraints.ipady = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 18, 20, 0);
        pridatBlockyPanel.add(pridatButton, gridBagConstraints);

        jDateChooser1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jDateChooser1PropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 12;
        gridBagConstraints.ipadx = 118;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 8, 0, 0);
        pridatBlockyPanel.add(jDateChooser1, gridBagConstraints);

        jLabel2.setText("Dátum:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 18, 0, 0);
        pridatBlockyPanel.add(jLabel2, gridBagConstraints);

        jLabel1.setText("DKP:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 33, 0, 0);
        pridatBlockyPanel.add(jLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 13;
        gridBagConstraints.ipadx = 127;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 4, 0, 0);
        pridatBlockyPanel.add(sumaTextField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 12;
        gridBagConstraints.ipadx = 139;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 7, 0, 0);
        pridatBlockyPanel.add(dkpTextField, gridBagConstraints);

        jLabel3.setText("Suma:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(21, 18, 0, 0);
        pridatBlockyPanel.add(jLabel3, gridBagConstraints);

        jLabel15.setText("meno registrátora: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 20;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(21, 2, 0, 0);
        pridatBlockyPanel.add(jLabel15, gridBagConstraints);

        menoRegistratoraTextField.setText("registrator");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 20;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.ipadx = 249;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 2, 0, 18);
        pridatBlockyPanel.add(menoRegistratoraTextField, gridBagConstraints);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Rýchle zadanie času a sumy"));

        jLabel9.setFont(new java.awt.Font("Times New Roman", 2, 13)); // NOI18N
        jLabel9.setText("hhmm_suma+ENTER");

        casTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                casTextFieldActionPerformed(evt);
            }
        });
        casTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                casTextFieldKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                casTextFieldKeyTyped(evt);
            }
        });

        jLabel18.setFont(new java.awt.Font("Times New Roman", 2, 13)); // NOI18N
        jLabel18.setText("čas 9:25 a 5,2 Eur= 0925 5.2");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(casTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                .addComponent(jLabel18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(casTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 19;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.ipady = 1;
        gridBagConstraints.weightx = 0.2;
        pridatBlockyPanel.add(jPanel1, gridBagConstraints);

        jButton1.setText("-");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 20;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(16, 7, 0, 0);
        pridatBlockyPanel.add(jButton1, gridBagConstraints);

        jButton2.setText("+");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 19;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 18, 0, 0);
        pridatBlockyPanel.add(jButton2, gridBagConstraints);

        jButton3.setText("+");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 19;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(16, 19, 0, 0);
        pridatBlockyPanel.add(jButton3, gridBagConstraints);

        jButton4.setText("-");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 20;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 7, 0, 0);
        pridatBlockyPanel.add(jButton4, gridBagConstraints);

        extendedCheckBox.setText("extended");
        pridatBlockyPanel.add(extendedCheckBox, new java.awt.GridBagConstraints());

        predajcoviaPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Najčastejší predajcovia"));

        predajcoviaTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        predajcoviaTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                predajcoviaTableMouseReleased(evt);
            }
        });
        jScrollPane3.setViewportView(predajcoviaTable);

        jLabel7.setFont(new java.awt.Font("Tahoma", 3, 13)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(153, 153, 153));
        jLabel7.setText("Kliknutím pridáš DKP predajcu");

        javax.swing.GroupLayout predajcoviaPanelLayout = new javax.swing.GroupLayout(predajcoviaPanel);
        predajcoviaPanel.setLayout(predajcoviaPanelLayout);
        predajcoviaPanelLayout.setHorizontalGroup(
            predajcoviaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(predajcoviaPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(predajcoviaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(predajcoviaPanelLayout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        predajcoviaPanelLayout.setVerticalGroup(
            predajcoviaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(predajcoviaPanelLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jLabel7)
                .addGap(8, 8, 8)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Stopky"));

        resetCasovacButton.setText("Reset");
        resetCasovacButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetCasovacButtonActionPerformed(evt);
            }
        });

        startPridavanieButton.setText("Štart");
        startPridavanieButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startPridavanieButtonActionPerformed(evt);
            }
        });

        stopwatchLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        stopwatchLabel.setText("00:00:00");

        jLabel10.setText("pridanych bločkov:");

        pridanychSessionLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        pridanychSessionLabel.setText("0");

        jLabel11.setText("rýchlosť:");

        rychlostLabelLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        rychlostLabelLabel.setText("0");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(64, 64, 64)
                        .addComponent(stopwatchLabel))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(rychlostLabelLabel))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(pridanychSessionLabel)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 42, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(startPridavanieButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(resetCasovacButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(resetCasovacButton)
                .addContainerGap())
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(stopwatchLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(pridanychSessionLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(rychlostLabelLabel)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(startPridavanieButton)))
                .addGap(0, 4, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Informácie"));

        jLabel12.setText("nezaregistrovaných: ");

        nezaregistrovanychLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        nezaregistrovanychLabel.setForeground(new java.awt.Color(0, 153, 0));
        nezaregistrovanychLabel.setText("0");

        zaregistrovanychLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        zaregistrovanychLabel.setForeground(new java.awt.Color(204, 0, 0));
        zaregistrovanychLabel.setText("0");

        jLabel13.setText("zaregistrovaných:");

        jLabel17.setText("najbližšie žrebovanie:");

        najblizsieZrebovanieLabel.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        najblizsieZrebovanieLabel.setText("jLabel18");

        jLabel16.setText("V aktuálnom žrebovaní:");

        aktualneRegistrovanychLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        aktualneRegistrovanychLabel.setForeground(new java.awt.Color(204, 102, 0));
        aktualneRegistrovanychLabel.setText("0");

        verziaLabel.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        verziaLabel.setText("jLabel8");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel12)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel17)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 141, Short.MAX_VALUE)
                                .addComponent(najblizsieZrebovanieLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(nezaregistrovanychLabel))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(zaregistrovanychLabel)))
                        .addGap(63, 63, 63))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(aktualneRegistrovanychLabel)
                        .addGap(62, 62, 62))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(verziaLabel)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(nezaregistrovanychLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(zaregistrovanychLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(najblizsieZrebovanieLabel))
                .addGap(8, 8, 8)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(aktualneRegistrovanychLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(verziaLabel))
        );

        zaregistrovatButton.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        zaregistrovatButton.setText("Zaregistrovať bločky");
        zaregistrovatButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zaregistrovatButtonActionPerformed(evt);
            }
        });

        blockovVHreTextField.setText("710000");
        blockovVHreTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                blockovVHreTextFieldActionPerformed(evt);
            }
        });
        blockovVHreTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                blockovVHreTextFieldKeyReleased(evt);
            }
        });

        sancaNaVyhruLabel.setText("jLabel8");

        jLabel8.setText("Bločkov v hre:");

        jLabel19.setText("Šanca na výhru:");

        jTextField1.setText("5000");
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 489, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(zaregistrovatButton, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                            .addGap(34, 34, 34)
                                            .addComponent(jLabel8))
                                        .addGroup(layout.createSequentialGroup()
                                            .addGap(25, 25, 25)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jLabel19))))
                                    .addGroup(layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(blockovVHreTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(sancaNaVyhruLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pridatBlockyPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 473, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(predajcoviaPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pridatBlockyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(predajcoviaPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(zaregistrovatButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 5, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(blockovVHreTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel19)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sancaNaVyhruLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void hourSliderPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_hourSliderPropertyChange

    }//GEN-LAST:event_hourSliderPropertyChange

    private void minuteSliderPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_minuteSliderPropertyChange
    }//GEN-LAST:event_minuteSliderPropertyChange

    private void minuteSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_minuteSliderStateChanged
        //System.out.println("minuteSliderPropertyCHange: " + minuteSlider.getValue());
        String minut = "" + minuteSlider.getValue();
        if (minuteSlider.getValue() < 10) {
            minut = "" + 0 + minuteSlider.getValue();
        }
        String cas = timeLabel.getText();
        timeLabel.setText(cas.substring(0, 2) + ":" + minut);
        if (jDateChooser1.getDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            //datumTextField.setText(sdf.format(jDateChooser1.getDate().getTime()) + " " + timeLabel.getText());
            datumText = sdf.format(jDateChooser1.getDate().getTime()) + " " + timeLabel.getText();
        } else {
            //System.out.println("jdatechooser is null");
        }
    }//GEN-LAST:event_minuteSliderStateChanged

    private void hourSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_hourSliderStateChanged
        //System.out.println("hourSliderPropertyCHange: " + hourSlider.getValue());
        String hodin = "" + hourSlider.getValue();
        if (hourSlider.getValue() < 10) {
            hodin = "" + 0 + hourSlider.getValue();
        }
        String cas = timeLabel.getText();
        timeLabel.setText(hodin + ":" + cas.substring(3));
        if (jDateChooser1.getDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            //datumTextField.setText(sdf.format(jDateChooser1.getDate().getTime()) + " " + timeLabel.getText());
            datumText = sdf.format(jDateChooser1.getDate().getTime()) + " " + timeLabel.getText();
        } else {
            // System.out.println("jdatechooser is null");
        }
    }//GEN-LAST:event_hourSliderStateChanged

    private void jDateChooser1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jDateChooser1PropertyChange
        //System.out.println("jdatechooser property change");
        if (jDateChooser1.getDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            //datumTextField.setText(sdf.format(jDateChooser1.getDate().getTime()) + " " + timeLabel.getText());
            datumText = sdf.format(jDateChooser1.getDate().getTime()) + " " + timeLabel.getText();
        } else {
            //System.out.println("jdatechooser is null");
        }

    }//GEN-LAST:event_jDateChooser1PropertyChange

    private void pridatButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pridatButtonActionPerformed
        if (!beziCasovac) {
            startPridavanieButtonActionPerformed(evt);
        }
        Blocek novy = new Blocek();
        if (dkpTextField.getText().equalsIgnoreCase("")) {
            JOptionPane.showMessageDialog(rootPane, "Nevyplnena položka: DKP.");
            return;
        } else {
            novy.dkp = dkpTextField.getText();
        }
        if (datumText.equalsIgnoreCase("")) {
            JOptionPane.showMessageDialog(rootPane, "Nevyplnena položka: dátum.");
            return;
        } else {
            novy.datum = datumText + ":00";
        }
//        if (datumTextField.getText().equalsIgnoreCase("")) {
//            JOptionPane.showMessageDialog(rootPane, "Nevyplnena položka: dátum.");
//            return;
//        } else {
//            novy.datum = datumTextField.getText() + ":00";
//        }
        if (sumaTextField.getText().equalsIgnoreCase("")) {
            JOptionPane.showMessageDialog(rootPane, "Nevyplnena položka: suma.");
            return;
        } else {
            novy.suma = Double.parseDouble(sumaTextField.getText());
        }
        if (menoRegistratoraTextField.getText().equalsIgnoreCase("")) {
            JOptionPane.showMessageDialog(rootPane, "Nevyplnena položka: menoRegistrátora.");
            return;
        } else {
            novy.registrator = menoRegistratoraTextField.getText();
        }
        novy.pocet = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
        novy.timeInserted = sdf.format(new Date(System.currentTimeMillis())).toString();

        database.insertBlocekToDB(novy);
        if (jeNovyPredajca(novy.dkp)) {
            // System.out.println("je novy predajca");
            database.insertPredajcaToDB(new Predajca(novy.dkp, novy.dkp));
        } else {
            refreshPredajcoviaTable(predajcovia);
        }

        sumaTextField.setText("");
        casTextField.setText("");
        pridanychSession++;
        pridanychSessionLabel.setText(Integer.toString(pridanychSession));
        DecimalFormat df = new DecimalFormat("##.##");
        double rychlost=(double)cas / pridanychSession;
        System.out.println("rychlost: "+rychlost);
        rychlostLabelLabel.setText(df.format(rychlost));

    }//GEN-LAST:event_pridatButtonActionPerformed

    private void blockyTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_blockyTableMouseReleased
        //    System.out.println("selected row: " + blockyTable.getSelectedRow());
        Point p = evt.getPoint();
        int rowNumber = blockyTable.rowAtPoint(p);
        //refreshBlockyTableColors();

        if (previouslySelectedRow != -1) {
            Blocek akt = null;
            Object idcko = blockyTable.getValueAt(previouslySelectedRow, 0);
            if (idcko instanceof Integer) {
                int id = (Integer) idcko;
                akt = getAktBlocek(id);
                //System.out.println("vybrany blocek s id: " + akt.id);
                //dkpTextField.setText(akt.dkp);
            } else {
                System.err.println("chyba castovania prveho column na int (ID)");
                return;
            }
            if (akt.pocet > 2) {
                if (SYSOUT_ON) {
                    System.out.println("cervena farba v riadku " + previouslySelectedRow);
                }
                blockyTableModel.setRowColour(previouslySelectedRow, Color.red);
            }
            if (akt.pocet == 2) {
                blockyTableModel.setRowColour(previouslySelectedRow, Color.orange);
            }
            if (akt.pocet < 2) {
                blockyTableModel.setRowColour(previouslySelectedRow, Color.green);
            }
        }
        previouslySelectedRow = rowNumber;
        blockyTableModel.setRowColour(rowNumber, Color.yellow);

        // LEFT MOUSE BUTTON CLICKED
        if (evt.getButton() == MouseEvent.BUTTON1) {
            //Blocek akt = blocky.get(rowNumber);
            Object idcko = blockyTable.getValueAt(rowNumber, 0);
            Blocek akt = null;
            if (idcko instanceof Integer) {
                int id = (Integer) idcko;
                akt = getAktBlocek(id);
                //System.out.println("vybrany blocek s id: " + akt.id);
                dkpTextField.setText(akt.dkp);
            } else {
                System.err.println("chyba castovania prveho column na int (ID)");
                return;
            }

            // System.out.println("datum na blocku: " + akt.datum);
            //datumTextField.setText(akt.datum.substring(0, 16));
            datumText = akt.datum.substring(0, 16);
            Date dat = new Date(System.currentTimeMillis());
            try {
                dat = new SimpleDateFormat("yyyy-MM-dd").parse(akt.datum.split(" ")[0]);
            } catch (ParseException ex) {
                Logger.getLogger(EditBlocekForm.class.getName()).log(Level.SEVERE, null, ex);
            }
            jDateChooser1.setDate(dat);
            //System.out.println("datum nastaveny na: " + dat);
            //jDateChooser1.setDate(new Date(blocek.datum));
            //jDateChooser1.setDate(new Date(Integer.parseInt(blocek.datum.split(" ")[0].split("-")[0]), Integer.parseInt(blocek.datum.split(" ")[0].split("-")[1]), Integer.parseInt(blocek.datum.split(" ")[0].split("-")[2])));

            hourSlider.setValue(Integer.parseInt(akt.datum.split(" ")[1].split(":")[0]));
            minuteSlider.setValue(Integer.parseInt(akt.datum.split(" ")[1].split(":")[1]));
            sumaTextField.setText(Double.toString(akt.suma));
            menoRegistratoraTextField.setText(akt.registrator);
        }
        // RIGHT MOUSE BUTTON CLICKED, display popup menu
        if (evt.getButton() == MouseEvent.BUTTON3) {
            Object idcko = blockyTable.getValueAt(rowNumber, 0);
            Blocek akt = null;
            if (idcko instanceof Integer) {
                int id = (Integer) idcko;
                akt = getAktBlocek(id);
                // System.out.println("vybrany blocek s id: " + akt.id);
                dkpTextField.setText(akt.dkp);
            } else {
                System.err.println("chyba castovania prveho column na int (ID)");
                return;
            }
            //refreshBlockyTableColors();
            // select row with right mouse click
            // get the coordinates of the mouse click

            ListSelectionModel model = blockyTable.getSelectionModel();
            model.setSelectionInterval(rowNumber, rowNumber);

            ActionListener menuListener;
            menuListener = new PopupMenuActionListener(akt, database, this);
            Pmenu = new JPopupMenu();
            menuItem = new JMenuItem("Delete");
            Pmenu.add(menuItem);
            menuItem.addActionListener(menuListener);
            menuItem = new JMenuItem("Edit");
            Pmenu.add(menuItem);
            menuItem.addActionListener(menuListener);
//            menuItem = new JMenuItem("View Alarm");
//            Pmenu.add(menuItem);
//            menuItem.addActionListener(menuListener);
//            menuItem = new JMenuItem("Delete");
//            Pmenu.add(menuItem);
//            menuItem = new JMenuItem("Undo");
//            Pmenu.add(menuItem);
            Pmenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_blockyTableMouseReleased

    private void predajcoviaTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_predajcoviaTableMouseReleased
        Point p = evt.getPoint();
        int rowNumber = predajcoviaTable.rowAtPoint(p);
        //System.out.println("predajcovia rownumber: " + rowNumber);

        // LEFT MOUSE BUTTON CLICKED
        if (evt.getButton() == MouseEvent.BUTTON1) {
            Object dkpcko = predajcoviaTable.getValueAt(rowNumber, 1);
            Predajca akt = null;
            if (dkpcko instanceof String) {
                String dkp = (String) dkpcko;
                akt = getAktPredajca(dkp);
                // System.out.println("vybrany predajca s menom: " + akt.meno);
                dkpTextField.setText(akt.dkp);
            } else {
                System.err.println("chyba castovania prveho column na String (DKP)");
                return;
            }
            dkpTextField.setText(akt.dkp);
        }

        // RIGHT MOUSE BUTTON CLICKED, display popup menu
        if (evt.getButton() == MouseEvent.BUTTON3) {
            //refreshBlockyTableColors();
            // select row with right mouse click
            // get the coordinates of the mouse click
            Object dkpcko = predajcoviaTable.getValueAt(rowNumber, 1);
            Predajca akt = null;
            if (dkpcko instanceof String) {
                String dkp = (String) dkpcko;
                akt = getAktPredajca(dkp);
                //   System.out.println("vybrany predajca s menom: " + akt.meno);
                dkpTextField.setText(akt.dkp);
            } else {
                System.err.println("chyba castovania prveho column na String (DKP)");
                return;
            }
            ListSelectionModel model = predajcoviaTable.getSelectionModel();

            model.setSelectionInterval(rowNumber, rowNumber);

            ActionListener menuListener;
            //System.out.println("row number do menulistenera je : " + rowNumber);
            menuListener = new PopupMenuActionListener(akt, database, this);
            Pmenu = new JPopupMenu();
            menuItem = new JMenuItem("Zmen Meno");
            Pmenu.add(menuItem);
            menuItem.addActionListener(menuListener);
            Pmenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_predajcoviaTableMouseReleased

    private void startPridavanieButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startPridavanieButtonActionPerformed
        if (!beziCasovac) {
            beziCasovac = true;
            startPridavanieButton.setText("STOP");
            startPridavanieCas = System.currentTimeMillis();
            timer.startTime = startPridavanieCas;
            timer.stop = false;
            es.execute(timer);
        } else {
            beziCasovac = false;
            startPridavanieButton.setText("Štart");
            timer.stop = true;
            // poslednyCas += System.currentTimeMillis() - startPridavanieCas;
        }
    }//GEN-LAST:event_startPridavanieButtonActionPerformed

    private void resetCasovacButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetCasovacButtonActionPerformed
        poslednyCas = 0;
        beziCasovac = false;
        startPridavanieButton.setText("Štart");
        timer.stop = true;
        stopwatchLabel.setText("00:00:00");
        pridanychSession = 0;
        pridanychSessionLabel.setText(Integer.toString(pridanychSession));
    }//GEN-LAST:event_resetCasovacButtonActionPerformed

    private void casTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_casTextFieldKeyTyped
//        if (casTextField.getText().length() < 4) {
//            System.out.println("cas je mensi ako 4 cislice");
//        }
//        int key = evt.getKeyCode();
//        if (key == KeyEvent.VK_ENTER) {
//            System.out.println("stlaceny enter");
//            if (casTextField.getText().length() == 4) {
//                int cislo = Integer.parseInt(casTextField.getText().substring(0, 4));
//                int hodin = cislo / 100;
//                int minut = cislo % 100;
//                hourSlider.setValue(hodin);
//                minuteSlider.setValue(minut);
//            }else{
//                return;
//            }
//            Double value=Double.parseDouble(casTextField.getText().substring(5, casTextField.getText().length()));
//            sumaTextField.setText(Double.toString(value));
//            pridatButtonActionPerformed(null);
//        }


    }//GEN-LAST:event_casTextFieldKeyTyped

    private void casTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_casTextFieldKeyReleased

        int key = evt.getKeyCode();
        if (key == KeyEvent.VK_ENTER) {
            if (!extendedCheckBox.isSelected()) {
                casTextField.setText(casTextField.getText().replace(',', '.'));
                // check pocet zloziek
                String[] zlozky = casTextField.getText().split(" ");
                if (zlozky.length < 2) {
                    JOptionPane.showMessageDialog(rootPane, "Nesprávny formát času a sumy. \n Príklad: čas 9:25 a 5,2 Eur= 0925 5.2");
                    return;
                }
                // check udaje o case su cislo
                try {
                    int pcislo = Integer.parseInt(zlozky[0]);
                } catch (NumberFormatException numberFormatException) {
                    JOptionPane.showMessageDialog(rootPane, "Nesprávny formát času a sumy. \n Príklad: čas 9:25 a 5,2 Eur= 0925 5.2");
                    return;
                }
                // check udaje o case maju dlzku 4
                if (zlozky[0].length() != 4) {
                    JOptionPane.showMessageDialog(rootPane, "Nesprávny formát času a sumy. \n Príklad: čas 9:25 a 5,2 Eur= 0925 5.2");
                    return;
                }
                // check pocet hodin je od 0..23
                int phodin = Integer.parseInt(zlozky[0].substring(0, 2));
                if (phodin < 0 || phodin >= 24) {
                    JOptionPane.showMessageDialog(rootPane, "Nesprávny formát času a sumy. \n Príklad: čas 9:25 a 5,2 Eur= 0925 5.2");
                    return;
                }
                // check pocet minut je od 0..59
                int pminut = Integer.parseInt(zlozky[0].substring(2, 4));
                if (pminut >= 60) {
                    JOptionPane.showMessageDialog(rootPane, "Nesprávny formát času a sumy. \n Príklad: čas 9:25 a 5,2 Eur= 0925 5.2");
                    return;
                }
                // check cislo sumy je cislo
                try {
                    double psuma = Double.parseDouble(zlozky[1]);
                } catch (NumberFormatException numberFormatException) {
                    JOptionPane.showMessageDialog(rootPane, "Nesprávny formát času a sumy. \n Príklad: čas 9:25 a 5,2 Eur= 0925 5.2");
                    return;
                }

                // System.out.println("stlaceny enter");
                if (casTextField.getText().length() > 4) {
                    int cislo = Integer.parseInt(casTextField.getText().substring(0, 4));
                    int hodin = cislo / 100;
                    int minut = cislo % 100;
                    hourSlider.setValue(hodin);
                    minuteSlider.setValue(minut);
                } else {
                    return;
                }
                Double value = Double.parseDouble(casTextField.getText().substring(5, casTextField.getText().length()));
                sumaTextField.setText(Double.toString(value));
            } else {
                int cislo = Integer.parseInt(casTextField.getText().substring(3, 7));
                int hodin = cislo / 100;
                int minut = cislo % 100;
                hourSlider.setValue(hodin);
                minuteSlider.setValue(minut);
                Double value = Double.parseDouble(casTextField.getText().substring(7, casTextField.getText().length()));
                sumaTextField.setText(Double.toString(value));
                int den = Integer.parseInt(casTextField.getText().substring(0, 2));

                Date dat = new Date(System.currentTimeMillis());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Calendar c = Calendar.getInstance();
                try {
                    //c.setTime(sdf.parse(datumTextField.getText()));
                    c.setTime(sdf.parse(datumText));
                    c.set(Calendar.DAY_OF_MONTH, den);
                    dat = sdf.parse(sdf.format(c.getTime()));
                } catch (ParseException ex) {
                    Logger.getLogger(EditBlocekForm.class.getName()).log(Level.SEVERE, null, ex);
                }
                jDateChooser1.setDate(dat);
            }
            Toolkit.getDefaultToolkit().beep();
            pridatButtonActionPerformed(null);
        }
    }//GEN-LAST:event_casTextFieldKeyReleased

    private void blockyTableHierarchyChanged(java.awt.event.HierarchyEvent evt) {//GEN-FIRST:event_blockyTableHierarchyChanged
        //refreshBlockyTableColors();
    }//GEN-LAST:event_blockyTableHierarchyChanged

    private void blockyTablePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_blockyTablePropertyChange
        //refreshBlockyTableColors();
    }//GEN-LAST:event_blockyTablePropertyChange

    private void zeleneToCerveneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zeleneToCerveneButtonActionPerformed
        List<Blocek> toUpdate = new ArrayList<Blocek>();
        for (Blocek b : blocky) {
            if (b.pocet < 3) {
                b.pocet = 3;
                toUpdate.add(b);
                if (b.timeMakroGenerated == null) {
                    JOptionPane.showMessageDialog(rootPane, "Niektoré bločky neboli exportované do bot makra. Značenie nevybavených bločkov ako vybavených je prerušené.");
                    return;
                }
            }
        }
        database.updateBlocekFromDB(toUpdate);
        zeleneToCerveneButton.setEnabled(false);
    }//GEN-LAST:event_zeleneToCerveneButtonActionPerformed

    private void vygenerujBOTMakroButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vygenerujBOTMakroButtonActionPerformed
        PrintWriter out = null;
        try {
            String nazov = "autoRegisterMakro" + idTextField.getText() + "_1.js";
            out = new PrintWriter("C:\\Users\\Janco1\\Documents\\iMacros\\Macros\\" + nazov, "UTF-8");

            StringBuilder dkps = new StringBuilder("[");
            StringBuilder datums = new StringBuilder("[");
            StringBuilder sums = new StringBuilder("[");
            int count = 0;
            List<Blocek> toUpdate = new ArrayList<Blocek>();
            Collections.shuffle(blocky);
            for (Blocek b : blocky) {
                if (b.pocet < 3) {
                    toUpdate.add(b);
                    count++;
                    dkps.append("\"" + b.dkp + "\",");
                    datums.append("\"" + b.datum.substring(0, 16) + "\",");
                    sums.append("\"" + b.suma + "\",");

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
                    b.timeMakroGenerated = sdf.format(new Date(System.currentTimeMillis())).toString();
                    b.pocet++;
                }
            }
            // System.out.println("toUpdate size: " + toUpdate.size());

            String dkpcka = dkps.toString().substring(0, dkps.length() - 1) + "];";
            String datumy = datums.toString().substring(0, datums.length() - 1) + "];";
            String sumy = sums.toString().substring(0, sums.length() - 1) + "];";
            //System.out.println(dkpcka);
            // System.out.println(datumy);
            // System.out.println(sumy);
            out.println("var dkps=" + dkpcka);
            out.println("var datums=" + datumy);
            out.println("var sums=" + sumy);
            out.println("var errorlog=\"" + idTextField.getText() + "_1_errorlog.txt\";");
            out.println("var log=\"" + idTextField.getText() + "_1_log.txt\";");

            BufferedReader f = new BufferedReader(new FileReader("C:\\Users\\Janco1\\Documents\\iMacros\\Macros\\rawAutoRegisterMakro.txt"));

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
            database.updateBlocekFromDB(toUpdate);
            // System.out.println("VYTVORENE MAKRO S " + count + " BLOCKAMI PRE FORMULAR");
        } catch (IOException ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
        }

        out.close();

        // DRUHE MAKRO
        out = null;
        try {
            String nazov = "autoRegisterMakro" + idTextField.getText() + "_2.js";
            out = new PrintWriter("C:\\Users\\Janco1\\Documents\\iMacros\\Macros\\" + nazov, "UTF-8");

            StringBuilder dkps = new StringBuilder("[");
            StringBuilder datums = new StringBuilder("[");
            StringBuilder sums = new StringBuilder("[");
            int count = 0;
            List<Blocek> toUpdate = new ArrayList<Blocek>();
            Collections.shuffle(blocky);
            for (Blocek b : blocky) {
                if (b.pocet < 3) {
                    toUpdate.add(b);
                    count++;
                    dkps.append("\"" + b.dkp + "\",");
                    datums.append("\"" + b.datum.substring(0, 16) + "\",");
                    sums.append("\"" + b.suma + "\",");

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
                    b.timeMakroGenerated = sdf.format(new Date(System.currentTimeMillis())).toString();
                    b.pocet++;
                }
            }
            // System.out.println("toUpdate size: " + toUpdate.size());

            String dkpcka = dkps.toString().substring(0, dkps.length() - 1) + "];";
            String datumy = datums.toString().substring(0, datums.length() - 1) + "];";
            String sumy = sums.toString().substring(0, sums.length() - 1) + "];";
            //System.out.println(dkpcka);
            // System.out.println(datumy);
            // System.out.println(sumy);
            out.println("var dkps=" + dkpcka);
            out.println("var datums=" + datumy);
            out.println("var sums=" + sumy);
            out.println("var errorlog=\"" + idTextField.getText() + "_2_errorlog.txt\";");
            out.println("var log=\"" + idTextField.getText() + "_2_log.txt\";");

            BufferedReader f = new BufferedReader(new FileReader("C:\\Users\\Janco1\\Documents\\iMacros\\Macros\\rawAutoRegisterMakro2.txt"));

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
            database.updateBlocekFromDB(toUpdate);
            //System.out.println("VYTVORENE MAKRO S " + count + " BLOCKAMI PRE FORMULAR");
        } catch (IOException ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
        }

        out.close();

        //  TRETIE MAKRO
        out = null;
        try {
            String nazov = "autoRegisterMakro" + idTextField.getText() + "_3.js";
            out = new PrintWriter("C:\\Users\\Janco1\\Documents\\iMacros\\Macros\\" + nazov, "UTF-8");

            StringBuilder dkps = new StringBuilder("[");
            StringBuilder datums = new StringBuilder("[");
            StringBuilder sums = new StringBuilder("[");
            int count = 0;
            List<Blocek> toUpdate = new ArrayList<Blocek>();
            Collections.shuffle(blocky);
            for (Blocek b : blocky) {
                if (b.pocet < 3) {
                    toUpdate.add(b);
                    count++;
                    dkps.append("\"" + b.dkp + "\",");
                    datums.append("\"" + b.datum.substring(0, 16) + "\",");
                    sums.append("\"" + b.suma + "\",");

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
                    b.timeMakroGenerated = sdf.format(new Date(System.currentTimeMillis())).toString();
                    b.pocet++;
                }
            }
            // System.out.println("toUpdate size: " + toUpdate.size());

            String dkpcka = dkps.toString().substring(0, dkps.length() - 1) + "];";
            String datumy = datums.toString().substring(0, datums.length() - 1) + "];";
            String sumy = sums.toString().substring(0, sums.length() - 1) + "];";
            //System.out.println(dkpcka);
            //System.out.println(datumy);
            // System.out.println(sumy);
            out.println("var dkps=" + dkpcka);
            out.println("var datums=" + datumy);
            out.println("var sums=" + sumy);
            out.println("var errorlog=\"" + idTextField.getText() + "_3_errorlog.txt\";");
            out.println("var log=\"" + idTextField.getText() + "_3_log.txt\";");

            BufferedReader f = new BufferedReader(new FileReader("C:\\Users\\Janco1\\Documents\\iMacros\\Macros\\rawAutoRegisterMakro3.txt"));

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
            database.updateBlocekFromDB(toUpdate);
            // System.out.println("VYTVORENE MAKRO S " + count + " BLOCKAMI PRE FORMULAR");
        } catch (IOException ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
        }

        out.close();
    }//GEN-LAST:event_vygenerujBOTMakroButtonActionPerformed

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        List<Blocek> toExport = new ArrayList<Blocek>();
        int max = 0;
        int min = Integer.MAX_VALUE;
        for (Blocek b : blocky) {
            if (b.pocet < 3 && b.id < min) {
                min = b.id;
            }
            if (b.pocet < 3 && b.id > max) {
                max = b.id;
            }
            if (b.pocet < 3) {

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
                b.timeMakroGenerated = sdf.format(new Date(System.currentTimeMillis())).toString();
                toExport.add(b);
            }
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("choosertitle");
        chooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
        chooser.setSelectedFile(new File(min + "to" + max + "_export.txt"));

        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            // System.out.println("getCurrentDirectory(): " + chooser.getCurrentDirectory());
            // System.out.println("getSelectedFile() : " + chooser.getSelectedFile());
            try {
                PrintWriter pw = new PrintWriter(chooser.getSelectedFile());
                //pw.println("skuska export");
                for (Blocek b : toExport) {
                    pw.println(b.dkp + "_" + b.datum + "_" + b.suma + "_" + b.registrator + "_" + b.timeInserted);
                }

                pw.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
            }
            database.updateBlocekFromDB(toExport);
        } else {
            //  System.out.println("No Selection ");
        }
        zeleneToCerveneButton.setEnabled(true);
    }//GEN-LAST:event_exportButtonActionPerformed

    private void importButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importButtonActionPerformed
        List<Blocek> toImport = new ArrayList<Blocek>();

        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("choosertitle");
        chooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            //System.out.println("getCurrentDirectory(): " + chooser.getCurrentDirectory());
            // System.out.println("getSelectedFile() : " + chooser.getSelectedFile());
            try {
                BufferedReader f = new BufferedReader(new FileReader(chooser.getSelectedFile()));

                while (true) {
                    StringTokenizer st = null;
                    try {
                        String line = f.readLine();

                        if (line == null) {
                            break;
                        }
                        String[] zlozky = line.split("_");
                        Blocek novy = new Blocek();
                        novy.dkp = zlozky[0];
                        novy.datum = zlozky[1];
                        novy.suma = Double.parseDouble(zlozky[2]);
                        novy.registrator = zlozky[3];
                        novy.timeInserted = zlozky[4];
                        toImport.add(novy);
                    } catch (Exception exception) {
                        break;
                    }
                }
                int i = 0;
                for (Blocek b : toImport) {
                    i++;
                    //   System.out.println(i + " = " + b.dkp + " " + b.datum + " " + b.suma + " " + b.registrator+ "_" + b.timeInserted);
                }
                database.insertBlockyToDB(toImport);
            } catch (Exception ex) {
                Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(rootPane, "Nesprávny formát súboru: " + chooser.getSelectedFile());
            }
        } else {
            // System.out.println("No Selection ");
        }
    }//GEN-LAST:event_importButtonActionPerformed

    private void plusDKPButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plusDKPButtonActionPerformed
        try {
            long dkp = Long.parseLong(dkpTextField.getText());
            dkp++;
            dkpTextField.setText(Long.toString(dkp));
        } catch (NumberFormatException numberFormatException) {
        }
    }//GEN-LAST:event_plusDKPButtonActionPerformed

    private void minusDKPButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minusDKPButtonActionPerformed
        try {
            long dkp = Long.parseLong(dkpTextField.getText());
            dkp--;
            dkpTextField.setText(Long.toString(dkp));
        } catch (NumberFormatException numberFormatException) {
        }
    }//GEN-LAST:event_minusDKPButtonActionPerformed

    private void manualMakroButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualMakroButtonActionPerformed
        PrintWriter out = null;
        try {
            String nazov = "manualRegisterMakro" + idTextField.getText() + ".js";
            out = new PrintWriter("C:\\Users\\Janco1\\Documents\\iMacros\\Macros\\" + nazov, "UTF-8");

            StringBuilder dkps = new StringBuilder("[");
            StringBuilder datums = new StringBuilder("[");
            StringBuilder sums = new StringBuilder("[");
            int count = 0;
            List<Blocek> toUpdate = new ArrayList<Blocek>();
            Collections.shuffle(blocky);
            for (Blocek b : blocky) {
                if (b.pocet < 3) {
                    toUpdate.add(b);
                    count++;
                    dkps.append("\"" + b.dkp + "\",");
                    datums.append("\"" + b.datum.substring(0, 16) + "\",");
                    sums.append("\"" + b.suma + "\",");

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
                    b.timeMakroGenerated = sdf.format(new Date(System.currentTimeMillis())).toString();
                    b.pocet++;
                }
            }
            //System.out.println("toUpdate size: " + toUpdate.size());

            String dkpcka = dkps.toString().substring(0, dkps.length() - 1) + "];";
            String datumy = datums.toString().substring(0, datums.length() - 1) + "];";
            String sumy = sums.toString().substring(0, sums.length() - 1) + "];";
            // System.out.println(dkpcka);
            // System.out.println(datumy);
            // System.out.println(sumy);
            out.println("var dkps=" + dkpcka);
            out.println("var datums=" + datumy);
            out.println("var sums=" + sumy);
            out.println("var errorlog=\"" + idTextField.getText() + "errorlog.txt\";");
            out.println("var log=\"" + idTextField.getText() + "log.txt\";");

            BufferedReader f = new BufferedReader(new FileReader("C:\\Users\\Janco1\\Documents\\iMacros\\Macros\\rawManualRegisterMakro.txt"));

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
            database.updateBlocekFromDB(toUpdate);
            //System.out.println("VYTVORENE MAKRO S " + count + " BLOCKAMI PRE FORMULAR");
        } catch (IOException ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
        }

        out.close();
    }//GEN-LAST:event_manualMakroButtonActionPerformed

    private void minusDatumButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minusDatumButton1ActionPerformed
        Date dat = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        try {
            //c.setTime(sdf.parse(datumTextField.getText()));
            c.setTime(sdf.parse(datumText));
            c.add(Calendar.DATE, -1);  // number of days to add
            dat = sdf.parse(sdf.format(c.getTime()));
        } catch (ParseException ex) {
            Logger.getLogger(EditBlocekForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        jDateChooser1.setDate(dat);
    }//GEN-LAST:event_minusDatumButton1ActionPerformed

    private void plusDatumButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plusDatumButton1ActionPerformed
        Date dat = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        try {
            //c.setTime(sdf.parse(datumTextField.getText()));
            c.setTime(sdf.parse(datumText));
            c.add(Calendar.DATE, 1);  // number of days to add
            dat = sdf.parse(sdf.format(c.getTime()));
        } catch (ParseException ex) {
            Logger.getLogger(EditBlocekForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        jDateChooser1.setDate(dat);
    }//GEN-LAST:event_plusDatumButton1ActionPerformed

    private void zaregistrovatButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zaregistrovatButtonActionPerformed
        if (SYSOUT_ON) {
            System.out.println("Zaregistrovat form");
        }
        ZaregistrovatForm zf = new ZaregistrovatForm(database);
        zf.setVisible(true);
    }//GEN-LAST:event_zaregistrovatButtonActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        plusDKPButtonActionPerformed(evt);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        minusDKPButtonActionPerformed(evt);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        plusDatumButton1ActionPerformed(evt);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        minusDatumButton1ActionPerformed(evt);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void blockovVHreTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_blockovVHreTextFieldKeyReleased
        int key = evt.getKeyCode();
        if (key == KeyEvent.VK_ENTER) {
            blockovVHreEnterPressed();
        }
    }//GEN-LAST:event_blockovVHreTextFieldKeyReleased

    private void casTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_casTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_casTextFieldActionPerformed

    private void blockovVHreTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_blockovVHreTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_blockovVHreTextFieldActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void blockovVHreEnterPressed() {
        // spocitame sancu na vyhru
        double aktualneBlockov = Integer.parseInt(aktualneRegistrovanychLabel.getText());
        double blockovVHre = Integer.parseInt(blockovVHreTextField.getText());
        aktualneBlockov=Double.parseDouble(jTextField1.getText());
        System.out.println("aktualne blockov: " + aktualneBlockov + " blockov v loterii: " + blockovVHre);
        double sancaNaPrehru = 1 - aktualneBlockov / blockovVHre;
        double sancaNicNevyhrajem = Math.pow(sancaNaPrehru, 101);
        double sancaNaVyhru = (1 - sancaNicNevyhrajem);
        System.out.println("sanca na prehru: " + sancaNaPrehru + " sanca nic nevyhrajem zo 101 zrebovani: " + sancaNicNevyhrajem);
        DecimalFormat df = new DecimalFormat("###.##");
        sancaNaVyhruLabel.setText(df.format(sancaNaVyhru * 100) + " %");
    }

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
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel aktualneRegistrovanychLabel;
    private javax.swing.JTextField blockovVHreTextField;
    private javax.swing.JTable blockyTable;
    private javax.swing.JTextField casTextField;
    private javax.swing.JTextField dkpTextField;
    private javax.swing.JButton exportButton;
    private javax.swing.JCheckBox extendedCheckBox;
    private javax.swing.JSlider hourSlider;
    private javax.swing.JTextField idTextField;
    private javax.swing.JButton importButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JButton manualMakroButton;
    private javax.swing.JTextField menoRegistratoraTextField;
    private javax.swing.JButton minusDKPButton;
    private javax.swing.JButton minusDatumButton1;
    private javax.swing.JSlider minuteSlider;
    private javax.swing.JLabel najblizsieZrebovanieLabel;
    private javax.swing.JLabel nezaregistrovanychLabel;
    private javax.swing.JButton plusDKPButton;
    private javax.swing.JButton plusDatumButton1;
    private javax.swing.JPanel predajcoviaPanel;
    private javax.swing.JTable predajcoviaTable;
    private javax.swing.JLabel pridanychSessionLabel;
    private javax.swing.JPanel pridatBlockyPanel;
    private javax.swing.JButton pridatButton;
    private javax.swing.JButton resetCasovacButton;
    private javax.swing.JLabel rychlostLabelLabel;
    private javax.swing.JLabel sancaNaVyhruLabel;
    private javax.swing.JButton startPridavanieButton;
    private javax.swing.JLabel stopwatchLabel;
    private javax.swing.JTextField sumaTextField;
    private javax.swing.JLabel timeLabel;
    private javax.swing.JLabel verziaLabel;
    private javax.swing.JButton vygenerujBOTMakroButton;
    private javax.swing.JLabel zaregistrovanychLabel;
    private javax.swing.JButton zaregistrovatButton;
    private javax.swing.JButton zeleneToCerveneButton;
    // End of variables declaration//GEN-END:variables

    public void propertyChange(PropertyChangeEvent evt) {
        if ("blocekAdded".equals(evt.getPropertyName())) {
            if (SYSOUT_ON) {
                System.out.println("Pridany blocek");
            }
            blocky = database.getBlocekList();
            //System.out.println(blocky.get(0));
            Collections.reverse(blocky);
            //System.out.println(blocky.get(0));
            if (pridatButton.isEnabled() && demo && blocky.size() != 0 && blocky.get(0).id >= LIMIT_BLOCKOV) {
                JOptionPane.showMessageDialog(rootPane, "Ľutujeme, ale na tomto pc ste už zaregistrovali viac ako " + LIMIT_BLOCKOV + " bločkov. \n "
                        + "Pre zakúpenie plnej verzie navštívte stránku registratorblockov.sk");
                pridatButton.setEnabled(false);
                casTextField.setEnabled(false);
            }
            refreshBlockyTable(blocky);
            refreshBlockyTableColors();

        }
        if ("predajcaAdded".equals(evt.getPropertyName())) {
            if (SYSOUT_ON) {
                System.out.println("Pridany predajca");
            }
            predajcovia = database.getPredajcaList();
            refreshPredajcoviaTable(predajcovia);
        }
        if ("loginAdded".equals(evt.getPropertyName())) {
            if (SYSOUT_ON) {
                System.out.println("UPDATNUTY login");
            }
        }
        if ("medzicas".equals(evt.getPropertyName())) {
            if (SYSOUT_ON) {
                System.out.println("novy medzicas: " + evt.getNewValue());
            }
            if (evt.getNewValue() instanceof Integer) {
                // System.out.println("novy medzicas " + evt.getNewValue());
                int medzicas = (Integer) evt.getNewValue();
                poslednyCas += medzicas;
            } else {
                System.out.println("medzicas nie je integer");
            }

        }
        if ("secondAdded".equals(evt.getPropertyName())) {
            // System.out.println("secondAdded property change");
            int hodin = (int) ((System.currentTimeMillis() - startPridavanieCas + poslednyCas) / (1000 * 3600));
            int minut = (int) ((System.currentTimeMillis() - startPridavanieCas + poslednyCas) / (1000 * 60));
            int sekund = (int) ((System.currentTimeMillis() - startPridavanieCas + poslednyCas) / (1000));
            sekund %= 60;
            minut %= 60;

            String hodinString = "" + hodin;
            if (hodin < 10) {
                hodinString = "0" + hodin;
            }
            String minutString = "" + minut;
            if (minut < 10) {
                minutString = "0" + minut;
            }
            String sekundString = "" + sekund;
            if (sekund < 10) {
                sekundString = "0" + sekund;
            }
            cas = sekund + minut * 60 + hodin * 3600;
            stopwatchLabel.setText(hodinString + ":" + minutString + ":" + sekundString);
        }
    }

    private void refreshBlockyTable(List<Blocek> blocky) {
        previouslySelectedRow = -1;
// System.out.println("refreshBlockyTable");
        blockyTableModel = new BlockyTableModel(blockyTableColumnNames, blocky);
        nezaregistrovanych = 0;
        for (Blocek b : blocky) {
            if (b.pocet < 3) {
                nezaregistrovanych++;
            }
        }
        idTextField.setText(getMakroIDName());
        aktualneRegistrovanychLabel.setText(Integer.toString(database.getAktualneRegistrovanych()));
        blockovVHreEnterPressed();
        nezaregistrovanychLabel.setText(Integer.toString(nezaregistrovanych));
        zaregistrovanychLabel.setText(Integer.toString(database.getPocetBlockov()));
        blockyTable.setModel(blockyTableModel);
        // blockyTable.setAutoCreateRowSorter(true);
        // {"Source", "Priority", "User Priority", "Hostname", "Alarm Code", "Alarm", "Time", "STATUS", "incident"};
        // id column
        blockyTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        blockyTable.getColumnModel().getColumn(0).setMaxWidth(100);
        // DKP column
        blockyTable.getColumnModel().getColumn(1).setPreferredWidth(270);
        blockyTable.getColumnModel().getColumn(1).setMaxWidth(270);
        // Datum column
        blockyTable.getColumnModel().getColumn(2).setPreferredWidth(270);
        blockyTable.getColumnModel().getColumn(2).setMaxWidth(270);
        // Suma column
        blockyTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        // Pocet column
        blockyTable.getColumnModel().getColumn(4).setPreferredWidth(100);

        //refreshMessageTableColors();
    }

    /**
     * refreshes table row background colors according to type of problem they belong to
     */
    private void refreshBlockyTableColors() {
        int row = 0;
        for (int i = 0; i < blocky.size(); i++) {
            // Blocek blocek = blocky.get(i);
            Object idcko = blockyTable.getValueAt(i, 0);
            Blocek akt = null;
            if (idcko instanceof Integer) {
                int id = (Integer) idcko;
                akt = getAktBlocek(id);
                //System.out.println("vybrany blocek s id: " + akt.id);
                //dkpTextField.setText(akt.dkp);
            } else {
                //System.err.println("chyba castovania prveho column na int (ID)");
                return;
            }
            if (akt.pocet > 2) {
                blockyTableModel.setRowColour(row, Color.red);
            }
            if (akt.pocet == 2) {
                blockyTableModel.setRowColour(row, Color.orange);
            }
            if (akt.pocet < 2) {
                blockyTableModel.setRowColour(row, Color.green);
            }
            row++;

        }
    }

    private void refreshPredajcoviaTable(List<Predajca> predajcovia) {
        // refresh predajcovia counts
        Map<String, Integer> predajcoviaPocty = new HashMap<String, Integer>();
        for (Predajca p : predajcovia) {
            predajcoviaPocty.put(p.dkp, 0);
        }
        //System.out.println(predajcovia);
        //System.out.println(predajcoviaPocty);
        //System.out.println(blocky);
        for (Blocek b : blocky) {
            //System.out.println("zvysujem count predajcu "+b.dkp);
            try {
                predajcoviaPocty.put(b.dkp, predajcoviaPocty.get(b.dkp) + 1);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(rootPane, "vynimka");
                return;
            }
        }
        for (Predajca p : predajcovia) {
            p.pocet = predajcoviaPocty.get(p.dkp);
        }
        Collections.sort(predajcovia);
        Collections.reverse(predajcovia);

        predajcoviaTableModel = new PredajcoviaTableModel(predajcoviaTableColumnNames, predajcovia);
        predajcoviaTable.setModel(predajcoviaTableModel);
        predajcoviaTable.setAutoCreateRowSorter(true);
        // meno column
        predajcoviaTable.getColumnModel().getColumn(0).setPreferredWidth(250);
        predajcoviaTable.getColumnModel().getColumn(0).setMaxWidth(250);
        // DKP column
        predajcoviaTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        predajcoviaTable.getColumnModel().getColumn(1).setMaxWidth(250);
    }

    private boolean jeNovyPredajca(String dkp) {
        for (Predajca p : predajcovia) {
            if (p.dkp.equalsIgnoreCase(dkp)) {
                return false;
            }
        }
        return true;
    }

    private Blocek getAktBlocek(int id) {
        for (Blocek b : blocky) {
            if (b.id == id) {
                return b;
            }
        }
        return null;
    }

    private Predajca getAktPredajca(String dkp) {
        for (Predajca p : predajcovia) {
            if (p.dkp == dkp) {
                return p;
            }
        }
        return null;
    }

    private String getMakroIDName() {
        int max = 0;
        int min = Integer.MAX_VALUE;
        for (Blocek b : blocky) {
            if (b.pocet < 3 && b.id < min) {
                min = b.id;
            }
            if (b.pocet < 3 && b.id > max) {
                max = b.id;
            }
        }

        return min + "to" + max;
    }

    private String getMacAddress(String macAddress) {
        InetAddress ip;
        try {
            ip = InetAddress.getLocalHost();
            //System.out.println("Current IP address : " + ip.getHostAddress());
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = null;
            try {
                mac = network.getHardwareAddress();
            } catch (SocketException socketException) {
                JOptionPane.showMessageDialog(rootPane, "Chyba 5689! Obnovte pripojenie k internetu. \n Ak ste pripojení k internetu a stále dostávate túto chybu, \n kontaktujte administrátora na registratorblockov@registratorblockov.sk");
                System.exit(0);
                return "systemExit";
            }
            if (SYSOUT_ON) {
                System.out.print("Current MAC address : ");
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            System.out.println(sb.toString());
            macAddress = sb.toString();
            if (macAddress.length() > 17) {
                macAddress = "macaddress" + APP_ID;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return macAddress;
    }

    private void checkUsersCount() {
        String hostname = "Unknown";
        try {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
        } catch (UnknownHostException ex) {
            System.out.println("Hostname can not be resolved");
        }
        if (hostname.equalsIgnoreCase("Unknown")) {
            JOptionPane.showMessageDialog(null, "Neznámy používateľ. Program skončí.");
            System.exit(0);
        }

        database.getUsers(hostname);

    }
}
