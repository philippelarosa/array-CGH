
package fr.curie.vamp.gui.optim;

import fr.curie.vamp.data.*;

public class GraphicProfile {

    private Profile profile;
    private GraphicInfo graphicInfo;

    public GraphicProfile(Profile profile) throws Exception {
	this.profile = profile;
	if (profile.isFullImported()) {
	    this.graphicInfo = GraphicInfo.read(profile);
	}
	else {
	    this.graphicInfo = null;
	}
	profile.setGraphicProfile(this);
    }

    public GraphicProfile(Profile profile, GraphicInfo graphicInfo) {
	this.profile = profile;
	/*
	if (graphicInfo == null) {
	    graphicInfo = new GraphicInfo(profile);
	}
	*/
	this.graphicInfo = graphicInfo;
	profile.setGraphicProfile(this);
    }

    public Profile getProfile() {
	return profile;
    }

    public GraphicInfo getGraphicInfo() {
	return graphicInfo;
    }

    public void release() {
	graphicInfo = null;
    }

    public void restore() {
	if (profile.isFullImported() && graphicInfo == null) {
	    try {
		graphicInfo = GraphicInfo.read(profile);
	    }
	    catch(Exception e) {
		e.printStackTrace();
	    }
	}
    }
}
