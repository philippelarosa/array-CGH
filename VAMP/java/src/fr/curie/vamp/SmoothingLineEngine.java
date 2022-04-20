
/*
 *
 * SmoothingLineEngine.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2007
 *
 */

package fr.curie.vamp;

import java.util.*;
import fr.curie.vamp.data.*;

public class SmoothingLineEngine {

    static final Property smoothVProp = Property.getHiddenProperty("SmoothVProp");
    static final private String DATASET = "DATASET";
    static final private String VECTOR = "VECTOR";
    static final boolean new_algo = false;

    public static class SmoothingSegment {
	private double posx;
	private long len;
	private int gnl;

	SmoothingSegment(double posx, long len, int gnl) {
	    this.posx = posx;
	    this.len = len;
	    this.gnl = gnl;
	}
	
	double getPosX() {return posx;}
	long getLen() {return len;}
	int getGNL() {return gnl;}
    }

    public static class SmoothingInfo {

	//	private DataSet dataSet;
	private GraphElement graphElem;
	static final int LOST_IND = 0;
	static final int NORMAL_IND = 1;
	static final int GAINED_IND = 2;
	static final int AMPLICON_IND = 3;
	static final int ALT_CNT = 4;

	private Vector<SmoothingSegment> alt_v[];
	private long total_size[];

	private void init() {
	    alt_v = new Vector[ALT_CNT];
	    total_size = new long[ALT_CNT];

	    for (int n = 0; n < alt_v.length; n++) {
		alt_v[n] = new Vector();
		total_size[n] = 0;
	    }
	}

	public SmoothingInfo(Profile profile) {
	    init();

	    this.graphElem = profile;

	    int smt[] = profile.getSmoothings();

	    int sz = smt.length;
	    try {
		long oposx = 0;
		int ognl = 0;
		boolean started = false;
		int probe_cnt = profile.getProbeCount();
		int probe_offset = profile.getProbeOffset();
		for (int n = 0; n < sz; n++) {
		    int m = smt[n] - probe_offset;
		    if (m >= probe_cnt) {
			//assert started;
			break;
		    }
		    Probe p = profile.getProbe(m);

		    if (p == null) {
			if (started) {
			    break;
			}
			continue;
		    }

		    //long posx = p.getPanGenPos(profile);
		    long posx = p.getPos();
		    int gnl = p.getGnl();

		    if (started) {
			//SmoothingSegment s = new SmoothingSegment(oposx, (posx-oposx)/2, ognl); // /2 is for testing !
			SmoothingSegment s = new SmoothingSegment(oposx, 200, ognl); // /2 is for testing !
			//SmoothingSegment s = new SmoothingSegment(oposx, posx-oposx, ognl);
			//System.out.println("segment: " + oposx + " " + (posx-oposx));
			if (gnl == VAMPConstants.CLONE_GAINED) {
			    alt_v[GAINED_IND].add(s);
			}
			else if (gnl == VAMPConstants.CLONE_AMPLICON) {
			    alt_v[AMPLICON_IND].add(s);
			}
			else if (gnl == VAMPConstants.CLONE_LOST) {
			    alt_v[LOST_IND].add(s);
			}
			else if (gnl == VAMPConstants.CLONE_NORMAL) {
			    alt_v[NORMAL_IND].add(s);
			}
		    }
		    else {
			started = true;
		    }

		    oposx = posx;
		    ognl = gnl;
		}
	    }
	    catch(Exception e) {
		e.printStackTrace();
	    }

	    // I think a segment is missing
	    // result: on doit remplir ce tableau::
	    //Vector alt_v[];
	}

