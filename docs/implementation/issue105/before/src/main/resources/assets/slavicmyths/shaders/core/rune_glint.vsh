#version 150
#moj_import <fog.glsl>
in vec3 Position;
in vec4 Color;
in vec2 UV0;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float GameTime;
uniform int FogShape;
out vec4 vertexColor;
out vec2 texCoord0;
out float vertexDistance;
void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color;
    vertexDistance = fog_distance(Position, FogShape);
    // UV0 is computed in model units by the overlay consumer, independent of atlas and GUI scale.
    texCoord0 = UV0 * 2.6 + vec2(GameTime * 12.0, GameTime * 7.0);
}
