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
    float n = 5.0;
    vec2 src_size = vec2(uWidth, uHeight);

    vec3 m[4];
    vec3 s[4];
    for (int k = 0; k < 4; ++k) {
        m[k] = vec3(0.0);
        s[k] = vec3(0.0);
    }

    float index_1 = float((float(uRadius)) / uWidth);
    float index_2 = float((float(uRadius)) / uHeight);
    float half_index_1 = index_1 / 2.0;
    float half_index_2 = index_2 / 2.0;

    vec3 c0 = texture2D(sTexture, uv).rgb;
    vec3 c0_2 = c0 * c0;

    vec3 c1 = texture2D(sTexture, uv + vec2(-index_1, -index_2)).rgb;
    vec3 c1_2 = c1 * c1;
    vec3 c2 = texture2D(sTexture, uv + vec2(-index_1, 0.0)).rgb;
    vec3 c2_2 = c2 * c2;

    vec3 c3 = texture2D(sTexture, uv + vec2(0.0, -index_2)).rgb;
    vec3 c3_2 = c3 * c3;

    vec3 c4 = texture2D(sTexture, uv + vec2(-half_index_1, -half_index_2)).rgb;
    vec3 c4_2 = c4 * c4;

    vec3 c5 = texture2D(sTexture, uv + vec2(index_1, -index_2)).rgb;
    vec3 c5_2 = c5 * c5;

    vec3 c6 = texture2D(sTexture, uv + vec2(index_1, 0.0)).rgb;
    vec3 c6_2 = c6 * c6;
    vec3 c7 = texture2D(sTexture, uv + vec2(half_index_1, -half_index_2)).rgb;
    vec3 c7_2 = c7 * c7;
    vec3 c8 = texture2D(sTexture, uv + vec2(index_1, index_2)).rgb;
    vec3 c8_2 = c8 * c8;
    vec3 c9 = texture2D(sTexture, uv + vec2(0.0, index_2)).rgb;
    vec3 c9_2 = c9 * c9;
    vec3 c10 = texture2D(sTexture, uv + vec2(half_index_1, half_index_2)).rgb;
    vec3 c10_2 = c10 * c10;
    vec3 c11 = texture2D(sTexture, uv + vec2(-index_1, index_2)).rgb;
    vec3 c11_2 = c11 * c11;
    vec3 c12 = texture2D(sTexture, uv + vec2(-half_index_1, half_index_2)).rgb;
    vec3 c12_2 = c12 * c12;


    m[0] = c0 + c1 + c2 + c3 + c4;
    s[0] = c0_2 + c1_2 + c2_2 + c3_2 + c4_2;

    m[1] = c3 + c5 + c6 + c7 + c0;
    s[1] = c3_2 + c5_2 + c6_2 + c7_2 + c0_2;

    m[2] = c6 + c8 + c9 + c10 + c0;
    s[2] = c6_2 + c8_2 + c9_2 + c10_2 + c0_2;

    m[3] = c9 + c2 + c11 + c12 + c0;
    s[3] = c9_2 + c2_2 + c11_2 + c12_2 + c0_2;

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

    final_color = final_color / count;

    final_color = final_color.rgb  + final_color.rgb * ((1.0 / (1.0 - 0.2)) - 1.0);
    final_color = vec3(0.5, 0.5, 0.5) +
        (final_color.rgb  - vec3(0.5, 0.5, 0.5)) * 1.4;

    float max = max(max(final_color.r, final_color.g), final_color.b);
    float min = min(min(final_color.r, final_color.g), final_color.b);
    float delta = (max - min);

    float addSum = (max + min);
    float l = addSum / 2.0;
    float ss;
    vec3 result;

    if(l < 0.5) {
        ss = delta / addSum;
    } else {
        ss = delta / (2.0 - addSum);
    }

    float saturation = 0.1;
    float ratio = saturation + ss;
    if((ratio) >= 1.0){
        ratio = ss;
    } else {
       ratio = 1.0 - saturation;
    }

    ratio = 1.0/ratio - 1.0;
    result = final_color.rgb + (final_color.rgb - l) * ratio;

    gl_FragColor = vec4(final_color , 1.0);
 }