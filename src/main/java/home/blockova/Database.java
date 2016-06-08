/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package home.blockova;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.hsqldb.jdbc.JDBCDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

/**
 *
 * @author Janco1
 */
public class Database {

    private JdbcTemplate jdbcTemplate;
    private PropertyChangeSupport changes = new PropertyChangeSupport(this);
    private Iterable<Predajca> predajcovia;
    private String POSLEDNA_REGISTRACIA = "2014-07-07 00:00:00";
    private String PREDPOSLEDNA_REGISTRACIA = "2014-07-07 00:00:00";
    Date poslednaRegistracia;
    Date najblizsiaRegistracia;
    private int pocetPovolenychUserov = 1;

    public Database() {
        BufferedReader f = null;
        Date dnesny = new Date(System.currentTimeMillis());
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            poslednaRegistracia = new Date(0);
            najblizsiaRegistracia = sdf.parse("1.1.2020");

            JDBCDataSource dataSource = new JDBCDataSource();
            dataSource.setUrl("jdbc:hsqldb:hsql://localhost/blockovadb");
            dataSource.setUser("sa");
            dataSource.setPassword("");
            jdbcTemplate = new JdbcTemplate(dataSource);
            // nacitat datumy zrebovani

            f = new BufferedReader(new FileReader(new File("plan_zrebovani.txt")));
            while (true) {
                StringTokenizer st = null;
                try {
                    String line = f.readLine();
                    if (line == null) {
                        break;
                    }
                    Date dat = new Date(System.currentTimeMillis());

                    Calendar c = Calendar.getInstance();
                    try {
                        c.setTime(sdf.parse(line));
                        dat = sdf.parse(sdf.format(c.getTime()));
                    } catch (ParseException ex) {
                        Logger.getLogger(EditBlocekForm.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    //System.out.println(line + "= " + sdf.format(dat));
                    if (dat.before(dnesny)) {
                        //System.out.println("before "+sdf.format(dnesny)+" je "+sdf.format(dat));
                        if (dat.after(poslednaRegistracia)) {
                            poslednaRegistracia = dat;
                        }
                    } else {
                        //System.out.println("after "+sdf.format(dnesny)+" je "+sdf.format(dat));
                    }
                    if (dat.after(dnesny)) {
                        //System.out.println("before "+sdf.format(dnesny)+" je "+sdf.format(dat));
                        if (dat.before(najblizsiaRegistracia)) {
                            najblizsiaRegistracia = dat;
                        }
                    }
                } catch (Exception exception) {
                    break;
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                f.close();
            } catch (IOException ex) {
                Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
        POSLEDNA_REGISTRACIA = sdf.format(poslednaRegistracia);
        System.out.println("posledna registracia: " + POSLEDNA_REGISTRACIA);
        // teraz skontrolujeme ci nam neostali este nejake nezaregistrovane blocky z predosleho zrebovania
        // TODO: co ak blocek zaregistrujem, ale iba na jeden ucet? tak bude zaregistrovany iba na jednom ucte a nebudem ho riesit
        checkCiSuNezaregistrovaneBlockyPredPoslednouRegistraciou();

    }

    /**
     * selects all incidents within last X days, and updates user priorities counts
     *
     * @param lastDays
     * @return
     */
    public List<Blocek> getBlocekList() {
        try {
            RowMapper<Blocek> rowMapper = new BlocekRowMapper();
            String sql = "SELECT * FROM blocky where time_inserted > timestamp('" + POSLEDNA_REGISTRACIA + "')";
            List<Blocek> blocky = jdbcTemplate.query(sql, rowMapper);
            return blocky;
        } catch (Exception exception) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, exception);
        }
        return null;
    }

    public int getPocetBlockov() {
        try {
            RowMapper<Blocek> rowMapper = new BlocekRowMapper();
            String sql = "SELECT * FROM blocky ";
            List<Blocek> blocky = jdbcTemplate.query(sql, rowMapper);
            return blocky.size();
        } catch (Exception exception) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, exception);
        }
        return 0;
    }

