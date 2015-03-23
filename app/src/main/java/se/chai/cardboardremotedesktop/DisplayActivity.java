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

import android.androidVNC.COLORMODEL;
import android.androidVNC.ConnectionBean;
import android.androidVNC.VncDatabase;
import android.androidVNC.VncView;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.FloatMath;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;

import se.chai.cardboardtools.CardboardOverlayView;
import se.chai.cardboardtools.Vec4f;
import se.chai.cardboardtools.WorldLayoutData;

/**
 * A Cardboard sample application.
 */
public class DisplayActivity extends CardboardActivity implements CardboardView.StereoRenderer, SurfaceTexture.OnFrameAvailableListener, VncView.IConnectionInfo {

    private static final String TAG = "MainActivity";

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100.0f;

    private static final float CAMERA_Z = 0.01f;
    private static final float TIME_DELTA = 0.3f;

    private static final float YAW_LIMIT = 0.12f;
    private static final float PITCH_LIMIT = 0.12f;

    private static final int COORDS_PER_VERTEX = 3;

    private static final WorldLayoutData DATA = new WorldLayoutData();

    // We keep the light always position just above the user.
    private static final float[] LIGHT_POS_IN_WORLD_SPACE = new float[]{0.0f, 2.0f, 0.0f, 1.0f};

    private final float[] mLightPosInEyeSpace = new float[4];

    private FloatBuffer mFloorVertices;
    private FloatBuffer mFloorColors;
    private FloatBuffer mFloorNormals;

    private FloatBuffer mScreenVertices;
    private FloatBuffer mScreenColors;
    private FloatBuffer mScreenTexcoords;
    private FloatBuffer mScreenTexcoordsLeft;
    private FloatBuffer mScreenTexcoordsRight;
    private FloatBuffer mScreenNormals;

    private int mScreenProgram;
    private int mEnvProgram;

    private int mScreenPositionA;
    private int mScreenNormalA;
    private int mScreenColorA;
    private int mScreenTextureA;
    private int mScreenTextureU;
    private int mScreenTextureTransformU;
    private int mScreenModelA;
    private int mScreenModelViewA;
    private int mScreenModelViewProjectionA;
    //private int mScreenModelViewProjectionA;
    private int mScreenLightPosA;

    private int mFloorPositionParam;
    private int mFloorNormalParam;
    private int mFloorColorParam;
    private int mFloorTextureParam;
    private int mFloorTextureUniformParam;
    private int mFloorModelParam;
    private int mFloorModelViewParam;
    private int mFloorModelViewProjectionParam;
    private int mFloorLightPosParam;

    private float[] mModelScreen;
    private float[] mModelCamScreen;
    private float[] mCamera;
    private float[] mView;
    private float[] mHeadView;
    private float[] mForwardVector;
    private float[] mModelViewProjection;
    private float[] mModelView;
    private float[] mModelFloor;

    private int mScore = 0;
    private float mObjectDistance = 12f;
    private float mFloorDepth = 20f;

    private Vibrator mVibrator;
    private CardboardOverlayView mOverlayView;

    private int mScreenTexture;
    private boolean mVideoFrameAvailable = false;
    private SurfaceTexture mVideoSurfaceTexture;
    private Surface mVideoSurface;
    private float[] mVideoTextureTransform;
    private MediaPlayer player;
    private float mScreenDistance = 6.0f;
    private float mScreenSize = 3f;
    private float mScreenTexYoff = 0.0f;

    private String mDataSource;
    private String mProjection;
    private String mDimensions;
    private String mScreenType;
    private String mVideo3DLayout;
    private String mExtraName;
    private String mExtraHost;
    private String mExtraUsername;
    private String mExtraPassword;
    private String mExtraColorMode;
    private Boolean mExtraViewerMode;

    private int mCameraTexture;
    private boolean mPreviewFrameAvailable = false;
    private SurfaceTexture mCameraSurfaceTexture;
    private Surface mCameraSurface;
    private float[] mCameraTextureTransform;
    private Camera mHWCamera;

    private VncView vncView;
    private VncDatabase db;
    private ConnectionBean connection;
    private boolean mVncReady = false;
    private int mX = 0, mY = 0;
    private float mScreenYaw = 0;
    private float mYaw = 0;
    private float mCamScreenSize = 5;
    private boolean mPrefCamera = false;

