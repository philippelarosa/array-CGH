
package fr.curie.vamp.utils.serial;

import java.io.*;
import java.util.*;
import fr.curie.vamp.utils.*;
import fr.curie.vamp.Property;

public class SerialUtils {

    public static final String DSPREFIX = "DS";
    public static final String FILE_PREFIX = System.getenv("SERIALDIR") != null ? (System.getenv("SERIALDIR") + "/" + DSPREFIX) : "serial_profiles/" + DSPREFIX;

    private static Coder coder;

    static {
	coder = new Coder();
	coder.adapt(1024);
    }

    static ObjectOutputStream reopen(FileOutputStream fos, ObjectOutputStream os) throws Exception {

	os.flush();
	int off = (int)fos.getChannel().position();
	fos.getChannel().position(0);	    
	ObjectOutputStream nos = new ObjectOutputStream(fos);
	fos.getChannel().position(off);
	return nos;
    }

    static ObjectOutputStream reopen(BufferedFileOutputStream bfos, ObjectOutputStream os) throws Exception {

	os.flush();
	int off = (int)bfos.position();
	bfos.pass(true);

	bfos.getChannel().position(0);	    
	ObjectOutputStream nos = new ObjectOutputStream(bfos);
	bfos.getChannel().position(off);

	bfos.pass(false);
	return nos;
    }

    
    public static void write(OutputStream os, Coder coder) throws IOException {
	os.write(coder.getData(), 0, coder.getOffset());
    }

    public static void writeByte(OutputStream os, byte b) throws IOException {
	coder.reset();
	coder.code(b);
	write(os, coder);
    }

    public static void writeInt(OutputStream os, int i) throws IOException {
	coder.reset();
	coder.code(i);
	write(os, coder);
    }

    public static void writeLong(OutputStream os, long l) throws IOException {
	coder.reset();
	coder.code(l);
	write(os, coder);
    }

    public static void writeFloat(OutputStream os, float f) throws IOException {
	coder.reset();
	coder.code(f);
	write(os, coder);
    }

    public static void writeDouble(OutputStream os, double d) throws IOException {
	coder.reset();
	coder.code(d);
	write(os, coder);
    }

    public static void writeShort(OutputStream os, short s) throws IOException {
	coder.reset();
	coder.code(s);
	write(os, coder);
    }

    public static void writeString(OutputStream os, String str) throws IOException {
	coder.reset();
	coder.code(str);
	write(os, coder);
    }

    static final byte TYPE_OFFSET = 100;

    static final byte NULL_TYPE = 0 + TYPE_OFFSET;
    static final byte BYTE_TYPE = 1 + TYPE_OFFSET;
    static final byte INT16_TYPE = 2 + TYPE_OFFSET;
    static final byte INT32_TYPE = 3 + TYPE_OFFSET;
    static final byte INT64_TYPE = 4 + TYPE_OFFSET;
    static final byte STRING_TYPE = 5 + TYPE_OFFSET;
    static final byte PROPERTY_TYPE = 6 + TYPE_OFFSET;
    //static final byte MAP_TYPE = 6;

    public static void codeObject(OutputStream os, Object obj) throws IOException {
	if (obj == null) {
	    coder.code(NULL_TYPE);
	    return;
	}

	if (obj instanceof String) {
	    coder.code(STRING_TYPE);
	    coder.code((String)obj);
	    return;
	}

	if (obj instanceof Integer) {
	    coder.code(INT32_TYPE);
	    coder.code(((Integer)obj).intValue());
	    return;
	}
	
	if (obj instanceof Short) {
	    coder.code(INT16_TYPE);
	    coder.code(((Short)obj).shortValue());
	    return;
	}
	
	if (obj instanceof Long) {
	    coder.code(INT64_TYPE);
	    coder.code(((Long)obj).longValue());
	    return;
	}
	
	if (obj instanceof Byte) {
	    coder.code(BYTE_TYPE);
	    coder.code(((Byte)obj).byteValue());
	    return;
	}
	
	if (obj instanceof Property) {
	    coder.code(PROPERTY_TYPE);
	    coder.code(((Property)obj).getName());
	    return;
	}
	
	System.err.println("invalid type to write " + obj.getClass().getName());
	throw new IOException();
    }

    public static void writeMap(OutputStream os, HashMap map) throws IOException {
	coder.reset();

	int size = map.size();
	coder.code(size);
	Iterator it = map.entrySet().iterator();
	while(it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    codeObject(os, entry.getKey());
	    codeObject(os, entry.getValue());
	}

	write(os, coder);
    }

