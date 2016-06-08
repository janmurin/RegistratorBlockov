/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package home.blockova;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Janco1
 */
class PredajcoviaTableModel extends DefaultTableModel {

    private int COLUMN_COUNT;
    private int ROW_COUNT;
    private List<Predajca> predajcovia = new ArrayList<Predajca>();
    Color[] rowColours = null;

    public PredajcoviaTableModel(Object[] columnNames, List<Predajca> prdjcs) {
        super(columnNames, prdjcs.size());
        predajcovia.addAll(prdjcs);
        ROW_COUNT = predajcovia.size();
        rowColours = new Color[ROW_COUNT];
        COLUMN_COUNT = columnNames.length;
        //System.out.println(columnNames[1]);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex){
      switch (columnIndex) {
                    case 0:
                        return String.class;
                    case 1:
                        return String.class;
                    case 2:
                        return Integer.class;
                    default:
                        return String.class;
                }
    }
    
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public Color[] getRowColours() {
        return rowColours;
    }

    public void setRowColour(int row, Color c) {
        rowColours[row] = c;
        fireTableRowsUpdated(row, row);
    }

    public Color getRowColour(int row) {
        return rowColours[row];
    }

    @Override
    public int getRowCount() {
        return ROW_COUNT;
    }

    @Override
    public int getColumnCount() {
        return COLUMN_COUNT;
    }

    public void setMessages(List<Predajca> predajcovia) {
        this.predajcovia.clear();
        this.predajcovia.addAll(predajcovia);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Predajca predajca = predajcovia.get(rowIndex);
        switch (columnIndex) {
            case 1:
                return predajca.dkp;
            case 0:
                return predajca.meno;
            case 2:
                return (Number) predajca.pocet;
            default:
                return "invalid";
        }
    }
}
