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

package se.chai.cardboardremotedesktop;

import android.util.FloatMath;
import android.util.Log;

import com.google.vrtoolkit.cardboard.Eye;

import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import se.chai.vr.objparse.ObjData;
import se.chai.vr.objparse.ObjParser;

/**
 * Contains vertex, normal and color data.
 */
public final class WorldLayoutData {

    public static final float[] FACE_COORDS = new float[] {
            // Front face
            -1.0f,  1.0f, 0f,
            -1.0f, -1.0f, 0f,
             1.0f,  1.0f, 0f,
            -1.0f, -1.0f, 0f,
             1.0f, -1.0f, 0f,
             1.0f,  1.0f, 0f,
    };

    public static final float[] FACE_COLORS = new float[] {
            // front, white
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
    };

    public static final float[] FACE_NORMALS = new float[] {
            // Front face
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
    };

    public static final float[] FACE_TEXCOORDS_MONO = new float[] {
            // Front face
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
    };

    public static final float[] FACE_TEXCOORDS_LEFT = new float[] {
            // Front face
            0.0f, 0.0f,
            0.0f, 1.0f,
            .5f, 0.0f,
            0.0f, 1.0f,
            0.5f, 1.0f,
            0.5f, 0.0f,
    };

    public static final float[] FACE_TEXCOORDS_RIGHT = new float[] {
            // Front face
            0.5f, 0.0f,
            0.5f, 1.0f,
            1.0f, 0.0f,
            0.5f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
    };

    public static final float[] FACE_TEXCOORDS_DOWN = new float[] {
            // Front face
            0.0f, 0.5f,
            0.0f, 1.0f,
            1.0f, 0.5f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.5f,
    };

    public static final float[] FACE_TEXCOORDS_UP = new float[] {
            // Front face
            0.0f, 0.0f,
            0.0f, 0.5f,
            1.0f, 0.0f,
            0.0f, 0.5f,
            1.0f, 0.5f,
            1.0f, 0.0f,
    };

    public static final float[] FLOOR_COORDS = new float[] {
            200f, 0, -200f,
            -200f, 0, -200f,
            -200f, 0, 200f,
            200f, 0, -200f,
            -200f, 0, 200f,
            200f, 0, 200f,
    };

    public static final float[] FLOOR_NORMALS = new float[] {
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
    };

    public static final float[] FLOOR_COLORS = new float[] {
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
    };

    public static float[] GeneratePanoramicTexCords(int xsteps, int ysteps, int eye, float yscale, boolean sbs) {
        int stride = 2 * 3 * 2 * (xsteps-1);
        float[] verts = new float[(ysteps-1) * stride];
        float xd, yd, xoff = 0, yoff = 0;



        if (eye == Eye.Type.MONOCULAR) {
            xd = 1.0f / (xsteps-1);
            yd = 1.0f / (ysteps-1);
        } else {
            if (sbs) {
                xd = 0.5f / (xsteps - 1);
                yd = yscale * 1.0f / (ysteps - 1);
                yoff = (1.0f - yscale) / 2;

                if (eye == Eye.Type.RIGHT) {
                    xoff = 0.5f;
                }
            } else {
                xd = 1.0f / (xsteps - 1);
                yd = 0.5f / (ysteps - 1);

                if (eye == Eye.Type.RIGHT) {
                    yoff = 0.5f;
                }
            }
        }

        for (int y = 0; y < ysteps - 1; y++) {
            for (int x = 0; x < xsteps - 1; x++) {
                int i = 0;
                //1
                //x
                verts[y * stride + x*12 + i++] = x * xd + xoff;
                //y
                verts[y * stride + x*12 + i++] = y * yd + yoff;

                //2
                //x
                verts[y * stride + x*12 + i++] = x * xd + xoff;
                //y
                verts[y * stride + x*12 + i++] = (y + 1) * yd + yoff;

                //3
                //x
                verts[y * stride + x*12 + i++] = (x + 1) * xd + xoff;
                //y
                verts[y * stride + x*12 + i++] = y * yd + yoff;

                //4
                //x
                verts[y * stride + x*12 + i++] = (x + 1) * xd + xoff;
                //y
                verts[y * stride + x*12 + i++] = y * yd + yoff;

                //5
                //x
                verts[y * stride + x*12 + i++] = x * xd + xoff;
                //y
                verts[y * stride + x*12 + i++] = (y + 1) * yd + yoff;

                //6
                //x
                verts[y * stride + x*12 + i++] = (x + 1) * xd + xoff;
                //y
                verts[y * stride + x*12 + i++] = (y + 1) * yd + yoff;

                Log.d("VNC", String.format("texture: x = %d/%d, xy: %.2f,%.2f x+1,y+1: %.2f,%.2f", x, xsteps-2, x*xd+xoff, (x+1)*xd+xoff, y * yd + yoff, (y + 1) * yd + yoff));
            }
        }

        return verts;
    }

