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
public class Camera1977Program extends Texture2dProgram{
    public static final String CAMERA_1977_FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision highp float;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "uniform highp float mixturePercent;\n" +
            "uniform samplerExternalOES inputImageTexture;\n" +
            "uniform sampler2D inputImageTexture2;\n" +
            "const highp float EPSILON = 0.001;\n"+
            "const highp float SUB_EPSLION = 0.999;\n"+
            "\n" +
            "void main()\n" +
            "{\n" +
            "   highp vec3 texel1 = texture2D(inputImageTexture, vTextureCoord).rgb;\n" +
            "   texel1 = clamp(texel1, EPSILON, SUB_EPSLION);\n"+
            "\n" +
            "   highp vec3 texel2 = vec3(\n" +
            "                 texture2D(inputImageTexture2, vec2(texel1.r, .1666)).r,\n" +
            "                 texture2D(inputImageTexture2, vec2(texel1.g, .5)).g,\n" +
            "                 texture2D(inputImageTexture2, vec2(texel1.b, .8333)).b);\n" +
            "   texel2 = clamp(texel2, EPSILON, SUB_EPSLION);\n"+
            "   highp vec3 result = clamp(mix(texel1, texel2, mixturePercent), EPSILON, SUB_EPSLION);\n"+
            "\n" +
            "   gl_FragColor = vec4(result, 1.0);\n" +
            "}";

    private int m1977TextureHandler = -1;
    private int m1977TextureLoc = -1;

    private int mMixturePercentLoc = -1;
    private float mMixturePercent = 0.7f;

    public Camera1977Program(Context context) {
        mDefaultTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
        mFilterType = FilterType.FILTER_1977;
        mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, CAMERA_1977_FRAGMENT_SHADER);

        if(mProgramHandle == 0) {
            throw new RuntimeException("Unable to create program");
        }
        m1977TextureHandler = createTextureObject(context, R.mipmap.in1977_map);
        initParams();
    }

    @Override
    public void release(){
        super.release();
        if(m1977TextureHandler != -1) {
            GLES20.glDeleteTextures(1, new int[]{m1977TextureHandler}, 0);
            m1977TextureHandler = -1;
        }
    }

    @Override
    protected void initParams(){
        super.initParams();
        m1977TextureLoc = GLES20.glGetUniformLocation(mProgramHandle, "inputImageTexture2");
        mMixturePercentLoc = GLES20.glGetUniformLocation(mProgramHandle, "mixturePercent");
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
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, m1977TextureHandler);
        GLES20.glUniform1i(m1977TextureLoc, 1);

        GLES20.glUniform1f(mMixturePercentLoc, mMixturePercent);

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
