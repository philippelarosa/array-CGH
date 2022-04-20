
/*
 *
 * Band.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;

class Band extends PropertyElement {

    private Chromosome chr;
    private String arm, name;
    private long begin, end;
    private int code;
    private Color color;
    private String giestain;

    Band(Chromosome chr, String arm, String name, long begin, long end,
	 int code, int colorCode, String giestain) {
	this.chr = chr;
	this.arm = arm;
	this.name = name;
	this.begin = begin;
	this.end = end;
	this.code = code;
	this.color = new Color(colorCode);
	this.giestain = giestain;
	setPropertyValue(VAMPProperties.NameProp, name);
	setPropertyValue(VAMPProperties.ArmProp, arm);
	setPropertyValue(VAMPProperties.ChromosomeProp, chr.getName());
    }

    public void display() {
	System.out.println("\t\tBand {");
	System.out.println("\t\t\tname: " + name);
	System.out.println("\t\t\tarm: " + arm);
	System.out.println("\t\t\tbegin: " + begin);
	System.out.println("\t\t\tend: " + end);
	System.out.println("\t\t\tcolor: " + color.getRGB());
	System.out.println("\t\t\tcode: " + code);
	System.out.println("\t\t\tgiestain: " + giestain);
	System.out.println("\t\t}");
    }

    Chromosome getChromosome() {return chr;}
    String getArm() {return arm;}
    String getName() {return name;}
    long getBegin() {return begin;}
    long getEnd() {return end;}
    int getCode() {return code;}
    Color getColor() {return color;}
    String getGiestain() {return giestain;}
}
