package se.chai.vr;

import android.util.FloatMath;

/**
 * Created by henrik on 15. 1. 30.
 */
public class Vec4f {

    public static float dot(float[] a, float[] b) {
        return a[0]*b[0]+a[1]*b[1]+a[2]*b[2]+a[3]*b[3];
    }

    public static float[] add(float[] a, float[] b) {
        float[] c = new float[4];

        c[0] = a[0]+b[0];
        c[1] = a[1]+b[1];
        c[2] = a[2]+b[2];
        c[3] = a[3]+b[3];

        return c;
    }

    public static float[] sub(float[] a, float[] b) {
        float[] c = new float[4];

        c[0] = a[0]-b[0];
        c[1] = a[1]-b[1];
        c[2] = a[2]-b[2];
        c[3] = a[3]-b[3];

        return c;
    }

    public static float[] mul(float s, float[] v) {
        float[] c = new float[4];

        c[0] = s*v[0];
        c[1] = s*v[1];
        c[2] = s*v[2];
        c[3] = s*v[3];

        return c;
    }

    public static float[] div(float s, float[] v) {
        float[] c = new float[4];

        c[0] = s/v[0];
        c[1] = s/v[1];
        c[2] = s/v[2];
        c[3] = s/v[3];

        return c;
    }

    public static float[] normalize(float v[]) {
        float mag = FloatMath.sqrt(v[0]*v[0]+v[1]*v[1]+v[2]*v[2]+v[3]*v[3]);
        return mul(1/mag, v);
    }

    public static float[] billboard(float m[]) {
        float tmp;
        tmp = m[1];
        m[1] = m[4];
        m[4] = tmp;

        tmp = m[2];
        m[2] = m[8];
        m[8] = tmp;

        tmp = m[6];
        m[6] = m[9];
        m[9] = tmp;

        return m;
    }

    public static void getEulerAngles(float[] eulerAngles, int offset, float[] mHeadView) {
        if (offset + 3 > eulerAngles.length) {
            throw new IllegalArgumentException(
                    "Not enough space to write the result");
        }
        float pitch = (float) Math.asin(mHeadView[6]);
        float roll;
        float yaw;
        if (FloatMath.sqrt(1.0F - mHeadView[6] * mHeadView[6]) >= 0.01F) {
            yaw = (float) Math.atan2(-mHeadView[2],
                    mHeadView[10]);
            roll = (float) Math.atan2(-mHeadView[4], mHeadView[5]);
        } else {
            yaw = 0.0F;
            roll = (float) Math.atan2(mHeadView[1], mHeadView[0]);
        }
        eulerAngles[(offset + 0)] = (-pitch);
        eulerAngles[(offset + 1)] = (-yaw);
        eulerAngles[(offset + 2)] = (-roll);
    }
}
