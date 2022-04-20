
package fr.curie.vamp.data.serial;

import fr.curie.vamp.data.*;
import fr.curie.vamp.properties.*;
import fr.curie.vamp.utils.*;
import fr.curie.vamp.utils.serial.*;

import java.io.*;
import java.util.*;

public class ProfileSerializerFactory {

    public static final int NATIVE_JAVA = 1;
    public static final int PORTABLE = 2;

    private static ProfileSerializerFactory instance;

    private ProfileSerializerFactory() {
    }

    static public ProfileSerializerFactory getInstance() {
	if (instance == null) {
	    instance = new ProfileSerializerFactory();
	}

	return instance;
    }

    public ProfileSerializer getSerializer(int type, Profile profile, long chrPos[]) throws Exception {
	return getSerializer(type, profile, chrPos, profile.getName(), true);
    }

    public ProfileSerializer getSerializer(int type, Profile profile, long chrPos[], String file) throws Exception {
	return getSerializer(type, profile, chrPos, file, true);
    }

    public ProfileSerializer getSerializer(int type, Profile profile, long chrPos[], String file, boolean pangen) throws Exception {
	if (type == NATIVE_JAVA) {
	    return new ProfileNativeJavaSerializer(profile, chrPos, file, pangen);
	}

	if (type == PORTABLE) {
	    return new ProfilePortableSerializer(profile, chrPos, file, pangen);
	}

	return null;
    }

    static final int HEADER_SIZE = 12;

    int getType(String filename) throws IOException {
	//FileInputStream fis = new FileInputStream(filename + ProfileSerialUtils.DISPLAY_SUFFIX);
	FileInputStream fis = new FileInputStream(UnserializingContext.getFilename(filename) + ProfileSerialUtils.DISPLAY_SUFFIX);
	Coder coder = new Coder();
	coder.adapt(HEADER_SIZE);
	fis.read(coder.getData(), 0, HEADER_SIZE);
	coder.decodeInt(); // dummy
	long magic = coder.decodeLong();
	fis.close();

	return magic == ProfileSerialUtils.PORTABLE_MAGIC ? PORTABLE :
	    NATIVE_JAVA;
    }

    public ProfileUnserializer getUnserializer(String filename) throws Exception {
	int type = getType(filename);

	if (type == NATIVE_JAVA) {
	    return new ProfileNativeJavaUnserializer(filename);
	}

	if (type == PORTABLE) {
	    return new ProfilePortableUnserializer(filename);
	}

	return null;
    }
}
