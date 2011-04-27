package de.schrell.aok;

import java.io.IOException;

public class AutoMagnetics implements Runnable {

	private final Aok aok;
	private final int gasToCheck;

	public AutoMagnetics(final Aok aok, final int gasToCheck) {
		this.aok = aok;
		this.gasToCheck = gasToCheck;
	}

	public void run() {
		if (confirmAokIsFixed()) {
			System.out.println("\nYou confirmed that your AOK is fixed and the test can use high gas values.");
			try {
				aok.getDebugReader().setInfo(false);
				writeSingleConfig(Aok.CONFIG_MAG_X_GAS_SLOPE, 0);
				writeSingleConfig(Aok.CONFIG_MAG_Y_GAS_SLOPE, 0);
				writeSingleConfig(Aok.CONFIG_MAG_Z_GAS_SLOPE, 0);
				System.out.println("\nSlope values initialized to zero.");
				aok.act.clearSelection();
				aok.aco.readConfigFromAokSync();
				aok.aco.setInfo(false);
				int minGas = aok.getAokConfig(Aok.CONFIG_MIN_GAS);
				int[] oldDebugs = getOldDebugValues();
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
				System.out.printf("\nmean heading     = %d\n", headingMotorsOff);
				System.out.printf("mean X           = %d\n", rawXMotorsOff);
				System.out.printf("mean Y           = %d\n", rawYMotorsOff);
				System.out.printf("mean Z           = %d\n", rawZMotorsOff);
				// starting up motors
				System.out.println("\nstarting motors...");
				aok.motorTest.start();
				for (int motVal = minGas; motVal <= gasToCheck; motVal++) {
					aok.motorTest.setMotor(-1, motVal);
					Thread.sleep(100);
				}
				System.out.println("\nmotors now running at test values.");

				System.out.println("\noptimizing x-axis...");
				int slopeX = optimize(rawXMotorsOff, Aok.STATUS_MAG_X_RAW, gasToCheck);
				System.out.println("Slope-X=" + slopeX);
				writeSingleConfig(Aok.CONFIG_MAG_X_GAS_SLOPE, slopeX);

				System.out.println("\noptimizing y-axis...");				
				int slopeY = optimize(rawYMotorsOff, Aok.STATUS_MAG_Y_RAW, gasToCheck);
				System.out.println("Slope-Y=" + slopeY);
				writeSingleConfig(Aok.CONFIG_MAG_Y_GAS_SLOPE, slopeY);

				System.out.println("\noptimizing z-axis...");
				int slopeZ = optimize(rawZMotorsOff, Aok.STATUS_MAG_Z_RAW, gasToCheck);
				System.out.println("Slope-Z=" + slopeZ);
				writeSingleConfig(Aok.CONFIG_MAG_Z_GAS_SLOPE, slopeZ);

				System.out.println("\nstopping motors...");
				for (int motVal = gasToCheck; motVal >= minGas; motVal--) {
					aok.motorTest.setMotor(-1, motVal);
					Thread.sleep(100);
				}
				aok.motorTest.stop();
				System.out.println("\nmotors stopped.");

				System.out.println("\nrestore old debug values...");
				restoreDebugValues(oldDebugs);
				aok.aco.setInfo(true);
				aok.getDebugReader().setInfo(true);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("\ndone.");
		}
	}

	private int optimize(int rawTarget, int statusNumber, int gas)
		throws InterruptedException {
		MeanStatus ms = new MeanStatus(aok, statusNumber, 500);
		int mean = ms.getMean();
		return (mean - rawTarget)*10000/gas;
	}

	private boolean confirmAokIsFixed() {
		MyDialog dialog = new MyDialog("AOK fixed?","Please confirm that you AOK is fixed.|The following procedure will test with high gas values.|Beware!","Abort|OK",true);
		return dialog.show()==1;
	}

	private void restoreDebugValues(int[] oldDebugs) {
		for (int i = 0; i < oldDebugs.length; i++) {
			writeSingleConfig(Aok.CONFIG_DEBUG1 + i, oldDebugs[i]);
		}
	}

	private void writeSingleConfig(int configNumber, int value) {
		try {
			aok.setAokConfig(configNumber, value);
			aok.aco.writeSingleConfigToAok(configNumber);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int[] getOldDebugValues() {
		final int[] old = new int[5];
		for (int i = 0; i < 5; i++) {
			old[i] = aok.getAokConfig(Aok.CONFIG_DEBUG1 + i);
		}
		return old;
	}

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
