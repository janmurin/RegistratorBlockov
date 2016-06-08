/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package home.blockova;

import blockova.WaveData;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author Janco1
 */
public class ZaregistrovatForm extends javax.swing.JFrame implements PropertyChangeListener {

    private long startTime;
    private long startPridavanieCas;
    private int cas;
    private int neuspesnychCaptcha;
    private int uspesnychCaptcha;
    private double rychlost;
    private double etaTime;

    private class Macadresa implements Comparable<Macadresa> {

        String macadresa;
        int pocet;

        public Macadresa(String macadresa, int pocet) {
            this.macadresa = macadresa;
            this.pocet = pocet;
        }

        public int compareTo(Macadresa o) {
            return this.pocet - o.pocet;
        }

        @Override
        public String toString() {
            return macadresa + ":" + pocet;
        }

    }

    Database database;
    List<Login> loginy;
    private boolean[] pouziteLoginy = new boolean[3];
    private JsoupClient[] clienti = new JsoupClient[3];
    private JsoupClient aktualnyClient;
    private ArrayList<Uloha> ulohy = new ArrayList<Uloha>();
    private int aktUlohaIdx;
    private Uloha aktualnaUloha;
    ExecutorService es = Executors.newCachedThreadPool();
    private List<Blocek> blocky;
    private String uspesnaRegistraciaText = "Registr&aacute;cia prebehla &uacute;spešne. Registračn&yacute; k&oacute;d ";
    private String alreadyRegisteredText = "Ľutujeme, dan&yacute; doklad už bol registrovan&yacute;.";
    private String wrongDKPText = "Zadan&eacute; č&iacute;slo DKP je nespr&aacute;vne";
    private String spravneCaptchaZada = "<span id=\"M6_cvCaptcha\" class=\"errMsg\">Zadajte pros&iacute;m spr&aacute;vne bezpečnostn&yacute; prvok</span>";
    private int uspesneZaregistrovanych;
    private int vsetkychBlockov;
    private String notLoggedText = "Registr&aacute;cia st&aacute;vky je možn&aacute; len pre registrovan&yacute;ch hr&aacute;čov";
    private String[] registerLogTableColumnNames = {"ID", "Login1", "Login2", "Login3"};
    private List<RegisterLog> registerLogItems;
    private RegisterLogTableModel registerLogTableModel;
    private JPopupMenu Pmenu;
    private JMenuItem menuItem;
    private Authenticator authenticator = new Authenticator();
    private boolean blockyCountUpdated;
    private UrychlovacUpdateRegisterLog urychlovacRegisterLog;
    private UrychlovacUpdateBlocek urychlovacUpdateBlocek;
    private UrychlovacMp3Captcha urychlovacMp3Captcha;
    private WaveData wdata;
    private File mp3Captcha;
    Timer timer = new Timer(0);
    private long period;
    // PARAMETRE
    public static final int LIMIT_BLOCKOV = 50;
    private boolean demo = false;
    private int APP_ID = 6;
    private boolean SYSOUT_ON = false;

    /**
     * Creates new form ZaregistrovatForm
     */
    public ZaregistrovatForm(Database database) {
        initComponents();
        this.database = database;
        database.addPropertyChangeListener(this);
        loginy = database.getLoginList();
        registerLogItems = database.getRegisterLogList();
        Collections.reverse(registerLogItems);
        registerLogTableModel = new RegisterLogTableModel(registerLogTableColumnNames, registerLogItems);
        refreshRegisterLogTable();
        refreshRegisterLogTableColors();
        timer.addPropertyChangeListener(this);

        Login l = loginy.get(0);
        login1TextField.setText(l.meno);
        login1PasswordField.setText(l.heslo);
        l = loginy.get(1);
        login2TextField.setText(l.meno);
        login2PasswordField.setText(l.heslo);
        l = loginy.get(2);
        login3TextField.setText(l.meno);
        login3PasswordField.setText(l.heslo);
        blocky = database.getBlocekList();
        wdata = new WaveData();
        urychlovacMp3Captcha = new UrychlovacMp3Captcha(wdata, "mp3captcha.mp3");
        urychlovacMp3Captcha.addPropertyChangeListener(this);
        urychlovacUpdateBlocek = new UrychlovacUpdateBlocek(database, new Blocek());
        urychlovacRegisterLog = new UrychlovacUpdateRegisterLog(database, notLoggedText, wrongDKPText, wrongDKPText, wrongDKPText, cas);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("window closing action");
                odpojitButtonActionPerformed(null);
                super.windowClosing(e);
            }

        });
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        registerLogTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                RegisterLogTableModel model = (RegisterLogTableModel) table.getModel();
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(model.getCellColour(row, column));

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

        aktualnyClient = new JsoupClient();
        refreshCaptcha();
    }

