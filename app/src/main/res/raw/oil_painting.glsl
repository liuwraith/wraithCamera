#extension GL_OES_EGL_image_external : require
precision highp float;
varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;
uniform int uRadius;
uniform float uWidth;
uniform float uHeight;

 void main (void) 
 {
    vec2 uv = vTextureCoord;
    float n = float((uRadius + 1) * (uRadius + 1));
    vec2 src_size = vec2(uWidth, uHeight);

    vec3 m[4];
    vec3 s[4];
    for (int k = 0; k < 4; ++k) {
        m[k] = vec3(0.0);
        s[k] = vec3(0.0);
    }

    for (int j = -uRadius; j <= 0; j+=1)  {
        for (int i = -uRadius; i <= 0; i+=1)  {
            vec3 c = texture2D(sTexture, uv + vec2(i,j) / src_size).rgb;
            m[0] += c;
            s[0] += c * c;
        }
    }

    for (int j = -uRadius; j <= 0; j+=1)  {
        for (int i = 0; i <= uRadius; i+=1)  {
            vec3 c = texture2D(sTexture, uv + vec2(i,j) / src_size).rgb;
            m[1] += c;
            s[1] += c * c;
        }
    }

    for (int j = 0; j <= uRadius; j+=1)  {
        for (int i = 0; i <= uRadius; i+=1)  {
            vec3 c = texture2D(sTexture, uv + vec2(i,j) / src_size).rgb;
            m[2] += c;
            s[2] += c * c;
        }
    }

    for (int j = 0; j <= uRadius; j+=1)  {
        for (int i = -uRadius; i <= 0; i+=1)  {
            vec3 c = texture2D(sTexture, uv + vec2(i,j) / src_size).rgb;
            m[3] += c;
            s[3] += c * c;
        }
    }
    float min_sigma2 = 1e+2;
    float sigma_array[4];
    for (int k = 0; k < 4; ++k) {
        m[k] /= n;
        s[k] = abs(s[k] / n - m[k] * m[k]);
        float sigma2 = s[k].r + s[k].g + s[k].b;
        sigma_array[k] = sigma2;
        min_sigma2 = min(sigma2, min_sigma2);
    }
    vec3 final_color = vec3(0.0, 0.0, 0.0);
    float count = 0.0;
    for(int kk = 0; kk < 4; ++kk){
        count += (1.0 + sign(min_sigma2 - sigma_array[kk]));
        final_color += (1.0 + sign(min_sigma2 - sigma_array[kk])) * m[kk];
    }

    final_color = final_color/count;
    final_color = final_color.rgb  + final_color.rgb * ((1.0 / (1.0 - 0.1)) - 1.0);
    final_color = vec3(0.5, 0.5, 0.5) +
            (final_color.rgb  - vec3(0.5, 0.5, 0.5)) * 1.2;

    gl_FragColor = vec4(final_color, 1.0);
 }