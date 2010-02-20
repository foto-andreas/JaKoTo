package de.schrell.aok;

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Options extends JPanel implements ItemListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7792970879298054222L;

	int bits = 32;
	int value = 0;
	Aok aok = null;

	JCheckBox[] boxen = new JCheckBox[bits];
	JLabel lab = new JLabel("---");

	/**
	 * @param aok
	 */
	public Options(Aok aok) {
		super(new GridLayout(11, 3));
		this.aok = aok;
		for (int i = 0; i < bits; i++) {
			boxen[i] = new JCheckBox("Bit " + i);
			boxen[i].addItemListener(this);
			add(boxen[i]);
		}
		add(lab);
	}

	public void setText(int i, String text) {
		boxen[i].setText(text);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		for (int i = 0; i < bits; i++) {
			if (e.getItemSelectable().equals(boxen[i])) {
				if (boxen[i].isSelected()) {
					value |= 1 << i;
				} else {
					value &= ~(1 << i);
				}
				lab.setText("<html><b>Options = " + String.valueOf(value)
						+ "</b></html>");
			}
		}
		aok.act.setConfigAt(aok.convertConfigToView(Aok.CONFIG_OPTIONS),value);
	}

	public void itemChanged(int value) {
		this.value = value;
		for (int i = 0; i < bits; i++) {
			boxen[i].setSelected((value & (1 << i)) != 0);
			lab.setText("<html><b>Options = " + String.valueOf(value)
					+ "</b></html>");
		}
	}

}
