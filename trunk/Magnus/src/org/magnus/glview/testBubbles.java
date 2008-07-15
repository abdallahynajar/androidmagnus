package org.magnus.glview;

import javax.microedition.khronos.opengles.GL10;

import org.magnus.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.OpenGLContext;
import android.graphics.PixelFormat;
import android.graphics.glutils.GLView;
import android.opengl.GLU;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;

public class testBubbles extends Activity {	
	private GLSurfaceView mGLSurfaceView;
	
	@Override
	protected void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);

		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		mGLSurfaceView = new GLSurfaceView(this);
		setContentView(mGLSurfaceView);
	}

	@Override
	protected boolean isFullscreenOpaque() {
		return true;
	}

	@Override
	protected void onResume() {
		// Ideally a game should implement onResume() and onPause()
		// to take appropriate action when the activity looses focus
		super.onResume();
	}

	@Override
	protected void onPause() {
		// Ideally a game should implement onResume() and onPause()
		// to take appropriate action when the activity looses focus
		super.onPause();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (mGLSurfaceView.glView.processKey(keyCode)) {
		     return true;
		   }
		return super.onKeyDown(keyCode, event);
	}
}

// ----------------------------------------------------------------------

class GLSurfaceView extends SurfaceView implements SurfaceHolder.Callback
{
	SurfaceHolder mHolder;
	private GLThread mGLThread;
	private float K,ratio;
	
	private GLBackGround mBackGround;
	public GLView glView;
	private GLSolidSphere mGLSolidSphere;
	private Bubble b1,b2;//Define your bubbles here...
	
	//Variables to handle mouse click events..
	
	private boolean isClicked=false;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//use this to handle the click on bubble... yet to be implemented..
		if(event.getAction()==MotionEvent.ACTION_UP){
			synchronized(mGLThread){
				
				isClicked=true;
			}
		}
		mGLThread.requestExitAndWait();
		return super.onTouchEvent(event);
	}

	GLSurfaceView(Context context) {
		super(context);
		mHolder = getHolder();
		mHolder.addCallback(this);
		glView = new GLView();//this is used to measure frame rate.. to be removed in production..
	}

	public void surfaceCreated(SurfaceHolder holder) {
		mGLThread = new GLThread();
		mGLThread.start();
		
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		mGLThread.requestExitAndWait();
		mGLThread = null;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		mGLThread.onWindowResize(w, h);
	}

// ----------------------------------------------------------------------

	class GLThread extends Thread
	{
		private boolean mDone;
		private int     mWidth;
		private int     mHeight;
		
		
		GLThread() {
			super();
			mDone = false;
			mWidth = 0;
			mHeight = 0;
		}
		
		void initGL(GL10 gl){
			//Create your bubbles here and use them later
			//instead of creating them for every frame. like b1 and b2..
			//Have an array of bubbles??
			mGLSolidSphere=new GLSolidSphere(12,18,1.0f);
			b1 = new Bubble(mGLSolidSphere);
			b2 = new Bubble(mGLSolidSphere);
			b1.x = 1.5f;
			b2.x = -1.5f;			
			refreshOnResize(gl);
			
			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glLoadIdentity();
			K=4.0f;
			ratio = (float)mHeight / (float)mWidth;
			gl.glViewport(0, 0, mWidth, mHeight);

			mBackGround = new GLBackGround(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.waterxp_glview),
					-K,2*K, -K*ratio, 2*K*ratio, -5.0f);
			
			mBackGround.init(gl);
			
			gl.glOrthof (-K,K, -K*ratio, K*ratio, -10.0f, 10.0f);
			
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadIdentity();
			GLU.gluLookAt(gl, 0.0f, 0.0f, 4.0f, 0.0f, 0.0f, 0.0f, 0, 1, 0);
			
			gl.glClearColor (0.0f, 0.0f, 0.0f, 0.0f);
			gl.glShadeModel (GL10.GL_SMOOTH);
			gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			
			gl.glEnable(GL10.GL_DEPTH_TEST);
			gl.glEnable(GL10.GL_NORMALIZE);
			
			//Code to setup lighting.. move it to some other place??
				float modellightAmbient[] = { 1.0f, 1.0f, 1.0f, 1.0f };
				float lightAmbient[] = { 1.0f, 1.0f, 1.0f, 1.0f };
				float lightDiffuse[] = { 1.0f, 1.0f, 1.0f, 1.0f };
				float lightSpecular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
				
				float light_position[] = { 1.0f, 1.0f, 1.0f, 0 };
				
				gl.glLightModelfv(GL10.GL_LIGHT_MODEL_AMBIENT, modellightAmbient, 0);			
				gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, light_position,0);
				gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuse,0);
				gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbient,0);
				gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, lightSpecular,0);
				gl.glEnable(GL10.GL_LIGHT0);
		}
		
		void refreshOnResize(GL10 gl){
			ratio = (float)mHeight / (float)mWidth;
			//Resize does not work presently!.. do we need it??
		}

		@Override
		public void run() {
			OpenGLContext glc = new OpenGLContext( OpenGLContext.DEPTH_BUFFER );

			//using the debugging helper wrapper now.. is if of any use??
			//GL10 gl = (GL10)android.opengl.GLDebugHelper.wrap(glc.getGL(),true,null);
			GL10 gl = (GL10)glc.getGL();

			initGL(gl);

			SurfaceHolder holder = mHolder;
			while (!mDone) {
				int w;
				synchronized(this) {
					w = mWidth;
				
					if(isClicked){
						//process click here..first convert the pixel co-ords to model co-ords;
					}
				}

				Canvas canvas = holder.lockCanvas();
                glc.makeCurrent(holder);
				glc.waitNative();

				drawFrame(gl);
				
				glc.waitGL();
				glView.showMessages(canvas);
				glView.showStatistics(canvas, w);

				holder.unlockCanvasAndPost(canvas);
				try {
					Thread.sleep(5);
				}catch(InterruptedException e) {
				}
			}
		}
		
		private void drawFrame(GL10 gl) {
			
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

			gl.glDisable(GL10.GL_LIGHTING);
			gl.glDisable(GL10.GL_BLEND);			
			mBackGround.draw(gl);
			gl.glEnable(GL10.GL_BLEND);
			gl.glEnable(GL10.GL_LIGHTING);			
			
			//Do what ever you want to the bubbles here!
			//set their color, transparency, location, radius, text..
			//and then call Bubble.drawBubble() on all of them..
			//create animations by changing the position of the bubble
			//every frame..
			
			b1.alpha=0.5f;//make the first bubble half transparent..
			
			b1.drawBubble(gl);
			b2.drawBubble(gl);
		}

		public void onWindowResize(int w, int h) {
			synchronized(this) {
				mWidth = w;
				mHeight = h;
			}
		}

		public void requestExitAndWait() {
			// don't call this from GLThread thread or it a guaranteed
			// deadlock!
			mDone = true;
			try {
				join();
			} catch (InterruptedException ex) { }
		}		
	}
}
