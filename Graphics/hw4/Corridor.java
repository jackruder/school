
/**
 * @author Jack Ruder
 * 
 *  
 */

import java.io.*;
import java.util.*;
import java.nio.*;
import java.lang.Math;
import javax.swing.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.texture.*;
import com.jogamp.opengl.util.*;
import org.joml.*;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.awt.geom.AffineTransform;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;

public class Corridor extends JFrame implements GLEventListener {
	// constants
	private static int WINDOW_WIDTH = 800, WINDOW_HEIGHT = 600;
	private static final String WINDOW_TITLE = "Corridor";
	private static final String VERTEX_SHADER_FILE = "corridor-vertex.glsl",
			FRAGMENT_SHADER_FILE = "corridor-fragment.glsl";

	// window fields
	private GLCanvas glCanvas;
	private int renderingProgram;
	private int[] vao = new int[1];
	private int[] vbo = new int[2];


	// allocate variables for display() function
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4f pMat = new Matrix4f(); // perspective matrix
	private Matrix4f vMat = new Matrix4f(); // view matrix
	private Matrix4f[] mMats = new Matrix4f[4]; // model matrix, n,e,s,w

	private int pLoc,vLoc,mLoc,nLoc;
	private float aspect;
		
	private final float cameraTravelLength = 900.0f;
	private final float corridorLength = 1000.0f;
	private final float corridorHeight = 100.0f;
	private final float baseHeight = 2.0f * corridorHeight / 3.0f; // 2/3 total height
	private Vector3f backBottomLeft; // coordinates
	private Vector3f backBottomRight; //coordinates
	private Vector3f frontBottomLeft;
	private Vector3f frontBottomRight;

	private float walkingPeriod;
	private float turningPeriod;
	private int stepCount;
	private float stepHeight;

	private String[] texFiles;
	private int[] texRefs;


	private long startTime; // for update()
	private float lastL; // for update()


	private float cameraX, cameraY, cameraZ;
	private float targetX, targetY, targetZ;
	private int corridorLocation; //1, 2, 3, 4 means in the corridor, 
				      //negative means in the corner at beginning of corridor. N E S W.
	/**
	 * Create a new Corridor and kick off animation.
	 */

	public Corridor(float walkingPeriod, float turningPeriod, int stepCount, float stepHeight, String[] texFiles) {
		setTitle(WINDOW_TITLE);
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);

		glCanvas = new GLCanvas();
		glCanvas.addGLEventListener(this);

		Animator animator = new Animator(glCanvas);
		animator.start();

		this.add(glCanvas);
		this.setVisible(true);

		this.backBottomLeft = new Vector3f(-corridorLength / 2.0f, 0.0f, -corridorLength / 2.0f);
		this.backBottomRight = new Vector3f(corridorLength / 2.0f, 0.0f, -corridorLength / 2.0f);
		this.frontBottomLeft = new Vector3f(-corridorLength / 2.0f, 0.0f, corridorLength / 2.0f);
		this.frontBottomRight = new Vector3f(corridorLength / 2.0f, 0.0f, corridorLength / 2.0f);

