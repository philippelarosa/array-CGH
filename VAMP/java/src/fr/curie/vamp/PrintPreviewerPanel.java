
/*
 *
 * PrintPreviewerPanel.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.util.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.print.*;
import javax.swing.*;
import javax.swing.text.html.*;
import java.io.*;
import java.net.*;

class PrintPreviewerPanel extends JPanel {
    //class PrintPreviewerPanel extends JLayeredPane {

    PrintableSet printableSet;
    PageFormat format;
    int pageNum = 0;
    private PrintArea selectedArea = null;
    private int selectedOffsetX = 0;
    private int selectedOffsetY = 0;
    private int resizeMode = 0;
    private boolean mayDrag = false;

    PrintPreviewerPanel() {
	setLayout(null);
	addMouseListener(new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
		    if (!printableSet.isEditMode()) return;
		    int button = e.getButton();
		    if (button == MouseEvent.BUTTON2) return;
		    mayDrag = (button == MouseEvent.BUTTON1);

		    PrintArea area =
			printableSet.getArea(e.getX(), e.getY());

		    printableSet.setSelected(selectedArea, false);
		    selectedArea = area;
		    if (area != null) {
			printableSet.setSelected(selectedArea, true);
			Rectangle2D.Double r = area.getArea();
			selectedOffsetX = roundX(e.getX());
			selectedOffsetY = roundY(e.getY());
			resizeMode = getResizeMode(e.getX(),
						   e.getY(),
						   selectedArea);
			setCursor(Cursor.getPredefinedCursor(resizeMode));
		    }

		    if (button == MouseEvent.BUTTON3) {
			if (area == null) return;
			JPopupMenu popup = createPopupMenu(area);
			popup.show(e.getComponent(), e.getX(), e.getY());
		    }

		    repaint();
		}

		public void mouseReleased(MouseEvent e) {
		    mayDrag = false;
		    if (!printableSet.isEditMode()) return;
		    //setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	    });

	addMouseMotionListener(new MouseMotionAdapter() {
		public void mouseMoved(MouseEvent e) {
		    if (!printableSet.isEditMode()) return;
		    PrintArea area =
			printableSet.getArea(e.getX(), e.getY());
		    if (area == null) {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			return;
		    }

		    int r = getResizeMode(e.getX(), e.getY(), area);
		    if (r == Cursor.MOVE_CURSOR)
			r = Cursor.DEFAULT_CURSOR;
		    setCursor(Cursor.getPredefinedCursor(r));
		}

		public void mouseDragged(MouseEvent e) {
		    if (!printableSet.isEditMode()) return;
		    if (!mayDrag) return;
		    if (selectedArea == null) return;
		    
		    int e_x = roundX(e.getX());
		    int e_y = roundY(e.getY());

		    /*
		    Rectangle2D.Double r = selectedArea.getArea();
		    selectedArea.setLocation(roundX((int)r.x), roundY((int)r.y));
		    selectedArea.setSize(roundW((int)r.width), roundH((int)r.height));
		    */

		    if (resizeMode == Cursor.SE_RESIZE_CURSOR) {
			selectedArea.resize(e_x - selectedOffsetX,
					    e_y - selectedOffsetY);
		    }
		    else if (resizeMode == Cursor.NE_RESIZE_CURSOR) {
			selectedArea.move(0, e_y - selectedOffsetY);
			selectedArea.resize(e_x - selectedOffsetX,
					    -e_y + selectedOffsetY);
		    }
		    else if (resizeMode == Cursor.NW_RESIZE_CURSOR) {
			selectedArea.move(e_x - selectedOffsetX, 
					  e_y - selectedOffsetY);
			selectedArea.resize(-e_x + selectedOffsetX,
					    -e_y + selectedOffsetY);
		    }
		    else if (resizeMode == Cursor.SW_RESIZE_CURSOR) {
			selectedArea.move(e_x - selectedOffsetX, 0);
			selectedArea.resize(-e_x + selectedOffsetX,
					    e_y - selectedOffsetY);
		    }
		    else if (resizeMode == Cursor.S_RESIZE_CURSOR) {
			selectedArea.resize(0, e_y - selectedOffsetY);
		    }
		    else if (resizeMode == Cursor.N_RESIZE_CURSOR) {
			selectedArea.move(0, e_y - selectedOffsetY);
			selectedArea.resize(0, -e_y + selectedOffsetY);
		    }
		    else if (resizeMode == Cursor.E_RESIZE_CURSOR) {
			selectedArea.resize(e_x - selectedOffsetX, 0);
		    }
		    else if (resizeMode == Cursor.W_RESIZE_CURSOR) {
			selectedArea.move(e_x - selectedOffsetX, 0);
			selectedArea.resize(-e_x + selectedOffsetX, 0);
		    }
		    else {
			selectedArea.move(e_x - selectedOffsetX,
					  e_y - selectedOffsetY);
		    }
		    selectedOffsetX = e_x;
		    selectedOffsetY = e_y;
		    repaint();
		}
	    });
    }

    void setPrintableSet(PrintableSet _printableSet) {
	this.printableSet = _printableSet;
	this.printableSet.setPrintPreviewerPanel(this);
	format = printableSet.format;
	
	this.printableSet.makeComponents(this);

	//alignAreas();
    }

    void setPageNum(int pageNum) {
	this.pageNum = pageNum;
    }

    public void paintGrid(Graphics g) {
	if (GRID <= 1 || !showGrid() || !activeGrid()) return;

	int beg_x = (int)format.getImageableX() - 1;
	int end_x = (int)(beg_x + format.getImageableWidth() + 2);

	int beg_y = (int)format.getImageableY() - 1;
	int end_y = (int)(beg_y + format.getImageableHeight() + 2);

	g.setColor(Color.LIGHT_GRAY);
	for (int x = beg_x; x <= end_x; x += GRID)
	    g.drawLine(x, beg_y, x, end_y);

	for (int y = beg_y; y <= end_y; y += GRID)
	    g.drawLine(beg_x, y, end_x, y);
    }

    public void paint(Graphics g) {
	g.setColor(Color.WHITE);
	Dimension sz = getSize();
	g.fillRect(0, 0, sz.width, sz.height);

	if (printableSet.isEditMode())
	    paintGrid(g);

	if (printableSet.isEditMode())
	    g.setColor(Color.ORANGE);
	else
	    g.setColor(Color.GRAY);

	g.drawRect((int)format.getImageableX()-1,
		   (int)format.getImageableY()-1,
		   (int)format.getImageableWidth()+2,
		   (int)format.getImageableHeight()+2);
	printableSet.print(g, null, pageNum);
    }

    static final int EPS = 2;

    static boolean isNear(int x, double from) {
	return (x >= (int)from - EPS && x <= (int)from + EPS);
    }

    int getResizeMode(int x, int y, PrintArea area) {
	Rectangle2D.Double r = area.getArea();

	if (isNear(x, r.x)) {
	    if (isNear(y, r.y))
		return Cursor.NW_RESIZE_CURSOR;

	    if (isNear(y, r.y + r.height))
		return Cursor.SW_RESIZE_CURSOR;

	    return Cursor.W_RESIZE_CURSOR;
	}

	if (isNear(x, r.x + r.width)) {
	    if (isNear(y, r.y))
		return Cursor.NE_RESIZE_CURSOR;

	    if (isNear(y, r.y + r.height))
		return Cursor.SE_RESIZE_CURSOR;

	    return Cursor.E_RESIZE_CURSOR;
	}

	if (isNear(y, r.y))
	    return Cursor.N_RESIZE_CURSOR;

	if (isNear(y, r.y + r.height))
	    return Cursor.S_RESIZE_CURSOR;

	return Cursor.MOVE_CURSOR;
    }

    static final String FONT_FAMILY = "SansSerif";
    static final int FONT_MIN_SIZE = 8;
    static final int FONT_MAX_SIZE = 18;
    static final int FONT_INCR_SIZE = 2;

    JPopupMenu createPopupMenu(PrintArea area) {
	JPopupMenu popup;
	JMenuItem menuItem;

	popup = new JPopupMenu();
	menuItem = new JMenuItem(area.getName());
	popup.add(menuItem);
	Rectangle2D.Double r = area.getArea();
	PageFormat format = printableSet.format;
	menuItem = new JMenuItem((int)r.width + "x" + (int)r.height + "+" +
				 (int)(r.x - format.getImageableX()) + "+" +
				 (int)(r.y - format.getImageableY()));
	menuItem.setFont(new Font("SansSerif", Font.PLAIN, 12));
	popup.add(menuItem);
	popup.addSeparator();

	menuItem = new JMenuItem("Push back");
	menuItem.addActionListener(new ActionListenerWrapper(area) {
		public void actionPerformed(ActionEvent e) {
		    PrintArea area = (PrintArea)getValue();
		    printableSet.pushBack(area);
		    repaint();
		}
	    });

	popup.add(menuItem);

	if (area instanceof PrintTextArea) {
	    menuItem = new JMenuItem("Edit text");
	    menuItem.addActionListener(new ActionListenerWrapper(area) {
		    public void actionPerformed(ActionEvent e) {
			PrintTextArea area = (PrintTextArea)getValue();
			printableSet.setEditTextMode(area, true);
			repaint();
		    }
		});
	    popup.add(menuItem);

	    popup.add(makeFontMenu(area));
	}
	else if (area instanceof PrintHTMLArea) {
	    menuItem = new JMenuItem("Edit HTML");

	    menuItem.addActionListener(new ActionListenerWrapper(area) {
		    public void actionPerformed(ActionEvent e) {
			PrintHTMLArea area = (PrintHTMLArea)getValue();
			printableSet.setEditTextMode(area, true);
			repaint();
		    }
		});
	    
	    popup.add(menuItem);
	}

	if (area.getName().equals(PrintArea.YSCALE)) {
	    Object value = area.getHint("SHOW_ICONS");
	    menuItem = new JMenuItem(value != null ? "Hide icons" :
				     "Show icons");
	    menuItem.addActionListener(new ActionListenerWrapper(area) {
		    public void actionPerformed(ActionEvent e) {
			PrintArea area = (PrintArea)getValue();

			if (area.getHint("SHOW_ICONS") == null)
			    area.setHint("SHOW_ICONS", new Boolean(true));
			else
			    area.removeHint("SHOW_ICONS");
			repaint();
		    }
		});
	    
	    popup.add(menuItem);
	}

	if (area instanceof PrintImageArea) {
	    menuItem = new JMenuItem("Load image...");
	    menuItem.addActionListener(new ActionListenerWrapper(area) {
		    public void actionPerformed(ActionEvent e) {
			PrintImageArea area = (PrintImageArea)getValue();
			File file = DialogUtils.openFileChooser(new Frame(),
								"Load", 0, false);
			if (file == null)
			    return;
			
			area.setImageURL("file:" +
					 file.getAbsolutePath());
			repaint();
		    }
		});
	    
	    popup.add(menuItem);

	    menuItem = new JMenuItem("Adjust");
	    menuItem.addActionListener(new ActionListenerWrapper(area) {
		    public void actionPerformed(ActionEvent e) {
			PrintImageArea area = (PrintImageArea)getValue();
			area.adjust();
			repaint();
		    }
		});
	    
	    popup.add(menuItem);
	}

	menuItem = new JMenuItem(area.hasBorder() ? "Remove border" :
				 "Add border");
	menuItem.addActionListener(new ActionListenerWrapper(area) {
		public void actionPerformed(ActionEvent e) {
		    PrintArea area = (PrintArea)getValue();
		    area.hasBorder(!area.hasBorder());
		    repaint();
		}
	    });

	popup.add(menuItem);

	menuItem = new JMenuItem("Set BG color...");
	menuItem.addActionListener(new ActionListenerWrapper(area) {
		public void actionPerformed(ActionEvent e) {
		    PrintArea area = (PrintArea)getValue();
		    Color color = JColorChooser.showDialog
			(null, area.getName() + " background color",
			 area.getBGColor());
		    if (color != null) {
			area.setBGColor(color);
			repaint();
		    }
		}
	    });

	popup.add(menuItem);

	if (area instanceof PrintTextArea) {
	    menuItem = new JMenuItem("Set FG color...");
	    menuItem.addActionListener(new ActionListenerWrapper(area) {
		    public void actionPerformed(ActionEvent e) {
			PrintTextArea area = (PrintTextArea)getValue();
			Color color = JColorChooser.showDialog
			    (null, area.getName() + " foreground color",
			     area.getFGColor());
			if (color != null) {
			    area.setFGColor(color);
			    repaint();
			}
		    }
		});
	    popup.add(menuItem);
	}
	else if (area instanceof PrintHTMLArea) {
	    menuItem = new JMenuItem("Set Style Sheet...");
	    menuItem.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			File file = DialogUtils.openFileChooser
			    (new Frame(), "Load", 0, false);
			if (file == null) return;
			try {
			    printableSet.getTemplate().
				setCSSFile(file.getAbsolutePath(), printableSet);
			    repaint();
			} catch(Exception ex) {
			    InfoDialog.pop(printableSet.getView().getGlobalContext(), ex.getMessage());
			}	
		    }
		});
	    popup.add(menuItem);
	}


	menuItem = new JMenuItem("Set border color...");
	menuItem.addActionListener(new ActionListenerWrapper(area) {
		public void actionPerformed(ActionEvent e) {
		    PrintArea area = (PrintArea)getValue();
		    Color color = JColorChooser.showDialog
			(null, area.getName() + " border color",
			 area.getBDColor());
		    if (color != null) {
			area.setBDColor(color);
			repaint();
		    }
		}
	    });

	popup.add(menuItem);

	/*
	menuItem = new JMenuItem("Push front");
	menuItem.addActionListener(new ActionListenerWrapper(area) {
		public void actionPerformed(ActionEvent e) {
		    PrintArea area = (PrintArea)getValue();
		    printableSet.pushFront(area);
		    repaint();
		}
	    });

	popup.add(menuItem);
	*/

	menuItem = new JMenuItem("Remove");
	menuItem.addActionListener(new ActionListenerWrapper(area) {
		public void actionPerformed(ActionEvent e) {
		    PrintArea area = (PrintArea)getValue();
		    printableSet.suppress(area);
		    repaint();
		}
	    });

	popup.add(menuItem);

	return popup;
    }

    JMenu makeFontMenu(PrintArea area) {
	JMenu fontMenu = new JMenu("Font");
	Vector vfonts = new Vector();
	for (int fsz = FONT_MIN_SIZE; fsz <= FONT_MAX_SIZE; fsz +=FONT_INCR_SIZE) {
	    vfonts.add(fsz + " plain");
	    vfonts.add(new Font(FONT_FAMILY, Font.PLAIN, fsz));
	}

	for (int fsz = FONT_MIN_SIZE; fsz <= FONT_MAX_SIZE; fsz +=FONT_INCR_SIZE) {
	    vfonts.add(fsz + " bold");
	    vfonts.add(new Font(FONT_FAMILY, Font.BOLD, fsz));
	}

	for (int fsz = FONT_MIN_SIZE; fsz <= FONT_MAX_SIZE; fsz +=FONT_INCR_SIZE) {
	    vfonts.add(fsz + " italic");
	    vfonts.add(new Font(FONT_FAMILY, Font.ITALIC, fsz));
	}

	int sz = vfonts.size();
	for (int n = 0; n < sz; n += 2) {
	    String name = (String)vfonts.get(n);
	    Font font = (Font)vfonts.get(n+1);
	    JMenuItem menuItem = new JMenuItem(name);
	    menuItem.setFont(font);
	    Vector v = new Vector();
	    v.add(area);
	    v.add(font);
	    menuItem.addActionListener(new ActionListenerWrapper(v) {
		    public void actionPerformed(ActionEvent e) {
			Vector v = (Vector)getValue();
			PrintTextArea area = (PrintTextArea)v.get(0);
			area.setFont((Font)v.get(1));
			repaint();
		    }
		});

	    fontMenu.add(menuItem);
	}
	return fontMenu;
    }

    PrintableSet getPrintableSet() {return printableSet;}

    int GRID = 5;

    int roundX(int n) {
	if (!activeGrid()) return n;
	int img_x = (int)format.getImageableX()-1;
	return roundW(n - img_x) + img_x + 1;
    }

    int roundY(int n) {
	if (!activeGrid()) return n;
	int img_y = (int)format.getImageableY()-1;
	return roundH(n - img_y) + img_y;
    }

    int roundW(int n) {
	if (!activeGrid()) return n;
	int pn = (n/GRID)*GRID;
	if (n-pn <= pn+GRID-n)
	    return pn;
	return pn+GRID;
    }

    int roundH(int n) {
	if (!activeGrid()) return n;
	int pn = (n/GRID)*GRID;
	if (n-pn <= pn+GRID-n)
	    return pn;
	return pn+GRID;
    }

    void alignAreas() {
	Vector areas = printableSet.getTemplate().getAreas();
	int sz = areas.size();

	for (int n = 0; n < sz; n++) {
	    PrintArea area = (PrintArea)areas.get(n);
	    Rectangle2D.Double r = area.getArea();
	    area.setLocation(roundX((int)r.x), roundY((int)r.y));
	    area.setSize(roundW((int)r.width), roundH((int)r.height));
	}
    }

    private boolean show_grid = true, active_grid = true;

    boolean showGrid() {
	return show_grid;
    }

    void showGrid(boolean show_grid) {
	this.show_grid = show_grid;
    }

    boolean activeGrid() {
	return active_grid;
    }

    void activeGrid(boolean active_grid) {
	this.active_grid = active_grid;
    }

    void setGridWidth(int grid) {
	this.GRID = grid;
	alignAreas();
    }
}
