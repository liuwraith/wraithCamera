#extension GL_OES_EGL_image_external : require
precision highp float;							// precision in the fragment shader.
uniform float u_SaturationRatio;
varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;
  
// The entry point for our fragment shader.
void main()                    		
{
    vec4 originalColor = texture2D(sTexture, vTextureCoord);

    float max = max(max(originalColor.r, originalColor.g), originalColor.b);
    float min = min(min(originalColor.r, originalColor.g), originalColor.b);
    float delta = (max - min);

    float addSum = (max + min);
    float l = addSum / 2.0;
    float s;
    vec3 result;

    if(l < 0.5) {
        s = delta / addSum;
    } else {
        s = delta / (2.0 - addSum);
    }

    float ratio;
    if(u_SaturationRatio > 0.0) {
        if((u_SaturationRatio + s) >= 1.0){
            ratio = s;
        } else {
            ratio = 1.0 - u_SaturationRatio;
        }

        ratio = 1.0/ratio - 1.0;
        result = originalColor.rgb + (originalColor.rgb - l) * ratio;
    } else {
        ratio = 1.0 + u_SaturationRatio;

        result = vec3(l, l, l) + (originalColor.rgb - vec3(l, l, l)) *
            vec3(ratio, ratio, ratio);
    }

	// Multiply the color by the diffuse illumination level and texture value to get final output color.
    gl_FragColor = vec4(result, originalColor.a); //(diffuse * );
  }                                                                     	

