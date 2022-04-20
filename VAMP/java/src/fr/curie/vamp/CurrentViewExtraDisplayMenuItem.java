
/*
 *
 * CurrentViewExtraDisplayMenuItem.java
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

class CurrentViewExtraDisplayMenuItem extends ViewMenuItem {

    static final int SHOW_NORMAL = 1;
    static final int SHOW_BRK = 2;
    //    static final int SHOW_SMT_PTS = 3;
    static final int SHOW_SMT_LNS = 4;
    static final int SHOW_CTM = 5;
    static final int SHOW_OUT = 6;
    static final int SHOW_SIZE = 7;
    static final int SHOW_TAG = 8;
    static final int SHOW_TAG_STRING = 9;
    static final int SHOW_INFO = 10;
    static final int SHOW_OUT_OF_BOUNDS_INFO = 11;
    static final int SHOW_GENE_INFO = 12;
    static final int SHOW_SYNTENY_LINKS = 13;
    static final int SHOW_CHR_SEP = 14;
    static final int SHOW_ANNOT = 15;

    static final int SHOW_ALL = 100;
    static final int HIDE_ALL = 200;

    int what;

    CurrentViewExtraDisplayMenuItem(String defaultName,
				    int what,
				    boolean addSeparator) {
	super(defaultName, SUPPORT_X | SUPPORT_CURRENT_VIEW, addSeparator);
	this.what = what;
    }

    boolean hasActionListener() {return true;}
    boolean hasMenuListener() {return true;}

    void actionPerformed(View view, GraphPanel panel) {
	GraphElementDisplayer ds = panel.getDefaultGraphElementDisplayer();
	actionPerformed(view, panel, ds);
	LinkedList l = panel.getGraphElements();
	int size = l.size();
	for (int n = 0; n < size; n++) {
	    GraphElement ge = (GraphElement)l.get(n);
	    ds = ge.getGraphElementDisplayer();
	    if (ds != null)
		actionPerformed(view, panel, ds);
	}
    }

    private void actionPerformed(View view, GraphPanel panel,
				 GraphElementDisplayer ds) {
	if (what == SHOW_CHR_SEP) {
	    AxisDisplayer ax = panel.getDefaultAxisDisplayer();
	    if (ax instanceof ChromosomeNameAxisDisplayer) {
		ChromosomeNameAxisDisplayer cax = (ChromosomeNameAxisDisplayer)ax;
		cax.showChrSep(!cax.showChrSep());
		view.repaint();
	    }
	}
	else if (what == SHOW_ANNOT)
	    panel.showHideEastY();
	else if (ds instanceof YDendrogramGraphElementDisplayer) {
	    YDendrogramGraphElementDisplayer dds = (YDendrogramGraphElementDisplayer)ds;
	    if (what == SHOW_TAG_STRING) {
		dds.showTagString(!dds.showTagString());
		view.repaint();
	    }
	}
	else if (ds instanceof StandardDataSetDisplayer) {
	    StandardDataSetDisplayer sds = (StandardDataSetDisplayer)ds;
	    if (what == SHOW_NORMAL)
		sds.showNormal(!sds.showNormal());
	    else if (what == SHOW_BRK)
		sds.showBreakpoints(!sds.showBreakpoints());
	    /*
	    else if (what == SHOW_SMT_PTS)
		sds.showSmoothingPoints(!sds.showSmoothingPoints());
	    */
	    else if (what == SHOW_SMT_LNS)
		sds.showSmoothingLines(!sds.showSmoothingLines());
	    else if (what == SHOW_CTM)
		sds.showCentromere(!sds.showCentromere());
	    else if (what == SHOW_OUT)
		sds.showOut(!sds.showOut());
	    else if (what == SHOW_SIZE) {
		sds.showSize(!sds.showSize());
		if (sds.showSize())
		    view.applyOnGraphElements
			(DataSetSizePerformer.getSetSizePerformer());
		else
		    view.applyOnGraphElements
			(DataSetSizePerformer.getUnsetSizePerformer());
	    }
	    else if (what == SHOW_TAG)
		sds.showTag(!sds.showTag());
	    else if (what == SHOW_TAG_STRING)
		sds.showTagString(!sds.showTagString());
	    else if (what == SHOW_INFO)
		sds.showInfo(!sds.showInfo());
	    else if (what == SHOW_GENE_INFO)
		sds.showGeneInfo(!sds.showGeneInfo());
	    else if (what == SHOW_SYNTENY_LINKS)
		sds.showLinks(!sds.showLinks());
	    else if (what == SHOW_OUT_OF_BOUNDS_INFO)
		sds.showOutOfBoundsInfo(!sds.showOutOfBoundsInfo());
	    else if (what == SHOW_ALL) {
		sds.showBreakpoints(true);
		sds.showSmoothingLines(true);
		//sds.showSmoothingPoints(true);
		sds.showCentromere(true);
		sds.showOut(true);
	    }
	    else if (what == HIDE_ALL) {
		sds.showBreakpoints(false);
		sds.showSmoothingLines(false);
		sds.showSmoothingPoints(false);
		sds.showCentromere(false);
		sds.showOut(false);
	    }
	    view.repaint();
	}
    }

    void menuSelected(View view, GraphPanel panel, JMenuItem menuItem) {
	String text = null;
	GraphElementDisplayer ds = panel.getDefaultGraphElementDisplayer();
	if (what == SHOW_CHR_SEP) {
	    AxisDisplayer ax = panel.getDefaultAxisDisplayer();
	    if (ax instanceof ChromosomeNameAxisDisplayer) {
		ChromosomeNameAxisDisplayer cax = (ChromosomeNameAxisDisplayer)ax;
		text = (cax.showChrSep() ? "Hide" : "Show") + " Chromosome Separators";
		menuItem.setText(text);
		menuItem.setEnabled(!view.isEmpty());
	    }
	    else {
		menuItem.setEnabled(false);
		menuItem.setText(getDefaultName());
	    }
	}
	else if (what == SHOW_ANNOT) {
	    text = (panel.hasEastY() ? "Hide" : "Show") + " Annotations";
	    menuItem.setEnabled(true);
	    menuItem.setText(text);
	}
	else if (ds instanceof YDendrogramGraphElementDisplayer) {
	    YDendrogramGraphElementDisplayer dds = (YDendrogramGraphElementDisplayer)ds;
	    if (what == SHOW_TAG_STRING) {
		text = (dds.showTagString() ? "Hide" : "Show") + " Tags";
		menuItem.setText(text);
		menuItem.setEnabled(true);
	    }
	    else {
		menuItem.setText(getDefaultName());
		menuItem.setEnabled(false);
	    }
	}
	else if (ds instanceof StandardDataSetDisplayer) {
	    StandardDataSetDisplayer sds = (StandardDataSetDisplayer)ds;

	    Vector propV = new Vector();
	    Vector notPropV = new Vector();
	    if (what == SHOW_NORMAL) {
		if (sds.showNormalEnabled()) {
		    text = (sds.showNormal() ? "Hide" : "Show") + " Normal Probes";
		    propV.add(VAMPProperties.LargeProfileProp);
		    //propV.add(VAMPProperties.GNLProp);
		}
	    }
	    else if (what == SHOW_BRK) {
		if (sds.showBreakpointsEnabled()) {
		    text = (sds.showBreakpoints() ? "Hide" : "Show") + " Breakpoints";
		
		    propV.add(VAMPProperties.BreakpointProp);
		}
	    }
	    /*
	    else if (what == SHOW_SMT_PTS) {
		if (sds.showSmoothingPointsEnabled()) {
		    text = (sds.showSmoothingPoints() ? "Hide" : "Show") + " Smoothing Points";
		
		    propV.add(VAMPProperties.SmoothingProp);
		}
	    }
	    */
	    else if (what == SHOW_SMT_LNS) {
		if (sds.showSmoothingLinesEnabled()) {
		    text = (sds.showSmoothingLines() ? "Hide" : "Show") + " Smoothing Lines";
		    propV.add(VAMPProperties.SmoothingProp);
		}
	    }
	    else if (what == SHOW_CTM) {
		if (sds.showCentromereEnabled()) {
		    text = (sds.showCentromere() ? "Hide" : "Show") + " Centromeres";
		    propV.add(VAMPProperties.CentromereProp);
		}
	    }
	    else if (what == SHOW_OUT) {
		if (sds.showOutEnabled()) {
		    text = (sds.showOut() ? "Un-Highlight" : "Highlight") + " Outliers";
		    propV.add(VAMPProperties.OutProp);
		}
	    }
	    else if (what == SHOW_SIZE) {
		if (sds.showSizeEnabled()) {
		    text = (sds.showSize() ? "Hide" : "Show") + " Probe Sizes";
		    propV.add(VAMPProperties.SizeProp);
		}
	    }
	    else if (what == SHOW_TAG) {
		if (sds.showTagEnabled()) {
		    text = (sds.showTag() ? "Hide" : "Show") + " Flags";
		    propV.add(VAMPProperties.TagProp);
		}
	    }
	    else if (what == SHOW_TAG_STRING) {
		if (sds.showTagStringEnabled()) {
		    text = (sds.showTagString() ? "Hide" : "Show") + " Tags";
		    propV.add(VAMPProperties.TagProp);
		}
	    }
	    else if (what == SHOW_SYNTENY_LINKS) {
		if (sds.showLinksEnabled()) {
		    text = (sds.showLinks() ? "Hide" : "Show") + " Synteny Links";
		    //propV.add(VAMPConstants.NameProp); // to be shure to be displayed
		    propV.add(VAMPProperties.LinkedDataProp); // to be shure to be displayed
		}
	    }
	    else if (what == SHOW_INFO) {
		if (sds.showInfoEnabled()) {
		    text = (sds.showInfo() ? "Hide" : "Show") + " Probe Info";
		    notPropV.add(VAMPProperties.LargeProfileProp);
		    propV.add(VAMPProperties.NameProp);
		    propV.add(VAMPProperties.NmcProp);
		    propV.add(TranscriptomeFactory.ObjectIdProp);
		}
	    }
	    else if (what == SHOW_GENE_INFO) {
		if (sds.showInfoEnabled()) {
		    text = (sds.showGeneInfo() ? "Hide" : "Show") + " Gene Info";
		    propV.add(VAMPProperties.GeneSymbolProp);
		}
	    }
	    else if (what == SHOW_OUT_OF_BOUNDS_INFO) {
		if (sds.showOutOfBoundsInfoEnabled()) {
		    text = (sds.showOutOfBoundsInfo() ? "Hide" : "Show") +
			" Out of range Probe Info";
		    notPropV.add(VAMPProperties.LargeProfileProp);
		    propV.add(VAMPProperties.NameProp);
		    propV.add(VAMPProperties.NmcProp);
		    propV.add(TranscriptomeFactory.ObjectIdProp);
		}
	    }
	    else if (what == SHOW_ALL || what == HIDE_ALL) {
		if (sds.showBreakpointsEnabled())
		    propV.add(VAMPProperties.BreakpointProp);

		if (sds.showSmoothingLinesEnabled())
		    propV.add(VAMPProperties.SmoothingProp);

		if (sds.showCentromereEnabled())
		    propV.add(VAMPProperties.CentromereProp);

		if (sds.showOutEnabled())
		    propV.add(VAMPProperties.OutProp);

		if (sds.showSizeEnabled())
		    propV.add(VAMPProperties.SizeProp);
	    }
	    if (text == null)
		text = getDefaultName();
	    menuItem.setText(text);
	    if (what == SHOW_CTM) {
		menuItem.setEnabled(!view.isEmpty());
	    }
	    else {
		menuItem.setEnabled(VAMPUtils.hasProperty(view, propV));
		if (menuItem.isEnabled()) {
		    if (VAMPUtils.hasProperty(view, notPropV)) {
			menuItem.setEnabled(false);
		    }
		}
	    }
	}
	else {
	    menuItem.setText(getDefaultName());
	    menuItem.setEnabled(false);
	}
    }
}

