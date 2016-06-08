/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package home.blockova;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author Janco1
 */
public class PopupMenuActionListener implements ActionListener {

    private Blocek blocek;
    private Predajca predajca;
    private Database database;
    private Frame owner;
    private PropertyChangeSupport changes = new PropertyChangeSupport(this);
    public int column;

    public PopupMenuActionListener(Blocek blocek, Database db, Frame owner) {
        database = db;
        this.blocek = blocek;
        this.owner = owner;
    }

    PopupMenuActionListener(Predajca predajca, Database database, MainForm aThis) {
        this.database = database;
        this.predajca = predajca;
        this.owner = owner;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changes.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changes.removePropertyChangeListener(listener);
    }

    public void actionPerformed(ActionEvent e) {
        System.out.println("Popup menu item ["
                + e.getActionCommand() + "] was pressed.");
        if (e.getActionCommand().equalsIgnoreCase("Delete")) {
            int naozaj = JOptionPane.showConfirmDialog(owner, "Naozaj chcete zmazať bloček?");
            if (naozaj == 0) {
                database.deleteBlocekFromDB(blocek);
            }
        }
        if (e.getActionCommand().equalsIgnoreCase("Edit")) {
            System.out.println("EDIT BLOCEK");
            EditBlocekForm ebf = new EditBlocekForm(owner, true, blocek, database);
            ebf.setVisible(true);
        }
        if (e.getActionCommand().equalsIgnoreCase("Zobrazit")) {
            System.out.println("ZOBRAZIT BLOCEK");
            DetailRegistracieForm ebf = new DetailRegistracieForm(owner, true, blocek, database, column);
            ebf.setVisible(true);
        }
        if (e.getActionCommand().equalsIgnoreCase("Zmen Meno")) {
            System.out.println("ZMEN MENO");
            System.out.println("zmen meno  = " + predajca.meno);
            ZmenMenoForm zmf = new ZmenMenoForm(owner, true, predajca, database);
            zmf.setVisible(true);
        }
//        if (e.getActionCommand().equalsIgnoreCase("View Alarm")) {
//            AlarmViewer ev=new AlarmViewer(owner, blocky.get(rowNumber).getAlarmAlarmID(), database);
//            ev.setVisible(true);
//        }
//        if (e.getActionCommand().equalsIgnoreCase("View Alarms for this server")) {
//            changes.firePropertyChange("serverAlarms", "", blocky.get(rowNumber).getEmailHostname());
//        }

    }
}
