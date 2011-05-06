package de.schrell.aok;

public class StatusListener {
	
	private final Aok aok;
	private final int count;
	private final int statusNumber;
	
	private volatile int counter = 0;
	private int meanStart = 0;
	private int diffSum = 0;

	public StatusListener(Aok aok, int count, int statusNumber) {
		this.aok = aok;
		this.count = count;
		this.statusNumber = statusNumber;
		aok.getDebugReader().addStatusListener(statusNumber, this);
	}

	public final synchronized void valueChanged(final int value) {
		if (counter==0) 
			meanStart=value;
		if (counter > count)
			return;
		counter++;
		diffSum += value-meanStart;
		if (counter == count) {
			notify();
		}
	}
	
	public final synchronized int getMean() throws InterruptedException {
		while (counter < count) {
			wait();
		}
		aok.getDebugReader().removeStatusListener(statusNumber, this);
		return meanStart+diffSum/counter;
	}
	
}
