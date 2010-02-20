package de.schrell.aok;

import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class TrafficLight extends JPanel {

	private static final long serialVersionUID = -1371644912707691753L;

	Color[] colors;
	String[] text;
	String title;
	JLabel label;

	public TrafficLight(Aok aok, String title, Color[] colors, String[] text) {
		super();
		this.title = title;
		this.colors = colors;
		this.text = text;
		label = new JLabel();
		this.add(label);
		setState(0);
	}

	void setState(final int state) {
		final TrafficLight tl = this;
		EventQueue.invokeLater(new Runnable() { 
			public void run() {
				tl.setBackground(colors[state]);
				label.setText("<html>" + title + "<br>" + text[state] + "</html>");
				tl.repaint();
		}});
	}
}
