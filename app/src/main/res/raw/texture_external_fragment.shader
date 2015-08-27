#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec4 v_Color;

varying lowp vec2 v_TexCoordOut;
uniform samplerExternalOES u_Texture;
uniform vec2 u_Mouse;
uniform float u_Aspect;

void main() {
   	vec2 uv = v_TexCoordOut;
    //vec2 mouseUV = iMouse.xy/iResolution.xy;
    //float aspect = iResolution.x/iResolution.y;
    float size = 0.2;
    float border = 0.0;
    float magf = 1.5;

    vec2 dist = uv - u_Mouse;
    dist.x = dist.x * u_Aspect;
    float d = length(dist);
    float mag = step(size,d); // 0 - 1
    //vec2 nuv = uv / (2.0 - mag) + u_Mouse * (1.0 - mag) / 2.0;
    //vec2 nuv = uv / (magf - (mag * (magf - 1.0))) + u_Mouse * (1.0 - mag) / magf ;
    vec2 nuv = uv / (1.0 + (magf-1.0) * (1.0 - mag)) + u_Mouse * (1.0 - mag) * (1.0 - 1.0/magf);
	vec4 c = clamp(texture2D(u_Texture, nuv) + step(size,d) - step(size + border,d), 0.0, 1.0);

    gl_FragColor = c;
    //gl_FragColor = texture2D(u_Texture, v_TexCoordOut);
    //gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
}
