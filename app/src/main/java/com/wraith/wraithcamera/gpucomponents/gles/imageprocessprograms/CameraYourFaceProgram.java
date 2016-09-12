package com.wraith.wraithcamera.gpucomponents.gles.imageprocessprograms;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.wraith.wraithcamera.R;
import com.wraith.wraithcamera.gpucomponents.gles.GlUtil;
import com.wraith.wraithcamera.gpucomponents.gles.Texture2dProgram;

import java.nio.FloatBuffer;

/**
 * Created by liuzongyang on 15/11/9.
 */
public class CameraYourFaceProgram extends Texture2dProgram {
    private static final String FragmentShader =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "uniform samplerExternalOES inputImageTexture;\n" +
            "uniform sampler2D inputImageTexture2;\n" +
            "uniform float faceRatio;\n"+
            "uniform mediump float shadow;\n" +
            "uniform mediump float highlight;\n" +
            "uniform mediump float contrast;\n" +
            "uniform mediump float centerLight;\n" +
            "uniform mediump float brightness;\n" +
            "uniform mediump float saturation;\n" +
            "const highp float EPSILON = 0.001;\n" +
            "const mediump vec3 luminanceWeighting = vec3(0.3, 0.3, 0.3);\n" +
            "mediump vec3 adjustBrightness(vec3 originalColor){\n" +
            "    return originalColor.rgb  + originalColor.rgb * ((1.0 / (1.0 - brightness)) - 1.0);\n" +
            "}\n" +
            "highp vec3 adjustShadowHighlight(highp vec3 originColor){\n" +
            "   mediump float luminanceValue = dot(originColor, luminanceWeighting);\n" +
            " 	mediump float shadowValue = clamp((pow(luminanceValue, 1.0/(abs(shadow)+1.0)) + (-0.76)*pow(luminanceValue, 2.0/(abs(shadow)+1.0))) - luminanceValue, 0.0, 1.0);\n" +
            " 	mediump float highlightValue = clamp((1.0 - (pow(1.0-luminanceValue, 1.0/(2.0-abs(1.0-abs(highlight)))) + (-0.6)*pow(1.0-luminanceValue, 2.0/(2.0-abs(1.0-abs(highlight)))))) - luminanceValue, -1.0, 0.0);\n" +
            "   shadowValue = sign(shadow) * pow(shadowValue, 0.65) - sign(-shadow) * pow(shadowValue, 0.65);\n"+
            "   highlightValue = sign(highlight) * abs(highlightValue) - sign(-highlight) * abs(highlightValue);\n"+
            "   highp vec3 result = originColor.rgb  + originColor.rgb * ((1.0 / (1.0 - (shadowValue + highlightValue))) - 1.0);\n"+

            "   return result;\n"+
            "}\n" +
            "highp vec3 adjustContrast(highp vec3 originalColor){\n" +
            "    return vec3(0.5, 0.5, 0.5) + (originalColor.rgb  - vec3(0.5, 0.5, 0.5)) * (1.0 + contrast);\n" +
            "}\n" +
            "highp float Luminance(highp vec3 color)\n" +
            "{\n" +
            "    highp float fmin = min(min(color.r, color.g), color.b);\n" +
            "    highp float fmax = max(max(color.r, color.g), color.b);\n" +
            "    return (fmax + fmin) / 2.0;\n" +
            "}\n" +
            "highp vec3 adjustCoffieStyle(highp vec3 originColor){\n"+
            "   highp vec3 result;"+
            "   result.r = texture2D(inputImageTexture2, vec2(originColor.r, 0.16666)).r;"+
            "   result.g = texture2D(inputImageTexture2, vec2(originColor.g, 0.5)).g;"+
            "   result.b = texture2D(inputImageTexture2, vec2(originColor.b, 0.83333)).b;"+
            "   return result;\n"+
            "}\n"+
            "highp vec3 adjustSaturation(highp vec3 originalColor){\n" +
            "     float max = max(max(originalColor.r, originalColor.g), originalColor.b);\n" +
            "    float min = min(min(originalColor.r, originalColor.g), originalColor.b);\n" +
            "    float delta = (max - min);\n" +
            "\n" +
            "    float addSum = (max + min);\n" +
            "    float l = addSum / 2.0;\n" +
            "    float s;\n" +
            "    vec3 result;\n" +
            "\n" +
            "    if(l < 0.5) {\n" +
            "        s = delta / addSum;\n" +
            "    } else {\n" +
            "        s = delta / (2.0 - addSum);\n" +
            "    }\n" +
            "\n" +
            "    float ratio;\n" +
            "    if(saturation > 0.0) {\n" +
            "        if((saturation + s) >= 1.0){\n" +
            "            ratio = s;\n" +
            "        } else {\n" +
            "            ratio = 1.0 - saturation;\n" +
            "        }\n" +
            "\n" +
            "        ratio = 1.0/ratio - 1.0;\n" +
            "        result = originalColor.rgb + (originalColor.rgb - l) * ratio;\n" +
            "    } else {\n" +
            "        ratio = 1.0 + saturation;\n" +
            "\n" +
            "        result = vec3(l, l, l) + (originalColor.rgb - vec3(l, l, l)) *\n" +
            "            vec3(ratio, ratio, ratio);\n" +
            "    }\n"+
            "   return result;\n" +
            "}\n" +
            "highp vec3 adjustCenterLight(highp vec3 originColor){\n" +
            "   highp float length = length(vTextureCoord - vec2(0.5, 0.5));\n" +
            "   highp float weight =  0.25 * clamp(exp(-2.0 * length * length) - (1.0 - centerLight) * 0.8 * exp(-4.0 * length * length * centerLight) - length , 0.0, 1.0);\n" +
            "   return originColor.rgb  + originColor.rgb * ((1.0 / (1.0 - weight)) - 1.0);\n" +
            "}"+
            "void main() \n"+
            "{  highp vec4 textureColor = texture2D(inputImageTexture, vTextureCoord);\n" +
            "   highp float oneSubEpsilon = 1.0 - EPSILON;\n"+
            "   highp vec3 result = clamp(textureColor.rgb, EPSILON, oneSubEpsilon);\n"+
            "   highp vec3 temp = result;\n"+
            "   result = mix(temp, clamp(adjustCoffieStyle(result), EPSILON, oneSubEpsilon), faceRatio);\n"+
            "   result = clamp(adjustBrightness(result.rgb), EPSILON, oneSubEpsilon);\n" +
            "   result = clamp(adjustContrast(result), EPSILON, oneSubEpsilon);\n" +
            "   result = clamp(adjustSaturation(result), EPSILON, oneSubEpsilon);\n" +
            "   result = clamp(adjustCenterLight(result), EPSILON, oneSubEpsilon);\n"+
            "   result = clamp(adjustShadowHighlight(result), EPSILON, oneSubEpsilon);\n" +
            "   result = mix(textureColor.rgb, result, faceRatio);\n"+
            "   gl_FragColor = vec4(result, 1.0);\n" +
            "} \n" +
            "";