		this.walkingPeriod = walkingPeriod;
		this.turningPeriod = turningPeriod;
		this.stepCount = stepCount;
		this.stepHeight = stepHeight;
		this.texFiles = texFiles;
		this.texRefs = new int[texFiles.length];


	}

	/**
	 * Draw a frame to screen. This includes recomputing all matrices, and loading
	 * buffers onto the GPU, drawing triangles as it goes.
	 *
	 */

	@Override
	public void display(GLAutoDrawable drawable) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glClear(GL_DEPTH_BUFFER_BIT);

		update(); // update object locations

		gl.glUseProgram(renderingProgram);


		aspect = (float) glCanvas.getWidth() / (float) glCanvas.getHeight();
		pMat.setPerspective((float) Math.toRadians(95.0f), aspect, 10.0f, 1000.0f);
		vMat.setLookAt(cameraX,cameraY,cameraZ,targetX,targetY,targetZ,0.0f,1.0f,0.0f); //look at center, eye level with fan center
		vLoc = gl.glGetUniformLocation(renderingProgram, "v_matrix");
		pLoc = gl.glGetUniformLocation(renderingProgram, "p_matrix");


		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));

		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));

		/* do the drawing */
		// load corridor positions
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		//load corridor textures
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(1,2,GL_FLOAT, false,0,0);
		gl.glEnableVertexAttribArray(1);

		// draw each corridor
		gl.glDrawArraysInstanced(GL_TRIANGLES,0,24,4);

	}

	/**
	 * Do the initial setup for drawing
	 */

	@Override
	public void init(GLAutoDrawable drawable) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		// enable z buffer
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glEnable(GL_CULL_FACE);

		renderingProgram = Utils.createShaderProgram(VERTEX_SHADER_FILE, FRAGMENT_SHADER_FILE); // compile and
													// link
		gl.glUseProgram(renderingProgram);

		
		// try to load texture files
		for (int i = 0; i < texFiles.length; i++) {
			try {
			    this.texRefs[i] = loadTexture(texFiles[i]);
			} catch (IOException e) {
			    System.out.println("Could not find texture in file \"" + texFiles[i] + "\": " + e.getMessage());
			    System.exit(-1);
			}
		}
	
		// at least bind one texture
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, texRefs[0]);
		
		// bind as many more as we can
		if (texRefs.length > 1) {
			gl.glActiveTexture(GL_TEXTURE1);
			gl.glBindTexture(GL_TEXTURE_2D, texRefs[1]);
		}

		if (texRefs.length > 2) {
			gl.glActiveTexture(GL_TEXTURE2);
			gl.glBindTexture(GL_TEXTURE_2D, texRefs[2]);
		}

		if (texRefs.length > 3) {
			gl.glActiveTexture(GL_TEXTURE3);
			gl.glBindTexture(GL_TEXTURE_2D, texRefs[3]);
		}
	

		// load uniforms
		mLoc = gl.glGetUniformLocation(renderingProgram, "m_matrix");
		nLoc = gl.glGetUniformLocation(renderingProgram, "num_textures");

		gl.glUniformMatrix4fv(mLoc, 4, false, genCorridorModelMatrices()); // 4 matrices here
		gl.glUniform1i(nLoc,texRefs.length);

		setupVertices(); // load vbos
		start(); // kick off 'engine'
	}
	
	/*
	 * Initialize/Reset camera and target positions.
	 */
	private void start() {
		this.startTime = System.currentTimeMillis();
		cameraX = -cameraTravelLength/2.0f;
		cameraZ = -cameraTravelLength/2.0f;
		cameraY = baseHeight;
		targetX = corridorLength/2.0f;
		targetY = baseHeight;
		targetZ = -cameraTravelLength/2.0f;
		corridorLocation = 1;
	}

	/*
	 * Initialize vao and vbos containing object positions and colors
	 */
	private void setupVertices() {
		GL4 gl = (GL4) GLContext.getCurrentGL(); 

//		int[] corridorIndices = {0,1,2,3,99999,4,5,6,7,99999,0,3,5,4,99999,2,1,7,6};
		float[] corridorPositions = { 0.0f, 0.0f,   0.0f,     100.0f,  0.0f, -100.0f,    100.0f, 0.0f, -900.0f,
					      0.0f, 0.0f, 0.0f,       100.0f, 0.0f, -900.0f,     0.0f, 0.0f, -1000.0f, // bottom
														       
					      0.0f, 100.0f, 0.0f,     0.0f,  100.0f, -1000.0f,   100.0f, 100.0f, -900.0f,
					      0.0f, 100.0f, 0.0f,     100.0f, 100.0f, -900.0f,   100.0f, 100.0f,-100.0f, // top
															 
					      0.0f, 0.0f, 0.0f,       0.0f, 0.0f, -1000.0f,      0.0f, 100.0f, -1000.0f, 
					      0.0f, 0.0f, 0.0f,       0.0f, 100.0f, -1000.0f,    0.0f, 100.0f, 0.0f,	//long side
															
					      100.0f, 0.0f, -900.0f,  100.0f, 0.0f, -100.0f,    100.0f, 100.0f, -100.0f,
					      100.0f, 0.0f, -900.0f, 100.0f, 100.0f, -100.0f, 100.0f, 100.0f, -900.0f //short side$
					//
		};// tube body with bottom left close point at origin, 1000 long, 100 wide

		float[] corridorTexPos =    { 0.0f, 0.0f, 1.0f,1.0f, 1.0f,9.0f,  //bottom
					     0.0f, 0.0f, 1.0f, 9.0f, 0.0f, 10.0f,
					     
					      1.0f, 0.0f, 1.0f,10.0f, 0.0f,9.0f,
					      1.0f, 0.0f, 0.0f,9.0f, 0.0f,1.0f, // top aligned with bottom
										 //
					      0.0f, 0.0f,   10.0f, 0.0f, 10.0f, 1.0f,
					      0.0f, 0.0f,   10.0f, 1.0f, 0.0f, 1.0f,	//long side up

					      0.0f, 0.0f, 8.0f,0.0f, 8.0f, 1.0f,// short side up
					      0.0f, 0.0f, 8.0f,1.0f, 0.0f, 1.0f // 
		}; // texture coords

		// vao
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(vbo.length, vbo, 0);

		// corridor positions
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer corBuf = Buffers.newDirectFloatBuffer(corridorPositions);
		gl.glBufferData(GL_ARRAY_BUFFER, corBuf.limit() * 4, corBuf, GL_STATIC_DRAW);

		//texture positions
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer corTexBuf = Buffers.newDirectFloatBuffer(corridorTexPos);		
		gl.glBufferData(GL_ARRAY_BUFFER, corTexBuf.limit() * 4, corTexBuf, GL_STATIC_DRAW);

	}

	/*
	 * Generate model matrices to build corridor so that the corridor is centered at
	 * 0,50,0.
	 * 
	 * @return Float Buffer containing 4 matrices, ordered by N,E,S,W
	 */
	private FloatBuffer genCorridorModelMatrices() {
		Matrix4f m = new Matrix4f();
		float[] corridorModelMatrices = new float[64];

		//North
		m.identity();
		m.translate(this.backBottomLeft);
		m.rotateY(-(float)Math.PI/2.0f);
		corridorModelMatrices = m.get(corridorModelMatrices, 0 * 16);

		//East
		m.identity();
		m.translate(this.backBottomRight);
		m.rotateY((float)Math.PI);
		corridorModelMatrices = m.get(corridorModelMatrices, 1 * 16);

		//South
		m.identity();
		m.translate(this.frontBottomRight);
		m.rotateY((float)Math.PI/2.0f);
		corridorModelMatrices = m.get(corridorModelMatrices, 2 * 16);

		//West
		m.identity();
		m.translate(this.frontBottomLeft);
		corridorModelMatrices = m.get(corridorModelMatrices, 3 * 16);
		
		return Buffers.newDirectFloatBuffer(corridorModelMatrices);
	}


	/* update camera position, and  */
	private void update() {
		long elapsedTime = System.currentTimeMillis() - startTime;	
		if (corridorLocation < 0) { // we are at a corner
			float t = elapsedTime % (1000.0f * turningPeriod); // time spent in turn
		 	float l = t / (1000.0f * turningPeriod); // proportion of time till done
				
			if (l < lastL) { //values wrapped, we have finished finished turning
				if (corridorLocation == -1){ //north
					corridorLocation = 1; // look/head east
					targetX = corridorLength / 2.0f;
					targetZ = -cameraTravelLength/2.0f;
				} else if (corridorLocation == -2){ //east 
					corridorLocation = 2; //look/head south
					targetX = cameraTravelLength/2.0f;
					targetZ = corridorLength/2.0f;
				} else if (corridorLocation == -3){ //south
					corridorLocation = 3; //look/head west
					targetX = -corridorLength/2.0f;
					targetZ = cameraTravelLength/2.0f;
				} else if (corridorLocation == -4){ //west
					corridorLocation = 4; //look/head north
					targetX = -cameraTravelLength/2.0f;
					targetZ = -corridorLength/2.0f;
				}
				startTime = System.currentTimeMillis();
				lastL = 0.0f;
				return;
			}

			float theta = (float)Math.PI/2.0f * (1-l); //
			float i = corridorLength * (float)Math.cos(theta); //new direction to face, start 0, end 1
			float j = corridorLength * (float)Math.sin(theta); //original direction, start 1, end 0
			if (corridorLocation == -1) {
				targetX = cameraX + i; // rotate around northwest corner
				targetZ = cameraZ - j;
			} else if (corridorLocation == -2) {
				targetX = cameraX + j; //rotate around northeast corner
				targetZ = cameraZ + i;
			} else if (corridorLocation == -3) {
				targetX = cameraX - i; //rotate around southeast corner
				targetZ = cameraZ + j;
			} else if (corridorLocation == -4) {
				targetX = cameraX - j; //rotate around southwest corner
				targetZ = cameraZ - i;
			}

			lastL = l; // check to see if we wrapped to 0 

		} else if (corridorLocation > 0) { // we are in a corridor, walking
			float t = elapsedTime % (1000.0f * walkingPeriod); // time spent in corridor
			float l = t / (1000.0f * walkingPeriod); // proportion of traveled length
			
				
			if (l < lastL) { //Values wrapped, we entered a new corner. Fix position
				if (corridorLocation == 1) { //north
						corridorLocation = -2; //enter east
						cameraX = cameraTravelLength/2.0f;
				} else if (corridorLocation == 2) { //south
						corridorLocation = -3;  //enter south
						cameraZ = cameraTravelLength/2.0f;
				} else if (corridorLocation == 3) { //east
						corridorLocation = -4; //enter west
						cameraX = -cameraTravelLength/2.0f;
				} else if (corridorLocation == 4) { //west
						corridorLocation = -1; //enter north
						cameraZ = -cameraTravelLength/2.0f;
				}
				cameraY = baseHeight;
				startTime = System.currentTimeMillis();
				lastL = 0.0f;
				return;
			}
			

			float d = l * cameraTravelLength - cameraTravelLength/2; //calculate pos	
			//assign pos to correct axis
			if (corridorLocation == 1) { //north
					cameraX = d;
			} else if (corridorLocation == 2) { //south
					cameraZ = d;
			} else if (corridorLocation == 3) { //east 
					cameraX = -d;
			} else if (corridorLocation == 4) { //west
					cameraZ = -d;
			}
			lastL = l; // check for l wrap to 0 

			//calculate bounce
			double theta = l * stepCount * Math.PI;
			float b = stepHeight * (float)Math.abs(Math.sin(theta));
			cameraY = baseHeight + b;
		}
	}

	/**
	 * main() just makes the object.
	 */
	
	/*
	 *  Get command line input, check if valid
	 */
	public static void main(String[] args) {
		float walkingPeriod;
		float turningPeriod;
		int stepCount;
		float stepHeight;

		if (args.length < 5 || args.length > 8) {
			System.out.println("Incorrect number of arguments provided, expected 5 to 8 arguments");
		}

		try {
			walkingPeriod = Float.parseFloat(args[0]);
		} catch (Exception e) {
			System.out.println("Expected a numerical first argument: " + e.getMessage());
			return;
		}

		try {
			turningPeriod = Float.parseFloat(args[1]);
		} catch (Exception e) {
			System.out.println("Expected a numerical second argument: " + e.getMessage());
			return;
		}

		try {
			stepCount = Integer.parseInt(args[2]);
		} catch (Exception e) {
			System.out.println("Expected an integer third argument: " + e.getMessage());
			return;
		}

		try {
			stepHeight = Float.parseFloat(args[3]);
		} catch (Exception e) {
			System.out.println("Expected a numerical fourth argument: " + e.getMessage());
			return;
		}

		int n_tex_files = args.length - 4;

		String[] texFiles = new String[n_tex_files];
		for (int i = 0; i < n_tex_files; i++) {
			texFiles[i] = args[i + 4];
		}

		new Corridor(walkingPeriod, turningPeriod, stepCount, stepHeight, texFiles); 


	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
	}

	private static int loadTexture(String textureFileName) throws IOException, GLException{
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int finalTextureRef;
		Texture tex = null;
		tex = TextureIO.newTexture(new File(textureFileName), false);
		finalTextureRef = tex.getTextureObject();

		// building a mipmap and use anisotropic filtering
		gl.glBindTexture(GL_TEXTURE_2D, finalTextureRef);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		gl.glGenerateMipmap(GL_TEXTURE_2D);
		if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic"))
		{	float anisoset[] = new float[1];
			gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, anisoset, 0);
			gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, anisoset[0]);
		}
		return finalTextureRef;
	}
}





























