
/*
 *
 * CommonDataSetDisplayer.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.awt.geom.*;
import java.awt.image.*;

abstract public class CommonDataSetDisplayer extends GraphElementDisplayer {

    static final int OFFSCREEN_PADY = 10;

    public CommonDataSetDisplayer(String name, GraphElementIDBuilder graphElementIDBuilder) {
	super(name, graphElementIDBuilder);
    }

    boolean gnlColorCodes;

    public boolean isGNLColorCodes() {
	return gnlColorCodes;
    }

    public void setGNLColorCodes(boolean gnlColorCodes) {
	this.gnlColorCodes = gnlColorCodes;
    }

    public void GNLCodeManage(boolean skip) {

	boolean isGnl = gnlColorCodes && !skip;

	VAMPProperties.GNLProp.setActive(isGnl);
	VAMPProperties.RatioProp.setActive(!isGnl);
	VAMPProperties.CopyNBProp.setActive(!isGnl);
	VAMPProperties.SignalProp.setActive(!isGnl);
	VAMPProperties.RSignalProp.setActive(!isGnl);

	/*
	if (skip) {
	    if (gnlColorCodes) {
		VAMPConstants.GNLProp.setActive(false);
		VAMPConstants.RatioProp.setActive(true);
	    }
	    return;
	}
	VAMPConstants.GNLProp.setActive(gnlColorCodes);
	VAMPConstants.RatioProp.setActive(!gnlColorCodes);
	*/
    }

    static final Color transparentBG = new Color(0, 0, 0, 0);

    class ASyncOffscreen extends Thread {
	GraphCanvas canvas;
	GraphElement graphElement;
	Graphics2D g;
	int m;
	double y, scale_y;

	ASyncOffscreen(GraphCanvas canvas, Graphics2D g, GraphElement graphElement, int m) {
	    this.canvas = canvas;
	    this.g = g;
	    this.graphElement = graphElement;
	    this.m = m;
	    y = canvas.getOrig().y;
	    scale_y = canvas.getScale().getScaleY();
	}

	public void run() {
	    if (graphElement.isOffScreenCompute())
		return;

	    if (y != canvas.getOrig().y) {
		return;
	    }

	    if (scale_y != canvas.getScale().getScaleY()) {
		return;
	    }
	    graphElement.setOffScreenCompute(true);
	    BufferedImage offScreen = graphElement.getOffScreen(canvas);
	    Rectangle2D.Double rBounds = graphElement.getRBounds();
	    if (GraphCanvas.VERBOSE)
		System.out.println("ASync starting " + graphElement.getID() +
				   " " + rBounds);

	    Graphics2D img_g = (Graphics2D)offScreen.getGraphics();

	    img_g.setColor(transparentBG);
	    img_g.fillRect(0, 0, (int)offScreen.getWidth(),
			   (int)offScreen.getHeight());

	    img_g.translate((int)-rBounds.x,
			    (int)-(rBounds.y-rBounds.height) +
			    OFFSCREEN_PADY);

	    if (GraphCanvas.VERBOSE)
		System.out.println("display offscreen");
	    display_r(canvas, img_g, graphElement, true, m, null);

	    if (GraphCanvas.VERBOSE)
		System.out.println("ASync done "  + graphElement.getID());
	    graphElement.setOffScreenValid(true);

	    canvas.repaint();

	    graphElement.setOffScreenCompute(false);
	}
    }

    static boolean NO_ASYNC = false;

    protected void display_os(GraphCanvas canvas, Graphics2D g,
			      GraphElement graphElement, int m, PrintContext pctx) {

	if (graphElement.isOffScreenable() && pctx == null) {
	    if (!graphElement.isVisible())
		return;

	    Rectangle2D.Double rBounds = graphElement.getRBounds();
	    BufferedImage offScreen = graphElement.getOffScreen(canvas);

	    if (!graphElement.isOffScreenValid() && !GraphCanvas.NO_DISPLAY) {
		ASyncOffscreen s = new ASyncOffscreen(canvas, g, graphElement, m);
		if (NO_ASYNC) {
		    s.start();
		    try {
			s.join();
		    }
		    catch(Exception e) {
		    }
		}
		else {
		    SwingUtilities.invokeLater(s);
		    if (GraphCanvas.VERBOSE)
			System.out.println("display direct (waiting for thread)");
		    display_r(canvas, g, graphElement, false, m, pctx);
		    return;
		}
	    }


	    if (GraphCanvas.VERBOSE)
		System.out.println("display inscreen");

	    if (g != null && !GraphCanvas.NO_DISPLAY)
		g.drawImage(offScreen,
			    (int)rBounds.x,
			    (int)(rBounds.y-rBounds.height)-OFFSCREEN_PADY,
			    canvas);
	    return;
	}

	if (GraphCanvas.VERBOSE)
	    System.out.println("display direct");

	display_r(canvas, g, graphElement, false, m, pctx);
    }

    public void display_r(GraphCanvas canvas, Graphics2D g,
			  GraphElement graphElement,
			  boolean offScreen, int m, PrintContext pctx) {
    }
}
