
/*
 *
 * GraphElementCache.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2006
 *
 */

package fr.curie.vamp;

import java.util.*;

class GraphElementCache {

    private static GraphElementCache instance;
    private HashMap map;
    private static final boolean TRACE = false;

    synchronized static GraphElementCache getInstance() {
	if (instance == null)
	    instance = new GraphElementCache();
	return instance;
    }

    GraphElementCache() {
	map = new HashMap();
    }

    synchronized GraphElement get(String url) {
	if (TRACE) {
	    GraphElement graphElem = (GraphElement)map.get(url);
	    System.out.println("getting " + url + " -> " + 
			       (graphElem == null ? "null" : VAMPUtils.getChr(graphElem)));
	}
	return (GraphElement)map.get(url);
    }

    synchronized void put(String url, GraphElement graphElem) {
	if (TRACE)
	    System.out.println("putting " + url + "/" + VAMPUtils.getChr(graphElem) + " in cache [" + map.size() + "]");
	if (graphElem.isFullImported())
	    map.put(url, graphElem);
    }

    synchronized void remove(String url) {
	if (get(url) == null) {
	    System.out.println("*warning* " + url + " not in cache");
	}
	else if (TRACE)

	    System.out.println("removing " + url + "/" + VAMPUtils.getChr(get(url)) + " from cache");
	map.remove(url);
    }

    public HashMap getMap() {
	return map;
    }
}

