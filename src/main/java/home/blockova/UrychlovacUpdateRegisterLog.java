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
public class UrychlovacUpdateRegisterLog implements Runnable{

    private Database database;
    String akyLogin;
    String EMAIL;
    String akyStatus;
    String dbText;
            int i;

    public UrychlovacUpdateRegisterLog(Database database, String akyLogin, String EMAIL, String akyStatus, String dbText, int i) {
        this.database = database;
        this.akyLogin = akyLogin;
        this.EMAIL = EMAIL;
        this.akyStatus = akyStatus;
        this.dbText = dbText;
        this.i = i;
    }

    
    public void run() {
        database.updateRegisterLogFromDBNOFirePropertyChange(akyLogin, EMAIL, akyStatus, dbText,  i);
    }
   
}
