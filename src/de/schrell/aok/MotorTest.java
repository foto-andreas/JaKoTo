package de.schrell.aok;

import java.io.IOException;

public class MotorTest implements Runnable {

	private Aok aok = null;
	private int motorCount = 0;
	private byte[] motorValues = null;
	private boolean running = false;

	public MotorTest(Aok aok, int motorCount) {
		this.aok = aok;
		aok.motorTest = this;
		this.motorCount = motorCount;
		this.motorValues = new byte[motorCount];
		new Thread(this).start();
	}
	
	public synchronized void clear() {
		for (int i = 0; i < motorCount; i++) {
			motorValues[i] = 0;
		}
	}

	@Override
	public void run() {
		while (true) {
			if (running) {
				try {
					for (byte n = 0; n < motorCount; n++) {
						aok.aco.motorTest(n, motorValues[n]);
					}
					Thread.sleep(150);
				} catch (InterruptedException e) {
					break;
				} catch (IOException e) {
					break;
				}
			} else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public synchronized void setMotor(int motor, int value) {
		if (motor == -1) {
			for (int i=0; i<motorCount; i++) {
				setMotor(i, value);
			}
		} else {
			if (motorCount > motor) {
				motorValues[motor] = (byte) value;
			}
		}
	}
	
	public synchronized void start() {
		running = true;
	}
	
	public synchronized void stop() {
		running = false;
	}

	public boolean isRunning() {
		return running;
	}

}