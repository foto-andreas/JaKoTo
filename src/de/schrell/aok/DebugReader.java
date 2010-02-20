package de.schrell.aok;

import java.io.IOException;
import java.io.InputStream;

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

	Aok aok;
	InputStream in;
	boolean iscomport;
	DisplayAngle displayAngle = null;
	
	/**
	 * create a debug reader instance
	 * 
	 * @param in
	 */
	public DebugReader(Aok aok, InputStream in, boolean iscomport) {
		super();
		this.aok = aok;
		this.in = in;
		this.iscomport = iscomport;
	}

	/**
	 * run task for the debug reader. The task runs until the Aok is unplugged
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			byte b = 0, B[];
			B = new byte[6];
			System.out.println("Aok debug value reader starting...");
			long lastC = 0;
			int nullcount = 0;
			while (iscomport || in.available() > 0) { // run only while Aok is
				// connected
				if (iscomport && !aok.aco.connected) {
					Thread.sleep(500);
					// System.out.print(".");
					continue;
				}
				// System.out.print(":");
				if (iscomport) {
					in = aok.aco.in;
					aok.aco.sc.acquire(); // acquire the serial line
				}
				if (in.available() > 0) { // data available at serial line
					byte nr = 0;
					b = getByte(); // get the info byte
					if (b == 0x1C) { // perhaps the start of a debug entry
						nr = getByte(); // get number of debug entry
						byte crc = getBytes(B, 5, nr); // get value and crc
						// sum
						if (crc == 0x00) { // check crc sum
							int value = bytesToInt(B); // convert the bytes
							// to an integer
							// update the table only when the current number
							// is defined by the legend file
							if (nr == 0) {
								if (displayAngle==null) {
//									displayAngle = new DisplayAngle(aok);
								} else {
//									displayAngle.calc();
								}
								// System.out.print(":");
								aok.logfile.log(aok.AokStateValues);
								long aktC = System.currentTimeMillis();
								nullcount++;
								if (lastC > 0) {
									if (aktC - lastC > 5000) {

										String freq = String.format("%.2f Hz",
												1000.0 * nullcount
														/ (aktC - lastC));
										aok.ast.setFreq(freq);
										lastC = aktC;
										nullcount = 0;
									}
								} else {
									lastC = System.currentTimeMillis();
								}
							}
							if (nr >= 0 && nr < aok.getAokStateCount()) {
								// System.out.printf("XX=%d\n",nr);
								aok.setAokState(nr, value);
							}
							// update the debug status if the crc checksum
							// was correct
							if (iscomport && nr == 0) {
								aok.setDebug(true);
								aok.asb.debug.setSelected(true);
							}
						} else { // CRC error
                            System.out.print('D');
/*							System.out
									.printf(
											"CRC-Error (DBG): %02x   %02x %02x %02x %02x >> %02x != %02x\n",
											nr, B[0], B[1], B[2], B[3], crc, 0);
*/						}
					} else {
						// if (b > 0x1F && b < 0x80)
						System.out.print((char)b);
					}

					if (iscomport)
						aok.aco.sc.release(); // release the serial line
					// else if (nr==0) Thread.sleep(10);
				} else {
					if (iscomport)
						aok.aco.sc.release(); // release the serial line
					Thread.sleep(10); // wait a moment before next
					if (!aok.getDebug()) lastC = 0;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	/**
	 * read a byte from the aok
	 * 
	 * @return the byte read
	 * @throws IOException
	 */
	private byte getByte() throws IOException {
        if (!aok.aco.connected && this.in.available()<1) return (byte)0xFF;
		return (byte) this.in.read();
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
	private byte getBytes(byte B[], int num, int init, int offset)
			throws IOException {
		byte crc = (byte) init;
		for (int i = 0; i < num; i++) {
			B[i + offset] = getByte();
			crc ^= B[i + offset];
		}
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
	private byte getBytes(byte B[], int num, int init) throws IOException {
		return getBytes(B, num, init, 0);
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

}
