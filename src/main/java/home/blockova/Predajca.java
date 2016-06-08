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
public class Predajca implements Comparable<Predajca>{
    
    public String meno;
    public String dkp;
    public long id;
    int pocet;

    public Predajca() {
    }

    
    
    public Predajca(String meno, String DKP) {
        this.meno = meno;
        this.dkp = DKP;
    }

    public int compareTo(Predajca o) {
        return this.pocet-o.pocet;
    }

    @Override
    public String toString() {
        return meno; //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
