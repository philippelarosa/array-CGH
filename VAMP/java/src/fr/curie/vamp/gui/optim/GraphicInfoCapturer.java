
package fr.curie.vamp.gui.optim;

import fr.curie.vamp.data.*;
import java.util.*;
import java.io.*;

public class GraphicInfoCapturer {

    /*
    private static final double alpha_x[] = new double[]{2.5E-7, 5E-7, 10E-7, 50E-7, 200E-7};
    private static final double alpha_y[] = new double[]{3, 8, 20, 50, 100, 250};
    */

    // 13/03/08: I think these scales are better (but not yet tried)
    private static final double alpha_x[] = new double[]{2.6E-7, 5E-7, 10E-7, 50E-7, 200E-7};
    private static final double alpha_y[] = new double[]{1.8, 3, 8, 20, 80, 150};

    private final int PROBE_MIN_WIDTH = 2;
    private final int PROBE_HEIGHT = 2;

    private GraphicProfile graphicProfile;

    public GraphicInfoCapturer(Profile profile) {
	this(profile, profile.getName());
    }

    public GraphicInfoCapturer(Profile profile, String file) {
	GraphicInfo graphicInfo = new GraphicInfo(profile, file);
	graphicProfile = new GraphicProfile(profile, graphicInfo);
    }

    public void captureAndWrite() throws Exception {
	capture();
	graphicProfile.getGraphicInfo().write();
    }

    public void capture() throws Exception {
	Profile profile = graphicProfile.getProfile();
	GraphicInfo graphicInfo = graphicProfile.getGraphicInfo();
	for (int i = 0; i < alpha_x.length; i++) {
	    for (int j = 0; j < alpha_y.length; j++) {
		OptimEngine.register(profile, graphicInfo,
				     alpha_x[i], alpha_y[j],
				     PROBE_MIN_WIDTH, PROBE_HEIGHT);
	    }
	    
	}
    }

    public Profile getProfile() {
	return graphicProfile.getProfile();
    }

    public GraphicInfo getGraphicInfo() {
	return graphicProfile.getGraphicInfo();
    }
}

