
package fr.curie.vamp.utils;

import java.io.*;
import java.nio.channels.*;

public class BufferedFileOutputStream extends FileOutputStream {

    public BufferedFileOutputStream(String filename) throws FileNotFoundException {
	super(filename);
    }

    public long position() throws IOException {
	return super.getChannel().position();
    }

    public void position(long nposition) throws IOException {
	super.getChannel().position(nposition);
    }

    public void pass(boolean pass) {
    }

    /*
    static final int BUF_SIZE = 256000;

    int position, posbuf;
    byte buf[];
    boolean position_changed = false;
    String filename;
    private boolean verbose = false;
    private boolean pass = false;

    public BufferedFileOutputStream(String filename) throws FileNotFoundException {
	super(filename);
	this.filename = filename;
	position = 0;
	posbuf = 0;

	buf = new byte[BUF_SIZE];
    }

    public void write(byte b[]) {
	System.out.println("write(byte[]");
    }

    public void verbose(boolean verbose) {
	this.verbose = verbose;
    }

    public void pass(boolean pass) {
	this.pass = pass;
    }

    public void write(byte b[], int offset, int len) throws IOException {
	if (verbose) {
	    System.out.println("write " + offset + " " + len + " " + position_changed);
	}

	if (position_changed || pass) {
	    super.write(b, offset, len);
	    return;
	}

	//System.out.println("write(byte[], " + offset + ", " + len + ")");
	int overb = posbuf + len - BUF_SIZE;
	if (overb > 0) {
	    int towr = len - overb;
	    System.arraycopy(b, offset, buf, posbuf, towr);
	    super.write(buf);
	    posbuf = 0;
	    System.arraycopy(b, offset + towr, buf, posbuf, overb);
	}
	else {
	    System.arraycopy(b, offset, buf, posbuf, len);
	}

	posbuf += len;
	position += len;
    }

    public void write(int b) {
	System.out.println("write(int)");
    }

    public void close() throws IOException {
	//System.out.println("final position " + position + " " + filename);
	iflush();
	flush();
	super.close();
    }

    private void iflush() throws IOException {
	if (buf != null) {
	    super.write(buf, 0, posbuf);
	    posbuf = 0;
	    buf = null;
	}
    }

    public void flush() throws IOException {
	if (buf != null) {
	    super.write(buf, 0, posbuf);
	    posbuf = 0;
	}
	super.flush();
    }

    public long position() throws IOException {
	if (position_changed) {
	    return super.getChannel().position();
	}
	return position;
    }

    public void position(int nposition) throws IOException {
	//System.out.println("position -> " + nposition + " (" + position + ") " + filename);
	if (!position_changed) {
	    iflush();
	    flush();
	    position_changed = true;
	}
	super.getChannel().position(nposition);
    }
    */
}
