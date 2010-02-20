package de.schrell.aok;

import java.awt.*;
import javax.swing.*;

public class AokTabGraph extends JScrollPane {

	private static final long serialVersionUID = 1984991275483209691L;
	
	static JPanel box = new JPanel();

	AokTabGraph(Aok aok) {
		super(box);

		/** the GridBagLayout layout manager is used for the main window */
		GridBagLayout lc = new GridBagLayout();
		/** the constraints for the main window */
		GridBagConstraints c = new GridBagConstraints();
		// initialize the constraints for the main window

		box.setLayout(lc);

		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		// set titles for the window parts
		c.insets = new Insets(10, 10, 10, 10);

		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 10;
		c.weighty = 1;
		ScrolledGraph sp0 = new ScrolledGraph(aok, 10000, 200, 200, 200);
		lc.setConstraints(sp0.getPanel(), c);
		box.add(sp0.getPanel());
		sp0.setVisible(true);

		c.gridy++;
		ScrolledGraph sp1 = new ScrolledGraph(aok, 10000, 200, 200, 200);
		lc.setConstraints(sp1.getPanel(), c);
		box.add(sp1.getPanel());
		sp1.setVisible(true);

		c.gridy++;
		ScrolledGraph sp2 = new ScrolledGraph(aok, 10000, 200, 200, 200);
		lc.setConstraints(sp2.getPanel(), c);
		box.add(sp2.getPanel());
		sp2.setVisible(true);

		c.gridy++;
		ScrolledGraph sp3 = new ScrolledGraph(aok, 10000, 200, 200, 200);
		lc.setConstraints(sp3.getPanel(), c);
		box.add(sp3.getPanel());
		sp3.setVisible(true);

		// add filler space
		c.gridy++;
		c.weighty=10;
		JLabel fil = new JLabel("");
		c.fill = GridBagConstraints.VERTICAL;
		lc.setConstraints(fil, c);		
		box.add(fil);

//		box.setPreferredSize(box.getPreferredSize());

	}

}
