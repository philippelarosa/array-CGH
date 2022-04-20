package fr.curie.vamp.gui.optim;

import fr.curie.vamp.data.*;
import fr.curie.vamp.utils.serial.*;
import java.util.*;
import java.io.*;

public class GraphicInfo {

    private static final int SCALE_CNT = 32;
    private static final boolean WRITE_DMAP = true;

    private static class Scale {
	double scalex;
	double scaley;

	Scale(double scalex, double scaley) {
	    this.scalex = scalex;
	    this.scaley = scaley;
	}
    }

    private Scale scale_v[];
    private int probe_ginfo[];
    private int dmap[][];
    private Profile profile;
    private String file;

    public GraphicInfo(Profile profile) {
	this(profile, profile.getName());
    }

    public GraphicInfo(Profile profile, String file) {
	this.profile = profile;
	this.file = file;
	probe_ginfo = new int[profile.getProbeCount()];
	scale_v = new Scale[SCALE_CNT];
	dmap = new int[SCALE_CNT][];
    }

    public static final int GRINFO_INDEX = 0;
    public static final String GRINFO_SUFFIX = ".gri";

    public void write() throws Exception {
	SerializingContext serialCtx;
	serialCtx = new SerializingContext(file,
					   new String[]{GRINFO_SUFFIX},
					   SerializingContext.STANDARD);
	ObjectOutputStream oos = serialCtx.getOOS(GRINFO_INDEX);
	int scale_sz = 0;
	for (int n = 0; n < scale_v.length; n++) {
	    if (scale_v[n] == null) {
		break;
	    }
	    scale_sz++;
	}

	oos.writeInt(scale_sz);

	for (int n = 0; n < scale_sz; n++) {
	    oos.writeDouble(scale_v[n].scalex);
	    oos.writeDouble(scale_v[n].scaley);
	}

	oos.writeObject(probe_ginfo);

	if (WRITE_DMAP) {
	    oos.writeObject(dmap);
	}

	serialCtx.close();
    }

    public static GraphicInfo read(Profile profile) throws Exception {
	UnserializingContext unserialCtx;
	unserialCtx = new UnserializingContext(profile.getFileName(),
					       new String[]{GRINFO_SUFFIX});
	ObjectInputStream ois = unserialCtx.getOIS(GRINFO_INDEX);

	GraphicInfo graphicInfo = new GraphicInfo(profile);
	int scale_sz = ois.readInt();
	for (int n = 0; n < scale_sz; n++) {
	    double scalex = ois.readDouble();
	    double scaley = ois.readDouble();
	    graphicInfo.scale_v[n] = new Scale(scalex, scaley);
	}
	
	graphicInfo.probe_ginfo = (int[])ois.readObject();

	if (WRITE_DMAP) {
	    graphicInfo.dmap = (int[][])ois.readObject();
	}
	else {
	    for (int n = 0; n < scale_sz; n++) {
		graphicInfo.compile(n);
	    }
	}

	return graphicInfo;
    }

    public String toString() {
	return toString("");
    }

    public String toString(String indent) {
	String s = indent + "GraphicInfo {\n";
	for (int n = 0; n < scale_v.length; n++) {
	    if (scale_v[n] == null) {
		break;
	    }

	    s += indent + "  alphaX: " + scale_v[n].scalex + ", alphaY: " +
		scale_v[n].scaley + " (probe count: " + dmap[n].length + ")\n";
	}

	return s + indent + "}";
    }

    public int getIndScale(double scalex, double scaley) {
	return getIndScale(scalex, scaley, false);
    }

    public int getIndScale(double scalex, double scaley, boolean create) {

	double deltax = Double.MAX_VALUE;
	double deltay = Double.MAX_VALUE;
	int best_ind = -1;

	int n = 0;

	for (; n < scale_v.length; n++) {
	    Scale scale = scale_v[n];
	    if (scale == null) {
		break;
	    }

	    if (scalex == scale.scalex && scaley == scale.scaley) {
		if (create) {
		    scale_v[n] = new Scale(scalex, scaley);
		    System.out.println("Overriding Scale #" + n);
		}
		return n;
	    }

	    if (scalex <= scale.scalex && scaley <= scale.scaley) {
		double ndeltax = scale.scalex - scalex;
		double ndeltay = scale.scaley - scaley;

		if (deltax == Double.MAX_VALUE ||
		    (ndeltax <= deltax && ndeltay <= deltay)) {
		    deltax = ndeltax;
		    deltay = ndeltay;
		    best_ind = n;
		}
	    }
	}

	if (create) {
	    scale_v[n] = new Scale(scalex, scaley);
	    //System.out.println("Creating Scale #" + n);
	    return n;
	}

	return best_ind;
    }

    public void setProbeInfo(int indScale, int probe_num, boolean visible) {
	int mask = 1 << indScale;
	if (visible) {
	    probe_ginfo[probe_num] |= mask;
	}
	else {
	    probe_ginfo[probe_num] &= ~mask;
	}
    }

    public boolean isVisible(int indScale, int probe_num) {
	return (probe_ginfo[probe_num] & (1 << indScale)) != 0;
    }

    public int[] getDirectMap(int indScale) {
	return indScale >= 0 ? dmap[indScale] : null;
    }

    public void compile(int indScale) {
	if (indScale < 0) {
	    return;
	}

	Vector<Integer> v = new Vector();
	int mask = 1 << indScale;
	for (int n = 0; n < probe_ginfo.length; n++) {
	    if ((probe_ginfo[n] & mask) != 0) {
		v.add(n);
	    }
	}

	int len = v.size();
	dmap[indScale] = new int[len];
	for (int n = 0; n < len; n++) {
	    dmap[indScale][n] = v.get(n);
	}
	/*
	System.out.println("DMAP[" + indScale + "] " + dmap[indScale].length +
			   "/" + profile.getProbeCount());
	*/
    }
}
