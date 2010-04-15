package de.schrell.aok;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.Semaphore;

//-as import chrriis.dj.nativeswing.NativeSwing;
import de.schrell.CompileTime;

/**
 * Class Aok represents the status of an Arm-o-Kopter
 * 
 * @author Andreas Schrell
 * 
 */
public class Aok {

	public final static int CONFIG_VOLTAGE = 38;
	public final static int CONFIG_ROLLMID = 14;
	public final static int CONFIG_NICKMID = 15;
	public final static int CONFIG_YAWMID = 16;
	public final static int CONFIG_CHANNEL_ROLL = 9;
	public final static int CONFIG_CHANNEL_NICK = 10;
	public final static int CONFIG_CHANNEL_PITCH = 11;
	public final static int CONFIG_CHANNEL_YAW = 12;
	public final static int CONFIG_OPTIONS = 13;
	public final static int CONFIG_CHANNEL_HEIGHT = 60;
	public final static int CONFIG_CHANNEL_VRATE = 82;
    public final static int CONFIG_DEBUG_DELAY = 110;
    public final static int CONFIG_MIXER1 = 111;

	public final static int STATUS_VOLTAGE = 50;
	public final static int STATUS_GPSFRAMES = 19;
	public final static int STATUS_GPSFIX = 20;
	public final static int STATUS_MM3HEADING = 14;
	public final static int STATUS_MOTRUNNING = 35;
	public final static int STATUS_RC0 = 55;
	public final static int STATUS_STICKYAW = 43;
	public final static int STATUS_HOMEHEADING = 33; // ???
	public final static int STATUS_RX_OK = 34;

	/** VERSION */
	final String version = "(v0.11 " + new CompileTime().toString() + ")";

	/** maximal possible state variables */
	final int STATEMAX = 256;

	/** maximal possible configuration variables */
	final int CONFIGMAX = 128;

	/** number of implemented debug/state values */
	int statecount = 0;
	/** number of implemented configuration values */
	int configcount = 0;
	/** number of lined in ordered legends */
	int legendcount = 0;
	/** number of tooltips for implemented configuration values */
	int tooltipcount = 0;

	/** table with the state values */
	int AokStateValues[];
	/** table with the state names */
	String AokStateNames[] = new String[statecount];
	/** display table for the states */
	String AokStateTable[][];

	/** table with the configuration values */
	private int AokConfigValues[];
	/** table with the configuration names */
	String AokConfigNames[] = new String[CONFIGMAX];
	int AokConfigNumbers[] = new int[CONFIGMAX];
	String AokConfigOrderedNames[] = new String[CONFIGMAX * 2];;
	int AokConfigOrderedNumbers[] = new int[CONFIGMAX * 2];;
	/** display table for the configuration */
	String AokConfigTable[][];
	/** get the config number from the name */
	Hashtable<String, Integer> NamesToNumbers = new Hashtable<String, Integer>();
	/** get the row from the name */
	HashMap<String, Integer> NamesToRows = new HashMap<String, Integer>();

	/** view ports */
	AokStatusWindow asw = null;
	AokConnector aco = null;
	AokStatusTable ast = null;
	AokConfigTable act = null;
	AokStatusButtons asb = null;
	AokTabPrefs atp = null;
	Configuration config = null;
	AokTabOptions ato = null;
    AokTabMixer atm = null;

	/** file io */
	ConfigFile cf = null;
	LogFile logfile = null;
	String binfile = null, binpath = null;
	String fwfile = null, fwpath = null;

	/** tool tips */
	String[] ToolTipConfig = null;

	/** status of debug mode. Does the Aok currently send state values */
	boolean debug;

	/** is a config loaded in the table */
	boolean configloaded;

	/** logging on/off */
	boolean log;

	boolean stopreplay;

