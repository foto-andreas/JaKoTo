package de.schrell.aok;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.infonode.docking.RootWindow;
import net.infonode.docking.View;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.ViewMap;

/**
 * displays the windows for the Aok-Tool
 * 
 * @author Andreas Schrell
 * 
 */
public class AokStatusWindow {

	Aok aok;

	/** the main window */
	JFrame frame;
	String actlaf = "";
	RootWindow rootWindow = null;
	int VMAX = 10;
	View[] views = new View[VMAX];
	ViewMap viewMap = new ViewMap();

	int v = 0;
	int dynGraphNr = 0;

	ByteArrayOutputStream bos = new ByteArrayOutputStream();

	public void setLookAndFeel(String s) {
		try {
			if (s.equals("")) {
				actlaf = UIManager.getSystemLookAndFeelClassName();
				UIManager.setLookAndFeel(actlaf);
			} else {
				actlaf = s;
				UIManager.setLookAndFeel(s);
			}
			if (frame != null) {
				SwingUtilities.updateComponentTreeUI(frame);
				frame.pack();
			}
		} catch (Exception e) {
			System.out.println("Error: setting native LAF: " + e);
			e.printStackTrace();
		}
	}

	public void saveLayout() {
		try {
			bos.reset();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			rootWindow.write(out, true);
			out.close();
			FileOutputStream fo = new FileOutputStream(new File(aok.config
					.getWinConf()));
			fo.write(bos.toByteArray());
			fo.close();
		} catch (IOException e) {
			System.out.println("WARNING: could not write Window Layout");
			e.printStackTrace();
		}
	}

	public void readLayout() {
		// Read the window state from a byte array
		try {
			bos.reset();
			FileInputStream fi = new FileInputStream(new File(aok.config
					.getWinConf()));
			rootWindow.read(new ObjectInputStream(fi));
			// rootWindow.read(new ObjectInputStream(new
			// ByteArrayInputStream(bos
			// .toByteArray())));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
	}

	public void addGraph() {
		JPanel box = new JPanel();
		ScrolledGraph sp0 = new ScrolledGraph(aok, 10000, 200, 200, 200);
		/** the GridBagLayout layout manager is used for the main window */
		GridBagLayout lc = new GridBagLayout();
		/** the constraints for the main window */
		GridBagConstraints c = new GridBagConstraints();
		// initialize the constraints for the main window
		box.setLayout(lc);
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		// set titles for the window parts
		c.insets = new Insets(10, 10, 10, 10);
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 10;
		c.weighty = 1;
		lc.setConstraints(sp0.getPanel(), c);
		box.add(sp0.getPanel());

		sp0.setVisible(true);
		views[v] = new View("SingleGraph " + (++dynGraphNr), null, box);
		viewMap.addView(v, views[v]);
		v++;
	}

	/**
	 * creates the main window and all tables, buttons etc...
	 * 
	 * @param aok
	 *            the Aok instance which is to be displayed
	 */
	public AokStatusWindow(final Aok aok) {

		this.aok = aok;
		aok.asw = this;
		views = new View[10];
		viewMap = new ViewMap();
		v = 0;

		final Image img = new ImageIcon(AokStatusWindow.class
				.getResource("armotool.png")).getImage();

		try {
			EventQueue.invokeAndWait(new Runnable() {
			 	public void run() {
					setLookAndFeel("");

					AokTabStatus ats = new AokTabStatus(aok);
					AokTabInfo ati = new AokTabInfo(aok);
					AokTabGraph atg = new AokTabGraph(aok);
					AokTabPrefs atp = new AokTabPrefs(aok);
					AokTabOptions ato = new AokTabOptions(aok);
					// AokTabGPS ate = new AokTabGPS(aok);

					views[v++] = new View("Debug and Config", null, ats);
					views[v++] = new View("RX Information", null, ati);
					views[v++] = new View("Graphs", null, atg);
					// views[v++] = new View("GPS", null, ate);
					views[v++] = new View("Preferences", null, atp);
					views[v++] = new View("Options", null, ato);

					/** add the views to the view map */
					for (int i = 0; i < v; i++) {
						viewMap.addView(i, views[i]);
					}

					for (int i = v; i < VMAX; i++) {
						addGraph();
					}
					/** create the root window inside our JFrame */
					rootWindow = DockingUtil.createRootWindow(viewMap, true);
					frame = new JFrame("Arm-o-Kopter Configuration"
							+ " $Revision: 20 $ " + aok.getVersion());
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					frame.setPreferredSize(new Dimension(1000, 1100));
					frame.setIconImage(img);

					/** read our configuration */
					aok.config.read();
					readLayout();

					// create the tab panels
					JTabbedPane tabs = new JTabbedPane();
					tabs.setFont(new Font("", Font.BOLD, 12));
					tabs
							.setBorder(BorderFactory.createEmptyBorder(10, 5,
									5, 5));
					frame.add(rootWindow);

					// pack the window and display it
					frame.pack();

					aok.config.read();
					frame.addWindowListener(new WindowListener() {

						@Override
						public void windowClosed(WindowEvent arg0) {
							// TODO Auto-generated method stub
						}

						@Override
						public void windowClosing(WindowEvent arg0) {
							// TODO Auto-generated method stub
							aok.asw.saveLayout();
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(AokStatusWindow.class.getName()).log(Level.SEVERE, null, ex);
                            }
							System.out.println("Main window closing...");

						}

						@Override
						public void windowDeactivated(WindowEvent arg0) {
							// TODO Auto-generated method stub

						}

						@Override
						public void windowDeiconified(WindowEvent arg0) {
							// TODO Auto-generated method stub

						}

						@Override
						public void windowIconified(WindowEvent arg0) {
							// TODO Auto-generated method stub

						}

						@Override
						public void windowOpened(WindowEvent arg0) {
							// TODO Auto-generated method stub

						}

						@Override
						public void windowActivated(WindowEvent e) {
							// TODO Auto-generated method stub

						}

					});

				}
			});
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		frame.setVisible(true);
	}

}