    /**
     * Checks if we've had an error inside of OpenGL ES, and if so what that error is.
     *
     * @param label Label to report in case of error.
     */
    private static void checkGLError(String label) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, label + ": glError " + error);
            throw new RuntimeException(label + ": glError " + error);
        }
    }

    /**
     * Converts a raw text file, saved as a resource, into an OpenGL ES shader.
     *
     * @param type  The type of shader we will be creating.
     * @param resId The resource ID of the raw text file about to be turned into a shader.
     * @return The shader object handler.
     */
    private int loadGLShader(int type, int resId) {
        String code = readRawTextFile(resId);
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        if (shader == 0) {
            throw new RuntimeException("Error creating shader.");
        }

        return shader;
    }

    /**
     * Sets the view to our CardboardView and initializes the transformation matrices we will use
     * to render our scene.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.common_ui);
        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRenderer(this);
        setCardboardView(cardboardView);

        mModelScreen = new float[16];
        mModelCamScreen = new float[16];
        mCamera = new float[16];
        mView = new float[16];
        mModelViewProjection = new float[16];
        mModelView = new float[16];
        mModelFloor = new float[16];
        mHeadView = new float[16];
        mForwardVector = new float[4];

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mOverlayView = (CardboardOverlayView) findViewById(R.id.overlay);
//        mOverlayView.show3DToast("Pull the magnet when you find an object.");

        Intent intent = getIntent();
        mDataSource = intent.getStringExtra("datasource");
        mDimensions = intent.getStringExtra("dimension");
        mProjection = intent.getStringExtra("projectionType");
        mScreenType = intent.getStringExtra("screenType");
        mVideo3DLayout = intent.getStringExtra("videoType");

        mExtraName = intent.getStringExtra("name");
        mExtraHost = intent.getStringExtra("host");
        mExtraUsername = intent.getStringExtra("username");
        mExtraPassword = intent.getStringExtra("password");
        mExtraColorMode = intent.getStringExtra("colormode");
        mExtraViewerMode = intent.getBooleanExtra("viewonly", false);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mScreenSize = Float.parseFloat(sharedPref.getString("pref_screenSize", "3"));
//        String prefScreenSize = sharedPref.getString("pref_screenSize", "Medium");
//        if (prefScreenSize.equals("Big")) mScreenSize = 4;
//        else if (prefScreenSize.equals("Small")) mScreenSize = 2;
//        else mScreenSize = 3;

    }

    @Override
    public void onRendererShutdown() {
        Log.i(TAG, "onRendererShutdown");
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.i(TAG, "onSurfaceChanged");
    }

    /**
     * Creates the buffers we use to store information about the 3D world.
     * <p/>
     * OpenGL doesn't use Java arrays, but rather needs data in a format it can understand.
     * Hence we use ByteBuffers.
     *
     * @param config The EGL configuration used when creating the surface.
     */
    @Override
    public void onSurfaceCreated(EGLConfig config) {
        Log.i(TAG, "onSurfaceCreated");
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // Dark background so text shows up well.
        int sub = 15;

        float[] u, tm, tl, tr;
        if (mScreenType.equals("Flat")) {
            u = WorldLayoutData.FACE_COORDS;
            tm = WorldLayoutData.FACE_TEXCOORDS_MONO;
            tl = WorldLayoutData.FACE_TEXCOORDS_LEFT;
            tr = WorldLayoutData.FACE_TEXCOORDS_RIGHT;
        } else {
            if (mProjection.equals("Square")) {
                tm = WorldLayoutData.GenerateUnitSphereTexCoords(sub, sub, Eye.Type.MONOCULAR, .0f);
                tl = WorldLayoutData.GenerateUnitSphereTexCoords(sub, sub, Eye.Type.LEFT, .0f);
                tr = WorldLayoutData.GenerateUnitSphereTexCoords(sub, sub, Eye.Type.RIGHT, .0f);
            } else {
                tm = WorldLayoutData.GenerateUnitSphereTexCoords2(sub, sub, Eye.Type.MONOCULAR);
                tl = WorldLayoutData.GenerateUnitSphereTexCoords2(sub, sub, Eye.Type.LEFT);
                tr = WorldLayoutData.GenerateUnitSphereTexCoords2(sub, sub, Eye.Type.RIGHT);
            }

            if (mScreenType.equals("Sphere")) {
                u = WorldLayoutData.GenerateUnitSphere(sub, sub, false);
            } else {
                u = WorldLayoutData.GenerateUnitSphere(sub, sub, true);
            }
        }

        ByteBuffer bbVertices = ByteBuffer.allocateDirect(u.length * 4);
        bbVertices.order(ByteOrder.nativeOrder());
        mScreenVertices = bbVertices.asFloatBuffer();
        mScreenVertices.put(u);
        mScreenVertices.position(0);

        ByteBuffer bbTexcoords = ByteBuffer.allocateDirect(tm.length * 4);
        bbTexcoords.order(ByteOrder.nativeOrder());
        mScreenTexcoords = bbTexcoords.asFloatBuffer();
        mScreenTexcoords.put(tm);
        mScreenTexcoords.position(0);

        ByteBuffer bbTexcoordsLeft = ByteBuffer.allocateDirect(tl.length * 4);
        bbTexcoordsLeft.order(ByteOrder.nativeOrder());
        mScreenTexcoordsLeft = bbTexcoordsLeft.asFloatBuffer();
        mScreenTexcoordsLeft.put(tl);
        mScreenTexcoordsLeft.position(0);

        ByteBuffer bbTexcoordsRight = ByteBuffer.allocateDirect(tr.length * 4);
        bbTexcoordsRight.order(ByteOrder.nativeOrder());
        mScreenTexcoordsRight = bbTexcoordsRight.asFloatBuffer();
        mScreenTexcoordsRight.put(tr);
        mScreenTexcoordsRight.position(0);

        // make a floor
        ByteBuffer bbFloorVertices = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_COORDS.length * 4);
        bbFloorVertices.order(ByteOrder.nativeOrder());
        mFloorVertices = bbFloorVertices.asFloatBuffer();
        mFloorVertices.put(WorldLayoutData.FLOOR_COORDS);
        mFloorVertices.position(0);

        ByteBuffer bbFloorNormals = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_NORMALS.length * 4);
        bbFloorNormals.order(ByteOrder.nativeOrder());
        mFloorNormals = bbFloorNormals.asFloatBuffer();
        mFloorNormals.put(WorldLayoutData.FLOOR_NORMALS);
        mFloorNormals.position(0);

        ByteBuffer bbFloorColors = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_COLORS.length * 4);
        bbFloorColors.order(ByteOrder.nativeOrder());
        mFloorColors = bbFloorColors.asFloatBuffer();
        mFloorColors.put(WorldLayoutData.FLOOR_COLORS);
        mFloorColors.position(0);

        int[] textureids = new int[2];
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glGenTextures(1, textureids, 0);
        mScreenTexture = textureids[0];
        mCameraTexture = textureids[1];
        //GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mScreenTexture);
        //GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        //GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);


