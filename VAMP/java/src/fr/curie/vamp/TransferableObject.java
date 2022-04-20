
/*
 *
 * TransferableObject.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2007
 *
 */

package fr.curie.vamp;

import java.awt.datatransfer.*;
import java.util.*;
import java.io.*;

public class TransferableObject implements Transferable {
    static DataFlavor pasteableFlavor = new DataFlavor(Pasteable.class, "Pasteable Object");

    static DataFlavor textPlainFlavor = DataFlavor.getTextPlainUnicodeFlavor();

    static DataFlavor[] supportedFlavors = {
	pasteableFlavor,
	textPlainFlavor
    };

    // list of PropertyElement
    LinkedList list;

    public TransferableObject(LinkedList list) {
	this.list = list;
    }

    public DataFlavor[] getTransferDataFlavors() {
	return supportedFlavors;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
	for (int n = 0; n < supportedFlavors.length; n++)
	    if (flavor.equals(supportedFlavors[n]))
		return true;
	return false;
    }

  public Object getTransferData(DataFlavor flavor) 
      throws UnsupportedFlavorException, IOException
  {
      if (flavor.equals(textPlainFlavor)) {
	  Object o;
	  if (list.size() == 1 &&
	      (o = list.get(0)) instanceof PropertyElement) {
	      Object id = ((PropertyElement)o).getID();
	      if (id != null)
		  return id;
	  }
      }

      if (flavor.equals(pasteableFlavor))
	  return list;
      throw new UnsupportedFlavorException(flavor);
  }
}

