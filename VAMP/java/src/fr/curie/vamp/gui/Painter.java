
package fr.curie.vamp.gui;

import fr.curie.vamp.data.*;
import fr.curie.vamp.properties.*;
import fr.curie.vamp.utils.Dichotomic;
import fr.curie.vamp.gui.optim.*;
import java.awt.*;
import fr.curie.vamp.VAMPResources;
import fr.curie.vamp.GraphCanvas;

import fr.curie.vamp.*;

public class Painter {

    private static final int HEIGHT = 2;
    private static final int MIN_WIDTH = 2;
    private static final int MIN_WIDTH2 = 1;
    private static final int OVER_WIDTH = 4;
    private static final int OVER_HEIGHT = 4;
    private static final double EPSILON = 0.1;

    public static final int POINT_MODE = 0x1;
    public static final int BARPLOT_MODE = 0x2;
    public static final int CENTERED_MODE = 0x4;

    private GraphicProfile graphicProfile;
    private boolean gnl_mode;
    private boolean skip_normal;
    private boolean show_size;
    private OptimEngine optimEngine;
    private int mode;
    private int gclip[] = new int[2];

    public interface GraphicSetter {
	public void setGraphics(Graphics g, Probe probe, Profile profile);
    }

    public static class Background {
	Color color;
	int x, y;
	int width, height;

	public Background(Color color, int x, int y, int width, int height) {
	    this.color = color;
	    this.x = x;
	    this.y = y;
	    this.width = width;
	    this.height = height;
	}
    }

    public Painter(GraphicProfile graphicProfile, OptimEngine optimEngine, boolean gnl_mode, boolean skip_normal, boolean show_size, int mode) {
	this.graphicProfile = graphicProfile;
	this.optimEngine = optimEngine;
	this.gnl_mode = gnl_mode;
	this.skip_normal = skip_normal;
	this.show_size = show_size;
	this.mode = mode;
    }

    public Painter(GraphicProfile graphicProfile, OptimEngine optimEngine) {
	this(graphicProfile, optimEngine, false, false, true, POINT_MODE);
    }

    public Painter(GraphicProfile graphicProfile, boolean gnl_mode, boolean skip_normal, boolean show_size, int mode) {
	this(graphicProfile, new OptimEngine(OptimEngine.USE_GRAPHIC_INFO_2_LEVEL), gnl_mode, skip_normal, show_size, mode);
    }

    public Painter(GraphicProfile graphicProfile, boolean gnl_mode, boolean skip_normal, boolean show_size) {
	this(graphicProfile, new OptimEngine(OptimEngine.USE_GRAPHIC_INFO_2_LEVEL), gnl_mode, skip_normal, show_size, POINT_MODE);
    }

    public Painter(GraphicProfile graphicProfile) {
	this(graphicProfile, false, false, true, POINT_MODE);
    }

    public void paint(Graphics g, Scale scale) throws Exception {
	paint(g, scale, null, null, null);
    }

    public void paint(Graphics g, Scale scale, Dimension size, PrintContext pctx, GraphicSetter graphicSetter) throws Exception {
	paint(g, scale, size, pctx, graphicSetter, null);
    }

