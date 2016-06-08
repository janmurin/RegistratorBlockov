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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JOptionPane;
import javazoom.jl.converter.Converter;

/**
 * saving and extracting amplitude data from wavefile byteArray
 *
 * @author Ganesh Tiwari
 */
public class WaveData {

    public WaveData() {
        nacitajPismenka();
    }

    private static String convertMp3ToWav(String nazovSuboru) {
        try {
            Converter converter = new Converter();
            converter.convert(nazovSuboru, nazovSuboru + ".wav");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return nazovSuboru + ".wav";
    }

    private void nacitajPismenka() {
        System.out.println("nacitavam pismenka");
        pismenka = new ArrayList<Chunk>();
        for (char p = 'a'; p <= 'z'; p++) {
            pismenka.add(new Chunk(Character.toString(p)));
        }
        File folder = new File("mp3captchas");
        File[] subory = folder.listFiles();
        for (int i = 0; i < subory.length; i++) {
            if (subory[i].getName().contains("wav")) {
                System.out.println("nacitavam " + subory[i].getName());
                int[] prvy = extractAmplitudeFromFile(subory[i]);

                String meno = subory[i].getName().split("\\.")[0];
                System.out.println("spracuvam mp3captcha: " + meno);
                prvy = skratUvod(1000, prvy);
                prvy = vycistiSum(prvy);
                ArrayList<Chunk> chunkyPismenok = getChunkyPismenok(prvy);
                if (chunkyPismenok.size() != meno.length() + 1) {
                    System.out.println("zly pocet PISMENOK AKO MA BYT");
                    JOptionPane.showMessageDialog(null, "Nie je možné spracovať captcha: Prázdne amplitúdové pole. Program skončí.");
                    System.exit(0);
                }
                // prechadzame pismenka a pridavame chunky pismenok, ktore este nemame
                for (int p = 0; p < meno.length(); p++) {
                    // pozrieme sa ci aktualne pismenko este nemame ulozene
                    for (Chunk c : pismenka) {
                        if (c.name.equalsIgnoreCase(Character.toString(meno.charAt(p)))) {
                            if (c.length == 0) {
                                // pridame aktualne pismenko
                                Chunk akt = chunkyPismenok.get(p + 1);
                                c.length = akt.length;
                                c.start = akt.start;
                                c.value = akt.value;
                            } else {
                                if (Math.abs(c.length - chunkyPismenok.get(p + 1).length) > 500) {
                                    System.out.println("ulozene pismenko " + c.name + " velkost: " + c.length + " nove pismenko " + meno.charAt(p) + " velkost: " + chunkyPismenok.get(p + 1).length);
                                }
                            }
                            break;
                        }
                    }
                }
//nakresliObrazokFromArray(prvy, "obrazkyMP3/" + meno + ".png");

            }
        }

        System.out.println("nacitane pismenka: " + pismenka);
    }

    private String zistiCaptcha(String menoSuboru) {
        System.out.println("nacitavam " + menoSuboru);
        //String nazovCaptcha = menoSuboru.split("/")[1].split("\\.")[0];
        int[] prvy = extractAmplitudeFromFile(new File(menoSuboru));
        prvy = skratUvod(1000, prvy);
        prvy = vycistiSum(prvy);
        ArrayList<Chunk> chunkyPismenok = getChunkyPismenok(prvy);
        char[] uhadnutePismenka = new char[chunkyPismenok.size() - 1];
        for (int i = 1; i < chunkyPismenok.size(); i++) {
            Chunk aktPismenko = chunkyPismenok.get(i);
            int minMieraZhody = Integer.MAX_VALUE;
            char maxPismenko = 'a';
            // porovnavame aktualne pismenko s ulozenymi pismenkami
            for (int k = 0; k < pismenka.size(); k++) {
                Chunk ulozenePismenko = pismenka.get(k);
                if (Math.abs(ulozenePismenko.length - aktPismenko.length) < 400) {
                    //System.out.println("porovnavam captcha pismenko '" + nazovCaptcha.charAt(i - 1) + "' s ulozenym pismenkom '" + ulozenePismenko.name + "'");
                    // je v rozmedzi, ideme hladat pismenko
                    int mieraZhody = getMieraZhody(aktPismenko, ulozenePismenko);
                    //System.out.println("captcha pismenko '" + nazovCaptcha.charAt(i - 1) + "' sa zhoduje s ulozenym pismenkom '" + ulozenePismenko.name + "' na " + mieraZhody);
                    if (mieraZhody < minMieraZhody) {
                        minMieraZhody = mieraZhody;
                        maxPismenko = ulozenePismenko.name.charAt(0);
                    }
                }
            }
            uhadnutePismenka[i - 1] = maxPismenko;
        }

        return String.valueOf(uhadnutePismenka);
    }

    private int getMieraZhody(Chunk aktPismenko, Chunk ulozenePismenko) {
        // System.out.println("ulozene pismenko: " + Arrays.toString(ulozenePismenko.value));
        //System.out.println("aktualne pismenko: " + Arrays.toString(aktPismenko.value));
        // INA STRATEGIA: zistim poziciu max hodnoty, o tolko posuniem druhu a spocitavam diff, najnizsi diff vyhraje
        int[] pom = ulozenePismenko.value.clone();
        int max1 = 0;
        int maxidx1 = 0;
        for (int i = 0; i < pom.length; i++) {
            if (pom[i] > max1) {
                max1 = pom[i];
                maxidx1 = i;
            }
        }
        pom = aktPismenko.value.clone();
        int max2 = 0;
        int maxidx2 = 0;
        for (int i = 0; i < pom.length; i++) {
            if (pom[i] > max2) {
                max2 = pom[i];
                maxidx2 = i;
            }
        }
        // if (Math.max(max1, max2)>Math.min(max1, max2)*1.08){
        if (Math.max(max1, max2) - Math.min(max1, max2) > 4) {
            return Integer.MAX_VALUE;
        }
        //System.out.println("max1: " + max1 + " maxidx1: " + maxidx1 + " max2: " + max2 + " maxidx2: " + maxidx2);
        int minRozdiel = Integer.MAX_VALUE;
        for (int rozdiel = -1000; rozdiel < 1000; rozdiel++) {
            int[] ulozene = ulozenePismenko.value;
            int[] aktualne = aktPismenko.value;
            int hranica = Math.min(ulozene.length, aktualne.length);
            // mame pozicie maximalnych hodnot podla toho urobime posun a potom len pocitame diff
            // ak je aktualne pismenko dalej posunute, tak vynechame niekolko prvych na zaciatku
            //int rozdiel = maxidx2 - maxidx1;
            hranica -= Math.abs(rozdiel);
            //System.out.println("hranica: " + hranica + " ulozene size: " + ulozene.length + " aktualne.length= " + aktualne.length);
            if (rozdiel > 0) {
                int diff = 0;
                for (int i = rozdiel; i < hranica; i++) {
                    diff += Math.abs(aktualne[i] - ulozene[i - rozdiel]);
                }
                if (diff < minRozdiel) {
                    minRozdiel = diff;
                }
                //System.out.println(diff+" ");
            } else {
                int diff = 0;
                for (int i = 0; i < hranica; i++) {
                    diff += Math.abs(aktualne[i] - ulozene[i - rozdiel]);
                }
                if (diff < minRozdiel) {
                    minRozdiel = diff;
                }
                //System.out.println(diff+" ");
            }

        }
        return minRozdiel;
        // ak je aktualne pismenko pred ulozenym, tak ulozene vynechame prvych par hodnot

//        int[] topPrienik = new int[0];
//        int topprienikStart = 0;
//        int mieraTolerancie = 5;
//        for (int velkostPrieniku = aktPismenko.length; velkostPrieniku >= aktPismenko.length * 2 / 3; velkostPrieniku -= 500) {
//            System.out.println("velkost prieniku: " + velkostPrieniku);
//            for (int i = 0; i < ulozenePismenko.length - velkostPrieniku; i += 1) {
//                for (int j = 0; j < aktPismenko.length - velkostPrieniku; j += 1) {
//                    //System.out.println(i+" "+j);
//                    // mame startovacie pozicie v prvom aj druhom ktore zacneme porovnavat, ak sa nebudu zhodovat tak zmenime startovacie pozicie
//                    boolean nasielSaPrienik = true;
//                    for (int k = 0; k < velkostPrieniku; k += 1) {
//                        // porovnavame s nejakou toleranciou            
//                        
//                        if (Math.abs(ulozenePismenko.value[i + k] - aktPismenko.value[j + k]) > mieraTolerancie) {
//                            nasielSaPrienik = false;
//                            //System.out.println(k+"="+Math.abs(ulozenePismenko.value[i + k] - aktPismenko.value[j + k]));
//                            break;
//                        }
//                    }
//                    if (nasielSaPrienik) {
//                        if (topPrienik.length < velkostPrieniku) {
//                            topPrienik = Arrays.copyOfRange(aktPismenko.value, j, j + velkostPrieniku);
//                            topprienikStart = j;
//                            System.out.println("TOP prienik velkost: " + topPrienik.length);
//                            System.out.println("TOP prienik start v druhom subore: " + topprienikStart);
//                            nakresliObrazokFromArray(Arrays.copyOfRange(aktPismenko.value, topprienikStart, topprienikStart + topPrienik.length), "prvyPrienik.png");
//                            System.out.println("TOP prienik start v prvom subore: " + i);
//                            nakresliObrazokFromArray(Arrays.copyOfRange(ulozenePismenko.value, i, i + topPrienik.length), "druhyPrienik.png");
//                            return (int) ((velkostPrieniku / (double)(ulozenePismenko.length))*100);
//                        }
//                    }
//                }
//            }
//        }
//        return 0;
    }

    private void testMieraZhody() {
        Chunk pismenko_q = null;
        for (Chunk p : pismenka) {
            if (p.name.equalsIgnoreCase("c")) {
                pismenko_q = p;
            }
        }
        nakresliObrazokFromArray(pismenko_q.value, "pismenko_c.png");

        int[] prvy = extractAmplitudeFromFile(new File("mp3captchas/yalj.wav"));
        prvy = skratUvod(1000, prvy);
        prvy = vycistiSum(prvy);
        ArrayList<Chunk> chunkyPismenok = getChunkyPismenok(prvy);
        //System.out.println("chunky size: "+chunkyPismenok);
        Chunk captcha_q = chunkyPismenok.get(2);
        nakresliObrazokFromArray(captcha_q.value, "captcha_a.png");

        int testMieraZhody = getMieraZhody(captcha_q, pismenko_q);
        System.out.println("miera zhody: " + testMieraZhody);
    }

    private class Chunk {

        int length;
        String name;
        int[] value;
        private int start;

        public Chunk(int start, int length) {
            this.start = start;
            this.length = length;
        }

        public Chunk(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name + ":" + length;
        }

    }

    private static final int AMPLIFY = 10;
    private int minChunkSizeNonzero = 20;

    private ArrayList<Chunk> pismenka;
    private byte[] arrFile;
    private byte[] audioBytes;
    private int[] audioData;
    private ByteArrayInputStream bis;
    private AudioInputStream audioInputStream;
    private AudioFormat format;
    private double durationSec;
    private double durationMSec;

    private int[] extractAmplitudeFromFile(File wavFile) {
        try {
            // create file input stream  
            FileInputStream fis = new FileInputStream(wavFile);
            // create bytearray from file  
            arrFile = new byte[(int) wavFile.length()];
            fis.read(arrFile);
        } catch (Exception e) {
            System.out.println("SomeException : " + e.toString());
        }
        return extractAmplitudeFromFileByteArray(arrFile);
    }

    private int[] extractAmplitudeFromFileByteArray(byte[] arrFile) {
        // System.out.println("File : "+wavFile+""+arrFile.length);  
        bis = new ByteArrayInputStream(arrFile);
        return extractAmplitudeFromFileByteArrayInputStream(bis);
    }

    /**
     * for extracting amplitude array the format we are using :16bit, 22khz, 1 channel,
     * littleEndian,
     *
     * @return PCM audioData
     * @throws Exception
     */
    private int[] extractAmplitudeFromFileByteArrayInputStream(ByteArrayInputStream bis) {
        try {
            audioInputStream = AudioSystem.getAudioInputStream(bis);
        } catch (UnsupportedAudioFileException e) {
            System.out.println("unsupported file type, during extract amplitude");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IOException during extracting amplitude");
            e.printStackTrace();
        }
        // float milliseconds = (long) ((audioInputStream.getFrameLength() *  
        // 1000) / audioInputStream.getFormat().getFrameRate());  
        // durationSec = milliseconds / 1000.0;  
        return extractAmplitudeDataFromAudioInputStream(audioInputStream);
    }

    private int[] extractAmplitudeDataFromAudioInputStream(AudioInputStream audioInputStream) {
        format = audioInputStream.getFormat();
        audioBytes = new byte[(int) (audioInputStream.getFrameLength() * format.getFrameSize())];
        // calculate durations  
        durationMSec = (long) ((audioInputStream.getFrameLength() * 1000) / audioInputStream.getFormat().getFrameRate());
        durationSec = durationMSec / 1000.0;
        // System.out.println("The current signal has duration "+durationSec+" Sec");  
        try {
            audioInputStream.read(audioBytes);
        } catch (IOException e) {
            System.out.println("IOException during reading audioBytes");
            e.printStackTrace();
        }
        return extractAmplitudeDataFromAmplitudeByteArray(format, audioBytes);
    }

    private int[] extractAmplitudeDataFromAmplitudeByteArray(AudioFormat format, byte[] audioBytes) {
        // convert  
        // TODO: calculate duration here  
        audioData = null;
        if (format.getSampleSizeInBits() == 16) {
            int nlengthInSamples = audioBytes.length / 2;
            audioData = new int[nlengthInSamples];
            if (format.isBigEndian()) {
                for (int i = 0; i < nlengthInSamples; i++) {
                    /* First byte is MSB (high order) */
                    int MSB = audioBytes[2 * i];
                    /* Second byte is LSB (low order) */
                    int LSB = audioBytes[2 * i + 1];
                    audioData[i] = MSB << 8 | (255 & LSB);
                }
            } else {
                for (int i = 0; i < nlengthInSamples; i++) {
                    /* First byte is LSB (low order) */
                    int LSB = audioBytes[2 * i];
                    /* Second byte is MSB (high order) */
                    int MSB = audioBytes[2 * i + 1];
                    audioData[i] = MSB << 8 | (255 & LSB);
                }
            }
        } else if (format.getSampleSizeInBits() == 8) {
            int nlengthInSamples = audioBytes.length;
            audioData = new int[nlengthInSamples];
            if (format.getEncoding().toString().startsWith("PCM_SIGN")) {
                // PCM_SIGNED  
                for (int i = 0; i < audioBytes.length; i++) {
                    audioData[i] = audioBytes[i];
                }
            } else {
                // PCM_UNSIGNED  
                for (int i = 0; i < audioBytes.length; i++) {
                    audioData[i] = audioBytes[i] - 128;
                }
            }
        }// end of if..else  
        // System.out.println("PCM Returned===============" +  
        // audioData.length);  
        return audioData;
    }

    private byte[] getAudioBytes() {
        return audioBytes;
    }

    private double getDurationSec() {
        return durationSec;
    }

    private double getDurationMiliSec() {
        return durationMSec;
    }

    private int[] getAudioData() {
        return audioData;
    }

    private AudioFormat getFormat() {
        return format;
    }

    private int[] nakresliObrazok(int[] mp3, String nazovObrazka) throws IOException {
        // najprv musime zmensit 100 nasobne vsetky rozmery
        int[] zmensenaMp3 = new int[mp3.length - 180000];
        for (int i = 0; i < zmensenaMp3.length; i++) {
            zmensenaMp3[i] = mp3[i + 180000] / 40;
        }
        System.out.println("zmensena mp3: " + Arrays.toString(zmensenaMp3));
        // odteraz pracujeme len so zmensenou verziou
        int[] pom = zmensenaMp3.clone();
        Arrays.sort(pom, 0, pom.length);
        int min = pom[0];
        int max = pom[pom.length - 1];
        System.out.println("max: " + max);
        System.out.println("min: " + min);
        System.out.println("nekreslim obrazok");
        BufferedImage obrazok = new BufferedImage(zmensenaMp3.length, max * AMPLIFY + 1, BufferedImage.TYPE_INT_RGB);
        // nakresli obrazok na bielo
        for (int i = 0; i < obrazok.getWidth(); i++) {
            for (int j = 0; j < obrazok.getHeight(); j++) {
                obrazok.setRGB(i, j, Color.WHITE.getRGB());
            }
        }
        // kresli amplitudy
        for (int i = 0; i < zmensenaMp3.length; i++) {
            if (zmensenaMp3[i] > 0) {
                for (int j = 0; j <= zmensenaMp3[i] * AMPLIFY; j++) {
                    obrazok.setRGB(i, j, Color.BLUE.getRGB());
                }
            } else {
//                for (int j = 0; j > pom[i]; j--) {
//                    obrazok.setRGB(i, -1*min + j+1, Color.BLUE.getRGB());
//                }
                zmensenaMp3[i] = 0;
            }
        }
        File outputfile = new File(nazovObrazka);
        ImageIO.write(obrazok, "png", outputfile);
        System.out.println(Arrays.toString(mp3));
        System.out.println("length: " + mp3.length);
        return zmensenaMp3;
    }

    private void nakresliWavSubor(String menoSuboru) {
        int[] prvy = extractAmplitudeFromFile(new File(menoSuboru));
        prvy = skratUvod(1000, prvy);
        prvy = vycistiSum(prvy);
        nakresliObrazokFromArray(prvy, menoSuboru + "_obrazok.png");
    }

    public String getCaptchaTextFromMp3(String menoSuboru) {
        String wavSubor = convertMp3ToWav(menoSuboru);
        return zistiCaptcha(wavSubor);
    }

    private void nakresliMp3Subor(String menoSuboru) {
        throw new UnsupportedOperationException("unsupported operation");
//        String wavSubor=convertMp3ToWav(menoSuboru);
//        //String wavSubor="mp3captchas/wncl.wav";
//        //nakresliWavSubor(wavSubor);
//        System.out.println("captcha zo suboru "+wavSubor+"je "+zistiCaptcha(wavSubor));
    }

    private void spracuj() {
        File folder = new File("mp3captchas");
        File[] subory = folder.listFiles();
        int uspesnych = 0;
        int neuspesnych = 0;
        for (int i = 0; i < subory.length; i++) {
            if (subory[i].getName().contains("wav")) {
                //System.out.println(subory[i].getName());
                //int[] prvy = extractAmplitudeFromFile(subory[i]);

                String meno = subory[i].getName().split("\\.")[0];
                String captchatext = zistiCaptcha("mp3captchas/" + meno + ".wav");
                System.out.println(meno + "=" + captchatext);
                if (meno.equalsIgnoreCase(captchatext)) {
                    uspesnych++;
                } else {
                    neuspesnych++;
                }
            }
        }
        System.out.println("Uspesnost: " + uspesnych + "/" + (uspesnych + neuspesnych));
    }

    private static void nakresliObrazokFromArray(int[] mp3, String nazovObrazka) {
        int[] pom = mp3.clone();
        Arrays.sort(pom, 0, pom.length);
        int max = pom[pom.length - 1];
        //System.out.println("max: " + max);
        for (int i = 0; i < mp3.length; i++) {
            if (mp3[i] == max) {
                //System.out.println("max position: " + i);
                break;
            }
        }
        BufferedImage obrazok = new BufferedImage(mp3.length, max * AMPLIFY + 1, BufferedImage.TYPE_INT_RGB);
        // nakresli obrazok na bielo
        for (int i = 0; i < obrazok.getWidth(); i++) {
            for (int j = 0; j < obrazok.getHeight(); j++) {
                obrazok.setRGB(i, j, Color.WHITE.getRGB());
            }
        }
        // kresli amplitudy
        for (int i = 0; i < mp3.length; i++) {
            if (mp3[i] > 0) {
                for (int j = 0; j <= mp3[i] * AMPLIFY; j++) {
                    obrazok.setRGB(i, j, Color.BLUE.getRGB());
                }
            } else {
//                for (int j = 0; j > pom[i]; j--) {
//                    obrazok.setRGB(i, -1*min + j+1, Color.BLUE.getRGB());
//                }
            }
        }
        File outputfile = new File(nazovObrazka);
        try {
            ImageIO.write(obrazok, "png", outputfile);
        } catch (IOException ex) {
            Logger.getLogger(WaveData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {

        // a-g, c-e, t-i, c-i
        WaveData wdata = new WaveData();
        //wdata.nakresliWavSubor("mp3captchas/zmla.wav");
        wdata.nacitajPismenka();
        wdata.nakresliMp3Subor("mp3captchas/wncl.mp3");

        //System.out.println("captcha kod: " + wdata.zistiCaptcha("mp3captchas/qcbnl.wav"));
        //wdata.testMieraZhody();
        //wdata.spracuj();
//        int[] prvy = wdata.extractAmplitudeFromFile(new File("mp3captchas/eqze.wav"));
//        // 1. zmensit o uvodne pokyny a znizit vysky
//        // 2. vycistit sum, ojedinele skupinky
//        // 3. skratit nuly
//        prvy = wdata.skratUvod(1000, prvy);
//        prvy = wdata.vycistiSum(prvy);
//        //nakresliObrazokFromArray(prvy, "vycisteny.png");
    }

    private int[] skratUvod(int kolkoSKratit, int[] mp3) {
        int[] zmensenaMp3 = new int[mp3.length - kolkoSKratit];
        for (int i = 0; i < zmensenaMp3.length; i++) {
            zmensenaMp3[i] = Math.abs(mp3[i + kolkoSKratit] / 40);
        }
        //System.out.println("skratena mp3: " + Arrays.toString(zmensenaMp3));
        return zmensenaMp3;
    }

    private int[] vycistiSum(int[] prvy) {
        List<Chunk> chunkyNul = new ArrayList<Chunk>();
        // 1.  odstranime male zoskupenia NENULOVYCH cisiel
        int i = 0;
        while (i < prvy.length) {
            if (prvy[i] > 0) {
                // nasli sme novy chunk tak si nacitame velkost
                Chunk novyChunk = new Chunk(i, 0);
                int j = 1;
                for (; j < prvy.length - i; j++) {
                    if (prvy[i + j] > 0) {
                        continue;
                    } else {
                        break;
                    }
                }
                // velkost chunku je j
                novyChunk.length = j;

                if (novyChunk.length < minChunkSizeNonzero) {
                    for (int k = novyChunk.start; k < novyChunk.length + novyChunk.start; k++) {
                        // vymazeme tento chunk ktory je prilis maly a robi len sum
                        prvy[k] = 0;
                    }
                }
                i = i + j;

                continue;
            }

            i++;
        }
        // System.out.println("odstranenie malych skupiniek cisiel: " + Arrays.toString(prvy));

// 2. spocitame chunky nul
        i = 0;
        while (i < prvy.length) {
            if (prvy[i] == 0) {
                // nasli sme novy chunk tak si nacitame velkost
                Chunk novyChunk = new Chunk(i, 0);
                int j = 1;
                for (; j < prvy.length - i; j++) {
                    if (prvy[i + j] == 0) {
                        continue;
                    } else {
                        break;
                    }
                }
                // velkost chunku je j
                novyChunk.length = j;
                chunkyNul.add(novyChunk);
                i = i + j;

                continue;
            }
            i++;
        }
        Map<Integer, Integer> mapa = new TreeMap<Integer, Integer>();
        for (Chunk c : chunkyNul) {
            if (mapa.containsKey(c.length)) {
                mapa.put(c.length, mapa.get(c.length) + 1);
            } else {
                mapa.put(c.length, 1);
            }
        }
        // System.out.println("chunky nul: " + mapa);

// 3. odstranime prilis male chunky a skratime velke, zmensime povodny mp3 
        int[] novaMp3 = new int[prvy.length];
        int novaSize = 0;
        for (Chunk c : chunkyNul) {
            // prechadzam chunky, ak je maly tak prejdem na jeho koniec a pridavam nenulove cisla
            // ak je velky tak pridam 10 nul a pridavam nenulove cisla na jeho konci zacinajuce
            if (c.length < 5000) {
                // pridame nenulovy chunk konciaci sa na zaciatku tohto
                for (int j = c.start + c.length; j < prvy.length; j++) {
                    if (prvy[j] > 0) {
                        novaMp3[novaSize] = prvy[j];
                        novaSize++;
                    } else {
                        break;
                    }
                }
            } else {
                for (int j = 0; j < 1000; j++) {
                    novaMp3[novaSize] = 0;
                    novaSize++;
                }
                // pridame nenulovy chunk konciaci sa na zaciatku tohto velkeho
                for (int j = c.start + c.length; j < prvy.length; j++) {
                    if (prvy[j] > 0) {
                        novaMp3[novaSize] = prvy[j];
                        novaSize++;
                    } else {
                        // koniec nenuloveho chunku
                        break;
                    }
                }
            }
        }
        //System.out.println("skrateny, ocisteny size: "+novaSize);
        prvy = Arrays.copyOfRange(novaMp3, 0, novaSize);
        //getChunkyPismenok(prvy);
        // System.out.println("nove chunky pismenok: " + chunkyPismenok);
        return prvy;
    }

    private ArrayList<Chunk> getChunkyPismenok(int[] mp3) {
        // 4. spocitame chunky PISMENOK
        ArrayList<Chunk> chunkyPismenok = new ArrayList<Chunk>();
        int i = 0;
        while (i < mp3.length) {
            if (mp3[i] > 0) {
                // nasli sme novy chunk tak si nacitame velkost
                Chunk novyChunk = new Chunk(i, 0);
                int j = 1;
                for (; j < mp3.length - i; j++) {
                    if (mp3[i + j] > 0) {
                        continue;
                    } else {
                        break;
                    }
                }
                // velkost chunku je j
                novyChunk.length = j;
                novyChunk.value = Arrays.copyOfRange(mp3, i, i + j);
                if (novyChunk.length > 5000) {
                    chunkyPismenok.add(novyChunk);
                }
                i = i + j;

                continue;
            }

            i++;
        }
        return chunkyPismenok;
    }
}
