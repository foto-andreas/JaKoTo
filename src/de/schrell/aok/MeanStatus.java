package de.schrell.aok;

public class MeanStatus {

	private final Aok aok;
	private final int statusNumber;
	private final int count;
	private StatusListener statusListener = null;
	
	public MeanStatus(final Aok aok, final int statusNumber, final int count) {
		this.aok = aok;
		this.statusNumber = statusNumber;
		this.count = count; 
		restart();
	}
	
	public void restart() {
		this.statusListener = new StatusListener(aok, count, statusNumber);
	}
	
	public int getMean() throws InterruptedException {
		if (this.statusListener==null)
			restart();
		int erg = this.statusListener.getMean();
		statusListener = null;
		return erg;
	}

}
