#extension GL_OES_EGL_image_external : require
precision highp float;							// precision in the fragment shader.
uniform float u_ColorRatio;
varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;

vec3 rgb2hsv(vec3 c)
{
    vec4 k = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, k.wz), vec4(c.gb, k.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
    float d = q.x - min(q.w, q.y);
    float e = 0.00000001;
    float h = abs(q.z + (q.w - q.y)/(6.0*d + e));
    float s = d/(q.x + e);
    vec3 result = vec3(h, s, q.x);
    return result;
}

vec3 hsv2rgb(vec3 c)
{
    vec4 k = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + k.xyz) * 6.0 - k.www);
    vec3 result = mix(k.xxx, clamp(p - k.xxx, 0.0, 1.0), c.y) * c.z;
    return result;
}

// The entry point for our fragment shader.
void main()                    		
{
    vec4 originalColor = texture2D(sTexture, vTextureCoord);
    vec3 hsv = rgb2hsv(originalColor.rgb);
    hsv.r = hsv.r + u_ColorRatio;
    hsv.r = mod(hsv.r, 1.0);
    vec3 rgbColor = hsv2rgb(hsv);
	// Multiply the color by the diffuse illumination level and texture value to get final output color.
    gl_FragColor = vec4(rgbColor, originalColor.a); //(diffuse * );
  }                                                                     	

