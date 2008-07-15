package org.magnus.glview;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;



public class GLSolidSphere {

	private FloatBuffer   mVertexBuffer;
	private FloatBuffer   mNormalBuffer;
	private float[] vertices,normals;
	private static final float PI_=3.141f;
	
	private int stacks,slices;
    
	
	public GLSolidSphere(int stacks,int slices,float radius)
	{
		this.stacks=stacks;
		this.slices=slices;
		
		vertices = new float[stacks*(slices+1)*2*3];
		normals=new float[stacks*(slices+1)*2*3];
		PlotSpherePoints(radius,stacks,slices,vertices,normals);

		mVertexBuffer = FloatBuffer.wrap(vertices);
		mVertexBuffer.position(0);

		mNormalBuffer = FloatBuffer.wrap(normals);
		mNormalBuffer.position(0);
	}

	public void draw(GL10 gl)
	{
		gl.glFrontFace(GL10.GL_CW);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
		gl.glNormalPointer(GL10.GL_FLOAT, 0, mNormalBuffer);
		
		gl.glEnableClientState (GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState (GL10.GL_NORMAL_ARRAY);
		
		int triangles = (slices + 1) * 2;

		for(int i = 0; i < stacks; i++)
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, i * triangles, triangles);
		
		gl.glDisableClientState (GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState (GL10.GL_NORMAL_ARRAY);
	}
	
	private void PlotSpherePoints(float radius, int stacks, int slices, float[] v, float[] n)
	{
		int i, j,vind,nind; 
		float slicestep, stackstep;
		
		
		vind=0;
		nind=0;

		stackstep = (PI_) / stacks;
		slicestep = 2.0f * (PI_) / slices;

		for (i = 0; i < stacks; ++i)		
		{
			float a = i * stackstep;
			float b = a + stackstep;

			float s0 =  (float)Math.sin(a);
			float s1 =  (float)Math.sin(b);

			float c0 =  (float)Math.cos(a);
			float c1 =  (float)Math.cos(b);

			for (j = 0; j <= slices; ++j)		
			{
				float c = j * slicestep;
				float x = (float)Math.cos(c);
				float y = (float)Math.sin(c);

				n[nind] = x * s0;
				v[vind] = n[nind] * radius;

				nind++;
				vind++;

				n[nind] = y * s0;
				v[vind] = n[nind] * radius;

				nind++;
				vind++;

				n[nind] = c0;
				v[vind] = n[nind] * radius;

				nind++;
				vind++;

				n[nind] = x * s1;
				v[vind] = n[nind] * radius;

				nind++;
				vind++;

				n[nind] = y * s1;
				v[vind] = n[nind] * radius;

				nind++;
				vind++;

				n[nind] = c1;
				v[vind] = n[nind] * radius;

				nind++;
				vind++;

			}
		}
	}
}
