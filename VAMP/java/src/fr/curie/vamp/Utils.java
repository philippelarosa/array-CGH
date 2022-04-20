
/*
 *
 * Utils.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.awt.font.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.applet.*;
import java.security.*;

public class Utils {

    static final int DEFAULT_PRECISION = 3;

    static String cookie;
    static boolean isApplet;
    static final boolean USE_TEMPFILE = false;

    static class SetCursorRunnable implements Runnable {
	Component comp;
	Cursor cursor;

	SetCursorRunnable(Component comp, Cursor cursor) {
	    this.comp = comp;
	    this.cursor = cursor;
	}

	public void run() {
	    comp.setCursor(cursor);
	}
    }

    static GridBagConstraints makeGBC(int gridx, int gridy) {
	GridBagConstraints c = new GridBagConstraints();
	c.gridx = gridx;
	c.gridy = gridy;
	return c;
    }

    static JPanel addPadPanel(JPanel panel, int x, int y,
			      int ipadx, int ipady, Color color) {
	JPanel pad = new JPanel();
	pad.setSize((ipadx != 0) ? 1 : 0, (ipady != 0) ? 1 : 0);
	//pad.setPreferredSize(new Dimension((ipadx != 0) ? 1 : 0, (ipady != 0) ? 1 : 0));
	pad.setBackground(color);
	GridBagConstraints c = makeGBC(x, y);
	c.ipadx = ipadx-1;
	if (c.ipadx < 0) c.ipadx = 0;
	c.ipady = ipady-1;
	if (c.ipady < 0) c.ipady = 0;
	c.fill = GridBagConstraints.NONE;
	c.weightx = 0;
	c.weighty = 0;
	panel.add(pad, c);
	return pad;
    }

    static Dimension getSize(Graphics2D g, String s) {
	return getSize(g, g.getFont(), s);
    }

    static Dimension getSize(Graphics2D g, Font f, String s) {
	FontRenderContext frc = g.getFontRenderContext();
	Rectangle2D bounds = f.getStringBounds(s, frc);
	return new Dimension((int)bounds.getWidth(), (int)bounds.getHeight());
    }

    static boolean isXMLFile(String name) {
	return hasExtension(name, "xml");
    }

    static boolean isHTMLFile(String name) {
	return hasExtension(name, "html");
    }

    static String getCanonName(String name) {
	int idx = name.lastIndexOf('.');
	if (idx < 0) return name;
	return name.substring(0, idx);
    }

    static boolean hasExtension(String name, String ext) {
	if (ext.charAt(0) != '.')
	    ext = "." + ext;
	int name_len = name.length();
	int ext_len = ext.length();
	if (name_len <= ext_len)
	    return false;

	String name_ext = name.substring(name_len - ext_len);
	return name_ext.equals(ext);
    }

    static boolean hasExtension(String name) {
	return name.indexOf('.') != -1;
    }

    static String suppressExtension(String name) {
	int r = name.lastIndexOf('.');
	if (r != -1) {
	    int r1 = name.lastIndexOf('/');
	    int r2 = name.lastIndexOf('\\');
	    if ((r1 == -1 || r1 < r) &&
		(r2 == -1 || r2 < r))
		return name.substring(0, r);
	}
	return name;
    }

    static Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

    static void centerString(Component comp, Graphics2D g, String s, int y) {
	Dimension size = comp.getSize();
	FontRenderContext frc = g.getFontRenderContext();
	Rectangle2D bounds = g.getFont().getStringBounds(s, frc);
	g.drawString(s, (int)((size.width - bounds.getWidth()) / 2), y);
    }

    static void centerOnScreen(Dialog dialog) {
	centerOnScreen(dialog, true);
    }

    static void centerOnScreen(Dialog dialog, boolean visible) {
	Dimension size = dialog.getSize();
	dialog.setLocation((screenSize.width - size.width) / 2,
			   (screenSize.height - size.height) / 2);
	if (visible) {
	    dialog.getToolkit().beep();
	    dialog.setVisible(visible);
	}
    }

    public static String [] getLines(String text) {
	return getLines(text, 0);
    }

    public static String [] getLines(String text, int splitWidth) {
	StringTokenizer st = new StringTokenizer(text, "\n");
	Vector v = new Vector();
	while (st.hasMoreTokens()) {
	    String s = st.nextToken();
	    String ns = splitText(s, splitWidth);
	    if (ns.equals(s))
		v.addElement(s);
	    else {
		String nlines[] = getLines(ns);
		for (int i = 0; i < nlines.length; i++)
		    v.addElement(nlines[i]);
	    }
	}

	int n = v.size();
	String lines[] = new String[n];
	for (int i = 0; i < n; i++)
	    lines[i] = (String)v.elementAt(i);
	return lines;
    }

    static char CHAR_SEPS[] = new char[]{' ', '\t', '|', '&'};

    private static int getIndexSep(String text) {
	
	for (int n = 0; n < CHAR_SEPS.length; n++) {
	    int idx = text.lastIndexOf(CHAR_SEPS[n]);
	    if (idx >= 0)
		return idx;
	}

	return -1;
    }

    public static String splitText(String text, int splitWidth) {
	if (splitWidth <= 0)
	    return text;

	String reste = text;
	String text_temp;
	String rs = "";
	int idx = 0;

	if (text.length() > splitWidth) {
	    while (reste.length() > splitWidth) {
		text_temp = reste.substring(0, splitWidth);
		//idx = text_temp.lastIndexOf(" ");
		idx = getIndexSep(text_temp);

		if (idx < 0)
		    break;

		text_temp = text_temp.substring(0, idx).trim();
		rs = rs + text_temp + "\n";
		reste = reste.substring(idx, reste.length()).trim();
	    }

	    rs = rs + reste;
	}
	else
	    rs = text;
    
	return rs;
    }

    public static void augment(Component f) {
	augment(f, 1.2, 1.2);
    }

    public static void augment(Component f, double coefx, double coefy) {
	Dimension size = f.getSize();
	size.width = (int)((double)size.width * coefx);
	size.height = (int)((double)size.height * coefy);
	f.setSize(size);
    }

    public static void augment(Component f, int w, int h) {
	Dimension size = f.getSize();
	size.width  += w;
	size.height += h;
	f.setSize(size);
    }

    static void setProxyConfiguration(String proxy, String port) {
	java.util.Properties systemProperties = System.getProperties();
	systemProperties.setProperty("http.proxyHost", proxy);
	systemProperties.setProperty("http.proxyPort", port);
    }

    static final boolean useServerCache = false;

    /*
    static void printContents(InputStream is) {
	byte b[] = new byte[1024];
	int n;
	try {
	    FileOutputStream os = new FileOutputStream(new File("/tmp/OO"));
	    PrintStream ps = new PrintStream(os);
	    while ((n = is.read(b)) >= 0) {
		ps.print(new String(b, n));
	    }
	}
	catch(Exception e) {
	}
    }
    */

    static HashMap url_map = new HashMap();
    static boolean COOKIE_V1_5 = true;

    public static InputStream openStream(String spec)
	throws MalformedURLException, IOException {
	return openStream(spec, null, true);
    }

    public static InputStream openStream(String spec, String data)
	throws MalformedURLException, IOException {
	return openStream(spec, data, true);
    }

    public static InputStream openStream(String spec, boolean throw_e)
	throws MalformedURLException, IOException {
	return openStream(spec, null, throw_e);
    }

    public static InputStream openStream(String spec, String data, boolean throw_e)
	throws MalformedURLException, IOException {
	URL url = new URL(spec);

	URLConnection urlConn = url.openConnection();

	if (cookie != null) {
	    if (isApplet && COOKIE_V1_5) {
		String host = url.getHost();
		if (host == null || url_map.get(host) == null) {
		    //System.out.println("setting cookie[2]: " + cookie);
		    urlConn.setRequestProperty("Cookie", cookie);
		}
		/*
		else
		    System.out.println("cookie already set");
		*/

		if (host != null)
		    url_map.put(host, new Boolean(true));
	    }
	    else
		urlConn.setRequestProperty("Cookie", cookie);
	}

	urlConn.setUseCaches(useServerCache);
	try {
	    if (data != null && data.length() > 0) {
		urlConn.setDoOutput(true);
		//System.out.println("Posting !");
		DataOutputStream os;
		os = new DataOutputStream (urlConn.getOutputStream ());
		os.writeBytes(data);
		os.flush();
		os.close();
	    }

	    return urlConn.getInputStream();
	}
	catch(MalformedURLException e) {
	    if (throw_e)
		throw e;
	    return null;
	}
	catch(IOException e) {
	    if (throw_e)
		throw e;
	    return null;
	}
    }

    static URL makeURL(String spec, String data, boolean throw_e)
	throws MalformedURLException, IOException {
	URL url = new URL(spec);

	URLConnection urlConn = url.openConnection();
	urlConn.setUseCaches(useServerCache);
	try {
	    if (data != null && data.length() > 0) {
		urlConn.setDoOutput(true);
		DataOutputStream os;
		os = new DataOutputStream(urlConn.getOutputStream());
		os.writeBytes(data);
		os.flush();
	    }
	    
	    return url;
	}
	catch(MalformedURLException e) {
	    if (throw_e)
		throw e;
	    return null;
	}
	catch(IOException e) {
	    if (throw_e)
		throw e;
	    return null;
	}
    }

    static InputStream tee(InputStream is, String file) {

	try {
	    File tempfile = new File(file);
	    FileOutputStream os = new FileOutputStream(tempfile);
	    PrintStream ps = new PrintStream(os);

	    byte b[] = new byte[4096];
	    int n;
	    while ((n = is.read(b)) >= 0)
		ps.print(new String(b, 0, n));
	    
	    ps.close();
	    is.close();
	    return new FileInputStream(tempfile);
	}
	catch(Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    static void showDocument(GlobalContext globalContext,
			     AppletContext appletContext, String spec,
			     String data, String target)
	throws MalformedURLException, IOException {

	if (data == null) {
	    URL url = new URL(spec);
	    appletContext.showDocument(url, target);
	    return;
	}

	InputStream is = openStream(spec, data, true);
	if (is == null)
	    return;

	if (USE_TEMPFILE) {
	    File tempfile = URLOP.getTempFile();
	    FileOutputStream os = new FileOutputStream(tempfile);
	    PrintStream ps = new PrintStream(os);

	    byte b[] = new byte[4096];
	    int n;
	    while ((n = is.read(b)) >= 0)
		ps.print(new String(b, 0, n));
	
	    ps.close();
	    URL url = new URL("file://" + tempfile.getAbsolutePath());
	    System.out.println("ShowDocument: " + url);
	    if (target == null)
		appletContext.showDocument(url);
	    else
		appletContext.showDocument(url, target);
	}
	else {
	    SystemConfig systemConfig = (SystemConfig)globalContext.
		get(SystemConfig.SYSTEM_CONFIG);
	    
	    String postman = systemConfig.getParameter("postmanager:URL");
	    byte b[] = new byte[4096];
	    int n;
	    data = "";
	    while ((n = is.read(b)) >= 0)
		data += new String(b, 0, n);

	    is = openStream(postman, "data=" + URLEncoder.encode(data), true);
	    if (is == null)
		return;
	    String surl = "";
	    while ((n = is.read(b)) >= 0)
		surl += new String(b, 0, n);
	    URL url = new URL(surl);
	    System.out.println("ShowDocument 2: " + url);
	    if (target == null)
		appletContext.showDocument(url);
	    else
		appletContext.showDocument(url, target);
	}
    }

    static void setCookie(String cookie, boolean isApplet) {
	Utils.cookie = cookie;
	Utils.isApplet = isApplet;
    }

    static final double BASE = 2.;
    static final double LOG2 = Math.log(BASE);

    static double log(double d) {
	if (d <= 0) {
	    d = 0.0000001;
	}
	return Math.log(d)/LOG2;
    }

    static double pow(double d) {
	return Math.pow(BASE, d);
    }

    static Cursor setWaitCursor(Component comp) {
	return Utils.setCursor(comp, Cursor.WAIT_CURSOR);
    }

    static Cursor setCursor(Component comp, Cursor cursor) {
	Cursor o_cursor = comp.getCursor();
	if (SwingUtilities.isEventDispatchThread())
	    comp.setCursor(cursor);
	else
	    SwingUtilities.invokeLater(new SetCursorRunnable(comp, cursor));
	return o_cursor;
    }

    static Cursor setCursor(Component comp, int type) {
	Cursor cursor = comp.getCursor();
	Utils.setCursor(comp, Cursor.getPredefinedCursor(type));
	return cursor;
    }

    static Cursor setWaitCursor(View view) {
	return Utils.setCursor(view, Cursor.WAIT_CURSOR);
    }

    static Cursor setCursor(View view, int type) {
	return Utils.setCursor(view, Cursor.getPredefinedCursor(type));
    }

    static Cursor setCursor(View view, Cursor cursor) {
	Cursor o_cursor = view.getCursor();

	Utils.setCursor(view.getGraphPanelSet(), cursor);
	Utils.setCursor((Component)view, cursor);

	return o_cursor;
    }

    static Cursor setCursor(GraphPanelSet panelSet, Cursor cursor) {
	Cursor o_cursor = panelSet.getCursor();

	GraphPanel[] panels = panelSet.getPanels();
	for (int n = 0; n < panels.length; n++)
	    Utils.setCursor(panels[n], cursor);

	Utils.setCursor((Component)panelSet, cursor);

	return o_cursor;
    }

    static Cursor setCursor(GraphPanel panel, Cursor cursor) {
	Cursor o_cursor = panel.getCursor();
	Utils.setCursor((Component)panel.getCanvas(), cursor);
	Utils.setCursor((Component)panel, cursor);
	return o_cursor;
    }

    static Vector listToVector(LinkedList list) {
	Vector v = new Vector();
	if (list != null)
	    v.addAll(list);
	return v;
    }

    static LinkedList vectorToList(Vector v) {
	LinkedList list = new LinkedList();
	if (v != null)
	    list.addAll(v);
	return list;
    }

    static Vector keyVector(HashMap map) {
	Iterator it = map.values().iterator();
	Vector v = new Vector();
	while (it.hasNext())
	    v.add(it.next());
	return v;
    }

    static Vector keyVector(TreeMap map) {
	Iterator it = map.values().iterator();
	Vector v = new Vector();
	while (it.hasNext())
	    v.add(it.next());
	return v;
    }

    static Vector treeSetToVector(TreeSet set) {
	Vector v = new Vector();
	Iterator it = set.iterator();
	while (it.hasNext())
	    v.add(it.next());
	return v;
    }

    static TreeMap hashToTreeMap(HashMap hash) {
	if (hash == null)
	    return null;
	Iterator it = hash.entrySet().iterator();
	TreeMap tree = new TreeMap();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    tree.put(entry.getKey(), entry.getValue());
	}
	return tree;
    }

    static HashMap treeToHashMap(TreeMap tree) {
	if (tree == null)
	    return null;
	Iterator it = tree.entrySet().iterator();
	HashMap map = new HashMap();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    map.put(entry.getKey(), entry.getValue());
	}
	return map;
    }

    static void display(AbstractMap map) {
	if (map == null)
	    return;
	Iterator it = map.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    System.out.println(entry.getKey() + " => " + entry.getValue());
	}
    }

    static String toString(double d) {
	return (new Double(d)).toString();
    }

    static String toString(int i) {
	return (new Integer(i)).toString();
    }

    static String toString(long l) {
	return (new Long(l)).toString();
    }

    static Dimension drawImage(Graphics g, Toolkit toolkit, String url,
			       int x, int y, int maxh,
			       boolean centerx, boolean centery) {

	if (url == null)
	    return null;

	try {
	    Image img = toolkit.getImage(new URL(url));
	    img = new ImageIcon(img).getImage();
	    int height = img.getHeight(null);
	    int width = img.getWidth(null);
	    if (height <= 0 || width <= 0 ||
		(maxh >= 0 && height >= maxh))
		return null;

	    if (centerx)
		x -= width / 2;

	    if (centery)
		y -= height / 2;

	    if (g.drawImage(img, x, y, null))
		return new Dimension(width, height);
	    return null;
	}
	catch(Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    static Integer makeInteger(Object s) {
	return new Integer(parseInt(s.toString()));
    }

    static int parseInt(String s) {
	try {
	    return Integer.parseInt(s);
	}
	catch(NumberFormatException e) {
	    return Integer.MIN_VALUE;
	}
    }

    static int parseInt(String s, int base) {
	try {
	    return Integer.parseInt(s, base);
	}
	catch(NumberFormatException e) {
	    return Integer.MIN_VALUE;
	}
    }

    static Double makeDouble(Object s) {
	return new Double(parseDouble(s.toString()));
    }

    static double parseDouble(String s) {
	if (s.equalsIgnoreCase("NA"))
	    return 0.;

	try {
	    return Double.parseDouble(s);
	}
	catch(NumberFormatException e) {
	    return Double.POSITIVE_INFINITY;
	}
    }

    static long parseLong(String s) {
	try {
	    return Long.parseLong(s);
	}
	catch(NumberFormatException e) {
	    //return Long.MIN_VALUE;
	    return 0;
	}
    }

    static boolean checkRead(GlobalContext globalContext, File file) {
	if (!file.exists()) {
	    InfoDialog.pop(globalContext, "Error: file " +
			   file.getAbsolutePath() +
			   " does not exist");
	    return false;
	}

	if (!file.canRead()) {
	    InfoDialog.pop(globalContext, "Error: cannot access file " +
			   file.getAbsolutePath() +
			   " for reading");
	    return false;
	}
	return true;
    }

    static String getCount(int cnt, String what) {
	String s = cnt + " " + what;
	if (cnt == 1)
	    return s;
	return s + "s";
    }

    static private String performRound_d(String s, int precis) {
	int idx = s.indexOf(".");
	if (idx < 0)
	    return s;
	int i;
	String suffix = "";
	if ((i = s.indexOf("E")) >= 0)
	    suffix = s.substring(i, s.length());

	idx += 1 + precis;
	if (idx > s.length()) idx = s.length();
	return s.substring(0, idx) + suffix;
    }

    static boolean checkDouble(Object o) {
	if (o == null)
	    return false;
	if (o instanceof Float || o instanceof Double)
	    return true;
	String s = o.toString();
	int len = s.length();
	for (int n = 0; n < len; n++) {
	    char c = s.charAt(n);
	    if (c != '-' && c != '.' && (c < '0' || c > '9'))
		return false;
	}

	return true;
    }

    static String toString(Object o) {
	if (o == null)
	    return "";
	return performRound(o);
    }

    static String performRound(double d, int precis) {
	return performRound_d(toString(d), precis);
    }

    static String performRound(Object o, int precis) {
	if (checkDouble(o))
	    return performRound_d(o.toString(), precis);
	return o.toString();
    }

    static String performRound(double d) {
	return performRound_d(toString(d), DEFAULT_PRECISION);
    }

    static String performRound(Object o) {
	return performRound(o, DEFAULT_PRECISION);
    }


    static long busyMemory() {
	return Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory();
    }

    static void freeMemory() {
	freeMemory(false);
    }

    static void freeMemory(boolean gc) {
	if (gc) {
	    gc();
	}

	System.out.println("MEMORY:");
	System.out.println(" Max: " + (Runtime.getRuntime().maxMemory()/1024) + "K");
	System.out.println(" Total: " + (Runtime.getRuntime().totalMemory()/1024) + "K");
	System.out.println(" Busy: " +
			   ((Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory())/1024) + "K");
	System.out.println(" Free: " + (Runtime.getRuntime().freeMemory()/1024) + "K");
	System.out.println(" Shared Map Size: " + Property.getSharedMapSize());
	PropertyElement.printMap();
    }

    static void gc() {
	//freeMemory();

	//long ms0 = (new Date()).getTime();

	//System.out.println("GC...");
	System.gc();


	//freeMemory();
	/*
	ms0 = (new Date()).getTime() - ms0;
	System.out.println("FREE MEMORY AFTER GC " + (Runtime.getRuntime().freeMemory()/1024) + "K [duration " + ms0 + " ms]");
	*/
    }

    static public String md5Crypt(String key) throws Exception {
	MessageDigest md5 = MessageDigest.getInstance("MD5");
	byte b[] = md5.digest(key.getBytes());
	
	StringBuffer strb = new StringBuffer();
	for (int i = 0; i < b.length; i++) {
	    strb.append(Integer.toHexString(0xFF & b[i]));
	}

	return strb.toString();
    }
}
