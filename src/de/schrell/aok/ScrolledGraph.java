package de.schrell.aok;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class ScrolledGraph extends Component {
	/**
	 * EventQueue
	 */
	private static final long serialVersionUID = 1201646230519251806L;
	JPanel panel;
	Graph graph;
	JScrollPane spane;
	JCheckBox active, visible;
	JLabel l1, l2, l3;

	Aok aok = null;
	String statenames[];
	JComboBox gList1, gList2, gList3;

	boolean gvisible;

	int vpheight, vpwidth;

	ScrolledGraph(Aok aok, int width, int height, final int vpwidth,
			final int vpheight) {
		this.aok = aok;

		gvisible = true;
		this.vpwidth = vpwidth;
		this.vpheight = vpheight;
		statenames = new String[aok.getAokStateCount() + 1];
		statenames[0] = "";
		for (int i = 0; i < aok.getAokStateCount(); i++) {
			statenames[i + 1] = aok.getAokStateName(i);
		}

		graph = new Graph(width, height);
		spane = new JScrollPane(graph);
		panel = new JPanel();
		panel.setBorder(BorderFactory.createLineBorder(Color.black));
		/** the GridBagLayout layout manager is used for the main window */
		GridBagLayout lc = new GridBagLayout();
		/** the constraints for the main window */
		GridBagConstraints c = new GridBagConstraints();
		// initialize the constraints for the main window
		panel.setLayout(lc);
		spane.getViewport().setMinimumSize(new Dimension(vpwidth, vpheight));
		spane.getViewport().setPreferredSize(new Dimension(vpwidth, vpheight));
		c.insets = new Insets(5, 5, 5, 5);
		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 3;
		c.weighty = 1;
		c.gridwidth = 10;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		lc.setConstraints(spane, c);
		panel.add(spane);

		c.gridwidth = 1;

		// add a checkbox to enable and disable graph
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		active = new JCheckBox("active");
		active.setSelected(false);
		active.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				graph.setActive(!graph.getActive());
			}
		});
		lc.setConstraints(active, c);
		panel.add(active);

		c.ipady = 0;
		c.insets = new Insets(0, 10, 0, 0);
		c.weightx = 4;
		// Create the combo box, select item at index 4.
		// Indices start at 0, so 4 specifies the pig.
		JPanel box1 = new JPanel();
		box1.setSize(20, 20);
		box1.setBackground(Color.red);
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx++;
		lc.setConstraints(box1, c);
		panel.add(box1);
		gList1 = new JComboBox(statenames);
		gList1.setSelectedIndex(0);
		// add a button to reset the graph
		c.gridx++;
		c.weightx = 4;
		c.weighty = 1;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		gList1.setForeground(Color.red);
		gList1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				graph.setList(0, gList1.getSelectedIndex());
			}
		});
		lc.setConstraints(gList1, c);
		panel.add(gList1);

		// Create the combo box, select item at index 4.
		// Indices start at 0, so 4 specifies the pig.
		JPanel box2 = new JPanel();
		box2.setSize(20, 20);
		box2.setBackground(Color.green);
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx++;
		lc.setConstraints(box2, c);
		panel.add(box2);
		gList2 = new JComboBox(statenames);
		gList2.setSelectedIndex(0);
		// add a button to reset the graph
		c.gridx++;
		c.weightx = 4;
		c.weighty = 1;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		gList2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				graph.setList(1, gList2.getSelectedIndex());
			}
		});
		lc.setConstraints(gList2, c);
		panel.add(gList2);

		// Create the combo box, select item at index 4.
		// Indices start at 0, so 4 specifies the pig.
		JPanel box3 = new JPanel();
		box3.setSize(20, 20);
		box3.setBackground(Color.blue);
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx++;
		lc.setConstraints(box3, c);
		panel.add(box3);
		gList3 = new JComboBox(statenames);
		gList3.setSelectedIndex(0);
		// add a button to reset the graph
		c.gridx++;
		c.weightx = 4;
		c.weighty = 1;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		gList3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				graph.setList(2, gList3.getSelectedIndex());
			}
		});
		lc.setConstraints(gList3, c);
		panel.add(gList3);

		// add a button to reset the graph
		c.gridx++;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.NONE;
		JButton reset = new JButton("reset");
		reset.setSelected(false);
		reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				graph.setActive(false);
				graph.reset();
				active.setSelected(false);
			}
		});
		lc.setConstraints(reset, c);
		panel.add(reset);

		// add a checkbox to enable and disable graph
		c.gridx++;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.NONE;
		visible = new JCheckBox("visible");
		visible.setSelected(gvisible);
		graph.setVisible(gvisible);
		visible.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(!gvisible);
			}
		});
		lc.setConstraints(visible, c);
		panel.add(visible);

		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy++;
		c.gridx = 1;
		l1 = new JLabel("[]");
		lc.setConstraints(l1, c);
		panel.add(l1);
		c.gridx += 2;
		l2 = new JLabel("[]");
		lc.setConstraints(l2, c);
		panel.add(l2);
		c.gridx += 2;
		l3 = new JLabel("[]");
		lc.setConstraints(l3, c);
		panel.add(l3);

		aok.registerGraph(this);
	}

	@Override
	public void setVisible(boolean gvisible) {
		this.gvisible = gvisible;
		if (gvisible) {
			spane.getViewport()
					.setMinimumSize(new Dimension(vpwidth, vpheight));
			spane.getViewport()
			.setPreferredSize(new Dimension(vpwidth, vpheight));
			spane.revalidate();
		} else {
			spane.getViewport().setMinimumSize(new Dimension(vpwidth, 0));
			spane.getViewport().setPreferredSize(new Dimension(vpwidth, 0));
			spane.revalidate();
		}
		((JComponent) spane.getParent()).revalidate();
		visible.setSelected(gvisible);
		graph.setVisible(gvisible);
	}

	public JPanel getPanel() {
		return panel;
	}

	public Graph getGraph() {
		return graph;
	}

	public Graphics2D getGraphics2D() {
		return (Graphics2D) graph.getGraphics();
	}

	public void addPoint(int gr, int x, int y) {
		graph.addPoint(gr, x, y);
	}

	public void update() {
		l1.setText(graph.getInterval(0));// l1.repaint();
		l2.setText(graph.getInterval(1));// l2.repaint();
		l3.setText(graph.getInterval(2));// l3.repaint();
	}

}
