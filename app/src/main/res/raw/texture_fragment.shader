#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec4 v_Color;

varying lowp vec2 v_TexCoordOut;
uniform samplerExternalOES u_Texture;

void main() {
    gl_FragColor = texture2D(u_Texture, v_TexCoordOut);
    //gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
}
