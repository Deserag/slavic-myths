#version 150
#moj_import <fog.glsl>
uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
in vec4 vertexColor;
in vec2 texCoord0;
in float vertexDistance;
out vec4 fragColor;
void main() {
    // One shared data texture: red=intensity, green=mint mask, blue=turquoise mask.
    vec3 pattern = texture(Sampler0, texCoord0).rgb;
    float alpha = pattern.r * vertexColor.a * ColorModulator.a * linear_fog_fade(vertexDistance, FogStart, FogEnd);
    if (alpha < 0.004) discard;
    vec3 tint = vertexColor.rgb;
    if (vertexColor.a > 0.18) tint = mix(tint, vec3(0.6392,0.9020,0.7412), pattern.g * 0.72);
    if (vertexColor.a > 0.28) tint = mix(tint, vec3(0.3451,0.8118,0.7765), pattern.b * 0.58);
    fragColor = vec4(tint * ColorModulator.rgb, alpha);
}
