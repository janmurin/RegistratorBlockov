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
class PredajcaRowMapper implements RowMapper<Predajca> {

    public PredajcaRowMapper() {
    }

    public Predajca mapRow(ResultSet rs, int i) throws SQLException {
        Predajca predajca = new Predajca();
        predajca.dkp = rs.getString("DKP");
        predajca.meno = rs.getString("meno");
        predajca.id=Integer.parseInt(rs.getString("predajca_id"));
        return predajca;
    }

}
