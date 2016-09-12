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
public class CameraSierraProgram extends Texture2dProgram{
    private int mMixLocation = -1;
    private float mMix = 0.7f;
    public static final String SIERRA_FRAGMENT_SHADER = "" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision highp float;\n" +
            "\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "\n" +
            "uniform samplerExternalOES inputImageTexture;\n" +
            "uniform sampler2D inputImageTexture2; //blowout;\n" +
            "uniform sampler2D inputImageTexture3; //overlay;\n" +
            "uniform sampler2D inputImageTexture4; //map\n" +
            "uniform highp float mixturePercent;\n" +
            "const highp float EPSILON = 0.001;\n"+
            "const highp float SUB_EPSLION = 0.999;\n"+
            "\n" +
            "void main()\n" +
            "{\n" +
            "    \n" +
            "    highp vec4 texel = texture2D(inputImageTexture, vTextureCoord);\n" +
            "    texel = clamp(texel, EPSILON, SUB_EPSLION);\n"+
            "    highp vec3 texel2 = texture2D(inputImageTexture, vTextureCoord).rgb;\n" +
            "    texel2 = clamp(texel2, EPSILON, SUB_EPSLION);\n"+
            "    highp vec3 bbTexel = texture2D(inputImageTexture2, vTextureCoord).rgb;\n" +
            "    bbTexel = clamp(bbTexel, EPSILON, SUB_EPSLION);\n"+
            "    \n" +
            "    texel.r = texture2D(inputImageTexture3, vec2(bbTexel.r, texel.r)).r;\n" +
            "    texel.g = texture2D(inputImageTexture3, vec2(bbTexel.g, texel.g)).g;\n" +
            "    texel.b = texture2D(inputImageTexture3, vec2(bbTexel.b, texel.b)).b;\n" +
            "    \n" +
            "    texel = clamp(texel, EPSILON, SUB_EPSLION);\n"+
            "    highp vec4 mapped;\n" +
            "    mapped.r = texture2D(inputImageTexture4, vec2(texel.r, .16666)).r;\n" +
            "    mapped.g = texture2D(inputImageTexture4, vec2(texel.g, .5)).g;\n" +
            "    mapped.b = texture2D(inputImageTexture4, vec2(texel.b, .83333)).b;\n" +
            "    mapped = clamp(mapped, EPSILON, SUB_EPSLION);\n"+
            "    \n" +
            "    gl_FragColor = vec4(mix(texel2,mapped.rgb,mixturePercent),1.0);\n" +
            "}";

    private int mInputImageTexture2Handler = -1;
    private int mInputImageTexture2Loc = -1;

    private int mInputImageTexture3Handler = -1;
    private int mInputImageTexture3Loc = -1;

    private int mInputImageTexture4Handler = -1;
    private int mInputImageTexture4Loc = -1;


    public CameraSierraProgram(Context context) {
        mDefaultTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
        mFilterType = FilterType.FILTER_SIERRA;
        mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, SIERRA_FRAGMENT_SHADER);

        if(mProgramHandle == 0) {
            throw new RuntimeException("Unable to create program");
        }
        mInputImageTexture2Handler = createTextureObject(context, R.mipmap.sierra_vignette);
        mInputImageTexture3Handler = createTextureObject(context, R.mipmap.overlay_map);
        mInputImageTexture4Handler = createTextureObject(context, R.mipmap.sierra_map);
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
    }

    @Override
    public void initParams() {
        super.initParams();
        mMixLocation = GLES20.glGetUniformLocation(mProgramHandle, "mixturePercent");
        mInputImageTexture2Loc = GLES20.glGetUniformLocation(mProgramHandle, "inputImageTexture2");
        mInputImageTexture3Loc = GLES20.glGetUniformLocation(mProgramHandle, "inputImageTexture3");
        mInputImageTexture4Loc = GLES20.glGetUniformLocation(mProgramHandle, "inputImageTexture4");
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
