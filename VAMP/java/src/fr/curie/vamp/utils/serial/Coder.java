
package fr.curie.vamp.utils.serial;

import java.io.*;
import fr.curie.vamp.utils.*;

public class Coder {

    private byte[] data;
    private int offset;
    private boolean isconst = false;

    public Coder() {
	this.data = null;
	offset    = 0;
    }
 
    public Coder(byte[] data) {
	this.data = data;
	offset    = 0;
    }

    public Coder(byte[] data, int offset) {
	this.data   = data;
	this.offset = offset;
    }

    public Coder(byte[] data, int offset, boolean isconst) {
	this.data    = data;
	this.offset  = offset;
	this.isconst = isconst;
    }

    public void code(char c) {
	adapt(CHAR_SIZE);

	data[offset] = (byte)c;

	offset += CHAR_SIZE;
    }

    public void code(byte b) {
	adapt(BYTE_SIZE);

	data[offset] = b;

	offset += BYTE_SIZE;
    }

    public void code(int x) {
	adapt(INT32_SIZE);

	int ind = SWAP_INT32_START + offset;

	data[ind] = (byte)(x & 0xff);
	ind += SWAP_INCR;

	data[ind] = (byte)((x & 0xff00)     >>> 8);
	ind += SWAP_INCR;

	data[ind] = (byte)((x & 0xff0000)   >>> 16);
	ind += SWAP_INCR;

	data[ind] = (byte)((x & 0xff000000) >>> 24);

	offset += INT32_SIZE;
    }

    public void code(short s) {
	adapt(INT16_SIZE);

	int ind = SWAP_INT16_START + offset;

	data[ind] = (byte)(s & 0xff);
	ind += SWAP_INCR;

	data[ind] = (byte)((s & 0xff00)     >>> 8);
	ind += SWAP_INCR;

	offset += INT16_SIZE;
    }

    public void code(long x) {
	adapt(INT64_SIZE);

	int ind = offset + SWAP_INT64_START;

	data[ind] = (byte)((x & 0x00000000000000ffL));
	ind += SWAP_INCR;

	data[ind] = (byte)((x & 0x000000000000ff00L) >>> 8);
	ind += SWAP_INCR;

	data[ind] = (byte)((x & 0x0000000000ff0000L) >>> 16);
	ind += SWAP_INCR;

	data[ind] = (byte)((x & 0x00000000ff000000L) >>> 24);
	ind += SWAP_INCR;

	data[ind] = (byte)((x & 0x000000ff00000000L) >>> 32);
	ind += SWAP_INCR;

	data[ind] = (byte)((x & 0x0000ff0000000000L) >>> 40);
	ind += SWAP_INCR;

	data[ind] = (byte)((x & 0x00ff000000000000L) >>> 48);
	ind += SWAP_INCR;

	data[ind] = (byte)((x & 0xff00000000000000L) >>> 56);
	ind += SWAP_INCR;

	offset += INT64_SIZE;
    }

    public void code(float f) {

	code(Float.floatToIntBits(f));
    }

    public void code(double d) {

	code(Double.doubleToLongBits(d));
    }

    public void code(String s) {
	int len = s.length();

	adapt(INT32_SIZE + len + 1);

	code(len+1);

	s.getBytes(0, len, data, offset);

	offset += len;

	data[offset] = 0;

	offset++;
    }

    public void code(String s, boolean b) {
	int len = s.length();

	adapt(len + 1);

	s.getBytes(0, len, data, offset);

	offset += len;

	data[offset] = 0;

	offset++;
    }

    //    static final int MAX_BYTE_LEN = 254;
    static final int MAX_BYTE_LEN = 25;
    static final int MAX_SHORT_LEN = 0xFFFF;
    static final byte INT_LEN_MARK = (byte)0xFF;
    static final byte SHORT_LEN_MARK = (byte)0xFE;

    public void codeStrLen(String s) {
	int len = s.length()+1;
	if (len < MAX_BYTE_LEN) {
	    code((byte)len);
	}
	else if (len < MAX_SHORT_LEN) {
	    code(SHORT_LEN_MARK);
	    code((short)len);
	}
	else {
	    code(INT_LEN_MARK);
	    code(len);
	}
    }

    public int decodeStrLen() {
	byte len_b = data[offset++];

	if (len_b == INT_LEN_MARK) {
	    return decodeInt();
	}

	if (len_b == SHORT_LEN_MARK) {
	    return decodeShort();
	}

	return len_b;
    }

    public boolean moreMaxIntLen(byte len_b) {
	return len_b == INT_LEN_MARK;
    }

    public boolean moreMaxShortLen(byte len_b) {
	return len_b == SHORT_LEN_MARK;
    }

    public void code(boolean b) {
	code(b ? (char)1 : (char)0);
    }

    public void code(byte[] b) {
	code(b, b.length);
    }

