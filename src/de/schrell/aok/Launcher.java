package de.schrell.aok;

import com.jdotsoft.jarloader.JarClassLoader;

public class Launcher {

	public static void main(String[] args) {
		JarClassLoader jcl = new JarClassLoader();
		try {
			jcl.invokeMain("de.schrell.aok.Aok", args);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	} // main()

} // class Launcher
