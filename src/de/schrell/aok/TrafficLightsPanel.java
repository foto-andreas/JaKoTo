package de.schrell.aok;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;

public class TrafficLightsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8258279596713057997L;

	Aok aok;
	TrafficLight tl1, tl2, tl3, tl4, tl5, tl6, tl7, tl8, tl9, tl10, tl11;

	public TrafficLightsPanel(final Aok aok) {
		super();
		this.aok = aok;
		Color[] GYR = { Color.green, Color.yellow, Color.red, Color.gray };

		/** Low Voltage */
		String[] TL1 = { "ok", "atn", "low", "?" };
		tl1 = new TrafficLight(aok, "Vcc", GYR, TL1);
		new Thread(new TR1()).start();

		/** GPS Online */
		String[] TL2 = { "ok", "srch", "nc", "?" };
		tl2 = new TrafficLight(aok, "GPS", GYR, TL2);
		new Thread(new TR2()).start();

		/** MM3 online */
		String[] TL3 = { "ok", "???", "nc", "?" };
		tl3 = new TrafficLight(aok, "MM3", GYR, TL3);
		new Thread(new TR3()).start();

		/** MOT running */
		String[] TL4 = { "off", "???", "on", "?" };
		tl4 = new TrafficLight(aok, "MOT", GYR, TL4);
		new Thread(new TR4()).start();

		/** Nick in neutral position */
		String[] TL5 = { "mid", "???", "out", "?" };
		tl5 = new TrafficLight(aok, "NICK", GYR, TL5);
		new Thread(new TR5()).start();

		/** Roll in neutral position */
		String[] TL6 = { "mid", "???", "out", "?" };
		tl6 = new TrafficLight(aok, "ROLL", GYR, TL6);
		new Thread(new TR6()).start();

		/** Yaw in neutral position */
		String[] TL7 = { "mid", "???", "out", "?" };
		tl7 = new TrafficLight(aok, "YAW", GYR, TL7);
		new Thread(new TR7()).start();

		/** Height / Coming Home */
		String[] TL8 = { "off", "PH", "CH", "?" };
		tl8 = new TrafficLight(aok, "PH/CH", GYR, TL8);
		new Thread(new TR8()).start();

		/** Vrate */
		String[] TL9 = { "hold", "down", "up", "?" };
		tl9 = new TrafficLight(aok, "VRATE", GYR, TL9);
		new Thread(new TR9()).start();

		/** Home Pos locked */
		String[] TL10 = { "lock", "???", "off", "?" };
		tl10 = new TrafficLight(aok, "HPOS", GYR, TL10);
		new Thread(new TR10()).start();

		/** RX_OK */
		String[] TL11 = { "ok", "???", "nok", "?" };
		tl11 = new TrafficLight(aok, "RX", GYR, TL11);
		new Thread(new TR11()).start();

		Dimension x = new Dimension(50, 50);
		tl1.setPreferredSize(x);
		tl2.setPreferredSize(x);
		tl3.setPreferredSize(x);
		tl4.setPreferredSize(x);
		tl5.setPreferredSize(x);
		tl6.setPreferredSize(x);
		tl7.setPreferredSize(x);
		tl8.setPreferredSize(x);
		tl9.setPreferredSize(x);
		tl10.setPreferredSize(x);
		tl11.setPreferredSize(x);

		add(tl1);
		add(tl11);
		add(tl2);
		add(tl3);
		add(tl4);
		add(tl5);
		add(tl6);
		add(tl7);
		add(tl8);
		add(tl9);
		add(tl10);
	}

	class TR1 implements Runnable {

		public void run() {
			try {
				while (true) {
					int n = 2;
					int conf = aok.getAokConfig(Aok.CONFIG_VOLTAGE);
					int voltage = aok.getAokState(Aok.STATUS_VOLTAGE);
					if (voltage > conf + 10)
						n = 0;
					else if (voltage > conf)
						n = 1;
					if (!aok.debug || !aok.configloaded)
						n = 3;
					tl1.setState(n);
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	class TR2 implements Runnable {

		public void run() {
			try {
				int lframes = aok.getAokState(Aok.STATUS_GPSFRAMES);
				while (true) {
					int frames = aok.getAokState(Aok.STATUS_GPSFRAMES);
					int n = 2;
					if (lframes != frames)
						n = 1;
					if (aok.getAokState(Aok.STATUS_GPSFIX) == 3)
						n = 0;
					if (!aok.debug || !aok.configloaded)
						n = 3;
					tl2.setState(n);
					lframes = frames;
					Thread.sleep(1000);
				}
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	class TR3 implements Runnable {

		public void run() {
			try {
				while (true) {
					int n = 2;
					if (-1 != aok.getAokState(Aok.STATUS_MM3HEADING))
						n = 0;
					if (!aok.debug || !aok.configloaded)
						n = 3;
					tl3.setState(n);
					Thread.sleep(1000);
				}
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	class TR4 implements Runnable {

		public void run() {
			try {
				while (true) {
					int n = 0;
					if (0 != aok.getAokState(Aok.STATUS_MOTRUNNING))
						n = 2;
					if (!aok.debug || !aok.configloaded)
						n = 3;
					tl4.setState(n);
					Thread.sleep(1000);
				}
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	class TR5 implements Runnable {

		public void run() {
			try {
				while (true) {
					int ch = aok.getAokConfig(Aok.CONFIG_CHANNEL_NICK) - 1;
					int mid = aok.getAokConfig(Aok.CONFIG_NICKMID);
					int n = 2;
					int rc = aok.getAokState(Aok.STATUS_RC0 + ch);
					// System.out.printf("Nick ch=%d v=%d m=%d\n",ch, rc, mid);
					if (rc > mid - 5 && rc < mid + 5)
						n = 0;
					if (!aok.debug || !aok.configloaded)
						n = 3;
					tl5.setState(n);
					Thread.sleep(1000);
				}
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	class TR6 implements Runnable {

		public void run() {
			try {
				while (true) {
					int ch = aok.getAokConfig(Aok.CONFIG_CHANNEL_ROLL) - 1;
					int mid = aok.getAokConfig(Aok.CONFIG_ROLLMID);
					int n = 2;
					int rc = aok.getAokState(Aok.STATUS_RC0 + ch);
					// System.out.printf("Roll ch=%d v=%d m=%d\n",ch, rc, mid);
					if (rc > mid - 5 && rc < mid + 5)
						n = 0;
					if (!aok.debug || !aok.configloaded)
						n = 3;
					tl6.setState(n);
					Thread.sleep(1000);
				}
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	class TR7 implements Runnable {

		public void run() {
			try {
				while (true) {
					int ch = aok.getAokConfig(Aok.CONFIG_CHANNEL_YAW) - 1;
					int mid = aok.getAokConfig(Aok.CONFIG_YAWMID);
					int n = 2;
					int rc = aok.getAokState(Aok.STATUS_RC0 + ch);
					// System.out.printf("Yaw ch=%d v=%d m=%d\n",ch, rc, mid);
					if (rc > mid - 8 && rc < mid + 8)
						n = 0;
					if (!aok.debug || !aok.configloaded)
						n = 3;
					tl7.setState(n);
					Thread.sleep(1000);
				}
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	class TR7HANS implements Runnable {

		public void run() {
			try {
				while (true) {
					int n = 2;
					if (0 == aok.getAokState(Aok.STATUS_STICKYAW))
						n = 0;
					if (!aok.debug || !aok.configloaded)
						n = 3;
					tl7.setState(n);
					Thread.sleep(1000);
				}
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	class TR8 implements Runnable {

		public void run() {
			try {
				while (true) {
					int ch = aok.getAokConfig(Aok.CONFIG_CHANNEL_HEIGHT) - 1;
					int mid = aok.getAokConfig(Aok.CONFIG_ROLLMID);
					int rc = aok.getAokState(Aok.STATUS_RC0 + ch);
					int n = 1;
					if (rc < mid - 50)
						n = 0;
					if (rc > mid + 50)
						n = 2;
					if (!aok.debug || !aok.configloaded)
						n = 3;
					tl8.setState(n);
					Thread.sleep(1000);
				}
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	class TR9 implements Runnable {

		public void run() {
			try {
				while (true) {
					int ch = aok.getAokConfig(Aok.CONFIG_CHANNEL_VRATE) - 1;
					int mid = aok.getAokConfig(Aok.CONFIG_ROLLMID);
					int rc = aok.getAokState(Aok.STATUS_RC0 + ch);
					int n = 0;
					if (rc < mid - 50)
						n = 1;
					if (rc > mid + 50)
						n = 2;
					if (!aok.debug || !aok.configloaded)
						n = 3;
					tl9.setState(n);
					Thread.sleep(1000);
				}
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	class TR10 implements Runnable {

		public void run() {
			try {
				while (true) {
					int n = 0;
					if (0 == aok.getAokState(Aok.STATUS_HOMEHEADING))
						n = 2;
					if (!aok.debug || !aok.configloaded)
						n = 3;
					tl10.setState(n);
					Thread.sleep(1000);
				}
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	class TR11 implements Runnable {

		public void run() {
			try {
				while (true) {
					int n = 0;
					if (0 == aok.getAokState(Aok.STATUS_RX_OK))
						n = 2;
					if (!aok.debug || !aok.configloaded)
						n = 3;
					tl11.setState(n);
					Thread.sleep(1000);
				}
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

}
