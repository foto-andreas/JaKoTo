package de.schrell.aok;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class StatusModel extends AbstractTableModel {

	Aok aok = null;

	public StatusModel(Aok aok) {
		this.aok = aok;
	}

	public int getColumnCount() {
		return 3;
	}

	public int getRowCount() {
		return aok.getAokStateCount();
	}

	public Object getValueAt(int row, int col) {
		return aok.getAokStateTable()[row][col];
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		aok.getAokStateTable()[row][col] = value.toString();
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
}
