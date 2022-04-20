
/*
 *
 * ProfileSequentialAccessFactory.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2008
 *
 */

package fr.curie.vamp;

import java.util.TreeMap;

import fr.curie.vamp.data.Profile;
import fr.curie.vamp.data.Probe;
import fr.curie.vamp.data.serial.*;
import fr.curie.vamp.gui.*;
import fr.curie.vamp.gui.optim.*;

class ProfileSequentialAccessFactory extends ProfileFactory {

    private int data_n;

    public ProfileSequentialAccessFactory(GlobalContext globalContext, String serialFile, int hints, GraphElement graphElementBase) {
	super(globalContext, serialFile, hints, graphElementBase);
	data_n = 0;
    }

    /*
    public void init(String name, int data_cnt) throws Exception {
    }
    */

    public void init(String name, int data_cnt, TreeMap properties) throws Exception {
	init(name, data_cnt, properties, true);
    }

    public void init(String name, int data_cnt, TreeMap properties, boolean pangen) throws Exception {
	super.init(name, data_cnt, properties, pangen);
	//System.out.println("writeHeader");
	profSerial.writeHeader();
    }

    public void add(RODataElementProxy data) throws Exception {
	Probe probe = makeProbe(data_n++, data);
	profSerial.writeProbe(probe);
	probe.release();
	if (data_n == profile.getProbeCount()) {
	    profile.setProbe(data_n-1, probe);
	}
    }

    public void write(RODataElementProxy data) throws Exception {
	if (!(data instanceof Probe)) {
	    add(data);
	    return;
	}

	Probe probe = (Probe)data;

	profSerial.writeProbe(probe);

	data_n++;

	if (data_n == profile.getProbeCount()) {
	    profile.setProbe(data_n-1, probe);
	}
    }

    /*
    public void set(int n, RODataElementProxy data) throws Exception {
	throw new Exception("void set(int n, RODataElementProxy data) not implemented");
    }
    */

    public GraphElement epilogue() throws Exception {
	//System.out.println("writeFooter #1");
	assert data_n == profile.getProbeCount();
	writeFooter();
	//System.out.println("writeFooter #2");

	//ProfileUnserializer profUnserial = new ProfileUnserializer(serialFile);
	ProfileUnserializer profUnserial = ProfileSerializerFactory.getInstance().getUnserializer(serialFile);

	profile = profUnserial.readProfile(true);
	profile.setUnserializingPolicy(Profile.CACHE_PROBES);
	profile.setPropertyValue(VAMPProperties.LargeProfileProp, new Boolean(true));
	profile.getProbe(0, true, true).addProp(VAMPProperties.LargeProfileProp, new Boolean(true));

	writeGraphicInfo();

	//System.out.println("ProfileSequentialAccessFactory: serialFile " + serialFile);
	GraphicProfile graphicProfile = new GraphicProfile(profile);
	//profile.print();
	//profile.setUnserializer(profUnserial);
	return profile;
    }
}
