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
			System.out.println("You confirmed that your AOK is fixed and the test can use high gas values.");
			try {
				aok.getDebugReader().setInfo(false);
				writeSingleConfig(Aok.CONFIG_MAG_X_GAS_SLOPE, 0);
				writeSingleConfig(Aok.CONFIG_MAG_Y_GAS_SLOPE, 0);
				writeSingleConfig(Aok.CONFIG_MAG_Z_GAS_SLOPE, 0);
				System.out.println("\n0-Slopes written.");
				aok.act.clearSelection();
				aok.aco.readConfigFromAokSync();
				aok.aco.setInfo(false);
				int minGas = aok.getAokConfig(Aok.CONFIG_MIN_GAS);
				int[] oldDebugs = getOldDebugValues();
				for (int i : oldDebugs) {
					System.out.println("old Debug: " + i);
				}
				System.out.println("\nset special debug values...");
				setOurDebugValues();
				// read our target values
				System.out.println("\nreading target values...");
				MeanStatus h1 = new MeanStatus(aok, Aok.STATUS_MM3HEADING, 100);
				MeanStatus rX = new MeanStatus(aok, Aok.STATUS_MM3_X, 100);
				MeanStatus rY = new MeanStatus(aok, Aok.STATUS_MM3_Y, 100);
				MeanStatus rZ = new MeanStatus(aok, Aok.STATUS_MM3_Z, 100);
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
				System.out.println("\nstarting motors...");
				new MyDialog("Start Motors", "Please start the motors now", "ok", true).show();
//				aok.motorTest.start();
//				for (int motVal = minGas; motVal <= gasToCheck; motVal++) {
//					aok.motorTest.setMotor(-1, motVal);
//					Thread.sleep(100);
//				}
				System.out.println("\nmotors now running at test values.");
				h1.restart();
				rX.restart();
				rY.restart();
				rZ.restart();
				int headingMotorsOn = h1.getMean();
				int rawXMotorsOn = rX.getMean();
				int rawYMotorsOn = rY.getMean();
				int rawZMotorsOn = rZ.getMean();
				System.out.printf("\nmean heading on  = %d\n", headingMotorsOn);
				System.out.printf("mean X on        = %d\n", rawXMotorsOn);
				System.out.printf("mean Y on        = %d\n", rawYMotorsOn);
				System.out.printf("mean Z on        = %d\n", rawZMotorsOn);

				System.out.println("\noptimizing y-axis...");
				int slopeY = optimize(rawYMotorsOff, Aok.STATUS_MM3_Y,
						Aok.CONFIG_MAG_Y_GAS_SLOPE);
				int rawYMotorsOnAfterY = rY.getMean();
				int headingMotorsOnAfterY = h1.getMean();
				System.out.println("\nafter Y:");
				System.out.printf("mean heading     = %d\n",
						headingMotorsOnAfterY);
				System.out
						.printf("mean Y           = %d\n", rawYMotorsOnAfterY);

				System.out.println("\noptimizing x-axis...");
				int slopeX = optimize(rawXMotorsOff, Aok.STATUS_MM3_X,
						Aok.CONFIG_MAG_X_GAS_SLOPE);
				int rawXMotorsOnAfterX = rX.getMean();
				int headingMotorsOnAfterX = h1.getMean();
				System.out.println("\nafter X:");
				System.out.printf("mean heading     = %d\n",
						headingMotorsOnAfterX);
				System.out
						.printf("mean X           = %d\n", rawXMotorsOnAfterX);

				System.out.println("\noptimizing z-axis...");
				int slopeZ = optimize(rawZMotorsOff, Aok.STATUS_MM3_Z,
						Aok.CONFIG_MAG_Z_GAS_SLOPE);
				int rawZMotorsOnAfterZ = rZ.getMean();
				int headingMotorsOnAfterZ = h1.getMean();
				System.out.println("\nafter Z:");
				System.out.printf("mean heading     = %d\n",
						headingMotorsOnAfterZ);
				System.out
						.printf("mean Z           = %d\n", rawZMotorsOnAfterZ);

				new MyDialog("Stop Motors", "Please stop the motors now", "ok", true).show();
//				System.out.println("\nstopping motors...");
//				for (int motVal = gasToCheck; motVal >= minGas; motVal--) {
//					aok.motorTest.setMotor(-1, motVal);
//					Thread.sleep(100);
//				}
//				aok.motorTest.stop();
				System.out.println("\nmotors stopped...");
				Thread.sleep(1000);
				restoreDebugValues(oldDebugs);
				System.out.println("\ndebug values restored...");
				aok.aco.setInfo(true);
				aok.getDebugReader().setInfo(true);
				System.out.println("Slope-X=" + slopeX);
				System.out.println("Slope-Y=" + slopeY);
				System.out.println("Slope-Z=" + slopeZ);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("\ndone.");
		}
	}

	private int getDiff(int rawTarget, MeanStatus ms) {
		ms.restart();
		try {
			int mean = ms.getMean();
			System.out.println("mean=" + mean);
			return Math.abs(rawTarget - mean);
		} catch (InterruptedException e) {
			return Integer.MAX_VALUE;
		}
	}

	private int optimize(int rawTarget, int statusNumber, int configNumber)
			throws InterruptedException {
		MeanStatus ms = new MeanStatus(aok, statusNumber, 100);
		int bestDiff;
		int best = 0;
		int[] steps = { 1000, 100, 10, 1 };

		int start = 0;
		for (int step : steps) {
			bestDiff = Integer.MAX_VALUE;
			int s = start;
			int diff;
			best = start;
			int MAX = start + 10 * step;
			do {
				writeSingleConfig(configNumber, s);
				diff = getDiff(rawTarget, ms);
				if (diff < bestDiff) {
					bestDiff = diff;
					best = s;
				}
				System.out.printf("checked value=%d diff=%d best=%d.\n", s,
						diff, best);
				s += step;
			} while (s <= MAX && bestDiff > 0);
			s = start - step;
			int MIN = start - 10 * step;
			do {
				writeSingleConfig(configNumber, s);
				diff = getDiff(rawTarget, ms);
				if (diff < bestDiff) {
					bestDiff = diff;
					best = s;
				}
				System.out.printf("checked value=%d diff=%d best=%d.\n", s,
						diff, best);
				s -= step;
			} while (s >= MIN && bestDiff > 0);
			System.out.printf("\nrawTarget=%d bestDiff=%d best=%d\n",
					rawTarget, bestDiff, best);
			start = best;
		}
		return best;
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
