
package fr.curie.vamp.tools;

import fr.curie.vamp.data.*;
import fr.curie.vamp.properties.*;
import fr.curie.vamp.data.serial.*;
import fr.curie.vamp.utils.*;
import fr.curie.vamp.gui.*;
import fr.curie.vamp.gui.optim.*;

import java.io.*;

class ComputeGraphicInfo {

    private static void usage() {
	System.err.println("usage: ComputeGraphicInfo native|portable PROFILES");
	System.exit(1);
    }

    public static void main(String args[]) {

	if (args.length < 1) {
	    usage();
	}

	try {
	    new ComputeGraphicInfo(args);
	}
	catch(Exception e) {
	    e.printStackTrace();
	}
    }

    static boolean VERBOSE = false;

    ComputeGraphicInfo(String names[]) throws Exception {

	for (int m = 0; m < names.length; m++) {

	    String profNames[] = names[m].split(":");

	    for (int n = 0; n < profNames.length; n++) {
		try {
		    ProfileUnserializer unserialProf = ProfileSerializerFactory.getInstance().getUnserializer(profNames[n]);
		
		    Profile profile = unserialProf.readProfile();
		    if (VERBOSE) {
			System.out.println("Capturing graphic info for " + profile.getName());
		    }
	
		    //profile.setUnserializingPolicy(Profile.NONE);
		    profile.setUnserializingPolicy(Profile.CACHE_PROBES);
	
		    GraphicInfoCapturer capturer = new GraphicInfoCapturer(profile, profNames[n]);
		    capturer.captureAndWrite();
		}
		catch(Exception e) {
		    e.printStackTrace();
		}
	    }
	}
    }
}