//    public ZaregistrovatForm() {
//        initComponents();
//        database = new Database();
//        database.addPropertyChangeListener(this);
//        loginy = database.getLoginList();
//        registerLogItems = database.getRegisterLogList();
//        Collections.reverse(registerLogItems);
//        registerLogTableModel = new RegisterLogTableModel(registerLogTableColumnNames, registerLogItems);
//        refreshRegisterLogTable();
//        refreshRegisterLogTableColors();
//
//        Login l = loginy.get(0);
//        login1TextField.setText(l.meno);
//        login1PasswordField.setText(l.heslo);
//        l = loginy.get(1);
//        login2TextField.setText(l.meno);
//        login2PasswordField.setText(l.heslo);
//        l = loginy.get(2);
//        login3TextField.setText(l.meno);
//        login3PasswordField.setText(l.heslo);
//        blocky = database.getBlocekListNoTimestamp();
//
//        addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowClosing(WindowEvent e) {
//                System.out.println("window closing action");
//                odpojitButtonActionPerformed(null);
//                super.windowClosing(e);
//            }
//
//        });
//        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//
//        registerLogTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
//            @Override
//            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//                RegisterLogTableModel model = (RegisterLogTableModel) table.getModel();
//                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//                c.setBackground(model.getCellColour(row, column));
//
//                // Only for specific cell
//                if (isSelected) {
//                    c.setFont(new Font("Tahoma", 1, 12));
//                    // you may want to address isSelected here too
//                    c.setForeground(Color.BLACK);
//                    //c.setBackground(/*special background color*/);
//                }
//                return c;
//            }
//        });
//    }
    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        pripojitCheckBox2 = new javax.swing.JCheckBox();
        pripojitCheckBox3 = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        meno1Label = new javax.swing.JLabel();
        heslo1Label = new javax.swing.JLabel();
        login1PasswordField = new javax.swing.JPasswordField();
        login2TextField = new javax.swing.JTextField();
        login2PasswordField = new javax.swing.JPasswordField();
        heslo2Label = new javax.swing.JLabel();
        meno2Label = new javax.swing.JLabel();
        heslo3Label = new javax.swing.JLabel();
        meno3Label = new javax.swing.JLabel();
        login1TextField = new javax.swing.JTextField();
        login3PasswordField = new javax.swing.JPasswordField();
        login3TextField = new javax.swing.JTextField();
        pripojitCheckBox1 = new javax.swing.JCheckBox();
        pripojitButton = new javax.swing.JButton();
        odpojitButton = new javax.swing.JButton();
        captchaLabel = new javax.swing.JLabel();
        captchaTextField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        logTextArea = new javax.swing.JTextArea();
        jScrollPane1 = new javax.swing.JScrollPane();
        registerLogTable = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        zaregistrovanychLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        rychlostLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        upCasLabel = new javax.swing.JLabel();
        ETACasLabel = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        captchaUspesnostLabel = new javax.swing.JLabel();

        pripojitCheckBox2.setText("použiť");
        pripojitCheckBox2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                pripojitCheckBox2MouseReleased(evt);
            }
        });
        pripojitCheckBox2.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                pripojitCheckBox2PropertyChange(evt);
            }
        });

        pripojitCheckBox3.setText("použiť");
        pripojitCheckBox3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                pripojitCheckBox3MouseReleased(evt);
            }
        });
        pripojitCheckBox3.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                pripojitCheckBox3PropertyChange(evt);
            }
        });

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Zaregistrovať");
        setMinimumSize(new java.awt.Dimension(725, 534));
        setResizable(false);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Zvoľte login"));

        meno1Label.setText("meno:");
        meno1Label.setEnabled(false);

        heslo1Label.setText("heslo:");
        heslo1Label.setEnabled(false);

        login1PasswordField.setText("jPasswordField1");
        login1PasswordField.setEnabled(false);

        login2TextField.setEnabled(false);
        login2TextField.setPreferredSize(new java.awt.Dimension(126, 22));

        login2PasswordField.setText("jPasswordField1");
        login2PasswordField.setEnabled(false);

        heslo2Label.setText("heslo:");
        heslo2Label.setEnabled(false);

        meno2Label.setText("meno:");
        meno2Label.setEnabled(false);

        heslo3Label.setText("heslo:");
        heslo3Label.setEnabled(false);

        meno3Label.setText("meno:");
        meno3Label.setEnabled(false);

        login1TextField.setEnabled(false);
        login1TextField.setPreferredSize(new java.awt.Dimension(126, 22));

        login3PasswordField.setText("jPasswordField1");
        login3PasswordField.setEnabled(false);

        login3TextField.setEnabled(false);
        login3TextField.setPreferredSize(new java.awt.Dimension(126, 22));

        pripojitCheckBox1.setText("použiť");
        pripojitCheckBox1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                pripojitCheckBox1MouseReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(heslo1Label)
                    .addComponent(meno1Label))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(login1TextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(login1PasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(meno2Label)
                            .addComponent(heslo2Label)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(pripojitCheckBox1)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(login2PasswordField, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                    .addComponent(login2TextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(meno3Label)
                    .addComponent(heslo3Label))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(login3TextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(login3PasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(pripojitCheckBox1)
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(meno2Label)
                            .addComponent(login1TextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(login2TextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(login2PasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(login1PasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(heslo2Label)
                            .addComponent(heslo1Label)))
                    .addComponent(meno1Label)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(meno3Label)
                        .addComponent(login3TextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(login3PasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(heslo3Label))))
                .addGap(34, 34, 34))
        );

        pripojitButton.setText("Pripojiť");
        pripojitButton.setEnabled(false);
        pripojitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pripojitButtonActionPerformed(evt);
            }
        });

        odpojitButton.setText("Odpojiť");
        odpojitButton.setEnabled(false);
        odpojitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                odpojitButtonActionPerformed(evt);
            }
        });

        captchaLabel.setEnabled(false);

        captchaTextField.setEnabled(false);
        captchaTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                captchaTextFieldKeyReleased(evt);
            }
        });

        jLabel7.setText("captcha text:");
        jLabel7.setEnabled(false);

        logTextArea.setColumns(20);
        logTextArea.setLineWrap(true);
        logTextArea.setRows(5);
        logTextArea.setEnabled(false);
        jScrollPane2.setViewportView(logTextArea);

        registerLogTable.setModel(new javax.swing.table.DefaultTableModel(
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
        registerLogTable.setRowSelectionAllowed(false);
        registerLogTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                registerLogTableMouseReleased(evt);
            }
        });
        jScrollPane1.setViewportView(registerLogTable);

        jLabel1.setText("Zaregistrovanych:");

        zaregistrovanychLabel.setText("jLabel2");

        jLabel2.setText("Rýchlosť:");

        rychlostLabel.setText("jLabel3");

        jLabel3.setText("Čas:");

        jLabel4.setText("ETA:");

        jLabel5.setText("UP");

        upCasLabel.setText("jLabel6");

        ETACasLabel.setText("jLabel6");

        jLabel6.setText("Captcha uspesnost:");

        captchaUspesnostLabel.setText("c");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(29, 29, 29)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(rychlostLabel)
                                    .addComponent(zaregistrovanychLabel)))
                            .addComponent(jLabel3))
                        .addContainerGap(36, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(upCasLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(ETACasLabel))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel4)))
                        .addGap(25, 25, 25))))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(captchaUspesnostLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(zaregistrovanychLabel)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rychlostLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addGap(18, 18, 18)
                .addComponent(captchaUspesnostLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel4))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(upCasLabel)
                    .addComponent(ETACasLabel))
                .addGap(21, 21, 21))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(pripojitButton)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(odpojitButton))
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(7, 7, 7)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(jLabel7)
                                            .addGap(18, 18, 18)
                                            .addComponent(captchaTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(captchaLabel))))
                            .addGap(18, 18, 18)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 675, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(pripojitButton)
                                    .addComponent(odpojitButton))
                                .addGap(18, 18, 18)
                                .addComponent(captchaLabel)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(captchaTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel7)))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addGap(27, 27, 27)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void pripojitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pripojitButtonActionPerformed

        // zapnut captcha a povypinat loginy a checkboxy
        captchaLabel.setEnabled(true);
        captchaTextField.setEnabled(true);
        logTextArea.setEnabled(true);
        pripojitCheckBox1.setEnabled(false);
        pripojitCheckBox2.setEnabled(false);
        pripojitCheckBox3.setEnabled(false);
        pripojitButton.setEnabled(false);
        odpojitButton.setEnabled(true);
        meno1Label.setEnabled(false);
        heslo1Label.setEnabled(false);
        login1TextField.setEnabled(false);
        login1PasswordField.setEnabled(false);
        meno2Label.setEnabled(false);
        heslo2Label.setEnabled(false);
        login2TextField.setEnabled(false);
        login2PasswordField.setEnabled(false);
        meno3Label.setEnabled(false);
        heslo3Label.setEnabled(false);
        login3TextField.setEnabled(false);
        login3PasswordField.setEnabled(false);
        // zapnut clienta 1
        if (pouziteLoginy[0]) {
            if (login1TextField.getText().equalsIgnoreCase("")) {
                JOptionPane.showMessageDialog(rootPane, "Prázdny reťazec v položke meno.");
                return;
            }
            database.updateLoginFromDB(login1TextField.getText(), String.valueOf(login1PasswordField.getPassword()), 1);
            if (SYSOUT_ON) {
                System.out.println("pripajam na " + login1TextField.getText() + " " + String.valueOf(login1PasswordField.getPassword()));
            }
            //pripojit(login1TextField.getText(), String.valueOf(login1PasswordField.getPassword()));
            clienti[0] = new JsoupClient();
            clienti[0].EMAIL = login1TextField.getText();
            clienti[0].PASSWORD = String.valueOf(login1PasswordField.getPassword());
            ulohy.add(new Uloha(0, 0));
        }
        // zapnut clienta 2
        if (pouziteLoginy[1]) {
            if (login2TextField.getText().equalsIgnoreCase("")) {
                JOptionPane.showMessageDialog(rootPane, "Prázdny reťazec v položke meno.");
                return;
            }
            database.updateLoginFromDB(login2TextField.getText(), String.valueOf(login2PasswordField.getPassword()), 2);
            if (SYSOUT_ON) {
                System.out.println("pripajam na " + login2TextField.getText() + " " + String.valueOf(login2PasswordField.getPassword()));
            }
            //pripojit(login2TextField.getText(), String.valueOf(login2PasswordField.getPassword()));
            clienti[1] = new JsoupClient();
            clienti[1].EMAIL = login2TextField.getText();
            clienti[1].PASSWORD = String.valueOf(login2PasswordField.getPassword());
            ulohy.add(new Uloha(1, 0));
        }
        // zapnut clienta 3
        if (pouziteLoginy[2]) {
            // treti login selected
            if (login3TextField.getText().equalsIgnoreCase("")) {
                JOptionPane.showMessageDialog(rootPane, "Prázdny reťazec v položke meno.");
                return;
            }
            database.updateLoginFromDB(login3TextField.getText(), String.valueOf(login3PasswordField.getPassword()), 3);
            if (SYSOUT_ON) {
                System.out.println("pripajam na " + login3TextField.getText() + " " + String.valueOf(login3PasswordField.getPassword()));
            }
            //pripojit(login3TextField.getText(), String.valueOf(login3PasswordField.getPassword()));
            clienti[2] = new JsoupClient();
            clienti[2].EMAIL = login3TextField.getText();
            clienti[2].PASSWORD = String.valueOf(login3PasswordField.getPassword());
            ulohy.add(new Uloha(2, 0));
        }
        pripojitNovehoClienta();
    }//GEN-LAST:event_pripojitButtonActionPerformed

    private void odpojitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_odpojitButtonActionPerformed
        updatePoctyVTabulkeBlockov();
        if (!blockyCountUpdated) {
            // updatneme kolko blockov sme zaregistrovali
            // pre pripad ked zaregistrujeme blocky a zavrieme nahle okno
            String macaddress = "unknownmac";
            if (demo) {
                authenticator.increaseAndReturnPocet(getMacAddress(macaddress), 1, uspesneZaregistrovanych);
            } else {
                authenticator.increaseAndReturnPocet(getMacAddress(macaddress), APP_ID, uspesneZaregistrovanych);
            }
            blockyCountUpdated = true;
        }
        if (SYSOUT_ON) {
            System.out.println("odpojit button action");
        }
        registerLogItems = database.getRegisterLogList();
        //System.out.println(blocky.get(0));
        Collections.reverse(registerLogItems);
        //System.out.println(blocky.get(0));
        refreshRegisterLogTable();
        refreshRegisterLogTableColors();
        //System.out.println(Arrays.toString(clienti));

        // pozapiname loginy a checkboxy
        pripojitButton.setEnabled(true);
        odpojitButton.setEnabled(false);
        logTextArea.append(vypisCas() + ": odhlaseny uzivatel \n");
        captchaLabel.setEnabled(false);
        captchaTextField.setEnabled(false);
        logTextArea.setEnabled(false);
        pripojitCheckBox1.setEnabled(true);
        pripojitCheckBox2.setEnabled(true);
        pripojitCheckBox3.setEnabled(true);
        if (pripojitCheckBox1.isSelected()) {
            meno1Label.setEnabled(true);
            heslo1Label.setEnabled(true);
            login1TextField.setEnabled(true);
            login1PasswordField.setEnabled(true);
            if (clienti[0] != null && clienti[0].isLogged()) {
                logTextArea.append(vypisCas() + ": odhlasujem uzivatela " + clienti[0].EMAIL + "\n");
                if (SYSOUT_ON) {
                    System.out.println(vypisCas() + ": odhlasujem uzivatela " + clienti[0].EMAIL + "\n");
                }
                clienti[0].logout();
            }
        }
        if (pripojitCheckBox2.isSelected()) {
            meno2Label.setEnabled(true);
            heslo2Label.setEnabled(true);
            login2TextField.setEnabled(true);
            login2PasswordField.setEnabled(true);
            if (clienti[1] != null && clienti[1].isLogged()) {
                logTextArea.append(vypisCas() + ": odhlasujem uzivatela " + clienti[1].EMAIL + "\n");
                if (SYSOUT_ON) {
                    System.out.println(vypisCas() + ": odhlasujem uzivatela " + clienti[1].EMAIL + "\n");
                }
                clienti[1].logout();
            }
        }
        if (pripojitCheckBox3.isSelected()) {
            meno3Label.setEnabled(true);
            heslo3Label.setEnabled(true);
            login3TextField.setEnabled(true);
            login3PasswordField.setEnabled(true);
            if (clienti[2] != null && clienti[2].isLogged()) {
                logTextArea.append(vypisCas() + ": odhlasujem uzivatela " + clienti[2].EMAIL + "\n");
                if (SYSOUT_ON) {
                    System.out.println(vypisCas() + ": odhlasujem uzivatela " + clienti[2].EMAIL + "\n");
                }
                clienti[2].logout();
            }
        }
