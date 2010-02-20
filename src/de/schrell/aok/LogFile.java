package de.schrell.aok;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Semaphore;

public class LogFile {

	PrintStream log = null;
	String path = "", logname = "";
	Aok aok = null;
	long startTime;
	Semaphore sem;
	boolean justStarted = false;

	public LogFile(Aok aok, String path) {
		this.path = path;
		this.aok = aok;
	}

	public String startLog(String[] table) {
		sem = new Semaphore(1);
		logname = "AOK-log-" + new TimeStamp().toString() + ".csv";
		try {
			sem.acquire();
			log = new PrintStream(path + File.separator + logname);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			aok.ast.table.setEnabled(true);
			return "";
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			aok.ast.table.setEnabled(true);
			return "";
		}
		aok.log = true;
		log.print("Timestamp;Time [ms]");
		boolean all = !aok.ast.isStatusSelected();
		for (int i = 0; i < table.length; i++)
			if (table[i] != null && all || aok.ast.isStatusSelected(i))
				log.printf(";%s (%d)", table[i], i);
		log.println();
		aok.ast.table.setEnabled(false);
		startTime = System.currentTimeMillis();
		justStarted = true;
		sem.release();
		return logname;
	}

	public void stopLog() {
		try {
			sem.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		aok.ast.table.setEnabled(true);
		log.close();
		aok.log = false;
		sem.release();
		// System.out.println("STOPLOG");
	}

	public void log(int[] table) {
		// System.out.println("LOG");
		if (aok.log && aok.logfile != null & !justStarted) {
			try {
				sem.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			long now = System.currentTimeMillis();
			log
					.printf("\"%s\";%d", new TimeStamp().toString(), now
							- startTime);
			boolean all = !aok.ast.isStatusSelected();
			for (int i = 0; i < table.length; i++)
				if (all || aok.ast.isStatusSelected(i))
					log.printf(";%d", table[i]);
			log.println();
			sem.release();
		}
		justStarted = false;
	}

	public class TimeStamp extends Date {

		private static final long serialVersionUID = -532413068887872325L;

		@Override
		public String toString() {
			SimpleDateFormat sdfToString = new SimpleDateFormat(
					"yyyy.MM.dd-HH.mm.ss");
			String sOutDate = sdfToString.format(this);
			return sOutDate;
		}
	}

}
