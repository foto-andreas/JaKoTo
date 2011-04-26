package de.schrell.aok;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.en.us.CMULexicon;

public class SpeechOutput {

	Voice voice = null;

	public SpeechOutput() {
		voice = VoiceManager.getInstance().getVoice("mbrola_us2");
		if (voice != null) {
			voice.setLexicon(new CMULexicon());
			voice.allocate();
			voice.speak("Speech output was configured.");
		}
	}

	public void speak(String text) {
		if (voice == null)
			return;
		try {
			voice.speak(text);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void close() {
		if (voice == null)
			return;
		voice.deallocate();
		voice = null;
	}

	public static void listAllVoices() {
		System.out.println();
		System.out.println("System-Property mbrola.base="
				+ System.getProperty("mbrola.base"));
		System.out.println("All voices available:");
		VoiceManager voiceManager = VoiceManager.getInstance();
		Voice[] voices = voiceManager.getVoices();
		for (int i = 0; i < voices.length; i++) {
			System.out.println("    " + voices[i].getName() + " ("
					+ voices[i].getDomain() + " domain)");
		}
	}

}