    public void paint(Graphics g, Scale scale, Dimension size, PrintContext pctx, GraphicSetter graphicSetter, Background bg) throws Exception {
	int probe_scanned_cnt = 0;
	int probe_loaded_cnt = 0;
	int probe_disp_cnt = 0;
	int probe_skipped_cnt = 0;

	Profile profile = graphicProfile.getProfile();
	if (!profile.isFullImported()) {
	    return;
	}

	GraphicInfo graphicInfo = graphicProfile.getGraphicInfo();

	if (GraphCanvas.VERBOSE) {
	    System.out.println((pctx != null ? "Printing " : "Drawing ") + profile.getName());
	}

	double posy_min = VAMPUtils.getThresholdMinY(profile);
	double posy_max = VAMPUtils.getThresholdMaxY(profile);
	//System.out.println("Posy_min: " + posy_min);
	//System.out.println("Posy_max: " + posy_max);
	Color naFG = VAMPResources.getColor(VAMPResources.CLONE_NA_FG);
	Color thrMinFG = VAMPResources.getColor(VAMPResources.THRESHOLD_MINY_FG);
	Color thrMaxFG = VAMPResources.getColor(VAMPResources.THRESHOLD_MAXY_FG);
	ColorCodes cc = VAMPUtils.getColorCodes(profile);
	StandardColorCodes scc = (cc instanceof StandardColorCodes) ?
	    (StandardColorCodes)cc : null;
	
	long ms = System.currentTimeMillis();
	int loaded_probe_cnt = profile.getLoadedProbeCount();

	OptimContext optimCtx = new OptimContext();

	int olevel = optimEngine.getLevel();

	int level;

	if ((level = optimEngine.prologue(optimCtx, graphicInfo, scale, false)) != optimEngine.getLevel()) {
	    optimEngine.setLevel(level);
	    if (GraphCanvas.VERBOSE) {
		System.out.println("setting temp level " + level + " for " + profile.getName());
	    }
	}
	
	if (bg != null) {
	    g.setColor(bg.color);
	    int x = bg.x;
	    int y = bg.y;
	    if (pctx != null) {
		x = (int)pctx.getRX(x);
		y = (int)pctx.getRX(y);
	    }
	    g.fillRect(bg.x, bg.y, bg.width, bg.height);
	}

	boolean point_mode = (mode & POINT_MODE) != 0;
	// <=> canvas.getMinY()
	double miny = scale.getY0() - profile.getVBounds().y;
	//System.out.println("mode : " + mode);

	double rminy = scale.getMinY();
	double y0 = rminy; // origin classic barplot
	if ((mode & CENTERED_MODE) != 0) {
	    y0 += scale.getHeight(miny); // origin centered barplot
	}

	double yy0 = y0;
	if (pctx != null) {
	    yy0 = pctx.getRY(yy0);
	}

	//System.out.println(profile.getID() + " " + profile.getVBounds() + ", rminy : " + rminy);

	int probe_cnt = profile.getProbeCount();
	int probe_offset = profile.getProbeOffset();
	int first = (new FirstVisible(profile, scale)).find();

	int dmap[] = optimEngine.getDirectMap(optimCtx);

	int start, end;
	if (dmap != null) {
	    first += probe_offset;
	    start = (new FirstInMap(dmap, first)).find();
	    end = dmap.length;
	}
	else {
	    start = first;
	    end = probe_cnt;
	}
	
	/*
	System.out.println(scale.getY(0) + " vs0. " + canvas.getRY(vy0 - 0 + miny));
	System.out.println(scale.getY(1) + " vs1. " + canvas.getRY(vy0 - 1 + miny));
	System.out.println(scale.getY0() + " vsz. " + (vy0 + miny));
	System.out.println(scale.getBetaY() + " vsb. " + canvas.getOrig().y);
	*/

	for (int np = start; np < end; np++) {
	    int n = np;
	    if (dmap != null) {
		n = dmap[np];
		n -= probe_offset;
		if (n < 0) {
		    continue;
		}
		if (n >= probe_cnt) {
		    break;
		}

		if (n < first - probe_offset) {
		    continue;
		}
	    }
	    else if (optimEngine.couldSkip(optimCtx, n)) {
		probe_skipped_cnt++;
		continue;
	    }

	    Probe p = profile.getProbe(n);
	    if (p == null) {
		continue;
	    }

	    probe_scanned_cnt++;
	    double x = scale.getX(p.getPanGenPos(profile));

	    if (pctx != null) {
		x = (int)pctx.getRX(x);
	    }

	    if (point_mode) {
		x -= MIN_WIDTH2; // <= bug #161
	    }

	    double width = (show_size ? scale.getWidth(p.getSize()) : 0);
			
	    if (pctx == null && !isVisibleX((int)x, (int)(width < MIN_WIDTH ? MIN_WIDTH : width), size)) {
		if (x < 0) {
		    continue;
		}
		else {
		    /*
		    if (GraphCanvas.VERBOSE) {
			System.out.println("breaking...");
		    }
		    */
		    break;
		}
	    }
		    
	    double posy = p.getPosY(profile);
	    boolean thresholded = false;
	    Color thrFG = null;

	    double y;
	    if (Double.isNaN(posy)) {
		continue;
	    }

	    if (!p.isNA()) {
		if (posy > posy_max) {
		    posy = posy_max;
		    thresholded = true;
		    thrFG = thrMaxFG; // no more useful <= bug #160
		}
		else if (posy < posy_min) {
		    posy = posy_min;
		    thresholded = true;
		    thrFG = thrMinFG; // no more useful <= bug #160
		}

		y = scale.getY(posy);
	    }
	    else {
		if (posy < posy_min-EPSILON) {
		    continue;
		}

		y = y0;
	    }

	    //double y = scale.getY(posy);
	    //double y = canvas.getRY(vy0 - posy + miny);
		
	    if (pctx != null) {
		y = pctx.getRY(y);
		width = (int)pctx.getRW(width);
	    }

	    if (width < MIN_WIDTH) {
		width = MIN_WIDTH;
	    }

	    if (p.isNA()) {
		y -= HEIGHT;
	    }

	    if (point_mode &&
		optimEngine.getLevel() < OptimEngine.USE_GRAPHIC_INFO_1_LEVEL &&
		optimEngine.couldSkip(optimCtx, (int)x, (int)y, (int)width, HEIGHT, n)) {
		continue;
	    }

	    //if (pctx != null || isVisibleY((int)y, size)) {
	    if (pctx != null || isVisibleY(y0, y, size)) {
		int gnl = p.getGnl();
		if (skip_normal) {
		    if (gnl_mode) {
			if (gnl == 0) {
			    continue;
			}
			if (gnl == -Probe.GNL_OFFSET) {
			    if (scc.isNormal(posy)) {
				continue;
			    }
			}
		    }
		    else {
			if (scc.isNormal(posy)) {
			    continue;
			}
		    }
		}

		int clip[] = getClip(p, y0, y);

		//		if (pctx != null || g.hitClip((int)x, (int)y, (int)width, HEIGHT)) {
		if (pctx != null || g.hitClip((int)x, (int)clip[0], (int)width, clip[1])) {
		    /*if (thresholded) {  // disconnected <= bug #160
			g.setColor(thrFG);
		    }
		    else*/ if (p.isNA()) {
			g.setColor(naFG);
		    }
		    else if (graphicSetter != null) {
			graphicSetter.setGraphics(g, p, profile);
		    }
		    else {
			g.setColor(getColor(gnl));
		    }

		    if (thresholded) {
			fr.curie.vamp.StandardDataSetDisplayer.drawThresholded(g, x, y);
		    }
		    else {
			if (point_mode || p.isNA()) {
			    g.fillRect((int)x, (int)y, (int)width, HEIGHT);
			}
			else {
			    g.drawLine((int)x, (int)yy0, (int)x, (int)y);
			}
		    }
		    probe_disp_cnt++;
		    optimEngine.epilogue(optimCtx, (int)x, (int)y, (int)width, HEIGHT);
		}
	    }
	}

	if ((mode & CENTERED_MODE) != 0) {
	    g.setColor(Color.BLACK);
	    double x1 = scale.getBetaX();
	    double x2 = scale.getBetaX() + scale.getWidth(profile.getVBounds().width);
	    if (pctx != null) {
		x1 = pctx.getRX(x1);
		x2 = pctx.getRX(x2);
	    }
	    // g.drawLine((int)scale.getBetaX(), (int)y0, (int)(scale.getBetaX() + scale.getWidth(profile.getVBounds().width)), (int)y0);
	    g.drawLine((int)x1, (int)yy0, (int)x2, (int)yy0);
	}


	optimEngine.setLevel(olevel);

	probe_loaded_cnt += profile.getLoadedProbeCount() - loaded_probe_cnt;
	if (GraphCanvas.VERBOSE) {
	    long ms2 = System.currentTimeMillis();
	    System.out.println("Optim level #" + optimEngine.getLevel());
	    System.out.println("Scale " + scale.getAlphaX() + " " + scale.getAlphaY());
	    System.out.println("Probes [" + first + " : " + probe_cnt + "]");
	    System.out.println("Probes scanned " + probe_scanned_cnt);
	    System.out.println("Probes loaded " + probe_loaded_cnt);
	    System.out.println("Probes skipped " + probe_skipped_cnt);
	    System.out.println("Probes displayed " + probe_disp_cnt);
	    System.err.println("[" + (ms2 - ms) + " ms]\n");
	}
    }

