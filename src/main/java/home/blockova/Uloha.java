/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package home.blockova;

/**
 *
 * @author Janco1
 */
public class Uloha {
    
    int cisloClienta;
    int cisloBlocka;
    String akyLogin;
    String akyStatus;

    /**
     * ak je cislo blocka 0, tak sa jedna o prihlasenie
     * @param cisloClienta
     * @param cislolocka 
     */
    public Uloha(int cisloClienta, int cislolocka) {
        this.cisloClienta = cisloClienta;
        this.cisloBlocka = cislolocka;
    }

    Uloha(int cisloClienta, int blocek_id, String login, String status) {
        this.cisloClienta = cisloClienta;
        this.cisloBlocka = blocek_id;
        this.akyLogin=login;
        this.akyStatus=status;
    }

    @Override
    public String toString() {
        return cisloClienta+":"+cisloBlocka+":"+akyLogin+":"+akyStatus;
    }
    
    
}
