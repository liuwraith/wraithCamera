package com.wraith.wraithcamera.gpucomponents.gles.imageprocessprograms;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.wraith.wraithcamera.gpucomponents.gles.GlUtil;
import com.wraith.wraithcamera.gpucomponents.gles.Texture2dProgram;

import java.nio.FloatBuffer;

/**
 * Created by liuzongyang on 15/5/21.
 */
public class CameraMoiveStyleProgram extends Texture2dProgram {

    // Fragment shader that converts color to moive style with a simple transformation.
    private static final String FRAGMENT_SHADER_EXT_MOIVE_STYLE =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "uniform sampler2D movieFilterTexture;\n" +
                    "void main() {\n" +
                    "    vec4 tc = texture2D(sTexture, vTextureCoord);\n" +
                    "    vec4 movieFilter = texture2D(movieFilterTexture, vTextureCoord);\n" +
                    "    vec3 color = vec3(tc.r + movieFilter.r, tc.g + movieFilter.g, tc.b + movieFilter.b);\n" +
                    "    gl_FragColor = vec4(color, 1.0);\n" +
                    "}\n";

    private static int mMoiveFilterTextureHandler = -1;
    private int muMoiveFilterTextureLoc = -1;
    private static int CURRENT_ACTIVE_TEXTURE = GLES20.GL_TEXTURE1;
    private static int NEXT_ACTIVE_TEXTURE = GLES20.GL_TEXTURE1;

    public CameraMoiveStyleProgram(Context context){
        mDefaultTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
        mFilterType = FilterType.FILTER_MOVIE;

        mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_EXT_MOIVE_STYLE);

        if(mProgramHandle == 0){
            throw new RuntimeException("Unable to create program!");
        }

        //mMoiveFilterTextureHandler = createTextureObject(context, R.drawable.test_poster);

        CURRENT_ACTIVE_TEXTURE = NEXT_ACTIVE_TEXTURE;
        NEXT_ACTIVE_TEXTURE++;

        initParams();
    }

    @Override
    public void release() {
        super.release();
        if(mMoiveFilterTextureHandler != -1) {
            GLES20.glDeleteTextures(1, new int[]{mMoiveFilterTextureHandler}, 0);
            mMoiveFilterTextureHandler = -1;
        }
    }

    // get locations of attributes and uniforms
    protected void initParams(){
        super.initParams();
        muMoiveFilterTextureLoc = GLES20.glGetUniformLocation(mProgramHandle, "movieFilterTexture");
        GlUtil.checkLocation(muMoiveFilterTextureLoc, "movieFilterTexture");
    }

    public void draw(float[] mvpMatrix, FloatBuffer vertexBuffer, int firstVertex,
                     int vertexCount, int coordsPerVertex, int vertexStride,
                     float[] texMatrix, FloatBuffer texBuffer, int textureId, int texStride) {
        // Select the program.
        GLES20.glUseProgram(mProgramHandle);
        // Set the texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(mDefaultTextureTarget, textureId);

        GLES20.glActiveTexture(CURRENT_ACTIVE_TEXTURE);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mMoiveFilterTextureHandler);
        GLES20.glUniform1i(muMoiveFilterTextureLoc, CURRENT_ACTIVE_TEXTURE - GLES20.GL_TEXTURE0);

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
