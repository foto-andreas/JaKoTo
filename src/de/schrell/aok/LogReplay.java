package de.schrell.aok;

import java.io.IOException;
import java.io.RandomAccessFile;

public class LogReplay implements Runnable {

	Aok aok = null;
	String filename = null;
	boolean replay = false;
	boolean quick = false;

	public LogReplay(Aok aok, String filename, boolean quick) {
		this.aok = aok;
		this.filename = filename;
		this.quick = quick;
		replay = true;
		(new Thread(this)).start();
	}

	public void run() {
		// disable the debug mode of the Aok which would interfere with the
		// commands and answers
		try {
			// disable the debug mode of the Aok which would interfere with the
			// log replay
			while (aok.getDebug()) {
				aok.aco.sc.acquire();
				aok.aco.command(aok.CMD_DEBUG_OFF);
				aok.aco.sc.release();
				aok.setDebug(false);
				aok.asb.debug.setSelected(false);
				Thread.sleep(200); // wait a moment for the reaction
			}
			RandomAccessFile f = new RandomAccessFile(filename, "r");
			long flen = f.length();
			aok.asb.acquireProgressBar(0, (int) (flen >> 8));
			String line = f.readLine();
			long start = System.currentTimeMillis();
			String[] columns;
			String[] columnnames = line.split(";");
			int[] columnnumbers = new int[columnnames.length];
			for (int i = 2; i < columnnames.length; i++) {
				columnnumbers[i] = new Integer(columnnames[i].split("[()]")[1]);
			}
			aok.debug = true;
			aok.asb.debug.setEnabled(false);
			aok.asb.connect.setEnabled(false);
			aok.asb.log.setEnabled(false);
			aok.asb.reset.setEnabled(false);
			aok.stopreplay = false;
			while (replay && (null != (line = f.readLine()))) {
				aok.asb.setProgressBarVal((int) (f.getFilePointer() >> 8));
				columns = line.split(";");
				long tim = new Long(columns[1]);
				long now = System.currentTimeMillis();
				long wait = tim - (now - start);
				if (!quick && wait > 0) {
					try {
						Thread.sleep(wait);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				for (int i = 2; i < columnnames.length; i++) {
					if (aok.ast != null)
						try {
							aok.setAokState(columnnumbers[i], new Integer(
									columns[i]));
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				}
				if (aok.stopreplay)
					break;
			}
			f.close();
			aok.setDebug(false);
			aok.asb.debug.setEnabled(true);
			aok.asb.connect.setEnabled(true);
			aok.asb.log.setEnabled(true);
			aok.asb.reset.setEnabled(true);
			aok.asb.releaseProgressBar();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
