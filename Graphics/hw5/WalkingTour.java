
/**
 * @author Jack Ruder
 * 
 * Igloo containing the mystical frozen pumpkin
 *
 * Controls, WASD/Arrow keys for movement
 * Q/E or Home/PgUp for yaw
 * R/F or PgDn/End for pitch
 * 
 * Hold Shift to 'sprint'
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

import sun.awt.AWTAccessor.KeyEventAccessor;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.awt.geom.AffineTransform;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;


/* specific to Utils */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.File;

public class WalkingTour extends JFrame implements GLEventListener, KeyListener{
	// constantns
	private static int WINDOW_WIDTH = 1920, WINDOW_HEIGHT = 1080;
	private static final String WINDOW_TITLE = "WalkingTour";
	private static final String VERTEX_SHADER_FILE = "walkingtour-vertex.glsl",
			FRAGMENT_SHADER_FILE = "walkingtour-fragment.glsl";
	private static final String[] texFiles = {"pumpkin.jpg","igloo.jpg","ice.jpg"};
	private static final String[] objFiles = {"pumpkin.obj","igloo.obj"};
	// window fields
	private GLCanvas glCanvas;
	private int renderingProgram;
	private double scale;



	private int[] vao = new int[1];
	private int[] pvbo = new int[3];
	private int[] tvbo = new int[3];
	private int nvIgloo;
	private int nvPenguin;
	private int nvPumpkin;


	// allocate variables for display() function
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4f pMat = new Matrix4f(); // perspective matrix
	private Matrix4f vMat = new Matrix4f(); // view matrix
	private int pLoc,vLoc,pumpLoc,oLoc,iglooLoc;
	private float aspect;
		
	private int[] texRefs;


	private Vector3f camera, forward, up;

	/* movement */
	private int dirF, dirS, dirY, dirP;
	private long before; // timestamp
	private float playerSpeed = 3.0f; // m/s
	private float turnSpeed = (float) Math.PI/3.0f; // rad/s
	

	/* object properties */
	private float pW = 1000.0f; //plane width/2
	private float tW = 100.0f; // Num tiles in row/column in plane
	private float pumpkinScale = 0.3f;
	private float iglooScale = 2.0f;
	
	/**
	 */
	public WalkingTour() {
		setTitle(WINDOW_TITLE);
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);

		glCanvas = new GLCanvas();
		glCanvas.addGLEventListener(this);
		glCanvas.addKeyListener(this);

		Animator animator = new Animator(glCanvas);
		animator.start();

		this.add(glCanvas);
		this.setVisible(true);

		this.texRefs = new int[texFiles.length];

