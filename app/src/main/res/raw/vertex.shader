uniform mat4 u_MVP;
uniform mat4 u_TexTransform;

attribute vec4 a_Position;
attribute vec4 a_TexCoordIn;

varying vec4 v_Color;
varying vec2 v_TexCoordOut;

void main() {
   v_Color = vec4(1.0, 1.0, 1.0, 1.0);
   gl_Position = u_MVP * a_Position;
   v_TexCoordOut = a_TexCoordIn.xy;
//   v_TexCoordOut = (uTexTransform * a_TexCoordIn).xy;
}