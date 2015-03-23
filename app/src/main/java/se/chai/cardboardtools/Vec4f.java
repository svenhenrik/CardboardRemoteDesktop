package se.chai.cardboardtools;

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
}
