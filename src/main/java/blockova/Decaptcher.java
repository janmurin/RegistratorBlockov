/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blockova;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author Janco1
 */
public class Decaptcher {

    
    
    public void najdiZ() {
        Path prvy = Paths.get("mp3captchas/pismeno_c.wav");//8164 a 28500
        Path druhy = Paths.get("mp3captchas/wncl.wav");
        try {
            byte[] prvyBytes = Files.readAllBytes(prvy);
            byte[] druhyBytes = Files.readAllBytes(druhy);
            System.out.println("prvybytes length=" + prvyBytes.length);
            System.out.println("druhybytes length=" + druhyBytes.length);
            // hladam najvecsi prienik, minimalne 1000 B
            // velkost prieniku
            byte[] topPrienik = new byte[0];
            int topprienikStart = 0;
            for (int velkostPrieniku = 20000; velkostPrieniku >= 1000; velkostPrieniku -= 500) {
                System.out.println("velkost prieniku: " + velkostPrieniku);
                for (int i = 5000; i < prvyBytes.length - velkostPrieniku; i += 5) {
                    for (int j = 100000; j < druhyBytes.length - velkostPrieniku; j += 5) {
                        //System.out.println(i+" "+j);
                        // mame startovacie pozicie v prvom aj druhom ktore zacneme porovnavat, ak sa nebudu zhodovat tak zmenime startovacie pozicie
                        boolean nasielSaPrienik = true;
                        for (int k = 0; k < velkostPrieniku; k += 10) {
                            if (prvyBytes[i + k] != druhyBytes[j + k]) {
                                nasielSaPrienik = false;
                                break;
                            }
                        }
                        if (nasielSaPrienik) {
                            if (topPrienik.length < velkostPrieniku) {
                                topPrienik = Arrays.copyOfRange(druhyBytes, j, j + velkostPrieniku);
                                topprienikStart = j;
                                System.out.println("TOP prienik velkost: " + topPrienik.length);
                                System.out.println("TOP prienik start v druhom subore: " + topprienikStart);
                                System.out.println("TOP prienik start v prvom subore: " + i);
                                return;
                            }
                        }
                    }
                }
            }

//            String prveSlovo = "prvetslovo aaa dfer";
//            String druheSlovo = "druaaahe slevj resf ";
//            String prienik="";
//            int topprienikStart = 0;
//            for (int v = 3; v >= 1; v--) {
//                for (int i = 0; i < prveSlovo.length() - v; i++) {
//                    for (int j = 0; j < druheSlovo.length() - v; j++) {
//                        System.out.println(v+" "+i+" "+j);
//                        boolean nasielSaPrienik = true;
//                        for (int k = 0; k < v; k += 1) {
//                            if (prveSlovo.charAt(i+k) != druheSlovo.charAt(j+k)) {
//                                nasielSaPrienik = false;
//                                break;
//                            }
//                        }
//                        if (nasielSaPrienik) {
//                            if (prienik.length() < v) {
//                                prienik=druheSlovo.substring(j, j+v);
//                                topprienikStart = j;
//                                System.out.println("TOP prienik velkost: " + prienik.length());
//                                System.out.println("TOP prienik start v druhom subore: " + topprienikStart);
//                                System.out.println("TOP prienik start v prvom subore: " + i);
//                                System.out.println("top prienik slovo: "+prienik);
//                                return;
//                            }
//                        }
//                    }
//                }
//            }
//            System.out.println("TOP prienik velkost: " + topPrienik.length);
//            System.out.println("TOP prienik start v druhom subore: " + topprienikStart);
        } catch (IOException ex) {
            Logger.getLogger(Decaptcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void nakresliObrazok(int[] mp3) throws IOException {
        BufferedImage obrazok = new BufferedImage(mp3.length, 200, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < mp3.length; i++) {
            for (int j = 0; j < 200; j++) {
                obrazok.setRGB(i, j, Color.WHITE.getRGB());
            }
        }
        for (int i = 0; i < mp3.length; i++) {
            for (int j = 0; j < mp3[i]; j++) {
                obrazok.setRGB(i, j+100, Color.BLUE.getRGB());
                obrazok.setRGB(i, -j+100, Color.BLUE.getRGB());
            }
        }
        File outputfile = new File("mp3obrazok.png");
        ImageIO.write(obrazok, "png", outputfile);
    }

    public static void main(String[] args) {
        Decaptcher d = new Decaptcher();
//        d.najdiZ();

        int[] obrazokMp3 = new int[1000];
        for (int i = 1; i < obrazokMp3.length; i++) {
            if (Math.random() > 0.5) {
                obrazokMp3[i] += obrazokMp3[i - 1] + 1;
                if (obrazokMp3[i] > 99) {
                    obrazokMp3[i] = 99;
                }
            } else {
                obrazokMp3[i] += obrazokMp3[i - 1] - 1;
                if (obrazokMp3[i] < 0) {
                    obrazokMp3[i] = 0;
                }
            }
        }
        try {
            d.nakresliObrazok(obrazokMp3);
        } catch (IOException ex) {
            Logger.getLogger(Decaptcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
