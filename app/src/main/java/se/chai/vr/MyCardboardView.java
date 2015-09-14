package se.chai.vr;

import android.content.Context;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.vrtoolkit.cardboard.CardboardView;

/**
 * Created by henrik on 15. 9. 1.
 */
public class MyCardboardView extends CardboardView {
    private float mPreviousX, mPreviousY, rotX, rotY;
    private float[] rotMatrix;
    private boolean useManual;

    public MyCardboardView(Context context) {
        super(context);
        init();
    }

    public MyCardboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        rotMatrix = new float[16];
        Matrix.setIdentityM(rotMatrix,0);
        mPreviousX = mPreviousY = 0;
        useManual = false;
    }

    public void setUseManual(boolean use) {
       useManual = use;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (!useManual)
            return super.onTouchEvent(e);

        float x = e.getX();
        float y = e.getY();
        float dx = (x - mPreviousX);
        float dy = (y - mPreviousY);
        mPreviousX = x;
        mPreviousY = y;

        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                rotX += dx / 4;
                rotY += dy / 4;

                Matrix.setIdentityM(rotMatrix, 0);
                Matrix.rotateM(rotMatrix, 0, rotY, 1, 0, 0);
                Matrix.rotateM(rotMatrix, 0, rotX, 0, 1, 0);
                break;
        }

        return true;
    }

    public float[] getRotMatrix() {
        return rotMatrix;
    }

    public void resetRot() {
        rotX = rotY = 0;
        Matrix.setIdentityM(rotMatrix, 0);
    }
}
