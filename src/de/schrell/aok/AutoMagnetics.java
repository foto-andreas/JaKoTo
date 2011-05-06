package de.schrell.aok;

import java.io.File;
import java.io.IOException;

public class AutoMagnetics implements Runnable {

	private enum GasStyle {
		NIX, RC, MOT;
	}

	private final Aok aok;
	private int gasToCheck;
	private GasStyle gasStyle = GasStyle.NIX;

	/**
	 * Constructor
	 * @param aok
	 * @param gasToCheck
	 */
	public AutoMagnetics(final Aok aok, final int gasToCheck) {
		this.aok = aok;
		if (gasToCheck == 0) {
			this.gasToCheck = aok.motorTest.getMotorValue(0);
		} else {
			this.gasToCheck = gasToCheck;
		}
	}

	/**
	 * Run the calibration procedure.
	 */
	public void run() {
		if (confirmAokIsFixed()) {
			System.out
					.println("\nYou confirmed that your AOK is fixed and the test can use high gas values.");
			try {
				int[] xyz = {0, 1, 2};

				aok.getDebugReader().setInfo(false);

				aok.act.clearSelection();
				aok.aco.readConfigFromAokSync();

				System.out.println("\nsaving config to temp file...");
				ConfigFile cf = new ConfigFile(aok);
				File tempfile = null;
				try {
					tempfile = File.createTempFile("aokconfig", "aok");
					cf.write(tempfile.getAbsolutePath());
				} catch (IOException e) {
					e.printStackTrace();
				}
				for (int i : xyz) {
					writeSingleConfig(Aok.CONFIG_MAG_X_GAS_SLOPE + i, 0);
				}
				for (int mix = 0; mix < 12; mix++) {
					writeSingleConfig(mix, 64);
				}
				System.out.println("\nSlope values initialized to zero.");
				aok.aco.setInfo(false);
				int minGas = aok.getAokConfig(Aok.CONFIG_MIN_GAS);
				System.out.println("\nsetting special debug values...");
				setOurDebugValues();
				System.out.println("\nreading target values...");
				MeanStatus h1 = new MeanStatus(aok, Aok.STATUS_MM3HEADING, 500);
				MeanStatus[] rM = new MeanStatus[xyz.length];
				for (int i : xyz) {
					rM[i] = new MeanStatus(aok, Aok.STATUS_MAG_X_RAW + i, 500);
				}
				int headingMotorsOff = h1.getMean();
				int[] rawMotorsOff = new int[xyz.length];
				for (int i : xyz) {
					rawMotorsOff[i] = rM[i].getMean();
				}
				System.out
						.printf("\nmean heading     = %d\n", headingMotorsOff);
				System.out.printf("mean X           = %d\n", rawMotorsOff[0]);
				System.out.printf("mean Y           = %d\n", rawMotorsOff[1]);
				System.out.printf("mean Z           = %d\n", rawMotorsOff[2]);
				// starting up motors
				switch (gasStyle) {
					case MOT:
						gasToCheck = aok.motorTest.getMotorValue(0);
						System.out.println("\nUsing motor value " + gasToCheck);
						aok.motorTest.setMotor(-1, 0);
						System.out.println("\nstarting motors...");
						aok.motorTest.start();
						for (int motVal = minGas; motVal <= gasToCheck; motVal++) {
							aok.motorTest.setMotor(-1, motVal);
							Thread.sleep(100);
						}
						System.out.println("\nmotors now running at test values.");
						break;
					case RC:
						MyDialog dialog = new MyDialog(
								"Start up Motors now...",
								"Please start up the motors now and press 'Done' after that.",
								"Done", true);
						dialog.show();
						break;
					default:
						return;
				}

				Thread.sleep(2000);
				
				System.out.println("\noptimizing...");
				int[] slopes = optimize(rawMotorsOff, rM, gasToCheck);
				
				System.out.println("Slope-X=" + slopes[0]);
				writeSingleConfig(Aok.CONFIG_MAG_X_GAS_SLOPE, slopes[0]);
				System.out.println("Slope-Y=" + slopes[1]);
				writeSingleConfig(Aok.CONFIG_MAG_Y_GAS_SLOPE, slopes[1]);
				System.out.println("Slope-Z=" + slopes[2]);
				writeSingleConfig(Aok.CONFIG_MAG_Z_GAS_SLOPE, slopes[2]);

				switch (gasStyle) {
					case MOT:
						System.out.println("\nstopping motors...");
						for (int motVal = gasToCheck; motVal >= minGas; motVal--) {
							aok.motorTest.setMotor(-1, motVal);
							Thread.sleep(100);
						}
						aok.motorTest.stop();
						System.out.println("\nmotors stopped.");
						break;
					case RC:
						MyDialog dialog = new MyDialog(
								"Stop Motors now...",
								"Please stop the motors now and press 'Done' after that.",
								"Done", true);
						dialog.show();
						break;
					default:
						return;
				}

				System.out.println("\nreloading config from temp file...");
				if (tempfile != null) {
					cf.read(tempfile.getAbsolutePath());
					tempfile.delete();
				}

				System.out.println("\nsetting new slope values...");
				for (int i : xyz) {
					aok.setAokConfig(Aok.CONFIG_MAG_X_GAS_SLOPE + i, slopes[i]);
					writeSingleConfig(Aok.CONFIG_MAG_X_GAS_SLOPE + i, slopes[i]);
				}
				
				System.out.println("\nwriting all config to AOK...");
				aok.aco.setInfo(true);
				aok.aco.writeConfigToAokSync();

				aok.getDebugReader().setInfo(true);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("\ndone.");
		}
	}

	/**
	 * Optimize the slope values by calculating them from the measured
	 * difference.
	 * 
	 * @param rawTarget
	 *            the target value for this status
	 * @param statusNumberField
	 *            the number of the status value to measure
	 * @param gas
	 *            the current gas value
	 * @return the optimized slope values
	 * @throws InterruptedException
	 */
	private int[] optimize(int[] rawTargets, MeanStatus[] means, int gas)
			throws InterruptedException {
		int[] xyz = {0, 1, 2};
		for (int i : xyz) {
			means[i].restart();
		}
		int meanValues[] = new int[xyz.length];
		for (int i : xyz) {
			int m = means[i].getMean();
			System.out.println("Slope["+i+"]="+m);
			meanValues[i] = (m - rawTargets[i]) * 10000 / gas;
		}
		return meanValues;
	}

	/**
	 * Dialog to get a confirmation from the user that the aok is fixed to the
	 * ground.
	 * 
	 * @return
	 */
	private boolean confirmAokIsFixed() {
		MyDialog dialog = new MyDialog(
				"AOK fixed?",
				"Please confirm that your AOK is fixed.|" 
				+ "The following procedure will test with high gas values.|"
				+ "To use the motor test, you should set the motor 1 value|"
				+ "in the motor test tool to the pitch value to check.|"
				+ "Beware that your AOK is fixed correctly!",
				"Abort|OK with RC|OK with Motortest", true);
		int rc = dialog.show();
		this.gasStyle = rc == 1 ? GasStyle.RC : GasStyle.MOT;
		System.out.println("RC="+rc);
		return rc > 0;
	}

	/**
	 * Write a single configuration value to the config table and to the aok
	 * ram.
	 * 
	 * @param configNumber
	 * @param value
	 */
	private void writeSingleConfig(int configNumber, int value) {
		try {
			aok.setAokConfig(configNumber, value);
			aok.aco.writeSingleConfigToAok(configNumber);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the actual debug values from the AOK to check.
	 * @return
	 */
	private int[] getOldDebugValues() {
		final int[] old = new int[5];
		for (int i = 0; i < 5; i++) {
			old[i] = aok.getAokConfig(Aok.CONFIG_DEBUG1 + i);
		}
		return old;
	}

	/**
	 * Set special debug values to get fast status values from the AOK. It sets the
	 * MAG, RC and MOT values.
	 */
	private void setOurDebugValues() {
		boolean retry = true;
		while (retry) {
			writeSingleConfig(Aok.CONFIG_DEBUG1, 16385);
			writeSingleConfig(Aok.CONFIG_DEBUG2, 28672);
			writeSingleConfig(Aok.CONFIG_DEBUG3, 0);
			writeSingleConfig(Aok.CONFIG_DEBUG4, 114688);
			writeSingleConfig(Aok.CONFIG_DEBUG5, 0);
			int[] now = getOldDebugValues();
			retry = false;
			if (now[0] != 16385)
				retry = true;
			if (now[1] != 28672)
				retry = true;
			if (now[2] != 0)
				retry = true;
			if (now[3] != 114688)
				retry = true;
			if (now[4] != 0)
				retry = true;
		}
	}

}
