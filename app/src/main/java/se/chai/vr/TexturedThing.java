package se.chai.vr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.FloatBuffer;

import se.chai.cardboardremotedesktop.R;

/**
 * Created by henrik on 15. 4. 2.
 */
public class TexturedThing extends Thing {
    protected int vertexShader;
    protected int textureShader;
    protected int texture;
    protected int textureA;
    protected int textureU;
    protected int textureTransformU;
    protected int alphaU;

    protected float alpha;
    private boolean alphaSet;

    protected FloatBuffer texCords;

    protected final Bitmap debugBitmap;


    public TexturedThing(Engine engine) {
        super(engine);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;   // No pre-scaling
        debugBitmap = BitmapFactory.decodeResource(engine.resources, R.drawable.whitesquare, options);;
    }


    public void setAlpha(float a) {
        this.alpha = a;
        alphaSet = true;
    }

    public void setupShaders() {
        vertexShader = engine.loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.vertex);
        textureShader = engine.loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.texture_fragment);

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

        GLES20.glVertexAttribPointer(textureA, 2, GLES20.GL_FLOAT, false, 0, texCords);
        GLES20.glUniform1i(textureU, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.limit()/ Thing.COORDS_PER_VERTEX);
    }

//    /@Override
    public void olddraw(int eye, float[] modelViewProjection) {
        GLES20.glUseProgram(program);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);


        //GLES20.glUniform3fv(mScreenLightPosA, 1, mLightPosInEyeSpace, 0);

        // Set the Model in the shader, used to calculate lighting
        //GLES20.glUniformMatrix4fv(mScreenModelA, 1, false, screen.model, 0);

        // Set the ModelView in the shader, used to calculate lighting
        //GLES20.glUniformMatrix4fv(mScreenModelViewA, 1, false, mModelView, 0);

        // Set the position of the screen
        GLES20.glVertexAttribPointer(positionA, Thing.COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, 0, vertices);

        // Set the ModelViewProjection matrix in the shader.
        GLES20.glUniformMatrix4fv(modelViewProjectionA, 1, false, modelViewProjection, 0);

        // Set the normal positions of the screen, again for shading
        //GLES20.glVertexAttribPointer(mScreenNormalA, 3, GLES20.GL_FLOAT, false, 0, mScreenNormals);
        //GLES20.glVertexAttribPointer(mScreenColorA, 4, GLES20.GL_FLOAT, false, 0, mScreenColors);

//        GLES20.glVertexAttribPointer(textureA, 2, GLES20.GL_FLOAT, false, 0, onoords);

        GLES20.glVertexAttribPointer(textureA, 2, GLES20.GL_FLOAT, false, 0, texCords);


        GLES20.glUniform1i(textureU, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.limit() / Thing.COORDS_PER_VERTEX);
        //DisplayActivity.checkGLError("Drawing screen");
    }


    public void addTexture(Bitmap bitmap) {
        // Bind to the texture in OpenGL
        texture = engine.getTextureId();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);

        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        // Load the bitmap into the bound texture.
        if (engine.debug)
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, debugBitmap, 0);
        else
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        // Recycle the bitmap, since its data has been loaded into OpenGL.
        bitmap.recycle();
    }
}
