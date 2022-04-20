
package fr.curie.vamp.utils;

abstract public class Dichotomic {

    private int min, max;
    private int lastIterCount = 0;
    private int flags;
    private boolean verbose = false;

    public static final int CLOSER = 1;
    public static final int LOWER = 2;
    public static final int UPPER = 3;

    private static final int INVALID = Integer.MAX_VALUE;

    protected Dichotomic(int min, int max) {
	this(min, max, CLOSER);
    }

    protected Dichotomic(int min, int max, int flags) {
	this.min = min;
	this.max = max;
	this.flags = flags;
    }

    public int getMin() {
	return min;
    }

    public int getMax() {
	return max;
    }

    public int getLastIterCount() {
	return lastIterCount;
    }

    abstract public int getValue(int n);

    public int find() {
	int l = getMin();
	int r = getMax();
	int n = 0;

	lastIterCount = 0;

	int upper_n_last = INVALID;
	int upper_x_last = INVALID;
	int lower_n_last = INVALID;
	int lower_x_last = INVALID;

	while (l <= r) {
	    n = (l + r) >> 1;
	    int x = getValue(n);
	    if (verbose) {
		System.out.println("l " + l + ", r " + r + ", n " + n + ", x " + x);
	    }

	    if (x < 0) {
		l = n + 1;
	    }
	    else {
		r = n - 1;
	    }

	    lastIterCount++;

	    if (x == 0 || l > r) {
		if (flags == UPPER) {
		    if (x < 0) {
			if (upper_n_last != INVALID) {
			    n = upper_n_last;
			    x = upper_x_last;
			}
			else {
			    x = -x;
			}
		    }
		}
		else if (flags == LOWER) {
		    if (x > 0) {
			if (lower_n_last != INVALID) {
			    n = lower_n_last;
			    x = -lower_x_last;
			}
		    }
		    else {
			x = -x;
		    }
		}
		else if (flags == CLOSER) {
		    if (x > 0) {
			if (lower_n_last != INVALID && -lower_x_last < x) {
			    x = -lower_x_last;
			    n = lower_n_last;
			}
		    }
		    else if (x < 0) {
			if (upper_n_last != INVALID && upper_x_last < -x) {
			    x = upper_x_last;
			    n = upper_n_last;
			}
			else {
			    x = -x;
			}
		    }
		}

		n = getClosest(n);
		break;
	    }

	    if (x > 0) {
		upper_n_last = n;
		upper_x_last = x;
	    }
	    else if (x < 0) {
		lower_n_last = n;
		lower_x_last = x;
	    }
	}

	if (verbose) {
	    System.out.println("returning " + n);
	}

	return n;
    }

    public int getClosest(int n) {
	return n;
    }
}
