
/*
 *
 * PropertyTrigger.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

interface PropertyTrigger {


    Object add_before(Property prop, PropertyElement elem, Object value);

    void add_after(Property prop, PropertyElement elem, Object value);
    void remove_before(Property prop, PropertyElement elem);
    void remove_after(Property prop, PropertyElement elem);

    Object get_property_value(Property prop, PropertyElement elem);
}
