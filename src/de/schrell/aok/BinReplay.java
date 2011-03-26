package de.schrell.aok;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BinReplay implements Runnable {

	Aok aok = null;
	String filename = null;
	boolean replay = false;

	public BinReplay(Aok aok, String filename) {
		this.aok = aok;
		this.filename = filename;
		replay = true;
		(new Thread(this)).start();
	}

	public void run() {
		// disable the debug mode of the Aok which would interfere with the
		// commands and answers
		try {
			// disable the debug mode of the Aok which would interfere with the
			// log replay
			aok.aco.disconnect();
			aok.stopreplay = false;
		    if (filename==null) {
		    	System.err.println("InputStream could not be opened.");
		    } else {
		    	System.out.println("InputStream for '" + filename + "' will be opened...");
		    }
		    File file = new File(filename);
		    InputStream is = new FileInputStream(file);
		    long flen = file.length();
			aok.asb.acquireProgressBar(0, (int) (flen >> 8));
		    aok.getDebugReader().setStream(is);
		    aok.asb.releaseProgressBar();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
