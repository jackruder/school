public class Transform {
	public static void main(String[] args) throws IllegalArgumentException {
		Tx t = parse(args[0]);
		System.out.println(t.toString());


	}

	/**
	 * Returns a Tx object from a given string
	 * 
	 * @param  s string to parse
	 * @exception IllegalArgumentException if string cannot be parsed
	 * @return tx object
	 * */
	public static Tx parse(String s) throws IllegalArgumentException {
		String[] sargs = s.split("\\s+");

		if (sargs.length < 2) { //too few arguments
				throw new IllegalArgumentException("Could not parse transformation: no arguments provided");
		}
		double[] args = new double[sargs.length-1];

		try { // ensure all arguments after first are doubles 
			for (int i = 0; i < args.length; i++) {
				args[i] = Double.parseDouble(sargs[i + 1]);
			}

		} catch(IllegalArgumentException e) {
				throw new IllegalArgumentException("Could not parse transformation: non-numerical arguments");
		}

		// Handle different commands, and call appropriate constructors
		int invalid = -1; // negative if invalid, otherwise store expected number of args
		switch(sargs[0].toUpperCase()) {
			// handle each input, calling appropriate default constructor,
			// set invalid number of arguments to positive value if wrong number of args supplied
			case "T":
				if (args.length == 3) {
					return new tTx(args[0], args[1], args[2]);
				}
				else {
					invalid = 3;
				}
				break;
			case "S":
				if (args.length == 1) {
					return new sTx(args[0]);
				}
				else if (args.length == 3) {
					return new s3Tx(args[0], args[1], args[2]);
				}
				else {
		 			throw new IllegalArgumentException("Invalid number of arguments to \"S\": expected 1 or 3 arguments, got " + Integer.toString(args.length));
				}
			case "RX":
				if (args.length == 1) {
					return new rxTx(args[0]);	
				}
				else {
					invalid = 1;
				}
				break;

			case "RY":
				if (args.length == 1) {
					return new ryTx(args[0]);
				}
				else {
					invalid = 1;
				}
				break;

			case "RZ":
				if (args.length == 1) {
					return new rzTx(args[0]);
				}
				else {
					invalid = 1;
				}
				break;
			case "L":
				if (args.length == 6) {
					return new lTx(args[0], args[1], args[2], args[3], args[4], args[5]);
				}
				else {
					invalid = 6;
				}
				break;
			case "O":

				if (args.length == 4) {
					return new oTx(args[0], args[1], args[2], args[3]);
				}
				else {
					invalid = 4;
				}
				break;
			case "P":
				if (args.length == 4) {
					return new pTx(args[0], args[1], args[2], args[3]);
				}
				else {
					invalid = 4;
				}
				break;
			default:
				throw new IllegalArgumentException("Could not parse transformation: unknown transform type");
		}
		// if we got here then the command was correctly entered with the wrong number of args
	 	throw new IllegalArgumentException("Invalid number of arguments to " + sargs[0] + ": expected " + Integer.toString(invalid) + " arguments, got " + Integer.toString(args.length));
	}
}




interface Tx {
	/** 
	 * double dispatch accept method
	 * */
	public void accept(txVisitor v);
	
	/**
	 * Return the string representation of the transformation, in 
	 * the same format it may be parsed from
	 *
	 * @return the string representation
	 * */
	public String toString();
}



interface txVisitor {
	public void visit(tTx tx); 
	public void visit(sTx tx); 
	public void visit(s3Tx tx);
	public void visit(rxTx tx);
	public void visit(ryTx tx);
	public void visit(rzTx tx);
	public void visit(lTx tx) ;
	public void visit(oTx tx) ;
	public void visit(pTx tx) ;
}

class tTx implements Tx{
	double dx;
	double dy;
	double dz;

	tTx(double dx, double dy, double dz) {
		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
	}

	@Override
	public void accept(txVisitor v) {
		v.visit(this);
	}

	@Override
	public String toString() {
		return "T " + Double.toString(this.dx) + " " + Double.toString(this.dy) + " " + Double.toString(this.dz);
	}
}
class sTx implements Tx{
	double s;

	sTx(double s) {
		this.s = s;
	}
	@Override
	public void accept(txVisitor v) {
		v.visit(this);
	}
	@Override
	public String toString() {
		return "S " + Double.toString(this.s);
	}
}
class s3Tx implements Tx{
	double sx;
	double sy;
	double sz;

	s3Tx(double sx, double sy, double sz) {
		this.sx = sx;
		this.sy = sy;
		this.sz = sz;
	}
	@Override
	public void accept(txVisitor v) {
		v.visit(this);
	}
	@Override
	public String toString() {
		return  "S " + Double.toString(this.sx) + " " +  Double.toString(this.sy) + " " + Double.toString(this.sz);
	}
}

class rxTx implements Tx{
	double theta;

	rxTx(double theta) {
		this.theta = theta;
	}
	@Override
	public void accept(txVisitor v) {
		v.visit(this);
	}
	@Override
	public String toString() {
		return  "RX " + Double.toString(this.theta);
	}
}
class ryTx implements Tx{
	double theta;

	ryTx(double theta) {
		this.theta = theta;
	}
	@Override
	public void accept(txVisitor v) {
		v.visit(this);
	}
	@Override
	public String toString() {
		return  "RY " + Double.toString(this.theta);
	}
}
class rzTx implements Tx{
	double theta;

	rzTx(double theta) {
		this.theta = theta;
	}

	@Override
	public void accept(txVisitor v) {
		v.visit(this);
	}
	@Override
	public String toString() {
		return "RZ " + Double.toString(this.theta);
	}
}
class lTx implements Tx {
	double cx;
	double cy;
	double cz;
	double tx;
	double ty;
	double tz;

	lTx(double cx, double cy, double cz, double tx, double ty, double tz) {
		this.cx = cx;
		this.cy = cy;
		this.cz = cz;
		this.tx = tx;
		this.ty = ty;
		this.tz = tz;
	}
	@Override
	public void accept(txVisitor v) {
		v.visit(this);
	}
	@Override
	public String toString() {
		return "L " + Double.toString(this.cx) + " " + Double.toString(this.cy) + " " + Double.toString(this.cz) + " " + Double.toString(this.tx) + " " + Double.toString(this.ty) + " " + Double.toString(this.tz);
	}
}
class oTx implements Tx{
	double w;
	double h;
	double dn;
	double df;

	oTx(double w, double h, double dn, double df) {
		this.w = w;
		this.h = h;
		this.dn = dn;
		this.df = df;
	}
	@Override
	public void accept(txVisitor v) {
		v.visit(this);
	}
	@Override
	public String toString() {
		return "O " + Double.toString(this.w) + " " + Double.toString(this.h) + " " + Double.toString(this.dn) + " " + Double.toString(this.df);
	}
}
class pTx implements Tx{
	double p;
	double a;
	double dn;
	double df;

	pTx(double p, double a, double dn, double df) {
		this.p = p;
		this.a = a;
		this.dn = dn;
		this.df = df;
	}

	@Override
	public void accept(txVisitor v) {
		v.visit(this);
	}

	@Override
	public String toString() {
		return "P " + Double.toString(this.p) + " " + Double.toString(this.a) + " " + Double.toString(this.dn) + " " + Double.toString(this.df);
	}
}