    private boolean isVisibleX(int x, int width, Dimension size) {
	return size == null || (x >= 0 && x <= size.width) || (x+width >= 0 && x+width <= size.width) || (0 >= x && 0 <= x+width) ;
    }

    private boolean _isVisibleY(int y, Dimension size) {
	return size == null || (y >= 0 && y <= size.height);
    }

    private boolean _isVisibleY(int y, int height, Dimension size) {
	return size == null || (y >= 0 && y <= size.height) || (y+height >= 0 && y+height <= size.height) || (y >= size.height && y+height <= size.height);
    }

    private int [] getBarplotClip(double y0, double y) {
	if (y < y0) {
	    gclip[0] = (int)y;
	    gclip[1] = (int)(y0 - y);
	}
	else {
	    gclip[0] = (int)y0;
	    gclip[1] = (int)(y - y0);
	}
	
	return gclip;
    }

    private int [] getClip(Probe p, double y0, double y) {
	if ((mode & POINT_MODE) != 0 || p.isNA()) {
	    gclip[0] = (int)y;
	    gclip[1] = HEIGHT;
	    return gclip;
	}

	return getBarplotClip(y0, y);
    }

    private boolean isVisibleY(double y0, double y, Dimension size) {
	if ((mode & POINT_MODE) != 0) {
	    return _isVisibleY((int)y, size);
	}

	int clip[] = getBarplotClip(y0, y);

	boolean r = _isVisibleY(clip[0], clip[1], size);
	if (!r) {
	    if (GraphCanvas.VERBOSE) {
		System.out.println("Y barplot not visible: " + y0 + ", " + y + ", " + size);
	    }
	}
	return r;
    }

