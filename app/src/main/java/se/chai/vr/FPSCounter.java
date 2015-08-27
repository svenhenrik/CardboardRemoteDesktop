package se.chai.vr;

import android.util.Log;

public class FPSCounter {
    static long startTime = System.nanoTime();
    static int frames = 0;

    public static void logFrame() {
        frames++;
        if(System.nanoTime() - startTime >= 1000000000) {
            Log.d("FPSCounter", "fps: " + frames);
            frames = 0;
            startTime = System.nanoTime();
        }
    }
}