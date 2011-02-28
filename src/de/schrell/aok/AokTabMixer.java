package de.schrell.aok;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;

public class AokTabMixer extends JPanel {

	private static final long serialVersionUID = -7759288790707801433L;
	static int motors = 12;
    static int perLine = 4;
    Aok aok = null;
    JSpinner[] spinner = new JSpinner[motors * perLine];
    JLabel dummy0 = new JLabel();
    JLabel check1 = new JLabel("?");
    JLabel check2 = new JLabel("?");
    JLabel check3 = new JLabel("?");
    static GridLayout gl = new GridLayout(motors + 3, perLine);
    JButton readButton = new JButton("read");
    JButton saveButton = new JButton("write");
    JButton applyButton = new JButton("apply");
    JButton resetButton = new JButton("reset");

    /**
     * @param aok
     */
    public AokTabMixer(Aok aok) {
        super(gl);
        this.aok = aok;
        aok.atm = this;
        add(new JLabel("gas"));
        add(new JLabel("nick"));
        add(new JLabel("roll"));
        add(new JLabel("yaw"));
        for (int i = 0; i < motors; i++) {
            for (int j = 0; j < perLine; j++) {
                spinner[i * perLine + j] =
                    new JSpinner(new SpinnerNumberModel(0, -127, 127, 1));
                add(spinner[i * perLine + j]);
            }
        }
        add(dummy0);
        add(check1);
        add(check2);
        add(check3);
        add(readButton);
        readButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String path = "";
                JFileChooser chooser = new JFileChooser(path);
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "Arm-o-Kopter Mixer Settings", "mix", "mixer");
                chooser.setFileFilter(filter);
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                // chooser.setDialogType(JFileChooser.OPEN_DIALOG);
                chooser.setApproveButtonText("Read");
                int returnVal = chooser.showOpenDialog(readButton);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    path = chooser.getSelectedFile().getAbsolutePath();
                    readMixer(path);
                }
            }
        });

        add(saveButton);
        saveButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String path = "";
                JFileChooser chooser = new JFileChooser(path);
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "Arm-o-Kopter Mixer Settings", "mix");
                chooser.setFileFilter(filter);
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                // chooser.setDialogType(JFileChooser.OPEN_DIALOG);
                chooser.setApproveButtonText("Write");
                int returnVal = chooser.showOpenDialog(saveButton);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    path = chooser.getSelectedFile().getAbsolutePath();
                    if (!path.toLowerCase().endsWith(".mix")) {
                        path += ".mix";
                    }
                    writeMixer(path);
                }
            }
        });
        add(applyButton);
        applyButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < motors; i++) {
                    int gas = (Integer) (spinner[i * perLine + 0].getValue());
                    int nick = (Integer) (spinner[i * perLine + 1].getValue());
                    int roll = (Integer) (spinner[i * perLine + 2].getValue());
                    int yaw = (Integer) (spinner[i * perLine + 3].getValue());
                    int config = (gas & 0x000000FF)
                        | ((nick & 0x000000FF) << 8)
                        | ((roll & 0x000000FF) << 16)
                        | ((yaw & 0x000000FF) << 24);
                    setConfig(i, config);
                }
            }
        });
        add(resetButton);
        resetButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < motors; i++) {
                    for (int j = 0; j < perLine; j++) {
                        spinner[i * perLine + j].setValue(0);
                    }
                }
            }
        });
    }

    private void writeMixer(String filename) {
        try {
            BufferedWriter stream = new BufferedWriter(new FileWriter(filename));
            stream.write("[MIXER]\r\n");
            for (int i = 0; i < motors; i++) {
                for (int j = 0; j < perLine; j++) {
                    int v = (Integer) (spinner[i * perLine + j].getModel().getValue());
                    stream.write("MOT" + (i + 1) + "-" + (j + 1) + "=" + v + "\r\n");
                }
            }
            stream.close();
        } catch (IOException ex) {
            Logger.getLogger(AokTabMixer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void readMixer(String filename) {
        try {
            BufferedReader stream = new BufferedReader(new FileReader(filename));
            String line = stream.readLine();
            if (line.matches("\\[MIXER\\]")) {
                while (null != (line = stream.readLine())) {
                    line = line.substring(3).replaceFirst("-", "=");
                    String[] parts = line.split("=");
                    if (parts.length != 3) {
                        System.out.println("ERR PARTS="
                            + parts[0] + "/" + parts[1] + "/" + parts[2]
                            + " (" + parts.length + ")");
                    } else {
                        System.out.println("PARTS="
                            + parts[0] + "/" + parts[1] + "/" + parts[2]
                            + " (" + parts.length + ")");
                        int mot = new Integer(parts[0]);
                        int col = new Integer(parts[1]);
                        Integer val = new Integer(parts[2]);
                        spinner[(mot - 1) * perLine + (col - 1)].setValue(val);
                    }
                }
            } else {
                System.out.print("ERR:" + line);
                /* TODO */
            }
            stream.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(AokTabMixer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AokTabMixer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void mixerSettingChanged(int motor, int val) {
//        System.out.println("mixerSettingChanged Motor=" + motor);
        int gas = (byte) (val & 0x000000FF);
        int nick = (byte) ((val & 0x0000FF00) >> 8);
        int roll = (byte) ((val & 0x00FF0000) >> 16);
        int yaw = (byte) ((val & 0xFF000000) >> 24);
//        System.out.println("MOT=" + motor + " VAL=" + String.format("%08x", val) + " GAS=" + gas + " NICK=" + nick + " ROLL=" + roll + " YAW=" + yaw);
        spinner[motor * perLine + 0].setValue(gas);
        spinner[motor * perLine + 1].setValue(nick);
        spinner[motor * perLine + 2].setValue(roll);
        spinner[motor * perLine + 3].setValue(yaw);
        checker();
    }

    public void checker() {
        Integer nick = 0, roll = 0, yaw = 0;
        for (int i = 0; i < motors; i++) {
            nick += (Integer) (spinner[i * perLine + 1].getModel().getValue());
            roll += (Integer) (spinner[i * perLine + 2].getModel().getValue());
            yaw += (Integer) (spinner[i * perLine + 3].getModel().getValue());
        }
        // System.out.println("Checker " + nick + " " + roll + " " + yaw);
        check1.setText("SumNick=" + nick.toString());
        check2.setText("SumRoll=" + roll.toString());
        check3.setText("SumYaw=" + yaw.toString());
        check1.setForeground((nick == 0) ? Color.GREEN : Color.RED);
        check2.setForeground((roll == 0) ? Color.GREEN : Color.RED);
        check3.setForeground((yaw == 0) ? Color.GREEN : Color.RED);
    }

    private void setConfig(int motor, int value) {
//        aok.setAokConfig(Aok.CONFIG_MIXER1 + motor, value);
        aok.act.setConfigAt(aok.convertConfigToView(Aok.CONFIG_MIXER1 + motor),
            value);
    }
}
