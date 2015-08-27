package se.chai.vr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import se.chai.vr.TexturedThing;
import se.chai.vr.objparse.ObjData;
import se.chai.vr.objparse.ObjParser;
import se.chai.cardboardremotedesktop.R;
import se.chai.cardboardremotedesktop.WorldLayoutData;

/**
 * Created by henrik on 15. 5. 8.
 */

public class EnvironmentThing extends TexturedThing {

    public boolean isReady = false;

    public EnvironmentThing(Engine engine) {
        super(engine);
    }

    public boolean init(int i) {
        if (WorldLayoutData.envLoaded != null) {
            if (WorldLayoutData.envLoaded.get(i)) {
                vertices = WorldLayoutData.envVerts.get(i);
                texCords = WorldLayoutData.envTexCords.get(i);
                int resId = WorldLayoutData.envTexResId.get(i);

                Matrix.setIdentityM(model, 0);

                alpha = 1;

                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false;   // No pre-scaling
                final Bitmap envBitmap = BitmapFactory.decodeResource(engine.resources, resId, options);
                addTexture(envBitmap);

                isReady = true;
                return true;
            }
        }

        return false;
    }
@Override
    public void draw(int eye, float[] modelViewProjection) {
        if (isHidden || !isReady)
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

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.limit() / Thing.COORDS_PER_VERTEX);
        //GLES20.glDrawElements(GLES20.GL_TRIANGLES, faceCount, GLES20.GL_UNSIGNED_SHORT, index);
    }

    public boolean isReady() {
        return isReady;
    }
}
