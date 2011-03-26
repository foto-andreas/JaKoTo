package de.schrell.aok;

import javax.swing.SwingUtilities;

public class AokTabOptions extends Options {

	private static final long serialVersionUID = 1109318873423249845L;

	AokTabOptions(Aok aok) {
		super(aok);
		aok.ato = this;
		
		setText(0,"invert roll");
		setText(1,"invert nick");
		setText(2,"invert yaw");
		setText(3,"X-Mode (new front is left front)");
		setText(4,"Pullup on receiver pin (r)");
		setText(5,"LEDs on AUX1..4");
		setText(6,"Motors always off for debugging");
		setText(7,"Debug always on");
		setText(8,"Inflight parameter change");
		setText(9,"board version 4 (r)");
		setText(10,"turn aft to home before CH");
		setText(11,"Pitch low voltage warning");
		setText(12,"ACT S3D composite signal V2 (r)");
		setText(13,"SRF-08 (r)");
		setText(14,"Tricopter servo (instead of roll servo)");
		setText(15,"Spinup motors one by one");
		setText(16,"Spektrum receiver (r)");
		setText(17,"ACT DSL receiver (r)");
		setText(18,"ACT S3D composite signal (r)");
		setText(19,"ACC sensor rotated by 90°");
		setText(20,"CH when radio lost");
		setText(21,"use PID ramp");
		setText(22,"GPS debug beeping");
		setText(23,"Settingswitching with roll/nick");
		setText(24,"HMC5843 no MM3 (r)");
		setText(25,"Heading Hold mode");

	    SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	        	
	        }});
	 
	}
	
}
