#version 330 core

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>


in vec4 vertexColor;
out vec4 FragColor;

void main() {
    FragColor = vertexColor;
}