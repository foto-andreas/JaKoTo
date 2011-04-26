package de.schrell.aok;

public class StatusListenerImpl extends StatusListener {

	public StatusListenerImpl(Aok aok, int count, int statusNumber) {
		super(aok, count, statusNumber);
	}

	private volatile int counter = 0;
	private int meanStart = 0;
	private int diffSum = 0;

	@Override
	public final void valueChanged(final int value) {
		if (counter==0) 
			meanStart=value;
		if (counter > count)
			return;
		counter++;
		diffSum += value-meanStart;
		if (counter == count) {
			synchronized (this) {
				notify();
			}
		}
//		System.out.printf("listener called num=%d counter=%d/%d value=%d.\n", statusNumber, counter, count, value);
	}
	
	public final int getMean() throws InterruptedException {
		synchronized (this) {
			while (counter != count) {
				wait();
			}
		}
		aok.getDebugReader().removeStatusListener(statusNumber, this);
		return meanStart+diffSum/counter;
	}

}
