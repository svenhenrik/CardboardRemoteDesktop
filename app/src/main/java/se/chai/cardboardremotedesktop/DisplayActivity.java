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

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;

import org.hitlabnz.sensor_fusion_demo.orientationProvider.CalibratedGyroscopeProvider;
import org.hitlabnz.sensor_fusion_demo.orientationProvider.ImprovedOrientationSensor2Provider;
import org.hitlabnz.sensor_fusion_demo.orientationProvider.OrientationProvider;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;

import se.chai.cardboardtools.CardboardOverlayView;
import se.chai.vr.ButtonThing;
import se.chai.vr.CameraScreen;
import se.chai.vr.Cursor;
import se.chai.vr.Engine;
import se.chai.vr.EnvironmentThing;
import se.chai.vr.FPSCounter;
import se.chai.vr.MyCardboardView;
import se.chai.vr.OnTriggerListener;
import se.chai.vr.OnVideoSizeChangeListener;
import se.chai.vr.StateButton;
import se.chai.vr.Thing;
import se.chai.vr.VNCScreen;

/**
 * A Cardboard sample application.
 */
public class DisplayActivity extends CardboardActivity implements CardboardView.StereoRenderer,
        OnTriggerListener, OnVideoSizeChangeListener {

    private static final int UI_POS_DOWN = 0;
    private static final int UI_POS_LEFT = 1;
    private static final int UI_POS_RIGHT = 2;
    private static final int UI_POS_UP = 3;

    private OrientationProvider orientationProvider;
    private String prefOrientationProviderString;
    private int firstOnDrawEye = 2;
    private float[] mEyeRightView;
    private float[] mEyeLeftView;

    private AudioManager audio;

    private Engine engine;
    private VNCScreen screen;
    private CameraScreen cameraPreview;

    private ButtonThing magnifyButton;
    private StateButton screenModeButton;
    private ButtonThing exitButton;

    // special
    private ButtonThing uiBackGround;
    private Cursor aimPoint;

    private EnvironmentThing bgEnv;

    ArrayList<ButtonThing> buttonList;

    private static final String TAG = "MainActivity";

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100.0f;

    private static final float CAMERA_Z = 0.01f;
    private static final float TIME_DELTA = 0.3f;

    private static final float YAW_LIMIT = 0.12f;
    private static final float PITCH_LIMIT = 0.12f;

    private static final WorldLayoutData DATA = new WorldLayoutData();

       //private float[] mModelScreen;
    private float[] mCamera;
    private float[] mView;
    private float[] mHeadViewSDK;
    private float[] mHeadViewUse;
    private float[] mModelViewProjection;
    private float[] mModelView;
    private float[] mOffsetView;

    private int mScore = 0;
    private float mObjectDistance = 12f;

    private Vibrator mVibrator;
    private CardboardOverlayView mOverlayView;


    private float mScreenDistance = 11.95f;
    private float mScreenSize = 8.5f;
    private float mScreenHeight = 0;//.25f;
    private float mScreenTexYoff = 0.0f;

    private long fuseStart = 0;
    private ButtonThing fuseButton;
    private int bgColor;

    float r, g, b, a;
    private float prefFuseLength;
    private AdLogic interstitialAdLogic;

    private Boolean prefShowEnv;
    private String prefShowEnvString;

    private float mScreenSizeMultiplier;
    private float mRatio;
    private String mExtraName;
    private String mExtraHost;
    private String mExtraUsername;
    private String mExtraPassword;
    private String mExtraColorMode;
    private boolean mExtraViewerMode;

    private boolean mCurvedScreen;
    private boolean mMagnify;
    private DisplayMetrics mMetrics;

    /**
     * Sets the view to our CardboardView and initializes the transformation matrices we will use
     * to render our scene.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        Intent intent = getIntent();
        mScreenSizeMultiplier = 1;//intent.getFloatExtra("videoSize", 1);

        mExtraName = intent.getStringExtra("name");
        mExtraHost = intent.getStringExtra("host");
        mExtraUsername = intent.getStringExtra("username");
        mExtraPassword = intent.getStringExtra("password");
        mExtraColorMode = intent.getStringExtra("colormode");
        mExtraViewerMode = intent.getBooleanExtra("viewonly", false);

        prefFuseLength = PreferenceManager.getDefaultSharedPreferences(this).getInt("pref_fuseTimeout", 1500);
        prefShowEnvString = PreferenceManager.getDefaultSharedPreferences(this).getString("pref_showEnv", "Home Theater");
        prefShowEnv = false;//!prefShowEnvString.equals("None");

        try {
            String c = PreferenceManager.getDefaultSharedPreferences(this).getString("pref_bgColor", "black");
            bgColor = Color.parseColor(c);
        } catch (IllegalArgumentException e) {
            bgColor = Color.parseColor("black");
        }
        mScreenDistance = 1;//11.95f;
        mScreenSize = 1f;

        mCurvedScreen = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_curvedScreen", true);
        mMagnify = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_magnify", true);

        r = Color.red(bgColor)/255f;
        g = Color.green(bgColor)/255f;
        b = Color.blue(bgColor)/255f;
        a = Color.alpha(bgColor)/255f;

        setContentView(R.layout.common_ui);
        MyCardboardView cardboardView = (MyCardboardView) findViewById(R.id.cardboard_view);

        cardboardView.setRestoreGLStateEnabled(false);
        cardboardView.setRenderer(this);
        setCardboardView(cardboardView);

        engine = new Engine(getResources());
//        engine.debug = true;

        aimPoint = new Cursor(engine);
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_UseFuse", true))
            fuseStart = -1;

        if (prefShowEnv) {
            bgEnv = new EnvironmentThing(engine);
        }


        buttonList = new ArrayList<>();
        uiBackGround = new ButtonThing(engine);
        uiBackGround.setName("bg");

        magnifyButton = new ButtonThing(engine);
        magnifyButton.setOnTriggerListener(this);
        screenModeButton = new StateButton(engine);
        screenModeButton.setOnTriggerListener(this);
        exitButton = new ButtonThing(engine);
        exitButton.setName("stop");
        exitButton.setOnTriggerListener(this);

        buttonList.add(exitButton);
        buttonList.add(magnifyButton);
        buttonList.add(screenModeButton);

        mCamera = new float[16];
        mView = new float[16];
        mModelViewProjection = new float[16];
        mModelView = new float[16];
        mHeadViewSDK = new float[16];

        mOffsetView = new float[16];
        Matrix.setIdentityM(mOffsetView, 0);

        prefOrientationProviderString = PreferenceManager.getDefaultSharedPreferences(this).getString("pref_orientationProvider", "0");
        if (prefOrientationProviderString.equals("1"))
            cardboardView.setUseManual(true);
        else {
            SensorManager sensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);

            if (prefOrientationProviderString.equals("2")) {
                orientationProvider = new ImprovedOrientationSensor2Provider(sensorManager);
                Matrix.rotateM(mOffsetView, 0, 90, 1, 0, 0);
            } else if (prefOrientationProviderString.equals("3")) {
                orientationProvider = new CalibratedGyroscopeProvider(sensorManager);
                Matrix.rotateM(mOffsetView, 0, 90, 0, 0, 1);
            }
        }
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mOverlayView = (CardboardOverlayView) findViewById(R.id.overlay);

        cameraPreview = new CameraScreen(engine, this);

        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);

        mMetrics = getResources().getDisplayMetrics();
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
     *
     * OpenGL doesn't use Java arrays, but rather needs data in a format it can understand.
     * Hence we use ByteBuffers.
     *
     * @param config The EGL configuration used when creating the surface.
     */
    @Override
    public void onSurfaceCreated(EGLConfig config) {
        Log.i(TAG, "onSurfaceCreated");

        screen = new VNCScreen(engine, this);

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_DisableDist", false)) {
            getCardboardView().setDistortionCorrectionEnabled(false);
        }

        screen.setOnVideoSizeChangeListener(this);
        screen.setCurveEnabled(mCurvedScreen);
        screen.setMagnifyEnabled(mMagnify);
        screen.setViewerMode(mExtraViewerMode);
        screen.initGeometry(ratioToDegrees(16f / 9));
        screen.setupPosition(mScreenSize, mScreenHeight, -mScreenDistance);
        if (!screen.initVnc(mExtraHost, mExtraUsername, mExtraPassword, mExtraColorMode, this)) {
            finish();
        }
        screen.setFixedModel();
        screen.setupShaders();

        cameraPreview.init();
        cameraPreview.setupShaders();
        float cameraSize = PreferenceManager.getDefaultSharedPreferences(this).getInt("pref_previewSize", 100) /100.0f;

        Matrix.setIdentityM(cameraPreview.model, 0);
        Matrix.translateM(cameraPreview.model, 0, 0, 0, -mScreenDistance * 2);
        Matrix.scaleM(cameraPreview.model, 0, cameraSize*cameraPreview.getRatio(), cameraSize, 1);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;   // No pre-scaling

        final Bitmap aimBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.whitecircle, options);
        aimPoint.init();
        aimPoint.addTexture(aimBitmap);
        aimPoint.setupShaders();
        Matrix.setIdentityM(aimPoint.model, 0);
        float aimSize = mScreenSize * .05f;
        aimPoint.scale(aimSize, aimSize, 1);
        aimPoint.translate(0, 0, -1.5f);
        aimPoint.setAlpha(0);

        if (prefShowEnv) {
//        final Bitmap envBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.home_texture, options);
//            bgEnv.init();
//        bgEnv.addTexture(envBitmap);
            bgEnv.setupShaders();
        }

        final Bitmap homeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.home, options);
        exitButton.init();
        exitButton.addTexture(homeBitmap);
        exitButton.setupShaders();

        magnifyButton.init();
        final Bitmap magnifyBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.zoom_in, options);
        magnifyButton.addTexture(magnifyBitmap);
        magnifyButton.setupShaders();

        screenModeButton.init();
        final Bitmap curveOnBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.curve_on, options);
        final Bitmap curveOffBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.curve_off, options);
        screenModeButton.addTexture(curveOffBitmap);
        screenModeButton.addTexture(curveOnBitmap);
        screenModeButton.setupShaders();
        if (mCurvedScreen) {
            screenModeButton.setInitState(0);
        } else {
            screenModeButton.setInitState(1);
        }

        setupUI(1.5f);

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);

        screen.pitchLimit = (float) Math.atan2(mScreenSize, mScreenDistance);
        screen.yawLimit = (float) Math.atan2(mScreenSize, mScreenDistance);

        Engine.checkGLError("onSurfaceCreated");

    }

    private int ratioToDegrees(float ratio) {
        return Math.min((int) Math.toDegrees(ratio * Math.PI / 3), 360);
    }

    private void setupUI(float ratio) {
        positionButton(exitButton, ratio, -1, 0, 1, 1, UI_POS_DOWN);

        positionButton(screenModeButton, ratio, 0, 0, 1, 1, UI_POS_DOWN);

        positionButton(magnifyButton, ratio, 1, 0, 1, 1, UI_POS_DOWN);
    }


    private void positionButton(ButtonThing button, float ratio, float x, float y, float xscale, float yscale, int pos) {
        float UI_DOWN, UI_UP, UI_LEFT, UI_RIGHT,
                UI_XSTEP, UI_YSTEP, UI_SCALE, UI_ZPOS, UI_XROT;

        UI_SCALE = .1f * mScreenSize;
        UI_XSTEP = UI_YSTEP = 2 * UI_SCALE + UI_SCALE/2; //0.25
        UI_DOWN = -(mScreenSize/2) - UI_XSTEP/2;
        UI_UP = - UI_DOWN - mScreenHeight*2;
        UI_LEFT = -(mScreenSize * mScreenSizeMultiplier) - UI_XSTEP/2;
        UI_RIGHT = -UI_LEFT;
        UI_ZPOS = -mScreenDistance;
        UI_XROT = 0;

        Matrix.setIdentityM(button.model, 0);

        float xpos = 0, ypos = 0;
        if (pos == UI_POS_DOWN) {
            xpos = x * UI_XSTEP;
            ypos = UI_DOWN - y * UI_YSTEP;
        } else if (pos == UI_POS_LEFT) {
            xpos = UI_LEFT - x * UI_XSTEP;
            ypos = y * UI_YSTEP;
        } else if (pos == UI_POS_RIGHT) {
            xpos = UI_RIGHT - x * UI_XSTEP;
            ypos = y * UI_YSTEP;
        } else if (pos == UI_POS_UP) {
            xpos = x * UI_XSTEP;
            ypos = UI_UP - y * UI_YSTEP;
        }

        button.yawLimit = 1;
        button.pitchLimit = 1;

        button.rotate(UI_XROT, 1, 0, 0);
        button.translate(xpos, ypos, UI_ZPOS);
        button.scale(UI_SCALE * xscale, UI_SCALE * yscale, 1);

        button.setFixedModel();
        moveThing(button);
    }

    /**
     * Prepares OpenGL ES before we draw a frame.
     *
     * @param headTransform The head transformation in the new frame.
     */
    @Override
    public void onNewFrame(final HeadTransform headTransform) {
        FPSCounter.logFrame();

        if (prefShowEnv && !bgEnv.isReady()) {
            if (prefShowEnvString.equals("Home Theater"))
                bgEnv.init(0);
            else
                bgEnv.init(1);

            bgEnv.setFixedModel();
            moveThing(bgEnv);
        }

        // Build the camera matrix and apply it to the ModelView.
        Matrix.setLookAtM(mCamera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        GLES20.glClearColor(r, g, b, a);

        headTransform.getHeadView(mHeadViewSDK, 0);

        mHeadViewUse = getHeadView();

        if (screen.isLookingAtObject(mHeadViewUse)) {
            aimPoint.setAlpha(0);
        } else {
            aimPoint.setAlpha(1);
        }

        for (ButtonThing button : buttonList) {
            if (button.isLookingAtObject(mHeadViewUse) && !button.isHidden) {
                if (fuseStart == -1) {
                    fuseStart = SystemClock.elapsedRealtime();
                    fuseButton = button;
                } else if (fuseButton == button){
                    checkFuse(SystemClock.elapsedRealtime());
                }
            } else if (fuseButton == button){
                checkFuse(0);
                fuseStart = -1;
                fuseButton = null;
            }
        }

        Engine.checkGLError("onReadyToDraw");
    }

    private void checkFuse(long time) {
        if (fuseStart != -1) {
            long d = time - fuseStart;
            if (fuseButton != null) {
                aimPoint.setFuse(d / prefFuseLength);
            }
            if (d > prefFuseLength) {
                fuseButton.onTrigger(mHeadViewUse);
                fuseStart = -1;
                fuseButton = null;
            }
        }
    }

    /**
     * Draws a frame for an eye.
     *
     * @param eye The eye to render. Includes all required transformations.
     */
    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Engine.checkGLError("mColorParam");
        if (!prefOrientationProviderString.equals("0")) {
            if (firstOnDrawEye > 0) {
                firstOnDrawEye--;
                float[] m = new float[16];
                Matrix.invertM(m, 0, mHeadViewSDK, 0);
                Matrix.multiplyMM(m, 0, eye.getEyeView(), 0, m, 0);

                if (eye.getType() == Eye.Type.RIGHT) {
                    mEyeRightView = m;
                } else if (eye.getType() == Eye.Type.LEFT) {
                    mEyeLeftView = m;
                }

                return;
            }

            // Apply the eye transformation to the camera.
            float[] m = new float[16];
            if (eye.getType() == Eye.Type.RIGHT) {
                Matrix.multiplyMM(m, 0, mHeadViewUse, 0, mEyeRightView, 0);
            } else {
                Matrix.multiplyMM(m, 0, mHeadViewUse, 0, mEyeLeftView, 0);
            }

            Matrix.multiplyMM(mView, 0, m, 0, mCamera, 0);
        } else {
            Matrix.multiplyMM(mView, 0, eye.getEyeView(), 0, mCamera, 0);
        }

        // Apply the eye transformation to the camera.

        // Build the ModelView and ModelViewProjection matrices
        // for calculating screen position and light.
        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);

        if (prefShowEnv && bgEnv.isReady()) {

            //Env first
            Matrix.multiplyMM(mModelView, 0, mView, 0, bgEnv.model, 0);
            Matrix.multiplyMM(mModelViewProjection, 0, perspective, 0, mModelView, 0);
            bgEnv.draw(eye.getType(), mModelViewProjection);

        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        Matrix.multiplyMM(mModelViewProjection, 0, perspective, 0, cameraPreview.model, 0);
        cameraPreview.draw(eye.getType(), mModelViewProjection);

        Matrix.multiplyMM(mModelView, 0, mView, 0, screen.model, 0);
        Matrix.multiplyMM(mModelViewProjection, 0, perspective, 0, mModelView, 0);
        screen.draw(eye.getType(), mModelViewProjection);

        for (ButtonThing button : buttonList) {
            Matrix.multiplyMM(mModelView, 0, mView, 0, button.model, 0);
            Matrix.multiplyMM(mModelViewProjection, 0, perspective, 0, mModelView, 0);
            button.draw(eye.getType(), mModelViewProjection);
        }

        Matrix.multiplyMM(mModelViewProjection, 0, perspective, 0, aimPoint.model, 0);
        aimPoint.draw(Eye.Type.MONOCULAR, mModelViewProjection);
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
    }

    /**
     * Called when the Cardboard trigger is pulled.
     */
    @Override
    public void onCardboardTrigger() {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_UseFuse", true))
            fuseStart = -1;

        boolean somethingPressed = false;
        for (ButtonThing button : buttonList) {
            if (button.onTrigger(mHeadViewUse))
                somethingPressed = true;
        }

        if (screen.onTrigger(mHeadViewUse)) {
            somethingPressed = true;
        }
            
        if (!somethingPressed) {
            Matrix.invertM(mOffsetView, 0, mView, 0);
            moveall();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        int lastpos = 0;
        if (screen != null) {
            screen.onPause();
        }
        if (cameraPreview != null) {
            cameraPreview.onPause();
        }
        if (orientationProvider != null) {
            orientationProvider.stop();
        }

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean("pref_curvedScreen", mCurvedScreen);
        editor.putBoolean("pref_magnify", mMagnify);
        editor.commit();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

   @Override
    public void onDestroy() {
       super.onDestroy();
   }

    @Override
    public void onResume() {
        super.onResume();

        if (orientationProvider != null) {
            orientationProvider.start();
        }
    }

    @Override
    public void onTrigger(ButtonThing thing) {

        if (thing == screenModeButton) {
            mCurvedScreen = !mCurvedScreen;
            screen.setCurveEnabled(mCurvedScreen);
            screen.initGeometry(ratioToDegrees(mRatio));
            screen.setupPosition(mScreenSize, mScreenHeight, -mScreenDistance);
            screen.setFixedModel();
            moveThing(screen);
        }

        if (thing == magnifyButton) {
            mMagnify = !mMagnify;
            screen.setMagnifyEnabled(mMagnify);
        }

        if (thing == exitButton) {
                finish();
        }
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent evt) {
        super.onKeyDown(keycode, evt);
        return screen.processKeyEvent(keycode, evt);
    }

    @Override
    public boolean onKeyUp(int keycode, KeyEvent evt) {
        super.onKeyUp(keycode, evt);
        return screen.processKeyEvent(keycode, evt);
    }


    @Override
    public void onVideoSizeChange(int w, int h) {
        mRatio = (float) w / (float) h;
        screen.initGeometry(ratioToDegrees(mRatio));
        screen.setupPosition(mScreenSize, mScreenHeight, -mScreenDistance);
        screen.setFixedModel();
        moveThing(screen);

        for (ButtonThing button  : buttonList) {
            button.show();
        }
    }

    private void moveall() {
        if (prefShowEnv) {
            moveThing(bgEnv);
        }
        moveThing(screen);
        for (ButtonThing button : buttonList) {
            moveThing(button);
        }

    }

    private void moveThing(Thing thing) {
        Matrix.multiplyMM(thing.model, 0, mOffsetView, 0, thing.getFixedModel(), 0);
    }

    public float[] getHeadView() {
        if (prefOrientationProviderString.equals("0")) {
            return mHeadViewSDK;
        } else if (prefOrientationProviderString.equals("1")) {
            return ((MyCardboardView)getCardboardView()).getRotMatrix();
        }

        //else
        float[] m = new float[16];
        SensorManager.remapCoordinateSystem(orientationProvider.getRotationMatrix().getMatrix(),
                SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, m);
        return m;
    }

}
