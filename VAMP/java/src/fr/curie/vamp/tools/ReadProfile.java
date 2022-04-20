
package fr.curie.vamp.tools;

import fr.curie.vamp.data.*;
import fr.curie.vamp.properties.*;
import fr.curie.vamp.data.serial.*;
import fr.curie.vamp.utils.*;
import fr.curie.vamp.gui.optim.*;

import java.io.*;

class ReadProfile {

    private static void usage() {
	System.err.println("usage: ReadProfile [-gri] [-probes] NAME");
	System.exit(1);
    }

    public static void main(String args[]) {

	//fr.curie.vamp.utils.serial.SerialUtils.test();

	if (args.length < 1) {
	    usage();
	}

	String file = null;
	boolean display_gri = false, display_probes = false;

	for (int n = 0; n < args.length; n++) {
	    if (args[n].equals("-gri")) {
		display_gri = true;
	    }
	    else if (args[n].equalsIgnoreCase("-probes")) {
		display_probes = true;
	    }
	    else if (args[n].charAt(0) == '-') {
		usage();
	    }
	    else if (file != null) {
		usage();
	    }
	    else {
		file = args[n];
	    }
	}

	try {
	    new ReadProfile(file, display_gri, display_probes);
	}
	catch(Exception e) {
	    //System.err.println(e);
	    e.printStackTrace();
	}
    }

    ReadProfile(String name, boolean display_gri, boolean display_probes) throws Exception {

	ProfileUnserializer unserialProf = ProfileSerializerFactory.getInstance().getUnserializer(name);
	
	System.out.println("\n                       <<<<< " + ((unserialProf instanceof ProfilePortableUnserializer) ? "Portable" : "Native Java") + " Format >>>>>\n");

	Profile profile = unserialProf.readProfile();
	
	profile.setUnserializingPolicy(Profile.NONE);
	
	profile.print();
	
	if (display_gri) {
	    GraphicProfile graphicProfile = new GraphicProfile(profile);
	    System.out.println("\n" + graphicProfile.getGraphicInfo().toString("  "));
	}

	if (display_probes) {
	    int probe_cnt = profile.getProbeCount();
	    for (int n = 0; n < probe_cnt; n++) {
		Probe p = profile.getProbe(n, true);
		if ((n % 10000) == 0 || n == probe_cnt-1) {
		    System.out.println(n + " probes");
		}
		p.print(n);
	    }
	}

    }
}
