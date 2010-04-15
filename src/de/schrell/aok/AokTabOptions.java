package de.schrell.aok;

import javax.swing.SwingUtilities;

public class AokTabOptions extends Options {

	private static final long serialVersionUID = 1109318873423249845L;

	AokTabOptions(Aok aok) {
		super(aok);
		aok.ato = this;
		
		setText(0,"OPTIONS_INV_ROLL");
		setText(1,"OPTIONS_INV_NICK");
		setText(2,"OPTIONS_INV_YAW");
		setText(3,"OPTIONS_X_MODE");
		setText(4,"OPTIONS_RX_PULLUP (r)");
		setText(5,"OPTIONS_AUX_LEDS");
		setText(6,"OPTIONS_MOTORS_OFF");
		setText(7,"unused MIX_6_RING");
		setText(8,"unused MIX_6_H (ni)");
		setText(9,"unused MIX_8_RING");
		setText(10,"unused MIX_8_H");
		setText(11,"unused MIX_10_RING (ni)");
		setText(12,"unused MIX_10_H");
		setText(13,"unused MIX_Y6");
		setText(14,"unused MIX_X8");
		setText(15,"OPTIONS_MOTORSPINUP");
		setText(16,"OPTIONS_SPEKTRUM (r)");
		setText(17,"OPTIONS_ACT (r)");
		setText(18,"OPTIONS_S3DSUM (r)");
		setText(19,"OPTIONS_ACC_ROTATED");
		setText(20,"OPTIONS_CH_RX_LOST");
		setText(21,"unused MIX_84_JOERG");
		setText(22,"OPTIONS_GPS_DEBUG_BEEP");
		setText(23,"OPTIONS_SETTINGSWITCH_ROLL_NICK");
		setText(24,"OPTIONS_HMC5843_NO_MM3");

	    SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	        	
	        }});
	 
	}
	
}
