// External imports
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.awt.event.WindowAdapter;
import java.util.Random;

import javax.imageio.ImageIO;
// Sound and Swing imports
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JOptionPane;

// OpenGL imports
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities; // Graphics Library Capabilities
import com.jogamp.opengl.awt.GLCanvas; // Abstract Window Toolkit for Canvas
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.GLProfile;
/** https://jogamp.org/deployment/jogamp-next/javadoc/jogl/javadoc/com/jogamp/opengl/util/FPSAnimator.html */
import com.jogamp.opengl.util.FPSAnimator; 

/**
 * Example application that demonstrates how to put together a single-threaded
 * rendering system.
 * 
 * @author Justin Couch
 * @version $Revision: 1.5 $
 * @param <IGLObject>
 */

@SuppressWarnings({ "serial", "unused" }) // Warnings are suppressed using 'SupressWarnings'.
public class ThreeDStarter <IGLObject> extends Frame implements GLEventListener, KeyListener
{
	// The Space Ship moves only in 2D space
	int Z = 0; // z-axis is set to 0
	int Ztranslate = 0; // Translation is set to 0
	int angle = 0; // Angle is set to 0
	int inc = 1;
	int zoff = 0;
	
	// For Missile Fired
	boolean fired = false;
	int fireDistance = 2; // Distance traveled by the 'Missile' from the Space ship 
	boolean showTarget = false;
	boolean iscollided = false;
	String scoreString = "Points";
	int targetsshown =0;
	int bulletsfired =0;
	int bulletscollide = 0;
	int timerseconds = 30; // Length of the game play determined by the timer in 'Seconds' (i.e) 1 min 30 sec
	boolean timerfinished = false;
	
	// Parameters for Target
	public void manageTarget() 
	{
		Runnable run = new Runnable() // constructor is used with new object 'Runnable()'
		{
			public void run() 
			{
				// Try-Catch block is used.
				try 
				{
					while (true) 
					{
						showTarget = true;
						Thread.sleep(6000);
						showTarget = false;
						Thread.sleep(3000);
					}

				} 
				catch (InterruptedException e) // Checks the flag for us and throws 'Interrupted Exception'.
				{
					System.out.println(" interrupted"); // If interrupted displays this message. 
				}
			}
		};
		new Thread(run).start(); // Again a new thread is started for the next target. Hence 'start()'.
	}
	
	// Parameters for the timer (i.e.) Duration of the game play
	public void manageTimer() 
	{
		Runnable run = new Runnable() // constructor is used with new object 'Runnable()'
		{
			public void run() 
			{
				// Try-Catch block is used.
				try 
				{
					while (true) 
					{
						Thread.sleep(1000);
						timerseconds--; // timer seconds gets decremented 
						if(timerseconds==0) // if timer becomes 0 then game is finished
						timerfinished = true;	
					}

				} 
				catch (InterruptedException e) 
				{
					System.out.println(" interrupted"); // If interrupted displays this message. 
				}
			}
		};
		new Thread(run).start();
	}
	
	// For 3D starter
	public ThreeDStarter() 
	{
		super("Space Craft");
		manageTarget();
		manageTimer();
		setLayout(new BorderLayout());
		setSize(1000, 500);
		setLocation(40, 40);
		this.addWindowListener(new WindowAdapter() 
		{
			public void windowClosing(WindowEvent e) 
			{
				System.exit(0);
			}
		});

		// Need to set visible first before starting the rendering thread due
		// to a bug in JOGL. See JOGL Issue #54 for more information on this.
		/** http://jogl.dev.java.net
		 * 
		 */
		setVisible(true); // to display the window
		setupJOGL();
	}

	// Methods defined by GLEventListener
	
