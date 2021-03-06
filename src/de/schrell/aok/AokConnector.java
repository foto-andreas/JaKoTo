package de.schrell.aok;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * Class for the communication with the aok through serial interfaces
 * 
 * @author Andreas Schrell
 * 
 */
public class AokConnector {

	/** instance of connected Aok class */
	Aok aok = null;
	/** Communication Port */
	CommPort commPort = null;
	/** output stream to the aok */
	OutputStream outs = null;
	/** input stream to the Aok */
	InputStream in = null;
	/** is the serial line connected to the Aok */
	boolean connected = false;
	/** a semaphore for the serial communication line used by parallel threads */
	// Semaphore sc = new Semaphore(1);
	Thread disconThread = null;
	boolean inshutdown = false;
	boolean info = true;
	
	/**
	 * Create an instance and wire the necessary instances of other classes
	 * 
	 * @param aok
	 *            the Aok instance to use
	 */
	public AokConnector(Aok aok) {
		this.aok = aok;
		aok.aco = this;
	}

	public class DisCon implements Runnable {

		public void run() {
			inshutdown = true;
			disconnect();
		}
	}

	/**
	 * connect over a serial line to the Aok
	 * 
	 * @param portName
	 *            the device, e.g. /dev/rfcomm0, /dev/ttyS0, COM1: etc.
	 * @param portSpeed
	 *            the Speed of the communication line
	 * @return show whether the connection is established or not
	 */
	public boolean connect(String portName, String portSpeed) {
		CommPortIdentifier portIdentifier = null;
		connected = false;
		try {
			portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		} catch (NoSuchPortException e1) {
			System.out.println("Error: Port " + portName + " is not available");
			return false;
		}
		if (portIdentifier.isCurrentlyOwned()) {
			System.out.println("Error: Port " + portName
					+ " is currently in use");
			System.out.printf("Owner: %s\n", portIdentifier.getCurrentOwner());
			return false;
		}
		commPort = null;
		try {
			commPort = portIdentifier.open(this.getClass().getName(), 2000);
		} catch (PortInUseException e) {
			System.out.println("Error: Port " + portName + " is in use");
			connected = false;
			return false;
		}

		if (commPort != null && commPort instanceof SerialPort) {
			SerialPort serialPort = (SerialPort) commPort;
			try {
				serialPort.setSerialPortParams(new Integer(portSpeed),
						SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
						SerialPort.PARITY_NONE);
				serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
				serialPort.setDTR(false);
				serialPort.setInputBufferSize(1024);
				serialPort.setOutputBufferSize(1024);
				in = serialPort.getInputStream();
				outs = serialPort.getOutputStream();
				disconThread = new Thread(new DisCon());
				Runtime.getRuntime().addShutdownHook(disconThread);
				connected = true;
				System.out.println("Port initialized.");
				aok.getDebugReader().setStream(in);
				return true;
			} catch (UnsupportedCommOperationException e) {
				e.printStackTrace();
				System.out.println("Error: Port " + portName
						+ " unsupported command");
				serialPort.close();
			} catch (IOException e) {
				System.out.println("Error: Port " + portName + " IO-Exception");
				serialPort.close();
			}
		} else {
			System.out
					.println("Error: Only serial ports are handled by this task.");
		}
		connected = false;
		return false;
	}

