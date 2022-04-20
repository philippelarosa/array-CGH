
/*
 *
 * PropertyManager.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

class PropertyManager {

    HashMap propListMap;
    static PropertyManager instance;

    static PropertyManager getInstance() {
	if (instance == null)
	    instance = new PropertyManager();
	return instance;
    }
    
    PropertyManager() {
	propListMap = new HashMap();
    }

    void setPropertyList(String type, Vector propList) {
	propListMap.put(type, propList);
    }

    // Vector of String
    Vector getPropertyList(String type) {
	return (Vector)propListMap.get(type);
    }

    // Vector of Property
    Vector makePropertyList(String type, TreeMap properties) {

	Vector propList = getPropertyList(type);
	Vector ordPropList = new Vector();

	Iterator it = properties.entrySet().iterator();
	if (propList == null) {
	    while (it.hasNext()) {
		Map.Entry entry = (Map.Entry)it.next();
		Property prop = (Property)entry.getKey();
		ordPropList.add(prop);
	    }
	    return ordPropList;
	}

	int size = propList.size();
	for (int n = 0; n < size; n++) {
	    String propName = (String)propList.get(n);
	    Property prop = Property.getProperty(propName);
	    if (properties.get(prop) != null)
		ordPropList.add(prop);
	}

	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    Property prop = (Property)entry.getKey();
	    if (ordPropList.indexOf(prop) == -1)
		ordPropList.add(prop);
	}

	return ordPropList;
    }
}

