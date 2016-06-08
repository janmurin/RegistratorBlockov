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
class BlocekRowMapper implements RowMapper<Blocek> {

    public BlocekRowMapper() {
    }

    public Blocek mapRow(ResultSet rs, int i) throws SQLException {
        Blocek blocek = new Blocek();
        blocek.dkp = rs.getString("DKP");
        blocek.datum = rs.getString("datum");
        blocek.suma = Double.parseDouble(rs.getString("suma"));
        blocek.pocet = Integer.parseInt(rs.getString("pocet"));
        blocek.id=Integer.parseInt(rs.getString("blocek_id"));
        blocek.registrator=rs.getString("registrator");
        blocek.timeMakroGenerated=rs.getString("time_makro_generated");
        blocek.timeInserted=rs.getString("time_inserted");
        return blocek;
    }

}
