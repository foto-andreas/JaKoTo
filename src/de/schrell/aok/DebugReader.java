package de.schrell.aok;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Reader task for the Aok debugging values. Starts in the background and waits
 * for debug values from the Aok. If a correct debug field for the debug value
 * with number zero is read the debug flag in the Aok instance is updated and
 * the debug checkbox in AokStatusWindow set to checked.
 * 
 * @author Andreas Schrell
 * 
 */
public class DebugReader implements Runnable {

	private Aok aok;
	// private DisplayAngle displayAngle = null;
	private InputStream in = null;
	private InputStream savedStream = null;
	private long lastC;
	private static int nullcount = 0;
	private boolean info = true;
	
	private StatusListenerList[] listener = null;

	private class StatusListenerList extends ArrayList<StatusListener> {
		private static final long serialVersionUID = 8239692029617540417L;
	}

	/**
	 * create a debug reader instance
	 * 
	 * @param in
	 */
	public DebugReader(Aok aok) {
		super();
		this.aok = aok;
		listener = new StatusListenerList[aok.configcount];
		for (int i = 0; i < aok.configcount; i++) {
			listener[i] = new StatusListenerList();
		}
	}

	public InputStream getStream() {
		return in;
	}

	/**
	 * run task for the debug reader. The task runs until the Aok is unplugged
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			System.out.println("Aok debug value reader created...");
			lastC = 0;
			while (true) {
				synchronized (DebugReader.this) {
					System.out.println("\nputting DebugReader to sleep...");
					while (getStream() == null) {
						DebugReader.this.wait(500);
					}
					System.out.println("\nwaking up DebugReader...");
				}
				InputStream in = getStream();
				long bytesRead = 0;
				while (((in instanceof FileInputStream) && (in.available() > 0))
						|| aok.aco.connected) {
					bytesRead += readValues(in);
					if (in instanceof FileInputStream) {
						aok.asb.setProgressBarVal((int) (bytesRead >> 8));
					}
				}
				if (in instanceof FileInputStream) {
					in.close();
				}
				setStream(null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	private int readValues(InputStream in) throws IOException,
			InterruptedException {
		byte B[] = new byte[6];
		int b;
		int av = in.available();
		if (av > 0) { // data available at serial line
			int nr = 0;
			b = getByte(); // get the info byte
			if (b == -1)
				return 0;
			if (b == 0x1C) { // perhaps the start of a debug entry
				nr = getByte(); // get number of debug entry
				if (nr == -1)
					return 1;
				byte crc = getBytes(B, 5, nr); // get value and crc
				if (crc == 0x00) { // check crc sum
					aok.setDebug(true);
					final int value = bytesToInt(B); // convert the bytes to an
					// integer
					// update the table only when the current number
					// is defined by the legend file
					if (nr < aok.configcount) {
						synchronized (listener[nr]) {
							for (StatusListener listen : listener[nr]) {
								listen.valueChanged(value);
							}
						}
					}
					if (nr == 0) {
						// if (displayAngle == null) {
						// displayAngle = new DisplayAngle(aok);
						// } else {
						// displayAngle.calc();
						// }
						aok.setDebug(true);
						aok.logfile.log(aok.AokStateValues);
						long aktC = System.currentTimeMillis();
						nullcount++;
						if (lastC > 0) {
							if (aktC - lastC > 5000) {
								String freq = String.format("%.2f Hz", 1000.0
										* nullcount / (aktC - lastC));
								aok.ast.setFreq(freq);
								lastC = aktC;
								nullcount = 0;
							}
						} else {
							lastC = System.currentTimeMillis();
						}
					}
					if (nr >= 0 && nr < aok.getAokStateCount()) {
						// System.out.printf("XX=%d\n", nr);
						aok.setAokState(nr, value);
					}
					if (nr == Aok.STATUS_MAG_X_RAW) {
						aok.atmag.updateRaw(0, value);
					}
					if (nr == Aok.STATUS_MAG_Y_RAW) {
						aok.atmag.updateRaw(1, value);
					}
					if (nr == Aok.STATUS_MAG_Z_RAW) {
						aok.atmag.updateRaw(2, value);
					}
					// update the debug status if the crc checksum was correct
					if (nr == 0) {
						aok.setDebug(true);
						aok.asb.debug.setSelected(true);
					}
				} else { // CRC error
					if (info) System.out.print('D');
				}
			} else {
				if ((b > 0x1F && b < 0x80) || (b == 0x0d) || (b == 0x0a))
					if (info) System.out.print((char) b);
				else
					if (info) System.out.printf("[%02X]", b);
			}
		} else {
			Thread.sleep(10); // wait a moment before next
			if (!aok.getDebug())
				lastC = 0;
		}
		return 5;
	}

	/**
	 * read a byte from the aok
	 * 
	 * @return the byte read
	 * @throws IOException
	 */
	private synchronized int getByte() throws IOException {
		int read = 0xFF;
		if (in == null)
			return -1;
		try {
			long s1 = System.currentTimeMillis();
			while (in.available() == 0) {
				Thread.sleep(10);
				if (System.currentTimeMillis() - s1 > 100) {
					return -1;
				}
			}
			read = in.read();
		} catch (InterruptedException e) {
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
	private synchronized byte getBytes(InputStream in, byte B[], int num,
			int init, int offset) throws IOException {
		boolean err = false;
		byte crc = (byte) init;
		for (int i = 0; i < num; i++) {
			int b = getByte();
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
	private synchronized byte getBytes(byte B[], int num, int init)
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
	@SuppressWarnings("unused")
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

	public synchronized void setStream(InputStream in) {
		this.in = in;
		if (in != null) {
			System.out.println("new Stream for DebugReader.");
		} else {
			System.out.println("Disabled Stream for DebugReader.");
		}
		notify();
	}

	public synchronized void pause() {
		savedStream = getStream();
		setStream(null);
	}

	public synchronized void resume() {
		setStream(this.savedStream);
		this.savedStream = null;
		notify();
	}

	public void addStatusListener(int configNumber,
			StatusListener statusListener) {
		synchronized (listener[configNumber]) {
			listener[configNumber].add(statusListener);
		}
	}

	public void removeStatusListener(int configNumber,
			StatusListener statusListener) {
		synchronized (listener[configNumber]) {
			listener[configNumber].remove(statusListener);
		}
	}

	public void setInfo(boolean info) {
		this.info = info;
	}
	
}