    public static byte readByte(InputStream is) throws IOException {
	coder.reset();
	// adapt non needed
	is.read(coder.getData(), 0, Coder.BYTE_SIZE);
	return coder.decodeByte();
    }

    public static int readInt(InputStream is) throws IOException {
	coder.reset();
	// adapt non needed
	is.read(coder.getData(), 0, Coder.INT32_SIZE);
	return coder.decodeInt();
    }

    public static long readLong(InputStream is) throws IOException {
	coder.reset();
	// adapt non needed
	is.read(coder.getData(), 0, Coder.INT64_SIZE);
	return coder.decodeLong();
    }

    public static double readDouble(InputStream is) throws IOException {
	coder.reset();
	// adapt non needed
	is.read(coder.getData(), 0, Coder.DOUBLE_SIZE);
	return coder.decodeDouble();
    }

    public static float readFloat(InputStream is) throws IOException {
	coder.reset();
	// adapt non needed
	is.read(coder.getData(), 0, Coder.FLOAT_SIZE);
	return coder.decodeFloat();
    }

    public static short readShort(InputStream is) throws IOException {
	coder.reset();
	// adapt non needed
	is.read(coder.getData(), 0, Coder.INT16_SIZE);
	return coder.decodeShort();
    }

    public static String readString(InputStream is) throws IOException {
	coder.reset();
	int len = readInt(is);
	coder.adapt(len+1);
	is.read(coder.getData(), Coder.INT32_SIZE, len);
	coder.reset();
	return coder.decodeString();
    }

    public static Object readObject(InputStream is) throws IOException {
	byte b = readByte(is);

	if (b == NULL_TYPE) {
	    return null;
	}

	if (b == STRING_TYPE) {
	    return readString(is);
	}

	if (b == INT32_TYPE) {
	    return new Integer(readInt(is));
	}

	if (b == INT16_TYPE) {
	    return new Short(readShort(is));
	}

	if (b == INT64_TYPE) {
	    return new Long(readLong(is));
	}

	if (b == BYTE_TYPE) {
	    return new Byte(readByte(is));
	}

	if (b == PROPERTY_TYPE) {
	    return Property.getProperty(readString(is));
	}

	System.err.println("invalid type to read " + b + " " + ((FileInputStream)is).getChannel().position());
	throw new IOException();
	//return null;
    }

    public static HashMap readMap(InputStream is) throws IOException {
	BufferedInputStream bis = new BufferedInputStream(is);
	//bis.skip(((FileInputStream)is).getChannel().position());
	return readMap_direct(bis);
    }

    public static HashMap readMap_direct(InputStream is) throws IOException {
	int size = readInt(is);
	HashMap map = new HashMap();
	for (int n = 0; n < size; n++) {
	    Object key = readObject(is);
	    Object value = readObject(is);
	    map.put(key, value);
	}

	return map;
    }

    public static void test() {
	try {
	    File file = new File("/tmp/SERIAL.DAT");
	    FileOutputStream fos = new FileOutputStream(file);
	    writeInt(fos, 2);
	    writeInt(fos, 10);
	    writeString(fos, "hello world xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxyxz");
	    writeInt(fos, 20394);
	    HashMap map = new HashMap();
	    map.put("alors ?", new Integer(1));
	    map.put("quoi ?", new Integer(10));
	    map.put("zoulou les lapins", "coucou les canards");
	    map.put("zzzz", new Integer(-29291));
	    writeMap(fos, map);
	    writeInt(fos, -1203);
	    fos.close();
	    FileInputStream fis = new FileInputStream(file);
	    int n1 = readInt(fis);
	    int n2 = readInt(fis);
	    String s = readString(fis);
	    int n3 = readInt(fis);
	    HashMap map2 = readMap(fis);
	    int n4 = readInt(fis);
	    fis.close();
	    System.out.println("n1 " + n1 + " " + n2 + " " + s + " " + n3 + " " + map2.size() + " " + n4);
	    Iterator it = map2.entrySet().iterator();
	    while(it.hasNext()) {
		Map.Entry entry = (Map.Entry)it.next();
		System.out.println(entry.getKey() + " => " + entry.getValue());
	    }
	}
	catch(Exception e) {
	    e.printStackTrace();
	}
    }
}
