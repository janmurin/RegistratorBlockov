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
class BlockyTableModel extends DefaultTableModel {

    private int COLUMN_COUNT;
    private int ROW_COUNT;
    private List<Blocek> blocky = new ArrayList<Blocek>();
    Color[] rowColours = null;

    public BlockyTableModel(Object[] columnNames, List<Blocek> blcks) {
        super(columnNames, blcks.size());
        blocky.addAll(blcks);
        ROW_COUNT = blocky.size();
        rowColours = new Color[ROW_COUNT];
        COLUMN_COUNT = columnNames.length;
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
                return Double.class;
            case 4:
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

    public void setMessages(List<Blocek> blocky) {
        this.blocky.clear();
        this.blocky.addAll(blocky);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Blocek blocek = blocky.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return blocek.id;
            case 1:
                return blocek.dkp;
            case 2:
                return blocek.datum.substring(0, 16);
            case 3:
                return blocek.suma;
            case 4:
                return blocek.pocet;
            default:
                return "invalid";
        }
    }
}
