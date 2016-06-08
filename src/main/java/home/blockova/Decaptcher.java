/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package home.blockova;

import static com.musicg.main.demo.WaveDemo.main;
import com.musicg.wave.Wave;
import com.musicg.wave.WaveFileManager;

/**
 *
 * @author Janco1
 */
public class Decaptcher {

    public static void main(String[] args) {
        String filename = "captcha.mp3";
        String outFolder = "out";

// create a wave object
        Wave wave = new Wave(filename);

// print the wave header and info
        System.out.println(wave);

//// trim the wav
//        wave.leftTrim(1);
//        wave.rightTrim(0.5F);
//
//// save the trimmed wav
//        WaveFileManager waveFileManager = new WaveFileManager(wave);
//        waveFileManager.saveWaveAsFile(outFolder + "/out.wav");
    }
}