    /**
     * inserts emails into DB 1- check DB for duplicity 2- if not already in, insert 3- create
     * incident, find appropriate alarm which email is related to
     *
     * @param emails
     */
    public void insertBlocekToDB(Blocek blocek) {
        RowMapper<Blocek> rowMapper = new BlocekRowMapper();

        StringBuilder sql = new StringBuilder("SELECT * FROM blocky WHERE ");
        sql.append("DKP='" + blocek.dkp + "'");
        sql.append(" AND datum='" + blocek.datum + "'");
        sql.append(" AND suma='" + blocek.suma + "'");
        //System.out.println("1. searching sql: " + sql.toString());
        List<Blocek> foundBlocky = jdbcTemplate.query(sql.toString(), rowMapper);

        if (foundBlocky.isEmpty()) {
            // INSERT blocek
            // System.out.println("INSERT SQL: " + sql.toString());
            // inserting new message
            SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate);
            insert.withTableName("blocky");
            insert.setGeneratedKeyName("blocek_id");
            Map<String, Object> mapa = new HashMap<String, Object>();
            mapa.put("dkp", blocek.dkp);
            mapa.put("datum", blocek.datum);
            mapa.put("suma", blocek.suma);
            mapa.put("pocet", blocek.pocet);
            mapa.put("registrator", blocek.registrator);
            mapa.put("time_makro_generated", blocek.timeMakroGenerated);
            mapa.put("time_inserted", blocek.timeInserted);
            //mapa.put("incident_ID", incidentID);
            Number insertedBlocekId = insert.executeAndReturnKey(mapa);

            // vytvor register log pre dany blocek
            insert = new SimpleJdbcInsert(jdbcTemplate);
            insert.withTableName("register_log");
            insert.setGeneratedKeyName("log_id");
            mapa = new HashMap<String, Object>();
            mapa.put("blocek_id", insertedBlocekId);
            mapa.put("login1", "");
            mapa.put("login2", "");
            mapa.put("login3", "");
            mapa.put("status1", "TODO");
            mapa.put("status2", "TODO");
            mapa.put("status3", "TODO");
            //mapa.put("incident_ID", incidentID);
            Number insertedLogId = insert.executeAndReturnKey(mapa);

        } else {
            // email already in DB
//                Warning wrn = new Warning(msg, "already");
//                wrn.getIds().add(foundBlocky.get(0).getEmailID());
//                warnings.add(wrn);
            System.out.println("ERROR");
            System.out.println("FOUND Blocek: " + foundBlocky.get(0).dkp + " " + foundBlocky.get(0).datum);
            System.out.println("2. message already in database: " + blocek.suma);
            System.out.println("3. sql search script: " + sql.toString());
        }

