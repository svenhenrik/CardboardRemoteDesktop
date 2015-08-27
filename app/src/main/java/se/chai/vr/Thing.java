package se.chai.vr;

import android.opengl.Matrix;
import android.util.Log;

import java.nio.FloatBuffer;

/**
 * Created by henrik on 15. 3. 28.
 */
public class Thing {
    public static final int COORDS_PER_VERTEX = 3;

    public String name;

    public boolean isHidden;

    public FloatBuffer vertices;
    public FloatBuffer colors;
    public FloatBuffer normals;

    public int program;

    public int positionA;
    public int normalA;
    public int colorA;
    public int modelViewProjectionA;

    public float[] model;

    public void setupShaders() {}

    public float pitchLimit;
    public float yawLimit;

    protected Engine engine;

    public Thing(Engine engine) {
        this.engine = engine;
        this.model = new float[16];
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Thing))
            return false;
        Thing data = (Thing) obj;

        return data.name != null && this.name != null && data.name.equals(this.name);
    }

    public void draw(int eye, float[] modelViewProjection) {
    }

    public boolean isLookingAtObject(float[] mHeadView) {
        float[] initVec = { 0, 0, 0, 1.0f };
        float[] objPositionVec = new float[4];
        float[] mModelView = new float[16];

        // Convert object space to camera space. Use the headView from onNewFrame.
        Matrix.multiplyMM(mModelView, 0, mHeadView, 0, this.model, 0);
        Matrix.multiplyMV(objPositionVec, 0, mModelView, 0, initVec, 0);

        float pitch = (float) Math.atan2(Math.abs(objPositionVec[1]), Math.abs(objPositionVec[2]));
        float yaw = (float) Math.atan2(Math.abs(objPositionVec[0]), Math.abs(objPositionVec[2]));

//        float pitch = (float) Math.atan2(objPositionVec[1], -objPositionVec[2]);
//        float yaw = (float) Math.atan2(objPositionVec[0], -objPositionVec[2]);

        return Math.abs(pitch) < pitchLimit && Math.abs(yaw) < yawLimit;
//        return Math.abs(pitch) < PITCH_LIMIT && Math.abs(yaw) < YAW_LIMIT;
    }

    public void translate(float x, float y, float z) {
        Matrix.translateM(model, 0, x, y, z);
    }

    public void scale(float x, float y, float z) {
        Matrix.scaleM(model, 0, x, y, z);
    }

    public void rotate(float a, float x, float y, float z) {
        Matrix.rotateM(model, 0, a, x, y, z);
    }

    public boolean onTrigger(float[] mHeadView) {
        return false;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void hide() {
        isHidden = true;
    }

    public void show() {
        isHidden = false;
    }


    protected float[] intersects(float[] headView) {
        float[] forward = { 0, 0, -1, 0};
        float[] initPos = { 0, 0, 0, 1 };
        float[] initNormal = { 0, 0, 1, 0 };

        float[] viewDirection = new float[4];
        float[] headInv = new float[16];
        float[] modelInv = new float[16];
        float[] di = new float[2];
        float[] n = new float[4];


        if (!Matrix.invertM(headInv, 0, headView, 0)) {
            di[0] = di[1] = 100;
            Log.d("VRTV", "Intersect: Couldn't invert headView matrix");
            return di;
        }
        if (!Matrix.invertM(modelInv, 0, this.model, 0)) {
            di[0] = di[1] = 100;
            Log.d("VRTV", "Intersect: Couldn't invert modelView matrix");
            return di;
        }

        Matrix.multiplyMV(viewDirection, 0, headInv, 0, forward, 0);

        float[] l = viewDirection;
        float[] p0 = new float[4];

        Matrix.multiplyMV(p0, 0, this.model, 0, initPos, 0);
        Matrix.multiplyMV(n, 0, this.model, 0, initNormal, 0);

        float[] l0 = new float[4];
        Matrix.multiplyMV(l0, 0, headView, 0, initPos, 0);
        float d;
        float[] res = new float[4];

        float denominator = Vec4f.dot(l, n);
        if (denominator == 0) {
            di[0] = di[1] = 100;
            Log.d("VR", "Intersect, parallell");
            return di;
        } else {
            d = Vec4f.dot(Vec4f.sub(p0, l0), n) / denominator;
            res = Vec4f.add(Vec4f.mul(d, l), l0);
        }

        //res = Vec4f.sub(res, p0);

        Matrix.multiplyMV(res, 0, modelInv, 0, res, 0);

//        di[0] = res[0];
//        di[1] = res[1];

        return res;
    }

}
