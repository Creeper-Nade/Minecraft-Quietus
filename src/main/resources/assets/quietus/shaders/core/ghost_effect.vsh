#version 150 core

in vec3 Position;
in vec2 UV0;

out vec2 texCoord;

void main() {
    // Directly use position since we're in NDC
    gl_Position = vec4(Position, 1.0);
    texCoord = UV0;
}