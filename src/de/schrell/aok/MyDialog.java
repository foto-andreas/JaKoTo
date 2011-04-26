package de.schrell.aok;

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;

/**
 * Anzeigen und Auswerten einer Dialogbox. Durch Strings kann Aussehen von Text
 * und Buttons bestimmt werden. Die Numemr des Buttons wird zurückgeliefert.
 * 
 * @author Andreas Schrell
 * 
 */
public class MyDialog {

	JButton[] butts;
	JLabel[] labels;
	int returnValue = -1;
	JDialog dial;

	/**
	 * Ein Dialog wird erzeugt, aber noch nicht angezeigt. Für die Anzeige ist
	 * die Methode show() zu benutzen.
	 * 
	 * @param title
	 *            der Titel des Dialogfensters
	 * @param infostring
	 *            der Textstring, Zeilen können durch | (pipe) abgeteilt werden
	 * @param buttonstring
	 *            der String mit den Buttontexten, Trennung jeweils durch |
	 *            (pipe)
	 * @param modal
	 *            Auswahl, ob der Dialog modal behandelt werden soll
	 */
	public MyDialog(String title, String infostring, String buttonstring,
			boolean modal) {

		// Strings anhand der Trennzeichen in Array wandeln
		String[] infos = infostring.split("\\|");
		String[] buttons = buttonstring.split("\\|");

		// Dialog erzeugen und Titel setzen
		dial = new JDialog((JFrame) null, modal);
		dial.setTitle(title);

		// Dialog bei der Maus positionieren
		Point p = MouseInfo.getPointerInfo().getLocation();
		p.x -= 100;
		p.y -= 100;
		dial.setLocation(p);

		// Layout erstellen
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		dial.setLayout(gbl);

		gbc.insets = new Insets(5, 5, 0, 5);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.weighty = 0;

		// Text ausgeben
		labels = new JLabel[infos.length];
		for (int i = 0; i < labels.length; i++) {
			labels[i] = new JLabel(infos[i]);
			dial.add(labels[i], gbc);
			gbc.gridy++;
		}

		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;

		// Button-Panel ausgeben
		JPanel buttonPanel = new JPanel();
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.weightx = 0;
		gbc.weighty = 0;
		dial.add(buttonPanel, gbc);

		// Layout innerhalb des Button-Panel
		GridBagLayout gbl1 = new GridBagLayout();
		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		buttonPanel.setLayout(gbl1);
		gbc1.insets = new Insets(5, 5, 5, 5);
		gbc1.gridx = 0;
		gbc1.gridy = 0;
		gbc1.weightx = 0;
		gbc1.weighty = 0;

		// Buttons ausgeben
		butts = new JButton[buttons.length];
		for (int i = 0; i < buttons.length; i++) {
			butts[i] = new JButton(buttons[i]);
			buttonPanel.add(butts[i], gbc1);
			gbc1.gridx++;
			final int n = i;
			// Aktion erzeugen und hinterlegen
			butts[i].setAction(new AbstractAction(butts[i].getText()) {
				private static final long serialVersionUID = 6635883464097705920L;

				// Event hinterlegen
				public void actionPerformed(ActionEvent e) {
					returnValue = n;
					dial.dispose();
				}
			});
		}
	}

	/**
	 * Dialog anzeigen und auf Benutzereingaben reagieren
	 * 
	 * @return die Nummer des betätigten Buttons
	 */
	public int show() {
		dial.pack();
		dial.setVisible(true);
		return returnValue;
	}
}
