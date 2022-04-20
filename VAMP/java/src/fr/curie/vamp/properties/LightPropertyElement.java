
package fr.curie.vamp.properties;

import fr.curie.vamp.Property;
import java.util.*;

public class LightPropertyElement {

    protected HashMap<Property, Object> map;

    protected LightPropertyElement() {
    }

    public void addProp(Property key, Object value) {
	if (map == null)
	    map = new HashMap();
	map.put(key, value);
    }

    public void rmvProp(Property key) {
	if (map != null) {
	    map.remove(key);
	}
    }

    public Object getProp(Property key) {
	return map == null ? null : map.get(key);
    }

    public void printMap(String indent) {
	if (map == null) {
	    System.out.println(indent + "<Null Map>");
	    return;
	}

	Iterator it = map.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry<Property, Object> entry = (Map.Entry)it.next();
	    System.out.println(indent + entry.getKey().getName() + ": " + entry.getValue());
	}
    }

    public HashMap<Property, Object> getPropMap() {
	return map;
    }

    public void setPropMap(HashMap<Property, Object> map) {
	this.map = map;
    }

    public void removePropMap() {
	map = null;
    }

    public void release() {
	map = null;
    }
}

