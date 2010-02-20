package de.schrell.aok;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class AokTabStatus extends JScrollPane {

	private static final long serialVersionUID = 6224261041204186214L;

	Aok aok;
	static JPanel box = new JPanel();

	AokTabStatus(Aok aok) {

		super(box);

		this.aok = aok;

		/** the GridBagLayout layout manager is used for the main window */
		GridBagLayout lc = new GridBagLayout();
		/** the constraints for the main window */
		GridBagConstraints c = new GridBagConstraints();
		// initialize the constraints for the main window

		box.setLayout(lc);
		getViewport().setMinimumSize(new Dimension(200, 200));
		getViewport().setPreferredSize(new Dimension(200, 200));

		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.BOTH;
		// set titles for the window parts
		c.insets = new Insets(10, 10, 5, 10);

		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1000;
		c.weighty = 1;
		AokStatusTable ast = new AokStatusTable(aok);
		JLabel astLabel = ast.getLabel();
		c.ipady = 3;
		c.insets = new Insets(10, 10, 0, 10);
		c.fill = GridBagConstraints.HORIZONTAL;
		lc.setConstraints(astLabel, c);
		box.add(astLabel);
		c.fill = GridBagConstraints.BOTH;
		c.gridy++;
		c.weighty = 100;
		c.insets = new Insets(5, 10, 10, 10);
		JScrollPane astPane = ast.getPane();
		lc.setConstraints(astPane, c);
		box.add(astPane);

		c.gridx++;
		c.gridy = 0;
		c.weightx = 1000;
		c.weighty = 1;
		AokConfigTable act = new AokConfigTable(aok);
		JLabel actLabel = act.getLabel();
		c.insets = new Insets(10, 10, 0, 10);
		c.fill = GridBagConstraints.HORIZONTAL;
		lc.setConstraints(actLabel, c);
		box.add(actLabel);
		c.fill = GridBagConstraints.BOTH;
		c.gridy++;
		c.weighty = 100;
		c.insets = new Insets(5, 10, 10, 10);
		JScrollPane actPane = act.getPane();
		lc.setConstraints(actPane, c);
		box.add(actPane);

		c.gridx++;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		AokStatusButtons asb = new AokStatusButtons(aok);
		JLabel asbLabel = asb.getLabel();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 10, 0, 10);
		lc.setConstraints(asbLabel, c);
		box.add(asbLabel);
		c.fill = GridBagConstraints.VERTICAL;
		c.gridy++;
		c.weighty = 100;
		c.insets = new Insets(5, 10, 10, 10);
		JScrollPane asbPane = asb.getPane();
		Dimension ps = asbPane.getPreferredSize();
		ps.width += 6;
		asbPane.setPreferredSize(ps);
		asbPane.setMinimumSize(ps);
		asbPane.setMaximumSize(ps);
		lc.setConstraints(asbPane, c);
		box.add(asbPane);
		asbPane.setMaximumSize(ps);
		asbPane.setMinimumSize(ps);

		if (System.getenv("AOK_NO_TLP") == null) {
			c.gridx++;
			c.gridy = 0;
			c.weightx = 2;
			c.weighty = 1;
			TrafficLightsPanel tlp = new TrafficLightsPanel(aok);
			JScrollPane tlpane = new JScrollPane(tlp);
			JLabel tlpLabel = new JLabel("State");
			tlpLabel.setFont(new Font("Default", Font.BOLD, 12));
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(10, 10, 0, 10);
			lc.setConstraints(tlpLabel, c);
			box.add(tlpLabel);
			c.fill = GridBagConstraints.BOTH;
			c.gridy++;
			c.weighty = 100;
			c.insets = new Insets(5, 10, 10, 10);
			lc.setConstraints(tlpane, c);
			tlp.setPreferredSize(new Dimension(50, 100));
			tlpane.setMinimumSize(new Dimension(60, 50));
			box.add(tlpane);
		}
		
		c.ipady = 0;
		c.weighty = 1.1;
		c.weightx = 1000;
		c.gridx = 0;
		c.gridwidth = 4;
		c.gridy = 3;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		ScrolledGraph sp1 = new ScrolledGraph(aok, 10000, 200, 200, 200);
		lc.setConstraints(sp1.getPanel(), c);
		// sp1.setVisible(false);
		box.add(sp1.getPanel());

	}

}