//        player = new MediaPlayer();
//        mVideoSurface = new Surface(mVideoSurfaceTexture);
//        try {
//            player.setDataSource(mDataSource);
//            player.setSurface(mVideoSurface);
//            player.setLooping(true);
//            player.prepare();//async!
//            player.start();
//        } catch (IOException e) {
//            throw new RuntimeException("Could not open input video!");
//        }

        vncView = (VncView) findViewById(R.id.vncview);

        connection = new ConnectionBean();

        URL url;

        try {
            url = new URL("http://" + mExtraHost);

            connection.setAddress(url.getHost());
            int port = url.getPort();
            connection.setPort(port == -1 ? 5900 : port);
            connection.setUserName(mExtraUsername);
            connection.setPassword(mExtraPassword);
            String colorModel = COLORMODEL.C256.nameString();
            switch (mExtraColorMode) {
                case "24bit":
                    colorModel = COLORMODEL.C24bit.nameString();
                    break;
                case "256":
                    colorModel = COLORMODEL.C256.nameString();
                    break;
                case "64":
                    colorModel = COLORMODEL.C64.nameString();
                    break;
            }
            connection.setColorModel(colorModel);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        vncView.initializeVncCanvas(connection, new Runnable() {
            public void run() {
                setModes();
            }
        });
        vncView.setConnectionInfoCallback(this);

        mCameraSurfaceTexture = new SurfaceTexture(mCameraTexture);
        mCameraSurfaceTexture.setOnFrameAvailableListener(this);
        mCameraTextureTransform = new float[16];

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefCamera = sharedPref.getBoolean("pref_camera", false);

        if (mPrefCamera) {
            mHWCamera = Camera.open();

            try {
                mHWCamera.setPreviewTexture(mCameraSurfaceTexture);
            } catch (IOException t) {
            }

            mHWCamera.startPreview();
        } else {
            mHWCamera = null;
        }

        int vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.vertex);
        int vertexLightShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.light_vertex);
        int gridShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.grid_fragment);
        int textureShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.texture_fragment);

        mScreenProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mScreenProgram, vertexShader);
        GLES20.glAttachShader(mScreenProgram, textureShader);
        GLES20.glLinkProgram(mScreenProgram);
        GLES20.glUseProgram(mScreenProgram);

        checkGLError("Screen program");

        mScreenPositionA = GLES20.glGetAttribLocation(mScreenProgram, "a_Position");
        //mScreenNormalA = GLES20.glGetAttribLocation(mScreenProgram, "a_Normal");
        //mScreenColorA = GLES20.glGetAttribLocation(mScreenProgram, "a_Color");
        mScreenTextureA = GLES20.glGetAttribLocation(mScreenProgram, "a_TexCoordIn");
        mScreenTextureU = GLES20.glGetUniformLocation(mScreenProgram, "u_Texture");
        mScreenTextureTransformU = GLES20.glGetUniformLocation(mScreenProgram, "u_TexTransform");

        //mScreenModelA = GLES20.glGetUniformLocation(mScreenProgram, "u_Model");
        //mScreenModelViewA = GLES20.glGetUniformLocation(mScreenProgram, "u_MVMatrix");
        mScreenModelViewProjectionA = GLES20.glGetUniformLocation(mScreenProgram, "u_MVP");
        //mScreenLightPosA = GLES20.glGetUniformLocation(mScreenProgram, "u_LightPos");

        GLES20.glEnableVertexAttribArray(mScreenPositionA);
        //GLES20.glEnableVertexAttribArray(mScreenNormalA);
        //GLES20.glEnableVertexAttribArray(mScreenColorA);
        GLES20.glEnableVertexAttribArray(mScreenTextureA);

        checkGLError("Screen program params");

        mEnvProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mEnvProgram, vertexLightShader);
        GLES20.glAttachShader(mEnvProgram, gridShader);
        GLES20.glLinkProgram(mEnvProgram);
        GLES20.glUseProgram(mEnvProgram);

        checkGLError("Floor program");

        mFloorModelParam = GLES20.glGetUniformLocation(mEnvProgram, "u_Model");
        mFloorModelViewParam = GLES20.glGetUniformLocation(mEnvProgram, "u_MVMatrix");
        mFloorModelViewProjectionParam = GLES20.glGetUniformLocation(mEnvProgram, "u_MVP");
        mFloorLightPosParam = GLES20.glGetUniformLocation(mEnvProgram, "u_LightPos");

        mFloorPositionParam = GLES20.glGetAttribLocation(mEnvProgram, "a_Position");
        mFloorNormalParam = GLES20.glGetAttribLocation(mEnvProgram, "a_Normal");
        mFloorColorParam = GLES20.glGetAttribLocation(mEnvProgram, "a_Color");
        mFloorTextureParam = GLES20.glGetAttribLocation(mScreenProgram, "a_TexCoordIn");
        mFloorTextureUniformParam = GLES20.glGetUniformLocation(mScreenProgram, "u_Texture");

        GLES20.glEnableVertexAttribArray(mFloorPositionParam);
        GLES20.glEnableVertexAttribArray(mFloorNormalParam);
        GLES20.glEnableVertexAttribArray(mFloorColorParam);
        GLES20.glEnableVertexAttribArray(mFloorTextureParam);

        checkGLError("Floor program params");

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // Object first appears directly in front of user.
        Matrix.setIdentityM(mModelCamScreen, 0);
        if (mScreenType.equals("Flat") && mHWCamera != null) {
//            float ratio = (float)player.getVideoWidth()/(float)player.getVideoHeight();
            Camera.Size previewSize = mHWCamera.getParameters().getPreviewSize();
            float ratio = (float) previewSize.width / (float) previewSize.height;

            Matrix.scaleM(mModelCamScreen, 0, mScreenSize * ratio, mScreenSize, 1.0f);
            Matrix.translateM(mModelCamScreen, 0, 0, 0, -mScreenDistance);
        }
        //Matrix.scaleM(mModelCamScreen, 0, 5.0f, 5.0f, 1.0f);

        Matrix.setIdentityM(mModelFloor, 0);
        Matrix.translateM(mModelFloor, 0, 0, -mFloorDepth, 0); // Floor appears below user.

        checkGLError("onSurfaceCreated");
    }

    void setModes() {
        mVideoSurfaceTexture = new SurfaceTexture(mScreenTexture);
        mVideoSurfaceTexture.setOnFrameAvailableListener(this);
        mVideoSurfaceTexture.setDefaultBufferSize(vncView.getImageWidth(), vncView.getImageHeight());
        //mVideoSurfaceTexture.setDefaultBufferSize(1920,1080);
        System.out.println("Cardboard VNC: " + vncView.getImageWidth() + ", " + vncView.getImageHeight());

        mVideoSurface = new Surface(mVideoSurfaceTexture);
        mVideoTextureTransform = new float[16];
        mVideoSurfaceTexture.getTransformMatrix(mVideoTextureTransform);

        Matrix.setIdentityM(mModelScreen, 0);
        if (mScreenType.equals("Flat")) {
            //ratio = 1920f/1080f;//(float)player.getVideoWidth()/(float)player.getVideoHeight();
            float ratio = (float) vncView.getImageWidth() / (float) vncView.getImageHeight();

            Matrix.rotateM(mModelScreen, 0, (float) Math.toDegrees(mScreenYaw), 0, 1, 0);
            Matrix.scaleM(mModelScreen, 0, mScreenSize * ratio, mScreenSize, 1.0f);
            Matrix.translateM(mModelScreen, 0, 0, 0, -mScreenDistance);
        }

        vncView.setSurface(mVideoSurface);
        mVncReady = true;
    }

    /**
     * Converts a raw text file into a string.
     *
     * @param resId The resource ID of the raw text file about to be turned into a shader.
     * @return The context of the text file, or null in case of error.
     */
    private String readRawTextFile(int resId) {
        InputStream inputStream = getResources().openRawResource(resId);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Prepares OpenGL ES before we draw a frame.
     *
     * @param headTransform The head transformation in the new frame.
     */
    @Override
    public void onNewFrame(HeadTransform headTransform) {
//        if (!mVncReady)
//            return;


        // Build the Model part of the ModelView matrix.
        //Matrix.setIdentityM(mModelScreen, 0);
        //Matrix.scaleM(mModelScreen, 0, mScreenDistance, mScreenDistance, mScreenDistance);
        //Matrix.rotateM(mModelScreen, 0, TIME_DELTA, 0.5f, 0.5f, 1.0f);

        // Build the camera matrix and apply it to the ModelView.
        Matrix.setLookAtM(mCamera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

        headTransform.getHeadView(mHeadView, 0);
        headTransform.getForwardVector(mForwardVector, 0);
        float[] angles = new float[3];
        headTransform.getEulerAngles(angles, 0);
        mYaw = angles[1];

        //mScreenYaw = - (float) Math.atan2(mForwardVector[0], -mForwardVector[2]);

        //
        float[] pos = intersectsScreen();
        float[] p0 = {pos[0] * FloatMath.cos(-mScreenYaw) + pos[2] * FloatMath.sin(-mScreenYaw),
                pos[1],
                -pos[0] * FloatMath.sin(-mScreenYaw) + pos[2] * FloatMath.cos(-mScreenYaw),
                pos[3]};


        if (!mVncReady || mExtraViewerMode)
            return;

        float ratio = (float) vncView.getImageWidth() / (float) vncView.getImageHeight();

        mX = (int) ((p0[0] + mScreenSize * ratio) / (mScreenSize * ratio * 2) * vncView.getImageWidth());
        mY = (int) ((p0[1] * -1 + mScreenSize) / (mScreenSize * 2) * vncView.getImageHeight());
        if (mX >= 0 && mX < vncView.getImageWidth()) {
            if (mY >= 0 && mY < vncView.getImageHeight()) {
                vncView.processPointerEvent(mX, mY, MotionEvent.ACTION_MOVE, 0, false, false);
            }
        }
        checkGLError("onReadyToDraw");
    }

    /**
     * Draws a frame for an eye.
     *
     * @param eye The eye to render. Includes all required transformations.
     */
    @Override
    public void onDrawEye(Eye eye) {
        if (!mVncReady)
            return;


        //vncView.processPointerEvent(evt, false);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        checkGLError("mColorParam");

        // Apply the eye transformation to the camera.
        Matrix.multiplyMM(mView, 0, eye.getEyeView(), 0, mCamera, 0);

        // Apply drift compensation matrix
        Matrix.rotateM(mView, 0, (float) Math.toDegrees(mScreenYaw), 0, 1, 0);


        // Set the position of the light
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mView, 0, LIGHT_POS_IN_WORLD_SPACE, 0);

        // Build the ModelView and ModelViewProjection matrices
        // for calculating screen position and light.
        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
//        Matrix.multiplyMM(mModelView, 0, mView, 0, mModelScreen, 0);
        //      Matrix.multiplyMM(mModelView, 0, eye.getEyeView(), 0, mModelScreen, 0);

        Matrix.setIdentityM(mModelCamScreen, 0);

        float ratio;

        if (mHWCamera != null) {
            Camera.Size previewSize = mHWCamera.getParameters().getPreviewSize();
            ratio = (float) previewSize.width / (float) previewSize.height;

            Matrix.scaleM(mModelCamScreen, 0, mCamScreenSize * ratio, mCamScreenSize, 1.0f);
            Matrix.translateM(mModelCamScreen, 0, 0, 0, -mScreenDistance * 2);

            mModelView = mModelCamScreen;
            //Matrix.multiplyMM(mModelView, 0, mView, 0, mModelCamScreen, 0);
            Matrix.multiplyMM(mModelViewProjection, 0, perspective, 0, mModelView, 0);
            drawCameraPreview();
        }

        Matrix.setIdentityM(mModelScreen, 0);
        if (mScreenType.equals("Flat")) {
            //ratio = 1920f/1080f;//(float)player.getVideoWidth()/(float)player.getVideoHeight();
            ratio = (float) vncView.getImageWidth() / (float) vncView.getImageHeight();

            //Matrix.rotateM(mModelScreen, 0, -(float)Math.toDegrees(mScreenYaw), 0, 1, 0);
            Matrix.scaleM(mModelScreen, 0, mScreenSize * ratio, mScreenSize, 1.0f);
            Matrix.translateM(mModelScreen, 0, 0, 0, -mScreenDistance);
        }
        Matrix.multiplyMM(mModelView, 0, mView, 0, mModelScreen, 0);
        Matrix.multiplyMM(mModelViewProjection, 0, perspective, 0, mModelView, 0);
        drawScreen(eye.getType());

        // Set mModelView for the floor, so we draw floor in the correct location
        //Matrix.multiplyMM(mModelView, 0, mView, 0, mModelFloor, 0);
        //Matrix.multiplyMM(mModelViewProjection, 0, perspective, 0,
//            mModelView, 0);
        //drawFloor();
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
    }

    /**
     * Draw the screen.
     * <p/>
     * We've set all of our transformation matrices. Now we simply pass them into the shader.
     */

    // TODO rewrite to use glbindbuffer and offset instead of passing entire array
    void drawScreen(int type) {
        GLES20.glUseProgram(mScreenProgram);

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mScreenTexture);

        synchronized (this) {
            if (mVideoFrameAvailable) {
                mVideoSurfaceTexture.updateTexImage();
                mVideoSurfaceTexture.getTransformMatrix(mVideoTextureTransform);
                mVideoFrameAvailable = false;
            }
        }

        //GLES20.glUniform3fv(mScreenLightPosA, 1, mLightPosInEyeSpace, 0);

        GLES20.glUniformMatrix4fv(mScreenTextureTransformU, 1, false, mVideoTextureTransform, 0);

        // Set the Model in the shader, used to calculate lighting
        //GLES20.glUniformMatrix4fv(mScreenModelA, 1, false, mModelScreen, 0);

        // Set the ModelView in the shader, used to calculate lighting
        //GLES20.glUniformMatrix4fv(mScreenModelViewA, 1, false, mModelView, 0);

        // Set the position of the screen
        GLES20.glVertexAttribPointer(mScreenPositionA, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, 0, mScreenVertices);

        // Set the ModelViewProjection matrix in the shader.
        GLES20.glUniformMatrix4fv(mScreenModelViewProjectionA, 1, false, mModelViewProjection, 0);

        // Set the normal positions of the screen, again for shading
        //GLES20.glVertexAttribPointer(mScreenNormalA, 3, GLES20.GL_FLOAT, false, 0, mScreenNormals);
        //GLES20.glVertexAttribPointer(mScreenColorA, 4, GLES20.GL_FLOAT, false, 0, mScreenColors);

//        GLES20.glVertexAttribPointer(mScreenTextureA, 2, GLES20.GL_FLOAT, false, 0, onoords);
        if (mDimensions.equals("3d")) {
            if (type == Eye.Type.LEFT)
                GLES20.glVertexAttribPointer(mScreenTextureA, 2, GLES20.GL_FLOAT, false, 0, mScreenTexcoordsLeft);
            else
                GLES20.glVertexAttribPointer(mScreenTextureA, 2, GLES20.GL_FLOAT, false, 0, mScreenTexcoordsRight);
        } else {
            GLES20.glVertexAttribPointer(mScreenTextureA, 2, GLES20.GL_FLOAT, false, 0, mScreenTexcoords);
        }

        GLES20.glUniform1i(mScreenTextureU, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mScreenVertices.limit() / COORDS_PER_VERTEX);
        checkGLError("Drawing screen");
    }

    void drawCameraPreview() {
        GLES20.glUseProgram(mScreenProgram);

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mCameraTexture);

        synchronized (this) {
            if (mPreviewFrameAvailable) {
                mCameraSurfaceTexture.updateTexImage();
                mCameraSurfaceTexture.getTransformMatrix(mCameraTextureTransform);
                mPreviewFrameAvailable = false;
            }
        }

        GLES20.glUniformMatrix4fv(mScreenTextureTransformU, 1, false, mCameraTextureTransform, 0);

        // Set the position of the screen
        GLES20.glVertexAttribPointer(mScreenPositionA, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, 0, mScreenVertices);

        // Set the ModelViewProjection matrix in the shader.
        GLES20.glUniformMatrix4fv(mScreenModelViewProjectionA, 1, false, mModelViewProjection, 0);

        GLES20.glVertexAttribPointer(mScreenTextureA, 2, GLES20.GL_FLOAT, false, 0, mScreenTexcoords);

        GLES20.glUniform1i(mScreenTextureU, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mScreenVertices.limit() / COORDS_PER_VERTEX);
        checkGLError("Drawing camera preview");
    }

    /**
     * Draw the floor.
     * <p/>
     * This feeds in data for the floor into the shader. Note that this doesn't feed in data about
     * position of the light, so if we rewrite our code to draw the floor first, the lighting might
     * look strange.
     */
    public void drawFloor() {
        GLES20.glUseProgram(mEnvProgram);

        // Set ModelView, MVP, position, normals, and color.
        GLES20.glUniform3fv(mFloorLightPosParam, 1, mLightPosInEyeSpace, 0);
        GLES20.glUniformMatrix4fv(mFloorModelParam, 1, false, mModelFloor, 0);
        GLES20.glUniformMatrix4fv(mFloorModelViewParam, 1, false, mModelView, 0);
        GLES20.glUniformMatrix4fv(mFloorModelViewProjectionParam, 1, false,
                mModelViewProjection, 0);
        GLES20.glVertexAttribPointer(mFloorPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, 0, mFloorVertices);
        GLES20.glVertexAttribPointer(mFloorNormalParam, 3, GLES20.GL_FLOAT, false, 0,
                mFloorNormals);
        GLES20.glVertexAttribPointer(mFloorColorParam, 4, GLES20.GL_FLOAT, false, 0, mFloorColors);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

        checkGLError("drawing floor");
    }

    /**
     * Called when the Cardboard trigger is pulled.
     */
    @Override
    public void onCardboardTrigger() {
//        int seekpos = (int) (player.getCurrentPosition() + .1 * player.getDuration()) % player.getDuration();

//        player.seekTo(seekpos);

        if (mExtraViewerMode) {
            getCardboardView().resetHeadTracker();
        }
        else if (vncView != null) {
            if (mX >= 0 && mX < vncView.getImageWidth()) {
                if (mY >= 0 && mY < vncView.getImageHeight()) {
                    vncView.processPointerEvent(mX, mY, MotionEvent.ACTION_DOWN, 0, false, false);
                    vncView.processPointerEvent(mX, mY, MotionEvent.ACTION_UP, 0, false, false);
                }
            } else {
//                getCardboardView().onPause();
//                getCardboardView().onResume();
                getCardboardView().resetHeadTracker();
            }
//                float[] v = mForwardVector;
//                //v[2] = -v[2];
//                mScreenYaw = (float) Math.atan2(v[0], v[2]);
//                //mScreenYaw = - (float) Math.atan2(mForwardVector[0], -mForwardVector[2]);
//                float deg = (float)Math.toDegrees(mScreenYaw);
////                //Matrix.multiplyMM(mModelScreen, 0, mHeadView, 0, mModelScreen, 0);
////                Matrix.rotateM(mModelScreen, 0, deg, 0,1,0);
//                System.out.println("Compensating " + deg + " degrees around y");
//
//                float[] pos = intersectsScreen();
//                float[] p0 = { pos[0]*FloatMath.cos(-mScreenYaw)+pos[2]*FloatMath.sin(-mScreenYaw),
//                        pos[1],
//                        -pos[0]*FloatMath.sin(-mScreenYaw)+pos[2]*FloatMath.cos(-mScreenYaw),
//                        pos[3]};
//                float ratio = (float)vncView.getImageWidth()/(float)vncView.getImageHeight();
//
//                int x = (int) ((p0[0]+mScreenSize*ratio)/(mScreenSize*ratio*2) * vncView.getImageWidth());
//                int y = (int) ((p0[1]*-1+mScreenSize)/(mScreenSize*2) * vncView.getImageHeight());
//                System.out.println("v:   " + v[0] + ", " + v[1] + ", " + v[2] + ", " + v[3]);
//                System.out.println("pos: " + pos[0] + ", " + pos[1] + ", " + pos[2] + ", " + pos[3]);
//                System.out.println("p0:  " + p0[0] + ", " + p0[1] + ", " + p0[2] + ", " + p0[3]);
//                System.out.println("xy:  " + x + ", " + y);
//                System.out.println("yaw: " + mYaw);
            //}
        }

    }

    /**
     * Check if user is looking at object by calculating where the object is in eye-space.
     *
     * @return true if the user is looking at the object.
     */
    private boolean isLookingAtObject() {
        float[] initVec = {0, 0, 0, 1.0f};
        float[] objPositionVec = new float[4];

        // Convert object space to camera space. Use the headView from onNewFrame.
        Matrix.multiplyMM(mModelView, 0, mHeadView, 0, mModelScreen, 0);
        Matrix.multiplyMV(objPositionVec, 0, mModelView, 0, initVec, 0);

        float pitch = (float) Math.atan2(objPositionVec[1], -objPositionVec[2]);
        float yaw = (float) Math.atan2(objPositionVec[0], -objPositionVec[2]);

        return Math.abs(pitch) < PITCH_LIMIT && Math.abs(yaw) < YAW_LIMIT;
    }

    private float[] intersectsScreen() {
        float[] l = mForwardVector;//new float[4];
        l[0] = -l[0];
        l[1] = -l[1];
        float[] n = {0, 0, 1, 0};
        float[] p0 = {0, 0, -mScreenDistance, 0};
        float[] l0 = {0, 0, CAMERA_Z, 0};
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

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        synchronized (this) {
            if (surfaceTexture == mVideoSurfaceTexture) {
                //System.out.println("VNC frame available");
                mVideoFrameAvailable = true;
            } else
                mPreviewFrameAvailable = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (player != null) player.pause();
        if (mHWCamera != null) {
            mHWCamera.release();        // release the camera for other applications
            mHWCamera = null;
        }
        if (vncView != null) {
            vncView.closeConnection();
//            vncView.
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null) player.release();
        if (mHWCamera != null) {
            mHWCamera.release();        // release the camera for other applications
            mHWCamera = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (player != null) player.start();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mScreenSize = Float.parseFloat(sharedPref.getString("pref_screenSize", "3"));
//        String prefCamera = sharedPref.getString("pref_screenSize", "Medium");
//        if (prefCamera.equals("Big")) mScreenSize = 4;
//        else if (prefCamera.equals("Small")) mScreenSize = 2;
//        else mScreenSize = 3;

        if (mHWCamera == null && mPrefCamera) {
            mHWCamera = Camera.open();

            try {
                mHWCamera.setPreviewTexture(mCameraSurfaceTexture);
            } catch (IOException t) {
            }

            mHWCamera.startPreview();
        }
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent evt) {
//        if (keycode = KeyEvent.KEYCODE_BACK) {
//            return super.onKeyDown(keycode, evt);
//        } else {
//            return vncView.processLocalKeyEvent(keycode, evt);
//        }
        super.onKeyDown(keycode, evt);
        return vncView.processLocalKeyEvent(keycode, evt);
    }

    @Override
    public boolean onKeyUp(int keycode, KeyEvent evt) {
//        if (keycode = KeyEvent.KEYCODE_BACK) {
//            return super.onKeyDown(keycode, evt);
//        } else {
//            return vncView.processLocalKeyEvent(keycode, evt);
//        }
        super.onKeyUp(keycode, evt);
        return vncView.processLocalKeyEvent(keycode, evt);
    }

//    @Override
//    public boolean onTrackballEvent(MotionEvent evt) {
//        boolean trackballButtonDown = false;
//        switch (evt.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                trackballButtonDown = true;
//                break;
//            case MotionEvent.ACTION_UP:
//                trackballButtonDown = false;
//                break;
//        }
//        return vncView.processPointerEvent(evt, trackballButtonDown, false);
//    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP)
            onCardboardTrigger();
        return super.onTouchEvent(event);
    }

    @Override
    public void show(String message) {
        mOverlayView.show3DToast(message);
    }
}
