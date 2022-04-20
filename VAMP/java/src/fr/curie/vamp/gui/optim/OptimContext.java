package fr.curie.vamp.gui.optim;


import java.util.*;

public class OptimContext {

    private static final int BASE_X = -1000;

    // package level: accessible from OptimEngine
    HashMap<Integer, Integer> draw_map[];
    int map_len, last_x = BASE_X;
    int indScale;
    GraphicInfo graphicInfo;

    public OptimContext() {
	draw_map = new HashMap[OptimEngine.MAP_LEN];
	map_len = draw_map.length;
	for (int n = 0; n < map_len; n++) {
	    draw_map[n] = new HashMap();
	}

	graphicInfo = null;
	indScale = -1;
    }

    public int getIndScale() {
	return indScale;
    }

    public GraphicInfo getGraphicInfo() {
	return graphicInfo;
    }
}
