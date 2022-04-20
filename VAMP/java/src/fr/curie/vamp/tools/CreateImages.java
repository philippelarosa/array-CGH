
package fr.curie.vamp.tools;

import fr.curie.vamp.data.*;
import fr.curie.vamp.properties.*;
import fr.curie.vamp.data.serial.*;
import fr.curie.vamp.utils.*;
import fr.curie.vamp.gui.*;
import fr.curie.vamp.gui.optim.*;

import java.io.*;
import java.awt.*;
import java.util.*;
import javax.imageio.*;
import java.awt.image.*;

class CreateImages {

    private static void usage() {
	System.err.println("usage: CreateImages -sizes wxh:... [-format png|jpeg] {<profile_names>}");
	System.exit(1);
    }

    public static void main(String args[]) {

	if (args.length < 3) {
	    usage();
	}

	Vector<String> profile_v = new Vector();
	String sizes = null;
	String fmt = null;

	for (int n  = 0; n < args.length; n++) {
	    if (args[n].equals("-sizes")) {
		if (++n == args.length || sizes != null) {
		    usage();
		}
		sizes = args[n];
	    }
	    else if (args[n].equals("-fmt")) {
		if (++n == args.length || fmt != null) {
		    usage();
		}
		fmt = args[n];
		if (!fmt.equalsIgnoreCase("png") ||
		    !fmt.equalsIgnoreCase("jpeg"))
		    usage();
	    }
	    else {
		profile_v.add(args[n]);
	    }
	}

	if (sizes == null) {
	    usage();
	}

	if (fmt == null) {
	    fmt = "png";
	}

	try {
	    new CreateImages(sizes, fmt, profile_v);
	}
	catch(Exception e) {
	    e.printStackTrace();
	}
    }

    static boolean VERBOSE = true;

    CreateImages(String sizes, String fmt, Vector<String> profile_v) throws Exception {

	String size_arr[] = sizes.split(":");

	Vector<Dimension> size_v = new Vector<Dimension>();
	for (int n = 0; n < size_arr.length; n++) {
	    String dims[] = size_arr[n].split("x");
	    if (dims.length != 2) {
		usage();
	    }
	    Dimension dim = new Dimension();
	    try {
		dim.width = Integer.parseInt(dims[0]);
		dim.height = Integer.parseInt(dims[1]);
	    }
	    catch(Exception e) {
		usage();
	    }
	    size_v.add(dim);
	}

	int size_sz = size_v.size();
	int profile_sz = profile_v.size();

	for (int n = 0; n < profile_sz; n++) {

	    String profName = profile_v.get(n);
	    try {
		ProfileUnserializer unserialProf = ProfileSerializerFactory.getInstance().getUnserializer(profName);
		
		Profile profile = unserialProf.readProfile();
		if (VERBOSE) {
		    System.out.println("Capturing images for " + profile.getName());
		}
	
		GraphicProfile graphicProfile = new GraphicProfile(profile);
		profile.setUnserializingPolicy(Profile.NONE);
		
		for (int j = 0; j < size_sz; j++) {
		    Dimension dim = size_v.get(j);
		    double amplX = profile.getMaxX() - profile.getMinX();
		    double amplY = profile.getMaxY() - profile.getMinY();
		    Scale scale = new Scale(dim.width / amplX, 0,
					    dim.height / amplY, dim.height/2);
		    BufferedImage img =
			new BufferedImage(dim.width, dim.height,
					  BufferedImage.TYPE_INT_ARGB);
	    
		    Graphics2D g = (Graphics2D)img.getGraphics();
		    Painter painter = new Painter(graphicProfile);
		    painter.paint(g, scale);
		    File file = new File("/tmp/img/img_" + profile.getName() + "_" + dim.width + "x" + dim.height + "." + fmt);
		    boolean b = ImageIO.write(img, fmt, file);
		}
	    }
	    catch(Exception e) {
		e.printStackTrace();
	    }
	}
    }
}