    public void code(byte[] b, int len) {
	adapt(len);
	System.arraycopy(b, 0, data, offset, len);
	offset += len;
    }

    public char decodeChar() {
	return (char)data[offset++];
    }

    public byte decodeByte() {
	return data[offset++];
    }

    public short decodeShort() {
	int ind = offset + SWAP_INT16_START;

	short x = ((short)unsign(data[ind]));

	x += (short)(unsign(data[ind + SWAP_INCR]) << 8);

	offset += INT16_SIZE;
	return x;
    }

    public int decodeInt() {
	int ind = offset + SWAP_INT32_START;

	int x = unsign(data[ind]);
	x += unsign(data[ind + SWAP_INCR])   << 8;
	x += unsign(data[ind + 2*SWAP_INCR]) << 16;
	x += unsign(data[ind + 3*SWAP_INCR]) << 24;

	offset += INT32_SIZE;
	return x;
    }

    public long decodeLong() {
	int ind = offset + SWAP_INT64_START;

	long x = ((long)unsign(data[ind]));

	x += ((long)unsign(data[ind + SWAP_INCR]))   <<  8;
	x += ((long)unsign(data[ind + 2*SWAP_INCR])) << 16;
	x += ((long)unsign(data[ind + 3*SWAP_INCR])) << 24;
	x += ((long)unsign(data[ind + 4*SWAP_INCR])) << 32;
	x += ((long)unsign(data[ind + 5*SWAP_INCR])) << 40;
	x += ((long)unsign(data[ind + 6*SWAP_INCR])) << 48;
	x += ((long)unsign(data[ind + 7*SWAP_INCR])) << 56;

	offset += INT64_SIZE;
	return x;
    }

    public double decodeDouble() {
	long x = decodeLong();

	return Double.longBitsToDouble(x);
    }

    public float decodeFloat() {
	int x = decodeInt();

	return Float.intBitsToFloat(x);
    }

    public String decodeString() {
	int len = decodeInt();
	return decodeString(len);
	/*
	String s = new String(data, 0, offset, len-1);
	offset += len;
	return s;
	*/
    }

    public String decodeString(int len) {
	String s = new String(data, 0, offset, len-1);
	offset += len;
	return s;
    }

    public boolean decodeBoolean() {
	char c = decodeChar();
	return (c == 0 ? false : true);
    }

    public byte[] decodeBuffer(int len) {
	byte[] b = new byte[len];
	System.arraycopy(data, offset, b, 0, len);
	offset += len;
	return b;
    }

    public int getOffset() {return offset;}

    public void setOffset(int offset) {this.offset = offset;}

    public byte[] getData() {return data;}

    public void reset() {offset = 0;}

    static private final int INCR_SIZE         = 128;
    static private final int SWAP_INT64_START  = 7;
    static private final int SWAP_INT32_START  = 3;
    static private final int SWAP_INT16_START  = 1;
    static private final int SWAP_DOUBLE_START = 7;
    static private final int SWAP_INCR         = -1;

    static public final int BYTE_SIZE         = 1;
    static public final int CHAR_SIZE         = 1;
    static public final int INT64_SIZE        = 8;
    static public final int INT32_SIZE        = 4;
    static public final int INT16_SIZE        = 2;
    static public final int FLOAT_SIZE        = 4;
    static public final int DOUBLE_SIZE       = 8;
    static public final int OID_SIZE          = 8;
    static public final int OBJECT_SIZE       = 4;
  
    public void adapt(int len, boolean copy) {
	int olength = (data != null ? data.length : 0);

	if (offset + len > olength) {
	    if (isconst) {
		System.err.println("Coder error in adapt(offset = " +
				   offset + ", len = " + len + ", olength = " +
				   olength + ") is const");
		return;
	    }

	    int nlength = Math.max(olength, offset + len) + INCR_SIZE;

	    if (copy && data != null) {
		byte[] b = new byte[nlength];
		System.arraycopy(data, 0, b, 0, olength);
		data = b;
	    }
	    else {
		data = new byte[nlength];
	    }
	}
    }

    public void adapt(int len) {
	adapt(len, true);
    }

    private static int unsign(byte b) {
	int y = (int)b;

	if (y < 0)
	    y = 0xff + y + 1;

	return y;
    }

    public static void memzero(byte[] b) {
	for (int i = 0; i < b.length; i++)
	    b[i] = 0;
    }

    public static void memzero(byte[] b, int offset, int size) {
	int end = size + offset;

	for (int i = offset; i < end; i++)
	    b[i] = 0;
    }

    public static boolean memcmp(byte[] b1, int offset1, byte[] b2, int offset2,
				 int size) {

	for (int i = 0, off1 = offset1, off2 = offset2; i < size; i++,
		 off1++, off2++)
	    if (b1[off1] != b2[off2])
		return false;

	return true;
    }
}
