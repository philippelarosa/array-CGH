
/*
 *
 * Chromosome.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

public class Chromosome extends PropertyElement {

    private String name;
    private Band bands[];
    private long cen_pos = -1;
    private long offset_pos;

    Chromosome(String name) {
	this(name, null);
    }

    Chromosome(String name, Band bands[]) {
	this.name = name;
	this.bands = bands;
	this.offset_pos = 0;
	setPropertyValue(VAMPProperties.NameProp, name);
    }

    void setBands(Band bands[]) {
	this.bands = bands;
    }

    void setBands(Object objbands[]) {
	bands = new Band[objbands.length];
	for (int n = 0; n < bands.length; n++)
	    bands[n] = (Band)objbands[n];
    }

    public String getName() {return name;}

    public Band[] getBands() {return bands;}

    public void display() {
	System.out.println("\tChromosome " + name + " {");
	for (int n = 0; n < bands.length; n++)
	    bands[n].display();
	System.out.println("\t}");
    }

    public long getCentromerePos() {
	if (cen_pos < 0) {
	    boolean found = false;
	    for (int n = 0; n < bands.length; n++)
		if (bands[n].getGiestain().equals("acen")) {
		    found = true;
		    cen_pos = bands[n].getBegin();
		}

	    if (!found)
		return cen_pos;
	}
	
	return cen_pos;
    }

    public long getCentromerePos_o() {
	long cen_pos = getCentromerePos();
	if (cen_pos < 0)
	    return cen_pos;

	return cen_pos + offset_pos;
    }

    public long getBegin() {return bands[0].getBegin();}
    public long getEnd() {return bands[bands.length-1].getEnd();}

    public long getBegin_o() {return getBegin() + offset_pos;}
    public long getEnd_o() {return getEnd() + offset_pos;}

    public Band getBand(long pos) {
	pos -= offset_pos;
	if (pos < bands[0].getBegin()) {
	    System.out.println("Chromosome.getBand(): error pos:" + pos +
			       " offset_pos:" + offset_pos + " " +
			       bands[0].getBegin() + " " +
			       bands[0].getEnd());
	}

	if (pos < bands[0].getBegin())
	    return bands[0];

	for (int n = 0; n < bands.length; n++)
	    if (pos >= bands[n].getBegin() && pos <= bands[n].getEnd())
		return bands[n];

	return bands[bands.length-1];
    }

    public long getOffsetPos() {return offset_pos;}
    public void setOffsetPos(long offset_pos) {this.offset_pos = offset_pos;}
}
