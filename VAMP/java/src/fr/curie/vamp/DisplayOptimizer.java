
/*
 *
 * DisplayOptimizer.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2006
 *
 */

package fr.curie.vamp;

import java.util.*;

class DisplayOptimizer {
    
    static final Boolean TRUE = new Boolean(true);

    int map_lastrx;
    int already_drawn_cnt;
    int drawn_cnt;
    TreeMap map = null;

    DisplayOptimizer() {
	map_lastrx = Integer.MAX_VALUE;
	drawn_cnt = 0;
	already_drawn_cnt = 0;
    }

    boolean alreadyDrawn(int rx, int ry, int rsize) {
	if (!GraphCanvas.DSP_MAP) {
	    drawn_cnt++;
	    return false;
	}

	Item s = new Item(rx, ry, rsize);

	if (map_lastrx == rx) {
	    if (map.get(s) != null) {
		already_drawn_cnt++;
		return true;
	    }
	}
	else {
	    map_lastrx = rx;
	    map = new TreeMap(new ItemComparator());
	}

	map.put(s, TRUE);
	drawn_cnt++;
	return false;
    }

    int getDrawnCount() {return drawn_cnt;}
    int getAlreadyDrawnCount() {return already_drawn_cnt;}

    static class Item {
	int rx, ry, rsize;
	Item(int rx, int ry, int rsize) {
	    this.rx = rx;
	    this.ry = ry;
	    this.rsize = rsize;
	}
    }

    static class ItemComparator implements Comparator {

	public int compare(Object o1, Object o2) {
	    Item s1 = (Item)o1;
	    Item s2 = (Item)o2;
	    if (s1.rx < s2.rx)
		return -1;
	    if (s1.rx > s2.rx)
		return 1;
	    if (s1.ry < s2.ry)
		return -1;
	    if (s1.ry > s2.ry)
		return 1;

	    if (s1.rsize < s2.rsize)
		return -1;
	    if (s1.rsize > s2.rsize)
		return 1;

	    return 0;
	}
    }
}
