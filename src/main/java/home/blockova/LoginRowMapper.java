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
class LoginRowMapper implements RowMapper<Login> {

    public LoginRowMapper() {
    }

    public Login mapRow(ResultSet rs, int i) throws SQLException {
        Login login = new Login();
        login.meno = rs.getString("meno");
        login.heslo = rs.getString("heslo");
        login.id=Integer.parseInt(rs.getString("login_id"));
        return login;
    }
    
}
