/**
 * Program 2.1: just make a red screen.
 */

import javax.swing.*;
import com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;

public class RedWindow extends JFrame implements GLEventListener {
	// constants
	private static final int WINDOW_WIDTH = 600, WINDOW_HEIGHT = 400;
	private static final String WINDOW_TITLE = "Red Window";

	// window fields
	private GLCanvas glCanvas;

	/**
	 * main() just makes the object.
	 */

	public static void main(String[] args) {
		new RedWindow();
	}

	/**
	 * Constructor--set up the window.
	 */
	
	public RedWindow() {
		setTitle(WINDOW_TITLE);
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		setLocation(200, 200);
		glCanvas = new GLCanvas();
		glCanvas.addGLEventListener(this);
		this.add(glCanvas);
		this.setVisible(true);
	}

	/**
	 * Actually draw the picture.
	 */

	@Override
	public void display(GLAutoDrawable drawable) {
		GL4 gl = (GL4)GLContext.getCurrentGL();
		gl.glClearColor(1f, 0f, 0f, 1f);
		gl.glClear(GL4.GL_COLOR_BUFFER_BIT);
	}
	
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}

	@Override
	public void dispose(GLAutoDrawable drawable) {}

	@Override
	public void init(GLAutoDrawable drawable) {}
}