class Utils {
	// private constructor prevents instantiation (static only)
	private Utils() {}

	public static int createShaderProgram(String vS, String tCS, String tES, String gS, String fS) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int vShader  = prepareShader(GL_VERTEX_SHADER, vS);
		int tcShader = prepareShader(GL_TESS_CONTROL_SHADER, tCS);
		int teShader = prepareShader(GL_TESS_EVALUATION_SHADER, tES);
		int gShader = prepareShader(GL_GEOMETRY_SHADER, gS);
		int fShader  = prepareShader(GL_FRAGMENT_SHADER, fS);
		int vtgfprogram = gl.glCreateProgram();
		gl.glAttachShader(vtgfprogram, vShader);
		gl.glAttachShader(vtgfprogram, tcShader);
		gl.glAttachShader(vtgfprogram, teShader);
		gl.glAttachShader(vtgfprogram, gShader);
		gl.glAttachShader(vtgfprogram, fShader);
		finalizeProgram(vtgfprogram);
		return vtgfprogram;
	}

	public static int createShaderProgram(String vS, String tCS, String tES, String fS) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int vShader  = prepareShader(GL_VERTEX_SHADER, vS);
		int tcShader = prepareShader(GL_TESS_CONTROL_SHADER, tCS);
		int teShader = prepareShader(GL_TESS_EVALUATION_SHADER, tES);
		int fShader  = prepareShader(GL_FRAGMENT_SHADER, fS);
		int vtfprogram = gl.glCreateProgram();
		gl.glAttachShader(vtfprogram, vShader);
		gl.glAttachShader(vtfprogram, tcShader);
		gl.glAttachShader(vtfprogram, teShader);
		gl.glAttachShader(vtfprogram, fShader);
		finalizeProgram(vtfprogram);
		return vtfprogram;
	}

	public static int createShaderProgram(String vS, String gS, String fS) {
	GL4 gl = (GL4) GLContext.getCurrentGL();
		int vShader  = prepareShader(GL_VERTEX_SHADER, vS);
		int gShader = prepareShader(GL_GEOMETRY_SHADER, gS);
		int fShader  = prepareShader(GL_FRAGMENT_SHADER, fS);
		int vgfprogram = gl.glCreateProgram();
		gl.glAttachShader(vgfprogram, vShader);
		gl.glAttachShader(vgfprogram, gShader);
		gl.glAttachShader(vgfprogram, fShader);
		finalizeProgram(vgfprogram);
		return vgfprogram;
	}

	public static int createShaderProgram(String vS, String fS) {
	GL4 gl = (GL4) GLContext.getCurrentGL();
		int vShader  = prepareShader(GL_VERTEX_SHADER, vS);
		int fShader  = prepareShader(GL_FRAGMENT_SHADER, fS);
		int vfprogram = gl.glCreateProgram();
		gl.glAttachShader(vfprogram, vShader);
		gl.glAttachShader(vfprogram, fShader);
		finalizeProgram(vfprogram);
		return vfprogram;
	}

	public static int createShaderProgram(String cS) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int cShader  = prepareShader(GL_COMPUTE_SHADER, cS);
		int cprogram = gl.glCreateProgram();
		gl.glAttachShader(cprogram, cShader);
		finalizeProgram(cprogram);
		return cprogram;
	}

	public static int finalizeProgram(int sprogram) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] linked = new int[1];
		gl.glLinkProgram(sprogram);
		checkOpenGLError();
		gl.glGetProgramiv(sprogram, GL_LINK_STATUS, linked, 0);
		if (linked[0] != 1)
		{	System.out.println("linking failed");
			printProgramLog(sprogram);
		}
		return sprogram;
	}
	
	private static int prepareShader(int shaderTYPE, String shader) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] shaderCompiled = new int[1];
		String shaderSource[] = readShaderSource(shader);
		int shaderRef = gl.glCreateShader(shaderTYPE);
		gl.glShaderSource(shaderRef, shaderSource.length, shaderSource, null, 0);
		gl.glCompileShader(shaderRef);
		checkOpenGLError();
		gl.glGetShaderiv(shaderRef, GL_COMPILE_STATUS, shaderCompiled, 0);
		if (shaderCompiled[0] != 1)
		{	if (shaderTYPE == GL_VERTEX_SHADER) System.out.print("Vertex ");
			if (shaderTYPE == GL_TESS_CONTROL_SHADER) System.out.print("Tess Control ");
			if (shaderTYPE == GL_TESS_EVALUATION_SHADER) System.out.print("Tess Eval ");
			if (shaderTYPE == GL_GEOMETRY_SHADER) System.out.print("Geometry ");
			if (shaderTYPE == GL_FRAGMENT_SHADER) System.out.print("Fragment ");
			if (shaderTYPE == GL_COMPUTE_SHADER) System.out.print("Compute ");
			System.out.println("shader compilation error.");
			printShaderLog(shaderRef);
		}
		return shaderRef;
	}
	
	private static String[] readShaderSource(String filename) {
		ArrayList<String> lines = new ArrayList<String>();
		try {
			Scanner scanner = new Scanner(new File(filename));
			while (scanner.hasNext()) lines.add(scanner.nextLine() + "\n");
			scanner.close();

			return lines.toArray(new String[lines.size()]);
		}
		catch (IOException e) {
			System.err.println("IOException reading file: " + e);
			return null;
		}
	}

	private static void printShaderLog(int shader) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] len = new int[1];
		int[] chWrittn = new int[1];
		byte[] log = null;

		// determine the length of the shader compilation log
		gl.glGetShaderiv(shader, GL_INFO_LOG_LENGTH, len, 0);
		if (len[0] > 0) {
			log = new byte[len[0]];
			gl.glGetShaderInfoLog(shader, len[0], chWrittn, 0, log, 0);
			System.out.println("Shader Info Log: ");
			for (int i = 0; i < log.length; i++) System.out.print((char) log[i]);
		}
	}

	public static void printProgramLog(int prog) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] len = new int[1];
		int[] chWrittn = new int[1];
		byte[] log = null;

		// determine length of the program compilation log
		gl.glGetProgramiv(prog, GL_INFO_LOG_LENGTH, len, 0);
		if (len[0] > 0) {
			log = new byte[len[0]];
			gl.glGetProgramInfoLog(prog, len[0], chWrittn, 0, log, 0);
			System.out.println("Program Info Log: ");
			for (int i = 0; i < log.length; i++) System.out.print((char) log[i]);
		}
	}

	public static boolean checkOpenGLError() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		boolean foundError = false;
		GLU glu = new GLU();
		int glErr = gl.glGetError();
		while (glErr != GL_NO_ERROR) {
			System.err.println("glError: " + glu.gluErrorString(glErr));
			foundError = true;
			glErr = gl.glGetError();
		}
		return foundError;
	}

	public static void displayComputeShaderLimits() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] work_grp_cnt = new int[3];
		int[] work_grp_siz = new int[3];
		int[] work_grp_inv = new int[1];
		gl.glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_COUNT, 0, work_grp_cnt, 0);
		gl.glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_COUNT, 1, work_grp_cnt, 1);
		gl.glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_COUNT, 2, work_grp_cnt, 2);
		gl.glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_SIZE, 0, work_grp_siz, 0);
		gl.glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_SIZE, 1, work_grp_siz, 1);
		gl.glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_SIZE, 2, work_grp_siz, 2);
		gl.glGetIntegerv(GL_MAX_COMPUTE_WORK_GROUP_INVOCATIONS, work_grp_inv, 0);
		System.out.println("maximum number of workgroups is: \n" +
			work_grp_cnt[0] + " " + work_grp_cnt[1] + " " + work_grp_cnt[2]);
		System.out.println("maximum size of workgroups is: \n" +
			work_grp_siz[0] + " " + work_grp_siz[1] + " " + work_grp_siz[2]);
		System.out.println("max local work group invocations is " + work_grp_inv[0]);
	}
	
	public static int loadTexture(String textureFileName) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int finalTextureRef;
		Texture tex = null;
		try { tex = TextureIO.newTexture(new File(textureFileName), false); }
		catch (Exception e) { e.printStackTrace(); }
		finalTextureRef = tex.getTextureObject();

		// building a mipmap and use anisotropic filtering
		gl.glBindTexture(GL_TEXTURE_2D, finalTextureRef);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		gl.glGenerateMipmap(GL_TEXTURE_2D);
		if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic"))
		{	float anisoset[] = new float[1];
			gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, anisoset, 0);
			gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, anisoset[0]);
		}
		return finalTextureRef;
	}

	public static int loadTextureAWT(String textureFileName) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		BufferedImage textureImage = loadBufferedImage(textureFileName);
		byte[ ] imgRGBA = getRGBAPixelData(textureImage, true);
		ByteBuffer rgbaBuffer = Buffers.newDirectByteBuffer(imgRGBA);
		
		int[ ] textureIDs = new int[1];				// array to hold generated texture IDs
		gl.glGenTextures(1, textureIDs, 0);
		int textureID = textureIDs[0];				// ID for the 0th texture object
		gl.glBindTexture(GL_TEXTURE_2D, textureID);	// specifies the currently active 2D texture
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA,	// MIPMAP Level, number of color components
			textureImage.getWidth(), textureImage.getHeight(), 0,	// image size, border (ignored)
			GL_RGBA, GL_UNSIGNED_BYTE,				// pixel format and data type
			rgbaBuffer);						// buffer holding texture data
		
		// build a mipmap and use anisotropic filtering if available
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		gl.glGenerateMipmap(GL_TEXTURE_2D);
		
		if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic"))
		{	float anisoset[] = new float[1];
			gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, anisoset, 0);
			gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, anisoset[0]);
		}	
		return textureID;
	}

	public static int loadCubeMap(String dirName) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		String topFile = dirName + File.separator + "yp.jpg";
		String leftFile = dirName + File.separator + "xn.jpg";
		String backFile = dirName + File.separator + "zn.jpg";
		String rightFile = dirName + File.separator + "xp.jpg";
		String frontFile = dirName + File.separator + "zp.jpg";
		String bottomFile = dirName + File.separator + "yn.jpg";
		
		BufferedImage topImage = loadBufferedImage(topFile);
		BufferedImage leftImage = loadBufferedImage(leftFile);
		BufferedImage frontImage = loadBufferedImage(frontFile);
		BufferedImage rightImage = loadBufferedImage(rightFile);
		BufferedImage backImage = loadBufferedImage(backFile);
		BufferedImage bottomImage = loadBufferedImage(bottomFile);
		
		byte[] topRGBA = getRGBAPixelData(topImage, false);
		byte[] leftRGBA = getRGBAPixelData(leftImage, false);
		byte[] frontRGBA = getRGBAPixelData(frontImage, false);
		byte[] rightRGBA = getRGBAPixelData(rightImage, false);
		byte[] backRGBA = getRGBAPixelData(backImage, false);
		byte[] bottomRGBA = getRGBAPixelData(bottomImage, false);
		
		ByteBuffer topWrappedRGBA = ByteBuffer.wrap(topRGBA);
		ByteBuffer leftWrappedRGBA = ByteBuffer.wrap(leftRGBA);
		ByteBuffer frontWrappedRGBA = ByteBuffer.wrap(frontRGBA);
		ByteBuffer rightWrappedRGBA = ByteBuffer.wrap(rightRGBA);
		ByteBuffer backWrappedRGBA = ByteBuffer.wrap(backRGBA);
		ByteBuffer bottomWrappedRGBA = ByteBuffer.wrap(bottomRGBA);

		int[] textureIDs = new int[1];
		gl.glGenTextures(1, textureIDs, 0);
		int textureID = textureIDs[0];
		
		checkOpenGLError();

		gl.glBindTexture(GL_TEXTURE_CUBE_MAP, textureID);
		gl.glTexStorage2D(GL_TEXTURE_CUBE_MAP, 1, GL_RGBA8, 1024, 1024);
		
		// attach the image texture to each face of the currently active OpenGL texture ID
		gl.glTexSubImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, 0, 0, 1024, 1024,
				GL_RGBA, GL_UNSIGNED_BYTE, rightWrappedRGBA);		
		gl.glTexSubImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, 0, 0, 1024, 1024,
				GL_RGBA, GL_UNSIGNED_BYTE, leftWrappedRGBA);
		gl.glTexSubImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, 0, 0, 1024, 1024,
				GL_RGBA, GL_UNSIGNED_BYTE, bottomWrappedRGBA);
		gl.glTexSubImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, 0, 0, 1024, 1024,
				GL_RGBA, GL_UNSIGNED_BYTE, topWrappedRGBA);
		gl.glTexSubImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, 0, 0, 1024, 1024,
				GL_RGBA, GL_UNSIGNED_BYTE, frontWrappedRGBA);
		gl.glTexSubImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, 0, 0, 1024, 1024,
				GL_RGBA, GL_UNSIGNED_BYTE, backWrappedRGBA);

		gl.glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
		
		checkOpenGLError();
		return textureID;
	}

	private static BufferedImage loadBufferedImage(String fileName) {
		BufferedImage img;
		try {
			img = ImageIO.read(new File(fileName));	// assumes GIF, JPG, PNG, BMP
		}
		catch (IOException e) {
			System.err.println("Error reading '" + fileName + '"');
			throw new RuntimeException(e);
		}
		return img;
	}

	private static byte[] getRGBAPixelData(BufferedImage img, boolean flip) {
		int height = img.getHeight(null);
		int width = img.getWidth(null);

		// create an (empty) BufferedImage with a suitable Raster and ColorModel
		WritableRaster raster = Raster.createInterleavedRaster(
				DataBuffer.TYPE_BYTE, width, height, 4, null);

		// convert to a color model that OpenGL understands
		ComponentColorModel colorModel = new ComponentColorModel(
				ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] { 8, 8, 8, 8 }, // bits
				true,  // hasAlpha
				false, // isAlphaPreMultiplied
				ComponentColorModel.TRANSLUCENT,
				DataBuffer.TYPE_BYTE);

		BufferedImage newImage = new BufferedImage(colorModel, raster, false, null);
		Graphics2D g = newImage.createGraphics();

		if (flip) {	// flip image vertically
			AffineTransform gt = new AffineTransform();
			gt.translate(0, height);
			gt.scale(1, -1d);
			g.transform(gt);
		}
		g.drawImage(img, null, null); // draw original image into new image
		g.dispose();

		// now retrieve the underlying byte array from the raster data buffer
		DataBufferByte dataBuf = (DataBufferByte) raster.getDataBuffer();
		return dataBuf.getData();
	}

	// GOLD material - ambient, diffuse, specular, and shininess
	public static float[] goldAmbient()  { return (new float[] {0.2473f,  0.1995f, 0.0745f, 1} ); }
	public static float[] goldDiffuse()  { return (new float[] {0.7516f,  0.6065f, 0.2265f, 1} ); }
	public static float[] goldSpecular() { return (new float[] {0.6283f,  0.5559f, 0.3661f, 1} ); }
	public static float goldShininess()  { return 51.2f; }

	// SILVER material - ambient, diffuse, specular, and shininess
	public static float[] silverAmbient()  { return (new float[] {0.1923f,  0.1923f,  0.1923f, 1} ); }
	public static float[] silverDiffuse()  { return (new float[] {0.5075f,  0.5075f,  0.5075f, 1} ); }
	public static float[] silverSpecular() { return (new float[] {0.5083f,  0.5083f,  0.5083f, 1} ); }
	public static float silverShininess()  { return 51.2f; }


	// BRONZE material - ambient, diffuse, specular, and shininess
	public static float[] bronzeAmbient()  { return (new float[] {0.2125f,  0.1275f, 0.0540f, 1} ); }
	public static float[] bronzeDiffuse()  { return (new float[] {0.7140f,  0.4284f, 0.1814f, 1} ); }
	public static float[] bronzeSpecular() { return (new float[] {0.3936f,  0.2719f, 0.1667f, 1} ); }
	public static float bronzeShininess()  { return 25.6f; }
}
