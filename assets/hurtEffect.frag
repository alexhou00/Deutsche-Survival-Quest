#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoord;

uniform sampler2D u_texture;
uniform float isHurt; // 1.0 = hurt, 0.0 = normal

void main() {
    vec4 texColor = texture2D(u_texture, v_texCoord);

    if (texColor.a > 0.0) {
        // Only apply to non-transparent pixels
        if (isHurt > 0.0) {
            // Apply red tint and keep alpha as original
            gl_FragColor = mix(texColor, vec4(1.0, 0.0, 0.0, texColor.a), 0.5);
        } else {
            gl_FragColor = texColor;
        }
    } else {
        // Keep fully transparent pixels as is
        gl_FragColor = texColor;
    }
}
