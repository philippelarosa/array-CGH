package fr.curie.vamp.gui.optim;

import fr.curie.vamp.gui.Scale;
import fr.curie.vamp.data.*;
import java.util.*;

public class OptimEngine {

    private int level;
    static final int MIN_PMAP_WIDTH = 6;
    static final int MAP_LEN = 2 * MIN_PMAP_WIDTH;

    public static final int NO_OPTIM_LEVEL = 0;
    public static final int EFFICIENT_STANDARD_LEVEL = 1;
    public static final int ACCURATE_STANDARD_LEVEL = 2;
    public static final int EFFICIENT_STANDARD_SKIP_HEIGHT_LEVEL = 3;
    public static final int ACCURATE_STANDARD_SKIP_HEIGHT_LEVEL = 4;
    public static final int USE_GRAPHIC_INFO_1_LEVEL = 5;
    public static final int USE_GRAPHIC_INFO_2_LEVEL = 6;
    public static final int LEVEL_COUNT = 7;

    //    public static final int DEFAULT_LEVEL = EFFICIENT_STANDARD_LEVEL;
    public static final int DEFAULT_LEVEL = EFFICIENT_STANDARD_SKIP_HEIGHT_LEVEL;

    public OptimEngine() {
	this(DEFAULT_LEVEL);
    }

    public OptimEngine(int level) {
	setLevel(level);
    }

    public void setLevel(int level) {
	if (level < LEVEL_COUNT) {
	    this.level = level;
	    return;
	}
	System.err.println("Unknown optim level #" + level);
    }

    public int getLevel() {
	return level;
    }

    public int prologue(OptimContext ctx, GraphicInfo graphicInfo, Scale scale, boolean create) {
	if (create || level >= USE_GRAPHIC_INFO_1_LEVEL) {
	    ctx.graphicInfo = graphicInfo;
	    if (ctx.graphicInfo == null) {
		return NO_OPTIM_LEVEL;
	    }
	    ctx.indScale = ctx.graphicInfo.getIndScale(scale.getAlphaX(), scale.getAlphaY(), create);
	    if (ctx.indScale < 0) {
		return DEFAULT_LEVEL;
	    }
	}

	return level;
    }

    public boolean couldSkip(OptimContext ctx, int probe_num) {
	if (level >= USE_GRAPHIC_INFO_1_LEVEL) {
	    if (ctx.indScale < 0) {
		return false;
	    }

	    return !ctx.graphicInfo.isVisible(ctx.indScale, probe_num);
	}
	return false;
    }

    public boolean couldSkip(OptimContext ctx, int x, int y, int width, int height, int probe_num) {
	if (level == NO_OPTIM_LEVEL) {
	    return false;
	}

	if (level >= USE_GRAPHIC_INFO_1_LEVEL) {
	    if (ctx.indScale < 0) {
		return false;
	    }

	    return !ctx.graphicInfo.isVisible(ctx.indScale, probe_num);
	}

	if (level < EFFICIENT_STANDARD_SKIP_HEIGHT_LEVEL) {
	    height = 1;
	}

	int map_len = ctx.map_len;
	if (x >= ctx.last_x + map_len - MIN_PMAP_WIDTH) {
	    int from_x = x - MIN_PMAP_WIDTH + 1;
	    if ((level == ACCURATE_STANDARD_LEVEL || level == ACCURATE_STANDARD_SKIP_HEIGHT_LEVEL) && from_x >= ctx.last_x && from_x < ctx.last_x + map_len) {
		int incr = from_x - ctx.last_x;
		for (int jj = 0; jj+incr < map_len; jj++) {
		    ctx.draw_map[jj] = ctx.draw_map[jj+incr];
		}
		for (int jj = map_len - incr; jj < map_len; jj++) {
		    ctx.draw_map[jj] = new HashMap();
		}
		ctx.last_x += incr;
		assert ctx.last_x >= 0;
	    }
	    else {
		for (int jj = 0; jj < map_len; jj++) {
		    ctx.draw_map[jj] = new HashMap();
		}
		ctx.last_x = x;
		return false;
	    }
	}

	for (int jj = 0; jj < map_len; jj++) {
	    int map_x = ctx.last_x + jj;
	    for (int xx = x; xx < x+width; xx++) {
		if (map_x == xx) {
		    for (int yy = y; yy < y+height; yy++) {
			Integer w = ctx.draw_map[jj].get(yy);
			if (w != null && w.intValue() >= (width - (xx - x))) {
			    return true;
			}
		    }
		}
		else if (xx > map_x) {
		    break;
		}
	    }
	}

	return false;
    }

    public void compile(OptimContext ctx) {
	ctx.graphicInfo.compile(ctx.indScale);
    }

    public int[] getDirectMap(OptimContext ctx) {
	if (level >= USE_GRAPHIC_INFO_2_LEVEL) {
	    return ctx.graphicInfo.getDirectMap(ctx.indScale);
	}
	return null;
    }

    public void epilogue(OptimContext ctx, int x, int y, int width, int height) {

	if (level == NO_OPTIM_LEVEL || level >= USE_GRAPHIC_INFO_1_LEVEL) {
	    return;
	}

	if (level < EFFICIENT_STANDARD_SKIP_HEIGHT_LEVEL) {
	    height = 1;
	}

	for (int jj = 0; jj < ctx.map_len; jj++) {
	    int map_x = ctx.last_x + jj;
	    for (int xx = x; xx < x+width; xx++) {
		if (map_x == xx) {
		    for (int yy = y; yy < y+height; yy++) {
			Integer w = ctx.draw_map[jj].get(yy);
			if (w == null ||
			    w.intValue() < width - (xx - x))
			    ctx.draw_map[jj].put(yy, width - (xx - x));
		    }
		}
		else if (xx > map_x) {
		    break;
		}
	    }
	}
    }

    public static void register(Profile profile, GraphicInfo graphicInfo, double alpha_x, double alpha_y, int min_width, int height) throws Exception {

	OptimEngine optimEngine = new OptimEngine(DEFAULT_LEVEL);
	Scale scale = new Scale(alpha_x, 0, alpha_y, 0);

	OptimContext optimCtx = new OptimContext();
	optimEngine.prologue(optimCtx, graphicInfo, scale, true);

	int loaded_probe_cnt = profile.getLoadedProbeCount();
	int probe_cnt = profile.getProbeCount();

	for (int n = 0; n < probe_cnt; n++) {
	    
	    Probe p = profile.getProbe(n);
	    double x = scale.getX(p.getPanGenPos(profile));
	    
	    double width = scale.getWidth(p.getSize());
	    if (width < min_width) {
		width = min_width;
	    }
	    
	    double y = scale.getY(p.getPosY(profile));
	    
	    boolean couldSkip = optimEngine.couldSkip(optimCtx, (int)x, (int)y, (int)width, height, n);

	    optimCtx.graphicInfo.setProbeInfo(optimCtx.getIndScale(), n, !couldSkip);

	    if (!couldSkip) {
		optimEngine.epilogue(optimCtx, (int)x, (int)y, (int)width, height);
	    }
	}
	
	optimEngine.compile(optimCtx);

	int probe_loaded_cnt = profile.getLoadedProbeCount() - loaded_probe_cnt;
	/*
	System.out.println(profile.getName() +
			   " registered for scale alphaX=" +
			   alpha_x + ", alphaY=" + alpha_y + ", probes loaded " + probe_loaded_cnt + "/" + probe_cnt);
	*/
    }
}
