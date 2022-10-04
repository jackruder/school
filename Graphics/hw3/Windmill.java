/**
 * @author Jack Ruder
 * 
 * Program to create a windmill.
 * Inspired by example code in "Computer Graphics Programming in OpenGL With Java"
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
public class Windmill extends JFrame implements GLEventListener {
	// constants
	private static int WINDOW_WIDTH = 800, WINDOW_HEIGHT = 600;
	private static final String WINDOW_TITLE = "Windmill";
	private static final String VERTEX_SHADER_FILE = "windmill-vertex.glsl",
		FRAGMENT_SHADER_FILE = "windmill-fragment.glsl";

	// window fields
	private GLCanvas glCanvas;
	private int renderingProgram;
	private int[] vao = new int[1];
	private int[] vbo = new int[4];
	private int[] ssbo = new int[1];
	
	private long startTime; // to compute rotation
	private int n_blades;
	private float bladePeriod;
	private float cameraPeriod;

	private final float cameraR = 8.0f; // distance from center
	private final float fanCenterX = 0.0f, fanCenterY=4.0f, fanCenterZ=-1.001f; //center fan (camera starts on back, we will put then put fan on back)
	private final float bodyLocX = 0.0f, bodyLocY = 2.5f, bodyLocZ = 0.0f; //put base at zero
	private final float fanScale = 3.5f;

	private float cameraX, cameraY, cameraZ;
	private float bladeTheta;
	private float cameraTheta;

	// allocate variables for display() function
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4f bladeTMat = new Matrix4f(); //temp blade matrix
	private Matrix4f pMat = new Matrix4f();  // perspective matrix
	private Matrix4f laMat = new Matrix4f();  // look-at matrix
	private Matrix4f bodyMMat = new Matrix4f();  // model matrix
	private int laLoc, pLoc, bodyMLoc, fLoc;
	private float aspect;

	/**
	 * Create a new Windmill and kick off animation.
	 */

	public Windmill(int n_blades, float bladePeriod, float cameraPeriod) {
		setTitle(WINDOW_TITLE);
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);

		glCanvas = new GLCanvas();
		glCanvas.addGLEventListener(this);

		Animator animator = new Animator(glCanvas);
		animator.start();

		this.add(glCanvas);
		this.setVisible(true);
		this.n_blades = n_blades;
		this.bladePeriod = bladePeriod;
		this.cameraPeriod = cameraPeriod;

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
		
		update(); //update object locations

		gl.glUseProgram(renderingProgram);
			
		laLoc = gl.glGetUniformLocation(renderingProgram, "la_matrix");
		pLoc = gl.glGetUniformLocation(renderingProgram, "p_matrix");
		bodyMLoc = gl.glGetUniformLocation(renderingProgram, "bodyM_matrix");
		fLoc = gl.glGetUniformLocation(renderingProgram, "drawBlades");

		//compute perspective matrix
		aspect = (float) glCanvas.getWidth() / (float) glCanvas.getHeight();
		pMat.setPerspective((float) Math.toRadians(70.0f), aspect, 0.1f, 1000.0f);
		//compute lookat matrix
		laMat.setLookAt(cameraX,cameraY,cameraZ,0.0f,fanCenterY,0.0f,0.0f,1.0f,0.0f); //look at center, eye level with fan center
											 
		//compute body model matrix
		bodyMMat.translation(bodyLocX, bodyLocY, bodyLocZ);

		//update blade model matrices
		gl.glBindBufferBase(GL_SHADER_STORAGE_BUFFER,0,ssbo[0]);
		FloatBuffer mBuf = genBladeModelMatrices();
		gl.glBufferSubData(GL_SHADER_STORAGE_BUFFER,0,mBuf.limit()*4, mBuf); 
		gl.glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);

		// load matrices onto graphics card
		gl.glUniformMatrix4fv(laLoc, 1, false, laMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(bodyMLoc, 1, false, bodyMMat.get(vals));
		
		/* do the drawing*/
		gl.glUniform1f(fLoc, 0.0f);
		//load windmill body position data
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		// load windmill body color data
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		//draw windmill body
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);

		gl.glUniform1f(fLoc, 1.0f);
		//load blades position data
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		// load blades color data
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		//draw blades
		gl.glDrawArraysInstanced(GL_TRIANGLE_FAN, 0, 5, n_blades);

	}

	/**
	 * Do the initial setup for drawing
	 */

	@Override
	public void init(GLAutoDrawable drawable) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		//enable z buffer 
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		renderingProgram = Utils.createShaderProgram(VERTEX_SHADER_FILE, FRAGMENT_SHADER_FILE); //compile and link
		setupVertices(); // load vbos
		initSSBO(); // initialize ssbo
		this.startTime = System.currentTimeMillis();
		cameraY = 4.0f;
	}
	
	/*
	 * Initialize vao and vbos containing object positions and colors
	 */
	private void setupVertices() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		float[] bodyPositions = { // rectangular body centered at origin, 2x6x2 base, pyramid is 1 unit tall
			-1.0f,  2.5f, -1.0f, -1.0f, -2.5f, -1.0f, 1.0f, -2.5f, -1.0f, //bk1
			1.0f, -2.5f, -1.0f, 1.0f,  2.5f, -1.0f, -1.0f,  2.5f, -1.0f, //bk2
			1.0f, -2.5f, -1.0f, 1.0f, -2.5f,  1.0f, 1.0f,  2.5f, -1.0f, //r1
			1.0f, -2.5f,  1.0f, 1.0f,  2.5f,  1.0f, 1.0f,  2.5f, -1.0f, //r2
			1.0f, -2.5f,  1.0f, -1.0f, -2.5f,  1.0f, 1.0f,  2.5f,  1.0f, //f1
			-1.0f, -2.5f,  1.0f, -1.0f,  2.5f,  1.0f, 1.0f,  2.5f,  1.0f,//f2
			-1.0f, -2.5f,  1.0f, -1.0f, -2.5f, -1.0f, -1.0f,  2.5f,  1.0f, //l1
			-1.0f, -2.5f, -1.0f, -1.0f,  2.5f, -1.0f, -1.0f,  2.5f,  1.0f,//l2
			-1.0f, 2.5f, 1.0f, 1.0f, 2.5f, 1.0f,  0.0f, 3.5f, 0.0f,     //p front
			1.0f, 2.5f, 1.0f, 1.0f, 2.5f, -1.0f, 0.0f, 3.5f, 0.0f,    //p right
			1.0f, 2.5f, -1.0f, -1.0f, 2.5f, -1.0f, 0.0f, 3.5f, 0.0f,  //p back
			-1.0f, 2.5f, -1.0f, -1.0f, 2.5f, 1.0f, 0.0f, 3.5f, 0.0f,  //p left
		};

		float[] bodyColors = {
			1.0f,  0.64f, 0f, 1.0f, 0.64f, 0f, 1.0f,  0.64f, 0f,// bk1: orange
			1.0f,  0.64f, 0f, 1.0f, 0.64f, 0f, 1.0f,  0.64f, 0f,// bk1: orange
			1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, //r1: white
			1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,1.0f, 1.0f, 1.0f, //r2: white
			1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,1.0f, 0.0f, 0.0f, //f1: red
			1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,1.0f, 0.0f, 0.0f, //f2: red
			1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,1.0f, 0.0f, 1.0f, //l1: yellow
			1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,1.0f, 0.0f, 1.0f, //l2: yellow
			0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,  1.0f,   //p front: green
			0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,  1.0f,  0.0f, //p right: blue
			0.62f, 0.12f, 0.94f, 0.62f, 0.12f, 0.94f, 0.62f, 0.12f, 0.94f,  //p back purple
			1.0f, 0.75f, 0.79f, 1.0f, 0.75f, 0.79f,1.0f, 0.75f, 0.79f,  //p left pink
		};
		float[] fanPositions = { //use a triangle fan
			0.0f, -0.5f, 0.0f, 
			0.1f, -0.45f, 0.0f, 
			0.06f, 0.5f, 0.0f, 
			-0.06f, 0.5f, 0.0f, 
			-0.1f, -0.45f, 0.0f
		};

		float[] fanColors = {
			0.42f, 0.27f, 0.0f,
			0.42f, 0.27f, 0.0f,
			0.42f, 0.27f, 0.0f,
			0.42f, 0.27f, 0.0f,
			0.42f, 0.27f, 0.0f,
		};
		
		//vao
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(vbo.length, vbo, 0);
		
		//body positions
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer bodyBuf = Buffers.newDirectFloatBuffer(bodyPositions);
		gl.glBufferData(GL_ARRAY_BUFFER, bodyBuf.limit()*4, bodyBuf, GL_STATIC_DRAW);
		
		//body colors
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer bodyColBuf = Buffers.newDirectFloatBuffer(bodyColors);
		gl.glBufferData(GL_ARRAY_BUFFER, bodyBuf.limit()*4, bodyColBuf, GL_STATIC_DRAW);
		
		// blade pos
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		FloatBuffer fanBuf = Buffers.newDirectFloatBuffer(fanPositions);
		gl.glBufferData(GL_ARRAY_BUFFER, fanBuf.limit()*4, fanBuf, GL_STATIC_DRAW);
		
		// blade colors
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		FloatBuffer fanColBuf = Buffers.newDirectFloatBuffer(fanColors);
		gl.glBufferData(GL_ARRAY_BUFFER, fanBuf.limit()*4, fanColBuf, GL_STATIC_DRAW);
		
	}
	
	/* Compute and load Blade model matrices into
	 * a direct buffer 
	 *
	 * @return Float buffer of n_blades * 4 * 4 floats */
	private FloatBuffer genBladeModelMatrices() {
		float[] bladeModelMatrices = new float[n_blades * 16];
		Matrix4f m = new Matrix4f();
		for (int i = 0; i < n_blades; i++) {
			float offsetT = (float)(n_blades-i) / n_blades * (2 * (float)Math.PI);
			
			m.identity();
			m.translate(fanCenterX, fanCenterY, fanCenterZ); //put at center
 			m.rotateZ(offsetT + bladeTheta); // spin them
			m.translate(0.0f, fanScale/2, 0.0f); //center at tip
			m.rotateX((float)Math.PI); //prop up
			m.scale(fanScale);
			bladeModelMatrices = m.get(bladeModelMatrices, i * 16);
		}

		return Buffers.newDirectFloatBuffer(bladeModelMatrices); //return direct buffer
	
	}
	
	/* init SSBO to hold blade rotation matrices */
	private void initSSBO() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glGenBuffers(ssbo.length,ssbo,0);
		gl.glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo[0]);
		FloatBuffer mBuf = genBladeModelMatrices();
		gl.glBufferData(GL_SHADER_STORAGE_BUFFER, mBuf.limit()*4, mBuf, GL_DYNAMIC_DRAW);
	}
		
	/* update camera and object positions */
	private void update() {
		long elapsed = System.currentTimeMillis() - startTime;
		if (cameraPeriod==0) {
			cameraTheta = -(float)Math.PI/2;
		} else {
			cameraTheta = (elapsed / 1000.0f)/cameraPeriod * 2 * (float)Math.PI - (float)Math.PI/2;
		}
		if (bladePeriod == 0) {
			bladeTheta = 0;
		} else {
			bladeTheta = (elapsed / 1000.0f)/bladePeriod * 2 * (float)Math.PI - (float)Math.PI/2;
		}
		
		cameraX = cameraR * (float)Math.cos(cameraTheta);
		cameraZ = cameraR * (float)Math.sin(cameraTheta);
	}

	/**
	 * main() just makes the object.
	 */

	public static void main(String[] args) {
		int n_blades = Integer.parseInt(args[0]);
		float bladePeriod = Float.parseFloat(args[1]);
		float cameraPeriod = Float.parseFloat(args[2]);
		new Windmill(n_blades,bladePeriod,cameraPeriod);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {}
}

