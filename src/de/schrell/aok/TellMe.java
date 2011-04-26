package de.schrell.aok;

public class TellMe implements Runnable {

	Aok aok = null;
	SpeechOutput speechOutput = new SpeechOutput();

	public TellMe(Aok aok) {
		this.aok = aok;
	}

	@Override
	public void run() {

		SpeechOutput.listAllVoices();
		while (true) {
			if (aok.aco.connected) {
				double voltage = aok.getAokState(Aok.STATUS_BATT_VOLTAGE) / 10;
				speechOutput.speak("The voltage is " + voltage + "volts.");
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
			}
		}
	}

}
