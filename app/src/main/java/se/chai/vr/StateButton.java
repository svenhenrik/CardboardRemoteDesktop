package se.chai.vr;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.util.ArrayList;

/**
 * Created by henrik on 15. 4. 14.
 */
public class StateButton extends ButtonThing {

    ArrayList<Integer> textures;
    int state = 0;

    public StateButton(Engine engine) {
        super(engine);
        textures = new ArrayList<>();
    }

    @Override
    public void addTexture(Bitmap bitmap) {
        int id = engine.getTextureId();
        textures.add(id);
        texture = textures.get(textures.size() - 1);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);

        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        // Load the bitmap into the bound texture.
        if (engine.debug)
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, debugBitmap, 0);
        else
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        // Recycle the bitmap, since its data has been loaded into OpenGL.
        bitmap.recycle();

        texture = textures.get(state);
    }

    public int nextState() {
        if (++state == textures.size()) {
            state = 0;
        }
        texture = textures.get(state);

        return state;
    }

    public int getState() { return state; }

    public void setInitState(int initstate) {
        state = initstate;
        texture = textures.get(state);
    }

    @Override
    public boolean onTrigger(float[] headView) {
        if (!isHidden && isLookingAtObject(headView)) {
            nextState();
            if (onTriggerListener != null) {
                onTriggerListener.onTrigger(this);
            }
            return true;
        }

        return false;
    }
}
