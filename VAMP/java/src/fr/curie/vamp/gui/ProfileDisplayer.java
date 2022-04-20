
package fr.curie.vamp.gui;

import java.awt.*;

import fr.curie.vamp.data.*;
import fr.curie.vamp.properties.*;
import fr.curie.vamp.gui.optim.*;
import fr.curie.vamp.*;

public class ProfileDisplayer extends CommonDataSetDisplayer {

    static Property GraphicPropList[] = new Property[] {
	VAMPProperties.CopyNBProp,
	VAMPProperties.SignalProp,
	VAMPProperties.RSignalProp,
	VAMPProperties.ColorCodeProp
    };

    public static class GraphicSetter implements Painter.GraphicSetter {
	public void setGraphics(Graphics g, Probe probe, Profile profile) {
	    _setGraphics(g, probe, profile);
	}

	static public void _setGraphics(Graphics g, Probe probe, Profile profile) {

	    Graphics2D g2 = (Graphics2D)g;
	    VAMPProperties.RatioProp.setGraphics(g2, probe.getRatio(), null, profile);
	    VAMPProperties.GNLProp.setGraphics(g2, (new Integer(probe.getGnl())).toString(), null, profile);

	    /*
	    for (int n = 0; n < GraphicPropList.length; n++) {
		Object value = probe.getProp(GraphicPropList[n]);
		if (value != null) {
		    GraphicPropList[n].setGraphics(g2, value, null, profile);
		}
	    }
	    */
	}
    }

    private boolean skip_normal;
    private boolean show_size;
    private int mode;

    public ProfileDisplayer(boolean gnl_mode, boolean skip_normal, boolean show_size, int mode) {
	super("Large Profile", null);
	setGNLColorCodes(gnl_mode);
	this.skip_normal = skip_normal;
	this.show_size = show_size;
	this.mode = mode;
    }

    public void display(GraphCanvas canvas, Graphics2D g,
			GraphElement graphElement,
			int m, PrintContext pctx) {

	Profile profile = graphElement.asProfile();
	if (profile == null)
	    return;
	fr.curie.vamp.Scale scale = canvas.getScale();

	/*
	// to test
	try {
	    int len = profile.getProbeCount();
	    for (int n = len-1; n >= 0; n--) {
		Probe p = profile.getProbe(n);
		if (profile.asProfile().getType(p.getType()) == null) {
		    System.out.println("oups : " + p.getPos());
		}
	    }
	}
	catch(Exception e) {
	    e.printStackTrace();
	}
	*/

	try {
	    GraphicProfile graphicProfile = profile.getGraphicProfile();
	    if (profile.isFullImported()) {
		GNLCodeManage(profile.getProbe(0, true, true).getProp(VAMPProperties.GNLProp) == null);
	    }
	    Painter painter = new Painter(graphicProfile, isGNLColorCodes(), skip_normal, show_size, mode);
	    painter.paint(g, painter.makeScale(canvas, profile), canvas.getSize(), pctx, new GraphicSetter());
	}
	catch(Exception e) {
	    e.printStackTrace();
	}
    }

    public void computeVBounds(GraphCanvas canvas,
			       Graphics2D g,
			       GraphElement graphElement,
			       int m) {
	graphElement.resetPaintVBounds();
    }
}
