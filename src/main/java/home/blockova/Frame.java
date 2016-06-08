/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package home.blockova;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.jsoup.nodes.Document;

public class Frame extends JFrame implements ActionListener {

    private static final long serialVersionUID = 4983727800375272097L;

    private JLabel captchaImage;
    private JTextField tfCaptcha;
    private JsoupClient client;
    private JButton btnLogin;
    private JButton btnSend;
    private JButton btnLogout;

    private void refreshCaptcha() {
        try {
            captchaImage.setIcon(new ImageIcon(client.getCaptcha()));
        } catch (IOException e) {
            System.err.println("nemame image?" + e);
        }
    }

    public Frame() {
        client = new JsoupClient();

        captchaImage = new JLabel();
        add(captchaImage, BorderLayout.NORTH);
        refreshCaptcha();

        add(tfCaptcha = new JTextField(), BorderLayout.CENTER);

        JPanel btns = new JPanel();
        add(btns, BorderLayout.SOUTH);

        btnLogin = new JButton("Login");
        btnLogin.setActionCommand("send.login");
        btnLogin.addActionListener(this);
        btnLogin.setEnabled(!client.isLogged());
        btns.add(btnLogin);

        btnSend = new JButton("Send DKP");
        btnSend.setActionCommand("send.dkp");
        btnSend.addActionListener(this);
        btnSend.setEnabled(true);//client.isLogged()
        btns.add(btnSend);

        btnLogout = new JButton("Logout");
        btnLogout.setActionCommand("send.logout");
        btnLogout.addActionListener(this);
        btnLogout.setEnabled(client.isLogged());
        btns.add(btnLogout);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                client.disconnect();
                super.windowClosing(e);
            }
        });
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
    }

    public static void main(String[] args) {
        new Frame().setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("send.login")) {
            client.EMAIL="janmurin2@hotmail.com";
            client.PASSWORD="pozdisovcešľý7";
            client.login(tfCaptcha.getText());
            tfCaptcha.setText("");
            refreshCaptcha();
            if (client.isLogged()) {
                JOptionPane.showMessageDialog(this, "Successfully logged in");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid login");
            }
            btnLogin.setEnabled(!client.isLogged());
            btnSend.setEnabled(client.isLogged());
            btnLogout.setEnabled(client.isLogged());
			// uz mame nacitanu novu url, staci refreshnut obrazok
            // bud sa bude treba nanovo loggnut, ak bolo zle meno/heslo/captcha..
            // .. alebo uz mozme ist zadat blocek
        } else if (command.equals("send.dkp")) {
            Calendar c = Calendar.getInstance(new Locale("sk"));
            c.set(Calendar.YEAR, 2014);
            c.set(Calendar.MONTH, Calendar.JUNE);
            c.set(Calendar.DAY_OF_MONTH, 27);
            c.set(Calendar.HOUR_OF_DAY, 7);
            c.set(Calendar.MINUTE, 59);

            SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            Blocek b = new Blocek("50020202342161284", df.format(c.getTime()).toString(), 35.87, 0);
            System.out.println(b.toString());
           // Document responseText = client.register(b, tfCaptcha.getText());
            //System.out.println(responseText);
            tfCaptcha.setText("");
            refreshCaptcha();
            // praca s responseText-om
        } else if (command.equals("send.logout")) {
            client.logout();
            if (!client.isLogged()) {
                JOptionPane.showMessageDialog(this, "Logged out");
                tfCaptcha.setText("");
                captchaImage.setIcon(null);
            }
            btnLogin.setEnabled(!client.isLogged());
            btnSend.setEnabled(true);//client.isLogged()
            btnLogout.setEnabled(client.isLogged());
        }
    }
}
