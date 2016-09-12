package com.wraith.wraithcamera.gpucomponents.gles.imageprocessprograms;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.wraith.wraithcamera.R;
import com.wraith.wraithcamera.gpucomponents.gles.GlUtil;
import com.wraith.wraithcamera.gpucomponents.gles.Texture2dProgram;

import java.nio.FloatBuffer;

/**
 * Created by liuzongyang on 15/11/10.
 */
public class CameraToasterProgram extends Texture2dProgram {
    private int mMixLocation = -1;
    private float mMix = 0.7f;
    public static final String TOASTER_FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision highp float;\n" +
            "\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "\n" +
            "uniform highp float mixturePercent;\n" +

            "uniform samplerExternalOES inputImageTexture;\n" +
            "uniform sampler2D inputImageTexture2; //toasterMetal\n" +
            "uniform sampler2D inputImageTexture3; //toasterSoftlight\n" +
            "uniform sampler2D inputImageTexture4; //toasterCurves\n" +
            "uniform sampler2D inputImageTexture5; //toasterOverlayMapWarm\n" +
            "uniform sampler2D inputImageTexture6; //toasterColorshift\n" +
                    "const highp float EPSILON = 0.001;\n"+
                    "const highp float SUB_EPSLION = 0.999;\n"+
            "\n" +
            "void main()\n" +
            "{\n" +
            "    highp vec3 texel;\n" +
            "    highp vec2 lookup;\n" +
            "    highp vec2 blue;\n" +
            "    highp vec2 green;\n" +
            "    highp vec2 red;\n" +
            "    highp vec4 tmpvar_1;\n" +
            "    tmpvar_1 = texture2D (inputImageTexture, vTextureCoord);\n" +
            "    tmpvar_1 = clamp(tmpvar_1, EPSILON, SUB_EPSLION);\n" +
            "    texel = tmpvar_1.xyz;\n" +
            "    highp vec4 tmpvar_2;\n" +
            "    tmpvar_2 = texture2D (inputImageTexture2, vTextureCoord);\n" +
                    "    tmpvar_2 = clamp(tmpvar_2, EPSILON, SUB_EPSLION);\n" +
                    "    highp vec2 tmpvar_3;\n" +
            "    tmpvar_3.x = tmpvar_2.x;\n" +
            "    tmpvar_3.y = tmpvar_1.x;\n" +
            "    texel.x = texture2D (inputImageTexture3, tmpvar_3).x;\n" +
                    "    texel = clamp(texel, EPSILON, SUB_EPSLION);\n" +
                    "    highp vec2 tmpvar_4;\n" +
            "    tmpvar_4.x = tmpvar_2.y;\n" +
            "    tmpvar_4.y = tmpvar_1.y;\n" +
            "    texel.y = texture2D (inputImageTexture3, tmpvar_4).y;\n" +
                    "    texel = clamp(texel, EPSILON, SUB_EPSLION);\n" +
            "    highp vec2 tmpvar_5;\n" +
            "    tmpvar_5.x = tmpvar_2.z;\n" +
            "    tmpvar_5.y = tmpvar_1.z;\n" +
            "    texel.z = texture2D (inputImageTexture3, tmpvar_5).z;\n" +
                    "    texel = clamp(texel, EPSILON, SUB_EPSLION);\n" +
            "    red.x = texel.x;\n" +
            "    red.y = 0.16666;\n" +
            "    green.x = texel.y;\n" +
            "    green.y = 0.5;\n" +
            "    blue.x = texel.z;\n" +
            "    blue.y = 0.833333;\n" +
            "    texel.x = texture2D (inputImageTexture4, red).x;\n" +
            "    texel.y = texture2D (inputImageTexture4, green).y;\n" +
            "    texel.z = texture2D (inputImageTexture4, blue).z;\n" +
                    "    texel = clamp(texel, EPSILON, SUB_EPSLION);\n" +
            "    highp vec2 tmpvar_6;\n" +
            "    tmpvar_6 = ((2.0 * vTextureCoord) - 1.0);\n" +
                    "    tmpvar_6 = clamp(tmpvar_6, EPSILON, SUB_EPSLION);\n" +
            "    highp vec2 tmpvar_7;\n" +
            "    tmpvar_7.x = dot (tmpvar_6, tmpvar_6);\n" +
            "    tmpvar_7.y = texel.x;\n" +
                    "    tmpvar_7 = clamp(tmpvar_7, EPSILON, SUB_EPSLION);\n" +
            "    lookup = tmpvar_7;\n" +
            "    texel.x = texture2D (inputImageTexture5, tmpvar_7).x;\n" +
                    "    texel = clamp(texel, EPSILON, SUB_EPSLION);\n" +
            "    lookup.y = texel.y;\n" +
            "    texel.y = texture2D (inputImageTexture5, lookup).y;\n" +
                    "    texel = clamp(texel, EPSILON, SUB_EPSLION);\n" +
            "    lookup.y = texel.z;\n" +
            "    texel.z = texture2D (inputImageTexture5, lookup).z;\n" +
                    "    texel = clamp(texel, EPSILON, SUB_EPSLION);\n" +
            "    red.x = texel.x;\n" +
            "    green.x = texel.y;\n" +
            "    blue.x = texel.z;\n" +
            "    texel.x = texture2D (inputImageTexture6, red).x;\n" +
            "    texel.y = texture2D (inputImageTexture6, green).y;\n" +
            "    texel.z = texture2D (inputImageTexture6, blue).z;\n" +
                    "    texel = clamp(texel, EPSILON, SUB_EPSLION);\n" +
            "    highp vec4 tmpvar_8;\n" +
            "    tmpvar_8.w = 1.0;\n" +
            "    tmpvar_8.xyz = texel;\n" +
                    "    tmpvar_8 = clamp(tmpvar_8, EPSILON, SUB_EPSLION);\n" +
            "    gl_FragColor = vec4(mix(tmpvar_1.rgb,tmpvar_8.rgb,mixturePercent), 1.0);\n"+
            "}";
    private int mInputImageTexture2Handler = -1;
    private int mInputImageTexture2Loc = -1;

