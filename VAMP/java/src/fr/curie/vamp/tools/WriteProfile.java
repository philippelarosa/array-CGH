
package fr.curie.vamp.tools;

import fr.curie.vamp.data.*;
import fr.curie.vamp.properties.*;
import fr.curie.vamp.data.serial.*;
import fr.curie.vamp.utils.*;

import java.io.*;

class WriteProfile {

    public static void main(String args[]) {

	if (args.length != 2) {
	    System.err.println("usage: WriteProfile <name> <probe_cnt>");
	    System.exit(1);
	}

	try {
	    new WriteProfile(args[0], Integer.parseInt(args[1]));
	}
	catch(Exception e) {
	    e.printStackTrace();
	}
    }

    WriteProfile(String name, int probe_cnt) throws Exception {

	Profile profile = new Profile(name, probe_cnt);

	profile.addProp(Property.getProperty("zoo"), "zoo2");
	ProfileSerializer profSerial = new ProfileSerializer(profile);
	profSerial.writeHeader();

	long ms = System.currentTimeMillis();
	long busyMem = Utils.busyMemory();

	for (int n = 0; n < probe_cnt; n++) {
	    Probe p = new Probe();

	    p.setPos(10 * n * probe_cnt);
	    p.setSize(100 + n);
	    p.setChrGnl((byte)(n % 23));
	    p.setRatio((float)(n+9)/10);

	    for (int j = 0; j < 5; j++) {
		p.addProp(Property.getProperty("key_" + j),
			  "val_" + n + "_" + j);
	    }

	    profSerial.writeProbe(p);
	}

	profSerial.writeFooter();

	//System.out.println("start_probe_off: " + start_probe_off);
	//System.out.println("probe_size: " + probe_size);
	long ms2 = System.currentTimeMillis();
	System.err.println((ms2 - ms) + " ms");
	System.out.println("Memory Used: " + Utils.KB(Utils.busyMemory() - busyMem));
	profSerial.close();
	System.out.println("Memory Used2: " + Utils.KB(Utils.busyMemory() - busyMem));
    }
}
