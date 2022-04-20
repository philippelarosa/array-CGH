
/*
 *
 * ProperyElement.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import fr.curie.vamp.properties.LightPropertyElement;
import java.util.*;
import java.awt.*;

public class PropertyElement {

    static class TMap {
	static private class Item {
	    Property prop;
	    Object obj;

	    Item(Property prop, Object obj) {
		this.prop = prop;
		this.obj = obj;
	    }

	    public Object clone() {
		return new Item(prop, obj);
	    }
	}

	private Item [] items;

	TMap() {
	    items = new Item[0];
	}

	TMap(TreeMap map) {
	    if (map == null) {
		items = new Item[0];
		return;
	    }

	    items = new Item[map.size()];
	    Iterator it = map.entrySet().iterator();
	    for (int n = 0; it.hasNext(); n++) {
		Map.Entry entry = (Map.Entry)it.next();
		Property prop = (Property)entry.getKey();
		items[n] = new Item(prop, entry.getValue());
	    }
	}

	Object get(Property prop) {
	    for (int n = 0; n < items.length; n++)
		if (items[n].prop.compareTo(prop) == 0)
		    return items[n].obj;

	    return null;
	}
	
	Vector getPropertyList() {
	    Vector list = new Vector();
	    for (int n = 0; n < items.length; n++)
		list.add(items[n].prop);
	    return list;
	}

	void put(Property prop, Object obj) {
	    for (int n = 0; n < items.length; n++) {
		if (items[n].prop.compareTo(prop) == 0) {
		    items[n].obj = obj;
		    return;
		}
	    }

	    Item [] nitems = new Item[items.length+1];
	    for (int n = 0; n < items.length; n++)
		nitems[n] = (Item)items[n].clone();
	    nitems[items.length] = new Item(prop, obj);
	    items = nitems;
	    nitems = null;
	}

	boolean remove(Property prop) {
	    if (get(prop) == null)
		return false;

	    Item nitems[] = new Item[items.length-1];
	    for (int n = 0; n < items.length; n++) {
		if (items[n].prop.compareTo(prop) != 0)
		    nitems[n] = items[n];
		else {
		    for (int m = n; m < items.length-1; m++)
			nitems[m] = items[m+1];
		    break;
		}
	    }

	    items = nitems;
	    return true;
	}

	int size() {
	    return items.length;
	}

	protected Object clone() {
	    TMap map = new TMap(toTreeMap());
	    return map;
	}

	TreeMap toTreeMap() {
	    TreeMap map = new TreeMap();
	    for (int n = 0; n < items.length; n++)
		map.put(items[n].prop, items[n].obj);
	    return map;
	}
    }

    //    private TreeMap properties;
    private TMap properties;
    private HashSet mod_properties;

    // SHARED_PROP
    private PropertyElement shared;
    //

    static private int prop_elem_alloc_count = 0;
    static private int data_elem_alloc_count = 0;
    static private int graph_elem_alloc_count = 0;
    static private int final_prop_elem_alloc_count = 0;
    static private int final_data_elem_alloc_count = 0;
    static private int final_graph_elem_alloc_count = 0;

    public PropertyElement(TreeMap properties) {
	this.properties = new TMap(properties);

	if (this instanceof DataElement)
	    data_elem_alloc_count++;
	else if (this instanceof GraphElement)
	    graph_elem_alloc_count++;
	else
	    prop_elem_alloc_count++;
    } 

    public PropertyElement() {
	this((TreeMap)null);
    }

    public void convert(LightPropertyElement elem) {
	if (elem.getPropMap() != null) {
	    Iterator it = elem.getPropMap().entrySet().iterator();
	    while (it.hasNext()) {
		Map.Entry<Property, Object> entry = (Map.Entry)it.next();
		if (entry.getKey() == VAMPProperties.PositionProp) {
		    setPropertyValue(entry.getKey(), entry.getValue().toString());
		}
		else {
		    setPropertyValue(entry.getKey(), entry.getValue());
		}
	    }
	}
    }

    /*
    static public TreeMap convert(LightPropertyElement elem) {
	TreeMap tmap = new TreeMap();
	if (elem.getPropMap() != null) {
	    Iterator it = elem.getPropMap().entrySet().iterator();
	    while (it.hasNext()) {
		Map.Entry<Property, Object> entry = (Map.Entry)it.next();
		tmap.put(entry.getKey(), entry.getValue());
	    }
	}
	return tmap;
    }
    */

    public PropertyElement(LightPropertyElement elem) {
	//this(convert(elem));
	convert(elem);
    }

    protected Object clone() throws CloneNotSupportedException {
	PropertyElement propElem = (PropertyElement)super.clone();
	if (this instanceof DataElement)
	    data_elem_alloc_count++;
	else if (this instanceof GraphElement)
	    graph_elem_alloc_count++;
	else
	    prop_elem_alloc_count++;
	return propElem;
    }

    public void cloneProperties(PropertyElement from) {
	cloneProperties(from, true);
    }

    /*
    public void cloneProperties(LightPropertyElement light_from) {
	cloneProperties(new PropertyElement(light_from), true);
    }
    */

    public void cloneProperties(PropertyElement from, boolean copy_mod) {
	properties = (TMap)(from.properties == null ? null :
			    from.properties.clone());
	// SHARED_PROP
        shared = from.shared;
	// ..
	if (copy_mod) {
	    mod_properties = (HashSet)(from.mod_properties == null ? null :
				       from.mod_properties.clone());
	}
    }

    public void setProperties(TreeMap properties) {
	this.properties = new TMap(properties);
    }

    public Object getPropertyValue_r(Property prop) {
	if (properties != null) {
	    return b2s(properties.get(prop));
	}

	return null;
    }

    public Object getPropertyValue(Property prop) {
	if (prop == null) {
	    return null;
	}

	if (prop.isShared() && !isShared() && shared != null) {
	    return shared.getPropertyValue(prop);
	}

	return prop.getPropertyValue(this);
    }

    public void setPropertyValues(Property props[], Object value) {
	for (int n = 0; n < props.length; n++) {
	    setPropertyValue(props[n], value);
	}
    }

    public void setPropertyValue(Property prop, Object value) {
	setPropertyValue(prop, value, true);
    }

    public void setPropertyValue(Property prop, Object value, boolean mod) {

	//checkPropValue(prop, value);

	setModProperties(prop, value, mod);

	value = prop.addBefore(this, value);
	if (properties == null)
	    properties = new TMap();
	if (value instanceof String)
	    properties.put(prop, ((String)value).getBytes());
	else
	    properties.put(prop, value);

	prop.addAfter(this, value);
    }

    protected void checkPropValue(Property prop, Object value) {
	if (value != null && !prop.getType().checkValue(value))
	    System.err.println("property " + prop.getName() + " value " +
			       value + " is incorrect");
    }

    protected void setModProperties(Property prop, Object value, boolean mod) {
	if (!mod || !prop.isEditable())
	    return;

	// if prop is already set AND prop is editable then add it in
	// the list of modified properties
	Object ovalue = getPropertyValue(prop);
	if (ovalue != null && !ovalue.equals(value)) {
	    if (mod_properties == null)
		mod_properties = new HashSet();
	    //System.out.println("mod " + prop.getName());
	    mod_properties.add(prop);
	}
    }

    public boolean removeProperty(Property prop) {
	if (prop.isShared()) {
	    System.err.println("trying to remove a shared property " +
			       prop.getName());
	    return false;
	}

	if (properties == null)
	    return false;

	prop.removeBefore(this);
	boolean r = properties.remove(prop);
	prop.removeAfter(this);
	return r;
    }

    public Object getID() {
	TreeMap properties = getProperties();
	Iterator it = properties.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    Property prop = (Property)entry.getKey();
	    if (prop.isID())
		return b2s(entry.getValue());
	}

	return null;
    }

    public void display() {
	display("");
    }

    public void display(String indent) {
	TreeMap properties = getProperties();
	Iterator it = properties.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    Property prop = (Property)entry.getKey();
	    System.out.println(indent + prop + " " + prop.getName() + ": " + entry.getValue());
	}
    }

    //TreeMap getProperties() {return properties;}
    // SHARED_PROP
    static Object b2s(Object value) {
	if (value != null && value instanceof byte[])
	    return new String((byte[])value);
	return value;
    }

    static String b2s(byte value[]) {
	if (value != null)
	    return new String(value);
	return null;
    }

    static TreeMap clone(TreeMap properties) {
	TreeMap treeMap = new TreeMap();
	Iterator it = properties.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    Property prop = (Property)entry.getKey();
	    treeMap.put(prop, b2s(entry.getValue()));
	}

	return treeMap;
    }

    public TreeMap getProperties() {
	if (shouldMergeProperties()) {
	    TreeMap u_properties = new TreeMap();
	    u_properties.putAll(shared.getProperties());
	    if (properties != null) {
		u_properties.putAll(properties.toTreeMap());
	    }
	    return clone(u_properties);
	}
	return clone(properties == null ? new TreeMap() : properties.toTreeMap());
    }

    Vector getPropertyList() {
	Vector list = new Vector();
	if (shouldMergeProperties())
	    list.addAll(shared.getPropertyList());

	if (properties != null)
	    list.addAll(properties.getPropertyList());

	return list;
    }

    protected boolean shouldMergeProperties() {
	return shared != null && !isShared();
    }

    HashSet getModifiedProperties() {return mod_properties;}
    void setModifiedProperties(HashSet mod_properties) {
	this.mod_properties = mod_properties;
    }

    static final String delim = "#";

    String fromTemplate(String template) {
	String rs = "";
	StringTokenizer st = new StringTokenizer(template, delim, true);
	boolean state = false;
	while (st.hasMoreTokens()) {
	    String s = st.nextToken();
	    if (s.equals(delim)) {
		state = !state;
	    }
	    else if (!state)
		rs += s;
	    else {
		Property prop = Property.getProperty(s);
		String val = (String)getPropertyValue(prop);
		if (val == null) {
		    /*
		    System.err.println("ERROR: invalid property name " +
				       s + " in template " + template);
		    */
		    continue;
		}
		rs += val;
	    }
	}

	return rs;
    }

    boolean hasProperty(Vector propV) {
	int size_prop = propV.size();
	for (int n = 0; n < size_prop; n++)
	    if (getPropertyValue((Property)propV.get(n)) != null)
		return true;

	return false;
    }

    public void removeAllProperties() {
	//	properties = new TreeMap();
	properties = null;
    }

    // SHARED_PROP
    private boolean is_shared = false;

    void setIsShared(boolean is_shared) {
	this.is_shared = is_shared;
    }

    boolean isShared() {
	return is_shared;
    }
    // ...

    PropertyElement getSharedElem() {return shared;}
    void setSharedElem(PropertyElement shared) {this.shared = shared;}

    boolean completed = false;
    void setCompleted() {completed = true;}
    boolean isCompleted() {return completed;}

    static final int EQUAL_OP = 1;
    static final int DIFF_OP = 2;
    static final int PATTERN_EQUAL_OP = 3;
    static final int PATTERN_DIFF_OP = 4;
    static final int GE_OP = 5;
    static final int GT_OP = 6;
    static final int LE_OP = 7;
    static final int LT_OP = 8;

    boolean matches(Property prop, int op, Object value) {
	Object ovalue = getPropertyValue(prop);
	String svalue = (ovalue == null ? "" : ovalue.toString());
	if (op == EQUAL_OP)
	    return value.equals(svalue);
	
	if (op == DIFF_OP)
	    return !value.equals(svalue);

	if (op == PATTERN_EQUAL_OP)
	    return ((Pattern)value).matches(svalue);

	if (op == PATTERN_DIFF_OP)
	    return !((Pattern)value).matches(svalue);
								
	if (op == GT_OP || op == GE_OP || op == LT_OP || op == LE_OP) {
	    double dvalue = Double.parseDouble((String)value);
	    double d = Double.parseDouble(svalue);
	    
	    if (op == GT_OP)
		return d > dvalue;
	    if (op == GE_OP)
		return d >= dvalue;
	    if (op == LT_OP)
		return d < dvalue;
	    if (op == LE_OP)
		return d <= dvalue;
	}
	
	return false;
    }

    static int getOP(String sop) {
	if (sop.equals("="))
	    return EQUAL_OP;
	if (sop.equals("!="))
	    return DIFF_OP;
	if (sop.equals("~"))
	    return PATTERN_EQUAL_OP;
	if (sop.equals("!~"))
	    return PATTERN_DIFF_OP;
	if (sop.equals("<"))
	    return LT_OP;
	if (sop.equals("<="))
	    return LE_OP;
	if (sop.equals(">"))
	    return GT_OP;
	if (sop.equals(">="))
	    return GE_OP;

	return 0;
    }

    static String getStringOP(int op) {
	if (op == EQUAL_OP)
	    return "=";
	if (op == DIFF_OP)
	    return "!=";
	if (op == PATTERN_EQUAL_OP)
	    return "~";
	if (op == PATTERN_DIFF_OP)
	    return "!~";
	if (op == LT_OP)
	    return "<";
	if (op == LE_OP)
	    return "<=";
	if (op == GT_OP)
	    return ">";
	if (op == GE_OP)
	    return ">=";

	return null;
    }

    void setCommonProperties(Vector elem_v) {
	int size = elem_v.size();
	if (size == 0)
	    return;

	cloneProperties((PropertyElement)elem_v.get(0));

	for (int i = 1; i < size; i++) {
	    PropertyElement e = (PropertyElement)elem_v.get(i);
	    Iterator it = e.getProperties().entrySet().iterator();
	    while (it.hasNext()) {
		Map.Entry entry = (Map.Entry)it.next();
		Property prop = (Property)entry.getKey();
		Object value = properties.get(prop);
		if (value == null || !b2s(value).equals(entry.getValue())) {
		    properties.remove(prop);
		}
	    }
	}
    }

    protected void finalize() throws Throwable {
	if (this instanceof DataElement) {
	    final_data_elem_alloc_count++;
	    data_elem_alloc_count--;
	}
	else if (this instanceof GraphElement) {
	    final_graph_elem_alloc_count++;
	    graph_elem_alloc_count--;
	}
	else {
	    final_prop_elem_alloc_count++;
	    prop_elem_alloc_count--;
	}

	// to be shure
	properties = null;
	mod_properties = null;
    }

    static void printMap() {
	System.out.println("PropertyElements: " + prop_elem_alloc_count +
			   " [final: " + final_prop_elem_alloc_count + "]");
	System.out.println("GraphElements: " + graph_elem_alloc_count +
			   " [final: " + final_graph_elem_alloc_count + "]");
	System.out.println("DataElements: " + data_elem_alloc_count +
			   " [final: " + final_data_elem_alloc_count + "]");
    }

    public void maskProperty(Property prop) {
	setPropertyValue(Property.getMaskedProperty(prop), new Boolean(true));
    }

    boolean isPropertyMasked(Property prop) {
	if (!Property.existProperty(Property.getMaskedPropertyName(prop)))
	    return false;
	return getPropertyValue(Property.getMaskedProperty(prop)) != null;
    }
}
