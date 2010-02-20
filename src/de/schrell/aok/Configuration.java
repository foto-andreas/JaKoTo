package de.schrell.aok;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

public class Configuration {

    Aok aok = null;
    String configPath, fileName, fullName, winFileName, winFullName;
    Rectangle winrect = null;
    String port = "";
    String speed = "";
    String aokpath = ".";
    String logpath = ".";
    String laf = new String(UIManager.getSystemLookAndFeelClassName());

    /**
     * @param aok
     */
    public Configuration(Aok aok) {
        this.aok = aok;
        String bs = System.getProperty("os.name");
        System.out.printf("Betriebssystem: %s [%d]\n", bs, bs.length());
        fileName = "jakoto.config";
        if (bs.length() >= 5 && bs.substring(0, 5).equals("Linux")) {
            configPath = System.getenv("HOME");
            fileName = ".jakotorc";
            winFileName = ".jakotowl";
        } else if (bs.length() >= 7 && bs.substring(0, 7).equals("Windows")) {
            fileName = "jakoto.ini";
            winFileName = "jakotowl.ini";
            configPath = System.getenv("USERPROFILE");
            if (configPath == null) {
                configPath = System.getenv("HOMEDRIVE")
                        + System.getenv("HOMEPATH");
            }
        }
        fullName = configPath + File.separator + fileName;
        winFullName = configPath + File.separator + winFileName;
        System.out.printf("Config: %s\n", fullName);
        System.out.printf("Window Layout: %s\n", winFullName);
        try {
            RandomAccessFile f = new RandomAccessFile(fullName, "r");
            f.close();
            read();
        } catch (FileNotFoundException e) {
            // nothing to do
        } catch (IOException e) {
            e.printStackTrace();
            // nothing to do
        }
    }

    public void read() {
        read(fullName);
    }

    /**
     * @param filename
     */
    public void read(String filename) {
        try {
            RandomAccessFile f = new RandomAccessFile(filename, "r");
            String line;
            while (null != (line = f.readLine())) {
                String[] splits = line.split("[$]");
                if (splits[0].equals("winrect")) {
                    winrect = new Rectangle(new Integer(splits[1]),
                            new Integer(splits[2]), new Integer(splits[3]),
                            new Integer(splits[4]));
                    if (aok.asw != null) {
                        // System.out.println(winrect.toString());
                        aok.asw.frame.setPreferredSize(new Dimension(new Integer(
                                splits[3]), new Integer(splits[4])));
                        aok.asw.frame.setBounds(winrect);
                    }
                }
                if (splits[0].equals("port")) {
                    port = splits[1];
                    if (port == null) {
                        port = "";
                    }
                    if (aok.asb != null) {
                        aok.asb.port.setText(port);
                    }
                }
                if (splits[0].equals("speed")) {
                    speed = splits[1];
                    if (aok.asb != null) {
                        aok.asb.speed.setText(speed);
                    }
                }
                if (splits[0].equals("aokpath")) {
                    aokpath = splits[1];
                    if (aok.cf != null) {
                        aok.cf.lastpath = aokpath;
                    }
                }
                if (splits[0].equals("logpath")) {
                    logpath = splits[1];
                    if (aok.logfile != null) {
                        aok.logfile.path = aokpath;
                    }
                }
                if (splits[0].equals("laf")) {
                    laf = splits[1];
                    if (aok.asw != null) {
                        aok.asw.setLookAndFeel(laf);
                        LookAndFeelInfo lafinfo[] = UIManager.getInstalledLookAndFeels();
                        String[] lafclasses = new String[lafinfo.length];
                        int sel = 0;
                        for (sel = 0; sel < lafinfo.length; sel++) {
                            lafclasses[sel] = lafinfo[sel].getClassName();
                            if (aok.asw.actlaf.equals(lafclasses[sel])) {
                                break;
                            }
                        }
                        if (aok.atp != null) {
                            aok.atp.laf.setSelectedIndex(sel);
                        }
                    }
                }
            }
            f.close();
        } catch (FileNotFoundException e) {
            // nothing to do
        } catch (IOException e) {
            e.printStackTrace();
            // TODO
        }

    }

    public void write() {
        write(fullName);
    }

    public void write(String filename) {
        try {
            String nl = System.getProperty("line.separator");
            RandomAccessFile f = new RandomAccessFile(filename, "rw");
            f.setLength(0);
            Rectangle wr = aok.asw.frame.getBounds();
            f.writeBytes(String.format("winrect$%d$%d$%d$%d$end%s", wr.x, wr.y,
                    wr.width, wr.height, nl));
            f.writeBytes(String.format("port$%s$end%s", aok.asb.port.getText(),
                    nl));
            f.writeBytes(String.format("speed$%s$end%s", aok.asb.speed.getText(), nl));
            if (aok.cf.lastpath != null) {
                f.writeBytes(String.format("aokpat$:%s$end%s", aok.cf.lastpath,
                        nl));
            }
            if (aok.logfile.path != null) {
                f.writeBytes(String.format("logpath$%s$end%s",
                        aok.logfile.path, nl));
            }
            f.writeBytes(String.format("laf$%s$end%s", aok.asw.actlaf, nl));
            f.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getWinConf() {
        return winFullName;
    }
}
