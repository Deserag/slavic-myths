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
    // Continuous low-contrast density; no threshold that hardens the pattern into straps.
    vec3 pattern = texture(Sampler0, texCoord0).rgb;
    float density = pattern.r * pattern.r;
    float alpha = density * vertexColor.a * ColorModulator.a * linear_fog_fade(vertexDistance, FogStart, FogEnd);
    if (alpha < 0.001) discard;
    // Thresholds follow the three per-stack strengths (24/40/58), independent of density.
    vec3 tint = vec3(0.30, 0.51, 0.38);
    if (vertexColor.a > 0.12) tint = mix(tint, vec3(0.48,0.62,0.53), pattern.g * 0.36);
    if (vertexColor.a > 0.19) tint = mix(tint, vec3(0.36,0.59,0.57), pattern.b * 0.32);
    fragColor = vec4(tint * ColorModulator.rgb, alpha);
}