    private Color getColor(int gnl) {
	if (gnl == -1) {
	    return Color.GREEN;
	}

	if (gnl == 1) {
	    return Color.RED;
	}

	if (gnl == 2) {
	    return Color.BLUE;
	}

	if (gnl == 0) {
	    return Color.YELLOW;
	}

	return Color.BLACK;
    }

    public Probe getProbeAt(int x, int y, Scale scale) {
	try {
	    Profile profile = graphicProfile.getProfile();
	    if (!profile.isFullImported()) {
		return null;
	    }
	
	    GetProbe gp = new GetProbe(profile, scale, x, y);

	    int np = gp.find();
	    if (np >= 0) {
		return profile.getProbe(np, true);
	    }
	}
	catch(Exception e) {
	}

	return null;
    }

    private class FirstVisible extends Dichotomic {

	Profile profile;
	Scale scale;

	FirstVisible(Profile profile, Scale scale) {
	    super(0, profile.getProbeCount() - 1);
	    this.profile = profile;
	    this.scale = scale;
	}

	public int getValue(int n) {
	    try {
		Probe p = profile.getProbeUnmasked(n);
		return (int)scale.getX(p.getPanGenPos(profile));
		//return (int)scale.getX(p.getPanGenPos(profile) + p.getSize());
	    }
	    catch(Exception e) {
		e.printStackTrace();
	    }
	    return -1;
	}
    }

