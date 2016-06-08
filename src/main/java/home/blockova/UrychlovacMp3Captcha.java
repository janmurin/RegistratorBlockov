/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package home.blockova;

import blockova.WaveData;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 *
 * @author Janco1
 */
public class UrychlovacMp3Captcha implements Runnable{

    private WaveData wdata;
    public String nazovMp3Captcha;
    private PropertyChangeSupport changes = new PropertyChangeSupport(this);
    
    public UrychlovacMp3Captcha(WaveData wdata, String nazovMp3Captcha) {
        this.wdata=wdata;
        this.nazovMp3Captcha=nazovMp3Captcha;
    }

    
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        changes.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changes.removePropertyChangeListener(listener);
    }
    
    public void run() {
        nazovMp3Captcha=wdata.getCaptchaTextFromMp3(nazovMp3Captcha);
        changes.firePropertyChange("captchaSolved", 0, 1);
    }
    
}
