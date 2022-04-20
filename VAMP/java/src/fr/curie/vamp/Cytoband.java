
/*
 *
 * Cytoband.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

public class Cytoband {

    private String name;
    private String organism;
    private String resolution;
    private Vector chrV;
    private HashMap chrMap;
    private long offset_pos;

    Cytoband(String name, String organism, String resolution) {
	this.name = name;
	this.organism = organism;
	this.resolution = resolution;
	chrMap = new HashMap();
	chrV = new Vector();
	offset_pos = 0;
    }

    public Chromosome getChromosome(String chrName) {
	return (Chromosome)chrMap.get(chrName);
    }

    public Chromosome getChromosome(long pos) {
	int size = chrV.size();
	for (int n = 0; n < size; n++) {
	    Chromosome chr = (Chromosome)chrV.get(n);
	    if (pos < chr.getBegin_o() || n == size-1)
		return chr;
	    if (pos >= chr.getBegin_o() &&
		pos <= chr.getEnd_o())
		return chr;
	}

	return null;
    }

    void addChromosome(Chromosome chr) {
	chrV.add(chr);
	chrMap.put(chr.getName(), chr);
	if (chr.getName().equals("X"))
	    chrMap.put("23", chr);
	else if (chr.getName().equals("Y"))
	    chrMap.put("24", chr);

	// assuming chromosomes are added in order !
	chr.setOffsetPos(offset_pos);
	/*
	System.out.println(organism + ":" + chr.getName() + ": " +
			   chr.getBegin_o() + ", " + chr.getEnd_o());
	*/
	offset_pos = chr.getEnd_o() + MergeChrOP.OFFSET_CHR;
    }

    public void display() {
	System.out.println("Cytoband {");
	System.out.println("\tname: " + name);
	System.out.println("\torganism: " + organism);
	System.out.println("\tresolution: " + resolution);
	Iterator it = chrMap.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    Chromosome chr = (Chromosome)entry.getValue();
	    chr.display();
	}
	System.out.println("}");
    }

    public String getName() {return name;}
    public String getOrganism() {return organism;}
    public String getResolution() {return resolution;}
    public Vector getChrV() {return chrV;}
}
