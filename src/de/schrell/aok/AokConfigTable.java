package de.schrell.aok;

import java.awt.*;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.table.TableModel;

public class AokConfigTable {

	Aok aok;

	/** the table for configuration displays */
	JTable table;
	JLabel label;
	TableModel model;
	JScrollPane spane;

	JLabel getLabel() {
		return label;
	}

	JScrollPane getPane() {
		return spane;
	}
	
	TableModel getModel() {
		return model;
	}

	AokConfigTable(final Aok aok) {

		this.aok = aok;
		aok.act = this;

		/** The tables are driven by TableModels. One for the state values */
		model = new ConfigModel(aok);

		/** the Status table we put it into a scroll pane */
		table = new JTable(model) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 2398877412110845066L;

			public JToolTip createToolTip() {
				JToolTip tip = super.createToolTip();
				tip.setMaximumSize(new Dimension(800, 800));
				tip.setMinimumSize(new Dimension(800, 800));
				tip.setPreferredSize(new Dimension(800, 800));
				ToolTipManager.sharedInstance().setDismissDelay(20000);
				ToolTipManager.sharedInstance().setInitialDelay(2000);
				return tip;
			}

			public String getToolTipText(MouseEvent e) {
				String tip = null;
				java.awt.Point p = e.getPoint();
				int rowIndex = rowAtPoint(p);
				int colIndex = columnAtPoint(p);
				int realRowIndex = convertRowIndexToModel(rowIndex);
				int realColIndex = convertColumnIndexToModel(colIndex);
				if (realColIndex == 0) {
					String name = aok.getAokConfigOrderedName(realRowIndex);
					if (name == null)
						return null;
					int num = aok.getAokConfigNumber(name);
					if (num > -1)
						tip = aok.getConfigToolTip(num);
				}
				return tip;
			}

		};

		spane = new JScrollPane(table);
		spane.setPreferredSize(new Dimension(240, 20));
		table.getColumn(model.getColumnName(0)).setPreferredWidth(30);
		table.getColumn(model.getColumnName(1)).setPreferredWidth(120);
		table.getColumn(model.getColumnName(2)).setPreferredWidth(90);

		label = new JLabel("Configuration Variables");
		int fs = label.getFont().getSize();
		label.setFont(new Font("", Font.BOLD, fs));
		label.setPreferredSize(new Dimension(240, 20));
	}

	/**
	 * set a configuration value in the table. This function is thread safe.
	 * 
	 * @param nr
	 *            number of the value to set
	 * @param value
	 *            the configuration value
	 */
	public void setConfigAt(final int row, final int value) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
//				System.out.println("AokConfigTable.setConfigAt("+row+","+value);
                aok.setAokConfig(aok.convertConfigFromView(row),new Integer(value) );
				table.setValueAt(new Integer(value).toString(), row, 2);
			}
		});
	}

	/**
	 * returns the configuration value from the displayed table
	 * 
	 * @param nr
	 *            number of config value
	 * @return the config value
	 */
	public int XgetConfigAt(int row) {
		return new Integer((String) table.getValueAt(row, 2));
	}

	public boolean isConfigSelected(int row) {
		return table.isRowSelected(row);
	}

	public boolean isConfigSelected() {
		return (table.getSelectedRowCount() > 0);
	}

	public int getConfigSelectedCount() {
		return table.getSelectedRowCount();
	}
	
	public void clearSelection() {
		table.clearSelection();
	}

}
