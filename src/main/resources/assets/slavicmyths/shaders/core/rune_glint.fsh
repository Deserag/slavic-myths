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
    float density = pattern.r;
    float alpha = density * vertexColor.a * ColorModulator.a * linear_fog_fade(vertexDistance, FogStart, FogEnd);
    if (alpha < 0.001) discard;
    // Thresholds follow the three per-stack strengths (64/100/140), independent of density.
    vec3 tint = vec3(0.36, 0.64, 0.46);
    if (vertexColor.a > 0.32) tint = mix(tint, vec3(0.53,0.73,0.61), pattern.g * 0.36);
    if (vertexColor.a > 0.47) tint = mix(tint, vec3(0.40,0.70,0.68), pattern.b * 0.32);
    fragColor = vec4(tint * ColorModulator.rgb, alpha);
}
