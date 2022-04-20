/*
 *
 * Property.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class Property extends PropertyElement implements Comparable {

    private String name;
    private boolean isID = false;
    static final String MASK_SUFFIX = "_mask";

    private PropertyType type;

    static boolean NEW_PROP_MEM_POLICY = true;

    public static final int FINDABLE = 0x1;
    public static final int INFOABLE = 0x2;
    public static final int EDITABLE = 0x4;
    public static final int SERIALIZABLE = 0x8;
    public static final int TEMPORARY = 0x10;
    public static final int SHARED = (NEW_PROP_MEM_POLICY ? 0x20 : 0);

    private int flags;

    protected Property(String name, int flags) {
	this(name, PropertyStringType.getInstance(), flags);
    }

    protected Property(String name, PropertyType type, int flags) {
	this(name, type, flags, false);
    }

    protected Property(String name, PropertyType type,
		       int flags, boolean isID) {
	this.name = name;
	this.type = type;
	this.flags = flags;
	this.isID = isID;
	//System.out.println("adding " + name + " " + this);
	propList.put(name, this);
    }

    private GraphMenuListener menuListener = null;

    private PropertyTrigger trigger = null;

    static TreeMap propList = new TreeMap();

    public static Property getProperty(String name) {
	return getProperty(name, INFOABLE|FINDABLE|SERIALIZABLE, false);
    }

    public static Property getProperty(String name, PropertyType type) {
	return getProperty(name, type, INFOABLE|FINDABLE|SERIALIZABLE, false);
    }

    public static Property getProperty(String name, PropertyType type,
				int flags) {
	return getProperty(name, type, flags, false);
    }

    public static Property getProperty(String name, int flags) {
	return getProperty(name, flags, false);
    }

    public static Property getProperty(String name, boolean isID) {
	return getProperty(name, INFOABLE|FINDABLE|SERIALIZABLE, isID);
    }

    public static Property getHiddenProperty(String name) {
	return getHiddenProperty(name, SERIALIZABLE);
    }

    public static Property getHiddenProperty(String name, int flags) {
	return getProperty(name, PropertyObjectType.getInstance(),
			   flags, false);
    }

    public static String getMaskedPropertyName(Property prop) {
	return prop.getName() + MASK_SUFFIX;
    }

    public static Property getMaskedProperty(Property prop) {
	return getHiddenProperty(getMaskedPropertyName(prop));
    }

    public static Property getProperty(String name,
				PropertyType type,
				int flags, boolean isID) {
	// 14/03/05 kludge ! waiting for SL update
	if (name.equals("HistologicalType_PrincipalComponent"))
	    name = "HistologicalType";

	Property prop = (Property)propList.get(name);
	if (prop == null)
	    prop = new Property(name, type, flags, isID);
	return prop;
    }

    public static boolean existProperty(String name) {
	return propList.get(name) != null;
    }

    public static Property getProperty(String name, int flags, boolean isID) {
	return getProperty(name, PropertyStringType.getInstance(),
			   flags, isID);
    }

    public static Property[] getProperties(String names[]) {
	Property props[] = new Property[names.length];
	for (int n = 0; n < props.length; n++)
	    props[n] = getProperty(names[n]);
	return props;
    }

    String getInfoValue(Object value) {
	return value.toString();
    }

    String getMenuValue(Object value) {
	return "[" + name + "] " + value.toString();
    }

    void setAction(JMenuItem menuItem, Object value) {
	if (menuListener == null) return;
	menuListener.setValue(value);
	menuItem.addActionListener(menuListener);
    }

    void setMenuListener(GraphMenuListener menuListener) {
	this.menuListener = menuListener;
    }

    public void setGraphics(Graphics2D g, Object propValue,
			    PropertyElement item,
			    GraphElement graphElement) {
    }

    public String getName() {return name;}

    public boolean isFindable() {return (flags & FINDABLE) != 0;}
    public boolean isInfoable() {return (flags & INFOABLE) != 0;}
    public boolean isEditable() {return (flags & EDITABLE) != 0;}
    public boolean isSerializable() {return (flags & SERIALIZABLE) != 0;}
    public boolean isTemporary() {return (flags & TEMPORARY) != 0;}

    // SHARED_PROP
    public boolean isShared() {return (flags & SHARED) != 0;}
    //

    public boolean isID() {return isID;}

    public int compareTo(Object o) {
	Property prop = (Property)o;
	return name.compareTo(prop.name);
    }

    double toDouble(PropertyElement elem) {
	String s = (String)elem.getPropertyValue(this);
	if (s.equals(VAMPProperties.NA))
	    return 0;
	return Utils.parseDouble(s);
    }

    int toInt(PropertyElement elem) {
	String s = (String)elem.getPropertyValue(this);
	if (s.equals(VAMPProperties.NA))
	    return 0;
	return Utils.parseInt(s);
    }

    Object getPropertyValue(PropertyElement elem) {
	if (trigger != null)
	    return trigger.get_property_value(this, elem);
	return elem.getPropertyValue_r(this);
    }

    Object addBefore(PropertyElement elem, Object value) {
	if (trigger != null)
	    return trigger.add_before(this, elem, value);
	return value;
    }

    void addAfter(PropertyElement elem, Object value) {
	if (trigger != null)
	    trigger.add_after(this, elem, value);
    }

    void removeBefore(PropertyElement elem) {
	if (trigger != null)
	    trigger.remove_before(this, elem);
    }

    void removeAfter(PropertyElement elem) {
	if (trigger != null)
	    trigger.remove_after(this, elem);
    }

    void setTrigger(PropertyTrigger trigger) {
	this.trigger = trigger;
    }

    PropertyTrigger getTrigger() {
	return trigger;
    }

    PropertyType getType() {return type;}

    // SHARED_PROP
    private static TreeMap shared_map = new TreeMap();

    PropertyElement getSharedElem(Object value) {
	return getSharedElem(value, true);
    }

    PropertyElement getSharedElem(Object value, boolean create) {
	PropertyElement shared = (PropertyElement)shared_map.get(value);
	if (shared == null && create) {
	    shared = new PropertyElement();
	    shared.setIsShared(true);
	    shared_map.put(value, shared);
	}

	return shared;
    }

    static int getSharedMapSize() {return shared_map.size();}

    // 8/3/05 : property annotations
    PropertyAnnot annots[];
    HashMap annot_map = new HashMap();
    boolean annot_hidden = false;
    HashMap annot_hidden_map = new HashMap();

    private static PropertyAnnot[] makeAnnots(Vector v) {
	int sz = v.size();
	if (sz == 0)
	    return null;
	PropertyAnnot annots[] = new PropertyAnnot[sz];
	for (int n = 0; n < sz; n++) {
	    annots[n] = (PropertyAnnot)v.get(n);
	    annots[n].setInd(n);
	}

	return annots;
    }

    void setAnnotations(View view, PropertyAnnot annots[]) {
	setAnnotations_r(view, annots, false);
    }

    void addAnnotations(View view, PropertyAnnot annots[]) {
	setAnnotations_r(view, annots, true);
    }

    private void setAnnotations_r(View view, PropertyAnnot annots[],
				  boolean add) {
	Vector v = new Vector();
	if (add && this.annots != null) {
	    for (int n = 0; n < this.annots.length; n++)
		if (!hasAnnotation(this.annots[n], annots, -1))
		    v.add(this.annots[n]);
	}

	if (annots != null)
	    for (int n = 0; n < annots.length; n++) {
		if (!hasAnnotation(annots[n], annots, n))
		    v.add(annots[n]);
	    }

	annots = makeAnnots(v);

	if (view == null || view.isAnnotGlobal())
	    this.annots = annots;
	else
	    annot_map.put(view, annots);
    }

    PropertyAnnot[] getAnnotations(View view) {
	if (view == null || view.isAnnotGlobal())
	    return annots;
	return (PropertyAnnot[])annot_map.get(view);
    }

    Color getColor(View view, PropertyElement elem) {
	PropertyAnnot annot = getPropertyAnnot(view, elem);
	if (annot == null)
	    return null;

	return annot.getColor(elem);
    }

    PropertyAnnot getPropertyAnnot(View view, PropertyElement elem) {
	PropertyAnnot annots[] = getAnnotations(view);
	if (annots == null)
	    return null;

	for (int n = 0; n < annots.length; n++)
	    if (annots[n].getColor(elem) != null)
		return annots[n];

	return null;
    }

    void setPropertyAnnotHidden(View view, boolean hidden) {
	if (view == null || view.isAnnotGlobal())
	    this.annot_hidden = hidden;
	else
	    annot_hidden_map.put(view, new Boolean(hidden));
    }

    boolean isPropertyAnnotHidden(View view) {
	if (view == null || view.isAnnotGlobal())
	    return annot_hidden;
	Boolean b = (Boolean)annot_hidden_map.get(view);
	return b != null && b.booleanValue();
    }


    boolean isEligible(View view, Property filter_prop) {
	if (isPropertyAnnotHidden(view))
	    return false;

	return filter_prop == null || getPropertyValue(filter_prop) != null;
    }

    static HashSet getAllProperties(GlobalContext globalContext, View view) {
	LinkedList list;

	HashSet propSet = new HashSet();
	if (view == null) {
	    Iterator it = propList.values().iterator();
	    while (it.hasNext())
		propSet.add(it.next());
	    return propSet;
	}

	if (view.isAnnotGlobal()) {
	    LinkedList view_list = View.getViewList(globalContext);
	    list = new LinkedList();
	    int sz = view_list.size();
	    for (int n = 0; n < sz; n++) {
		list.addAll(((View)view_list.get(n)).getGraphPanelSet().getGraphElements(View.ALL));
	    }		
	}
	else
	    list = view.getGraphPanelSet().getGraphElements(View.ALL);

	int sz = list.size();

	for (int n = 0; n < sz; n++) {
	    GraphElement graphElem = (GraphElement)list.get(n);
	    TreeMap properties = graphElem.getProperties();
	    Iterator it = properties.entrySet().iterator();

	    while (it.hasNext()) {
		Map.Entry entry = (Map.Entry)it.next();
		Property prop = (Property)entry.getKey();
		propSet.add(prop);
	    }
	}

	return propSet;
    }

    static PropertyAnnot[] getAllAnnotations(GlobalContext globalContext,
					     View view) {
	HashSet propSet = getAllProperties(globalContext, view);

	Iterator it = propSet.iterator();

	int cnt = 0;
	Vector v = new Vector();
	while (it.hasNext()) {
	    Property prop = (Property)it.next();
	    PropertyAnnot annots[] = prop.getAnnotations(view);
	    if (annots != null && annots.length > 0) {
		v.add(annots);
		cnt += annots.length;
	    }
	}

	int j = 0;
	PropertyAnnot annots_r[] = new PropertyAnnot[cnt];
	int sz = v.size();
	for (int n = 0; n < sz; n++) {
	    PropertyAnnot annots[] = (PropertyAnnot[])v.get(n);
	    for (int i = 0; i < annots.length; i++)
		annots_r[j++] = annots[i];
	}

	return annots_r;
    }

    private static boolean hasAnnotation(PropertyAnnot annot,
					 PropertyAnnot annots[],
					 int until) {
	for (int n = 0; n < annots.length; n++) {
	    if (until >= 0 && n >= until)
		break;
	    if (annots[n].getOP() == annot.getOP() &&
		annots[n].getValue().equals(annot.getValue()))
		return true;
	}
	
	return false;
    }
}
