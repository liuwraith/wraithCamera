#extension GL_OES_EGL_image_external : require
precision highp float;       	// Set the default precision to medium. We don't need as high of a
								// precision in the fragment shader.
#define KERNEL_SIZE #KERNEL_SIZE#
varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;
uniform highp float uKernel[KERNEL_SIZE * 2 + 1];
uniform highp float uHOffset[KERNEL_SIZE * 2 + 1];
uniform highp float uSigmaColorLength;

float gaussWeight(highp float currentPos, highp float delta)
{
    return exp(-(currentPos * currentPos) / (2.0 * delta * delta));
}

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

void main()
{
    highp float weight = 0.0;
    highp vec4 ocolor = texture2D(sTexture, vTextureCoord);
    highp vec4 result = vec4(ocolor.rgb, 0.0);
    
    highp vec3 hsv = rgb2hsv(ocolor.rgb);
    if(hsv.r > 0.01 && hsv.r <= 50.0 && hsv.g >= 0.1 && hsv.g < 0.63 && hsv.b > 0.01){
        result = vec4(0.0, 0.0, 0.0, 0.0);
        highp vec3 originColor = vec3(0.0, 0.0, 0.0);
        for(int i = 0; i <= 2 * KERNEL_SIZE; i++){
            highp vec3 sampleColor = texture2D(sTexture, vec2(vTextureCoord.x, vTextureCoord.y + uHOffset[i])).rgb;
            highp float gaussWeightValue = uKernel[i];
            highp float closeness = gaussWeight(distance(sampleColor, ocolor.rgb), uSigmaColorLength);
            highp float sampleWeight = gaussWeightValue * closeness;
            originColor += sampleColor * sampleWeight;
            weight += sampleWeight;
        }
        result = vec4(originColor/weight, 1.0);
    }

	gl_FragColor = vec4(result.rgb, ocolor.a);
}