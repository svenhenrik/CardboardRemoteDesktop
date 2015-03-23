/*
 * Copyright 2014 Google Inc. All Rights Reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.chai.cardboardtools;

import android.util.FloatMath;

import com.google.vrtoolkit.cardboard.Eye;

/**
 * Contains vertex, normal and color data.
 */
public final class WorldLayoutData {

    public static final float[] FACE_COORDS = new float[]{
            // Front face
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
    };

    public static final float[] FACE_TEXCOORDS_MONO = new float[]{
            // Front face
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
    };

    public static final float[] FACE_TEXCOORDS_LEFT = new float[]{
            // Front face
            0.0f, 0.0f,
            0.0f, 1.0f,
            .5f, 0.0f,
            0.0f, 1.0f,
            0.5f, 1.0f,
            0.5f, 0.0f,
    };

    public static final float[] FACE_TEXCOORDS_RIGHT = new float[]{
            // Front face
            0.5f, 0.0f,
            0.5f, 1.0f,
            1.0f, 0.0f,
            0.5f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
    };


    public static final float[] FLOOR_COORDS = new float[]{
            200f, 0, -200f,
            -200f, 0, -200f,
            -200f, 0, 200f,
            200f, 0, -200f,
            -200f, 0, 200f,
            200f, 0, 200f,
    };

    public static final float[] FLOOR_NORMALS = new float[]{
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
    };

    public static final float[] FLOOR_COLORS = new float[]{
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
    };

    public static float[] GenerateUnitSphereTexCoords(int xsteps, int ysteps, int eye, float yoff) {
        int stride = 2 * 3 * 2 * (xsteps - 1);
        float[] verts = new float[(ysteps - 1) * stride];
        float xd, yd, xoff = 0;
        if (eye == Eye.Type.MONOCULAR) {
            xd = 1.0f / (xsteps - 1);
            yd = 1.0f / (ysteps - 1);
        } else {
            xd = 0.5f / (xsteps - 1);
            yd = (1.0f - 2 * yoff) / (ysteps - 1);

            if (eye == Eye.Type.RIGHT) {
                xoff = 0.5f;
            }
        }

        for (int y = 0; y < ysteps - 1; y++) {
            for (int x = 0; x < xsteps - 1; x++) {
                int i = 0;
                //1
                //x
                verts[y * stride + x * 12 + i++] = x * xd + xoff;
                //y
                verts[y * stride + x * 12 + i++] = y * yd + yoff;

                //2
                //x
                verts[y * stride + x * 12 + i++] = x * xd + xoff;
                //y
                verts[y * stride + x * 12 + i++] = (y + 1) * yd + yoff;

                //3
                //x
                verts[y * stride + x * 12 + i++] = (x + 1) * xd + xoff;
                //y
                verts[y * stride + x * 12 + i++] = y * yd + yoff;

                //4
                //x
                verts[y * stride + x * 12 + i++] = (x + 1) * xd + xoff;
                //y
                verts[y * stride + x * 12 + i++] = y * yd + yoff;

                //5
                //x
                verts[y * stride + x * 12 + i++] = x * xd + xoff;
                //y
                verts[y * stride + x * 12 + i++] = (y + 1) * yd + yoff;

                //6
                //x
                verts[y * stride + x * 12 + i++] = (x + 1) * xd + xoff;
                //y
                verts[y * stride + x * 12 + i] = (y + 1) * yd + yoff;
            }
        }

        return verts;
    }

    public static float[] GenerateUnitSphereTexCoords2(int xsteps, int ysteps, int eye) {
        int stride = 2 * 3 * 2 * (xsteps - 1);
        float[] verts = new float[(ysteps - 1) * stride];
        float xoff = 0;
        float yoff = 0.5f;


        if (eye == Eye.Type.RIGHT) {
            xoff = 0.75f;
        } else {
            xoff = 0.25f;
        }


        float fPI = (float) Math.PI;

        float theta_step = fPI / (xsteps - 1);
        float phi_step = fPI / (ysteps - 1);
        float phi_start = -fPI / 2;
        float theta_start = fPI;


        for (int y = 0; y < ysteps - 1; y++) {
            for (int x = 0; x < xsteps - 1; x++) {
                int i = 0;
                //1
                //x
                verts[y * stride + x * 12 + i++] = FloatMath.cos(theta_start + x * theta_step) * FloatMath.cos(phi_start + y * phi_step) * 0.25f + xoff;
                //y
                verts[y * stride + x * 12 + i++] = FloatMath.sin(phi_start + y * phi_step) * yoff + yoff;

                //2
                //x
                verts[y * stride + x * 12 + i++] = FloatMath.cos(theta_start + x * theta_step) * FloatMath.cos(phi_start + (y + 1) * phi_step) * 0.25f + xoff;
                //y
                verts[y * stride + x * 12 + i++] = FloatMath.sin(phi_start + (y + 1) * phi_step) * yoff + yoff;

                //3
                //x
                verts[y * stride + x * 12 + i++] = FloatMath.cos(theta_start + (x + 1) * theta_step) * FloatMath.cos(phi_start + y * phi_step) * 0.25f + xoff;
                //y
                verts[y * stride + x * 12 + i++] = FloatMath.sin(phi_start + y * phi_step) * yoff + yoff;

                //4
                //x
                verts[y * stride + x * 12 + i++] = FloatMath.cos(theta_start + (x + 1) * theta_step) * FloatMath.cos(phi_start + y * phi_step) * 0.25f + xoff;
                //y
                verts[y * stride + x * 12 + i++] = FloatMath.sin(phi_start + y * phi_step) * yoff + yoff;

                //5
                //x
                verts[y * stride + x * 12 + i++] = FloatMath.cos(theta_start + x * theta_step) * FloatMath.cos(phi_start + (y + 1) * phi_step) * 0.25f + xoff;
                //y
                verts[y * stride + x * 12 + i++] = FloatMath.sin(phi_start + (y + 1) * phi_step) * yoff + yoff;

                //6
                //x
                verts[y * stride + x * 12 + i++] = FloatMath.cos(theta_start + (x + 1) * theta_step) * FloatMath.cos(phi_start + (y + 1) * phi_step) * 0.25f + xoff;
                //y
                verts[y * stride + x * 12 + i] = FloatMath.sin(phi_start + (y + 1) * phi_step) * yoff + yoff;
            }
        }

        return verts;
    }

