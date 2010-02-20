package de.schrell.aok;

import java.awt.*;

import javax.swing.*;

public class AokTabInfo extends JScrollPane {

	private static final long serialVersionUID = -1284112832564066739L;

	final Aok aok;
	int RC_MIN = 0;
	int RC_MAX = 1116;
	int RC_INIT = 558;
	int RC_ANZ = 12;
	int RC_STARTNR = 55;
	TrafficLight tl1;

	static JPanel box = new JPanel();

	AokTabInfo(Aok aok) {
		super(box);

		this.aok = aok;

		/** the GridBagLayout layout manager is used for the main window */
		GridBagLayout lc = new GridBagLayout();
		/** the constraints for the main window */
		GridBagConstraints c = new GridBagConstraints();
		// initialize the constraints for the main window

		box.setLayout(lc);
		JPanel rcbox = new JPanel();

		/** the GridBagLayout layout manager is used for the rc box */
		GridBagLayout rlc = new GridBagLayout();
		/** the constraints for the rc box */
		GridBagConstraints rc = new GridBagConstraints();
		// initialize the constraints for the main window
		rcbox.setLayout(rlc);
		rc.fill = GridBagConstraints.HORIZONTAL;
		rc.anchor = GridBagConstraints.NORTH;
		rc.gridx = 0;
		rc.gridy = 0;
		rc.weightx = 1;
		rc.weighty = 1;
		for (int i = 1; i <= 12; i++) {
			rc.anchor = GridBagConstraints.NORTH;
			JLabel vlabel = new JLabel("");
			RcSlider rsl = new RcSlider(aok, i, vlabel);
			rsl.setEnabled(false);
			rlc.setConstraints(rsl, rc);
			rcbox.add(rsl);
			JLabel l = new JLabel(String.format("CH %02d  ", i));
			l.setHorizontalAlignment(SwingConstants.CENTER);
			rc.anchor = GridBagConstraints.CENTER;
			rc.gridy++;
			rlc.setConstraints(l, rc);
			rcbox.add(l);
			rc.gridy++;
			rlc.setConstraints(vlabel, rc);
			vlabel.setHorizontalAlignment(SwingConstants.CENTER);
			rcbox.add(vlabel);
			rc.gridy -= 2;
			rc.gridx++;
		}
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 2;
		c.weighty = 3;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		lc.setConstraints(rcbox, c);
		box.add(rcbox);

		/** traffic lights */
		if (true) {
			c.gridy++;
			c.gridx = 0;
			TrafficLightsPanel traffics = new TrafficLightsPanel(aok);
			c.weightx = 2;
			c.weighty = 2;
			c.anchor = GridBagConstraints.NORTH;
			c.fill = GridBagConstraints.HORIZONTAL;
			lc.setConstraints(traffics, c);
			traffics.setMinimumSize(new Dimension(500, 60));
			box.add(traffics);
		}
	}

	class RcSlider extends JSlider {

		private static final long serialVersionUID = 5375265470668405893L;

		int channel;
		Aok aok;
		JLabel vlabel;

		public RcSlider(Aok aok, int channel, JLabel vlabel) {
			super();
			this.aok = aok;
			this.channel = channel;
			this.vlabel = vlabel;
			this.setMajorTickSpacing(100);
			this.setMinorTickSpacing(25);
			this.setPaintTicks(true);
			this.setPaintLabels(true);
			this.setPreferredSize(new Dimension(70, 380));
			this.setOrientation(SwingConstants.VERTICAL);
			this.setMinimum(RC_MIN);
			this.setMaximum(RC_MAX);
			(new Thread(new SliderUpdate())).start();
		}

		class SliderUpdate implements Runnable {
			public void run() {
				while (true) {
					int value = getValue();
					value = aok.getAokState(RC_STARTNR + channel - 1);
					setValue(value);
					if (vlabel != null)
						vlabel.setText(new Integer(value).toString() + "  ");
					fireStateChanged();
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

}
