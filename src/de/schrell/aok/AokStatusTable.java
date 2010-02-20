package de.schrell.aok;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.TableModel;

public class AokStatusTable {

	Aok aok;

	/** the tables for debug displays */
	JTable table;
	JLabel label;
	JScrollPane spane;

	JLabel getLabel() {
		return label;
	}

	void setFreq(String freq) {
		if (freq == null)
			label.setText("Debug Values");
		else
			label.setText(String.format("Debug Values [%s]", freq));
	}

	JScrollPane getPane() {
		return spane;
	}

	AokStatusTable(Aok aok) {

		this.aok = aok;
		aok.ast = this;

		/** The tables are driven by TableModels. One for the state values */
		TableModel model = new StatusModel(aok);

		/** the Status table we put it into a scroll pane */
		table = new JTable(model);

		spane = new JScrollPane(table);
		spane.setPreferredSize(new Dimension(240, 20));

		table.getColumn(model.getColumnName(0)).setPreferredWidth(30);
		table.getColumn(model.getColumnName(1)).setPreferredWidth(120);
		table.getColumn(model.getColumnName(2)).setPreferredWidth(90);

		label = new JLabel();
		setFreq(null);
		label.setFont(new Font("", Font.BOLD, 12));
		label.setPreferredSize(new Dimension(240, 20));

	}

	/**
	 * set a state value in the table. This function is thread safe.
	 * 
	 * @param nr
	 *            number of the value to set
	 * @param value
	 *            the state value
	 */
	public void setValueAt(final int nr, final int value) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				table.setValueAt(new Integer(value).toString(), nr, 2);
			}
		});
	}

	public boolean isStatusSelected(int nr) {
		return table.isRowSelected(nr);
	}

	public boolean isStatusSelected() {
		return (table.getSelectedRowCount() > 0);
	}

	public int getStatusSelectedCount() {
		return table.getSelectedRowCount();
	}

}
