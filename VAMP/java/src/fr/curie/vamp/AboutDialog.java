
/*
 *
 * AboutDialog.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.awt.event.*;
import java.awt.print.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.net.*;

class AboutDialog extends JDialog {

    static final String ABOUT_DIALOG = "ABOUT_DIALOG";

    public static void init(GlobalContext globalContext) {
	globalContext.put(ABOUT_DIALOG,
			  new AboutDialog(new Frame(), globalContext));
    }

    public static void pop(GlobalContext globalContext) {
	AboutDialog dialog = (AboutDialog)globalContext.get(ABOUT_DIALOG);
	dialog.pack();
	Utils.augment(dialog, 1.5, 1.5);
	Utils.centerOnScreen(dialog);
    }

    static final Color bgColor = Color.LIGHT_GRAY;

    AboutDialog(Frame f, GlobalContext globalContext) {
	super(f, "About VAMP", Config.DEFAULT_MODAL);

	getContentPane().setLayout(new BorderLayout());
	InfoPanel infoPanel = new InfoPanel("VAMP Version " +
					    VersionManager.getStringVersion(),
					    globalContext);
	infoPanel.setBackground(bgColor);
	getContentPane().setBackground(bgColor);

	/*
	MLLabel l = new MLLabel();
	//l.setBackground(bgColor);
	l.setFont(new Font("SansSerif", Font.BOLD, 12));
	l.setText("VAMP Version " +
		  VersionManager.getStringVersion() + "\n \n" +
		  VAMPConstants.COPYRIGHT);
	infoPanel.add(l, BorderLayout.NORTH);
	*/
	//infoPanel.setSize(250, 120);
	infoPanel.setPreferredSize(new Dimension(200, 140));

	getContentPane().add(infoPanel, BorderLayout.CENTER);

	JPanel buttonPanel = new JPanel(new FlowLayout());
	buttonPanel.setBackground(bgColor);

	JButton b = new JButton("OK");
	b.setFont(new Font("SansSerif", Font.BOLD, 12));
	//b.setFont(VAMPResources.getFont(VAMPResources.SEARCH_PANEL_BUTTON_FONT));

	b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    setVisible(false);
		}
	    });

	buttonPanel.add(b);
	getContentPane().add(buttonPanel, BorderLayout.SOUTH);

	pack();
	Utils.augment(this, 1.5, 1.5);
	Utils.centerOnScreen(this, false);
    }

    static class InfoPanel extends JPanel {
	String version;
	String logoURL;
	GlobalContext globalContext;

	InfoPanel(String version, GlobalContext _globalContext) {
	    this.version = version;
	    this.globalContext = _globalContext;

	    addMouseListener(new MouseAdapter() {
		    public void mouseReleased(MouseEvent _e) {
			java.applet.AppletContext appletContext =
			    globalContext.getAppletContext();

			try {
			    if (appletContext != null) {
				URL url = new URL(VAMPConstants.WEBSITE);
				appletContext.showDocument(url, "_blank");
			    }
			}
			catch(Exception e) {
			    e.printStackTrace();
			}
		    }
		});
	}

	public void paint(Graphics _g) {
	    if (logoURL == null) {
		SystemConfig sysCfg = (SystemConfig)globalContext.
		    get(SystemConfig.SYSTEM_CONFIG);
		logoURL = sysCfg.getParameter("logo:URL");
	    }

	    Graphics2D g = (Graphics2D)_g;
	    Dimension size = getSize();
	    g.setFont(new Font("SansSerif", Font.BOLD, 12));
	    g.setColor(Color.LIGHT_GRAY);
	    g.fillRect(0, 0, size.width, size.height);

	    g.setColor(Color.BLACK);

	    Utils.centerString(this, g, version, 20);

	    for (int n = 0; n < VAMPConstants.COPYRIGHT_S.length; n++) {
		String s = VAMPConstants.COPYRIGHT_S[n];
		Utils.centerString(this, g, s, 40 + n * 14);
	    }

	    Utils.drawImage(g, getToolkit(), logoURL,
			    size.width/2, 100, -1, true, false);

	    String s = "Web Site: " + VAMPConstants.WEBSITE;
	    Utils.centerString(this, g, s, 210);

	    s = "Contact: " + VAMPConstants.CONTACT;
	    Utils.centerString(this, g, s, 224);
	}

    }
}