    public static float[] GenerateFishEyeTexCords(int xsteps, int ysteps, int eye, boolean sbs) {
        int stride = 2 * 3 * 2 * (xsteps-1);
        float[] verts = new float[(ysteps-1) * stride];
        float xd, yd, xoff = 0, yoff = 0.5f, xrad = 0.5f, yrad = 0.5f;
        if (eye == Eye.Type.MONOCULAR) {
            xd = 1.0f / (xsteps-1);
            yd = 1.0f / (ysteps-1);
        } else {
            if (sbs) {
                xd = 0.5f / (xsteps - 1);
                yd = 1.0f / (ysteps - 1);
                yoff = 0.5f;
                yrad = 0.5f;
                xrad = 0.25f;

                if (eye == Eye.Type.RIGHT) {
                    xoff = 0.75f;
                } else {
                    xoff = 0.25f;
                }
            } else {
                xd = 1.0f / (xsteps - 1);
                yd = 0.5f / (ysteps - 1);
                xoff = 0.5f;
                xrad = 0.5f;
                yrad = 0.25f;

                if (eye == Eye.Type.RIGHT) {
                    yoff = 0.75f;
                } else {
                    yoff = 0.25f;
                }
            }
        }

        float fPI = (float)Math.PI;

        float theta_step = fPI/(xsteps-1);
        float phi_step = fPI/(ysteps-1);
        float phi_start = -fPI/2;
        float theta_start = fPI;


        for (int y = 0; y < ysteps - 1; y++) {
            for (int x = 0; x < xsteps - 1; x++) {
                int i = 0;
                //1
                //x
                verts[y * stride + x*12 + i++] = FloatMath.cos(theta_start + x * theta_step) * FloatMath.cos(phi_start + y * phi_step) * xrad + xoff;
                //y
                verts[y * stride + x*12 + i++] = FloatMath.sin(phi_start + y * phi_step) * yrad + yoff;

                //2
                //x
                verts[y * stride + x*12 + i++] = FloatMath.cos(theta_start + x * theta_step) * FloatMath.cos(phi_start + (y+1) * phi_step) * xrad + xoff;
                //y
                verts[y * stride + x*12 + i++] = FloatMath.sin(phi_start + (y+1) * phi_step)  * yrad + yoff;

                //3
                //x
                verts[y * stride + x*12 + i++] = FloatMath.cos(theta_start + (x+1) * theta_step) * FloatMath.cos(phi_start + y * phi_step) * xrad + xoff;
                //y
                verts[y * stride + x*12 + i++] = FloatMath.sin(phi_start + y * phi_step) * yrad + yoff;

                //4
                //x
                verts[y * stride + x*12 + i++] = FloatMath.cos(theta_start + (x+1) * theta_step) * FloatMath.cos(phi_start + y * phi_step) * xrad + xoff;
                //y
                verts[y * stride + x*12 + i++] = FloatMath.sin(phi_start + y * phi_step) * yrad + yoff;

                //5
                //x
                verts[y * stride + x*12 + i++] = FloatMath.cos(theta_start + x * theta_step) * FloatMath.cos(phi_start + (y+1) * phi_step) * xrad + xoff;
                //y
                verts[y * stride + x*12 + i++] = FloatMath.sin(phi_start + (y+1) * phi_step) * yrad + yoff;

                //6
                //x
                verts[y * stride + x*12 + i++] = FloatMath.cos(theta_start + (x+1) * theta_step) * FloatMath.cos(phi_start + (y+1) * phi_step) * xrad + xoff;
                //y
                verts[y * stride + x*12 + i++] = FloatMath.sin(phi_start + (y+1) * phi_step) * yrad + yoff;
            }
        }

        return verts;
    }

