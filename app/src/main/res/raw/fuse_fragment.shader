precision mediump float;

varying lowp vec2 v_TexCoordOut;
uniform sampler2D u_Texture;
uniform float u_Alpha;
uniform float u_Fuse;

void main() {
    float PI=3.141592;

    vec2 tex = 2.0 * v_TexCoordOut - vec2(0.5, 0.5);
    vec2 pos = 2.0 * v_TexCoordOut - vec2(1.0, 1.0);
    float angle = (atan(pos.y, pos.x) + PI) / (2.0*PI);
    float dist = sqrt(dot(pos, pos));
    float border = 0.1;

    float x = 1.0 - step(u_Fuse, angle) - step(1.0, dist) - (1.0 - step(0.8, dist)) + 2.0 * (1.0 - step(0.2, dist));
    x = clamp(x, 0.0, 1.0);
    gl_FragColor = vec4(x, x, x, x * u_Alpha);// + u_Alpha * texture2D(u_Texture, tex);

    //gl_FragColor = texture2D(u_Texture, tex);
}