    public static float[] GenerateUnitSphere(int xsteps, int ysteps, boolean half) {
        // phi : PI/2 -> -PI/2
        // theta : PI -> 2PI
        float fPI = (float) Math.PI;

        float phi_start = fPI / 2;
        float phi_step = -fPI / (ysteps - 1);
        float theta_start;
        float theta_step;

        if (half) {
            theta_start = fPI;
            theta_step = fPI / (xsteps - 1);
        } else {
            theta_start = 1.5f * fPI;
            theta_step = 2 * fPI / (xsteps - 1);
        }

        // 2 tris per step, 3 verts per tri, 3 coords per vert
        int stride = 2 * 3 * 3 * (xsteps - 1);
        float[] verts = new float[(ysteps - 1) * stride];

        for (int y = 0; y < ysteps - 1; y++) {
            for (int x = 0; x < xsteps - 1; x++) {
                int i = 0;
                // vert 1
                // x
                verts[y * stride + x * 18 + i++] = FloatMath.cos(theta_start + x * theta_step) * FloatMath.cos(phi_start + y * phi_step);
                // y
                verts[y * stride + x * 18 + i++] = FloatMath.sin(phi_start + y * phi_step);
                // z
                verts[y * stride + x * 18 + i++] = FloatMath.sin(theta_start + x * theta_step) * FloatMath.cos(phi_start + y * phi_step);

                // vert 2
                // x
                verts[y * stride + x * 18 + i++] = FloatMath.cos(theta_start + x * theta_step) * FloatMath.cos(phi_start + (y + 1) * phi_step);
                // y
                verts[y * stride + x * 18 + i++] = FloatMath.sin(phi_start + (y + 1) * phi_step);
                // z
                verts[y * stride + x * 18 + i++] = FloatMath.sin(theta_start + x * theta_step) * FloatMath.cos(phi_start + (y + 1) * phi_step);

                // vert 3
                // x
                verts[y * stride + x * 18 + i++] = FloatMath.cos(theta_start + (x + 1) * theta_step) * FloatMath.cos(phi_start + y * phi_step);
                // y
                verts[y * stride + x * 18 + i++] = FloatMath.sin(phi_start + y * phi_step);
                // z
                verts[y * stride + x * 18 + i++] = FloatMath.sin(theta_start + (x + 1) * theta_step) * FloatMath.cos(phi_start + y * phi_step);

                // vert 4
                // x
                verts[y * stride + x * 18 + i++] = FloatMath.cos(theta_start + (x + 1) * theta_step) * FloatMath.cos(phi_start + y * phi_step);
                // y
                verts[y * stride + x * 18 + i++] = FloatMath.sin(phi_start + y * phi_step);
                // z
                verts[y * stride + x * 18 + i++] = FloatMath.sin(theta_start + (x + 1) * theta_step) * FloatMath.cos(phi_start + y * phi_step);

                // vert 5
                // x
                verts[y * stride + x * 18 + i++] = FloatMath.cos(theta_start + x * theta_step) * FloatMath.cos(phi_start + (y + 1) * phi_step);
                // y
                verts[y * stride + x * 18 + i++] = FloatMath.sin(phi_start + (y + 1) * phi_step);
                // z
                verts[y * stride + x * 18 + i++] = FloatMath.sin(theta_start + x * theta_step) * FloatMath.cos(phi_start + (y + 1) * phi_step);

                // vert 6
                // x
                verts[y * stride + x * 18 + i++] = FloatMath.cos(theta_start + (x + 1) * theta_step) * FloatMath.cos(phi_start + (y + 1) * phi_step);
                // y
                verts[y * stride + x * 18 + i++] = FloatMath.sin(phi_start + (y + 1) * phi_step);
                // z
                verts[y * stride + x * 18 + i] = FloatMath.sin(theta_start + (x + 1) * theta_step) * FloatMath.cos(phi_start + (y + 1) * phi_step);

//                for (i=0; i<6; i++) {
//                    System.out.println("coords, x="+x+", y="+y+", i="+i);
//                    System.out.println("v[" +(y*stride+x*18+i*3) +"].x = "+ verts[y*stride+x*18+i*3]);
//                    System.out.println("v[" +(y*stride+x*18+i*3+1) +"].y = "+ verts[y*stride+x*18+i*3+1]);
//                    System.out.println("v[" +(y*stride+x*18+i*3+2) +"].z = "+ verts[y*stride+x*18+i*3+2]);
//                }
            }
        }

        return verts;

    }
}