		this.dirF = 0;
		this.dirS = 0;
		this.dirY = 0;
		this.dirP = 0;

	}


	/*
	 *  run program 
	 *  */
	public static void main(String[] args) {
		new WalkingTour(); 
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
		pMat.setPerspective((float) Math.toRadians(80.0f), aspect, 0.3f, 1000.0f);
		Vector3f target = new Vector3f();
		camera.sub(forward,target);
		vMat.setLookAt(camera,target,up); //look at center, eye level with fan center
		vLoc = gl.glGetUniformLocation(renderingProgram, "v_matrix");
		pLoc = gl.glGetUniformLocation(renderingProgram, "p_matrix");


		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));

		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));

		/* do the drawing */

		oLoc = gl.glGetUniformLocation(renderingProgram, "objDraw");
		// pumpkin
		gl.glUniform1i(oLoc, 0);
		drawTrianglesVBO(gl, pvbo[0], tvbo[0], nvPumpkin);

		// pumpkin
		gl.glUniform1i(oLoc, 1);
		drawTrianglesVBO(gl, pvbo[1], tvbo[1], nvIgloo);

		// plane
		gl.glUniform1i(oLoc, 2);
		drawTrianglesVBO(gl, pvbo[2], tvbo[2], 6);


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
		gl.glFrontFace(GL_CCW);

		gl.glClearColor(0.52f,0.81f,0.92f,1.0f);

		renderingProgram = Utils.createShaderProgram(VERTEX_SHADER_FILE, FRAGMENT_SHADER_FILE); // compile and
													// link
		gl.glUseProgram(renderingProgram);

		

		// try to load texture files
		//
		System.out.print("Loading Textures... ");
		for (int i = 0; i < texFiles.length; i++) {
			try {
			    this.texRefs[i] = loadTexture(texFiles[i]);
			} catch (IOException e) {
			    System.out.println("Could not find texture in file \"" + texFiles[i] + "\": " + e.getMessage());
			    System.exit(-1);
			}
		}
		System.out.println("done.");


		
		// bind textures
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, this.texRefs[0]);
		
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, this.texRefs[1]);

		gl.glActiveTexture(GL_TEXTURE2);
		gl.glBindTexture(GL_TEXTURE_2D, this.texRefs[2]);
	

		// load uniforms
		pumpLoc = gl.glGetUniformLocation(renderingProgram, "pumpkin_matrix");

		gl.glUniformMatrix4fv(pumpLoc, 1, false, genPumpkinMatrix().get(vals)); // 4 matrices here
		
		iglooLoc = gl.glGetUniformLocation(renderingProgram, "igloo_matrix");

		gl.glUniformMatrix4fv(iglooLoc, 1, false, genIglooMatrix().get(vals)); // 4 matrices here
											//
		System.out.print("Setting up vertices... ");
		setupVertices(); // load vbos
		System.out.println("done.");
		start(); // kick off 'engine'
	}
	
	/*
	 * Initialize/Reset camera and forward positions.
	 */
	private void start() {
		camera = new Vector3f(3.0f,2.0f,5.0f);
		forward = new Vector3f(1.0f,0.0f,0.0f);
		up = getUp(camera,forward);

	}

	/*
	 * Initialize vao and vbos containing object positions and colors.
	 * Load all objects here.
	 */
	private void setupVertices() {

		GL4 gl = (GL4) GLContext.getCurrentGL(); 
		
		System.out.print("Loading Objects... ");
		ImportedModel pumpkin = new ImportedModel(objFiles[0]);
		ImportedModel igloo = new ImportedModel(objFiles[1]);
		System.out.println("finished loading objects.");
		
		nvPumpkin = pumpkin.getNumVertices();
		nvIgloo = igloo.getNumVertices();

		// vao
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(pvbo.length, pvbo, 0);
		gl.glGenBuffers(tvbo.length, tvbo, 0);


		// load pumpkin
		initVBO(gl,pumpkin, pvbo[0], tvbo[0]);
		//load igloo
		initVBO(gl,igloo, pvbo[1], tvbo[1]);

		// plane pos
		float[] p =   {-pW,0,-pW,
				pW,0,pW, 
				pW,0,-pW,
				-pW,0,-pW,
				-pW,0,pW,
				pW,0,pW};
		// plane tex
		float[] t = {0,0,
			     tW,tW,
		             tW,0,
			     0,0,
			     0,tW,
			     tW,tW};
		//load plane
		initVBO(gl,p,t,pvbo[2],tvbo[2]);
	}
	
	// generate model matirx for a pumpkin
	private Matrix4f genPumpkinMatrix() {
		Matrix4f m = new Matrix4f();
		m.identity();
		m.scale(pumpkinScale);
		return m;
	}

	private Matrix4f genIglooMatrix() {
		Matrix4f m = new Matrix4f();
		m.identity();
		m.scale(iglooScale);
		return m;
	}
	
	/* update camera position, and  */
	private void update() {
		up = getUp(camera,forward);

		//update time
		long now = System.currentTimeMillis();
		long dt = now - before;
		before = now;


		Vector3f forwardsDirection = new Vector3f();
		Vector3f sidewaysDirection = new Vector3f();
		boolean nonZero = false;
		//compute each part
		if (dirF != 0) { // forwards
			forwardsDirection.x = forward.x;
			forwardsDirection.z = forward.z;
			forwardsDirection.mul(-1 * dirF); // change direction
			nonZero = true;
		}

		if (dirS != 0) { //sideways
			sidewaysDirection.x = -forward.z; //rotate 90 ccw
			sidewaysDirection.z = forward.x;
			sidewaysDirection.mul(-1 * dirS); // change direction
			nonZero = true;
		}

		
		if (nonZero) {	// if we have a movement vector, figure it out
			forwardsDirection.add(sidewaysDirection); //add together forwards and sideways directions
			forwardsDirection.normalize(); //normalize
			forwardsDirection.mul(dt/1000.0f * playerSpeed); //scale

			camera.add(forwardsDirection); //update
		}

		// handle yaw
		if (dirY != 0) {
			float theta = dt/1000.0f * turnSpeed * dirY;
			Matrix3f m = new Matrix3f();
			m.rotationY(theta);
			forward.mul(m);
		}

		if (dirP != 0) {
			float theta = dt/1000.0f * turnSpeed * dirP;
			Matrix3f m = new Matrix3f();

			Vector3f r = new Vector3f();
			forward.normalize(r); // normalize, store in t
			r.cross(0.0f,-1.0f, 0.0f); // 0,1,0 x t, right vector
						   
			m.rotation(theta,r); //rotate vertically
			forward.mul(m);
		}

	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL4 gl = drawable.getGL().getGL4(); 
		scale = ((Graphics2D) getGraphics()).getTransform().getScaleX();  //display scaling
		width = (int) (width * scale); //resize
		height = (int) (height * scale); //resize
		gl.glViewport(0,0,width,height); //resize
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
	}

	/* handle key presses, setting appropriate movement flags */
	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
			dirF = 1;
		}
		if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
			dirF = -1;

		}
		if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
			dirS = -1;

		}
		if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
			dirS = 1;
		}
		if (keyCode == KeyEvent.VK_HOME || keyCode == KeyEvent.VK_Q) {
			dirY = 1;
		}
		if (keyCode == KeyEvent.VK_PAGE_UP || keyCode == KeyEvent.VK_E) {
			dirY = -1;
		}
		if (keyCode == KeyEvent.VK_R || keyCode == KeyEvent.VK_PAGE_DOWN) {
			dirP = 1;
		}
		if (keyCode == KeyEvent.VK_F || keyCode == KeyEvent.VK_END) {
			dirP = -1;
		}
		if (keyCode == KeyEvent.VK_SHIFT) {
			playerSpeed *= 2f;		
		}

	}

	/* handle key presses, unsetting appropriate movement flags */
	@Override
	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
			if (dirF != -1) { // if not moving down
				dirF = 0;
			}
		}
		if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
			if (dirF != 1) { // if not moving up
				dirF = 0;
			}

		}
		if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
			if (dirS != 1) { // if not moving right
				dirS = 0;
			}

		}
		if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
			if (dirS != -1) { // if not moving left
				dirS = 0;
			}
		}

		if (keyCode == KeyEvent.VK_HOME || keyCode == KeyEvent.VK_Q) {
			if (dirY != -1) { // if not moving right
				dirY = 0;
			}
		}
		if (keyCode == KeyEvent.VK_PAGE_UP || keyCode == KeyEvent.VK_E) {
			if (dirY != 1) { // if not moving left
				dirY = 0;
			}
		}

		if (keyCode == KeyEvent.VK_R || keyCode == KeyEvent.VK_PAGE_DOWN) {
			if (dirP != -1) { // if not moving down
				dirP = 0;
			}
		}
		if (keyCode == KeyEvent.VK_F || keyCode == KeyEvent.VK_END) {
			if (dirP != 1) { // if not moving up
				dirP = 0;
			}
		}
		if (keyCode == KeyEvent.VK_SHIFT) {
			playerSpeed /= 2f;		
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}


	/* 
	 *
	 *
	 * UTILITY FUNCITONS 
	 *
	 *
	 * */


	// compute up vector
	private Vector3f getUp(Vector3f camera, Vector3f forward) {
		Vector3f t = new Vector3f();
		forward.normalize(t); // normalize, store in t
		t.cross(0.0f,-1.0f, 0.0f); // 0,1,0 x t 
		forward.cross(t,t);

		return t;
	}
	

	// draw n_vertices from pvbo and tvbo
	private void drawTrianglesVBO(GL4 gl, int pvbo, int tvbo, int n_vertices) {
		gl.glBindBuffer(GL_ARRAY_BUFFER, pvbo);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		// textures
		gl.glBindBuffer(GL_ARRAY_BUFFER, tvbo);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		// draw
		gl.glDrawArrays(GL_TRIANGLES, 0, n_vertices);
	}

	// load pos and tex coords into vbos
	private void initVBO(GL4 gl, float[] pos, float[] tex, int pvbo, int tvbo) {
		FloatBuffer fb;
		fb = Buffers.newDirectFloatBuffer(pos);
		gl.glBindBuffer(GL_ARRAY_BUFFER, pvbo);
		gl.glBufferData(GL_ARRAY_BUFFER, fb.limit() * 4, fb, GL_STATIC_DRAW);

		fb = Buffers.newDirectFloatBuffer(tex);
		gl.glBindBuffer(GL_ARRAY_BUFFER, tvbo);
		gl.glBufferData(GL_ARRAY_BUFFER, fb.limit() * 4, fb, GL_STATIC_DRAW);
	}
	
	// load model pos and tex coords into vbos
	private void initVBO(GL4 gl, ImportedModel m, int pvbo, int tvbo) {
		FloatBuffer fb;
		// positions
		fb = bufFromVecArray(m.getVertices());
		gl.glBindBuffer(GL_ARRAY_BUFFER, pvbo);
		gl.glBufferData(GL_ARRAY_BUFFER, fb.limit() * 4, fb, GL_STATIC_DRAW);
		//vertices
		fb = bufFromVecArray(m.getTexCoords());
		gl.glBindBuffer(GL_ARRAY_BUFFER, tvbo);
		gl.glBufferData(GL_ARRAY_BUFFER, fb.limit() * 4, fb, GL_STATIC_DRAW);
	}

		
	// flatten an array of vectors into a float buffer
	private FloatBuffer bufFromVecArray(Vector3f[] v) {
		FloatBuffer fb = Buffers.newDirectFloatBuffer(v.length * 3);
		for (int i = 0; i < v.length; i++) {
			fb = v[i].get(3 * i, fb);
		}
		return fb;
	}

	// flatten an array of vectors into a float buffer
	private FloatBuffer bufFromVecArray(Vector2f[] v) {
		FloatBuffer fb = Buffers.newDirectFloatBuffer(v.length * 2);
		for (int i = 0; i < v.length; i++) {
			fb = v[i].get(2 * i, fb);
		}
		return fb;
	}
	
	// slightly custom texture loader, throw exception if not found
	private static int loadTexture(String textureFileName) throws IOException, GLException{
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int finalTextureRef;
		Texture tex = null;
		tex = TextureIO.newTexture(new File(textureFileName), false);
		finalTextureRef = tex.getTextureObject();

		// building a mipmap and use anisotropic filtering
		gl.glBindTexture(GL_TEXTURE_2D, finalTextureRef);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);
		gl.glGenerateMipmap(GL_TEXTURE_2D);
		if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic"))
		{	float anisoset[] = new float[1];
			gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, anisoset, 0);
			gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, anisoset[0]);
		}
		return finalTextureRef;
	}
}




