    private class FirstInMap extends Dichotomic {

	int dmap[];
	int first;

	FirstInMap(int dmap[], int first) {
	    super(0, dmap.length - 1);
	    this.dmap = dmap;
	    this.first = first;
	}

	public int getValue(int n) {
	    return dmap[n] - first;
	}
    }

    private class GetProbe extends Dichotomic {

	Profile profile;
	Scale scale;
	int x, y;

	GetProbe(Profile profile, Scale scale, int x, int y) {
	    super(0, profile.getProbeCount() - 1, Dichotomic.CLOSER);
	    this.profile = profile;
	    this.scale = scale;
	    this.x = x;
	    this.y = y;
	}

	public int getValue(int n) {
	    try {
		Probe p = profile.getProbeUnmasked(n);
		return (int)(scale.getX(p.getPanGenPos(profile)) - x);
	    }
	    catch(Exception e) {
		e.printStackTrace();
	    }
	    return -1;
	}

	static final int OK = 1;
	static final int SKIP = 2;
	static final int CONTINUE = 3;

	private int isAcceptable(int m) {
	    try {
		Probe p = profile.getProbe(m);
		double diff = y - scale.getY(p.getPosY(profile));
		if (diff < 0) {
		    diff = -diff;
		}

		if (diff < OVER_HEIGHT) {
		    return OK;
		}
	    
		double vx = scale.getX(p.getPanGenPos(profile)) - x;

		if (vx < 0) {
		    vx = -vx;
		}
	    
		if (vx > OVER_WIDTH) {
		    return SKIP;
		}

		return CONTINUE;
	    }
	    catch(Exception e) {
		return SKIP;
	    }
	}

	public int getClosest(int n) {
	    for (int m = n; m >= 0; m--) {
		int is = isAcceptable(m);
		if (is == OK) {
		    //System.out.println("closest of " + n + " is " + m);
		    return m;
		}

		if (is == SKIP) {
		    break;
		}
	    }

	    int probe_cnt = profile.getProbeCount();

	    for (int m = n; m < probe_cnt; m++) {
		int is = isAcceptable(m);
		if (is == OK) {
		    //System.out.println("closest of " + n + " is " + m);
		    return m;
		}

		if (is == SKIP) {
		    break;
		}
	    }

	    //System.out.println("not found");
	    return getMin() - 1;
	}
    }

    public fr.curie.vamp.gui.Scale makeScale(GraphCanvas canvas, Profile profile) {
   	fr.curie.vamp.Scale scale = canvas.getScale();

	double x0 = canvas.getRX(0, 0);
	double y0 = canvas.getRY(profile.getVBounds().y);

	// a little bit heuristic...
	//double y0 = VAMPUtils.isLogScale(profile) ? canvas.getRY(profile.getVBounds().y - profile.getVBounds().height/2) : canvas.getRY(profile.getVBounds().y);
	//double y0 = canvas.getRY(profile.getVBounds().y - profile.getVBounds().height);

	//System.out.println("making scale " + profile.getID() + " y=" + profile.getVBounds().y + ", h=" + profile.getVBounds().height);
	//fr.curie.vamp.gui.Scale new_scale = new fr.curie.vamp.gui.Scale(scale.getScaleX(), (int)x0, scale.getScaleY(), (int)y0);
	//System.out.println("make_scale: " + x0 + ", " + y0 + ", " + new_scale.getY(0));
	//System.out.println("MINY: " + canvas.getMinY());
	fr.curie.vamp.gui.Scale new_scale = new fr.curie.vamp.gui.Scale(scale.getScaleX(), (int)x0, scale.getScaleY(), (int)canvas.getOrig().y, profile.getVBounds().y + canvas.getMinY(), y0);
	return new_scale;
    }
}
