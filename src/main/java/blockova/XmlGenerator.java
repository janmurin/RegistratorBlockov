/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blockova;

import home.blockova.Database;
import home.blockova.Blocek;
import home.blockova.Predajca;
import home.blockova.RegisterLog;
import java.util.List;

/**
 *
 * @author Janco1
 */
public class XmlGenerator {

    Database database;
    private int diff = 5;

    public XmlGenerator() {
        database = new Database();
    }

    public static void main(String[] args) {
        XmlGenerator xmlGen = new XmlGenerator();
        xmlGen.generateXml();
    }

    private void generateXml() {
        List<Blocek> blocky = database.getBlocekList();
        List<Predajca> predajcovia = database.getPredajcaList();
        List<RegisterLog> registerLog = database.getRegisterLogList();

        System.out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        System.out.println("<!-- This xml is generated with XmlGenerator.java -->");
        int indent = 0;

        printOpeningTagNoAttribute(indent, "REGISTRATOR_BLOCKOV");
        printSettings(indent + diff);
        printUsers(indent+ diff);
        printBlocky(indent+ diff, blocky);
        printPredajne(indent+ diff, predajcovia);
        printRegisterLogs(indent+ diff, registerLog);
        printClosingTag(indent, "REGISTRATOR_BLOCKOV");

    }

    private void printOpeningTagNoAttribute(int indent, String tagName) {
        System.out.println(getWhiteSpaces(indent) + "<" + tagName + ">");
    }

    private String getWhiteSpaces(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

    private void printSettings(int indent) {
        printOpeningTagNoAttribute(indent, "SETTINGS");
        printElement(indent + diff, "IS_DEMO", "true");
        printElement(indent + diff, "POCET_POVOLENYCH_USEROV", "1");
        printElement(indent + diff, "LIMIT_BLOCKOV", "50");
        printElement(indent + diff, "APP_ID", "4");
        printElement(indent + diff, "SYSOUT_ON", "true");
        printElement(indent + diff, "VERZIA", "2.0.4");
        printClosingTag(indent, "SETTINGS");

    }

    private void printElement(int indent, String elementName, String value) {
        System.out.println(getWhiteSpaces(indent) + "<" + elementName + ">" + value + "</" + elementName + ">");
    }

    private void printClosingTag(int indent, String tagName) {
        System.out.println(getWhiteSpaces(indent) + "</" + tagName + ">");
    }

    private void printUsers(int indent) {
        printOpeningTagNoAttribute(indent, "ALLOWED_USERS");
        printElement(indent + diff, "USERNAME", "JANCO1-PC");
        printClosingTag(indent, "ALLOWED_USERS");
    }

    private void printBlocky(int indent, List<Blocek> blocky) {
        printOpeningTagNoAttribute(indent, "BLOCKY");
        for (int i=0; i<2; i++){
            printBlocek(indent+diff,blocky.get(blocky.size()-1-i));
        }
        printClosingTag(indent, "BLOCKY");
    }

    private void printBlocek(int indent, Blocek blocek) {
        printOpeningTagWithAttribute(indent,"BLOCEK","id",""+blocek.id);
        printElement(indent+diff, "DKP", blocek.dkp);
        printElement(indent+diff, "SUMA", Double.toString(blocek.suma));
        printElement(indent+diff, "POCET_REGISTRACII", Integer.toString(blocek.pocet));
        printElement(indent+diff, "REGISTRATOR", blocek.registrator);
        
        printOpeningTagNoAttribute(indent+diff, "TIME_BLOCEK_GENERATED");
        printElement(indent+diff+diff, "DATETIME", blocek.datum);
        printDatum(indent+diff+diff, blocek.datum);
        printCas(indent+diff+diff, blocek.datum);
        printClosingTag(indent+diff, "TIME_BLOCEK_GENERATED");
        
        printOpeningTagNoAttribute(indent+diff, "TIME_MAKRO_GENERATED");
        printElement(indent+diff+diff, "DATETIME", blocek.timeMakroGenerated);
        printDatum(indent+diff+diff, blocek.timeMakroGenerated);
        printCas(indent+diff+diff, blocek.timeMakroGenerated);
        printClosingTag(indent+diff, "TIME_MAKRO_GENERATED");
        
        printOpeningTagNoAttribute(indent+diff, "TIME_BLOCEK_INSERTED");
        printElement(indent+diff+diff, "DATETIME", blocek.timeInserted);
        printDatum(indent+diff+diff, blocek.timeInserted);
        printCas(indent+diff+diff, blocek.timeInserted);
        printClosingTag(indent+diff, "TIME_BLOCEK_INSERTED");
        printClosingTag(indent, "BLOCEK");
    }

    private void printOpeningTagWithAttribute(int indent, String tagName, String attributeName, String attributeValue) {
        System.out.println(getWhiteSpaces(indent)+"<"+tagName+" "+attributeName+"='"+attributeValue+"'"+">");
    }

    private void printDatum(int indent, String datum) {
        printOpeningTagNoAttribute(indent, "DATUM");
        printElement(indent+diff, "DEN", datum.split(" ")[0].split("-")[2]);
        printElement(indent+diff, "MESIAC", datum.split(" ")[0].split("-")[1]);
        printElement(indent+diff, "ROK", datum.split(" ")[0].split("-")[0]);
        printClosingTag(indent, "DATUM");
    }

    private void printCas(int indent, String datum) {
        printOpeningTagNoAttribute(indent, "CAS");
        printElement(indent+diff, "HODINA", datum.split(" ")[1].split(":")[0]);
        printElement(indent+diff, "MINUTA", datum.split(" ")[1].split(":")[1]);
        printClosingTag(indent, "CAS");
    }

    private void printPredajne(int indent, List<Predajca> predajne) {
        printOpeningTagNoAttribute(indent, "PREDAJNE");
        for (int i=1; i<3; i++){
            printPredajna(indent+diff,predajne.get(i));
        }
        printClosingTag(indent, "PREDAJNE");
    }

    private void printPredajna(int indent, Predajca predajna) {
        printOpeningTagWithAttribute(indent, "PREDAJNA", "id", Integer.toString((int) predajna.id));
        printElement(indent+diff, "DKP", predajna.dkp);
        printElement(indent+diff, "MENO", predajna.meno);
        printClosingTag(indent, "PREDAJNA");
    }

    private void printRegisterLogs(int indent, List<RegisterLog> registerLogs) {
        printOpeningTagNoAttribute(indent, "REGISTER_LOG");
        for (int i=1; i<3; i++){
            printRegisterLog(indent+diff,registerLogs.get(i));
        }
        printClosingTag(indent, "REGISTER_LOG");
    }

    private void printRegisterLog(int indent, RegisterLog log) {
        printOpeningTagWithAttribute(indent, "LOG", "id", Integer.toString((int) log.logID));
        printElement(indent+diff, "BLOCEK_ID", Integer.toString(log.blocek_id));
        printElement(indent+diff, "LOGIN1", log.login1);
        printElement(indent+diff, "STATUS1", log.status1);
        printElement(indent+diff, "LOGIN2", log.login2);
        printElement(indent+diff, "STATUS2", log.status2);
        printElement(indent+diff, "LOGIN3", log.login3);
        printElement(indent+diff, "STATUS3", log.status3);
        printClosingTag(indent, "LOG");
    }
}
