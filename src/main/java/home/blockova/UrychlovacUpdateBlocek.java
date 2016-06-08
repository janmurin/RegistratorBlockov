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
public class UrychlovacUpdateBlocek implements Runnable{
    private Database database;
    Blocek blocek;

    public UrychlovacUpdateBlocek(Database database,Blocek blocek) {
        this.database = database;
        this.blocek=blocek;
    }

    
    public void run() {
       database.updateBlocekFromDBNOFirePropertyChange(blocek);
    }
}