/*
 * ImportedModel class
 */
class ImportedModel
{
	private Vector3f[] vertices;
	private Vector2f[] texCoords;
	private Vector3f[] normals;
	private int numVertices;

	public ImportedModel(String filename)
	{	ModelImporter modelImporter = new ModelImporter();
		try
		{	modelImporter.parseOBJ(filename);
			numVertices   = modelImporter.getNumVertices();
			float[] verts = modelImporter.getVertices();
			float[] tcs   = modelImporter.getTextureCoordinates();
			float[] norm  = modelImporter.getNormals();

			vertices = new Vector3f[numVertices];
			texCoords = new Vector2f[numVertices];
			normals = new Vector3f[numVertices];
			
			for(int i=0; i<vertices.length; i++)
			{	vertices[i] = new Vector3f();
				vertices[i].set(verts[i*3], verts[i*3+1], verts[i*3+2]);
				texCoords[i] = new Vector2f();
				texCoords[i].set(tcs[i*2], tcs[i*2+1]);
				normals[i] = new Vector3f();
				normals[i].set(norm[i*3], norm[i*3+1], norm[i*3+2]);
			}
		} catch (IOException e)
		{ e.printStackTrace();
	}	}

	public int getNumVertices() { return numVertices; }
	public Vector3f[] getVertices() { return vertices; }
	public Vector2f[] getTexCoords() { return texCoords; }	
	public Vector3f[] getNormals() { return normals; }	

