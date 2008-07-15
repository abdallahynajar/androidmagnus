package org.magnus.glview;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;

public class GLBackGround {

	private FloatBuffer   mVertexBuffer,mTexCoordBuffer;
	private int[] mTexNames=new int[2];
	private float[] vertices,texcoords;
	private Bitmap mBackGroundBitmap;
	int w,h;
	
	public GLBackGround(Bitmap backGroundBitmap,float cx,float lx,float cy,float ly,float z)
	{
		this.w=backGroundBitmap.width();
		this.h=backGroundBitmap.height();
		this.mBackGroundBitmap=backGroundBitmap;
		
		vertices = new float[]{
				cx,cy,z,
				cx+lx,cy,z,
				cx,cy+ly,z,														
				cx+lx,cy+ly,z,
		};
		texcoords=new float[]{
			0.0f,0.0f,
			1.0f,0.0f,
			0.0f,1.0f,			
			1.0f,1.0f,
		};
		mVertexBuffer = FloatBuffer.wrap(vertices);
		mVertexBuffer.position(0);
		mTexCoordBuffer = FloatBuffer.wrap(texcoords);
		mTexCoordBuffer.position(0);		
	}
	
	public void init(GL10 gl){
		
		gl.glActiveTexture(GL10.GL_TEXTURE0);
		gl.glGenTextures(1, mTexNames, 0);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTexNames[0]);
		int[] pixels = new int[w*h];
		mBackGroundBitmap.getPixels(pixels, 0, mBackGroundBitmap.width(), 0, 0, mBackGroundBitmap.width(), mBackGroundBitmap.height());
		ByteBuffer pxb = ByteBuffer.allocate(pixels.length*3);
		for(int i=0;i<pixels.length;i++){
			pxb.put((byte)(pixels[i]&0x000000FF));
			pxb.put((byte)((pixels[i]>>8)&0x000000FF));
			pxb.put((byte)((pixels[i]>>16)&0x000000FF));			
		}
		gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGB, w, h,
				0, GL10.GL_RGB, GL10.GL_UNSIGNED_BYTE, pxb);
		//gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		//gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);		
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
		mBackGroundBitmap=null;
	}

	public void draw(GL10 gl)
	{
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glFrontFace(GL10.GL_CW);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
		gl.glTexCoordPointer(2,GL10.GL_FLOAT, 0, mTexCoordBuffer);
		
		gl.glEnableClientState (GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState (GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0,4);
		
		gl.glDisableClientState (GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState (GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glDisable(GL10.GL_TEXTURE_2D);
	}
}
