/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package home.blockova;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author Janco1
 */
class RegisterLogRowMapper implements RowMapper<RegisterLog> {

    public RegisterLogRowMapper() {
    }

    public RegisterLog mapRow(ResultSet rs, int i) throws SQLException {
         RegisterLog login = new RegisterLog();
        login.blocek_id = Integer.parseInt(rs.getString("blocek_id"));
        login.login1 = rs.getString("login1");
        login.login2 = rs.getString("login2");
        login.login3 = rs.getString("login3");
        login.status1 = rs.getString("status1");
        login.status2 = rs.getString("status2");
        login.status3 = rs.getString("status3");
        login.logID=Integer.parseInt(rs.getString("log_id"));
        return login;
    }
    
}
