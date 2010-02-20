package de.schrell.aok;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

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
			while (aok.getDebug()) {
				aok.aco.sc.acquire();
				aok.aco.command(aok.CMD_DEBUG_OFF);
				aok.aco.sc.release();
				aok.setDebug(false);
				aok.asb.debug.setSelected(false);
				Thread.sleep(200); // wait a moment for the reaction
			}
			aok.debug = true;
			aok.asb.debug.setEnabled(false);
			aok.asb.connect.setEnabled(false);
			aok.asb.log.setEnabled(false);
			aok.asb.reset.setEnabled(false);
			aok.stopreplay = false;
			URL url = new URL ("file://"+filename);
		    InputStream is = url.openStream();
			new Thread(new DebugReader(aok, is, false)).start();
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
