package de.schrell.aok;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class AokTabMotorTest extends JScrollPane {

	private static final long serialVersionUID = -3227735374068565829L;

	final Aok aok;
	int MOT_MIN = 0;
	int MOT_MAX = 240;
	int MOT_ANZ = 4;
	int MOT_STARTNR = 73;
	private MotorTest motorTest = null;

	static JPanel box = new JPanel();

	private MotSlider rsls[] = new MotSlider[MOT_MAX + 1];
	
	AokTabMotorTest(final Aok aok) {
		super(box);

		this.aok = aok;

		/** the GridBagLayout layout manager is used for the main window */
		GridBagLayout lc = new GridBagLayout();
		/** the constraints for the main window */
		GridBagConstraints c = new GridBagConstraints();
		// initialize the constraints for the main window

		box.setLayout(lc);
		JPanel motbox = new JPanel();

		/** the GridBagLayout layout manager is used for the rc box */
		GridBagLayout rlc = new GridBagLayout();
		/** the constraints for the rc box */
		GridBagConstraints mot = new GridBagConstraints();
		// initialize the constraints for the main window
		motbox.setLayout(rlc);
		mot.fill = GridBagConstraints.HORIZONTAL;
		mot.anchor = GridBagConstraints.NORTH;
		mot.gridx = 0;
		mot.gridy = 0;
		mot.weightx = 1;
		mot.weighty = 1;
		for (int i = 0; i <= MOT_ANZ; i++) {
			mot.anchor = GridBagConstraints.NORTH;
			JLabel vlabel = new JLabel("");
			MotSlider rsl = new MotSlider(aok, i, vlabel);
			rsls[i] = rsl;
			rsl.setEnabled(true);
			rlc.setConstraints(rsl, mot);
			motbox.add(rsl);
			JLabel l = new JLabel(String.format("MOT %02d  ", i+1));
			if (i==MOT_ANZ) {
				l.setText("ALLE");
			}
			l.setHorizontalAlignment(SwingConstants.CENTER);
			mot.anchor = GridBagConstraints.CENTER;
			mot.gridy++;
			rlc.setConstraints(l, mot);
			motbox.add(l);
			mot.gridy++;
			rlc.setConstraints(vlabel, mot);
			vlabel.setHorizontalAlignment(SwingConstants.CENTER);
			motbox.add(vlabel);
			mot.gridy -= 2;
			mot.gridx++;
		}
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 2;
		c.weighty = 3;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		lc.setConstraints(motbox, c);
		box.add(motbox);
		motorTest = new MotorTest(aok, MOT_ANZ);
		final JCheckBox startStop = new JCheckBox("run");
		startStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (startStop.isSelected()) {
					if (!aok.aco.connected) {
						aok.aco.dialogNotConnected();
						startStop.setSelected(false);
					}
					motorTest.start();
				} else {
					motorTest.stop();
				}
			}
		});
		c.gridy++;
		lc.setConstraints(startStop, c);
		box.add(startStop);
	}
	
	class MotSlider extends JSlider {

		private static final long serialVersionUID = 5375265470668405893L;

		int mot;
		Aok aok;
		JLabel vlabel;

		public MotSlider(Aok aok, final int mot, JLabel vlabel) {
			super();
			this.aok = aok;
			this.mot = mot;
			this.vlabel = vlabel;
			this.setMajorTickSpacing(10);
			this.setMinorTickSpacing(5);
			this.setPaintTicks(true);
			this.setPaintLabels(true);
			this.setPreferredSize(new Dimension(70, 420));
			this.setOrientation(SwingConstants.VERTICAL);
			this.setMinimum(MOT_MIN);
			this.setMaximum(MOT_MAX);
			this.setValue(0);
			this.addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(ChangeEvent event) {
					int value = MotSlider.this.getValue();
					if (mot == MOT_ANZ) {
						motorTest.clear();
//						motorTest.setPitch(value);
						motorTest.setMotor(-1, value);
					} else {
						motorTest.setPitch(0);
						motorTest.setMotor(mot, value);
					}
				}
			});
		}

	}

}