        changes.firePropertyChange("blocekAdded", false, true);
        //changes.firePropertyChange("predajcaAdded", false, true);
    }

    public void insertBlockyToDB(List<Blocek> blocky) {
        throw new UnsupportedOperationException("treba dorobit aby vytvorilo aj registracne logy pre dane blocky");
//        if (blocky.size() == 0) {
//            JOptionPane.showMessageDialog(null, "Prázdny súbor. ");
//            return;
//        }
//        // check ci su vsetky blocky nove
//        StringBuilder dkpcka = new StringBuilder("(");
//        StringBuilder datumy = new StringBuilder("(");
//        StringBuilder sumy = new StringBuilder("(");
//        for (Blocek b : blocky) {
//            dkpcka.append("'" + b.dkp + "',");
//            datumy.append("timestamp('" + b.datum + "'),");
//            sumy.append("" + b.suma + ",");
//        }
//        String dkp = dkpcka.substring(0, dkpcka.length() - 1) + ")";
//        String datum = datumy.substring(0, datumy.length() - 1) + ")";
//        String suma = sumy.substring(0, sumy.length() - 1) + ")";
//
//        RowMapper<Blocek> rowMapper = new BlocekRowMapper();
//
//        StringBuilder sql = new StringBuilder("SELECT * FROM blocky WHERE ");
//        sql.append("DKP in" + dkp);
//        sql.append(" AND datum in" + datum);
//        sql.append(" AND suma in" + suma);
//        System.out.println(sql.toString());
//        //System.out.println("1. searching sql: " + sql.toString());
//        List<Blocek> foundBlocky = jdbcTemplate.query(sql.toString(), rowMapper);
//        List<Blocek> toInsert = new ArrayList<Blocek>();
//        System.out.println("found blocky size: " + foundBlocky.size());
//        for (Blocek b : blocky) {
//            if (!obsahujeBlocek(foundBlocky, b)) {
//                toInsert.add(b);
//            }
//        }
//
//        //get najvyssie id
//        sql = new StringBuilder("select top 1  * from blocky order by blocek_id desc");
//        foundBlocky = jdbcTemplate.query(sql.toString(), rowMapper);
//        int topID = foundBlocky.get(0).id;
//
//        // insert blocky
//        sql = new StringBuilder();
//        for (int i = 0; i < toInsert.size(); i++) {
//            Blocek akt = toInsert.get(i);
//            sql.append("insert into blocky values('" + akt.dkp + "', '" + (int) (topID + i + 1) + "','" + akt.datum + "', '" + akt.suma + "', '" + akt.pocet + "', '" + akt.registrator + "', null, '" + akt.timeInserted + "')\n");
//        }
//        System.out.println(sql.toString());
//        if (!sql.toString().equalsIgnoreCase("")) {
//            jdbcTemplate.queryForRowSet(sql.toString());
//        }
//        JOptionPane.showMessageDialog(null, "bolo vlozenych " + toInsert.size() + " blockov. " + (blocky.size() - toInsert.size()) + " blockov uz je v databaze.");
//
//        changes.firePropertyChange("blocekAdded", false, true);
//
//        // check ci su novi predajcovia
//        predajcovia = getPredajcaList();
//        List<Predajca> noviPredajcovia = new ArrayList<Predajca>();
//        for (Blocek b : blocky) {
//            if (jeNovyPredajca(b.dkp)) {
//                System.out.println("DB: je novy predajca");
//                noviPredajcovia.add(new Predajca(b.dkp, b.dkp));
//            }
//        }
//
//        insertPredajcaToDB(noviPredajcovia);
//        //changes.firePropertyChange("predajcaAdded", false, true);
    }

    public void deleteBlocekFromDB(Blocek blocek) {

        StringBuilder sql = new StringBuilder("DELETE from blocky WHERE ");
        sql.append("DKP='" + blocek.dkp + "'");
        sql.append(" AND datum='" + blocek.datum + "'");
        sql.append(" AND suma='" + blocek.suma + "'\n");
        sql.append("delete from register_log where blocek_id=" + blocek.id + "\n");
        //System.out.println("1. searching sql: " + sql.toString());
        jdbcTemplate.execute(sql.toString());

        changes.firePropertyChange("blocekAdded", false, true);
        changes.firePropertyChange("predajcaAdded", false, true);
        changes.firePropertyChange("logAdded", false, true);
    }

    public void deletePredajcaFromDB(Predajca predajca) {

        StringBuilder sql = new StringBuilder("DELETE from predajne WHERE ");
        sql.append("DKP='" + predajca.dkp + "'");
        sql.append(" AND meno='" + predajca.meno + "'");
        //System.out.println("1. searching sql: " + sql.toString());
        jdbcTemplate.execute(sql.toString());

        changes.firePropertyChange("predajcaAdded", false, true);
    }

    public void updateBlocekFromDB(Blocek blocek) {
        StringBuilder sql;
        if (blocek.timeMakroGenerated != null) {
            sql = new StringBuilder("UPDATE blocky SET dkp= '" + blocek.dkp + "' , datum='" + blocek.datum + "' , pocet= " + blocek.pocet + " , suma="
                    + blocek.suma + ", time_makro_generated=timestamp('" + blocek.timeMakroGenerated + "'), time_inserted=timestamp('"
                    + blocek.timeInserted + "'), registrator='" + blocek.registrator + "' WHERE ");
            sql.append("blocek_id=" + blocek.id + "");
        } else {
            sql = new StringBuilder("UPDATE blocky SET dkp= '" + blocek.dkp + "' , datum='" + blocek.datum + "' , pocet= " + blocek.pocet + " , suma="
                    + blocek.suma + ", time_inserted=timestamp('"
                    + blocek.timeInserted + "'), registrator='" + blocek.registrator + "' WHERE ");
            sql.append("blocek_id=" + blocek.id + "");
        }
        //System.out.println("1. searching sql: " + sql.toString());
        jdbcTemplate.execute(sql.toString());

        changes.firePropertyChange("blocekAdded", false, true);
    }

    public void updateBlocekFromDBNOFirePropertyChange(Blocek blocek) {
        StringBuilder sql;
        if (blocek.timeMakroGenerated != null) {
            sql = new StringBuilder("UPDATE blocky SET dkp= '" + blocek.dkp + "' , datum='" + blocek.datum + "' , pocet= " + blocek.pocet + " , suma="
                    + blocek.suma + ", time_makro_generated=timestamp('" + blocek.timeMakroGenerated + "'), time_inserted=timestamp('"
                    + blocek.timeInserted + "'), registrator='" + blocek.registrator + "' WHERE ");
            sql.append("blocek_id=" + blocek.id + "");
        } else {
            sql = new StringBuilder("UPDATE blocky SET dkp= '" + blocek.dkp + "' , datum='" + blocek.datum + "' , pocet= " + blocek.pocet + " , suma="
                    + blocek.suma + ", time_inserted=timestamp('"
                    + blocek.timeInserted + "'), registrator='" + blocek.registrator + "' WHERE ");
            sql.append("blocek_id=" + blocek.id + "");
        }
        //System.out.println("1. searching sql: " + sql.toString());
        jdbcTemplate.execute(sql.toString());

        //changes.firePropertyChange("blocekAdded", false, true);
    }

    public void updateLoginFromDB(String meno, String heslo, int id) {
        StringBuilder sql = new StringBuilder("UPDATE loginy SET meno= '" + meno + "' , heslo='" + heslo + "'  WHERE ");
        sql.append("login_id=" + id + "");
        //System.out.println("1. searching sql: " + sql.toString());
        jdbcTemplate.execute(sql.toString());

        changes.firePropertyChange("loginAdded", false, true);
    }

    public void updateRegisterLogFromDB(String login, String loginText, String status, String statusText, int blocek_id) {
        StringBuilder sql = new StringBuilder("UPDATE register_log SET " + status + "= '" + statusText + "' , " + login + "='" + loginText + "'  WHERE ");
        sql.append("blocek_id=" + blocek_id + "");
        //System.out.println("1. searching sql: " + sql.toString());
        jdbcTemplate.execute(sql.toString());

        changes.firePropertyChange("logAdded", false, true);
    }

    public void updateRegisterLogFromDBNOFirePropertyChange(String login, String loginText, String status, String statusText, int blocek_id) {
        StringBuilder sql = new StringBuilder("UPDATE register_log SET " + status + "= '" + statusText + "' , " + login + "='" + loginText + "'  WHERE ");
        sql.append("blocek_id=" + blocek_id + "");
        //System.out.println("1. searching sql: " + sql.toString());
        jdbcTemplate.execute(sql.toString());

        //changes.firePropertyChange("logAdded", false, true);
    }

    List<Login> getLoginList() {
        try {
            RowMapper<Login> rowMapper = new LoginRowMapper();
            String sql = "SELECT * FROM loginy ";
            List<Login> loginy = jdbcTemplate.query(sql, rowMapper);
            return loginy;
        } catch (Exception exception) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, exception);
        }
        return null;
    }

    /**
     * pouziva sa iba raz ked si potrebujem do uloh nacitat blocky co nesu zaregistrovane
     *
     * @return
     */
    List<RegisterLog> getNotRegisteredLogList() {
        try {
            RowMapper<RegisterLog> rowMapper = new RegisterLogRowMapper();
            String sql = "SELECT * FROM register_log R JOIN blocky B on B.blocek_id=R.blocek_id where status1='TODO' or status2='TODO' or status3='TODO' and B.time_inserted > timestamp('" + POSLEDNA_REGISTRACIA + "') ";
            List<RegisterLog> loginy = jdbcTemplate.query(sql, rowMapper);
            return loginy;
        } catch (Exception exception) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, exception);
        }
        return null;
    }

    RegisterLog getNotRegisteredLogList(int blocek_id) {
        try {
            RowMapper<RegisterLog> rowMapper = new RegisterLogRowMapper();
            String sql = "SELECT * FROM register_log where blocek_id='" + blocek_id + "'";
            List<RegisterLog> loginy = jdbcTemplate.query(sql, rowMapper);
            return loginy.get(0);
        } catch (Exception exception) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, exception);
        }
        return null;
    }

    /**
     * pouziva sa na zobrazenie do tabulky
     *
     * @return
     */
    public List<RegisterLog> getRegisterLogList() {
        try {
            RowMapper<RegisterLog> rowMapper = new RegisterLogRowMapper();
            String sql = "SELECT * FROM register_log R JOIN blocky B on B.blocek_id=R.blocek_id where B.time_inserted > timestamp('" + POSLEDNA_REGISTRACIA + "')";
            List<RegisterLog> loginy = jdbcTemplate.query(sql, rowMapper);
            return loginy;
        } catch (Exception exception) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, exception);
        }
        return null;
    }

    public void updatePredajcaFromDB(Predajca predajca) {
        StringBuilder sql = new StringBuilder("UPDATE predajne SET dkp= '" + predajca.dkp + "' , meno='" + predajca.meno + "'  WHERE ");
        sql.append("predajca_id=" + predajca.id + "");
        //System.out.println("1. searching sql: " + sql.toString());
        jdbcTemplate.execute(sql.toString());

        changes.firePropertyChange("predajcaAdded", false, true);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changes.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changes.removePropertyChangeListener(listener);
    }

    void insertPredajcaToDB(Predajca novy) {
        RowMapper<Predajca> rowMapper = new PredajcaRowMapper();

        StringBuilder sql = new StringBuilder("SELECT * FROM predajne WHERE ");
        sql.append("DKP='" + novy.dkp + "'");
        sql.append(" AND meno='" + novy.meno + "'");
        //System.out.println("1. searching sql: " + sql.toString());
        List<Predajca> foundPredajcovia = jdbcTemplate.query(sql.toString(), rowMapper);

        if (foundPredajcovia.isEmpty()) {
            // INSERT blocek
            // System.out.println("INSERT SQL: " + sql.toString());
            // inserting new message
            SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate);
            insert.withTableName("predajne");
            insert.setGeneratedKeyName("predajca_id");
            Map<String, Object> mapa = new HashMap<String, Object>();
            mapa.put("dkp", novy.dkp);
            mapa.put("meno", novy.meno);
            //mapa.put("incident_ID", incidentID);
            Number insertedBlocekId = insert.executeAndReturnKey(mapa);
            System.out.println("pridany predajca " + novy.dkp + " s idckom: " + insertedBlocekId);
        } else {
            // email already in DB
//                Warning wrn = new Warning(msg, "already");
//                wrn.getIds().add(foundBlocky.get(0).getEmailID());
//                warnings.add(wrn);
            System.out.println("ERROR");
            System.out.println("FOUND Predajca: " + foundPredajcovia.get(0).dkp + " " + foundPredajcovia.get(0).dkp);
            System.out.println("2. message already in database: " + novy.meno);
            System.out.println("3. sql search script: " + sql.toString());
        }

        changes.firePropertyChange("predajcaAdded", false, true);
    }

    public List<Predajca> getPredajcaList() {
        try {
            RowMapper<Predajca> rowMapper = new PredajcaRowMapper();
            String sql = "SELECT * FROM predajne ";
            List<Predajca> predajcovia = jdbcTemplate.query(sql, rowMapper);
            return predajcovia;
        } catch (Exception exception) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, exception);
        }
        return null;
    }

    Predajca getPredajca(String oldPredajca) {
        try {
            RowMapper<Predajca> rowMapper = new PredajcaRowMapper();
            String sql = "SELECT * FROM predajne where dkp='" + oldPredajca + "'";
            List<Predajca> predajcovia = jdbcTemplate.query(sql, rowMapper);
            return predajcovia.get(0);
        } catch (Exception exception) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, exception);
        }
        return null;
    }

    void updateBlocekFromDB(List<Blocek> toUpdate) {
        if (toUpdate.size() == 0) {
            JOptionPane.showMessageDialog(null, "Žiaden bloček nevybraný. ");
            return;
        }
        StringBuilder sql = new StringBuilder();
        for (Blocek blocek : toUpdate) {
            sql.append("UPDATE blocky SET dkp= '" + blocek.dkp + "' , datum='" + blocek.datum + "' , pocet= " + blocek.pocet + " , suma=" + blocek.suma + ", time_makro_generated='" + blocek.timeMakroGenerated + "', time_inserted='" + blocek.timeInserted + "' WHERE ");
            sql.append("blocek_id=" + blocek.id + "\n");
        }
        jdbcTemplate.execute(sql.toString());

        changes.firePropertyChange("blocekAdded", false, true);
    }

    private boolean obsahujeBlocek(List<Blocek> foundBlocky, Blocek b) {
        for (Blocek blocek : foundBlocky) {
            if (blocek.dkp.equalsIgnoreCase(b.dkp) && blocek.datum.substring(0, 16).equalsIgnoreCase(b.datum.substring(0, 16)) && blocek.suma == b.suma) {
                return true;
            }
        }

        return false;
    }

    private boolean jeNovyPredajca(String dkp) {
        for (Predajca p : predajcovia) {
            if (p.dkp.equalsIgnoreCase(dkp)) {
                return false;
            }
        }
        return true;
    }

    /**
     * METODA NEKONTROLUJE UNIKATNOST PREDAJCU
     *
     * @param noviPredajcovia
     */
    private void insertPredajcaToDB(List<Predajca> noviPredajcovia) {
        RowMapper<Predajca> rowMapper = new PredajcaRowMapper();
        //get najvyssie id
        StringBuilder sql = new StringBuilder("select top 1  * from PREDAJNE order by PREDAJCA_id desc");
        List<Predajca> foundPredajne = jdbcTemplate.query(sql.toString(), rowMapper);
        long topID = foundPredajne.get(0).id;

        // insert predajcov
        sql = new StringBuilder();
        for (int i = 0; i < noviPredajcovia.size(); i++) {
            Predajca akt = noviPredajcovia.get(i);
            sql.append("insert into predajne values('" + akt.dkp + "', '" + (int) (topID + i + 1) + "','" + akt.meno + "')\n");
        }
        //System.out.println(sql.toString());
        if (!sql.toString().equalsIgnoreCase("")) {
            jdbcTemplate.queryForRowSet(sql.toString());
        }
        JOptionPane.showMessageDialog(null, "bolo vlozenych " + noviPredajcovia.size() + " predajcov. ");
        changes.firePropertyChange("predajcaAdded", false, true);
    }

    int getAktualneRegistrovanych() {
        try {
            RowMapper<Blocek> rowMapper = new BlocekRowMapper();
            String sql = "SELECT * FROM blocky where time_inserted > timestamp('" + POSLEDNA_REGISTRACIA + "') ";
            List<Blocek> blocky = jdbcTemplate.query(sql, rowMapper);
            return blocky.size();
        } catch (Exception exception) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, exception);
        }
        return 0;
    }