	public SmoothingInfo(DataSet dataSet) {
	    init();

	    this.graphElem = dataSet;
	    Vector v = computeSmoothingLines(dataSet);


	    double oy = Double.MAX_VALUE;;
	    double oposx = 0;
	    int ognl = VAMPConstants.CLONE_UNKNOWN;

	    for (int n = 0; n < v.size(); n++) {
		SmoothPoint sp = (SmoothPoint)v.get(n);
		//System.out.println("smooth " + sp.posx + " " + sp.smty + " " + sp.gnl);
		if (oy != Double.MAX_VALUE) {
		    if (sp.smty == oy) {
			if (sp.gnl != ognl) {
			    System.out.println("GNL inconsistency " +
					       sp.gnl + " " + ognl);
			}

			SmoothingSegment s = new SmoothingSegment(oposx, (long)(sp.posx - oposx), sp.gnl);

			if (sp.gnl == VAMPConstants.CLONE_GAINED) {
			    alt_v[GAINED_IND].add(s);
			}
			else if (sp.gnl == VAMPConstants.CLONE_AMPLICON) {
			    alt_v[AMPLICON_IND].add(s);
			}
			else if (sp.gnl == VAMPConstants.CLONE_LOST) {
			    alt_v[LOST_IND].add(s);
			}
			else if (sp.gnl == VAMPConstants.CLONE_NORMAL) {
			    alt_v[NORMAL_IND].add(s);
			}
		    }
		}

		oy = sp.smty;
		oposx = sp.posx;
		ognl = sp.gnl;
	    }

	    //System.out.println("gained " + gained.size() + " lost " + lost.size() + " " + dataSet.getID() + " " + VAMPUtils.getChr(dataSet));

	    /*
	    if (gained.size() > 0) {
		System.out.println((long)((SmoothingSegment)gained.get(0)).posx + " len " + ((SmoothingSegment)gained.get(0)).len);
	    }
	    */
	}

	GraphElement getGraphElement() {
	    return graphElem;
	}

	SmoothingSegment getSegment(int which, int n) {
	    return (SmoothingSegment)alt_v[which].get(n);
	}

	int getSegmentCount(int which) {
	    return alt_v[which].size();
	}

	long getTotalSize(int which) {
	    if (total_size[which] == 0) {
		int size = alt_v[which].size();
		for (int n = 0; n < size; n++) {
		    total_size[which] += ((SmoothingSegment)alt_v[which].get(n)).len;
		}
	    }

	    return total_size[which];
	}
    }

    public static class SmoothPoint {
	double posx;
	double smty;
	int gnl;

	SmoothPoint(double posx, double smty, int gnl) {
	    this.posx = posx;
	    this.smty = smty;
	    this.gnl = gnl;
	}

	SmoothPoint(double posx, double smty) {
	    this(posx, smty, VAMPConstants.CLONE_UNKNOWN);
	}
    }

