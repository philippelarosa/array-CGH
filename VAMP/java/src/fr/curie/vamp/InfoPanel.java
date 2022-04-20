
/*
 *
 * InfoPanel.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.*;

class InfoPanel extends JSplitPane {

    public static final int INFO_PANEL = 1;
    public static final int MINIMAP_PANEL = 2;
    static final int MAX_COLUMNS = 18;

    private JScrollPane scrollPanel;
    private JScrollBar vScroll;
    private int vScrollValue = 0;
    private JPanel titlePanel, infoPanel;
    private MiniMapPanel miniMapPanel;
    private JLabel leftTitleLabel, topTitleLabel, bottomTitleLabel;
    private View view;
    private JTabbedPane tabbedPane;
    private GraphPanel last_graph_panel;
    private JPanel last_panel;
    private GraphElement last_set;
    private PropertyElement last_elem;
    //private DataElement last_elem;
    private Region last_region;
    private Mark last_mark;
    private DataElementRange last_range;
    private double last_vx;
    private boolean last_pinned_up;

    private JTextField topTitleTF;
    private JPanel updatePanel;

    InfoPanel(View _view) {
	this.view = _view;
	titlePanel = new JPanel(new GridBagLayout());
	infoPanel = new JPanel(null);
	scrollPanel = new JScrollPane
	    (infoPanel,
	     JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
	     JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	vScroll = scrollPanel.getVerticalScrollBar();
	//miniMapPanel = new MiniMapPanel(view, (MiniMapDataFactory)view.getGlobalContext().get(MiniMapDataFactory.MINIMAP_DATA_FACTORY));
	miniMapPanel = new MiniMapPanel(view);

	tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
	//	tabbedPane.addTab("Info", infoPanel);
	tabbedPane.addTab("Info", scrollPanel);
	tabbedPane.addTab("MiniMap", miniMapPanel);
	tabbedPane.setFont(VAMPResources.getFont(VAMPResources.TABBED_PANE_FONT));
	
	tabbedPane.setBackground(VAMPResources.getColor(VAMPResources.TAB_BG));
	tabbedPane.setForeground(Color.BLACK);

	setOrientation(JSplitPane.VERTICAL_SPLIT);
	setLeftComponent(titlePanel);
	setRightComponent(tabbedPane);
	
	setBackground(VAMPResources.getColor(VAMPResources.INFO_PANEL_BG));
	titlePanel.setBackground(VAMPResources.getColor
				 (VAMPResources.TITLE_PANEL_BG));
	infoPanel.setBackground(VAMPResources.getColor
				(VAMPResources.INFO_PANEL_BG));

        setOneTouchExpandable(true);
        setDividerSize(2);
	setResizeWeight(0.);
	setDividerLocation(45);
	setBackground(VAMPResources.getColor(VAMPResources.INFO_PANEL_BG));
	GridBagConstraints c;

	Font font = VAMPResources.getFont(VAMPResources.INFO_PANEL_TITLE_FONT);
	topTitleLabel = new JLabel();
	topTitleLabel.setFont(font);
	topTitleLabel.setForeground(VAMPResources.getColor
				    (VAMPResources.INFO_TOP_TITLE_FG));
	c = Utils.makeGBC(0, 0);
	titlePanel.add(topTitleLabel, c);

	// ....
	updatePanel = new JPanel();
	updatePanel.setBackground(VAMPResources.getColor(VAMPResources.TITLE_PANEL_BG));
	leftTitleLabel = new JLabel();
	leftTitleLabel.setFont(font);
	leftTitleLabel.setForeground(VAMPResources.getColor
				    (VAMPResources.INFO_TOP_TITLE_FG));
	updatePanel.add(leftTitleLabel);

	topTitleTF = new JTextField();
	topTitleTF.setFont(font);
	topTitleTF.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    view.setOrigName(((JTextField)e.getSource()).getText());
		    view.getGraphPanelSet().setTopTitle();
		    topTitleLabel.setVisible(true);
		    updatePanel.setVisible(false);
		    view.repaint();
		}
	    });

	updatePanel.add(topTitleTF);
	updatePanel.setVisible(false);

	c = Utils.makeGBC(0, 0);
	titlePanel.add(updatePanel, c);
	// ...

	bottomTitleLabel = new JLabel();
	bottomTitleLabel.setFont(font);
	bottomTitleLabel.setForeground(VAMPResources.getColor(VAMPResources.INFO_BOTTOM_TITLE_FG));
	c = Utils.makeGBC(0, 1);
	titlePanel.add(bottomTitleLabel, c);

	addMouseListener(new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
		    if (topTitleLabel.isVisible()) {
			topTitleLabel.setVisible(false);
			updatePanel.setVisible(true);
			String name = view.getOrigName() != null ?
			    view.getOrigName() : view.getName();
			int cols = name.length();
			if (cols > MAX_COLUMNS)
			    cols = MAX_COLUMNS;
			topTitleTF.setColumns(cols);
			topTitleTF.setText(name);
		    }
		    else {
			updatePanel.setVisible(false);
			topTitleLabel.setVisible(true);
		    }
		    //revalidate();
		}
	    });
    }

    void setInfo(GraphPanel graphPanel, JPanel panel, GraphElement set,
		 PropertyElement elem,
		 //DataElement elem,
		 Region region, Mark mark,
		 DataElementRange range, double vx, boolean pinned_up) {
	last_graph_panel = graphPanel;
	last_panel = panel;
	last_set = set;
	last_elem = elem;
	last_region = region;
	last_mark = mark;
	last_range = range;
	last_vx = vx;
	last_pinned_up = pinned_up;
	if (tabbedPane.getSelectedComponent() == scrollPanel)
	    update(panel);
	else if (tabbedPane.getSelectedComponent() == miniMapPanel)
	    miniMapPanel.setInfo(graphPanel, set, elem, range, vx);
    }

    void sync() {
	if (tabbedPane.getSelectedComponent() == miniMapPanel)
	    miniMapPanel.sync();
    }

    void setInfo() {
	setInfo(last_graph_panel, last_panel, last_set, last_elem, last_region,
		last_mark, last_range, last_vx, last_pinned_up);
    }

    void setPosX(long begin, long end, long position) {
	miniMapPanel.setPosX(begin, end, position);
    }

    void unsetPosX() {
	miniMapPanel.unsetPosX();
    }

    void setTopTitle(String left_title, String right_title) {
	//leftTitleLabel.setText(left_title);
	topTitleLabel.setText(left_title + right_title);
    }

    void setBottomTitle(String bottomTitle) {
	bottomTitleLabel.setText(bottomTitle);
    }

    void syncLock() {
    }

    private int maxWidth = 0;
    private final int XMARGIN = 10;

    private int maxHeight = 0;
    private final int YMARGIN = 10;

    void update(JPanel panel) {
	if (infoPanel.getComponentCount() == 0 ||
	    infoPanel.getComponent(0) != panel) {
	    infoPanel.removeAll();
	    infoPanel.add(panel);
	}

	panel.setLocation(5, 5);
	panel.setSize(panel.getPreferredSize());

	if (panel.getSize().width + XMARGIN > maxWidth)
	    maxWidth = panel.getSize().width + XMARGIN;

	if (panel.getSize().height + YMARGIN > maxHeight)
	    maxHeight = panel.getSize().height + YMARGIN;

	infoPanel.setPreferredSize(new Dimension(maxWidth, maxHeight));

	infoPanel.revalidate();
	infoPanel.repaint();
    }

    MiniMapPanel getMiniMap() {
	return miniMapPanel;
    }

    void setInfoPanel(int which) {
	if (which == INFO_PANEL) {
	    tabbedPane.setSelectedComponent(scrollPanel);
	    setInfo();
	}
	else if (which == MINIMAP_PANEL) {
	    tabbedPane.setSelectedComponent(miniMapPanel);
	    setInfo();
	}
    }

    static final int INCR = 10;

    void scrollDown(boolean page) {
	int max = vScroll.getMaximum() - vScroll.getVisibleAmount() -
	    vScroll.getMinimum();
	if (page)
	    vScrollValue = max;
	else {
	    vScrollValue = vScroll.getValue();
	    if (vScrollValue + INCR < max)
		vScrollValue = vScrollValue+INCR;
	    else
		vScrollValue = max;
	}

	vScroll.setValue(vScrollValue);
    }

    void scrollUp(boolean page) {
	if (page)
	    vScrollValue = 0;
	else {
	    vScrollValue = vScroll.getValue();
	    if (vScrollValue >= INCR)
		vScrollValue = vScrollValue-INCR;
	    else
		vScrollValue = 0;
	}

	vScroll.setValue(vScrollValue);
    }
}
