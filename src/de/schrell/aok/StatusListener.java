package de.schrell.aok;

public abstract class StatusListener {
	
	protected final Aok aok;
	protected final int count;
	protected final int statusNumber;
	
	public StatusListener(Aok aok, int count, int statusNumber) {
		this.aok = aok;
		this.count = count;
		this.statusNumber = statusNumber;
	}

	public void valueChanged(int value){
	}

	public int getMean() throws InterruptedException {
		return 0;
	}
	
}
