package de.schrell.aok;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Semaphore;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

public class AokStatusButtons {

	Aok aok;
	AokConnector aco;
	ConfigFile cf;

	JScrollPane spane;
	JPanel buttons;
	JLabel label, loglabel;

	/** checkbox entries for debug on/off and connect/disconnect */
	JCheckBox debug, connect, log;
	/** the buttons for the corresponding tasks */
	JButton reset, readfromaok, writetoaok, readfromfile, writetofile,
			writeseltofile, replay, replayq, replays, replayb, checkconf;
	/** the buttons panel */
	/** the text fields in the buttons panel */
	JTextField port, speed;
	/** the progress bar in the buttons panel */
	JProgressBar bar;

	/** semaphore for progress bar */
	Semaphore barsem = new Semaphore(1);

	JScrollPane getPane() {
		return spane;
	}

	JLabel getLabel() {
		return label;
	}

	AokStatusButtons(final Aok aok) {

		this.aok = aok;
		this.aco = aok.aco;
		this.cf = aok.cf;

		aok.asb = this;

		label = new JLabel("Actions");
		int fs = label.getFont().getSize();
		label.setFont(new Font("", Font.BOLD, fs));

		/** the GridBagLayout layout manager is used for the buttons area */
		GridBagLayout lbc = new GridBagLayout();
		/** the constraints for the button area */
		GridBagConstraints bc = new GridBagConstraints();

		// create the buttons area and set the layout manager
		buttons = new JPanel();
		spane = new JScrollPane(buttons);

		buttons.setLayout(lbc);
		// buttons.setPreferredSize(new Dimension(200, 100));

		// initialize the constraints for inside the button area
		bc.gridx = 0;
		bc.gridy = 0;
		bc.weighty = 1.0;
		bc.anchor = GridBagConstraints.NORTHWEST;
		bc.fill = GridBagConstraints.HORIZONTAL;
		bc.insets = new Insets(0, 0, 5, 0);

		// create the port entry
		port = new JTextField("", 10);
		lbc.setConstraints(port, bc);
		buttons.add(port);

		// create the speed entry
		bc.gridy++;
		speed = new JTextField("38400", 10);
		lbc.setConstraints(speed, bc);
		buttons.add(speed);

		/*
		 * TODO create the connect checkbox. At the moment there is only a
		 * connect and no possibility to disconnect. This will be implemented
		 * later
		 */
		bc.gridy++;
		connect = new JCheckBox("Connect to AOK");
		connect.setSelected(false);
		connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!aok.aco.connected) {
					if (aco != null) {
						if (aco.connect(port.getText(), speed.getText())) {
							connect.setSelected(aco.connected);
						} else {
							// connect went wrong, enable the checkbox again
							connect.setSelected(aco.connected);
						}
					}
				} else {
					if (aco != null) {
						if (aco.disconnect()) {
							connect.setSelected(aco.connected);
						}
					}

				}
			}
		});
		lbc.setConstraints(connect, bc);
		buttons.add(connect);

		// add a debug checkbox to enable and disable the debug mode
		bc.gridy++;
		debug = new JCheckBox("State Values");
		debug.setSelected(false);
		debug.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (aco != null)
						aco.debugToggle();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		lbc.setConstraints(debug, bc);
		buttons.add(debug);

		// add a debug checkbox to enable and disable the debug mode
		bc.gridy++;
		log = new JCheckBox("Write Logfile");
		log.setSelected(false);
		log.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (aok.logfile != null)
					if (aok.log) {
						aok.logfile.stopLog();
						loglabel.setText("");
					} else
						loglabel.setText(aok.logfile
								.startLog(aok.AokStateNames));
			}
		});
		lbc.setConstraints(log, bc);
		buttons.add(log);
		bc.gridy++;
		loglabel = new JLabel();
		loglabel.setFont(new Font("", Font.PLAIN, 9));
		lbc.setConstraints(loglabel, bc);
		buttons.add(loglabel);

		bc.gridy++;
		JPanel rambox = new JPanel();
		rambox.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createLineBorder(Color.black), "Read/Write AOK RAM"));
		lbc.setConstraints(rambox, bc);
		buttons.add(rambox);

		bc.gridy++;
		JPanel rwfile = new JPanel();
		rwfile.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createLineBorder(Color.black), "Read/Write File"));
		lbc.setConstraints(rwfile, bc);
		buttons.add(rwfile);

		// add a button to read configuration values from the Arm-o-Kopter
		readfromaok = new JButton("read");
		readfromaok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (aco != null) {
					aco.readConfigFromAok(false);
				}
			}
		});
		rambox.add(readfromaok);

		// add a button to write configuration values to the Arm-o-Kopter
		writetoaok = new JButton("write");
		writetoaok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (aco != null) {
					aco.writeConfigToAok();
				}
			}
		});
		rambox.add(writetoaok);

		// add a button to read configuration values from the Arm-o-Kopter
		checkconf = new JButton("check");
		checkconf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (aco != null) {
					aco.readConfigFromAok(true);
				}
			}
		});
		rambox.add(checkconf);

		// add a button to read configuration values from a file
		readfromfile = new JButton("read");
		readfromfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (cf != null) {
					JFileChooser chooser = new JFileChooser(cf.lastpath);
					FileNameExtensionFilter filter = new FileNameExtensionFilter(
							"Arm-o-Kopter Config", "aok", "aoksel");
					chooser.setFileFilter(filter);
					chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					// chooser.setDialogType(JFileChooser.OPEN_DIALOG);
					chooser.setApproveButtonText("Read");
					int returnVal = chooser.showOpenDialog(buttons);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						cf.lastpath = chooser.getSelectedFile()
								.getAbsolutePath();
						cf.read(cf.lastpath);
					}
				}
			}
		});
		rwfile.add(readfromfile);

		// add a button to write configuration values fo a file
		writetofile = new JButton("write");
		writetofile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (cf != null) {
					JFileChooser chooser = new JFileChooser(cf.lastpath);
					FileNameExtensionFilter filter = new FileNameExtensionFilter(
							"Arm-o-Kopter Config", "aok");
					chooser.setFileFilter(filter);
					chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					// chooser.setDialogType(JFileChooser.SAVE_DIALOG);
					chooser.setApproveButtonText("Write");
					int returnVal = chooser.showOpenDialog(buttons);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						cf.lastpath = chooser.getSelectedFile()
								.getAbsolutePath();
						if (!cf.lastpath.toLowerCase().endsWith(".aok")) {
							cf.lastpath += ".aok";
						}
						cf.write(cf.lastpath);
					}
				}
			}
		});
		rwfile.add(writetofile);

		// add a button to write configuration values fo a file
		writeseltofile = new JButton("write sel.");
		writeseltofile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (cf != null) {
					JFileChooser chooser = new JFileChooser(cf.lastpath);
					FileNameExtensionFilter filter = new FileNameExtensionFilter(
							"Arm-o-Kopter Selection Config", "aoksel");
					chooser.setFileFilter(filter);
					chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					// chooser.setDialogType(JFileChooser.SAVE_DIALOG);
					chooser.setApproveButtonText("Write");
					int returnVal = chooser.showOpenDialog(buttons);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						cf.lastpath = chooser.getSelectedFile()
								.getAbsolutePath();
						if (!cf.lastpath.toLowerCase().endsWith(".aoksel")) {
							cf.lastpath += ".aoksel";
						}
						cf.write(cf.lastpath, true);
					}
				}
			}
		});
		rwfile.add(writeseltofile);

		bc.gridy++;
		JPanel fwbox = new JPanel();
		fwbox.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createLineBorder(Color.black), "AOK-RAM to Flash"));
		lbc.setConstraints(fwbox, bc);
		buttons.add(fwbox);
		bc.gridy++;
		JPanel frbox = new JPanel();
		frbox.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createLineBorder(Color.black), "Flash to AOK-RAM"));
		lbc.setConstraints(frbox, bc);
		buttons.add(frbox);
		bc.gridy++;
		JPanel ssbox = new JPanel();
		ssbox.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createLineBorder(Color.black), "set startset "));
		lbc.setConstraints(ssbox, bc);
		buttons.add(ssbox);

		JButton fr[] = new JButton[4];
		for (int i = 0; i < 4; i++) {
			final int ii = i;
			fr[i] = new JButton("#" + (i + 1));
			lbc.setConstraints(fr[i], bc);
			frbox.add(fr[i]);
			fr[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						if (aco != null)
							aco.flashread(ii + 1);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
		}

		JButton fw[] = new JButton[4];
		for (int i = 0; i < 4; i++) {
			final int ii = i;
			fw[i] = new JButton("#" + (i + 1));
			lbc.setConstraints(fw[i], bc);
			fwbox.add(fw[i]);
			fw[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						if (aco != null)
							aco.flashwrite(ii + 1);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
		}

		JButton fwa = new JButton("ALL");
		lbc.setConstraints(fwa, bc);
		fwbox.add(fwa);
		fwa.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (aco != null) {
						aco.flashwrite(1);
						aco.flashwrite(2);
						aco.flashwrite(3);
						aco.flashwrite(4);
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		JButton ss[] = new JButton[4];
		for (int i = 0; i < 4; i++) {
			final int ii = i;
			ss[i] = new JButton("#" + (i + 1));
			lbc.setConstraints(ss[i], bc);
			ssbox.add(ss[i]);
			ss[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						if (aco != null)
							aco.startset(ii + 1);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
		}

		// add a button to read configuration values from a file
		bc.gridy++;
		JPanel rpbox = new JPanel();
		rpbox.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createLineBorder(Color.black), "Logfile replay"));
		lbc.setConstraints(rpbox, bc);
		buttons.add(rpbox);

		replay = new JButton("normal");
		replay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(
						(aok.logfile != null) ? aok.logfile.path : "");
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"Arm-o-Kopter Logfiles", "csv");
				chooser.setFileFilter(filter);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setApproveButtonText("Read");
				int returnVal = chooser.showOpenDialog(buttons);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					new LogReplay(aok, chooser.getSelectedFile()
							.getAbsolutePath(), false);
					aok.logfile.path = chooser.getSelectedFile().getParent();
				}
			}
		});
		rpbox.add(replay);

		replayq = new JButton("quick");
		replayq.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(
						(aok.logfile != null) ? aok.logfile.path : "");
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"Arm-o-Kopter Logfiles", "csv");
				chooser.setFileFilter(filter);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setApproveButtonText("Read");
				int returnVal = chooser.showOpenDialog(buttons);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					new LogReplay(aok, chooser.getSelectedFile()
							.getAbsolutePath(), true);
					aok.logfile.path = chooser.getSelectedFile().getParent();
				}
			}
		});
		rpbox.add(replayq);

		replays = new JButton("stop");
		replays.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				aok.stopreplay = true;
			}
		});
		rpbox.add(replays);

		replayb = new JButton("bin");
		replayb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(
						(aok.binpath != null) ? aok.binpath : "");
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"Binary Logfiles", "bin");
				chooser.setFileFilter(filter);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setApproveButtonText("Read");
				int returnVal = chooser.showOpenDialog(buttons);
				System.out.println("PATH: " + aok.binpath);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					new BinReplay(aok, chooser.getSelectedFile()
							.getAbsolutePath());
					aok.binpath = chooser.getSelectedFile().getParent();
				}
			}
		});
		rpbox.add(replayb);

		final JButton flash;
		bc.gridy++;
		flash = new JButton("flash AOK-Firmware");
		flash.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(
						(aok.fwpath != null) ? aok.fwpath : "");
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"AOK Firmware", "bin");
				chooser.setFileFilter(filter);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setApproveButtonText("Read");
				int returnVal = chooser.showOpenDialog(buttons);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					aco.flashAok(aok, chooser.getSelectedFile()
							.getAbsolutePath());
					aok.fwpath = chooser.getSelectedFile().getParent();
				}
			}
		});
		lbc.setConstraints(flash, bc);