	/**
	 * Called by the drawable immediately after the OpenGL context is
	 * initialized; the GLContext has already been made current when this method
	 * is called.
	 * @param drawable
	 *            
	 */
	@SuppressWarnings({ "static-access" })
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		
		//gl.glEnable(gl.GL_LIGHT0);    // Enable light source 0
		//gl.glEnable(gl.GL_LIGHT1);    // Enable light source 1	
		
		
		gl.glClearColor(0, 0, 0, 0);
		gl.glMatrixMode(gl.GL_PROJECTION);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glClearDepth(1.0f); // Set background depth to farthest
		gl.glEnable(gl.GL_DEPTH_TEST); // Enable depth testing for z-culling
		gl.glDepthFunc(gl.GL_LEQUAL); // Set the type of depth-test
		gl.glShadeModel(gl.GL_SMOOTH); // Enable smooth shading
		gl.glLoadIdentity();
		//gl.glOrtho(0, 1, 0, 1, -1, 1);
	}

	/**
	 * Called by the drawable when the surface resizes itself. Used to reset the
	 * viewport dimensions.
	 * 
	 * @param drawable
	 *            The display context to render to
	 */
	
	@SuppressWarnings("static-access")
	public void reshape(GLAutoDrawable drawable,int x,int y,int width,int height) 
	{
		GL2 gl = drawable.getGL().getGL2();
		GLU glu = GLU.createGLU(gl);
		// Computation of aspect ratio of the 'new window'.
		if (height == 0)
			height = 1; // To prevent divide by 0 'Arithmetic Exception'
		/**https://docs.oracle.com/javase/7/docs/api/java/lang/ArithmeticException.html
		 * 
		 */
		float aspect = (float) width / (float) height; // Float is used as data type
		// Set the viewport to cover the new window
		// Set the aspect ratio of the clipping volume to match the viewport
		gl.glMatrixMode(gl.GL_PROJECTION); // To operate on 'Projection'
		// For Matrix properties 									
		gl.glLoadIdentity();
		gl.glViewport(0, 0, width, height);
		
		/**http://www.programcreek.com/java-api-examples/index.php?api=android.opengl.GLU
		 * https://www.khronos.org/opengles/sdk/docs/man/xhtml/glViewport.xml
		 */
		glu.gluPerspective(45, aspect, 20f, 160);
		gl.glPushMatrix();
		glu.gluLookAt(10, -5, 40, 0, 0, 0, 0, 1, 0);
		gl.glMatrixMode(gl.GL_MODELVIEW);
		/** http://www.songho.ca/opengl/gl_transform.html
		 * 
		 */
		gl.glLoadIdentity();
	}

	/**
	 * Called by the drawable when the display mode or the display device
	 * associated with the GLDrawable has changed
	 */
	//public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) 
	//{
	//}

	/**
	 * Called by the drawable to perform rendering by the client.
	 * @param drawable
	 * The display context to render to
	 */
	/** http://jogamp.org/deployment/v2.1.5/javadoc/jogl/javadoc/javax/media/opengl/GLAutoDrawable.html
	 * 
	 */
	
	
	public void display(GLAutoDrawable drawable) 
	{
		update(drawable); //updates the imageView
		render(drawable); //renders the imageView
	}

	// Argument to allow updating the perspective
	@SuppressWarnings("static-access")
	public void update(GLAutoDrawable drawable) 
	{

		angle = (angle + inc) % 360;
		GL2 gl = drawable.getGL().getGL2();

		GLU glu = GLU.createGLU(gl);
		gl.glMatrixMode(gl.GL_PROJECTION);
		// Push and Pop for Matrix
		gl.glPopMatrix();
		gl.glPushMatrix();
		// Move the eye in relation to the scene of the object
		glu.gluLookAt(0, 0, 50, 0, 0, 0, 0, 1, 0);
		render(drawable);
	}

	public boolean checkCollision(float au, float av, float bu, float bv) // data type is 'float'
	{

		float temp = au;
		au = au - ballsize;
		av = temp + ballsize;
		System.out.println(au);
		System.out.println(av);
		System.out.println(bu);
		if (bu >= au && bu <= av)
			return true;
		else
			return false;
	}

	float X_CENTRE = -100, Y_CENTRE = -50;
	public GLUquadric quadratic1;
	boolean firstloop = true;
	boolean getnewcoordinate = true;
	int xLow = -165;
	int xHigh = 165;
	int yLow = 60;
	int yHigh = 160;
	int result;
	int yresult;
	float ballsize = 10f;
	boolean resultUpdated = false;
	//credits:- http://answers.unity3d.com/questions/492829/not-enough-rotation.html
	float rotateminX = -80;
	float rotateminY = 0;
	float rotatemaxX = 20;
	float rotatemaxY = 180;
	float rotateCurrentX = 0;
	float rotateCurrentY = 0;

	@SuppressWarnings({ "static-access" })
	public void render(GLAutoDrawable drawable) 
	{	
		GL2 gl = drawable.getGL().getGL2();
		
		gl.glRotatef(rotateCurrentY, 0, -1, 0);
		gl.glRotatef(rotateCurrentX, -1, 0, 0);

		gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT); 
		/** https://en.wikipedia.org/wiki/Java_Bindings_for_OpenGL
		 * https://www.opengl.org/sdk/docs/man3/xhtml/glClear.xml
		 */
		 /** https://books.google.com/books?id=_NYAcaf__SsC&pg=PA225&lpg=PA225&dq#v=onepage&q&f=false
		 * @author Vladimir Silva
		 * In his book Practical Eclipse Rich Client Platform Projects
		 */
		// gl.glClear - clears color and depth
		scoreString= "Targets:\n"+targetsshown;
        scoreString+= "Missiles Launched: "+bulletsfired;	
        scoreString+= "Hits:"+bulletscollide;		
        scoreString+= "Timer:"+timerseconds;		
        
        /*gl.glBegin();
        gl.glColor3f(0.0f, 1.0f, 0.0f); // Green
		gl.glVertex3f(1.0f, 1.0f, -1.0f);
		gl.glVertex3f(-1.0f, 1.0f, -1.0f);
		gl.glVertex3f(-1.0f, 1.0f, 1.0f);
		gl.glVertex3f(1.0f, 1.0f, 1.0f);

		// Bottom face (y = -1.0f)
		gl.glColor3f(1.0f, 0.5f, 0.0f); // Orange
		gl.glVertex3f(1.0f, -1.0f, 1.0f);
		gl.glVertex3f(-1.0f, -1.0f, 1.0f);
		gl.glVertex3f(-1.0f, -1.0f, -1.0f);
		gl.glVertex3f(1.0f, -1.0f, -1.0f);

		// Front face (z = 1.0f)
		gl.glColor3f(1.0f, 0.0f, 0.0f); // Red
		gl.glVertex3f(1.0f, 1.0f, 1.0f);
		gl.glVertex3f(-1.0f, 1.0f, 1.0f);
		gl.glVertex3f(-1.0f, -1.0f, 1.0f);
		gl.glVertex3f(1.0f, -1.0f, 1.0f);

		// Back face (z = -1.0f)
		gl.glColor3f(1.0f, 1.0f, 0.0f); // Yellow
		gl.glVertex3f(1.0f, -1.0f, -1.0f);
		gl.glVertex3f(-1.0f, -1.0f, -1.0f);
		gl.glVertex3f(-1.0f, 1.0f, -1.0f);
		gl.glVertex3f(1.0f, 1.0f, -1.0f);

		// Left face (x = -1.0f)
		gl.glColor3f(0.0f, 0.0f, 1.0f); // Blue
		gl.glVertex3f(-1.0f, 1.0f, 1.0f);
		gl.glVertex3f(-1.0f, 1.0f, -1.0f);
		gl.glVertex3f(-1.0f, -1.0f, -1.0f);
		gl.glVertex3f(-1.0f, -1.0f, 1.0f);

		// Right face (x = 1.0f)
		gl.glColor3f(1.0f, 0.0f, 1.0f); // Magenta
		gl.glVertex3f(1.0f, 1.0f, -1.0f);
		gl.glVertex3f(1.0f, 1.0f, 1.0f);
		gl.glVertex3f(1.0f, -1.0f, 1.0f);
		gl.glVertex3f(1.0f, -1.0f, -1.0f);
		gl.glEnd();
		gl.glPopMatrix();*/
        
        
        
        
        
		if(timerfinished)
		{
			JOptionPane.showMessageDialog(null, "Game Over \n"+scoreString);
			System.exit(1);
		return;
		}
		float LENGTH = 5;

		
		
		
		
			
		
		
		// //////////////////////
		// //////////////////////

		gl.glPushMatrix();
		gl.glScalef(2f, 1f, 3f);
		gl.glBegin(gl.GL_QUADS);
		// for the plane surface
		gl.glEnable(gl.GL_SHININESS);  // Enable lighting
		gl.glColor3f(0.1f, 0.8f, 0.5f);
		gl.glVertex3f(-20.0f, 0.0f, -20.0f);
		gl.glVertex3f(-20.0f, -10.0f, 0.0f);
		gl.glVertex3f(20.0f, -10.0f, 0.0f);
		gl.glVertex3f(20.0f, 0.0f, -20.0f);
		gl.glEnd();
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		gl.glScalef(.1f, .1f, .1f);
		gl.glTranslated(0, 0, (Ztranslate += zoff));

		zoff = 0;
		gl.glBegin(gl.GL_TRIANGLE_FAN);
		//vertices to draw Space Ship in 2D space
		gl.glColor3f(1.2f, 0.2f, 1.3f);
		gl.glVertex2f(X_CENTRE + LENGTH * 0, Y_CENTRE + LENGTH * 12);
		gl.glColor3f(1.6f, 1.1f, 0.5f);
		gl.glVertex2f(X_CENTRE - LENGTH * 8, Y_CENTRE - LENGTH * 10);
		gl.glColor3f(0.7f, 0.6f, 0.8f);
		gl.glVertex2f(X_CENTRE - LENGTH * 0, Y_CENTRE - LENGTH * 0);
		gl.glColor3f(1.0f, 1.0f, 0.9f);
		gl.glVertex2f(X_CENTRE + LENGTH * 8, Y_CENTRE - LENGTH * 10);
		gl.glEnd();

		if (fireDistance > 72)
			fired = false;
				if (fired) 
					{

					//http://stackoverflow.com/questions/31799670/applying-map-of-the-earth-texture-a-sphere
					//https://github.com/Moolt/Computergrafik/blob/master/src/Main/SkySphere.java, contributor: Moolt
					GLU glu = GLU.createGLU(gl);
						GLUquadric quadratic = glu.gluNewQuadric();

						if (checkCollision(result, yresult, X_CENTRE + LENGTH * 0, X_CENTRE + LENGTH * 12 + (fireDistance += 3))) {
							iscollided = true;
				
							if (!resultUpdated) 
							{
								resultUpdated = true;
								bulletscollide++;

							}
			}

			gl.glTranslatef(X_CENTRE + LENGTH * 0, X_CENTRE + LENGTH * 12 + (fireDistance += 2), 0);
			// flips the cylinder to point up along the y-axis instead of the z-axis
			gl.glRotatef(90.0f, 1f, 0.0f, 0f);
			glu.gluCylinder(quadratic, 1f, 5f, 10f, 32, 32);
			glu.gluDeleteQuadric(quadratic);
		}

		if (showTarget && !iscollided) {

			GLU glu = GLU.createGLU(gl);

			if (firstloop) 
			{
				quadratic1 = glu.gluNewQuadric();
				firstloop = false;
			}

			if (getnewcoordinate) 
			{
				targetsshown++;

				getnewcoordinate = false;
				Random r = new Random();
				result = r.nextInt(xHigh - xLow) + xLow;
				yresult = r.nextInt(yHigh - yLow) + yLow;
			}

			if (!fired) 
			{
				gl.glTranslatef(result, yresult, 0);
				gl.glColor3f(0.0f, 1.0f, 1.0f); 
				glu.gluCylinder(quadratic1, 1f, ballsize, ballsize, 56, 60);
			
			}
		}

		else
		{
			iscollided = false;

			getnewcoordinate = true;
			GLU glu = GLU.createGLU(gl);
			glu.gluDeleteQuadric(quadratic1);

		}

		{

			gl.glPopMatrix();
			gl.glPushMatrix();
			gl.glTranslatef(-25, 15, 0);

			float textPosx = -0.8f;
			float textPosy = -2.1f;

			gl.glColor3f(0.0f, 0.05f, 1.0f); //color of the score
			textPosx = 0f;
			textPosy = 4f;
			// Move to rastering position
			/**https://www.opengl.org/sdk/docs/man2/xhtml/glRasterPos.xml
			 * 
			 */
			gl.glRasterPos3d(-10, 2, 2);
			gl.glRasterPos2f(0,2);
			// convert text to bitmap and tell what string to put
			GLUT glut = new GLUT();
			glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, scoreString);
			
			gl.glEnd();


			

			gl.glBegin(gl.GL_QUADS); // Begin drawing the color cube with 6
			gl.glTranslatef(0f, 0f, 0f);
						// quads
			// Vertices are defined in counter-clockwise (CCW) order with normal pointing 'out'
			//gl.glColor3f(0.0f, 1.0f, 0.0f); // Green
			//gl.glVertex3f(1.0f, 1.0f, -1.0f);
			//gl.glVertex3f(-1.0f, 1.0f, -1.0f);
			//gl.glVertex3f(-1.0f, 1.0f, 1.0f);
			//gl.glVertex3f(1.0f, 1.0f, 1.0f);

			// Bottom face (y = -1.0f)
			//gl.glColor3f(1.0f, 0.5f, 0.0f); // Orange
			//gl.glVertex3f(1.0f, -1.0f, 1.0f);
			//gl.glVertex3f(-1.0f, -1.0f, 1.0f);
			//gl.glVertex3f(-1.0f, -1.0f, -1.0f);
			//gl.glVertex3f(1.0f, -1.0f, -1.0f);

			// Front face (z = 1.0f)
			//gl.glColor3f(1.0f, 0.0f, 0.0f); // Red
			//gl.glVertex3f(1.0f, 1.0f, 1.0f);
			//gl.glVertex3f(-1.0f, 1.0f, 1.0f);
			//gl.glVertex3f(-1.0f, -1.0f, 1.0f);
			//gl.glVertex3f(1.0f, -1.0f, 1.0f);

			// Back face (z = -1.0f)
			//gl.glColor3f(1.0f, 1.0f, 0.0f); // Yellow
			//gl.glVertex3f(1.0f, -1.0f, -1.0f);
			//gl.glVertex3f(-1.0f, -1.0f, -1.0f);
			//gl.glVertex3f(-1.0f, 1.0f, -1.0f);
			//gl.glVertex3f(1.0f, 1.0f, -1.0f);

			// Left face (x = -1.0f)
			//gl.glColor3f(0.0f, 0.0f, 1.0f); // Blue
			//gl.glVertex3f(-1.0f, 1.0f, 1.0f);
			//gl.glVertex3f(-1.0f, 1.0f, -1.0f);
			//gl.glVertex3f(-1.0f, -1.0f, -1.0f);
			//gl.glVertex3f(-1.0f, -1.0f, 1.0f);

			// Right face (x = 1.0f)
			//gl.glColor3f(1.0f, 0.0f, 1.0f); // Magenta
			//gl.glVertex3f(1.0f, 1.0f, -1.0f);
			//gl.glVertex3f(1.0f, 1.0f, 1.0f);
			//gl.glVertex3f(1.0f, -1.0f, 1.0f);
			//gl.glVertex3f(1.0f, -1.0f, -1.0f);
			//gl.glEnd();
			//gl.glPopMatrix();

			// Render a pyramid consists of 4 triangles
			gl.glPushMatrix();
			gl.glRotatef(angle, 1, -1, 1);
			gl.glBegin(gl.GL_TRIANGLES); // Begin drawing the pyramid with four triangles
											

		/*	// Front
			gl.glColor3f(1.0f, 0.0f, 0.0f); // Red
			gl.glVertex3f(0.0f, 1.0f, 0.0f);
			gl.glColor3f(0.0f, 1.0f, 0.0f); // Green
			gl.glVertex3f(-1.0f, -1.0f, 1.0f);
			gl.glColor3f(0.0f, 0.0f, 1.0f); // Blue
			gl.glVertex3f(1.0f, -1.0f, 1.0f);

			// Right
			gl.glColor3f(1.0f, 0.0f, 0.0f); // Red
			gl.glVertex3f(0.0f, 1.0f, 0.0f);
			gl.glColor3f(0.0f, 0.0f, 1.0f); // Blue
			gl.glVertex3f(1.0f, -1.0f, 1.0f);
			gl.glColor3f(0.0f, 1.0f, 0.0f); // Green
			gl.glVertex3f(1.0f, -1.0f, -1.0f);

			// Back
			gl.glColor3f(1.0f, 0.0f, 0.0f); // Red
			gl.glVertex3f(0.0f, 1.0f, 0.0f);
			gl.glColor3f(0.0f, 1.0f, 0.0f); // Green
			gl.glVertex3f(1.0f, -1.0f, -1.0f);
			gl.glColor3f(0.0f, 0.0f, 1.0f); // Blue
			gl.glVertex3f(-1.0f, -1.0f, -1.0f);

			// Left
			gl.glColor3f(1.0f, 0.0f, 0.0f); // Red
			gl.glVertex3f(0.0f, 1.0f, 0.0f);
			gl.glColor3f(0.0f, 0.0f, 1.0f); // Blue
			gl.glVertex3f(-1.0f, -1.0f, -1.0f);
			gl.glColor3f(0.0f, 1.0f, 0.0f); // Green
			gl.glVertex3f(-1.0f, -1.0f, 1.0f);*/
			gl.glEnd(); // Done drawing the pyramid
			gl.glPopMatrix();

		}

	}

	// ---------------------------------------------------------------
	// Local methods
	// ---------------------------------------------------------------

	/**
	 * Create the basics of the JOGL screen details.
	 * http://forum.jogamp.org/question-about-the-GLCanvas-and-GLJPanel-td3844025.html
	 */
	private void setupJOGL() 
	{
		GLProfile glp = GLProfile.getDefault();
		GLCapabilities caps = new GLCapabilities(glp);
		caps.setDoubleBuffered(true);
		caps.setHardwareAccelerated(true);

		GLCanvas canvas = new GLCanvas(caps);
		canvas.addGLEventListener(this);
		canvas.addKeyListener(this);

		add(canvas, BorderLayout.CENTER);
		FPSAnimator animator = new FPSAnimator(canvas, 40);
		animator.start();

	}

	public static void main(String[] args) 
	{
		@SuppressWarnings("rawtypes")
		ThreeDStarter demo = new ThreeDStarter();
		demo.setVisible(true);
	}

	

	public void isBorder(double x, double y, double z) {
		if (x == -40.0 && 6.0 * y + z == -60.0) {
			System.out.println("Border");

		}

	}

	public void keyPressed(KeyEvent e) 
	{
		int key = e.getKeyCode(); // Tells which key was pressed.
		System.out.println();
		if (key == KeyEvent.VK_HOME)
			System.exit(0);
		else if (key == KeyEvent.VK_SPACE && !fired) 
			{
				resultUpdated = false;
				playSound("blaster.wav");
				fired = true;
				bulletsfired++;
				fireDistance = 2;
			}
			else if (key == KeyEvent.VK_LEFT) 
				{
					System.out.println(X_CENTRE - 40);
					System.out.println(Y_CENTRE - 40);
					X_CENTRE -= 1;
				}
				else if (key == KeyEvent.VK_RIGHT) 
					{
						X_CENTRE += 1;
					} 
					else if (key == KeyEvent.VK_A) 
						{
							System.out.println(rotateCurrentY);
							if(rotateCurrentY<rotatemaxY)
							{
								rotateCurrentY++;
							}
						}
						else if (key == KeyEvent.VK_D) 
							{
								if(rotateCurrentY>rotateminY)
									rotateCurrentY--;
							}
						else if (key == KeyEvent.VK_W) 
						{
							if(rotateCurrentX<rotatemaxX)
								rotateCurrentX++;
						}
						else if (key == KeyEvent.VK_X) 
						{
							if(rotateCurrentX>rotateminX)
								rotateCurrentX--;
						}
					System.out.println(rotateCurrentX);
	}

	/**
	 * Is called when the USER types a 'character'.
	 */
	
	public void keyReleased(KeyEvent e) 
	{
		char ch = e.getKeyChar(); // To get the character typed.
	}
	
	public void keyTyped(KeyEvent e) 
	{
		char ch = e.getKeyChar(); // To get the character typed.
	}

	
	// For Sound (from 'resources')
	public static synchronized void playSound(final String url) 
	{
		new Thread(new Runnable() 
		{
			public void run() 
			{
				try 
				{
					Clip clip = AudioSystem.getClip();
					AudioInputStream inputStream = AudioSystem.getAudioInputStream(getClass().getResourceAsStream("resources/" + url));
					clip.open(inputStream);
					clip.start();
				} 
				catch (Exception e) 
				{
					System.err.println(e.getMessage());
				}
			}
		}).start();
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		
	}

}

