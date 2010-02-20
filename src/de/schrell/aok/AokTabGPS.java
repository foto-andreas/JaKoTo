package de.schrell.aok;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

//-as import chrriis.dj.nativeswing.swtimpl.NativeInterface;
//-as import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;


public class AokTabGPS extends JPanel {

	private static final long serialVersionUID = 1109318873423249845L;

	final Aok aok;
 
	AokTabGPS(Aok aok) {
		super(new BorderLayout());
		this.aok = aok;
//-as		NativeInterface.open();
//-as?    	final JWebBrowser wb = new JWebBrowser(JWebBrowser.destroyOnFinalization());
	    SwingUtilities.invokeLater(new Runnable() {


	        public void run() {
//-as		    	System.out.println("1="+JWebBrowser.class);
//-as		    	System.out.println("2="+JWebBrowser.class.getPackage());
//-as		    	System.out.println("3="+JWebBrowser.class.getPackage().getName());
//	        	final JWebBrowser wb = new JWebBrowser();
//	        	wb.navigate("http://schrell.de/download/andreas/map.html");
	        	
//	        	add(wb,BorderLayout.CENTER);
	        }});
//-as	    NativeInterface.runEventPump();
	}
}
