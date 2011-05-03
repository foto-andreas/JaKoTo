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
				aok.getDebugReader().setInfo(false);

				System.out.println("\nsaving config to temp file...");
				ConfigFile cf = new ConfigFile(aok);
				File tempfile = null;
				try {
					tempfile = File.createTempFile("aokconfig", "aok");
					cf.write(tempfile.getAbsolutePath());
				} catch (IOException e) {
					e.printStackTrace();
				}
				writeSingleConfig(Aok.CONFIG_MAG_X_GAS_SLOPE, 0);
				writeSingleConfig(Aok.CONFIG_MAG_Y_GAS_SLOPE, 0);
				writeSingleConfig(Aok.CONFIG_MAG_Z_GAS_SLOPE, 0);
				for (int mix = 0; mix < 12; mix++) {
					writeSingleConfig(mix, 64);
				}
				System.out.println("\nSlope values initialized to zero.");
				aok.act.clearSelection();
				aok.aco.readConfigFromAokSync();
				aok.aco.setInfo(false);
				int minGas = aok.getAokConfig(Aok.CONFIG_MIN_GAS);
				System.out.println("\nsetting special debug values...");
				setOurDebugValues();
				System.out.println("\nreading target values...");
				MeanStatus h1 = new MeanStatus(aok, Aok.STATUS_MM3HEADING, 500);
				MeanStatus rX = new MeanStatus(aok, Aok.STATUS_MAG_X_RAW, 500);
				MeanStatus rY = new MeanStatus(aok, Aok.STATUS_MAG_Y_RAW, 500);
				MeanStatus rZ = new MeanStatus(aok, Aok.STATUS_MAG_Z_RAW, 500);
				int headingMotorsOff = h1.getMean();
				int rawXMotorsOff = rX.getMean();
				int rawYMotorsOff = rY.getMean();
				int rawZMotorsOff = rZ.getMean();
				System.out
						.printf("\nmean heading     = %d\n", headingMotorsOff);
				System.out.printf("mean X           = %d\n", rawXMotorsOff);
				System.out.printf("mean Y           = %d\n", rawYMotorsOff);
				System.out.printf("mean Z           = %d\n", rawZMotorsOff);
				// starting up motors
				if (gasStyle == GasStyle.MOT) {
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
				}
				if (gasStyle == GasStyle.RC) {
					MyDialog dialog = new MyDialog(
							"Start up Motors now...",
							"Please start up the motors now and press 'Done' after that.",
							"Done", true);
					dialog.show();
				}

				System.out.println("\noptimizing x-axis...");
				int slopeX = optimize(rawXMotorsOff, Aok.STATUS_MAG_X_RAW,
						gasToCheck);
				System.out.println("Slope-X=" + slopeX);
				writeSingleConfig(Aok.CONFIG_MAG_X_GAS_SLOPE, slopeX);

				System.out.println("\noptimizing y-axis...");
				int slopeY = optimize(rawYMotorsOff, Aok.STATUS_MAG_Y_RAW,
						gasToCheck);
				System.out.println("Slope-Y=" + slopeY);
				writeSingleConfig(Aok.CONFIG_MAG_Y_GAS_SLOPE, slopeY);

				System.out.println("\noptimizing z-axis...");
				int slopeZ = optimize(rawZMotorsOff, Aok.STATUS_MAG_Z_RAW,
						gasToCheck);
				System.out.println("Slope-Z=" + slopeZ);
				writeSingleConfig(Aok.CONFIG_MAG_Z_GAS_SLOPE, slopeZ);

				if (gasStyle == GasStyle.MOT) {
					System.out.println("\nstopping motors...");
					for (int motVal = gasToCheck; motVal >= minGas; motVal--) {
						aok.motorTest.setMotor(-1, motVal);
						Thread.sleep(100);
					}
					aok.motorTest.stop();
					System.out.println("\nmotors stopped.");
				}
				if (gasStyle == GasStyle.RC) {
					MyDialog dialog = new MyDialog(
							"Stop Motors now...",
							"Please stop the motors now and press 'Done' after that.",
							"Done", true);
					dialog.show();
				}

				System.out.println("\nreloading config from temp file...");
				if (tempfile != null) {
					cf.read(tempfile.getAbsolutePath());
					tempfile.delete();
				}

				System.out.println("\nsetting new slope values...");
				writeSingleConfig(Aok.CONFIG_MAG_X_GAS_SLOPE, slopeX);
				writeSingleConfig(Aok.CONFIG_MAG_Y_GAS_SLOPE, slopeY);
				writeSingleConfig(Aok.CONFIG_MAG_Z_GAS_SLOPE, slopeZ);

				aok.aco.setInfo(true);
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
	 * @param statusNumber
	 *            the number of the status value to measure
	 * @param gas
	 *            the current gas value
	 * @return the optimized slope value
	 * @throws InterruptedException
	 */
	private int optimize(int rawTarget, int statusNumber, int gas)
			throws InterruptedException {
		MeanStatus ms = new MeanStatus(aok, statusNumber, 500);
		int mean = ms.getMean();
		return (mean - rawTarget) * 10000 / gas;
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
				"Abort|OK with RC|OK with Motortest", false);
		int rc = dialog.show();
		this.gasStyle = rc == 1 ? GasStyle.RC : GasStyle.MOT;
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
			System.out.println("try...");
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