	private class ModelImporter
	{	// values as read from OBJ file
		private ArrayList<Float> vertVals = new ArrayList<Float>();
		private ArrayList<Float> triangleVerts = new ArrayList<Float>(); 
		private ArrayList<Float> textureCoords = new ArrayList<Float>();

		// values stored for later use as vertex attributes
		private ArrayList<Float> stVals = new ArrayList<Float>();
		private ArrayList<Float> normals = new ArrayList<Float>();
		private ArrayList<Float> normVals = new ArrayList<Float>();

		public void parseOBJ(String filename) throws IOException
		{	InputStream input = new FileInputStream(new File(filename));
			BufferedReader br = new BufferedReader(new InputStreamReader(input));
			String line;
			while ((line = br.readLine()) != null)
			{	if(line.startsWith("v "))			// vertex position ("v" case)
				{	for(String s : (line.substring(2)).split(" "))
					{	vertVals.add(Float.valueOf(s));
				}	}
				else if(line.startsWith("vt"))			// texture coordinates ("vt" case)
				{	for(String s : (line.substring(3)).split(" "))
					{	stVals.add(Float.valueOf(s));
				}	}
				else if(line.startsWith("vn"))			// vertex normals ("vn" case)
				{	for(String s : (line.substring(3)).split(" "))
					{	normVals.add(Float.valueOf(s));
				}	}
				else if(line.startsWith("f"))			// triangle faces ("f" case)
				{	for(String s : (line.substring(2)).split(" "))
					{	String v = s.split("/")[0];
						String vt = s.split("/")[1];
						String vn = s.split("/")[2];
	
						int vertRef = (Integer.valueOf(v)-1)*3;
						int tcRef   = (Integer.valueOf(vt)-1)*2;
						int normRef = (Integer.valueOf(vn)-1)*3;
	
						triangleVerts.add(vertVals.get(vertRef));
						triangleVerts.add(vertVals.get((vertRef)+1));
						triangleVerts.add(vertVals.get((vertRef)+2));

						textureCoords.add(stVals.get(tcRef));
						textureCoords.add(stVals.get(tcRef+1));
	
						normals.add(normVals.get(normRef));
						normals.add(normVals.get(normRef+1));
						normals.add(normVals.get(normRef+2));
			}	}	}
			input.close();
		}

		public int getNumVertices() { return (triangleVerts.size()/3); }

		public float[] getVertices()
		{	float[] p = new float[triangleVerts.size()];
			for(int i = 0; i < triangleVerts.size(); i++)
			{	p[i] = triangleVerts.get(i);
			}
			return p;
		}

		public float[] getTextureCoordinates()
		{	float[] t = new float[(textureCoords.size())];
			for(int i = 0; i < textureCoords.size(); i++)
			{	t[i] = textureCoords.get(i);
			}
			return t;
		}
	
		public float[] getNormals()
		{	float[] n = new float[(normals.size())];
			for(int i = 0; i < normals.size(); i++)
			{	n[i] = normals.get(i);
			}
			return n;
		}	
	}
}

/**
 * Utils Class from Textbook, copied in to allow changes
 *
 * */
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
