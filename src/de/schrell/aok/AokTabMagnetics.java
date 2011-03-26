package de.schrell.aok;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class AokTabMagnetics extends JPanel {

		
	private static final long serialVersionUID = -2764779299089632522L;
	private int mag_min[] = { 1024, 1024, 1024 };
	private int mag_max[] = { -1024, -1024, -1024 };
	private int mag_akt[] = { 0, 0, 0 };
	private int mag_off[] = { 0, 0, 0 };
	private int mag_amp[] = { 0, 0, 0 };
    final Aok aok;
    static GridBagLayout gl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    

    JButton startButton = new JButton("Start");
    JLabel values[] = new JLabel[4*6];
    JPanel line[] = new JPanel[3];
    boolean online = false;

    private int min(int a, int b) {
    	return a<b ? a : b;
    }
    
    private int max(int a, int b) {
    	return a<b ? b : a;
    }
    
    public void updateRaw(int i, int val) {
    	if (online) {
    		mag_akt[i] = val;
			mag_min[i] = min(mag_min[i], mag_akt[i]);
			mag_max[i] = max(mag_max[i], mag_akt[i]);
			mag_amp[i] = mag_max[i] - mag_min[i];
			mag_off[i] = ( mag_max[i] + mag_min[i] ) / 2;
	    	updateInterface(i);
    	}
    }
    
    
    private void updateInterface(int i) {
		values[6*i+1].setText(Integer.toString(mag_min[i]));
		values[6*i+2].setText(Integer.toString(mag_akt[i]));
		values[6*i+3].setText(Integer.toString(mag_max[i]));
		values[6*i+4].setText(Integer.toString(mag_off[i]));
		values[6*i+5].setText(Integer.toString(mag_amp[i]));
    	Graphics g = line[i].getGraphics();
    	if (g != null) {
	    	double offset = 0.5 * (line[i].getWidth()-2);
	    	double scale = offset / 1024;
	    	g.setColor(Color.GRAY);
	    	g.fillRect(1, 1, line[i].getWidth()-2, line[i].getHeight()-2);
    		g.setColor(Color.WHITE);
	    	g.fillRect((int)(offset + scale * (double)mag_min[i]),	1, (int)(scale * (double)mag_amp[i]), line[i].getHeight()-2);
    		g.setColor(Color.RED);
    		g.drawLine((int)(offset + scale * (double)mag_akt[i]),	1, (int)(offset + scale * (double)mag_akt[i]), line[i].getHeight()-2);
    	}
    	
    }
    
    /**
     * @param aok
     */
    public AokTabMagnetics(final Aok aok) {
        super(gl);
        this.aok = aok;
        aok.atmag = this;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipadx=25;
        gbc.ipady=10;
        gbc.gridx=0; gbc.gridy=0;
        add(new JLabel(""),gbc); gbc.gridx++;
        add(new JLabel("Minimum"),gbc); gbc.gridx++;
        add(new JLabel("Aktuell"),gbc); gbc.gridx++;
        add(new JLabel("Maximum"),gbc); gbc.gridx++;
        add(new JLabel("Offset"),gbc); gbc.gridx++;
        add(new JLabel("Amplitude"),gbc); gbc.gridx++;
        for (int i=0; i<3; i++) {
        	gbc.gridx=0; gbc.gridy++;
        	for (int j=0; j<6; j++) {
        		JLabel l = new JLabel("0");
        		l.setPreferredSize(new Dimension(100,10));
        		values[6*i+j] = l;
        		add(l,gbc); gbc.gridx++;
        	}
        	line[i] = new JPanel();
        	line[i].setPreferredSize(new Dimension(514,10));
        	line[i].setBorder(new LineBorder(Color.BLACK,1));
        	line[i].setBackground(Color.WHITE);
        	gbc.gridx=0; gbc.gridy++;
        	gbc.gridwidth = 6;
        	add(line[i],gbc);
            gbc.gridwidth = 1;
        }
        values[6*0].setText("MM3-X");
        values[6*1].setText("MM3-Y");
        values[6*2].setText("MM3-Z");
        gbc.gridx=0; gbc.gridy++;
        add(startButton,gbc);
        startButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
            	online = ! online;
        		startButton.setSelected(online);
            	if (online) {
            		for (int i=0; i<3; i++) {
            			mag_akt[i] = 0;
            			mag_min[i] = 10000;
            			mag_max[i] = -10000;
            			mag_off[i] = 0;
            			mag_amp[i] = 0;
            		}
            		startButton.setText("Stop");
            	} else {
            		startButton.setText("Start");
            	}
            }
        });

        gbc.gridx++;
        JButton setButton = new JButton("Set");
        add(setButton,gbc);
        setButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
           		aok.act.setConfigAt(aok.convertConfigToView(46),mag_off[0]);
           		aok.act.setConfigAt(aok.convertConfigToView(47),mag_off[1]);
           		aok.act.setConfigAt(aok.convertConfigToView(48),mag_off[2]);
           		aok.act.setConfigAt(aok.convertConfigToView(139),mag_amp[0]);
           		aok.act.setConfigAt(aok.convertConfigToView(140),mag_amp[1]);
           		aok.act.setConfigAt(aok.convertConfigToView(141),mag_amp[2]);
            }
        });

    }

}
