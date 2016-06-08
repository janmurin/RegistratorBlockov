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
public class Blocek {
    
    public String dkp;
    public String datum;
    public String timeMakroGenerated;
    public String timeInserted;
    public double suma;
    public int pocet;
    public int id;
    public String registrator;

    public Blocek() {
    }
    
    public Blocek(String dkp, String datum, double suma, int pocet) {
        this.dkp = dkp;
        this.datum = datum;
        this.suma = suma;
        this.pocet = pocet;
    }

    @Override
    public String toString() {
        return dkp+" "+datum+" "+suma;//To change body of generated methods, choose Tools | Templates.
    }
    
    
}
