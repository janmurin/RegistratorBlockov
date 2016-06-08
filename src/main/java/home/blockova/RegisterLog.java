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
public class RegisterLog {
    
    public long logID;
    public String login1;
    public String login2;
    public String login3;
    public String status1;
    public String status2;
    public String status3;
    public int blocek_id;

    @Override
    public String toString() {
        return logID+" "+blocek_id;
    }
    
    
    
}
