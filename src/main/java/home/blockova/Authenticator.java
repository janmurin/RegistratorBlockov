/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package home.blockova;

import java.awt.Image;
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

public class Authenticator {

     static final String LOGIN_URL = "http://www.1000blockov.sk/registrator";
     //static final String REG_URL = "https://narodnablockovaloteria.tipos.sk/sk/administracia/registracia-dokladu";

    private Connection connection;
    private Map<String, String> cookies;
    private String viewState;
    private String eventValidation;
     String connectionUrl;
    private boolean isLogged;

    // TODO: sem pridu registracne udaje, pripadne v GUI ich mozte tahat z textovych policok
    public String EMAIL = "";
    public String PASSWORD = "";

    public Authenticator() {
        cookies = new HashMap<String, String>();
        // login je default
        connectionUrl = LOGIN_URL;
        System.out.println("Jsoup client process started.");

        connect();

    }

    public void connect() {
        connection = Jsoup.connect(getConnectionUrl());
    }
    public String getConnectionUrl() {
        return connectionUrl;
    }

    public Document increaseAndReturnPocet(String macAddress, int uid, int pocet){
        connectionUrl = LOGIN_URL+"/registracii.php?user="+macAddress+"&appid="+uid+"&pocet="+pocet;
        //System.out.println("authenticator url: "+connectionUrl);
        connect();

        connection.method(Method.GET);
        try {
            Response response = connection.execute();
            Document document = response.parse();
            //System.out.println(document);
            return document;
        } catch (IOException e) {
            System.err.println("error on connecting");
            Logger.getLogger(JsoupClient.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }
    
     public Document getMacAddressCountForAppid(int appid){
        connectionUrl = LOGIN_URL+"/appid.php?appid="+appid;
        //System.out.println("authenticator url: "+connectionUrl);
        connect();

        connection.method(Method.GET);
        try {
            Response response = connection.execute();
            Document document = response.parse();
            //System.out.println(document);
            return document;
        } catch (IOException e) {
            System.err.println("error on connecting");
            Logger.getLogger(JsoupClient.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

  public static void main( String[] args )
    {
        Authenticator a=new Authenticator();
        //Document d= increaseAndReturnMaxPocetet("novy user", 1, 7);
        Document d=a.getMacAddressCountForAppid(1);
        System.out.println(d);
    }
    
    
}
