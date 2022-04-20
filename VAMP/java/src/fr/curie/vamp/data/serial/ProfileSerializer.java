
package fr.curie.vamp.data.serial;

import fr.curie.vamp.data.*;
import fr.curie.vamp.properties.*;
import fr.curie.vamp.utils.*;
import fr.curie.vamp.utils.serial.*;
import fr.curie.vamp.Property;

import java.io.*;
import java.util.*;

abstract public class ProfileSerializer {

    protected Profile profile;
    protected boolean pangen;
    protected String file;

    protected ProfileSerializer(Profile profile, boolean pangen, String file) {
	this.profile = profile;
	this.pangen = pangen;
	this.file = file;
    }

    abstract public void writeHeader() throws Exception;

    abstract public void writeProbe(Probe p) throws Exception;

    abstract public void writeFooter(HashMap<String, Integer> typeMap,int bkp[], int out[], int smt[]) throws Exception;

    abstract public void close() throws Exception;
}
