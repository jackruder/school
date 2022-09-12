import org.joml.Matrix4d;
import org.joml.Vector3d;
import java.util.Scanner;

public class Transform {
	public static void main(String[] args) throws IllegalArgumentException {
		Matrix4d matrix = new Matrix4d();
		Scanner s = new Scanner(System.in);
		boolean exit = false;

		while (!exit) {//get user input until blank line
			String input = s.nextLine(); 
			if (input == "") {
				exit = true;
				break;
			}
			try {
				matrix = stringTransform(input, matrix); // apply transformation, otherwise throw error on bad input
			} catch (Exception e) {
				System.out.println(e.getMessage());
				continue;
			}
		}
		
		System.out.println("The final transform matrix is:");
		System.out.println(matrix.toString()); // Display 
		
		System.out.println("\nPlease enter 3D points to transform, and hit enter when done.");
		exit = false;

		while (!exit) {//get user input until blank line
			String input = s.nextLine(); 
			if (input == "") {
				exit = true;
				break;
			}
			Vector3d v;
			try{
				v = parsePoint(input).mulPosition(matrix);
			} catch (Exception e) {
				System.out.println(e.getMessage());
				continue;
			}
			System.out.print("\tTransformed: ");
			System.out.println(v.toString());
		}
	}

	/**
	 * Parse a string into a Vector3d object
	 * 
	 * @param s string to parse
	 * @exception IllegalArgumentException if string cannot be parsed
	 * @return Vector3d object
	 * */
	public static Vector3d parsePoint(String s) {
		String[] sargs = s.split("\\s+");
		if (sargs.length != 3) { //check length
			throw new IllegalArgumentException("Could not parse point: expected 3 points, got " + Integer.toString(sargs.length));
		}
		else {
			double[] args = new double[sargs.length];

			try { // ensure all arguments after first are doubles 
				for (int i = 0; i < args.length; i++) {
					args[i] = Double.parseDouble(sargs[i]);
				}
			} catch(Exception e) {
					throw new IllegalArgumentException("Could not parse point: non-numerical arguments");
			}

			return new Vector3d(args[0], args[1], args[2]);
		}
	}

	/**
	 * Applies a transformation to a Matrix4d object as described by a given string
	 * 
	 * @param  m matrix to modify
	 * @param  s string to parse
	 * @exception IllegalArgumentException if string cannot be parsed
	 * @return modified matrix
	 * */
	public static Matrix4d stringTransform(String s, Matrix4d m) throws IllegalArgumentException {
		String[] sargs = s.split("\\s+");

		if (sargs.length < 2) { //too few arguments
				throw new IllegalArgumentException("Could not parse transformation: no arguments provided");
		}
		double[] args = new double[sargs.length-1];
		try { // ensure all arguments after first are doubles 
			for (int i = 0; i < args.length; i++) {
				args[i] = Double.parseDouble(sargs[i + 1]);
			}
		} catch(Exception e) {
				throw new IllegalArgumentException("Could not parse transformation: non-numerical arguments");
		}

		// Handle different commands, and call appropriate constructors
		int invalid = -1; // negative if invalid, otherwise store expected number of args
		switch(sargs[0].toUpperCase()) {
			// handle each input, calling appropriate default constructor,
			// set invalid number of arguments to positive value if wrong number of args supplied
			case "T":
				if (args.length == 3) {
					return m.translate(args[0],args[1],args[2]);
				}
				else {
					invalid = 3;
				}
				break;
			case "S":
				if (args.length == 1) {
					return m.scale(args[0]);
				}
				else if (args.length == 3) {
					return m.scale(args[0], args[1], args[2]);
				}
				else {
		 			throw new IllegalArgumentException("Invalid number of arguments to \"S\": expected 1 or 3 arguments, got " + Integer.toString(args.length));
				}

			// Rotation syntax expects degrees, JOML requires radians
			// Have to convert first
			case "RX":
				if (args.length == 1) {
					double rad = java.lang.Math.toRadians(args[0]);
					return m.rotateX(rad);
				}
				else {
					invalid = 1;
				}
				break;

			case "RY":
				if (args.length == 1) {
					double rad = java.lang.Math.toRadians(args[0]);
					return m.rotateY(rad);
				}
				else {
					invalid = 1;
				}
				break;

			case "RZ":
				if (args.length == 1) {
					double rad = java.lang.Math.toRadians(args[0]);
					return m.rotateZ(rad);
				}
				else {
					invalid = 1;
				}
				break;
			case "L":
				if (args.length == 6) {
					return m.lookAlong(args[0], args[1], args[2], args[3], args[4], args[5]);				}
				else {
					invalid = 6;
				}
				break;
			case "O":

				if (args.length == 4) {
					return m.orthoSymmetric(args[0], args[1], args[2], args[3]);
				}
				else {
					invalid = 4;
				}
				break;
			case "P":
				if (args.length == 4) {
					return m.perspective(args[0], args[1], args[2], args[3]);
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
