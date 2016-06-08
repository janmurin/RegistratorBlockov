/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blockova;

import java.text.DecimalFormat;

/**
 *
 * @author Janco1
 */
public class skuska {
    
    public static void main(String[] args){
        int n=10000;
        int[] vysledky=new int[n];
        double pocetVyhier=0;
        double zarobok=0;
        int pocetRegistracii=3000*3;
        for (int i=0; i<n; i++){
            for (int j=0; j<100; j++){
                if (Math.random()*950000<pocetRegistracii){
                    vysledky[i]++;
                    zarobok+=100;
                }
            }
            if (vysledky[i]>0){
                pocetVyhier++;
            }
        }
        DecimalFormat df=new DecimalFormat("###.##");
        double sanca=pocetVyhier/n*100;
        System.out.println("sanca na vyhru pre "+(pocetRegistracii)+" registracii: "+df.format(sanca)+" %");
        double priemernyZarobok=zarobok/n;
        System.out.println("priemerny zarobok pre "+(pocetRegistracii)+" registracii: "+df.format(priemernyZarobok)+" €");
    }
    
}