    private int mInputImageTexture3Handler = -1;
    private int mInputImageTexture3Loc = -1;

    private int mInputImageTexture4Handler = -1;
    private int mInputImageTexture4Loc = -1;

    private int mInputImageTexture5Handler = -1;
    private int mInputImageTexture5Loc = -1;

    private int mInputImageTexture6Handler = -1;
    private int mInputImageTexture6Loc = -1;
    public CameraToasterProgram(Context context) {
        mDefaultTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
        mFilterType = FilterType.FILTER_TOASTER;
        mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, TOASTER_FRAGMENT_SHADER);

        if(mProgramHandle == 0) {
            throw new RuntimeException("Unable to create program");
        }
        mInputImageTexture2Handler = createTextureObject(context, R.mipmap.toaster_metal);
        mInputImageTexture3Handler = createTextureObject(context, R.mipmap.toaster_soft_light);
        mInputImageTexture4Handler = createTextureObject(context, R.mipmap.toaster_curves);
        mInputImageTexture5Handler = createTextureObject(context, R.mipmap.toaster_overlay_map_warm);
        mInputImageTexture6Handler = createTextureObject(context, R.mipmap.toaster_color_shift);
        initParams();
    }

    @Override
    public void release(){
        super.release();
        if(mInputImageTexture2Handler != -1) {
            GLES20.glDeleteTextures(1, new int[]{mInputImageTexture2Handler}, 0);
            mInputImageTexture2Handler = -1;
        }
        if(mInputImageTexture3Handler != -1) {
            GLES20.glDeleteTextures(1, new int[]{mInputImageTexture3Handler}, 0);
            mInputImageTexture3Handler = -1;
        }
        if(mInputImageTexture4Handler != -1) {
            GLES20.glDeleteTextures(1, new int[]{mInputImageTexture4Handler}, 0);
            mInputImageTexture4Handler = -1;
        }
        if(mInputImageTexture5Handler != -1) {
            GLES20.glDeleteTextures(1, new int[]{mInputImageTexture5Handler}, 0);
            mInputImageTexture5Handler = -1;
        }
        if(mInputImageTexture6Handler != -1) {
            GLES20.glDeleteTextures(1, new int[]{mInputImageTexture6Handler}, 0);
            mInputImageTexture6Handler = -1;
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        mMixLocation = GLES20.glGetUniformLocation(mProgramHandle, "mixturePercent");
        mInputImageTexture2Loc = GLES20.glGetUniformLocation(mProgramHandle, "inputImageTexture2");
        mInputImageTexture3Loc = GLES20.glGetUniformLocation(mProgramHandle, "inputImageTexture3");
        mInputImageTexture4Loc = GLES20.glGetUniformLocation(mProgramHandle, "inputImageTexture4");
        mInputImageTexture5Loc = GLES20.glGetUniformLocation(mProgramHandle, "inputImageTexture5");
        mInputImageTexture6Loc = GLES20.glGetUniformLocation(mProgramHandle, "inputImageTexture6");
    }

    private void setParam() {
        GLES20.glUniform1f(mMixLocation, mMix);
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
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mInputImageTexture2Handler);
        GLES20.glUniform1i(mInputImageTexture2Loc, 1);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mInputImageTexture3Handler);
        GLES20.glUniform1i(mInputImageTexture3Loc, 2);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mInputImageTexture4Handler);
        GLES20.glUniform1i(mInputImageTexture4Loc, 3);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mInputImageTexture5Handler);
        GLES20.glUniform1i(mInputImageTexture5Loc, 4);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE5);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mInputImageTexture6Handler);
        GLES20.glUniform1i(mInputImageTexture6Loc, 5);

        //set param
        setParam();

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
