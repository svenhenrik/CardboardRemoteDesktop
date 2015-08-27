package se.chai.vr;

import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import se.chai.cardboardremotedesktop.R;
import se.chai.cardboardremotedesktop.WorldLayoutData;

/**
 * Created by henrik on 15. 4. 24.
 */
public class Cursor extends TexturedThing  {

    protected float alpha;
    protected int fuseU;
    private float fuseValue;

    public Cursor(Engine engine) {
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

        alpha = 0.1f;

        Matrix.setIdentityM(model, 0);

//        pitchLimit = yawLimit = (float) Math.atan2(.7, 10);
//        pitchLimit = yawLimit = .05f;

        return true;
    }

    @Override
    public void setupShaders() {
        vertexShader = engine.loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.vertex);
        textureShader = engine.loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.fuse_fragment);

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

        //mScreenModelA = GLES20.glGetUniformLocation(screen.program, "u_Model");
        //mScreenModelViewA = GLES20.glGetUniformLocation(screen.program, "u_MVMatrix");
        modelViewProjectionA = GLES20.glGetUniformLocation(program, "u_MVP");
        //mScreenLightPosA = GLES20.glGetUniformLocation(screen.program, "u_LightPos");

        alphaU = GLES20.glGetUniformLocation(program, "u_Alpha");
        fuseU = GLES20.glGetUniformLocation(program, "u_Fuse");

        GLES20.glEnableVertexAttribArray(positionA);
        //GLES20.glEnableVertexAttribArray(mScreenNormalA);
        //GLES20.glEnableVertexAttribArray(mScreenColorA);
        GLES20.glEnableVertexAttribArray(textureA);

        Engine.checkGLError("Screen program params");
    }

    @Override
    public void draw(int eye, float[] modelViewProjection) {
        if (isHidden)
            return;

        GLES20.glUseProgram(program);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);

        // Set the position of the screen
        GLES20.glVertexAttribPointer(positionA, Thing.COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, 0, vertices);

        // Set the ModelViewProjection matrix in the shader.
        GLES20.glUniformMatrix4fv(modelViewProjectionA, 1, false, modelViewProjection, 0);
        GLES20.glUniform1f(alphaU, alpha);
        GLES20.glUniform1f(fuseU, fuseValue);

        GLES20.glVertexAttribPointer(textureA, 2, GLES20.GL_FLOAT, false, 0, texCords);
        GLES20.glUniform1i(textureU, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.limit()/ Thing.COORDS_PER_VERTEX);
    }


    public void setFuse(float v) {
        fuseValue = v;
    }

    public void setAlpha(float v) {
        alpha = v;
    }
}
