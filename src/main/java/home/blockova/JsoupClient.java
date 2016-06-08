/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package home.blockova;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class JsoupClient {

    static final String LOGIN_URL = "https://narodnablockovaloteria.tipos.sk/sk/administracia/prihlasenie";
    static final String REG_URL = "https://narodnablockovaloteria.tipos.sk/sk/administracia/registracia-dokladu";

    private Connection connection;
    private Map<String, String> cookies;
    private String viewState;
    private String eventValidation;
    String connectionUrl;
    private boolean isLogged;

    // TODO: sem pridu registracne udaje, pripadne v GUI ich mozte tahat z textovych policok
    public String EMAIL = "";
    public String PASSWORD = "";
    private File mp3Captcha;

    public JsoupClient() {
        cookies = new HashMap<String, String>();
        // login je default
        connectionUrl = LOGIN_URL;
        System.out.println("Jsoup client process started.");

        connect();

    }

    public void connect() {
        connection = Jsoup.connect(getConnectionUrl());

    }

    public Image getCaptcha() throws IOException {
        Response response = connection.execute();
        cookies.putAll(response.cookies());
        Document doc = response.parse();

        // Microsoft premenne idu zo servera na stranku, treba ich precitat a poslat, oni sa menia kazdym requestom, bez toho to bolo nechodive
        // toto zabralo najviac casu, kym som na to prisiel
        viewState = parseElement(doc, "input#__VIEWSTATE").val();
        eventValidation = parseElement(doc, "input#__EVENTVALIDATION").val();

        // read mp3 captcha
        Elements mp3captcha = parseElement(doc, "a#hlAudioDownload");
        if ((mp3captcha != null) && (mp3captcha.size() > 0)) {
            String src = mp3captcha.first().attr("href");
            URL url = null;
            try {
                url = new URL(src);
            } catch (MalformedURLException e) {
            }
            if (url == null) {
                src = getConnectionUrl() + src;
                try {
                    url = new URL(src);
                } catch (MalformedURLException e) {
                }
            }
            if (url != null) {
                org.apache.commons.io.FileUtils.copyURLToFile(url, new File("mp3captcha.mp3"));
                mp3Captcha=new File("mp3captcha.mp3");
            }
        }
        // read img captcha
        Elements captcha = parseElement(doc, "img#imgCaptcha");
        if ((captcha != null) && (captcha.size() > 0)) {
            String src = captcha.first().attr("src");
            URL url = null;
            try {
                url = new URL(src);
            } catch (MalformedURLException e) {
            }
            if (url == null) {
                src = getConnectionUrl() + src;
                try {
                    url = new URL(src);
                } catch (MalformedURLException e) {
                }
            }
            if (url != null) {
                Image image = ImageIO.read(url);
                return image;
            }
        }
        
        System.out.println("returning image is null");
        return null;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public Document login(String captchaCode) {
        connectionUrl = LOGIN_URL;
        connect();

        connection.data("M7$tbEmail", EMAIL);
        connection.data("M7$tbPass", PASSWORD);
        connection.data("M7$captcha$txtCaptcha", captchaCode);
        connection.data("M7$btnPrihlasit", "Prihlásiť"); // moze byt akykolvek String asi, ale pre istotu
        // toto tam musi byt, aj ked to je prazdne, to su veci pre Microsoft ASP.NET server, s tym som mal najvacsi problem
        connection.data("__EVENTTARGET", "");
        connection.data("__EVENTARGUMENT", "");
        connection.data("__VIEWSTATE", viewState);
        connection.data("__EVENTVALIDATION", eventValidation);
        connection.cookies(cookies);

        connection.method(Method.POST);
        Document document = null;
        try {
            Response response = connection.execute();
            document = response.parse();
            cookies.putAll(response.cookies());
            isLogged = cookies.containsKey("AuthToken");
            // vypisy, mozte zmazat
            System.out.println("isLogged:" + isLogged());
            System.out.println("Cookies:");
            for (Entry<String, String> entry : cookies.entrySet()) {
                System.out.println(entry.getKey() + "=" + entry.getValue());
            }
            if (isLogged) {
                // po uspesnom logine prepiname url na registraciu blockov
                connectionUrl = REG_URL;

                // tu asi netreba parsovat odpoved, to ze sme loggnuty vieme z cookie AuthToken
                //Document document = response.parse();
                //System.out.println(document.toString());
            }
        } catch (IOException e) {
            System.err.println("error on connecting");
        }
        return document;
    }

    public void logout() {
        System.out.println("logout spusteny");
        connectionUrl = REG_URL;
        connect();
//        try {
//            getCaptcha();
//        } catch (IOException ex) {
//            System.out.println("bad captcha error on connecting in logout");
//            Logger.getLogger(JsoupClient.class.getName()).log(Level.SEVERE, null, ex);
//        }

        connection.data("M2$btnOdhlasit", "Odhlásiť");
        // Opat Microsoft veci
        connection.data("__EVENTTARGET", "");
        connection.data("__EVENTARGUMENT", "");
        connection.data("__VIEWSTATE", viewState);
        connection.data("__EVENTVALIDATION", eventValidation);
        connection.cookies(cookies);

        connection.method(Method.POST);
        try {
            Response response = connection.execute();
            cookies = response.cookies();
            String authToken = cookies.get("AuthToken");
            // AuthToken ostava, ale prazdna hodnota, ak ostane nejaka value, tak sa nieco nepodarilo
            // prazdna hodnota=odhlaseny
            isLogged = (authToken != null) && (authToken.length() > 0);
            if (isLogged) {
                System.err.println("Odhlasenie neuspesne");
            } else {
                //cookies = null;
            }
            System.out.println("isLogged:" + isLogged());
        } catch (IOException e) {
            System.err.println("error on connecting");
            Logger.getLogger(JsoupClient.class.getName()).log(Level.SEVERE, null, e);
        }
        //connect();
    }

    private static Elements parseElement(Document doc, String selector) {
        return doc.select(selector);
    }

    public boolean isLogged() {
        return isLogged;
    }

    public Document register(Blocek blocek, String captchaCode) throws IOException {
//        if (!isLogged()) {
//            System.err.println("Call login first");
//            return null;
//        }

        connectionUrl = REG_URL;
        connect();
        if (blocek == null) {
            System.out.println("blocek is NULL");
        }
        connection.data("M6$captcha$txtCaptcha", captchaCode);
        connection.data("M6$tbDKP", blocek.dkp);
        connection.data("M6$tbDate", blocek.datum.substring(0, 16));
        connection.data("M6$tbPrice", Double.toString(blocek.suma));
        connection.data("M6$btnRegister", "Registruj");
        // Opat Microsoft veci
        connection.data("__EVENTTARGET", "");
        connection.data("__EVENTARGUMENT", "");
        connection.data("__VIEWSTATE", viewState);
        connection.data("__EVENTVALIDATION", eventValidation);

        connection.cookies(cookies);

        connection.method(Method.POST);
        try {
            Response response = connection.execute();
            cookies.putAll(response.cookies());
//            System.out.println("Cookies:");
//            for (Entry<String, String> entry : cookies.entrySet()) {
//                System.out.println(entry.getKey() + "=" + entry.getValue());
//            }
            Document document = response.parse();
            return document;
//            Elements ok = parseElement(document, "div#M6_pnlRegistered div.errorMsg.ok p strong");
//            Elements error = parseElement(document, "div#M6_pnlRegistered div.errorMsg p");
//            if ((ok != null) && (ok.size() > 0)) {
//                System.out.println("Registracia uspesna:");
//                // vystupny text
//                String text = ok.first().html();
//                System.out.println(text);
//                return text;
//            } else if ((error != null) && (error.size() > 0)) {
//                System.err.println("Registracia neuspesna:");
//                // vystupny text
//                String text = error.first().html();
//                System.err.println(text);
//                return text;
//            } else {
//                // toto by nemalo nastat
//                System.err.println("Co sa stalo???");
//            }
        } catch (IOException e) {
            System.err.println("register: error on connecting:" + e);
            throw new IOException();
        }

        //return null;
    }

    public void disconnect() {
        // v podstate nepotrebne
        connection = null;
    }

    public void run() {

    }

    @Override
    public String toString() {
        return Boolean.toString(isLogged);
    }

    File getMp3Captcha() throws IOException {
        return mp3Captcha;
    }

}
