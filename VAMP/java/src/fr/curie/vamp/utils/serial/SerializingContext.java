
package fr.curie.vamp.utils.serial;

import java.io.*;
import fr.curie.vamp.utils.*;

public class SerializingContext {

    public static final int STANDARD = 1;
    public static final int BUFFERED = 2;

    private FileOutputStream fos[];
    private BufferedFileOutputStream bfos[];
    private ObjectOutputStream oos[];
    private int mode;
    private boolean closed = false;

    public SerializingContext(String name, String suffix[], int mode) throws Exception {
	File file = new File(name);
	String filename = (!file.isAbsolute() ? SerialUtils.FILE_PREFIX : "") + name;

	this.mode = mode;
	oos = new ObjectOutputStream[suffix.length];

	if (mode == STANDARD) {
	    fos = new FileOutputStream[suffix.length];
	    for (int n = 0; n < suffix.length; n++) {
		String fname = filename + suffix[n];
		file = new File(fname);
		if (file.exists()) {
		    throw new Exception("Serialized file " + fname + " already exists");
		}
		fos[n] = new FileOutputStream(fname);
		oos[n] = new ObjectOutputStream(fos[n]);
	    }
	}
	else if (mode == BUFFERED) {
	    bfos = new BufferedFileOutputStream[suffix.length];
	    for (int n = 0; n < suffix.length; n++) {
		String fname = filename + suffix[n];
		file = new File(fname);
		if (file.exists()) {
		    throw new Exception("Serialized file " + fname + " already exists");
		}
		bfos[n] = new BufferedFileOutputStream(fname);
		oos[n] = new ObjectOutputStream(bfos[n]);
	    }
	}
	else {
	    System.err.println("invalid mode");
	}
    }

    public BufferedFileOutputStream getBFOS(int n) {
	assert !closed;
	return bfos[n];
    }

    public FileOutputStream getFOS(int n) {
	assert !closed;
	return fos[n];
    }

    public ObjectOutputStream getOOS(int n) {
	assert !closed;
	return oos[n];
    }

    public ObjectOutputStream getROpenOOS(int n) throws Exception {
	assert !closed;

	if (mode == BUFFERED) {
	    oos[n] = SerialUtils.reopen(bfos[n], oos[n]);
	    return oos[n];
	    /*
	    oos[n].flush();
	    return oos[n];
	    */
	}

	oos[n] = SerialUtils.reopen(fos[n], oos[n]);
	return oos[n];
    }

    public void close() throws Exception {
	if (closed) {
	    return;
	}

	for (int n = 0; n < oos.length; n++) {
	    oos[n].flush();
	    oos[n].close();
	}

	if (bfos != null) {
	    for (int n = 0; n < bfos.length; n++) {
		bfos[n].flush();
		bfos[n].close();
	    }
	}
	else {
	    for (int n = 0; n < fos.length; n++) {
		fos[n].flush();
		fos[n].close();
	    }
	}

	closed = true;
    }

    public void finalize() {
	try {
	    close();
	}
	catch(Exception e) {
	}
    }
}
