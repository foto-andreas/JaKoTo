package de.schrell.aok;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;

public class AokTabPrefs extends JPanel {

	private static final long serialVersionUID = 1087221451251656819L;

	JPanel tabprdefs;
	String[] lafnames, lafclasses;
	JComboBox laf;

	AokTabPrefs(final Aok aok) {
		super();

		aok.atp = this;

		/** the GridBagLayout layout manager is used for the main window */
		GridBagLayout lc = new GridBagLayout();
		/** the constraints for the main window */
		GridBagConstraints c = new GridBagConstraints();
		// initialize the constraints for the main window

		// a tab panel
		setLayout(lc);
		LookAndFeelInfo lafinfo[] = UIManager.getInstalledLookAndFeels();
		lafnames = new String[lafinfo.length];
		lafclasses = new String[lafinfo.length];
		int selected = 0;
		for (int i = 0; i < lafinfo.length; i++) {
			lafclasses[i] = lafinfo[i].getClassName();
			lafnames[i] = lafinfo[i].getName();
			if (aok.asw.actlaf.equals(lafclasses[i])) {
				selected = i;
			}
		}
		laf = new JComboBox(lafnames);
		laf.setSelectedIndex(selected);
		laf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = lafclasses[laf.getSelectedIndex()];
				aok.asw.setLookAndFeel(s);
			}
		});

		c.insets = new Insets(5, 10, -5, 10);
		c.gridx = 0;
		c.gridy = 0;
		JLabel laflabel = new JLabel("Look and Feel");
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.NONE;
		lc.setConstraints(laflabel, c);
		add(laflabel);
		c.gridy++;
		lc.setConstraints(laf, c);
		add(laf);

		c.gridy++;
		JButton save = new JButton("Save Default Preferences");
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				aok.config.write();
			}
		});
		lc.setConstraints(save, c);
		add(save);

		c.gridx++;
		final JButton saveas = new JButton("Save Preferences as...");
		saveas.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				JFileChooser chooser = new JFileChooser(aok.config.configPath);
				chooser.setFileHidingEnabled(false);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setApproveButtonText("Write");
				int returnVal = chooser.showSaveDialog(saveas);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					aok.config.configPath = chooser.getSelectedFile()
							.getParent();
					aok.config.fileName = chooser.getSelectedFile().getName();
					aok.config.write();
				}
			}
		});
		lc.setConstraints(saveas, c);
		add(saveas);

		c.gridx--;
		c.gridy++;
		JButton read = new JButton("Read Default Preferences");
		read.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				aok.config.read();
			}
		});
		lc.setConstraints(read, c);
		add(read);

		c.gridx++;
		final JButton readfrom = new JButton("Read Preferences from...");
		readfrom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				JFileChooser chooser = new JFileChooser(aok.config.configPath);
				chooser.setFileHidingEnabled(false);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setApproveButtonText("Read");
				int returnVal = chooser.showOpenDialog(readfrom);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					aok.config
							.read(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		lc.setConstraints(readfrom, c);
		add(readfrom);

		c.gridx=0;
		c.gridy++;
		JButton saveLayout = new JButton("Save Window Layout");
		saveLayout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				aok.asw.saveLayout();
			}
		});
		lc.setConstraints(saveLayout, c);
		add(saveLayout);

		c.gridx++;
		JButton readLayout = new JButton("Read Window Layout");
		readLayout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				aok.asw.readLayout();
			}
		});
		lc.setConstraints(readLayout, c);
		add(readLayout);

		/** Filler */
		c.gridx = 0;
		c.gridy++;
		c.weighty = 100;
		JLabel filler = new JLabel("");
		lc.setConstraints(filler, c);
		add(filler);
		c.gridx += 2;
		c.weightx = 100;
		JLabel fillerx = new JLabel("");
		lc.setConstraints(fillerx, c);
		add(fillerx);
	}

}
