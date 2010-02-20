package de.schrell.aok;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;

import javax.swing.JPanel;

class Graph extends JPanel {

	private static final long serialVersionUID = -5101816267037232275L;

	int width, height, owidth, oheight;
	int partitionsize = 1000;

	int aktmaxx = 0, aktmax1 = 0, aktmax2 = 0, aktmax3 = 0;
	int[] xarr, y1, y2, y3;
	int l1 = 0, l2 = 0, l3 = 0;
	int p1min, p2min, p3min, p1max, p2max, p3max;
	int list1 = -1, list2 = -1, list3 = -1;
	int aktx;
	long s1, s2, s3;
	int lastval[] = { 0, 0, 0 };

	boolean active = false, visible = true;

	static public int[] append(int[] arr, int len, int addlen) {
		int[] narr = new int[len + addlen];
		for (int i = 0; i < len; i++)
			narr[i] = arr[i];
		for (int i = len; i < len + addlen; i++)
			narr[i] = i;
		return narr;
	}

	public Graph(int width, int height) {
		super();
		this.width = width;
		owidth = width;
		this.height = height;
		oheight = height;
		setSize(width, height);
		setOpaque(false);
		p1max = Integer.MIN_VALUE;
		p2max = Integer.MIN_VALUE;
		p3max = Integer.MIN_VALUE;
		p1min = Integer.MAX_VALUE;
		p2min = Integer.MAX_VALUE;
		p3min = Integer.MAX_VALUE;
		s1 = s2 = s3 = 0;
	}

	public String getInterval(int gnr) {
		if (!visible)
			return "";
		switch (gnr) {
		case 0:
			if (p1min == Integer.MAX_VALUE)
				return "[]";
			return String.format("%d [%d,%d] [%.1f/%.1f/%d]", lastval[gnr],
					p1min, p1max, (p1max + p1min) / 2.0, (double)s1/l1, p1max - p1min);
		case 1:
			if (p2min == Integer.MAX_VALUE)
				return "[]";
			return String.format("%d [%d,%d] [%.1f/%.1f/%d]", lastval[gnr],
					p2min, p2max, (p2max + p2min) / 2.0, (double)s2/l2, p2max - p2min);
		case 2:
			if (p3min == Integer.MAX_VALUE)
				return "[]";
			return String.format("%d [%d,%d] [%.1f/%.1f/%d]", lastval[gnr],
					p3min, p3max, (p3max + p3min) / 2.0, (double)s3/l3, p3max - p3min);
		}
		return "";
	}

	public void addPoint(int graphnr, int x, int y) {
		// if (true) return;
		if (!active)
			return;
		// System.out.printf("%d (%d,%d)\n", graphnr, x, y);
		lastval[graphnr] = y;
		switch (graphnr) {
		case 0:
			if (l1 >= aktmax1) {
				y1 = append(y1, aktmax1, partitionsize);
				aktmax1 += partitionsize;
			}
			if (l1 >= aktmaxx) {
				xarr = append(xarr, aktmaxx, partitionsize);
				aktmaxx += partitionsize;
			}
			y1[l1++] = y;
			if (y < p1min)
				p1min = y;
			if (y > p1max)
				p1max = y;
			s1 += y;
			break;
		case 1:
			if (l2 >= aktmax2) {
				y2 = append(y2, aktmax2, partitionsize);
				aktmax2 += partitionsize;
			}
			if (l2 >= aktmaxx) {
				xarr = append(xarr, aktmaxx, partitionsize);
				aktmaxx += partitionsize;
			}
			y2[l2++] = y;
			if (y < p2min)
				p2min = y;
			if (y > p2max)
				p2max = y;
			s2+=y;
			break;
		case 2:
			if (l3 >= aktmax3) {
				y3 = append(y3, aktmax3, partitionsize);
				aktmax3 += partitionsize;
			}
			if (l3 >= aktmaxx) {
				xarr = append(xarr, aktmaxx, partitionsize);
				aktmaxx += partitionsize;
			}
			y3[l3++] = y;
			if (y < p3min)
				p3min = y;
			if (y > p3max)
				p3max = y;
			s3+=y;
			break;
		}
		if (x > width) {
			width = x + 1000;
			this.setSize(width, height);
		}
		// as repaint();
		this.scrollRectToVisible(new Rectangle(x, y, 10, 10));
		// System.out.printf("Graph.addPoint: (%d,%d)\n", x, y);
	}

