/*
 * @author Jack Ruder
 * @date Sep 12, 2022
 * */

import org.joml.Matrix4f;
import org.joml.Vector3f;
import java.util.Scanner;
import java.util.Vector;

/**
 * Class to compute transformations of points in 3D space
 *
 */
public class TransformCalculator {
	/**
	 * Get user input interactively in two stages. First, request transformations
	 * until a blank line is entered. Display the resulting 4x4 matrix, and prompt
	 * the user for points to transform, until the user enters a blank line.
	 * 
	 * @param args
	 *        pass
	 */
	public static void main(String[] args) {

		Matrix4f matrix = new Matrix4f(); // initialize identity matrix
		Scanner s = new Scanner(System.in);
		boolean exit = false;

		while (!exit) {// get user input until blank line
			String input = s.nextLine();
			if (input == "") {
				exit = true;
				break;
			}
			try {
				matrix = stringTransform(input.trim(), matrix); // apply transformation, otherwise throw
										// error on bad input
			} catch (Exception e) {
				System.out.println(e.getMessage());
				continue;
			}
		}

		System.out.println("The final transform matrix is:");
		System.out.println(matrix.toString()); // Display

		System.out.println("\nPlease enter 3D points to transform, and hit enter when done.");
		exit = false;

		while (!exit) {// get user point input until blank line
			String input = s.nextLine();
			if (input == "") {
				exit = true;
				break;
			}

			Vector3f v;
			try {
				// get point
				v = parsePoint(input.trim());
			} catch (Exception e) {
				System.out.println(e.getMessage());
				continue;
			}
			v.mulPosition(matrix); // matrix-vector product
			System.out.print("\tTRANSFORMED: ");
			System.out.println(v.toString());
		}

		System.out.println("Exiting");
	}

	/**
	 * Parse a string into a Vector3f object
	 * 
	 * @param s
	 *        string to parse
	 * @exception IllegalArgumentException
	 *            if string cannot be parsed
	 * @return Vector3f object
	 */
	public static Vector3f parsePoint(String s) {
		String[] sargs = s.split("\\s+");
		if (sargs.length != 3) { // check length
			throw new IllegalArgumentException("Could not parse point: expected 3 points, got "
					+ Integer.toString(sargs.length));
		} else {
			float[] args = new float[sargs.length];

			try { // ensure all arguments after first are floats
				for (int i = 0; i < args.length; i++) {
					args[i] = Float.parseFloat(sargs[i]); // make casts
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("Could not parse point: non-numerical arguments");
			}

			return new Vector3f(args[0], args[1], args[2]);
		}
	}

	/**
	 * Applies a transformation to a Matrix4f object as described by a given string
	 * 
	 * @param m
	 *        matrix to modify
	 * @param s
	 *        string to parse
	 * @exception IllegalArgumentException
	 *            if string cannot be parsed
	 * @return modified matrix
	 */
	public static Matrix4f stringTransform(String s, Matrix4f m) throws IllegalArgumentException {
		String[] sargs = s.split("\\s+"); // split whitespace

		if (sargs.length < 2) { // too few arguments
			throw new IllegalArgumentException("Could not parse transformation: no arguments provided");
		}
		float[] args = new float[sargs.length - 1]; // skip transform id
		try { // ensure all arguments after first are floats
			for (int i = 0; i < args.length; i++) {
				args[i] = Float.parseFloat(sargs[i + 1]);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not parse transformation: non-numerical arguments");
		}

		// Handle different commands, and call appropriate transformation
		int invalid = -1; // negative if invalid, otherwise store expected number of args
		switch (sargs[0].toUpperCase()) {
		// handle each input, calling appropriate matrix transform,
		// set invalid number of arguments to positive value if wrong number of args
		// supplied
		case "T": // translate
			if (args.length == 3) {
				return m.translate(args[0], args[1], args[2]);
			} else {
				invalid = 3;
			}
			break;

		case "S": // scale
			if (args.length == 1) { // all dims
				return m.scale(args[0]);
			} else if (args.length == 3) { // x, y, z
				return m.scale(args[0], args[1], args[2]);
			} else {
				throw new IllegalArgumentException(
						"Invalid number of arguments to \"S\": expected 1 or 3 arguments, got "
								+ Integer.toString(args.length));
			}

			// Rotations X, Y, Z
			// Rotation syntax expects degrees, JOML requires radians
			// Have to convert first
		case "RX":
			if (args.length == 1) { //theta
				float rad = (float)java.lang.Math.toRadians(args[0]);
				return m.rotateX(rad);
			} else {
				invalid = 1;
			}
			break;

		case "RY":
			if (args.length == 1) { //theta
				float rad = (float)java.lang.Math.toRadians(args[0]);
				return m.rotateY(rad);
			} else {
				invalid = 1;
			}
			break;

		case "RZ":
			if (args.length == 1) { //theta
				float rad = (float)java.lang.Math.toRadians(args[0]);
				return m.rotateZ(rad);
			} else {
				invalid = 1;
			}
			break;

		// look at
		case "L":
			if (args.length == 6) { // cx, cy, cz, tx, ty, tz
				float cx = args[0];
				float cy = args[1];
				float cz = args[2];
				float tx = args[3];
				float ty = args[4];
				float tz = args[5];
	
				Vector3f f = new Vector3f(tx-cx, ty-cy, tz-cz).normalize(); // forwards vector (vec pointing in -Z direction)

				if (f.equals(0,1,0) || f.equals(0,-1,0)) {
					throw new IllegalArgumentException("Could not parse transformation: cannot look directly up or down");
				}
	
				// r = ||f x y||
				Vector3f r = new Vector3f(); //TODO handle f = +-y
				f.cross(0,1,0,r); 
				r.normalize();

				// u = r x f
				Vector3f u = new Vector3f();
				r.cross(f,u);
				
				return m.lookAlong(f.negate(), u); // look at
														// (0,0,0)
			} else {
				invalid = 6;
			}
			break;

		// orthogonal projection
		case "O":

			if (args.length == 4) { //w, h, dn, df
				return m.orthoSymmetric(args[0], args[1], args[2], args[3]);
			} else {
				invalid = 4;
			}
			break;

		// perspective shift
		case "P":
			if (args.length == 4) { //horizontal fov, aspect ratio, dn, df
				float fovx = (float)java.lang.Math.toRadians(args[0]);
				float fovy = (float)(2 * java.lang.Math.atan(java.lang.Math.tan(fovx/2) / args[1])); //convert horizontal to vertical using trig
				return m.perspective(fovy, args[1], args[2], args[3]);
			} else {
				invalid = 4;
			}
			break;
		default: // user entered <invalid><whitespace>*
			throw new IllegalArgumentException("Could not parse transformation: unknown transform type");
		}
		// if we got here then the command was correctly entered with the wrong number
		// of args
		throw new IllegalArgumentException("Invalid number of arguments to " + sargs[0] + ": expected "
				+ Integer.toString(invalid) + " arguments, got " + Integer.toString(args.length));
	}

}
