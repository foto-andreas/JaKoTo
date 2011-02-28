package de.schrell.aok;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * class for reading and writing AOK config files
 * 
 * @author Andreas Schrell
 * 
 */
public class ConfigFile {

	/** The Aok instance to read and set configuration values in */
	Aok aok;

	/** last path and filename */
	String lastpath = null, lastfile = null;

	public ConfigFile(Aok aok) {
		this.aok = aok;
		aok.cf = this;
	}

	public boolean read(String filename) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			System.out.printf("Error: file '%s' not found\n", filename);
			return false;
		}
		{
			String defline = null, valline = null;
			int nr = 0;
			do {
				try {
					defline = in.readLine();
					valline = in.readLine(); // read one
				} catch (IOException e) {
					System.out.println("Error: io error on read");
				} // read one
				if (defline != null && valline != null) { // if defined set the
					// table value
					valline = valline.trim();
					try {
						if (!valline.isEmpty()) {
							aok.act.setConfigAt(aok.convertConfigToView(nr), new Integer(valline));
						}
					} catch (NumberFormatException e) {
						System.out.println("Error: not a number");
					}
					nr++;
				}
			} while (valline != null && nr <= aok.CONFIGMAX);
			try {
				in.close();
				aok.configloaded = true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}

	public boolean write(String filename) {
		return write(filename,false);
	}
	
	public boolean write(String filename, boolean onlySelected) {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(filename));
		} catch (IOException e) {
			System.out.printf("Error: file '%s' could not be created\n",
					filename);
			return false;
		}
		{
			for (int nr = 0; nr < aok.configcount; nr++) {
				String valStr = "";
				if (!onlySelected || aok.act.isConfigSelected(aok.convertConfigToView(nr))) {
					valStr = String.format("%d",aok.getAokConfig(nr));
				}
				try {
					
					out.write(String.format("(%03d) %s\r\n%s\r\n", nr, aok
							.getAokConfigName(nr), valStr));
				} catch (IOException e) {
					System.out.println("Error: io error during write.");
				}
			}
			try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}

}
