/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package home.blockova;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Janco1
 */
class RegisterLogTableModel extends DefaultTableModel {

    private int COLUMN_COUNT;
    private int ROW_COUNT;
    private List<RegisterLog> registerLogItems = new ArrayList<RegisterLog>();
    Color[] rowColours = null;
    private Color[][] tableColors;

    public RegisterLogTableModel(String[] registerLogTableColumnNames, List<RegisterLog> registerLogItems) {
        super(registerLogTableColumnNames, registerLogItems.size());
        this.registerLogItems.addAll(registerLogItems);
        ROW_COUNT = registerLogItems.size();
       // rowColours = new Color[ROW_COUNT];
        COLUMN_COUNT = registerLogTableColumnNames.length;
        tableColors=new Color[ROW_COUNT][COLUMN_COUNT];
        //System.out.println(columnNames[1]);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Integer.class;
            case 1:
                return String.class;
            case 2:
                return String.class;
            case 3:
                return String.class;
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

//    public void setRowColour(int row, Color c) {
//        rowColours[row] = c;
//        fireTableRowsUpdated(row, row);
//    }
//    public Color getRowColour(int row) {
//        return rowColours[row];
//    }
    public Color getCellColour(int row, int column) {
        return tableColors[row][column];
    }

    public void setCellColour(int row, int column, Color c) {
        tableColors[row][column] = c;
        fireTableRowsUpdated(row, row);
    }

    @Override
    public int getRowCount() {
        return ROW_COUNT;
    }

    @Override
    public int getColumnCount() {
        return COLUMN_COUNT;
    }

    public void setRegisterLogItems(List<RegisterLog> rlog) {
        this.registerLogItems.clear();
        this.registerLogItems.addAll(rlog);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        RegisterLog rlog = registerLogItems.get(rowIndex);
        switch (columnIndex) {
            case 1:
                return rlog.status1;
            case 0:
                return rlog.blocek_id;
            case 2:
                return rlog.status2;
            case 3:
                return rlog.status3;
            default:
                return "invalid";
        }
    }
}
