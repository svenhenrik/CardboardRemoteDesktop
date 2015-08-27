package se.chai.vr;

import android.androidVNC.COLORMODEL;
import android.androidVNC.ConnectionBean;
import android.androidVNC.VncView;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.preference.PreferenceManager;
import android.view.Surface;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import se.chai.cardboardremotedesktop.R;
import se.chai.cardboardremotedesktop.WorldLayoutData;

/**
 * Created by Henrik on 2015-06-30.
 */
public class CameraScreen extends TexturedThing implements SurfaceTexture.OnFrameAvailableListener {
    private boolean mVideoFrameAvailable = false;
    private SurfaceTexture mVideoSurfaceTexture;
    private Surface mVideoSurface;
    private float[] mVideoTextureTransform;

    private Camera mHWCamera;

    private float size, distance, height, ratio;
    private boolean mPrefCamera;

    private int mouseU;
    private int aspectU;

    private Context context;

    public CameraScreen(Engine engine, Context context) {
        super(engine);
        this.context = context;
    }

    @Override
    public void setupShaders() {
        vertexShader = engine.loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.vertex);
        textureShader = engine.loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.texture_external_fragment);

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, textureShader);
        GLES20.glLinkProgram(program);
        GLES20.glUseProgram(program);

        positionA = GLES20.glGetAttribLocation(program, "a_Position");
        //mScreenNormalA = GLES20.glGetAttribLocation(screen.program, "a_Normal");
        //mScreenColorA = GLES20.glGetAttribLocation(screen.program, "a_Color");
        textureA = GLES20.glGetAttribLocation(program, "a_TexCoordIn");
        textureU = GLES20.glGetUniformLocation(program, "u_Texture");
        textureTransformU = GLES20.glGetUniformLocation(program, "u_TexTransform");

        mouseU = GLES20.glGetUniformLocation(program, "u_Mouse");
        aspectU = GLES20.glGetUniformLocation(program, "u_Aspect");

        //mScreenModelA = GLES20.glGetUniformLocation(screen.program, "u_Model");
        //mScreenModelViewA = GLES20.glGetUniformLocation(screen.program, "u_MVMatrix");
        modelViewProjectionA = GLES20.glGetUniformLocation(program, "u_MVP");
        //mScreenLightPosA = GLES20.glGetUniformLocation(screen.program, "u_LightPos");

        GLES20.glEnableVertexAttribArray(positionA);
        //GLES20.glEnableVertexAttribArray(mScreenNormalA);
        //GLES20.glEnableVertexAttribArray(mScreenColorA);
        GLES20.glEnableVertexAttribArray(textureA);

        Engine.checkGLError("Screen program params");
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

        texture = engine.getTextureId();
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture);

        mVideoSurfaceTexture = new SurfaceTexture(texture);
        mVideoSurfaceTexture.setOnFrameAvailableListener(this);
        mVideoTextureTransform = new float[16];

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        mPrefCamera = sharedPref.getBoolean("pref_camera", false);

        if (mPrefCamera) {
            mHWCamera = Camera.open();

            try {
                mHWCamera.setPreviewTexture(mVideoSurfaceTexture);
            } catch (IOException t) {
            }

            mHWCamera.startPreview();
            Camera.Size previewSize = mHWCamera.getParameters().getPreviewSize();
            ratio = (float) previewSize.width / (float) previewSize.height;
        } else {
            mHWCamera = null;
            ratio = 1;
        }


        return true;
    }

    public void setupPosition(float size, float height, float distance) {
        setHeight(height);
        setDistance(distance);
        setSize(size);
    }

    public void setSize(float size) {
        this.size = size;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getRatio() {
        return ratio;
    }

    @Override
    public void draw(int eye, float[] modelViewProjection) {
        if (!mPrefCamera)
            return;

        GLES20.glUseProgram(program);

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture);

        synchronized (this) {
            if (mVideoFrameAvailable) {
                mVideoSurfaceTexture.updateTexImage();
                mVideoSurfaceTexture.getTransformMatrix(mVideoTextureTransform);
                mVideoFrameAvailable = false;
            }
        }

        GLES20.glUniformMatrix4fv(textureTransformU, 1, false, mVideoTextureTransform, 0);

        GLES20.glVertexAttribPointer(positionA, Thing.COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, 0, vertices);

        GLES20.glUniformMatrix4fv(modelViewProjectionA, 1, false, modelViewProjection, 0);

        GLES20.glVertexAttribPointer(textureA, 2, GLES20.GL_FLOAT, false, 0, texCords);

        GLES20.glUniform1i(textureU, 0);
        GLES20.glUniform1f(aspectU, 1);
        GLES20.glUniform2f(mouseU, -1, -1);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.limit()/ Thing.COORDS_PER_VERTEX);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        synchronized (this) {
            mVideoFrameAvailable = true;
        }
    }

    public void onPause() {
        if (mHWCamera != null) {
            mHWCamera.release();
            mHWCamera = null;
        }
    }
}
