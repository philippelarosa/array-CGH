
package fr.curie.vamp.tools;

import fr.curie.vamp.data.*;
import fr.curie.vamp.properties.*;
import fr.curie.vamp.data.serial.*;
import fr.curie.vamp.utils.*;

import java.io.*;

class ReadProfile {

    private static void usage() {
	System.err.println("usage: ReadProfile <name> [cache|none]");
	System.exit(1);
    }

    public static void main(String args[]) {

	if (args.length != 1 && args.length != 2) {
	    usage();
	}

	int unserialPolicy = 0;

	if (args.length == 1) {
	    unserialPolicy = Profile.NONE;
	}
	else if (args[1].equalsIgnoreCase("cache")) {
	    unserialPolicy = Profile.CACHE_PROBES;
	}
	else if (args[1].equalsIgnoreCase("none")) {
	    unserialPolicy = Profile.NONE;
	}
	else {
	    usage();
	}

	try {
	    new ReadProfile(args[0], unserialPolicy);
	}
	catch(Exception e) {
	    e.printStackTrace();
	}
    }

    static final int MAX_PROFILE_CNT = 1;

    ReadProfile(String name, int unserialPolicy) throws Exception {

	ProfileUnserializer unserialProf_arr[] = new ProfileUnserializer[MAX_PROFILE_CNT];
	long ibusyMem = Utils.busyMemory();
	for (int m = 0; m < MAX_PROFILE_CNT; m++) {
	    if (MAX_PROFILE_CNT != 1) {
		System.out.println("\nImporting #" + (m+1) + " profile");
	    }

	    ProfileUnserializer unserialProf = new ProfileUnserializer(name);
	    unserialProf_arr[m] = unserialProf;

	    long ms = System.currentTimeMillis();
	    long busyMem = Utils.busyMemory();

	    Profile profile = unserialProf.readProfile();

	    profile.setUnserializingPolicy(unserialPolicy);

	    profile.print();

	    int probe_cnt = profile.getProbeCount();
		
	    for (int n = probe_cnt - 1; n >= 0; n--) {
		Probe p = profile.getProbe(n);
	    
		/*
		  assert p.getPos() == 10 * n * probe_cnt;
		  
		  assert p.getSize() == 100 + n;
		  
		  assert p.getChrGnl() == (byte)(n % 23);
		  
		  assert p.getRatio() == (float)(n+9)/10;
		*/
		
		p.print();
	    }

	    long ms2 = System.currentTimeMillis();
	    System.err.println((ms2 - ms) + " ms");
	    System.err.println(((double)(ms2 - ms)/probe_cnt) + " ms per probe");
	    System.out.println("Memory Used: " + Utils.KB(Utils.busyMemory() - busyMem));
	    System.out.println("Total Memory Used: " + Utils.KB(Utils.busyMemory() - ibusyMem));
	}
    }
}
