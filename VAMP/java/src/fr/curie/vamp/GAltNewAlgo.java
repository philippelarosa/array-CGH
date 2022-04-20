
// ancien algo
class OldAlgo {
    private LinkedList computeRecurrentRegions(GraphPanelSet panel_set,
					       GraphPanel panel,
					       Vector graphElements, String gnl,
					       Vector bkp_v) {
	DataSet ds0 = (DataSet)graphElements.get(0);
	DataElement data[] = ds0.getData();

	// bkp_v ne contient que les breakpoints recurrents
	int size = bkp_v.size();

	Vector regions = new Vector();
	for (int n = 0; n < size; n++) {
	    Breakpoint bkp = (Breakpoint)bkp_v.get(n);
	    if (bkp.isIN() && bkp.getSupportCount() >= min_alt) {
		String inChr = VAMPUtils.getChr(data[bkp.ind]);
		for (int m = n+1; m < size; m++) {
		    Breakpoint bkp2 = (Breakpoint)bkp_v.get(m);
		    if (!inChr.equals(VAMPUtils.getChr(data[bkp2.ind])))
			break;
		    if (bkp2.isOUT()) {
			Vector inter = Breakpoint.getSupport(bkp, bkp2);
			if (inter.size() >= min_alt) {
			    regions.add(new RecurrentRegion(bkp, bkp2, inter));
			}
			break;
		    }
		}
	    }
	}
	    
	LinkedList reg_list = new LinkedList();
	    
	for (int n = 0; n < regions.size(); n++) {
	    RecurrentRegion r = (RecurrentRegion)regions.get(n);
	    if (TRACE_REG)
		System.out.println(r);
	    r.makeRegion(panel_set, panel, reg_list, graphElements, this);
	}

	postAddRegions(reg_list, getGNLString(gnl));
	return reg_list;
    }
}

// nouvel algo
class NewAlgo {
    private LinkedList computeRecurrentRegions(GraphPanelSet panel_set,
					       GraphPanel panel,
					       Vector graphElements, String gnl,
					       Vector bkp_v) {
	DataSet ds0 = (DataSet)graphElements.get(0);
	DataElement data[] = ds0.getData();

	// bkp_v ne contient tous les breakpoints cumulés
	// avec bkp.isRecurrent() == true pour les bkp recurrents
	// avec bkp.isRecurrent() == false pour les bkp non recurrents
	int size = bkp_v.size();

	Vector regions = new Vector();
	for (int n = 0; n < size; n++) {
	    Breakpoint bkp = (Breakpoint)bkp_v.get(n);
	    if (bkp.isIN() && bkp.getSupportCount() >= min_alt) {
		String inChr = VAMPUtils.getChr(data[bkp.ind]);
		for (int m = n+1; m < size; m++) {
		    Breakpoint bkp2 = (Breakpoint)bkp_v.get(m);
		    if (!inChr.equals(VAMPUtils.getChr(data[bkp2.ind])))
			break;

		    if (bkp2.isOUT() && bkp2.isRecurrent()) { // NEW
			Vector inter = Breakpoint.getSupport(bkp, bkp2);
			if (inter.size() >= min_alt) {
			    regions.add(new RecurrentRegion(bkp, bkp2, inter));
			}
			break;
		    }

		    // NEW
		    if (bkp2.isIN()) {
			bkp.suppressFromSupport(bkp2.getSupport());
			if (bkp.getSupportCount() < min_alt)
			    break;
		    }
		}
	    }
	}
	    
	LinkedList reg_list = new LinkedList();
	    
	for (int n = 0; n < regions.size(); n++) {
	    RecurrentRegion r = (RecurrentRegion)regions.get(n);
	    if (TRACE_REG)
		System.out.println(r);
	    r.makeRegion(panel_set, panel, reg_list, graphElements, this);
	}

	postAddRegions(reg_list, getGNLString(gnl));
	return reg_list;
    }
}

// TODO
// - ajouter attribut is_recurrent + methodes setRecurrent(boolean) et
//   isRecurrent() dans les Breakpoint
// - ajouter Breakpoint.suppressFromSupport()
// - passer le tableau des breakpoints cumul et non des recurrents =>
//   ~merge des deux tableaux

