precision mediump float;
varying vec4 v_Color;

varying lowp vec2 v_TexCoordOut;
uniform sampler2D u_Texture;
uniform float u_Alpha;
uniform float u_Fuse;

void main() {
    gl_FragColor = u_Alpha * texture2D(u_Texture, v_TexCoordOut);
    //gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
}
