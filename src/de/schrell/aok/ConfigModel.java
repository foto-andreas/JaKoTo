package de.schrell.aok;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class ConfigModel extends AbstractTableModel {

    Aok aok = null;

    public ConfigModel(Aok aok) {
        this.aok = aok;
    }

    public int getColumnCount() {
        return 3;
    }

    public int getRowCount() {
        return aok.getAokConfigTableCount();
    }

    public Object getValueAt(int row, int col) {
        return aok.getAokConfigTable(row, col);
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        String x = value.toString();
        x.trim();
        x = x.replaceAll("^0+", "");
        x = x.replaceAll("^-0+", "-");
        if (x.equals("")) {
            x = "0";
        }
        if (x.matches("-{0,1}[0-9]{1,11}")) {
            aok.setAokConfigTable(row, col, x);
            int num = aok.convertConfigFromView(row);
            if (num > -1) {
                int v = new Integer(x);
// das hier gibt Endlosupdates in der Tabelle, wird - so scheint - es nicht gebraucht
                aok.setAokConfig(num, v);
//                System.out.println("SET R=" + row + "/N=" + num + "/V=" + value);
                if (num == Aok.CONFIG_OPTIONS) {
                    aok.ato.itemChanged(v);
                }
                if (num >= Aok.CONFIG_MIXER1 && num <= Aok.CONFIG_MIXER1 + 11) {
                    aok.atm.mixerSettingChanged(num - Aok.CONFIG_MIXER1, v);
                }
            }


        }
        this.fireTableCellUpdated(row, col);
    }

    @Override
    public String getColumnName(int c) {
        switch (c) {
            case 0:
                return "No.";
            case 1:
                return "Title";
            case 2:
                return "Value";
        }
        return "";
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        try {
            if (col == 2 && !aok.getAokConfigTable(row, 0).equals("")) {
                return true;
            } else {
                return false;
            }
        } catch (NullPointerException e) {
            return false;
        }
    }
}
