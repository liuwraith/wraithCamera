precision highp float;       	// Set the default precision to medium. We don't need as high of a
								// precision in the fragment shader.
varying highp vec2 vTextureCoord;
uniform sampler2D sTexture;
const highp float EPSILON = 0.001;

vec3 rgb2hsv(highp vec3 c)
{
    highp vec4 k = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    highp vec4 p = mix(vec4(c.bg, k.wz), vec4(c.gb, k.xy), step(c.b, c.g));
    highp vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
    highp float d = q.x - min(q.w, q.y);
    highp float e = 0.00000001;
    highp float h = abs(q.z + (q.w - q.y)/(6.0*d + e));
    highp float s = d/(q.x + e);
    highp vec3 result = vec3(h, s, q.x);
    return result;
}
vec3 hsv2rgb(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

vec3 adjustSaturation(highp vec3 originalColor)
{
    highp float max = max(max(originalColor.r, originalColor.g), originalColor.b);
    highp float min = min(min(originalColor.r, originalColor.g), originalColor.b);
    highp float delta = (max - min);
    
    highp float addSum = (max + min);
    highp float l = addSum / 2.0;
    highp vec3 result;
    
    highp float ratio = 0.8;
    
    result = vec3(l, l, l) + (originalColor.rgb - vec3(l, l, l)) *
    vec3(ratio, ratio, ratio);
    return result;
}
void main()
{
    highp vec4 result = texture2D(sTexture, vTextureCoord);
    highp vec3 resultHsv = rgb2hsv(result.rgb);
    highp float newH = (-0.4 * resultHsv.b * resultHsv.b + 0.4 * resultHsv.b) * exp(-1.0/ resultHsv.b) + resultHsv.b;
    result.rgb = clamp(hsv2rgb(vec3(resultHsv.r, resultHsv.g, newH)), 0.001, 0.999);
    result.rgb = result.rgb * vec3(1.04, 1.04, 1.04);
    result.rgb = vec3(0.4, 0.4, 0.4) + (result.rgb  - vec3(0.4, 0.4, 0.4)) * 1.1;
    result.rgb = clamp(adjustSaturation(result.rgb), 0.001, 0.999);
    if(resultHsv.r > 0.01 && resultHsv.r <= 50.0 && resultHsv.g >= 0.1 && resultHsv.g < 0.63 && resultHsv.b > 0.015){
        result.rgb = result.rgb * vec3(1.0, 1.015, 1.015);
    }
    result = clamp(result, EPSILON, 1.0 - EPSILON);
    gl_FragColor = vec4(result.rgb, 1.0);
}