	public synchronized boolean disconnect() {
		if (!connected) {
			return true;
		}
		System.out.println("Closing connection...");
		connected = false;
		try {
			// if (!inshutdown) {
			// Runtime.getRuntime().removeShutdownHook(disconThread);
			// }
			aok.setDebug(false);
			aok.asb.debug.setSelected(false);
			aok.asb.connect.setSelected(false);
			in.close();
			outs.close();
			commPort.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		System.out.println("Connection closed.");
		aok.ast.setFreq(null);
		return true;
	}

	/**
	 * class for reading configuration values from the Aok
	 * 
	 * @author andreas
	 * 
	 */
	public class ReadConfigFromAok implements Runnable {

		/**
		 * running thread which calls the internal config variable reader
		 * 
		 * @see java.lang.Runnable#run()
		 */
		boolean check = false;

		public ReadConfigFromAok(boolean check) {
			this.check = check;
		}

		/** @Override */
		public void run() {
			try {
				readConfigFromAokInternal(check);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * starts a thread which reads all configuration variables from Aok ram
	 */
	public void readConfigFromAok(boolean check) {
		(new Thread(new ReadConfigFromAok(check))).start();
	}
	
	public void readConfigFromAokSync() {
		try {
			readConfigFromAokInternal(false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * read all configuration values from the Aok and import them into the
	 * config table. Use the status bar to show it running.
	 * 
	 * @throws Exception
	 */
	private void readConfigFromAokInternal(boolean check) throws Exception {

		// check, if we are online
		if (!connected) {
			return;
		}

		// config values byte field
		byte B[] = new byte[5];

		// check whether there are selected rows in in the configuration table
		boolean getall = !aok.act.isConfigSelected();

		// initialize the progress bar
		aok.asb.acquireProgressBar(0, getall ? (aok.getAokConfigCount() - 1)
				: aok.act.getConfigSelectedCount());

		// disable the debug mode of the Aok which would interfere with the
		// commands and answers
		boolean olddebug = debugOff();

		// acquire the serial line
		aok.getDebugReader().pause();

		// Info
		if (check) {
			System.out.print("check: [");
		} else {
			System.out.print("read:  [");
		}

		// ask the Aok for all configuration values or only the selected ones if
		// one ore more are selected
		int count = 0;
		boolean errors = false;
		for (int i = 0; i < aok.getAokConfigCount(); i++) {
			if (getall || aok.act.isConfigSelected(aok.convertConfigToView(i))) {
				int config = 0; // the actual configuration value
				// clean it up from spurious debug values
				while (in.available() > 0) {
					getByte(in);
				}
				command(aok.CMD_READCONF); // send command
				command((byte) i); // send the configuration value number
				getByte(in);
				int pnum = getByte(in);
				byte crc = getBytes(in, B, 5, 0); // read the value and the crc
				// checksum
				// set the configuration value if the crc was correct
				if (pnum == i && crc == 0x00) {
					config = bytesToInt(B); // calculate the configuration value
					if (check) {
						if (aok.getAokConfig(i) != config) {
							errors = true;
							System.out
									.printf("\nConfig Value %d (%s) differs: %d (TAB) != %d (AOK).\n",
											i, aok.getAokConfigName(i),
											aok.getAokConfig(i), config);
							aok.asb.checkconf.setForeground(Color.red);
							aok.asb.checkconf.repaint();
							if (info) System.out.print('X');
						} else {
							if (info) System.out.print('O');
						}
					} else {
						aok.act.setConfigAt(aok.convertConfigToView(i), config);
						if (info) System.out.print('O');
					}
					aok.asb.setProgressBarVal(count++); // update the status bar
				} else {
					if (info) System.out.print('E');
					/*
					 * System.out.printf(
					 * "CRC-Error (RCFG):   %03d %02x %02x %02x %02x >> %02x != %02x\n"
					 * , i, B[0], B[1], B[2], B[3], crc, 0);
					 */
					i--; // nochmal versuchen
				}
			}
		}
		System.out.println(']');
		aok.getDebugReader().resume();

		// release the progress bar
		aok.asb.releaseProgressBar();

		// set config to loaded
		if (!check) {
			aok.configloaded = true;
		}

		// info to user
		if (check) {
			if (errors) {
				aok.asb.checkconf.setForeground(Color.red);
			} else {
				aok.asb.checkconf.setForeground(Color.green);
			}
			Thread.sleep(3000);
			aok.asb.checkconf.setForeground(Color.black);
		}
		// if Aok was in debug mode before it is restored now
		if (olddebug) {
			// sc.acquire();
			command(aok.CMD_DEBUG_ON);
			// sc.release();
		}

	}

	/**
	 * class for writing configuration values to the Aok
	 * 
	 * @author andreas
	 * 
	 */
	public class WriteConfigToAok implements Runnable {

		/**
		 * running thread which calls the internal config variable writer
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			try {
				writeConfigToAokInternal();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error: could not write configuration");
			}
		}
	}

	/**
	 * starts a thread which reads all configuration variables from Aok ram
	 */
	public void writeConfigToAok() {
		(new Thread(new WriteConfigToAok())).start();
	}

	/**
	 * starts a thread which reads all configuration variables from Aok ram synchronous
	 */
	public void writeConfigToAokSync() {
		try {
			writeConfigToAokInternal();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * write all configuration values to the Aok - optionally only selected
	 * values. Use the status bar to show it running.
	 * 
	 * @throws Exception
	 */
	private void writeConfigToAokInternal() throws Exception {

		// check, if we are online
		if (!connected) {
			return;
		}

		// check whether there are selected rows in in the configuration table
		boolean writeall = !aok.act.isConfigSelected();

		// initialize the progress bar
		aok.asb.acquireProgressBar(0, writeall ? (aok.getAokConfigCount() - 1)
				: aok.act.getConfigSelectedCount());

		// disable the debug mode of the Aok which would interfere with the
		// commands and answers
		boolean olddebug = debugOff();

		// acquire the serial line
		aok.getDebugReader().pause();

		// Info
		System.out.print("write: [");

		// ask the Aok for all configuration values or only the selected ones if
		// one ore more are selected
		int count = 0;
		for (int i = 0; i < aok.getAokConfigCount(); i++) {
			if (writeall
					|| aok.act.isConfigSelected(aok.convertConfigToView(i))) {
				char erg = writeSingleConfigToAok(i);
				if (erg == 'X') {
					break;
				}
				if (erg == 'O') {
					aok.asb.setProgressBarVal(count++); // update the status bar
					if (info) System.out.print('O');
				} else {
					i--; // Wert nochmal versuchen
					if (info) System.out.print('E');
				}
			}
		}
		// Info
		System.out.println(']');

		aok.getDebugReader().resume();

		// release the progress bar
		aok.asb.releaseProgressBar();

		// if Aok was in debug mode before it is restored now
		if (olddebug) {
			// sc.acquire();
			command(aok.CMD_DEBUG_ON);
			// sc.release();
		}
	}

	public char writeSingleConfigToAok(int i) throws IOException {
		byte B[] = new byte[7];
		if (!connected)
			return 'X';
		int config = aok.getAokConfig(i); // the actual configuration
		aok.setAokConfig(i, config); // the actual configuration
		// value
		B[0] = aok.CMD_WRITECONF;
		B[1] = (byte) i;
		intToBytes(B, config, 2);
		B[6] = crc(B, 1, 5, 0);
		command(B); // send command
		int erg = getByte(in);
		if (erg == -1)
			return 'X';
		return (char) erg;
	}

	/**
	 * class for reading configuration values from the Aok
	 * 
	 * @author andreas
	 * 
	 */
	public class FlashAok implements Runnable {

		Aok aok;
		String fw;

		public FlashAok(Aok aok, String fw) {
			this.aok = aok;
			this.fw = fw;
		}

		/**
		 * running thread which uses the AOK bootloader to flash a new firmware
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try {
				flashAokInternal(aok, fw);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * starts a thread which reads all configuration variables from Aok ram
	 */
	public void flashAok(Aok aok, String fw) {
		(new Thread(new FlashAok(aok, fw))).start();
	}

	/**
	 * write firmware to AOK
	 * 
	 * @throws Exception
	 */
	private void flashAokInternal(Aok aok, String fw) throws Exception {

		// check, if we are online
		if (!connected) {
			return;
		}

		// create a file instance for reading the firmware
		File f = new File(fw);

		// initialize the progress bar
		aok.asb.acquireProgressBar(0, (int) f.length());

		// disable the debug mode of the Aok which would interfere with the
		// commands and answers
		aok.getDebugReader().pause();

		System.err.println("disable debug values...");
		debugOff();

		System.err.println("starting update...");

		// read the firmware file and treansfer it in 64 byte chunks to AOK
		RandomAccessFile fi = new RandomAccessFile(f, "r");
		long fpos = 0, pos = 0;
		command(aok.CMD_FLASH);
		for (int i = 0; i < 20; i++) {
			Thread.sleep(100);
			while (in.available() > 0) {
				int b = getByte(in);
				System.out.print((char) b);
			}
		}
		Thread.sleep(500);
		long length = fi.length();
		byte crc = 0;
		String line = "";
		// lesen, solange Dateiende noch nicht erreicht
		while (pos < length) {
			byte c = (byte) fi.read();
			// neuer Block startet hier
			if (pos % 64 == 0) {
				crc = 0;
				line = "";
				fpos = pos;
			}
			// alle Zeichen verarbeiten
			crc ^= c;
			line += String.format("%02X", c);
			// Blockende oder Dateiende erreicht
			if (pos % 64 == 63 || pos == length - 1) {
				// append the crc value and a CR/LF
				line += String.format("%02X\r\n", crc);
				// send it to the AOK
				// Thread.sleep(100);
				command(line.getBytes());
				// System.err.write(line.getBytes());
				// read the answer
				int erg = getByte(in);
				// System.out.printf("READBYTE=%02x (%c)",erg,erg);
				// and check it
				switch (erg) {
				case 'E': // error
					// reset to last chunk position to resend the last chunk
					fi.seek(fpos);
					pos = fpos - 1; // will be incremented some lines below
					System.out.print('E');
					break;
				case 'O': // ok
					// update the progress bar
					aok.asb.setProgressBarVal((int) pos);
					System.out.print('O');
					break;
				default: // unknown answer
					System.out.println();
					System.out.print("ERROR: wrong return value from AOK: "
							+ erg);
					break;
				}
				if (erg != 'E' && erg != 'O')
					break;
			}
			pos++;
		}

		// tell the AOK we have finished
		command(aok.CMD_FLASH_END);

		System.out.println();

		Thread.sleep(1000);
		while (in.available() > 0) {
			int b = getByte(in);
			System.out.print((char) b);
		}
		aok.getDebugReader().resume();

		// release the serial line
		// sc.release();

		// release the progress bar
		aok.asb.releaseProgressBar();

		// aok.aco.disconnect();
		// aok.asb.connect.setSelected(false);
	}

	/**
	 * write a command to the Aok
	 * 
	 * @param cmd
	 *            the command as a byte array
	 * @throws IOException
	 */
	public void command(byte cmd[]) throws IOException {
		outs.write(cmd);
	}

	/**
	 * write a command (single byte) to the Aok
	 * 
	 * @param cmd
	 *            the byte to transmit
	 * @throws IOException
	 */
	public void command(byte cmd) throws IOException {
		outs.write(cmd);
	}

	/**
	 * read a byte from the aok
	 * 
	 * @return the byte read
	 * @throws IOException
	 */
	private int getByte(InputStream in) throws IOException {
		int read = 0xFF;
		try {
			if (!connected) {
				return -1;
			}
			long s1 = System.currentTimeMillis();
			while (in.available() == 0) {
				Thread.sleep(50);
				if (System.currentTimeMillis() - s1 > 1000) {
					if (info) System.out.print('-');
					return -1;
				}
			}
			read = in.read();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return read;
	}

	/**
	 * read some bytes from the Aok and calculate the corresponding checksum
	 * 
	 * @param B
	 *            read into this byte array
	 * @param num
	 *            number of bytes to read
	 * @param init
	 *            initial value for the checksum
	 * @param offset
	 *            offset to start in byte array
	 * @return the corresponding crc checksum
	 * @throws IOException
	 */
	private byte getBytes(InputStream in, byte B[], int num, int init,
			int offset) throws IOException {
		byte crc = (byte) init;
		boolean err = false;
		for (int i = 0; i < num; i++) {
			int b = getByte(in);
			if (b == -1) {
				err = true;
				break;
			}
			B[i + offset] = (byte) b;
			crc ^= B[i + offset];
		}
		if (err)
			return crc ^= 0xFF;
		return crc;
	}

	/**
	 * read some bytes from the Aok and calculate the corresponding checksum
	 * 
	 * @param B
	 *            read into this byte array
	 * @param num
	 *            number of bytes to read
	 * @param init
	 *            initial value for the checksum
	 * @return the corresponding crc checksum
	 * @throws IOException
	 */
	private byte getBytes(InputStream in, byte B[], int num, int init)
			throws IOException {
		return getBytes(in, B, num, init, 0);
	}

	/**
	 * transform four read bytes to an integer
	 * 
	 * @param B
	 *            the byte array to transform
	 * @return the calculated integer
	 */
	private int bytesToInt(byte B[]) {
		return bytesToInt(B, 0);
	}

	/**
	 * transform four read bytes to an integer using an array offset
	 * 
	 * @param B
	 *            the byte array to transform
	 * @param off
	 *            the offset of the first byte to use (starting at 0)
	 * @return the calculated integer
	 */
	private int bytesToInt(byte B[], int off) {
		return ((B[3 + off] & 0x000000FF) << 24)
				| ((B[2 + off] & 0x000000FF) << 16)
				| ((B[1 + off] & 0x000000FF) << 8) | (B[0 + off] & 0x000000FF);
	}

	/**
	 * converts an integer value into a byte array
	 * 
	 * @param B
	 *            the byte array
	 * @param v
	 *            the integer value
	 * @param off
	 *            the offset into the byte array
	 */
	private void intToBytes(byte[] B, int v, int off) {
		int b;
		b = v & 0x000000FF;
		B[0 + off] = (byte) b;
		v ^= b;
		b = v & 0x0000FF00;
		B[1 + off] = (byte) (b >> 8);
		v ^= b;
		b = v & 0x00FF0000;
		B[2 + off] = (byte) (b >> 16);
		v ^= b;
		b = v & 0xFF000000;
		B[3 + off] = (byte) (b >> 24);
	}

	/**
	 * converts a short value into a byte array
	 * 
	 * @param B
	 *            the byte array
	 * @param v
	 *            the integer value
	 * @param off
	 *            the offset into the byte array
	 */
	private void shortToBytes(byte[] B, short v, int off) {
		int b;
		b = v & 0x000000FF;
		B[0 + off] = (byte) b;
		v ^= b;
		b = v & 0x0000FF00;
		B[1 + off] = (byte) (b >> 8);
	}

	/**
	 * calculates a xor crc value for some bytes.
	 * 
	 * @param B
	 *            the byte array which holds the data
	 * @param start
	 *            the first byte to use (starting at 0)
	 * @param end
	 *            the last byte to use
	 * @param init
	 *            an initial value for the checksum
	 * @return the calculated crc value
	 */
	private byte crc(byte B[], int start, int end, int init) {
		byte crc = (byte) init;
		for (int i = start; i <= end; i++) {
			crc ^= B[i];
		}
		return crc;
	}

	/**
	 * reset the Aok
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void reset() throws IOException, InterruptedException {
		// check if we are online
		if (!connected) {
			return;
		}
		// sc.acquire();
		debugOff();
  		command(aok.CMD_RESET);
		aok.getDebugReader().setStream(in);
		// sc.release();
		aok.ast.setFreq(null);
		System.out.println("AOK reset");
	}

	/**
	 * display a source position debug value on the screen
	 * 
	 * @param i
	 *            number to display
	 */
	void debug(int i) {
		System.out.printf("Debug: %d\n", i);
	}

	/**
	 * read AOK-RAM from flash set
	 * 
	 * @param i
	 *            the flash set to use
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void flashread(int i) throws InterruptedException, IOException {
		if (!connected) {
			return;
		}
		// sc.acquire();
		command(aok.CMD_FLASHREAD);
		command((byte) i);
		// sc.release();
		System.out.println("AOK flash read #" + i + " into RAM");
	}

	/**
	 * write AOK-RAM to flash set
	 * 
	 * @param i
	 *            the flash set to use
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void flashwrite(int i) throws InterruptedException, IOException {
		if (!connected) {
			return;
		}
		// sc.acquire();
		command(aok.CMD_FLASHWRITE);
		command((byte) i);
		// sc.release();
		System.out.println("AOK flash write #" + i + " from RAM");
	}

	/**
	 * set flash set as start set
	 * 
	 * @param i
	 *            the flash set to use
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void startset(int i) throws InterruptedException, IOException {
		if (!connected) {
			return;
		}
		command(aok.CMD_STARTSET);
		command((byte) i);
		System.out.println("AOK flash #" + i + " set as start set");
	}

	/**
	 * Motortest mit AOK
	 * 
	 * @param motor
	 * @param value
	 * @throws IOException
	 */
	public void motorTest(int motor, int value) throws IOException {
		if (!connected) {
			return;
		}
		byte c = (byte)(0x31+motor);
		command(c);
		command((byte)value);
		command((byte)(c^value));
	}
	
	public void joyStick(int roll, int nick, int yaw, int pitch, int expiry) {
		byte[] B = new byte[12];
		B[0] = 0x53;
		shortToBytes(B,(short)roll,1);
		shortToBytes(B,(short)nick,3);
		shortToBytes(B,(short)yaw,5);
		shortToBytes(B,(short)pitch,7);
		shortToBytes(B,(short)expiry,9);
		B[11] = crc(B,1,10,0);
		for (int i=0; i<12; i++) {
			System.out.printf("%02X ", B[i]);
		}
		System.out.println();
		try {
			command(B);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Toggle the Aok debugging mode and update the infos in the debug window.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void debugOn() {
		// check if we are online
		if (!connected) {
			return;
		}
		aok.getDebugReader().pause();
		System.out.print("[");
		int d[] = { 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF };
		if (aok.ast.isStatusSelected()) {
			d[0] = d[1] = d[2] = d[3] = d[4] = 0x00000000;
			for (int i = 0; i < 32; i++) {
				if (aok.ast.isStatusSelected(i)) {
					d[0] |= 1 << i;
				}
			}
			for (int i = 32; i < 64; i++) {
				if (aok.ast.isStatusSelected(i)) {
					d[1] |= 1 << (i - 32);
				}
			}
			for (int i = 64; i < 96; i++) {
				if (aok.ast.isStatusSelected(i)) {
					d[2] |= 1 << (i - 64);
				}
			}
			for (int i = 96; i < 128; i++) {
				if (aok.ast.isStatusSelected(i)) {
					d[3] |= 1 << (i - 96);
				}
			}
			for (int i = 128; i < 160; i++) {
				if (aok.ast.isStatusSelected(i)) {
					d[4] |= 1 << (i - 128);
				}
			}
		}
		/** disable debug-value-00, otherwise it would come twice */
		d[0] &= 0xFFFFFFFF | 1;
		aok.setAokConfig(aok.convertConfigToView(92), d[0]);
		aok.setAokConfig(aok.convertConfigToView(93), d[1]);
		aok.setAokConfig(aok.convertConfigToView(94), d[2]);
		aok.setAokConfig(aok.convertConfigToView(95), d[3]);
		aok.setAokConfig(aok.convertConfigToView(96), d[4]);

		try {
			for (int i = 0; i < 5; i++) {
				byte B[] = new byte[7];
				B[0] = aok.CMD_WRITECONF;
				B[1] = (byte) (92 + i);
				intToBytes(B, d[i], 2);
				B[6] = crc(B, 1, 5, 0);
				command(B); // send command
				int erg = getByte(in);
				if (erg != 'O') {
					if (info) System.out.print('F');
					i--;
				} else {
					if (info) System.out.print('O');
				}
			}
			command(aok.CMD_DEBUG_ON);
		} catch (Exception e) {
			e.printStackTrace();
		}
		aok.setDebug(true);
		System.out.println("]");
		aok.getDebugReader().resume();
		System.out.println("\nAOK debugging in now on");
	}

	public boolean debugOff() {
		System.out.println("debug off...");
		boolean wasOn = false;
		if (!connected) {
			return aok.getDebug();
		}
		aok.getDebugReader().pause();
		while (aok.getDebug()) {
			wasOn = true;
			try {
				command(aok.CMD_DEBUG_OFF);
				Thread.sleep(250); // wait a moment for the reaction
				command(aok.CMD_DEBUG_OFF);
				while (in.available() > 0) {
					in.skip(in.available());
					Thread.sleep(100);
				}
				aok.setDebug(false);
				aok.asb.debug.setSelected(false);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		System.out.println("AOK debugging is now off.");
		aok.ast.setFreq(null);
		aok.getDebugReader().resume();
		return wasOn;
	}

	public void setInfo(boolean info) {
		this.info = info;
	}
	
	public void dialogNotConnected() {
		new MyDialog("AOK not connected","The AOK is not connected to the the computer","OK",true).show();
	}
	
}