	public Dimension getMimimumSize() {
		return new Dimension(width, height);
	}

	@Override
	public Dimension getMaximumSize() {
		return new Dimension(width, height);
	}

	@Override
	public Dimension getSize() {
		return new Dimension(width, height);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}

	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean getVisible() {
		return visible;
	}

	@Override
	protected void paintComponent(final Graphics g) {
		if (!visible)
			return;
		Graphics2D g2 = (Graphics2D) g;
		Line2D line;
		int pmax, pmin;
		pmax = Math.max(p1max, Math.max(p2max, p3max));
		pmin = Math.min(p1min, Math.min(p2min, p3min));
		if (pmax==pmin) {
			pmax += 0.5;
			pmin -= 0.5;
		}
//		if (Math.abs(pmax) < 0.05 * Math.abs(pmin))
//			pmax = (int) (Math.signum(pmax) * 0.05 * Math.abs(pmin) + 0.5);
//		if (Math.abs(pmin) < 0.05 * Math.abs(pmax))
//			pmin = (int) (Math.signum(pmin) * 0.05 * Math.abs(pmax) - 0.5);
		double scale = -(double) (height - 8) / (pmax - pmin);
		int mid = height - 4 - (int) Math.round(pmin * scale);
		line = new Line2D.Double(0, mid, width, mid);
		g2.setColor(Color.black);
		g2.draw(line);
		for (int x = 0; x < width; x += 10) {
			if (x % 50 == 0) {
				g2.setColor(Color.yellow);
				line = new Line2D.Double(x, 0, x, height);
				g2.draw(line);
			}
			g2.setColor(Color.black);
			if (x % 10 == 0) {
				line = new Line2D.Double(x, mid - 4, x, mid + 4);
				g2.draw(line);
				if (x % 100 == 0) {
					line = new Line2D.Double(x, mid - 8, x, mid + 8);
					g2.draw(line);
				}
			}
		}
		AffineTransform tr = new AffineTransform();
		tr.setToIdentity();
		tr.translate(0, mid);
		tr.scale(1, scale);
		g2.transform(tr);
		g2.setStroke(new BasicStroke(0.001f));
		if (l1 > 0) {
			g2.setColor(Color.red);
			g2.drawPolyline(xarr, y1, l1);
		}
		if (l2 > 0) {
			g2.setColor(Color.green);
			g2.drawPolyline(xarr, y2, l2);
		}
		if (l3 > 0) {
			g2.setColor(Color.blue);
			g2.drawPolyline(xarr, y3, l3);
		}
		try {
			tr.invert();
		} catch (NoninvertibleTransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		g2.transform(tr);
	}

	void setList(int list, int nr) {
		switch (list) {
		case 0:
			list1 = nr - 1;
			break;
		case 1:
			list2 = nr - 1;
			break;
		case 2:
			list3 = nr - 1;
			break;
		}
	}

	public class GraphTest implements Runnable {

		int y = 0;
		int gnum;

		GraphTest(int gnum) {
			this.gnum = gnum;
		}

		public void run() {
			y = 0;
			int ly = 0;
			while (true) {
				if (active) {
					y = ly + (int) ((Math.random() - 0.5) * 10);
					ly = y;
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	public void valueUpdated(int nr, int value) {
		if (active) {
			// if (nr == 0)
			// aktx++;
			if (list1 == nr) {
				addPoint(0, l1, value);
				repaint();
			}
			if (list2 == nr) {
				addPoint(1, l2, value);
				repaint();
			}
			if (list3 == nr) {
				addPoint(2, l3, value);
				repaint();
			}
		}
	}

	public boolean getActive() {
		return active;
	}

	public void setActive(boolean x) {
		active = x;
	}

	public void reset() {
		width = owidth;
		height = oheight;
		setSize(width, height);
		setOpaque(false);
		l1 = l2 = l3 = 0;
		p1max = Integer.MIN_VALUE;
		p2max = Integer.MIN_VALUE;
		p3max = Integer.MIN_VALUE;
		p1min = Integer.MAX_VALUE;
		p2min = Integer.MAX_VALUE;
		p3min = Integer.MAX_VALUE;
		aktx = 0;
		s1 = s2 = s3 = 0;
		repaint();
	}
}
