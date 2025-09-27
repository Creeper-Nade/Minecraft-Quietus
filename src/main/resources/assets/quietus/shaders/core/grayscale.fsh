#version 150

#moj_import <minecraft:fog.glsl>
uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec4 lightMapColor;
in vec4 overlayColor;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    // Convert to grayscale using luminance formula
    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    //fragColor = vec4(gray, gray, gray, color.a);
    // Set semi-transparent alpha (50%)
    float alpha = color.a * 0.5;
    color=vec4(gray, gray, gray, alpha);
    #ifdef ALPHA_CUTOUT
    if (color.a < ALPHA_CUTOUT) {
        discard;
    }
    #endif
    color *= vertexColor * ColorModulator;
    #ifndef NO_OVERLAY
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    #endif
    #ifndef EMISSIVE
    color *= lightMapColor;
    #endif


    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);

    // Discard fully transparent fragments to fix outline issue


}