//    List<RegisterLog> getRegisteredLogList(ArrayList<Integer> cislaZaregistrovanychBlockov) {
//        StringBuilder cislaBlockov = new StringBuilder();
//        for (Integer in : cislaZaregistrovanychBlockov) {
//            cislaBlockov.append(Integer.toString(in) + ",");
//        }
//        String cBlockov = cislaBlockov.substring(0, cislaBlockov.length() - 1);
//        try {
//            RowMapper<RegisterLog> rowMapper = new RegisterLogRowMapper();
//            String sql = "SELECT * FROM register_log WHERE blocek_id IN (" + cBlockov + ")";
//            List<RegisterLog> loginy = jdbcTemplate.query(sql, rowMapper);
//            return loginy;
//        } catch (Exception exception) {
//            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, exception);
//        }
//        return null;
//    }
    void updateBlocekPocty(Map<Integer, Integer> poctyRegistraci) {
        if (poctyRegistraci.size() == 0) {
            System.out.println("Ziadne blocky nezaregistrovane");
            return;
        }
        StringBuilder sql = new StringBuilder();
        for (Integer blocek_id : poctyRegistraci.keySet()) {
            sql.append("UPDATE blocky SET  pocet= " + poctyRegistraci.get(blocek_id) + " WHERE blocek_id=" + blocek_id + "\n");
        }
        jdbcTemplate.execute(sql.toString());

        changes.firePropertyChange("blocekAdded", false, true);
    }

    RegisterLog getRegisterLog(int blocek_id) {
        try {
            RowMapper<RegisterLog> rowMapper = new RegisterLogRowMapper();
            String sql = "SELECT * FROM register_log WHERE blocek_id=" + blocek_id + "";
            List<RegisterLog> loginy = jdbcTemplate.query(sql, rowMapper);
            return loginy.get(0);
        } catch (Exception exception) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, exception);
        }
        return null;
    }

    /**
     * aby sme vedeli updatnut v tabulke blockov ktory kolko krat bol zaregistrovany TODO: my budeme
     * vzdy vytahovat len blocky podla toho kedy boli naposledy registrovane, ale v tomto pripade
     * berieme do uvahy vsetky blocky bez casoveho obmedzenia takze teoreticky mozeme mat nadbytocne
     * udaje
     *
     * @return
     */
    List<RegisterLog> getRegisterLogListWherePocetIsNot3() {
        try {
            RowMapper<RegisterLog> rowMapper = new RegisterLogRowMapper();
            String sql = "SELECT * FROM register_log r JOIN blocky b ON r.blocek_id=b.blocek_id WHERE pocet<3 ";
            List<RegisterLog> loginy = jdbcTemplate.query(sql, rowMapper);
            return loginy;
        } catch (Exception exception) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, exception);
        }
        return null;
    }

    int getTopRegisteredBlocekID() {
        //get najvyssie id
        RowMapper<RegisterLog> rowMapper = new RegisterLogRowMapper();
        StringBuilder sql = new StringBuilder("select top 1 * from register_log where status1!='TODO' and status2!='TODO' and status3!='TODO' order by blocek_id desc");
        List<RegisterLog> foundBlocky = jdbcTemplate.query(sql.toString(), rowMapper);
        if (foundBlocky.size() == 0) {
            return 0;
        }
        int topID = foundBlocky.get(0).blocek_id;
        return topID;
    }

    private void checkCiSuNezaregistrovaneBlockyPredPoslednouRegistraciou() {
        // nastavime poslednu registraciu na datum vlozenia najstarsieho blocku, ktory nie je zaregistrovany 
        try {
            RowMapper<Blocek> rowMapper = new BlocekRowMapper();
            String sql = "SELECT * FROM blocky where time_inserted < timestamp('" + POSLEDNA_REGISTRACIA + "') and time_makro_generated is null order by time_inserted";
            List<Blocek> blocky = jdbcTemplate.query(sql, rowMapper);
            if (blocky.size() > 0) {

                System.out.println("nasiel sa starsi blocek ako bola posledna registracia: " + POSLEDNA_REGISTRACIA);
                POSLEDNA_REGISTRACIA = blocky.get(0).datum.substring(0, 10) + " 00:00:00.0";
                System.out.println("nova posledna registracia: " + POSLEDNA_REGISTRACIA);
            }
        } catch (Exception exception) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, exception);
        }
    }

    void getUsers(String hostname) {
        RowMapper<String> rowMapper = new RowMapper() {

            public Object mapRow(ResultSet rs, int i) throws SQLException {
                return rs.getString(1);
            }
        };
        List<String> users = null;
        // najprv musime iba getnut userov a potom porovnavat ci tam uz je alebo nie

        StringBuilder sql = new StringBuilder("select * from users");
        try {
            users = jdbcTemplate.query(sql.toString(), rowMapper);
            // tabulka existuje, lebo neni vynimka, ideme sa hladat
            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).equalsIgnoreCase(hostname)) {
                    // nasli sme sa
                    System.out.println("nasli sme sa v zozname userov:users check OK!");
                    return;
                }
            }
            // nenasli sme sa v zozname userov, check ci mozeme pridat noveho usera
            if (users.size() >= pocetPovolenychUserov) {
                JOptionPane.showMessageDialog(null, "Príliš veľa používateľov. Program skončí.");
                System.exit(0);
            } else {
                // nie sme v zozname userov, ale sa este mozeme pridat
                sql = new StringBuilder("insert into users values('" + hostname + "'); select * from users");
                users = jdbcTemplate.query(sql.toString(), rowMapper);
                System.out.println("neboli sme v zozname, pridali sme sa: users check OK!");
                return;
            }
        } catch (Exception exception) {
            // nemame tabulku userov
            System.out.println("nemame tabulku userov" + exception);
            sql = new StringBuilder("CREATE TABLE users (pouzivatel varchar(255) NOT NULL UNIQUE); ");
            jdbcTemplate.execute(sql.toString());
            sql = new StringBuilder("insert into users values('" + hostname + "'); select * from users");
            users = jdbcTemplate.query(sql.toString(), rowMapper);
            System.out.println("vytvorena tabulka userov: users check OK! " + users);
        }
        return;
    }

}
