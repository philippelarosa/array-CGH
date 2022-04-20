
/*
 *
 * ZoomPanel.java
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

class ZoomPanel extends JPanel {

    private Color bgColor = VAMPResources.getColor(VAMPResources.ZOOM_PANEL_BG);
    
    static private int slideRatio = VAMPResources.getInt(VAMPResources.DEFAULT_SLIDE_RATIO);

    private GraphPanelSet graphPanelSet;
    private PanelLinks panelLinks[];
    private JCheckBox fitInPageCB;

    private static String powerOf = "";

    Vector slider_v = new Vector();

    class SliderTF {
	JSlider slider;
	JTextField textField;
	JCheckBox fitInPageCB;
	int n;
	int slide_min, slide_max;
	boolean isX;

	SliderTF(int n, boolean isX,
		 JSlider slider, JTextField textField,
		 JCheckBox fitInPageCB,
		 int slide_min, int slide_max) {
	    this.n = n;
	    this.isX = isX;
	    this.slider = slider;
	    this.textField = textField;
	    this.fitInPageCB = fitInPageCB;
	    this.slide_min = slide_min;
	    this.slide_max = slide_max;
	    slider_v.add(this);
	}

	void setScale(GraphPanelSet graphPanelSet, double value) {
	    if (isX) {
		if (n < 0) {
		    int ind[] = panelLinks[-n-1].getInd();
		    for (int j = 0; j < ind.length; j++)
			graphPanelSet.setScaleX(ind[j], value);
		}
		else
		    graphPanelSet.setScaleX(n, value);
		return;
	    }

	    if (n < 0) {
		int ind[] = panelLinks[-n-1].getInd();
		for (int j = 0; j < ind.length; j++)
		    graphPanelSet.setScaleY(ind[j], value);
	    }
	    else
		graphPanelSet.setScaleY(n, value);
	}
    }

    int getSliderInd(int whichPanel, boolean isX) {
	int cnt = slider_v.size();
	for (int m = 0; m < cnt; m++) {
	    SliderTF sliderTF = (SliderTF)slider_v.get(m);
	    if (sliderTF.isX != isX)
		continue;

	    int n = sliderTF.n;
	    if (n < 0) {
		int ind[] = panelLinks[-n-1].getInd();
		for (int j = 0; j < ind.length; j++)
		    if (ind[j] == whichPanel)
			return m;
	    }
	    else if (n == whichPanel)
		return m;
	}

	return -1;
    }

    void setSlidersScale(Scale scale, boolean set_value) {
	int cnt = slider_v.size();
	for (int n = 0; n < cnt; n++) {
	    setSliderScale(n, scale, set_value);
	    /*
	    SliderTF sliderTF = (SliderTF)slider_v.get(n);
	    double fps = adjust(sliderTF.textField,
				toFps(sliderTF.isX ? scale.getScaleX() :
				      scale.getScaleY()),
				sliderTF.slide_min, sliderTF.slide_max);
	    if (set_value)
		sliderTF.slider.setValue((int)fps);
	    */
	}
    }

    void setSlidersValue(int value) {
	int cnt = slider_v.size();
	for (int n = 0; n < cnt; n++) {
	    SliderTF sliderTF = (SliderTF)slider_v.get(n);
	    sliderTF.slider.setValue(value);
	}
    }

    private JLabel makeLabel(String s) {
	JLabel l = new JLabel(s);
	Font font = VAMPResources.getFont(VAMPResources.ZOOM_PANEL_LABEL_FONT);
	l.setFont(font);
	return l;
    }

    private double adjust(JTextField textField, double fps, int min, int max) {
	String s = null;
	if (fps > max)
	    fps = max;
	else if (fps < min)
	    fps = min;
	s = Utils.toString(fps/slideRatio);
	if (s != null)
	    textField.setText(s);
	return fps;
    }

    private JCheckBox addCheckBox(int x, int y) {
	return null;
    }

    private void addSliderLine(int x, int y, String s, JSlider slider,
			       JTextField textField,
			       JCheckBox fitInPageCB) {
	y *= 2;
	GridBagConstraints c = Utils.makeGBC(x, y);
	JLabel l = makeLabel("Fit");
	add(l, c);
	//x++;

	c = Utils.makeGBC(x, y+1);
	fitInPageCB.setBackground(bgColor);
	add(fitInPageCB, c);
	x++;

	/*
	Utils.addPadPanel(this, x, 0, 2, 0, bgColor);
	x++;
	*/

	l = makeLabel(s);
	c = Utils.makeGBC(x, y);
	setBackground(bgColor);
	add(l, c);

	c = Utils.makeGBC(x, y+1);
	c.ipady = 0;
	//c.ipadx = 5;
	//c.weightx = 0.5;
	c.ipadx = 90;
	c.gridheight = 2;
	c.fill = GridBagConstraints.HORIZONTAL;
	add(slider, c);

	Utils.addPadPanel(this, x+1, 0, 5, 0, bgColor);
	c = Utils.makeGBC(x+2, y+1);
	c.fill = GridBagConstraints.HORIZONTAL;
	//c.weightx = 0.3;// 0.4
	c.ipadx = 30;
	Font font = VAMPResources.getFont(VAMPResources.ZOOM_PANEL_TEXTFIELD_FONT);
	textField.setFont(font);
	add(textField, c);
	Utils.addPadPanel(this, x+3, 0, 2, 0, bgColor);
	textField.setText("0.0");
    }

    ZoomPanel(GraphPanelSet _graphPanelSet,
	      PanelProfile panelProfiles[],
	      PanelLinks panelLinks[],
	      Dimension size,
	      LinkedList graphElements) {

        setLayout(new GridBagLayout());
	setBackground(bgColor);
	this.graphPanelSet = _graphPanelSet;
	this.panelLinks = panelLinks;

	int y = 0;

	Utils.addPadPanel(this, 0, y, 5, 5, bgColor);
	y++;

	JLabel l;
	GridBagConstraints c;

	for (int n = 0; n < panelProfiles.length; n++) {
	    PanelProfile panelProfile = panelProfiles[n];
	    if (panelProfile.isDisabled())
		continue;
	    GraphElementDisplayer defaultGraphElementDisplayer =
		panelProfile.getDefaultGraphElementDisplayer();
	    Scale defScale = panelProfile.getDefaultScale();

	    if (defScale == null && defaultGraphElementDisplayer != null)
		defScale = defaultGraphElementDisplayer.getDefaultScale(size, graphElements);

	    ZoomTemplate zoomTemplate = panelProfile.getZoomTemplate();
	    
	    if (zoomTemplate == null)
		zoomTemplate = Config.defaultZoomTemplate;

	    int Xslide_min = zoomTemplate.getXSlideMin() * slideRatio; 
	    int Yslide_min = zoomTemplate.getYSlideMin() * slideRatio;
	    int Xslide_max = zoomTemplate.getXSlideMax() * slideRatio;
	    int Yslide_max = zoomTemplate.getYSlideMax() * slideRatio;
	    
	    SliderTF sliderTF;
	    int lnk = getLinkedX(n, panelLinks);

	    if (lnk < 0) {
		sliderTF = makeSliderTF(n, true,
					1, y, "X " +
					panelProfile.getName() +
					" Scale" + powerOf,
					Xslide_min, Xslide_max, defScale);
		y += 2;
	    }
	    else {
		int ind[] = panelLinks[lnk].getInd();
		if (ind[0] == n) {
		    sliderTF = makeSliderTF(-lnk-1, true,
					    1, y, "X " +
					    panelLinks[lnk].getName() +
					    " Scale" + powerOf,
					    Xslide_min, Xslide_max,
					    defScale);
		    y += 2;
		}
	    }

	    lnk = getLinkedY(n, panelLinks);

	    if (lnk < 0) {
		sliderTF = makeSliderTF(n, false,
					1, y, "Y " +
					panelProfile.getName() +
					" Scale" + powerOf,
					Yslide_min, Yslide_max,
					defScale);
		y += 2;
	    }
	    else {
		int ind[] = panelLinks[lnk].getInd();
		if (ind[ind.length-1] == n) {
		    sliderTF = makeSliderTF(-lnk-1, false,
					    1, y,
					    "Y " +
					    panelLinks[lnk].getName() +
					    " Scale" + powerOf,
					    Yslide_min, Yslide_max, defScale);
		    y += 2;
		}
	    }
	}


	JPanel p = new JPanel();
	p.setBackground(bgColor);
	l = makeLabel("Fit in page");
	p.add(l);

	fitInPageCB = new JCheckBox();
	fitInPageCB.setBackground(bgColor);
	fitInPageCB.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    boolean value = ((JCheckBox)e.getSource()).isSelected();
		    setFitInPage_r(value);
		    if (value) {
			GraphPanel panels[] = graphPanelSet.getPanels();
			boolean actives[] = new boolean[panels.length];
			for (int n = 0; n < panels.length; n++) {
			    actives[n] = UndoVMStack.getInstance(panels[n]).isActive();
			    UndoVMStack.getInstance(panels[n]).setActive(false);
			}
			setSlidersValue(0);
			for (int n = 0; n < panels.length; n++)
			    UndoVMStack.getInstance(panels[n]).setActive(actives[n]);
		    }			
		}
	    });


	p.add(fitInPageCB);
	c = Utils.makeGBC(0, 2*y);
	c.gridwidth = 4;
	add(p, c);

	//setFitInPage(defScale == null);
	setFitInPage(true);

	/*
	if (defScale != null) {
	    setScale(defScale);
	}
	*/
    }

    void setScale(Scale scale) {
	setSlidersScale(scale, false);
    }

    void setScaleAndValue(Scale scale) {
	setSlidersScale(scale, true);
    }

    void setScaleAndValue(int which, Scale scale) {
	setSliderScale(which, scale, true);
    }

    void setSliderScale(int which, Scale scale, boolean set_value) {
	SliderTF sliderTF = (SliderTF)slider_v.get(which);
	double fps = adjust(sliderTF.textField,
			    toFps(sliderTF.isX ? scale.getScaleX() :
				  scale.getScaleY()),
			    sliderTF.slide_min, sliderTF.slide_max);
	if (set_value)
	    sliderTF.slider.setValue((int)fps);
    }

    private SliderTF makeSliderTF(int n, boolean isX,
				  int x, int y, String s, int min, int max,
				  Scale scale) {
	JSlider slider = makeSlider(min, max);
	JTextField textField = new JTextField(4);
	JCheckBox fitInPageCB = new JCheckBox();
	addSliderLine(x, y, s, slider, textField, fitInPageCB);

	SliderTF sliderTF = new SliderTF(n, isX, slider, textField,
					 fitInPageCB, min, max);

	if (scale != null)
	    slider.setValue((int)adjust(textField,
					toFps(isX ? scale.getScaleX() : scale.getScaleY()),
					min,
					max));

	fitInPageCB.addActionListener(new ActionListenerWrapper(sliderTF) {
		public void actionPerformed(ActionEvent e) {
		    boolean value = ((JCheckBox)e.getSource()).isSelected();
		    //graphPanelSet.setFitInPage(value);
		    if (value) {
			SliderTF sliderTF = (SliderTF)getValue();
			sliderTF.slider.setValue(0);
		    }
		}
	    });

	slider.addChangeListener(new ChangeListenerWrapper(sliderTF) {
		public void stateChanged(ChangeEvent e) {
		    SliderTF sliderTF = (SliderTF)getValue();
		    JSlider source = (JSlider)e.getSource();
		    if (!source.getValueIsAdjusting() ||
			VAMPResources.getBool
			(VAMPResources.SCALING_WHILE_ADJUSTING)) {
			int fps = (int)source.getValue();
			double dfps = ((double)fps/(double)slideRatio);
			sliderTF.setScale(graphPanelSet, toScale(fps));
			sliderTF.textField.setText(Utils.toString(dfps));

			if (fps != 0) {
			    setFitInPage(false);
			    sliderTF.fitInPageCB.setSelected(false);
			}
		    }
		}
	    });
	
	textField.addActionListener(new ActionListenerWrapper(sliderTF) {
		public void actionPerformed(ActionEvent e) {
		    SliderTF sliderTF = (SliderTF)getValue();
		    JTextField tf = (JTextField)e.getSource();
		    double fps;
		    try {
			fps = Utils.parseDouble(tf.getText()) * slideRatio;
		    }
		    catch(NumberFormatException x) {
			return;
		    }
		    //System.out.println("ActionListener(" + fps + ")");
		    /*
		    if (sliderTF.isX) {
			fps = adjust(tf, fps, sliderTF.slide_min, sliderTF.slide_max);
			graphPanelSet.setScaleX(sliderTF.n,
						toScale(fps));
			if (sliderTF.slider != null)
			    sliderTF.slider.setValue((int)fps);
		    }
		    else {
			fps = adjust(tf, fps, sliderTF.slide_min,
				     sliderTF.slide_max);
			graphPanelSet.setScaleY(sliderTF.n,
						toScale(fps));
			sliderTF.slider.setValue((int)fps);
		    }
		    */

		    fps = adjust(tf, fps, sliderTF.slide_min, sliderTF.slide_max);
		    sliderTF.setScale(graphPanelSet, toScale(fps));
		    if (sliderTF.slider != null)
			sliderTF.slider.setValue((int)fps);

		    if (fps != 0)
			setFitInPage(false);
		    else
			sliderTF.fitInPageCB.setSelected(true);
		}
	    });

	return sliderTF;
    }

    private JSlider makeSlider(int min, int max) {
        JSlider slider = new JSlider(JSlider.HORIZONTAL, min, max, 0);

	max = max/slideRatio;
	min = min/slideRatio;

	Font font = VAMPResources.getFont(VAMPResources.ZOOM_PANEL_LABEL_FONT);
	Hashtable dict = new Hashtable();
	JLabel l = new JLabel(new Integer(min).toString());
	l.setFont(font);
	dict.put(new Integer(min*slideRatio), l);

	l = new JLabel("Fit");
	l.setFont(font);
	dict.put(new Integer(0), l);

	/*
	for (int n = max; n <= max; n++) {
	    l = new JLabel(new Integer(n).toString());
	    l.setFont(font);
	    dict.put(new Integer(n*slideRatio), l);
	}
	*/

	l = new JLabel(new Integer(max).toString());
	l.setFont(font);
	dict.put(new Integer(max*slideRatio), l);

	slider.setLabelTable(dict);
        slider.setMajorTickSpacing(slideRatio);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
	slider.setBackground(bgColor);
	return slider;
    }

    static double toScale(double fps) {
	return Utils.pow(fps/(double)slideRatio);
    }

    static double toFps(double fps) {
	return Utils.log(fps) * (double)slideRatio;
    }

    private void setFitInPage_r(boolean value) {
	graphPanelSet.setFitInPage(value);
	if (!value)
	    return;

	int cnt = slider_v.size();
	for (int n = 0; n < cnt; n++) {
	    SliderTF sliderTF = (SliderTF)slider_v.get(n);
	    sliderTF.fitInPageCB.setSelected(value);
	}
    }

    public void setFitInPage(boolean value) {
	fitInPageCB.setSelected(value);
	setFitInPage_r(value);
    }

    int getLinkedX(int n, PanelLinks panelLinks[]) {
	return getLinked(GraphPanel.SYNCHRO_X, n, panelLinks);
    }

    int getLinkedY(int n, PanelLinks panelLinks[]) {
	return getLinked(GraphPanel.SYNCHRO_Y, n, panelLinks);
    }

    int getLinked(int sync_mode, int n, PanelLinks panelLinks[]) {
	if (panelLinks == null) return -1;
	for (int j = 0; j < panelLinks.length; j++) {
	    if (panelLinks[j].getSyncMode() != sync_mode)
		continue;

	    int ind[] = panelLinks[j].getInd();
	    for (int k = 0; k < ind.length; k++)
		if (ind[k] == n)
		    return j;
	}

	return -1;
    }

    int getSliderCount() {return slider_v.size();}
}