	/** switch debug mode off */
	public byte CMD_DEBUG_OFF[] = { 0x63, 0x01 };
	/**
	 * switch debug mode on. The Aok sends Debug values in the following form:
	 * 0x1C, number of value, 4 bytes as the value, crc. The crc sum is an xor
	 * from number and the value bytes.
	 */
	public byte CMD_DEBUG_ON[] = { 0x63, 0x02 };
	/** reset the Aok */
	public byte CMD_RESET[] = { 0x63, 0x07 };
	/**
	 * read a configuration value, must be followed by a byte which sets number
	 * of the expected configuration value. The Aok answers with five bytes.
	 * Four represent the value, the fifth is the crc sum.
	 */
	public byte CMD_READCONF = 0x67;
	/**
	 * write a configuration value, must be followed by a byte which sets number
	 * of configuration value, then 4 bytes for the value and one byte with the
	 * crc checksum
	 */
	public byte CMD_WRITECONF = 0x73;
	/**
	 * lade aus Flash
	 */
	public byte CMD_FLASHREAD[] = { 0x63, 0x03 };

	/**
	 * speichere in Flash
	 */
	public byte CMD_FLASHWRITE[] = { 0x63, 0x04 };

	/**
	 * write fw to flash
	 */
	public byte CMD_FLASH = 0x62;
	public byte CMD_FLASH_END = 0x21;

	/**
	 * Startset setzen
	 */
	public byte CMD_STARTSET[] = { 0x63, 0x05 };

	/** a semaphore to control the reading and writing in the status table */
	Semaphore ssem = new Semaphore(1);

	/**
	 * a semaphore to control the reading and writing in the configuration table
	 */
	Semaphore csem = new Semaphore(1);

	/**
	 * Graphs to continuously display the values
	 */
	ArrayList<ScrolledGraph> graphs;

	// Graph graph1;

	public void registerGraph(ScrolledGraph graph) {
		graphs.add(graph);
		// System.out.println("GANZ="+graphs.size());
	}

	public String getVersion() {
		return version;
	}

	/**
	 * creates a new Aok instance, reads the legend files and initialized the
	 * status and configuration tables. The table size is defined by the legend
	 * files. The constants STATEMAX and CONFIGMAX limit these tables. The debug
	 * variable is set to false here. It is updated with the first debug value
	 * received from the Aok.
	 */
	public Aok() {

		readLegends(); // read the files with the legends
		// System.out.printf("Statecount:  %d\n", statecount); // only user info
		// System.out.printf("Configcount: %d\n", configcount); // only user
		// info
		// initialize the state tables
		AokStateTable = new String[statecount][];
		AokStateValues = new int[statecount];
		for (int i = 0; i < statecount; i++) {
			AokStateValues[i] = 0;
			AokStateTable[i] = new String[3]; // allocate one line
			// set the number
			AokStateTable[i][0] = String.format("%03d", i);
			// set the name
			AokStateTable[i][1] = AokStateNames[i];
			// set the value
			AokStateTable[i][2] = "0";
		}
		// initialize the configuration tables
		AokConfigTable = new String[legendcount][];
		AokConfigValues = new int[legendcount];
		for (int i = 0; i < legendcount; i++) {
			// System.out.println("AAAA=" + i);
			AokConfigTable[i] = new String[3]; // allocate one line
			// set the number
			int num = AokConfigOrderedNumbers[i];
			if (num != -1) {
				AokConfigTable[i][0] = String.format("%03d", num);
			}
			// set the name
			AokConfigTable[i][1] = AokConfigOrderedNames[i];
			// System.out.println("BBBB=" + i);
			// set the value
			AokConfigTable[i][2] = "";
		}
		// set debug to false, updated by AokStatusReader
		debug = false;
		// no config loaded in table
		configloaded = false;
		// no logging
		log = false;
		logfile = new LogFile(this, ".");
		cf = new ConfigFile(this);
		graphs = new ArrayList<ScrolledGraph>();
		config = new Configuration(this);
		stopreplay = false;
	}