    private float mShadow = -0.5f;
    //highlights: Decrease to darken highlights, from 0.0 to 1.0, with 1.0 as the default.
    private float mHighLight = 0.1f;
    // from -1 to 1 default 0
    private float mContrast = 0.22f;

    private float mCenterLight = 0.3f;
    //from 0 to 1 default 0
    private float mBrightness = 0.1f;
    //from -1 to 1 default 0
    private float mSaturation = 0.18f;
    private float mFaceRatio = 0.7f;

    private int mShadowLoc = -1;
    private int mHighLightLoc = -1;
    private int mContrastLoc = -1;
    private int mCenterLightLoc = -1;
    private int mBrightnessLoc = -1;
    private int mFaceRatioLoc = -1;
    private int mSaturationLoc = -1;

    private int mYourFaceTextureHandler = -1;
    private int mYourFaceTextureLoc = -1;

    public CameraYourFaceProgram(Context context) {
        mDefaultTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
        mFilterType = FilterType.FILTER_YOURFACE;
        mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FragmentShader);

        if(mProgramHandle == 0) {
            throw new RuntimeException("Unable to create program");
        }
        mYourFaceTextureHandler = createTextureObject(context, R.mipmap.travel);
        initParams();
    }

    @Override
    public void release(){
        super.release();
        if(mYourFaceTextureHandler != -1) {
            GLES20.glDeleteTextures(1, new int[]{mYourFaceTextureHandler}, 0);
            mYourFaceTextureHandler = -1;
        }
    }

    @Override
    protected void initParams() {
        super.initParams();
        mYourFaceTextureLoc = GLES20.glGetUniformLocation(mProgramHandle, "inputImageTexture2");

        mShadowLoc = GLES20.glGetUniformLocation(mProgramHandle, "shadow");
        mHighLightLoc = GLES20.glGetUniformLocation(mProgramHandle, "highlight");
        mContrastLoc = GLES20.glGetUniformLocation(mProgramHandle, "contrast");
        mCenterLightLoc = GLES20.glGetUniformLocation(mProgramHandle, "centerLight");
        mBrightnessLoc = GLES20.glGetUniformLocation(mProgramHandle, "brightness");
        mSaturationLoc = GLES20.glGetUniformLocation(mProgramHandle, "saturation");
        mFaceRatioLoc = GLES20.glGetUniformLocation(mProgramHandle, "faceRatio");
    }

    private void setParams() {
        GLES20.glUniform1f(mShadowLoc, mShadow);
        GLES20.glUniform1f(mHighLightLoc, mHighLight);
        GLES20.glUniform1f(mContrastLoc, mContrast);
        GLES20.glUniform1f(mCenterLightLoc, mCenterLight);
        GLES20.glUniform1f(mBrightnessLoc, mBrightness);
        GLES20.glUniform1f(mFaceRatioLoc, mFaceRatio);
        GLES20.glUniform1f(mSaturationLoc, mSaturation);
    }

    public void draw(float[] mvpMatrix, FloatBuffer vertexBuffer, int firstVertex,
                     int vertexCount, int coordsPerVertex, int vertexStride,
                     float[] texMatrix, FloatBuffer texBuffer, int textureId, int texStride) {
        // Select the program.
        GLES20.glUseProgram(mProgramHandle);
        // Set the texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(mDefaultTextureTarget, textureId);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mYourFaceTextureHandler);
        GLES20.glUniform1i(mYourFaceTextureLoc, 1);

        //set param
        setParams();

        // Copy the model / view / projection matrix over.
        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mvpMatrix, 0);

        // Copy the texture transformation matrix over.
        GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, texMatrix, 0);

        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(maPositionLoc);

        // Connect vertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer(maPositionLoc, coordsPerVertex,
                GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        // Enable the "aTextureCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(maTextureCoordLoc);

        // Connect texBuffer to "aTextureCoord".
        GLES20.glVertexAttribPointer(maTextureCoordLoc, 2,
                GLES20.GL_FLOAT, false, texStride, texBuffer);

        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, firstVertex, vertexCount);

        // Done -- disable vertex array, texture, and program.
        GLES20.glDisableVertexAttribArray(maPositionLoc);
        GLES20.glDisableVertexAttribArray(maTextureCoordLoc);

        GLES20.glBindTexture(mDefaultTextureTarget, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);
    }

}
