
/*
 *
 * ResourceItemList.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class ResourceItemList {
    HashMap map;

    ResourceItemList() {
	map = new HashMap();
    }

    void add(String name, ResourceBuilder builder, Object defval) {
	add(name, false, builder, defval);
    }

    void add(String name, boolean is_param,
	     ResourceBuilder builder, Object defval) {
	ResourceItem resItem = new ResourceItem(name, is_param,
						builder, defval);
	map.put(name, resItem);
    }

    ResourceItem get(String name) {
	return (ResourceItem)map.get(name);
    }

    HashMap getMap() {return map;}
}
