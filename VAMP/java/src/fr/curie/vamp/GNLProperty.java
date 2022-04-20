
/*
 *
 * GNLProperty.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;

public class GNLProperty extends Property {

    boolean active;

    public GNLProperty() {
	super("Gnl",
	      new PropertyChoiceType("gnl",
				     new String[]{
					 "-1",
					 "0",
					 "1",
					 "2",
					 VAMPProperties.NA}),
	      INFOABLE|EDITABLE|SERIALIZABLE);
	active = false;
    }

    static int getGNL(String propVal) {
	if (propVal.equals(VAMPProperties.NA))
	    return VAMPConstants.CLONE_NA;
	return Utils.parseInt((String)propVal);
    }

    public void setGraphics(Graphics2D g, Object value, PropertyElement item,
			    GraphElement graphElement) {
	if (!active || g == null) return;
	//Object propVal = item.getPropertyValue(this);
	Object propVal = value;
	if (propVal != null) {
	    if (propVal.equals(VAMPProperties.NA))
		g.setColor(VAMPResources.getColor
			   (VAMPResources.CLONE_NA_FG));
	    else {
		int val = Utils.parseInt((String)propVal);
		if (val == VAMPConstants.CLONE_LOST)
		    g.setColor(VAMPResources.getColor
			       (VAMPResources.GNL_LOST_FG));
		else if (val == VAMPConstants.CLONE_NORMAL)
		    g.setColor(VAMPResources.getColor
			       (VAMPResources.GNL_NORMAL_FG));
		else if (val == VAMPConstants.CLONE_GAINED)
		    g.setColor(VAMPResources.getColor
			       (VAMPResources.GNL_GAINED_FG));
		else if (val == VAMPConstants.CLONE_AMPLICON)
		    g.setColor(VAMPResources.getColor
			       (VAMPResources.GNL_AMPLICON_FG));
		else if (val == VAMPConstants.CLONE_UNKNOWN)
		    g.setColor(VAMPResources.getColor
			       (VAMPResources.GNL_UNKNOWN_FG));
	    }
	}
    }

    boolean isActive() {return active;}
    void setActive(boolean active) {this.active = active;}
}

