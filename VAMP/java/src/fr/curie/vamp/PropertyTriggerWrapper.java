
/*
 *
 * PropertyTriggerWrapper.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

abstract class PropertyTriggerWrapper implements PropertyTrigger {


    public Object add_before(Property prop, PropertyElement elem,
			     Object value) {
	return value;
    }

    public void add_after(Property prop, PropertyElement elem, Object value) { }
    public void remove_before(Property prop, PropertyElement elem) {}
    public void remove_after(Property prop, PropertyElement elem) {}

    public Object get_property_value(Property prop, PropertyElement elem) {
	return elem.getPropertyValue_r(prop);
    }
}