    public static Vector computeSmoothingLines(DataSet dataSet) {

	HashMap map = (HashMap)dataSet.getPropertyValue(smoothVProp);
	if (map != null && map.get(DATASET) == dataSet) {
	    return (Vector)map.get(VECTOR);
	}

	//System.out.println("COMPUTE SMOOTHING LINES " + dataSet.getID());

	DataElement data[] = dataSet.getData();
	double last_posx = Double.MAX_VALUE;
	double last_smty = Double.MAX_VALUE;
	Vector v = new Vector();

	boolean new_smooth = false;

	DataElement last_d = null;
	SmoothPoint last_point = null;

	int lost_cnt = 0;
	int normal_cnt = 0;
	int gained_cnt = 0;
	int amplicon_cnt = 0;

	String lastChr = null;

	for (int n = 0; n < data.length; n++) {
	    DataElement d = data[n];
	    new_smooth = false;

	    String value = (String)d.getPropertyValue(VAMPProperties.SmoothingProp);
	    String chr = VAMPUtils.getChr(d);

	    boolean breakChr = false;
	    if (!chr.equals(lastChr)) {
		breakChr = true;
		lastChr = chr;
	    }

	    if (value == null || value.equalsIgnoreCase("NA")) {
		if (n == 0)
		    last_posx = d.getPosX(dataSet);
		continue;
	    }

	    double smty = Utils.parseDouble(value);
	    double posx = d.getPosX(dataSet);

	    if (smty != last_smty || breakChr) {
		double pos;

		if (last_smty != Double.MAX_VALUE) {
		    if (new_algo)
			pos = last_posx;
		    else
			pos = last_posx + (posx - last_posx)/2;
		    /*
		    System.out.println(last_smty + " gained_cnt " + gained_cnt +
				       " lost_cnt " + lost_cnt + " " +
				       " normal_cnt " + normal_cnt);

		    */
		    int gnl = getGNL(lost_cnt, normal_cnt, gained_cnt, amplicon_cnt);
		    v.add(new SmoothPoint(pos, last_smty, gnl));
		    last_point.gnl = gnl;
		    normal_cnt = lost_cnt = gained_cnt = amplicon_cnt = 0;
		}
		else
		    pos = d.getPosX(dataSet);

		if (new_algo)
		    last_point = new SmoothPoint(posx, smty);
		else
		    last_point = new SmoothPoint(pos, smty);

		v.add(last_point);

		last_smty = smty;
		new_smooth = true;
	    }

	    String sgnl = (String)d.getPropertyValue(VAMPProperties.GNLProp);
	    int gnl = GNLProperty.getGNL(sgnl);

	    boolean outlier = VAMPUtils.isOutlier(d);
	    boolean na = VAMPUtils.isNA(d);

	    if (!na && !outlier) {
		if (gnl == VAMPConstants.CLONE_GAINED)
		    gained_cnt++;
		else if (gnl == VAMPConstants.CLONE_AMPLICON)
		    amplicon_cnt++;
		else if (gnl == VAMPConstants.CLONE_LOST)
		    lost_cnt++;
		else if (gnl == VAMPConstants.CLONE_NORMAL)
		    normal_cnt++;
	    }

	    if (!VAMPUtils.isNA(d) || n == 0) {
		last_posx = posx;
		last_d = d;
	    }
	}

	if (!new_smooth) {
	    //double posx = data[data.length-1].getPosX(dataSet);
	    if (last_d != null) {
		double posx = last_d.getPosX(dataSet);
		int gnl = getGNL(lost_cnt, normal_cnt, gained_cnt, amplicon_cnt);
		v.add(new SmoothPoint(posx, last_smty, gnl));
		last_point.gnl = gnl;
	    }
	    else
		System.err.println("avoid a null pointer exception for " + dataSet.getID() + " chr " + lastChr);
	}

	map = new HashMap();
	map.put(DATASET, dataSet);
	map.put(VECTOR, v);
	dataSet.setPropertyValue(smoothVProp, map);
	return v;
    }

    static int getGNL(int lost_cnt, int normal_cnt, int gained_cnt, int amplicon_cnt) {
	if (lost_cnt != 0) {
	    if (gained_cnt + normal_cnt + amplicon_cnt != 0) {
		System.out.println("smoothing line inconsistency #1");
	    }
	    return VAMPConstants.CLONE_LOST;
	}

	if (normal_cnt != 0) {
	    if (gained_cnt + lost_cnt + amplicon_cnt != 0) {
		System.out.println("smoothing line inconsistency #2");
	    }
	    return VAMPConstants.CLONE_NORMAL;
	}

	if (gained_cnt != 0) {
	    if (lost_cnt + normal_cnt + amplicon_cnt != 0) {
		System.out.println("smoothing line inconsistency #3");
	    }
	    return VAMPConstants.CLONE_GAINED;
	}

	if (amplicon_cnt != 0) {
	    if (lost_cnt + normal_cnt + gained_cnt != 0) {
		System.out.println("smoothing line inconsistency #4");
	    }
	    return VAMPConstants.CLONE_AMPLICON;
	}

	return VAMPConstants.CLONE_UNKNOWN;
    }
}