    public static float[] GenerateUnitSphere(int xsteps, int ysteps, boolean half) {
        // phi : PI/2 -> -PI/2
        // theta : PI -> 2PI
        float fPI = (float)Math.PI;

        float phi_start = fPI/2;
        float phi_step = -fPI/(ysteps-1);
        float theta_start;
        float theta_step;

        if (half) {
            theta_start = fPI;
            theta_step = fPI / (xsteps - 1);
        } else {
            theta_start = 1.5f*fPI;
            theta_step = 2*fPI / (xsteps - 1);
        }

        // 2 tris per step, 3 verts per tri, 3 coords per vert
        int stride = 2 * 3 * 3 * (xsteps - 1);
        float[] verts = new float[(ysteps-1)*stride];

        for (int y=0; y < ysteps - 1; y++) {
            for (int x=0; x < xsteps - 1; x++) {
                int i=0;
                // vert 1
                // x
                verts[y * stride + x * 18 + i++] = FloatMath.cos(theta_start + x * theta_step) * FloatMath.cos(phi_start + y * phi_step);
                // y
                verts[y * stride + x * 18 + i++] = FloatMath.sin(phi_start + y * phi_step);
                // z
                verts[y * stride + x * 18 + i++] = FloatMath.sin(theta_start + x*theta_step) * FloatMath.cos(phi_start + y*phi_step);

                // vert 2
                // x
                verts[y * stride + x * 18 + i++] = FloatMath.cos(theta_start + x * theta_step) * FloatMath.cos(phi_start + (y+1) * phi_step);
                // y
                verts[y * stride + x * 18 + i++] = FloatMath.sin(phi_start + (y+1) * phi_step);
                // z
                verts[y * stride + x * 18 + i++] = FloatMath.sin(theta_start + x*theta_step) * FloatMath.cos(phi_start + (y+1)*phi_step);

                // vert 3
                // x
                verts[y * stride + x * 18 + i++] = FloatMath.cos(theta_start + (x+1) * theta_step) * FloatMath.cos(phi_start + y * phi_step);
                // y
                verts[y * stride + x * 18 + i++] = FloatMath.sin(phi_start + y * phi_step);
                // z
                verts[y * stride + x * 18 + i++] = FloatMath.sin(theta_start + (x+1)*theta_step) * FloatMath.cos(phi_start + y*phi_step);

                // vert 4
                // x
                verts[y * stride + x * 18 + i++] = FloatMath.cos(theta_start + (x+1) * theta_step) * FloatMath.cos(phi_start + y * phi_step);
                // y
                verts[y * stride + x * 18 + i++] = FloatMath.sin(phi_start + y * phi_step);
                // z
                verts[y * stride + x * 18 + i++] = FloatMath.sin(theta_start + (x+1)*theta_step) * FloatMath.cos(phi_start + y*phi_step);

                // vert 5
                // x
                verts[y * stride + x * 18 + i++] = FloatMath.cos(theta_start + x * theta_step) * FloatMath.cos(phi_start + (y+1) * phi_step);
                // y
                verts[y * stride + x * 18 + i++] = FloatMath.sin(phi_start + (y+1) * phi_step);
                // z
                verts[y * stride + x * 18 + i++] = FloatMath.sin(theta_start + x*theta_step) * FloatMath.cos(phi_start + (y+1)*phi_step);

                // vert 6
                // x
                verts[y * stride + x * 18 + i++] = FloatMath.cos(theta_start + (x+1) * theta_step) * FloatMath.cos(phi_start + (y+1) * phi_step);
                // y
                verts[y * stride + x * 18 + i++] = FloatMath.sin(phi_start + (y+1) * phi_step);
                // z
                verts[y * stride + x * 18 + i++] = FloatMath.sin(theta_start + (x+1)*theta_step) * FloatMath.cos(phi_start + (y+1)*phi_step);

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

    public static float[] GenerateCylinder(int xsteps, int ysteps, float spanRads) {
        // phi : PI/2 -> -PI/2
        // theta : PI -> 2PI
        float fPI = (float)Math.PI;

        float phi_start = 0;//fPI/2;
        float phi_step = 0;//-fPI/(ysteps-1);
        float theta_start;
        float theta_step;

        theta_start = -fPI/2 -spanRads/2;
        theta_step = spanRads / (xsteps -1);

        // 2 tris per step, 3 verts per tri, 3 coords per vert
        int stride = 2 * 3 * 3 * (xsteps - 1);
        float[] verts = new float[(ysteps-1)*stride];

        for (int y=0; y < ysteps - 1; y++) {
            for (int x=0; x < xsteps - 1; x++) {
                int i=0;
                // vert 1
                // x
                verts[y * stride + x * 18 + i++] = FloatMath.cos(theta_start + x * theta_step) * FloatMath.cos(phi_start + y * phi_step);
                // y
                verts[y * stride + x * 18 + i++] = (y+1) - (ysteps-1)/2.0f;//FloatMath.sin(phi_start + y * phi_step);
                // z
                verts[y * stride + x * 18 + i++] = FloatMath.sin(theta_start + x*theta_step) * FloatMath.cos(phi_start + y*phi_step);

                // vert 2
                // x
                verts[y * stride + x * 18 + i++] = FloatMath.cos(theta_start + x * theta_step) * FloatMath.cos(phi_start + (y+1) * phi_step);
                // y
                verts[y * stride + x * 18 + i++] = (y) - (ysteps-1)/2.0f;//FloatMath.sin(phi_start + (y+1) * phi_step);
                // z
                verts[y * stride + x * 18 + i++] = FloatMath.sin(theta_start + x*theta_step) * FloatMath.cos(phi_start + (y+1)*phi_step);

                // vert 3
                // x
                verts[y * stride + x * 18 + i++] = FloatMath.cos(theta_start + (x+1) * theta_step) * FloatMath.cos(phi_start + y * phi_step);
                // y
                verts[y * stride + x * 18 + i++] = (y+1) - (ysteps-1)/2.0f;//FloatMath.sin(phi_start + y * phi_step);
                // z
                verts[y * stride + x * 18 + i++] = FloatMath.sin(theta_start + (x+1)*theta_step) * FloatMath.cos(phi_start + y*phi_step);

                // vert 4
                // x
                verts[y * stride + x * 18 + i++] = FloatMath.cos(theta_start + (x+1) * theta_step) * FloatMath.cos(phi_start + y * phi_step);
                // y
                verts[y * stride + x * 18 + i++] = (y+1) - (ysteps-1)/2.0f;//FloatMath.sin(phi_start + y * phi_step);
                // z
                verts[y * stride + x * 18 + i++] = FloatMath.sin(theta_start + (x+1)*theta_step) * FloatMath.cos(phi_start + y*phi_step);

                // vert 5
                // x
                verts[y * stride + x * 18 + i++] = FloatMath.cos(theta_start + x * theta_step) * FloatMath.cos(phi_start + (y+1) * phi_step);
                // y
                verts[y * stride + x * 18 + i++] = (y) - (ysteps-1)/2.0f;//FloatMath.sin(phi_start + (y+1) * phi_step);
                // z
                verts[y * stride + x * 18 + i++] = FloatMath.sin(theta_start + x*theta_step) * FloatMath.cos(phi_start + (y+1)*phi_step);

                // vert 6
                // x
                verts[y * stride + x * 18 + i++] = FloatMath.cos(theta_start + (x+1) * theta_step) * FloatMath.cos(phi_start + (y+1) * phi_step);
                // y
                verts[y * stride + x * 18 + i++] = (y) - (ysteps-1)/2.0f;//FloatMath.sin(phi_start + (y+1) * phi_step);
                // z
                verts[y * stride + x * 18 + i++] = FloatMath.sin(theta_start + (x+1)*theta_step) * FloatMath.cos(phi_start + (y+1)*phi_step);

                Log.d("VNC", String.format("x =%d/%d, theta(x) = %.2f, theta(x+1) = %.2f", x, xsteps-2, theta_start + x*theta_step, theta_start + (x+1)*theta_step));
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

    public static float[] GenerateCylinderTexCords(int xsteps, int ysteps) {
        int stride = 2 * 3 * 2 * (xsteps-1);
        float[] verts = new float[(ysteps-1) * stride];
        float xd, yd, xoff = 0, yoff = 0;

        xd = 1.0f / (xsteps-1);
        yd = 1.0f / (ysteps-1);

        for (int y = 0; y < ysteps - 1; y++) {
            for (int x = 0; x < xsteps - 1; x++) {
                int i = 0;
                //1
                //x
                verts[y * stride + x*12 + i++] = x * xd + xoff;
                //y
                verts[y * stride + x*12 + i++] = y * yd + yoff;

                //2
                //x
                verts[y * stride + x*12 + i++] = x * xd + xoff;
                //y
                verts[y * stride + x*12 + i++] = (y + 1) * yd + yoff;

                //3
                //x
                verts[y * stride + x*12 + i++] = (x + 1) * xd + xoff;
                //y
                verts[y * stride + x*12 + i++] = y * yd + yoff;

                //4
                //x
                verts[y * stride + x*12 + i++] = (x + 1) * xd + xoff;
                //y
                verts[y * stride + x*12 + i++] = y * yd + yoff;

                //5
                //x
                verts[y * stride + x*12 + i++] = x * xd + xoff;
                //y
                verts[y * stride + x*12 + i++] = (y + 1) * yd + yoff;

                //6
                //x
                verts[y * stride + x*12 + i++] = (x + 1) * xd + xoff;
                //y
                verts[y * stride + x*12 + i++] = (y + 1) * yd + yoff;
            }
        }

        return verts;
    }

    public static ArrayList<FloatBuffer> envVerts;
    public static ArrayList<FloatBuffer> envTexCords;
    public static ArrayList<Boolean> envLoaded;
    public static ArrayList<Integer> envTexResId;

    public static void loadEnvironment(InputStream obj, int resId) {
        if (envLoaded == null)
            envLoaded = new ArrayList<>();
        if (envVerts == null)
            envVerts = new ArrayList<>();
        if (envTexCords == null)
            envTexCords = new ArrayList<>();
        if (envTexResId == null)
            envTexResId = new ArrayList<>();

        ObjParser parser = new ObjParser();
        ObjData data = parser.parseOBJ(obj);

        envVerts.add(data.triBuffer);
        envTexCords.add(data.texCordBuffer);
        envTexResId.add(resId);
        envLoaded.add(true);
    }
}
