
package fr.curie.vamp.utils.serial;

import fr.curie.vamp.utils.FileInputManager;

import java.io.*;

public class UnserializingContext {

    private String filename;
    private String suffix[];
    private FileInputStream fis[] = null;
    private ObjectInputStream ois[] = null;;

    static final boolean USE_MANAGER = true;
    private static int ID = 0;
    private int id;
    private boolean closed = false;

    public static String getFilename(String name) {
	File file = new File(name);

	if (file.isAbsolute()) {
	    return name;
	}

	return SerialUtils.FILE_PREFIX + name;
    }

    public UnserializingContext(String name, String suffix[]) throws Exception {
	filename = getFilename(name);

	this.suffix = suffix;
	this.id = ID++;

	if (!USE_MANAGER) {
	    fis = new FileInputStream[suffix.length];
	    ois = new ObjectInputStream[suffix.length];

	    for (int n = 0; n < suffix.length; n++) {
		fis[n] = new FileInputStream(filename + suffix[n]);
		ois[n] = new ObjectInputStream(fis[n]);
	    }
	}
    }

    public FileInputStream getFIS(int n) {
	assert !closed;

	if (USE_MANAGER) {
	    return FileInputManager.getInstance().getFIS(filename + suffix[n], id);
	}

	return fis[n];
    }

    public ObjectInputStream getOIS(int n) {
	assert !closed;

	if (USE_MANAGER) {
	    return FileInputManager.getInstance().getOIS(filename + suffix[n], id);
	}

	return ois[n];
    }

    public void close() throws Exception {
	if (closed) {
	    return;
	}

	closed = true;

	if (USE_MANAGER) {
	    for (int n = 0; n < suffix.length; n++) {
		FileInputManager.getInstance().close(filename + suffix[n], id);
	    }
	    return;
	}

	for (int n = 0; n < suffix.length; n++) {
	    fis[n].close();
	    ois[n].close();
	}
    }

    public void finalize() {
	try {
	    close();
	}
	catch(Exception e) {
	}
    }
}
