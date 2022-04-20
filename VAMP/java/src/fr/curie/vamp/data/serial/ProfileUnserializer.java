
package fr.curie.vamp.data.serial;

import fr.curie.vamp.data.*;
import fr.curie.vamp.properties.*;
import fr.curie.vamp.utils.*;
import fr.curie.vamp.utils.serial.*;
import fr.curie.vamp.Property;
import java.io.*;
import java.util.*;

abstract public class ProfileUnserializer {

    abstract public ProfileUnserializer cloneRealize() throws Exception;

    abstract public int getVersion();

    public Profile readProfile() throws Exception {
	return readProfile(true);
    }
	
    abstract public Profile readProfile(boolean full_imported) throws Exception;

    abstract public Probe getProbe(int n) throws Exception;

    abstract public void complete(Probe p) throws Exception;

    abstract public void close() throws Exception;
}