//        client = new JsoupClient();
//        refreshCaptcha();
        ulohy = new ArrayList<Uloha>();
    }//GEN-LAST:event_odpojitButtonActionPerformed

    private void pripojitCheckBox2PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_pripojitCheckBox2PropertyChange

    }//GEN-LAST:event_pripojitCheckBox2PropertyChange

    private void pripojitCheckBox3PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_pripojitCheckBox3PropertyChange

    }//GEN-LAST:event_pripojitCheckBox3PropertyChange

    private void pripojitCheckBox2MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pripojitCheckBox2MouseReleased
        if (pripojitCheckBox2.isSelected()) {
            meno2Label.setEnabled(true);
            heslo2Label.setEnabled(true);
            login2TextField.setEnabled(true);
            login2PasswordField.setEnabled(true);
            pouziteLoginy[1] = true;
            pripojitButton.setEnabled(true);
            pripojitCheckBox1.setSelected(false);
            pripojitCheckBox1MouseReleased(evt);
            pripojitCheckBox3.setSelected(false);
            pripojitCheckBox3MouseReleased(evt);
        } else {
            meno2Label.setEnabled(false);
            heslo2Label.setEnabled(false);
            login2TextField.setEnabled(false);
            login2PasswordField.setEnabled(false);
            pouziteLoginy[1] = false;
            if (!pouziteLoginy[0] && !pouziteLoginy[1] && !pouziteLoginy[2]) {
                pripojitButton.setEnabled(false);
            }
        }
    }//GEN-LAST:event_pripojitCheckBox2MouseReleased

    private void pripojitCheckBox3MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pripojitCheckBox3MouseReleased
        if (pripojitCheckBox3.isSelected()) {
            meno3Label.setEnabled(true);
            heslo3Label.setEnabled(true);
            login3TextField.setEnabled(true);
            login3PasswordField.setEnabled(true);
            pouziteLoginy[2] = true;
            pripojitButton.setEnabled(true);
            pripojitCheckBox1.setSelected(false);
            pripojitCheckBox1MouseReleased(evt);
            pripojitCheckBox2.setSelected(false);
            pripojitCheckBox2MouseReleased(evt);
        } else {
            meno3Label.setEnabled(false);
            heslo3Label.setEnabled(false);
            login3TextField.setEnabled(false);
            login3PasswordField.setEnabled(false);
            pouziteLoginy[2] = false;
            if (!pouziteLoginy[0] && !pouziteLoginy[1] && !pouziteLoginy[2]) {
                pripojitButton.setEnabled(false);
            }
        }
    }//GEN-LAST:event_pripojitCheckBox3MouseReleased

    private void pripojitCheckBox1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pripojitCheckBox1MouseReleased
        if (pripojitCheckBox1.isSelected()) {
            meno1Label.setEnabled(true);
            heslo1Label.setEnabled(true);
            login1TextField.setEnabled(true);
            login1PasswordField.setEnabled(true);
            pouziteLoginy[0] = true;
            pripojitButton.setEnabled(true);
            pripojitCheckBox2.setSelected(false);
            pripojitCheckBox2MouseReleased(evt);
            pripojitCheckBox3.setSelected(false);
            pripojitCheckBox3MouseReleased(evt);
        } else {
            meno1Label.setEnabled(false);
            heslo1Label.setEnabled(false);
            login1TextField.setEnabled(false);
            login1PasswordField.setEnabled(false);
            pouziteLoginy[0] = false;
            if (!pouziteLoginy[0] && !pouziteLoginy[1] && !pouziteLoginy[2]) {
                pripojitButton.setEnabled(false);
            }
        }
    }//GEN-LAST:event_pripojitCheckBox1MouseReleased

    private void captchaTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_captchaTextFieldKeyReleased
        int key = evt.getKeyCode();
        if (key == KeyEvent.VK_ENTER) {
            captchaTextFieldEnterPressed(evt);
        }

    }

    private boolean captchaTextFieldEnterPressed(KeyEvent evt) throws HeadlessException {
        // captchatextfield je spustacom vsetkych eventov

        long startTime = System.currentTimeMillis();
        if (SYSOUT_ON) {
            System.out.println("start enter button time: 0 ");
        }
        // zistime aky typ ulohy riesime
        if (aktualnaUloha.cisloBlocka == 0) {
//=======================================L O G I N=========================================================================                
            // prihlasime aktualneho clienta
            Document loginResponseDocument = aktualnyClient.login(captchaTextField.getText());
            if (aktualnyClient.isLogged()) {
                // urobime kontrolu proti zlym demo uzivatelom
                if (demo) {
                    InetAddress ip;
                    String macAddress = "unknownMac";
                    macAddress = getMacAddress(macAddress);
                    try {
                        Document authResponse = authenticator.increaseAndReturnPocet(macAddress, 1, 0); // 1 je pre demo verziu, 0 lebo len chceme zistit pocet registracii
                        if (SYSOUT_ON) {
                            System.out.println(authResponse);
                        }
                        // naparsovat cislo
                        String text = authResponse.toString();
                        String pocetText = "id=\"pocetInput\" class=\"txtBoxElem long\" value=";
                        int prvaUvodzovkaPos = text.indexOf(pocetText) + pocetText.length() + 1;
                        int druhaUvodzovkaPos = text.substring(prvaUvodzovkaPos).indexOf("\"") + prvaUvodzovkaPos;
                        int pocetRegistracii = Integer.parseInt(text.substring(prvaUvodzovkaPos, druhaUvodzovkaPos));
                        if (SYSOUT_ON) {
                            System.out.println("value: " + pocetRegistracii);
                        }
                        if (pocetRegistracii >= LIMIT_BLOCKOV * 3) {
                            JOptionPane.showMessageDialog(rootPane, "Ľutujeme, ale na tomto pc ste už zaregistrovali viac ako " + LIMIT_BLOCKOV + " bločkov. \n "
                                    + "Pre zakúpenie plnej verzie navštívte stránku registratorblockov.sk");
                            // tu musi byt tento refresh inac sa nepodari odhlasit
                            System.out.println("prekroceny limit blockov REFRESH CAPTCHA");
                            refreshCaptcha();
                            captchaTextField.setText("");
                            blockyCountUpdated = true;
                            odpojitButtonActionPerformed(null);
                            return true;
                        }
                    } catch (Exception exception) {
                        JOptionPane.showMessageDialog(rootPane, "Vyskytla sa chyba. Skontrolujte vaše internetové pripojenie alebo \n skúste bločky zaregistrovať neskôr, alebo kontaktuje administrátora.");
                        logTextArea.append(vypisCas() + ": " + exception.getStackTrace()[0]);
                        // tu musi byt tento refresh inac sa nepodari odhlasit
                        System.out.println("demo nejde pripojit na stranku REFRESH CAPTCHA");
                        refreshCaptcha();
                        captchaTextField.setText("");
                        blockyCountUpdated = true;
                        odpojitButtonActionPerformed(null);
                        return true;
                    }
                } else {
                    // nie sme DEMO
                    InetAddress ip;
                    String nasaMacadresa = "unknownMac";
                    nasaMacadresa = getMacAddress(nasaMacadresa);
                    try {
                        Document authResponse = authenticator.getMacAddressCountForAppid(APP_ID); // kazdemu osobitne buildnem s inym appid
                        if (SYSOUT_ON) {
                            System.out.println(authResponse);
                        }
                        // naparsovat cislo
                        String text = authResponse.toString();
                        String pocetText = "pocet:";
                        int prvaUvodzovkaPos = text.indexOf(pocetText) + pocetText.length();
                        int druhaUvodzovkaPos = text.substring(prvaUvodzovkaPos).indexOf("<") + prvaUvodzovkaPos;
                        int pocetNacitanychMacadries = Integer.parseInt(text.substring(prvaUvodzovkaPos, druhaUvodzovkaPos));
                        if (SYSOUT_ON) {
                            System.out.println("pocet mac adries sme nacitali ako: " + pocetNacitanychMacadries);
                        }
                        if (pocetNacitanychMacadries > 0) {
                            // vzdy pocet je >0 lebo z aktualnej adresy sme sa uz v tejto chvili zaregistrovali ked sa spustil program
                            // nacitame macadresy
                            List<Macadresa> macadresy = new ArrayList<Macadresa>();
                            for (int i = 0; i < pocetNacitanychMacadries; i++) {
                                pocetText = "macaddress:";
                                prvaUvodzovkaPos = text.indexOf(pocetText) + pocetText.length();
                                druhaUvodzovkaPos = text.substring(prvaUvodzovkaPos).indexOf("<") + prvaUvodzovkaPos;
                                String meno = text.substring(prvaUvodzovkaPos, druhaUvodzovkaPos);
                                pocetText = "count:";
                                prvaUvodzovkaPos = text.indexOf(pocetText) + pocetText.length();
                                druhaUvodzovkaPos = text.substring(prvaUvodzovkaPos).indexOf("<") + prvaUvodzovkaPos;
                                int pocetRegistracii = Integer.parseInt(text.substring(prvaUvodzovkaPos, druhaUvodzovkaPos));
                                macadresy.add(new Macadresa(meno, pocetRegistracii));
                                text = text.substring(druhaUvodzovkaPos);
                            }
                            if (SYSOUT_ON) {
                                System.out.println("nacitane macadresy: " + macadresy);
                            }
                            if (pocetNacitanychMacadries <= 2) {
                                // mozeme sa stat 3 macadresou ktora bude na tomto appid registrovat blocky
                                // spravime jednoduchy insert
                                // nie je to nutne potrebne kedze sme uz zaregistrovani po spusteni programu ak sme vtedy boli pripojeni
                                authResponse = authenticator.increaseAndReturnPocet(nasaMacadresa, APP_ID, 0);
                            }
                            if (pocetNacitanychMacadries > 2) {
                                // mame 3 a viac macadries na jedno appid, treba zistit ci sme macadresa, ktora ma nula registracii
                                // a v zozname su 3 macadresy ktore maju aspon 1 registraciu
                                // prechadzame zoznam a ak sme macadresa s aspon 1 registraciou, tak dovolime HNED registrovat
                                // ak po prejdeni napocitame 3 macadresy s aspon 1 registraciou a my mame nulu, tak koncime
                                boolean mameAsponJednuRegistraciu = false;
                                int countPositive = 0;
                                for (Macadresa ma : macadresy) {
                                    if (ma.pocet > 0) {
                                        countPositive++;
                                    }
                                    if (ma.macadresa.equalsIgnoreCase(nasaMacadresa) && ma.pocet > 0) {
                                        mameAsponJednuRegistraciu = true;
                                        break;
                                    }
                                }
                                // TODO: moze mat vela uzivatelov unknownMacaddress?
                                if (mameAsponJednuRegistraciu) {
                                } else {
                                    if (countPositive >= 3) {
                                        // mame nula registracii a nasli sa 3 macadresy s 1 registraciou ktore nie su ja
                                        // teoreticky mozu byt 4 a viac nenulove macadresy ak manualne im v DB zvysim pocet registracii
                                        JOptionPane.showMessageDialog(rootPane, "Ľutujeme, ale tento program nemôžete použiť na viac ako 3 pc. \n "
                                                + "Pre zakúpenie programu na ďalšie pc navštívte stránku registratorblockov.sk");
                                        // tu musi byt tento refresh inac sa nepodari odhlasit
                                        System.out.println("prihlasenie z viac ako 3 macadries stop REFRESH CAPTCHA");
                                        refreshCaptcha();
                                        captchaTextField.setText("");
                                        blockyCountUpdated = true;
                                        odpojitButtonActionPerformed(null);
                                        return true;
                                    } else {
                                        // mame nula registracii a pozitivnych je menej ako 3
                                        // takze my budeme teraz treti
                                        // mame nula registracii a pozitivnych je menej ako 3
                                        // takze my budeme teraz treti
                                    }
                                }
                            }
                        } else {
                            // pocet==0
                            // toto by nemalo nikdy nastat, lebo pri spustani sa prihlasujeme a zistujeme pocet registracii
                            Document d = authResponse = authenticator.increaseAndReturnPocet(nasaMacadresa, APP_ID, 0);
                            if (SYSOUT_ON) {
                                System.out.println(d);
                            }
                        }
                    } catch (Exception exception) {
                        JOptionPane.showMessageDialog(rootPane, "Vyskytla sa chyba. Skúste bločky zaregistrovať neskôr, alebo kontaktuje administrátora.");
                        logTextArea.append(vypisCas() + ": " + exception.getStackTrace()[0]);
                        // tu musi byt tento refresh inac sa nepodari odhlasit
                        
                        captchaTextField.setText("");
                        blockyCountUpdated = true;
                        odpojitButtonActionPerformed(null);
                        System.out.println("nepodarilo sa zistit pocty z webu REFRESH CAPTCHA");
                        refreshCaptcha();
                        return true;
                    }
                }
                //JOptionPane.showMessageDialog(this, "Successfully logged in");
                logTextArea.append(vypisCas() + ": prihlaseny uzivatel " + aktualnyClient.EMAIL + "\n");
                // uspesne prihlasenie
                // automaticky popridavame do dalsich uloh registrovanie blockov ktore este nemame v aktualnom logine prihlasene
                List<RegisterLog> neregistrovaneBlocky = database.getNotRegisteredLogList();
                if (SYSOUT_ON) {
                    System.out.println("neregistrovane blocky: " + neregistrovaneBlocky);
                    System.out.println("count: " + neregistrovaneBlocky.size());
                }
                for (RegisterLog rl : neregistrovaneBlocky) {
                    if (rl.login1.equalsIgnoreCase(aktualnyClient.EMAIL) || rl.login2.equalsIgnoreCase(aktualnyClient.EMAIL) || rl.login3.equalsIgnoreCase(aktualnyClient.EMAIL)) {
                        // aktualny blocek uz bol registrovany v aktualnom logine
                        //System.out.println("aktualny client: " + aktualnyClient.EMAIL + " uz ma registrovany blocek s ID: " + rl.blocek_id);
                        continue;
                    }
                    if (rl.login1.equalsIgnoreCase("")) {
                        // login 1 je prazdny takze blocek pre aktualneho clienta neni zaregistrovany
                        ulohy.add(new Uloha(aktualnaUloha.cisloClienta, rl.blocek_id, "login1", "status1"));
                        vsetkychBlockov++;
                        continue;
                    }
                    if (rl.login2.equalsIgnoreCase("")) {
                        // login 2 je prazdny takze blocek pre aktualneho clienta neni zaregistrovany
                        ulohy.add(new Uloha(aktualnaUloha.cisloClienta, rl.blocek_id, "login2", "status2"));
                        vsetkychBlockov++;
                        continue;
                    }
                    if (rl.login3.equalsIgnoreCase("")) {
                        // login 3 je prazdny takze blocek pre aktualneho clienta neni zaregistrovany
                        ulohy.add(new Uloha(aktualnaUloha.cisloClienta, rl.blocek_id, "login3", "status3"));
                        vsetkychBlockov++;
                        continue;
                    }
                }
                //ulohy.add(new Uloha(0, 1));
                // pripravime si captcha pre dalsiu ulohu
                aktUlohaIdx++;
                if (aktUlohaIdx >= ulohy.size()) {
                    JOptionPane.showMessageDialog(rootPane, "Nenasli sa ziadne nezaregistrovane blocky.");
                    // tu musi byt tento refresh inac sa nepodari odhlasit
                    System.out.println("po prihlaseni ziadne blocky na registrovanie REFRESH CAPTCHA");
                    refreshCaptcha();
                    captchaTextField.setText("");
                    blockyCountUpdated = true;
                    odpojitButtonActionPerformed(null);
                    return true;
                } else {
                    // este mame nejake ulohy
                    aktualnaUloha = ulohy.get(aktUlohaIdx);
                    System.out.println("po pridani uloh REFRESH CAPTCHA");
                    refreshCaptcha();
                    if (aktualnaUloha.cisloBlocka == 0) {
                        // toto by nemalo nastat lebo vzdy budem riesit iba jedneho clienta
                        logTextArea.append(vypisCas() + ": prihlasujem uzivatela " + aktualnyClient.EMAIL + "\n");
                    } else {
                        logTextArea.append(vypisCas() + ": registrujem blocek " + aktualnaUloha.cisloBlocka + " uzivatela " + aktualnyClient.EMAIL + "\n");
                    }
                }
            } else {
                // nepodarilo sa prihlasit
                // check zly email a heslo
                // check zla captcha
                Elements badLoginData = parseElement(loginResponseDocument, "div#M7_pnlMsg div.errorMsg p");
                if ((badLoginData != null) && (badLoginData.size() > 0)) {
                    if (SYSOUT_ON) {
                        System.out.println("Bad login data error:");
                    }
                    logTextArea.append(vypisCas() + ": nespravne prihlasovacie udaje " + aktualnyClient.EMAIL + "\n");
                    JOptionPane.showMessageDialog(this, "Prihlásenie nebolo úspešné. Uistite sa, že ste zadali správne meno a heslo. Ak ste nedostali e-mail s odkazom na dokončenie registrácie, zaregistrujte sa prosím znova!");
                    captchaTextField.setText("");
                    blockyCountUpdated = true;
                    odpojitButtonActionPerformed(null);
                    return true;
                } else {
                    if (SYSOUT_ON) {
                        System.out.println("bad login captcha text");
                    }
                    logTextArea.append(vypisCas() + ": zly captcha text \n");
                    //JOptionPane.showMessageDialog(this, "Zadajte prosím správne bezpečnostný prvok!");
                }
                //logTextArea.append(vypisCas() + ": neuspesne prihlasenie uzivatela " + aktualnyClient.EMAIL + "\n");
                System.out.println("bad login REFRESH CAPTCHA");
                refreshCaptcha();
                logTextArea.append(vypisCas() + ": prihlasujem uzivatela " + aktualnyClient.EMAIL + "\n");
            }
        } else {
//===================================R E G I S T R A C I A====================================================================                
            boolean isBadCaptcha = false;
            // TODO zaregistrovat blocek a vypisat odpoved
            // check logged in status TODOT TODO
            Blocek aktualnyBlocek = getAktBlocek(aktualnaUloha.cisloBlocka);
            if (SYSOUT_ON) {
                System.out.println("zizkany aktualny blocek: " + (System.currentTimeMillis() - startTime));
                //Blocek aktualnyBlocek = new Blocek("7031047819542002", "2014-07-14 12:03:00.000", 2.2, 0);
                System.out.println("REGISTRUJEM BLOCEK: " + aktualnaUloha);
            }
            Document registerDocumentResponse;
            try {
                registerDocumentResponse = aktualnyClient.register(aktualnyBlocek, captchaTextField.getText());
                registerDocumentResponse.outputSettings(new Document.OutputSettings().charset("UTF8"));
            } catch (IOException ex) {
                //Logger.getLogger(ZaregistrovatForm.class.getName()).log(Level.SEVERE, null, ex);
                refreshCaptcha();
                return false;
            }

            if (SYSOUT_ON) {
                System.out.println("zizkany response od servera: " + (System.currentTimeMillis() - startTime));
            }
            System.out.println("PERIOD REGISTERED BLOCEK TIME: " + (System.currentTimeMillis() - period));
            //System.out.println(registerDocumentResponse);
            Elements ok = parseElement(registerDocumentResponse, "div#M6_pnlRegistered div.errorMsg.ok p strong");
            Elements error = parseElement(registerDocumentResponse, "div#M6_pnlRegistered div.errorMsg p");
            Elements badCaptcha = parseElement(registerDocumentResponse, "span[style=display:none;]#M6_cvCaptcha");
            // check uspesna registracia
            if ((ok != null) && (ok.size() > 0)) {
                if (SYSOUT_ON) {
                    System.out.println("Registracia uspesna:");
                }
                // vystupny text
                String text = ok.first().html();
                String[] zlozkyTextu = text.split(" ");
                String dbText = "Registrácia prebehla úspešne. Registračný kód " + zlozkyTextu[5] + " DKP " + zlozkyTextu[7] + " dátum a čas vyhotovenia " + zlozkyTextu[12] + " " + zlozkyTextu[13] + " suma " + zlozkyTextu[15] + " EUR, dátum žrebovania " + zlozkyTextu[19] + " overovací kód " + zlozkyTextu[22];
                if (SYSOUT_ON) {
                    System.out.println("do databasy vlozit: " + dbText);
                }
                logTextArea.append(vypisCas() + ": uspesna registracia, overovaci kod:  " + zlozkyTextu[22] + "\n");
                //database.updateRegisterLogFromDB(aktualnaUloha.akyLogin, aktualnyClient.EMAIL, aktualnaUloha.akyStatus, dbText, (int) aktualnaUloha.cisloBlocka);
                //urychlovacRegisterLog = new UrychlovacUpdateRegisterLog(database, aktualnaUloha.akyLogin, aktualnyClient.EMAIL, aktualnaUloha.akyStatus, dbText, (int) aktualnaUloha.cisloBlocka);
                urychlovacRegisterLog.akyLogin = aktualnaUloha.akyLogin;
                urychlovacRegisterLog.EMAIL = aktualnyClient.EMAIL;
                urychlovacRegisterLog.akyStatus = aktualnaUloha.akyStatus;
                urychlovacRegisterLog.dbText = dbText;
                urychlovacRegisterLog.i = (int) aktualnaUloha.cisloBlocka;
                es.execute(urychlovacRegisterLog);
                if (SYSOUT_ON) {
                    System.out.println("registracna sprava do DB: " + (System.currentTimeMillis() - startTime));
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
                aktualnyBlocek.timeMakroGenerated = sdf.format(new Date(System.currentTimeMillis())).toString();
                if (SYSOUT_ON) {
                    System.out.println("cas registracie: " + aktualnyBlocek.timeMakroGenerated);
                }
                //database.updateBlocekFromDB(aktualnyBlocek);
                //urychlovacUpdateBlocek = new UrychlovacUpdateBlocek(database, aktualnyBlocek);
                urychlovacUpdateBlocek.blocek = aktualnyBlocek;
                es.execute(urychlovacUpdateBlocek);
                if (SYSOUT_ON) {
                    System.out.println("cas o registracii blocku zaznamenany: " + (System.currentTimeMillis() - startTime));
                }
                uspesneZaregistrovanych++;
                uspesnychCaptcha++;
            }
            //System.out.println("PERIOD REGISTERED BLOCEK after check success time: " + (System.currentTimeMillis() - period));
            // check ALREADY registracia
            if ((error != null) && (error.size() > 0)) {
                // nenasla sa ziadna okienkova error notifikacia
                // if (false){
                String text = error.first().html();
                if (text.startsWith(alreadyRegisteredText)) {
                    // System.err.println("Registracia neuspesna:");
                    //System.err.println(text);
                    System.out.println("ALREADY REGISTERED");
                    logTextArea.append(vypisCas() + ": blocek uz bol zaregistrovany \n");
                    // ak dany blocek este nema zaznam v register logu tak sa tam napise ALREADY status, ak uz ma zaznam tak by tam mal byt overovaci kod
                    RegisterLog rlog = database.getNotRegisteredLogList(aktualnyBlocek.id);
                    if (rlog.login1.equalsIgnoreCase(aktualnyClient.EMAIL) || rlog.login2.equalsIgnoreCase(aktualnyClient.EMAIL) || rlog.login3.equalsIgnoreCase(aktualnyClient.EMAIL)) {
                        // uz tam je zaznam o aktualnom blocku
                    } else {
                        // este tam neni zaznam o aktualnom blocku
                        //database.updateRegisterLogFromDB(aktualnaUloha.akyLogin, aktualnyClient.EMAIL, aktualnaUloha.akyStatus, "Ľutujeme, daný doklad už bol registrovaný.", (int) aktualnaUloha.cisloBlocka);
                        urychlovacRegisterLog.akyLogin = aktualnaUloha.akyLogin;
                        urychlovacRegisterLog.EMAIL = aktualnyClient.EMAIL;
                        urychlovacRegisterLog.akyStatus = aktualnaUloha.akyStatus;
                        urychlovacRegisterLog.dbText = "Ľutujeme, daný doklad už bol registrovaný.";
                        urychlovacRegisterLog.i = (int) aktualnaUloha.cisloBlocka;
                        es.execute(urychlovacRegisterLog);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
                        aktualnyBlocek.timeMakroGenerated = sdf.format(new Date(System.currentTimeMillis())).toString();
                        //database.updateBlocekFromDB(aktualnyBlocek);
                        urychlovacUpdateBlocek.blocek = aktualnyBlocek;
                        es.execute(urychlovacUpdateBlocek);
                        //System.out.println("PERIOD REGISTERED BLOCEK after ALREADY urychlovace time: " + (System.currentTimeMillis() - period));
                    }
                    uspesnychCaptcha++;
                }
                if (text.startsWith(wrongDKPText)) {
                    //System.err.println("Registracia neuspesna:");
                    //System.err.println(text);
                    System.out.println("WRONG DKP");
                    logTextArea.append(vypisCas() + ": zle zadane dkp \n");
                    database.updateRegisterLogFromDB(aktualnaUloha.akyLogin, aktualnyClient.EMAIL, aktualnaUloha.akyStatus, "WRONG DKP", (int) aktualnaUloha.cisloBlocka);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
                    aktualnyBlocek.timeMakroGenerated = sdf.format(new Date(System.currentTimeMillis())).toString();
                    database.updateBlocekFromDB(aktualnyBlocek);
                    uspesnychCaptcha++;
                }

            } else {
                if (registerDocumentResponse.toString().contains("<span id=\"M6_cvCaptcha\" class=\"errMsg\">")) {
                    // nasli sme text o zlej captcha
                    logTextArea.append(vypisCas() + ": REGISTRACIA: zle zadana captcha pre  " + aktualnyBlocek.id + "\n");
//                    System.out.println("zle zadana captcha REFRESH CAPTCHA");
//                    refreshCaptcha();
                    isBadCaptcha = true;
                    neuspesnychCaptcha++;
                    //logTextArea.append(vypisCas() + ": registrujem blocek " + aktualnaUloha.cisloBlocka + " uzivatela " + aktualnyClient.EMAIL + "\n");
                } else {
                    if (SYSOUT_ON) {
                        System.out.println("nenaslo sa bad captcha hlaska");
                    }
                    // blocek nie je zaregistrovany, nie je ALREADY, nie je WRONG DKP, nie je ZLA CAPTCHA, proste error
                    // check ci sme prihlaseni 
                    Elements smePrihlaseni = parseElement(registerDocumentResponse, "div#M6_pnlNotRegistered p");
                    if (smePrihlaseni != null && smePrihlaseni.size() > 0) {
                        if (SYSOUT_ON) {
                            System.out.println("smePrihlaseni: " + smePrihlaseni);
                        }
                        String text = smePrihlaseni.first().html();
                        if (SYSOUT_ON) {
                            System.out.println("text: " + text);
                        }
                        if (text.contains(notLoggedText)) {
                            logTextArea.append(vypisCas() + ": REGISTRACIA: nahle odhlasenie, prihlasujem uzivatela " + aktualnyClient.EMAIL + "\n");
                            aktualnyClient.connectionUrl = JsoupClient.LOGIN_URL;
                            // prihlasime sa v dalsej ulohe
                            isBadCaptcha = true;
                            // TODO: otestovat, ci pri samovolnom odhlaseni bude tento kod fungovat a ma znova prihlasi
                            // nepridame novu ulohu, ale znizime pocitadlo uloh a tam si nastavime prihlasenie
                            String email = aktualnyClient.EMAIL;
                            String heslo = aktualnyClient.PASSWORD;
                            aktualnyClient = new JsoupClient();
                            aktualnyClient.EMAIL = email;
                            aktualnyClient.PASSWORD = heslo;
                            ulohy.get(aktUlohaIdx - 1).cisloClienta = aktualnaUloha.cisloClienta;
                            ulohy.get(aktUlohaIdx - 1).cisloBlocka = 0;
                            aktUlohaIdx--;
                        }
                    } else {
                        // ina chyba sa stala ked sme prihlaseni
                        database.updateRegisterLogFromDB(aktualnaUloha.akyLogin, aktualnyClient.EMAIL, aktualnaUloha.akyStatus, "ERROR", (int) aktualnaUloha.cisloBlocka);
                        uspesnychCaptcha++;
                    }
                }
            }
            //System.out.println("PERIOD REGISTERED BLOCEK after check errors time: " + (System.currentTimeMillis() - period));
            zaregistrovanychLabel.setText(uspesnychCaptcha + "/" + ulohy.size());
            DecimalFormat df = new DecimalFormat("#.##");
            double captchaUspesnost = (double) uspesnychCaptcha / (neuspesnychCaptcha + uspesnychCaptcha);
            captchaUspesnostLabel.setText(df.format(captchaUspesnost * 100));
            rychlost = (double) cas / (uspesnychCaptcha + neuspesnychCaptcha);

            //System.out.println("rychlost: "+rychlost);
            rychlostLabel.setText(df.format(rychlost));
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException ex) {
//                Logger.getLogger(ZaregistrovatForm.class.getName()).log(Level.SEVERE, null, ex);
//            }
            //System.out.println("zaregistrovany blocek: " + aktualnaUloha.cisloBlocka + " sprava: " + registerDocumentResponse);
            if (!isBadCaptcha) {
                aktUlohaIdx++;
            }
            if (aktUlohaIdx >= ulohy.size()) {
                JOptionPane.showMessageDialog(rootPane, "Vsetky blocky sa zaregistrovali. " + uspesneZaregistrovanych + " z " + vsetkychBlockov);
                // tu musi byt tento refresh inac sa nepodari odhlasit
                
                updatePoctyVTabulkeBlockov();
                registerLogItems = database.getRegisterLogList();
                //System.out.println(blocky.get(0));
                Collections.reverse(registerLogItems);
                //System.out.println(blocky.get(0));
                refreshRegisterLogTable();
                refreshRegisterLogTableColors();
                captchaTextField.setText("");
                String macaddress = "unknownmac";
                if (demo) {
                    authenticator.increaseAndReturnPocet(getMacAddress(macaddress), 1, uspesneZaregistrovanych);
                } else {
                    authenticator.increaseAndReturnPocet(getMacAddress(macaddress), APP_ID, uspesneZaregistrovanych);
                }
                blockyCountUpdated = true;
                odpojitButtonActionPerformed(null);
                System.out.println("vsetky ulohy splnene REFRESH CAPTCHA");                
                refreshCaptcha();
                return true;
            } else {
                // aktualnyClient = clienti[ulohy.get(aktUlohaIdx).cisloClienta];
                aktualnaUloha = ulohy.get(aktUlohaIdx);
                if (SYSOUT_ON) {
                    System.out.println("pred captcha refreshom: " + (System.currentTimeMillis() - startTime));
                }
                //System.out.println("spracovany blocek REFRESH CAPTCHA");
                System.out.println("PERIOD BLOCEK SPRACOVANY TIME: " + (System.currentTimeMillis() - period));
                refreshCaptcha();
                if (aktualnaUloha.cisloBlocka == 0) {
                    logTextArea.append(vypisCas() + ": prihlasujem uzivatela " + aktualnyClient.EMAIL + "\n");
                } else {
                    logTextArea.append(vypisCas() + ": registrujem blocek " + aktualnaUloha.cisloBlocka + " uzivatela " + aktualnyClient.EMAIL + "\n");
                }
                logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
                if (SYSOUT_ON) {
                    System.out.println("pripraveny dalsi blocek: " + (System.currentTimeMillis() - startTime));
                }
            }

            captchaTextField.setText("");
        }
        return false;
    }//GEN-LAST:event_captchaTextFieldKeyReleased

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
            if (SYSOUT_ON) {
                System.out.println(sb.toString());
            }
            macAddress = sb.toString();
            if (macAddress.length()>17){
                macAddress="macaddress"+APP_ID;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return macAddress;
    }

    private void registerLogTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_registerLogTableMouseReleased
        if (SYSOUT_ON) {
            System.out.println("registerlog table mouse released");
        }
        Point p = evt.getPoint();
        int rowNumber = registerLogTable.rowAtPoint(p);

        // RIGHT MOUSE BUTTON CLICKED, display popup menu
        if (evt.getButton() == MouseEvent.BUTTON3 || evt.getButton() == MouseEvent.BUTTON1) {
            Object idcko = registerLogTable.getValueAt(rowNumber, 0);
            Blocek akt = null;
            if (idcko instanceof Integer) {
                int id = (Integer) idcko;
                akt = getAktBlocek(id);
                // System.out.println("vybrany blocek s id: " + akt.id);
                if (akt == null) {
                    System.out.println("nenasiel sa blocek s id " + idcko);
                    return;
                }
            } else {
                System.err.println("chyba castovania prveho column na int (ID)");
                return;
            }
            //refreshBlockyTableColors();
            // select row with right mouse click
            // get the coordinates of the mouse click

            ListSelectionModel model = registerLogTable.getSelectionModel();
            model.setSelectionInterval(rowNumber, rowNumber);

            PopupMenuActionListener menuListener;
            menuListener = new PopupMenuActionListener(akt, database, this);
            menuListener.column = registerLogTable.getSelectedColumn();
            Pmenu = new JPopupMenu();
            menuItem = new JMenuItem("Zobrazit");
            Pmenu.add(menuItem);
            menuItem.addActionListener(menuListener);
            menuItem = new JMenuItem("Delete");
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
    }//GEN-LAST:event_registerLogTableMouseReleased

    private void updatePoctyVTabulkeBlockov() {
        // updatnut pocty v tabulke blockov
        // prejst vsetky zaregistrovane blocky a spocitat kolko krat su zaregistrovane
        List<RegisterLog> zaregistrovane = database.getRegisterLogListWherePocetIsNot3();
        Map<Integer, Integer> poctyRegistraci = new HashMap<Integer, Integer>();
        for (RegisterLog rl : zaregistrovane) {
            int pocet = 0;
            if (rl.status1.length() > 0 && !rl.status1.equalsIgnoreCase("TODO") && !rl.status1.equalsIgnoreCase("ERROR")) {
                // blocek bol spravne zaregistrovany
                pocet++;
            }
            if (rl.status2.length() > 0 && !rl.status2.equalsIgnoreCase("TODO") && !rl.status2.equalsIgnoreCase("ERROR")) {
                // blocek bol spravne zaregistrovany
                pocet++;
            }
            if (rl.status3.length() > 0 && !rl.status3.equalsIgnoreCase("TODO") && !rl.status3.equalsIgnoreCase("ERROR")) {
                // blocek bol spravne zaregistrovany
                pocet++;
            }
            poctyRegistraci.put(rl.blocek_id, pocet);
        }
        database.updateBlocekPocty(poctyRegistraci);
    }

    private String vypisCas() {
        String cas = "";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        cas = sdf.format(new Date(System.currentTimeMillis()));

        return cas;
    }

    private void pripojitNovehoClienta() {
        // prva uloha je vzdy prihlasenie uzivatela tak nacitame captcha z prihlasovacej stranky
        // iba tu sa bude nastavovat aktualny client
        if (SYSOUT_ON) {
            System.out.println("zacinam pripajanie");
        }
        startPridavanieCas = System.currentTimeMillis();
        timer.startTime = startPridavanieCas;
        timer.stop = false;
        es.execute(timer);
        uspesnychCaptcha = 0;
        neuspesnychCaptcha = 0;

        aktUlohaIdx = 0;
        vsetkychBlockov = 0;
        uspesneZaregistrovanych = 0;
        blockyCountUpdated = false;
        aktualnaUloha = ulohy.get(aktUlohaIdx);
        aktualnyClient = clienti[aktualnaUloha.cisloClienta];
        System.out.println("pripojit noveho clienta REFRESH CAPTCHA");
        refreshCaptcha();
        logTextArea.append(vypisCas() + ": prihlasujem uzivatela " + aktualnyClient.EMAIL + "\n");
    }

    private void refreshCaptcha() {
        try {
            //System.out.println("PERIOD START");
            period = System.currentTimeMillis();
            // TODO chytat null pointer ked ma nahodou odpoji alebo co
            try {
                captchaLabel.setIcon(new ImageIcon(aktualnyClient.getCaptcha()));
            } catch (NullPointerException ex) {
                System.out.println("refresh captcha: "+ex);
                return;
            }
            mp3Captcha = aktualnyClient.getMp3Captcha();
//         captchaTextField.setText(wdata.getCaptchaTextFromMp3("mp3captcha.mp3"));
//            if (captchaLabel.isEnabled()) {
//                captchaTextFieldEnterPressed(null);
//            }
            urychlovacMp3Captcha.nazovMp3Captcha = "mp3captcha.mp3";
            es.execute(urychlovacMp3Captcha);
        } catch (IOException e) {
            System.err.println("nemame image?" + e);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(ZaregistrovatForm.class.getName()).log(Level.SEVERE, null, ex);
            }
            refreshCaptcha();
        }
    }

    private void odpojit() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
            java.util.logging.Logger.getLogger(ZaregistrovatForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ZaregistrovatForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ZaregistrovatForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ZaregistrovatForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                //new ZaregistrovatForm().setVisible(true);
            }
        });
    }

    private Blocek getAktBlocek(int id) {
        for (Blocek b : blocky) {
            if (b.id == id) {
                return b;
            }
        }
        return null;
    }

    private static Elements parseElement(Document doc, String selector) {
        return doc.select(selector);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel ETACasLabel;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel captchaLabel;
    private javax.swing.JTextField captchaTextField;
    private javax.swing.JLabel captchaUspesnostLabel;
    private javax.swing.JLabel heslo1Label;
    private javax.swing.JLabel heslo2Label;
    private javax.swing.JLabel heslo3Label;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea logTextArea;
    private javax.swing.JPasswordField login1PasswordField;
    private javax.swing.JTextField login1TextField;
    private javax.swing.JPasswordField login2PasswordField;
    private javax.swing.JTextField login2TextField;
    private javax.swing.JPasswordField login3PasswordField;
    private javax.swing.JTextField login3TextField;
    private javax.swing.JLabel meno1Label;
    private javax.swing.JLabel meno2Label;
    private javax.swing.JLabel meno3Label;
    private javax.swing.JButton odpojitButton;
    private javax.swing.JButton pripojitButton;
    private javax.swing.JCheckBox pripojitCheckBox1;
    private javax.swing.JCheckBox pripojitCheckBox2;
    private javax.swing.JCheckBox pripojitCheckBox3;
    private javax.swing.JTable registerLogTable;
    private javax.swing.JLabel rychlostLabel;
    private javax.swing.JLabel upCasLabel;
    private javax.swing.JLabel zaregistrovanychLabel;
    // End of variables declaration//GEN-END:variables

    private void refreshRegisterLogTable() {
        registerLogTableModel = new RegisterLogTableModel(registerLogTableColumnNames, registerLogItems);
        registerLogTable.setModel(registerLogTableModel);
        // ID column
        registerLogTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        registerLogTable.getColumnModel().getColumn(0).setMaxWidth(80);
        // status1 column
        registerLogTable.getColumnModel().getColumn(1).setPreferredWidth(110);
        registerLogTable.getColumnModel().getColumn(1).setMaxWidth(250);
        // status2 column
        registerLogTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        registerLogTable.getColumnModel().getColumn(2).setMaxWidth(250);
        // status3 column
        registerLogTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        registerLogTable.getColumnModel().getColumn(3).setMaxWidth(250);
    }

    /**
     * refreshes table row background colors according to type of problem they belong to
     */
    private void refreshRegisterLogTableColors() {
        int row = 0;
        for (int i = 0; i < registerLogItems.size(); i++) {
            // Blocek blocek = blocky.get(i);
            Object idcko = registerLogTable.getValueAt(i, 0);
            RegisterLog akt = null;
            if (idcko instanceof Integer) {
                int id = (Integer) idcko;
                akt = getAktRegisterLogItem(id);
                //System.out.println("vybrany blocek s id: " + akt.id);
                //dkpTextField.setText(akt.dkp);
            } else {
                //System.err.println("chyba castovania prveho column na int (ID)");
                return;
            }
            registerLogTableModel.setCellColour(row, 0, Color.CYAN);
            if (akt.status1.equalsIgnoreCase("TODO")) {
                registerLogTableModel.setCellColour(row, 1, Color.white);
            } else {
                if (akt.status1.equalsIgnoreCase("Ľutujeme, daný doklad už bol registrovaný.")) {
                    registerLogTableModel.setCellColour(row, 1, Color.yellow);
                } else {
                    if (akt.status1.equalsIgnoreCase("WRONG DKP")) {
                        registerLogTableModel.setCellColour(row, 1, Color.PINK);
                    } else {
                        if (akt.status1.equalsIgnoreCase("ERROR")) {
                            registerLogTableModel.setCellColour(row, 1, Color.red);
                        } else {
                            registerLogTableModel.setCellColour(row, 1, Color.green);
                        }
                    }
                }
            }
            if (akt.status2.equalsIgnoreCase("TODO")) {
                registerLogTableModel.setCellColour(row, 2, Color.white);
            } else {
                if (akt.status2.equalsIgnoreCase("Ľutujeme, daný doklad už bol registrovaný.")) {
                    registerLogTableModel.setCellColour(row, 2, Color.yellow);
                } else {
                    if (akt.status2.equalsIgnoreCase("WRONG DKP")) {
                        registerLogTableModel.setCellColour(row, 2, Color.PINK);
                    } else {
                        if (akt.status2.equalsIgnoreCase("ERROR")) {
                            registerLogTableModel.setCellColour(row, 2, Color.red);
                        } else {
                            registerLogTableModel.setCellColour(row, 2, Color.green);
                        }
                    }
                }
            }
            if (akt.status3.equalsIgnoreCase("TODO")) {
                registerLogTableModel.setCellColour(row, 3, Color.white);
            } else {
                if (akt.status3.equalsIgnoreCase("Ľutujeme, daný doklad už bol registrovaný.")) {
                    registerLogTableModel.setCellColour(row, 3, Color.yellow);
                } else {
                    if (akt.status3.equalsIgnoreCase("WRONG DKP")) {
                        registerLogTableModel.setCellColour(row, 3, Color.PINK);
                    } else {
                        if (akt.status3.equalsIgnoreCase("ERROR")) {
                            registerLogTableModel.setCellColour(row, 3, Color.red);
                        } else {
                            registerLogTableModel.setCellColour(row, 3, Color.green);
                        }
                    }
                }
            }
            row++;

        }
    }

    private RegisterLog getAktRegisterLogItem(int id) {
        for (RegisterLog rl : registerLogItems) {
            if (rl.blocek_id == id) {
                return rl;
            }
        }
        return null;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if ("logAdded".equals(evt.getPropertyName())) {
            if (SYSOUT_ON) {
                System.out.println("Pridany log");
            }
            refreshDataFromRegisterLog();
        }
        if ("captchaSolved".equals(evt.getPropertyName())) {
            System.out.println("PERIOD CAPTCHA SOLVED TIME: " + (System.currentTimeMillis() - period));
            //System.out.println("captcha solved property changed");
            //System.out.println("captcha text: "+urychlovacMp3Captcha.nazovMp3Captcha);
            captchaTextField.setText(urychlovacMp3Captcha.nazovMp3Captcha);
            if (captchaTextField.isEnabled()) {
                captchaTextFieldEnterPressed(null);
            }
            //captchaTextFieldKeyReleased(null);
        }
        if ("secondAdded".equals(evt.getPropertyName())) {
            // System.out.println("secondAdded property change");
            int hodin = (int) ((System.currentTimeMillis() - startPridavanieCas) / (1000 * 3600));
            int minut = (int) ((System.currentTimeMillis() - startPridavanieCas) / (1000 * 60));
            int sekund = (int) ((System.currentTimeMillis() - startPridavanieCas) / (1000));
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
            upCasLabel.setText(hodinString + ":" + minutString + ":" + sekundString);

            etaTime = (ulohy.size() - uspesnychCaptcha) * rychlost;
            int hodinE = (int) ((etaTime) / (3600));
            int minutE = (int) ((etaTime) / (60));
            int sekundE = (int) ((etaTime));
            sekundE %= 60;
            minutE %= 60;
            String hodinStringE = "" + hodinE;
            if (hodinE < 10) {
                hodinStringE = "0" + hodinE;
            }
            String minutStringE = "" + minutE;
            if (minutE < 10) {
                minutStringE = "0" + minutE;
            }
            String sekundStringE = "" + sekundE;
            if (sekundE < 10) {
                sekundStringE = "0" + sekundE;
            }
            ETACasLabel.setText(hodinStringE + ":" + minutStringE + ":" + sekundStringE);
        }
    }

    private void refreshDataFromRegisterLog() {
        registerLogItems = database.getRegisterLogList();
        //System.out.println(blocky.get(0));
        Collections.reverse(registerLogItems);
        //System.out.println(blocky.get(0));
        refreshRegisterLogTable();
        refreshRegisterLogTableColors();
    }
}
