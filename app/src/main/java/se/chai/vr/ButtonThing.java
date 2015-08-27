package se.chai.vr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import se.chai.cardboardremotedesktop.R;
import se.chai.cardboardremotedesktop.WorldLayoutData;

/**
 * Created by henrik on 15. 4. 2.
 */
public class ButtonThing extends TexturedThing
{

    OnTriggerListener onTriggerListener;
    protected float distance;

    public ButtonThing(Engine engine) {
        super(engine);
    }

    public boolean init() {
        float[] u, tm, tl, tr;

        u = WorldLayoutData.FACE_COORDS;
        tm = WorldLayoutData.FACE_TEXCOORDS_MONO;

        ByteBuffer bbVertices = ByteBuffer.allocateDirect(u.length * 4);
        bbVertices.order(ByteOrder.nativeOrder());
        vertices = bbVertices.asFloatBuffer();
        vertices.put(u);
        vertices.position(0);

        ByteBuffer bbTexcoords = ByteBuffer.allocateDirect(tm.length * 4);
        bbTexcoords.order(ByteOrder.nativeOrder());
        texCords = bbTexcoords.asFloatBuffer();
        texCords.put(tm);
        texCords.position(0);

        alpha = 1;//0.1f;

        Matrix.setIdentityM(model, 0);

//        pitchLimit = yawLimit = (float) Math.atan2(.7, 10);
//        pitchLimit = yawLimit = .05f;

        return true;
    }



    public void setOnTriggerListener(OnTriggerListener listener) {
        onTriggerListener = listener;
    }

    @Override
    public boolean isLookingAtObject(float[] mHeadView) {
        float[] dist = intersects(mHeadView);

        float pitch = dist[1];
        float yaw = dist[0];

        //alpha = Math.min(1,Math.max(0.3f, (.5f - Math.abs(pitch/distance))));
//        alpha = Math.max(0, (1 - Math.abs(pitch)));

        //if (Math.abs(pitch) < pitchLimit && Math.abs(yaw) < yawLimit) {
        if (Math.abs(pitch) < pitchLimit && Math.abs(yaw) < yawLimit) {
            alpha = 1;
            return true;
        } else {
            return false;
        }
//        return Math.abs(pitch) < PITCH_LIMIT && Math.abs(yaw) < YAW_LIMIT;
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
            Log.d("VRTV", "Intersect, parallell");
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

    private float[] intersectsQuad(float[] forwardVector, float[] n, float[] p0, float[] l0) {
        float[] l = forwardVector;//new float[4];
        l[0] = -l[0];
        l[1] = -l[1];
//        float[] n = {0, 0, 1, 0};
//        float[] p0 = {0, 0, -mScreenDistance, 0};
//        float[] l0 = {0, 0, CAMERA_Z, 0};
        float d;
        float[] res = new float[4];

        //Matrix.multiplyMV(l, 0, mHeadView, 0, initVec, 0);
        float denominator = Vec4f.dot(l, n);
        if (denominator == 0) {
            res[3] = -1;
            return res;
        }

        d = Vec4f.dot(Vec4f.sub(p0, l0), n) / denominator;
        res = Vec4f.add(Vec4f.mul(d, l), l0);

        return res;
    }

    public boolean onTrigger(float[] headView) {
        if (!isHidden && isLookingAtObject(headView)) {
            if (onTriggerListener != null) {
                onTriggerListener.onTrigger(this);
            }
            return true;
        }

        return false;
    }



    @Override
    public void translate(float x, float y, float z) {
        distance = z;
        Matrix.translateM(model, 0, x, y, z);
    }

}
