package org.magnus.glview;

import javax.microedition.khronos.opengles.GL10;

public class Bubble {
	private GLSolidSphere mSolidSphere;
	public float x,y,z,radius;
	public float shininess;
	public float alpha;
	public float material_ambient[]={ 0.05f, 0.05f, 0.05f, 1.0f };
	public float material_diffuse[]={ 0.0f, 0.7f, 0.0f, 1.0f };
	public float material_specular[]={ 1.0f, 1.0f, 1.0f, 1.0f };
	public String BubbleText; 
	
	public Bubble(GLSolidSphere sphere){
		this.mSolidSphere=sphere;
		x=0;y=0;z=0;radius=1;
		shininess=50;
		alpha=1.0f;
	}
	
	public void drawBubble(GL10 gl){
		material_ambient[3]=material_diffuse[3]=material_specular[3]=alpha;
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, material_ambient,0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, material_diffuse,0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, material_specular,0);		
		gl.glMaterialf(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, shininess);
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glTranslatef(x, y, z);
		gl.glScalef(radius, radius, radius);
		mSolidSphere.draw(gl);
		gl.glPopMatrix();
	}
}