//		flash.setEnabled(false);
		buttons.add(flash);

		// add a reset button to reset the Arm-o-Kopter
		bc.gridy++;
		reset = new JButton("Reset AOK");
		reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (aco != null)
						aco.reset();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		lbc.setConstraints(reset, bc);
		buttons.add(reset);

		// add filler space
		bc.gridy++;
		JLabel fil = new JLabel("");
		fil.setVisible(true);
		bc.weighty = 1000;
		bc.fill = GridBagConstraints.BOTH;
		lbc.setConstraints(fil, bc);
		buttons.add(fil);
		bc.weighty = 1.0;
		bc.fill = GridBagConstraints.HORIZONTAL;

		// add a little progress bar, which can be used by other tasks
		bc.gridy++;
		bc.insets = new Insets(10, 0, 0, 0);
		bar = new JProgressBar();
		bar.setSize(150, 20);
		bar.setVisible(true);
		bar.setStringPainted(true);
		lbc.setConstraints(bar, bc);
		buttons.add(bar);

	}

	/**
	 * show the progress bar and acquire it for a task
	 * 
	 * @param min
	 *            minimum value to display
	 * @param max
	 *            maximum value to display
	 */
	public void acquireProgressBar(int min, int max) {
		try {
			barsem.acquire(); // get the semaphore
		} catch (InterruptedException e) {
			System.out.println("WARNING: progress bar acquire was interrupted");
		}
		// initialize the values and make it visible
		bar.setMinimum(min);
		bar.setMaximum(max);
		bar.setValue(min);
		// bar.setVisible(true);
	}

	/**
	 * set the progress bar to a given value and redraw it
	 * 
	 * @param m
	 *            the current value between the minimum and maximum
	 */
	public void setProgressBarVal(int m) {
		bar.setValue(m);
	}

	/**
	 * release the progress bar and erase it from the display
	 */
	public void releaseProgressBar() {
		// bar.setVisible(false);
		bar.setMinimum(0);
		bar.setMaximum(100);
		bar.setValue(0);
		barsem.release();
	}

}
