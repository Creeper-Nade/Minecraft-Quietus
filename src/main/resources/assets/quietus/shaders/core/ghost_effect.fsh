#version 150 core
in vec2 texCoord;
out vec4 fragColor;

uniform sampler2D DiffuseSampler;
uniform vec2 ScreenSize;
uniform float VignetteIntensity;
uniform float Time;
uniform float FadeFactor;

// Large-scale noise function
float largeNoise(vec2 uv) {
    // Scale down coordinates for larger noise patterns
    vec2 scaledUV = uv * 0.2;

    // Create multiple octaves of noise
    float noise = fract(sin(dot(scaledUV, vec2(12.9898, 78.233) + Time) * 43758.5453));
    noise += fract(sin(dot(scaledUV, vec2(38.5453, 12.9898) + Time * 0.7) * 29873.387));
    noise += fract(sin(dot(scaledUV, vec2(78.233, 38.5453) + Time * 1.3) * 38745.673));

    // Average and scale
    return noise * 0.33;
}

// Minimal distortion function
vec2 minimalDistortion(vec2 uv) {
    // Calculate center-based distortion
    vec2 centerVec = uv - vec2(0.5);
    float dist = length(centerVec);

    // Create very subtle ripple effect
    float ripple = sin(dist * 25.0 - Time * 2.5) * 0.0005;

    // Apply ripple only in the middle of the screen
    float centerFactor = 1.0 - smoothstep(0.3, 0.5, dist);
    ripple *= centerFactor * FadeFactor;

    return uv + centerVec * ripple;
}

void main() {
    // Apply minimal distortion
    vec2 distortedUV = minimalDistortion(texCoord);

    // Sample texture with distortion
    vec4 color = texture(DiffuseSampler, distortedUV);

    // Convert to grayscale
    float luminance = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    vec3 bwColor = vec3(luminance);

    // Add large-scale TV noise
    float noise = largeNoise(texCoord);
    float noisePattern = fract(noise * 10.0);
    bwColor = clamp(bwColor * (0.8 + noisePattern * 0.4), 0.0, 1.0);

    // Add scanlines
    float scanline = sin(texCoord.y * ScreenSize.y * 0.8 + Time * 5.0) * 0.1;
    bwColor += scanline * 0.1;

    // Apply deep black vignette
    vec2 uv = texCoord * 2.0 - 1.0;
    float dist = length(uv);
    float vignette = 1.0 - smoothstep(0.7, 1.0, dist * VignetteIntensity);

    // Apply fade factor to all effects
    vec3 finalColor = mix(color.rgb, bwColor, FadeFactor);
    finalColor *= vignette;  // Apply vignette last to maintain deep blacks

    fragColor = vec4(finalColor, color.a);
}