	/**
	 * reads the files with the legends for the status table and configuration
	 * table. This does also set the counts of state values and configuration
	 * values.
	 * 
	 * @throws IOException
	 */
	void readLegends() {
		String legend = "legend_debug.txt";
		// allocate the field
		AokStateNames = new String[STATEMAX];
		// initialize the BufferedReader with null
		BufferedReader legends = null;
		InputStream is = null;
		// try to find the legend file in file system then in jar
		try {
			legends = new BufferedReader(new FileReader(legend));
		} catch (FileNotFoundException e) {
			System.out.print("INFO: Legend  " + legend
					+ " not found in file system, trying jar... ");
		}
		if (legends == null) {
			is = Aok.class.getResourceAsStream(legend);
			legends = new BufferedReader(new InputStreamReader(is));
			if (legends != null)
				System.out.println("ok.");
		}
		// continue if file was found
		if (legends != null) {
			do {
				try {
					legend = legends.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // read one
				if (legend != null) { // if defined set the table value
					AokStateNames[statecount++] = legend;
				}
			} while (legend != null && statecount <= STATEMAX);
			try {
				legends.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.out.println();
			System.out.println("Error: legend_debug.txt not found.");
			System.exit(1);
		}

		// read the legends for the configuration values
		legend = "legend_parameters.txt";
		// initialize the BufferedReader with null
		legends = null;
		is = null;
		// try to find the legend file in file system, then in jar
		try {
			legends = new BufferedReader(new FileReader(legend));
		} catch (FileNotFoundException e) {
			System.out.print("INFO: Legend  " + legend
					+ " not found in file system, trying jar... ");
		}
		if (legends == null) {
			is = Aok.class.getResourceAsStream(legend);
			legends = new BufferedReader(new InputStreamReader(is));
			if (legends != null)
				System.out.println("ok.");
		}
		// continue if file was found
		if (legends != null) {
			do {
				try {
					legend = legends.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // read one
				if (legend != null) { // if defined set the table value
					AokConfigNames[configcount] = legend;
					NamesToNumbers.put(legend, configcount);
					configcount++;
				}
			} while (legend != null && configcount <= CONFIGMAX);
			try {
				legends.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.out.println("Error: legend_parameters.txt not found.");
			System.exit(1);
		}

		// read the ordered legends for the configuration values
		legend = "legend_parameters_ordered.txt";
		// initialize the BufferedReader with null
		legends = null;
		is = null;
		// try to find the legend file in file system, then in jar
		try {
			legends = new BufferedReader(new FileReader(legend));
		} catch (FileNotFoundException e) {
			System.out.print("INFO: Legend  " + legend
					+ " not found in file system, trying jar... ");
		}
		if (legends == null) {
			is = Aok.class.getResourceAsStream(legend);
			legends = new BufferedReader(new InputStreamReader(is));
			if (legends != null)
				System.out.println("ok.");
		}
		// continue if file was found
		if (legends != null) {
			do {
				try {
					legend = legends.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // read one
				if (legend != null) { // if defined set the table value
					AokConfigOrderedNames[legendcount] = legend;
					AokConfigOrderedNumbers[legendcount] = -1;
					char first;
					try {
						first = legend.charAt(0);
					} catch (IndexOutOfBoundsException e) {
						first = ' ';
					}
					if (first != ' ' && first != '*') {
						Integer num = NamesToNumbers.get(legend);
						AokConfigOrderedNumbers[legendcount] = num;
						AokConfigNumbers[num] = legendcount;
						if (num == null) {
							System.out
									.println("ERROR: config variable "
											+ legend
											+ " in ordered list, but not in unordered.");
							System.exit(1);
						}
						NamesToRows.put(legend, legendcount);
					} else {
						if (first == '*')
							AokConfigOrderedNames[legendcount] = "<html><b>"
									+ legend.substring(1) + "</b></html>";
					}
					legendcount++;
				}
			} while (legend != null);
			try {
				legends.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			AokConfigOrderedNames[legendcount] = "";
			AokConfigOrderedNumbers[legendcount] = -1;
			legendcount++;
			AokConfigOrderedNames[legendcount] = "<html><b>Unsorted</b></html>";
			AokConfigOrderedNumbers[legendcount] = -1;
			NamesToRows.put("*Unsorted", legendcount);
			legendcount++;
			for (int i = 0; i < configcount; i++) {
				String name = AokConfigNames[i];
				if (NamesToRows.get(name) == null) {
					AokConfigNumbers[i] = legendcount;
					AokConfigOrderedNames[legendcount] = name;
					AokConfigOrderedNumbers[legendcount] = i;
					NamesToRows.put(name + " (autoadd)", legendcount);
					legendcount++;
				}
			}

		} else {
			System.out.println("Error: legend_parameters.txt not found.");
			System.exit(1);
		}

		// read the tool tips for the configuration values
		String legendmask = "AOK-ParmDoku/AOK-ParmDoku_%d.html";
		// allocate the field
		ToolTipConfig = new String[CONFIGMAX];

		for (int i = 0; i < CONFIGMAX; i++) {
			legends = null;
			is = null;
			legend = String.format(legendmask, i);
			// try to find the legend file in file system, then in jar
			try {
				try {
					legends = new BufferedReader(new InputStreamReader(
							new FileInputStream(legend), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				// System.out.print("INFO: Tooltips  " + legend
				// + " not found in file system, trying jar... ");
			}
			if (legends == null) {
				is = Aok.class.getResourceAsStream(legend);
				try {
					legends = new BufferedReader(new InputStreamReader(is,
							"utf-8"));
				} catch (Exception e) {
				}
				// if (legends != null)
				// System.out.println("ok.");
			}
			// continue if file was found
			if (legends != null) {
				int bytes = 0;
				try {
					char[] charBuffer = new char[1024 * 1024];
					bytes = legends.read(charBuffer);
					if (bytes != 0) { // if defined set the table value
						ToolTipConfig[i] = String.valueOf(charBuffer, 0, bytes);
						// System.out.println("ToolTip[" + i + "] mit "
						// + ToolTipConfig[i].length() + " Bytes");
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // read one
				try {
					legends.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				// System.out.println("Info: " + legend + " not found.");
			}
		}
	}

	/**
	 * maximum number of state values
	 * 
	 * @return the count of state values
	 */
	public int getAokStateCount() {
		return statecount;
	}

	/**
	 * maximum number of configuration values
	 * 
	 * @return the count of configuration values
	 */
	public int getAokConfigCount() {
		return configcount;
	}

	/**
	 * maximum number of ordered configuration lines
	 * 
	 * @return the count of lines in the ordered table
	 */
	public int getAokConfigTableCount() {
		return legendcount;
	}

	/**
	 * reads a state from the table (not directly from the Aok). This function
	 * is thread safe.
	 * 
	 * @param nr
	 *            number of value to read
	 * @return state value
	 * @throws InterruptedException
	 */
	public int getAokState(int nr) {

		int v = -1;
		try {
			ssem.acquire();
			v = AokStateValues[nr];
			ssem.release();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return v;
	}

	/**
	 * reads a configuration from the table (not directly from the Aok). This
	 * function is thread safe.
	 * 
	 * @param nr
	 *            number of value to read
	 * @return configuration value
	 * @throws InterruptedException
	 */
	public int getAokConfig(int nr) {

		int v = -1;
		try {
			csem.acquire();
			// System.err.print("GET");
			v = AokConfigValues[nr];
			csem.release();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return v;
	}

	/**
	 * returns the number of a config variable given it's name
	 * 
	 * @param name
	 *            name of the config variable
	 * @return
	 */
	public int getAokConfigNumber(String name) {
		if (NamesToNumbers != null && name != null) {
			Integer i = NamesToNumbers.get(name);
			if (i == null)
				return -1;
			return NamesToNumbers.get(name);
		} else
			return -1;
	}

	/**
	 * returns the row of a config variable given it's name
	 * 
	 * @param name
	 *            name of the config variable
	 * @return
	 */
	public int getAokConfigRow(String name) {
		if (NamesToRows != null & name != null) {
			Integer i = NamesToRows.get(name);
			if (i == null)
				return -1;
			return NamesToRows.get(name);
		} else
			return -1;
	}

	/**
	 * reads the name of a state variable
	 * 
	 * @param nr
	 *            number of state name to read
	 * @return the state name
	 */
	public String getAokStateName(int nr) {
		return AokStateTable[nr][1];
	}

	/**
	 * reads the name of a configuration variable
	 * 
	 * @param nr
	 *            number of configuration name to read
	 * @return the configuration name
	 */
	public String getAokConfigName(int nr) {
		return AokConfigNames[nr];
	}

	/**
	 * reads the name of a ordered configuration variable
	 * 
	 * @param nr
	 *            number of configuration name to read
	 * @return the configuration name
	 */
	public String getAokConfigOrderedName(int nr) {
		return AokConfigOrderedNames[nr];
	}

	/**
	 * sets a state in the table (not directly in the Aok). This function is
	 * thread safe.
	 * 
	 * @param nr
	 *            number of value to set
	 * @param value
	 *            the state value
	 * @throws InterruptedException
	 */
	public void setAokState(int nr, int value) {
		try {
			ssem.acquire();
			// System.out.printf("NR=%d\n",nr);
			// if (nr==0) System.err.print(".");
			AokStateValues[nr] = value;
			if (ast != null)
				ast.setValueAt(nr, value);
			for (int i = 0; i < graphs.size(); i++) {
				ScrolledGraph g = graphs.get(i);
				if (g.graph.list1 == nr || g.graph.list2 == nr
						|| g.graph.list3 == nr) {
					g.graph.valueUpdated(nr, value);
					g.update();
				}
			}
			ssem.release();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * sets a configuration in the table (not directly in the Aok). This
	 * function is thread safe.
	 * 
	 * @param nr
	 *            number of value to set
	 * @param value
	 *            the configuration value
	 * @throws InterruptedException
	 */
	public void setAokConfig(int nr, int value) {
		try {
			csem.acquire();
			// System.err.println("SET");
			AokConfigValues[nr] = value;
			csem.release();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Exports the state table
	 * 
	 * @return the AokStateTable of this instance
	 */
	public String[][] getAokStateTable() {
		return AokStateTable;
	}

	/**
	 * Exports the configuration table
	 * 
	 * @return the AokConfigTable of this instance
	 */
	public String getAokConfigTable(int row, int col) {
		return AokConfigTable[row][col];
	}

	/**
	 * Exports the configuration table
	 * 
	 * @return the AokConfigTable of this instance
	 */
	public void setAokConfigTable(int row, int col, String x) {
		AokConfigTable[row][col] = x;
	}

	/**
	 * get the debug status of the Aok. This function is thread safe.
	 * 
	 * @return true if debug mode is on, false otherwise
	 * @throws InterruptedException
	 */
	public boolean getDebug() throws InterruptedException {
		boolean v;
		ssem.acquire();
		v = debug;
		ssem.release();
		return v;
	}

	/**
	 * set the debug status of the Aok instance (not of the Arm-o-kopter). This
	 * function is thread safe.
	 * 
	 * @param debug
	 *            true if debug mode is on, false otherwise
	 * @throws InterruptedException
	 */
	public void setDebug(boolean debug) throws InterruptedException {
		ssem.acquire();
		this.debug = debug;
		ssem.release();
	}

	public String getConfigToolTip(int n) {
		return ToolTipConfig[n];
	}

	public int convertConfigFromView(int row) {
		return AokConfigOrderedNumbers[row];
	}

	public int convertConfigToView(int nr) {
		return AokConfigNumbers[nr];
	}

	/**
	 * Main Program
	 * 
	 * @param args
	 *            not used
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

//-as		NativeSwing.initialize();
		// MultiLineToolTipUI.setMaximumWidth(500);
		// MultiLineToolTipUI.initialize();
		// ToolTipManager.sharedInstance().setDismissDelay(200000);

		// create the Aok instance
		Aok aok = new Aok();

		try {
			// start the status reader
			new AokConnector(aok);
			// and register it in the status window
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		// create the window
		new AokStatusWindow(aok);

	}

}
