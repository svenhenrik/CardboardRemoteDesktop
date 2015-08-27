package se.chai.vr.objparse;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;

public class ObjData {
    Vector<Float> v;
    Vector<Float> vn;
    Vector<Float> vt;
    public Vector<ObjDataPart> parts;
    public FloatBuffer vertexBuffer;
    public FloatBuffer normalBuffer;

    public FloatBuffer triBuffer;
    public FloatBuffer texCordBuffer;

    public ObjData(Vector<Float> v, Vector<Float> vn, Vector<Float> vt,
                   Vector<ObjDataPart> parts) {
        super();
        this.v = v;
        this.vn = vn;
        this.vt = vt;
        this.parts = parts;
    }
    public String toString(){
        String str=new String();
        str+="Number of parts: "+parts.size();
        str+="\nNumber of vertexes: "+v.size();
        str+="\nNumber of vns: "+vn.size();
        str+="\nNumber of vts: "+vt.size();
        str+="\n/////////////////////////\n";
        for(int i=0; i<parts.size(); i++){
            str+="Part "+i+'\n';
            str+=parts.get(i).toString();
            str+="\n/////////////////////////";
        }
        return str;
    }
    public void draw(GL10 gl) {
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        for(int i=0; i<parts.size(); i++){
            ObjDataPart t=parts.get(i);
            Material m=t.getMaterial();
            if(m!=null){
                FloatBuffer a=m.getAmbientColorBuffer();
                FloatBuffer d=m.getDiffuseColorBuffer();
                FloatBuffer s=m.getSpecularColorBuffer();
                gl.glMaterialfv(GL10.GL_FRONT_AND_BACK,GL10.GL_AMBIENT,a);
                gl.glMaterialfv(GL10.GL_FRONT_AND_BACK,GL10.GL_SPECULAR,s);
                gl.glMaterialfv(GL10.GL_FRONT_AND_BACK,GL10.GL_DIFFUSE,d);
            }
            gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
            gl.glNormalPointer(GL10.GL_FLOAT, 0, t.getNormalBuffer());
            gl.glDrawElements(GL10.GL_TRIANGLES,t.getFacesCount(),GL10.GL_UNSIGNED_SHORT,t.getFaceBuffer());
            //gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            //gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
            gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
        }
    }
    public void buildFloatBuffers(){
        ByteBuffer vBuf = ByteBuffer.allocateDirect(v.size() * 4);
        vBuf.order(ByteOrder.nativeOrder());
        vertexBuffer = vBuf.asFloatBuffer();
        vertexBuffer.put(toPrimitiveArrayF(v));
        vertexBuffer.position(0);

        ByteBuffer vnBuf = ByteBuffer.allocateDirect(vn.size() * 4);
        vnBuf.order(ByteOrder.nativeOrder());
        normalBuffer = vnBuf.asFloatBuffer();
        normalBuffer.put(toPrimitiveArrayF(vn));
        normalBuffer.position(0);

        ObjDataPart t=parts.get(0);
        ByteBuffer triBuf = ByteBuffer.allocateDirect(t.faces.size() * 4 * 3);
        triBuf.order(ByteOrder.nativeOrder());
        triBuffer = triBuf.asFloatBuffer();
        for (int i=0; i<t.faces.size(); i++) {
            float x=v.get(t.faces.get(i)*3);
            float y=v.get(t.faces.get(i)*3+1);
            float z=v.get(t.faces.get(i)*3+2);
            triBuffer.put(x);
            triBuffer.put(y);
            triBuffer.put(z);
        }
        triBuffer.position(0);
        Log.d("VRTV", "Triangle buffer size: " + triBuffer.limit());
        Log.d("VRTV", "Triangles: " + triBuffer.limit()/3);

        ByteBuffer texBuf = ByteBuffer.allocateDirect(t.vtPointer.size() * 4 * 2);
        texBuf.order(ByteOrder.nativeOrder());
        texCordBuffer = texBuf.asFloatBuffer();
        for (int i=0; i<t.vtPointer.size(); i++) {
            float x=vt.get(t.vtPointer.get(i)*2);
            float y=vt.get(t.vtPointer.get(i)*2+1);
            texCordBuffer.put(x);
            texCordBuffer.put(y);
        }
        texCordBuffer.position(0);
        Log.d("VRTV", "Texcord buffer size: " + triBuffer.limit());
        Log.d("VRTV", "Texcoords: " + triBuffer.limit() / 3);
    }

    private static float[] toPrimitiveArrayF(Vector<Float> vector){
        float[] f;
        f=new float[vector.size()];
        for (int i=0; i<vector.size(); i++){
            f[i]=vector.get(i);
        }
        return f;
    }
}

