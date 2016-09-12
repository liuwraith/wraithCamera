package com.wraith.wraithcamera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.util.Log;

import com.wraith.wraithcamera.gpucomponents.GLCameraPhotoTextureView;
import com.wraith.wraithcamera.gpucomponents.gles.FullFrameRect;
import com.wraith.wraithcamera.gpucomponents.gles.Texture2dProgram;
import com.wraith.wraithcamera.gpucomponents.gles.Texture2dProgramFactory;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by liuzongyang on 16/5/26.
 */
public class WraithCameraRender implements GLCameraPhotoTextureView.Renderer {
    private static final String TAG = WraithCameraRender.class.getSimpleName();
    private static final boolean VERBOSE = false;
    private FullFrameRect mFullScreen;

    private final float[] mSTMatrix = new float[16];
    private int mTextureId;
    private Texture2dProgram mCurrentProgram;
    private SurfaceTexture mSurfaceTexture;
    // width/height of the incoming camera preview frames
    private int mIncomingWidth;
    private int mIncomingHeight;
    private WeakReference<Context> mContextWeakRef = null;

    private Texture2dProgram.FilterType mCurrentFilterType = Texture2dProgram.FilterType.FILTER_NONE;

    public Texture2dProgram.FilterType getCurrentFilterType(){
        return mCurrentFilterType;
    }

    public void setFilterType(Texture2dProgram.FilterType filterType) {
        mCurrentFilterType = filterType;
    }

    public interface RenderCallBack{
        void onSurfaceCreated(SurfaceTexture surfaceTexture);
    }

    private RenderCallBack mRenderCallBack = null;

    public WraithCameraRender(RenderCallBack callBack, Context context) {
        mTextureId = -1;
        mIncomingWidth = mIncomingHeight = -1;

        // We could preserve the old filter mode, but currently not bothering.
        mCurrentFilterType = Texture2dProgram.FilterType.FILTER_NONE;
        mRenderCallBack = callBack;

        mContextWeakRef = new WeakReference<Context>(context);
    }

    /**
     * Notifies the renderer thread that the activity is pausing.
     * <p>
     * For best results, call this *after* disabling Camera preview.
     */
    public void notifyPausing() {
        if (mSurfaceTexture != null) {
            Log.d(TAG, "renderer pausing -- releasing SurfaceTexture");
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        if (mFullScreen != null) {
            mFullScreen.release(false);     // assume the GLSurfaceView EGL context is about
            mFullScreen = null;             //  to be destroyed
        }
        mIncomingWidth = mIncomingHeight = -1;
    }

    public void notifyResume() {
        mFullScreen = new FullFrameRect();
        mTextureId = mFullScreen.createTextureObject();

        // Create a SurfaceTexture, with an external texture, in this EGL context.  We don't
        // have a Looper in this thread -- GLSurfaceView doesn't create one -- so the frame
        // available messages will arrive on the main thread.

        mSurfaceTexture = new SurfaceTexture(mTextureId);
        // Tell the UI thread to enable the camera preview.
        mRenderCallBack.onSurfaceCreated(mSurfaceTexture);
    }

    /**
     * Records the size of the incoming camera preview frames.
     * <p>
     * It's not clear whether this is guaranteed to execute before or after onSurfaceCreated(),
     * so we assume it could go either way.  (Fortunately they both run on the same thread,
     * so we at least know that they won't execute concurrently.)
     */
    public void setCameraPreviewSize(int width, int height) {
        Log.d(TAG, "setCameraPreviewSize");
        mIncomingWidth = width;
        mIncomingHeight = height;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        Log.d("wraith", "onSurfaceCreated");
        // Set up the texture blitter that will be used for on-screen display.  This
        // is *not* applied to the recording, because that uses a separate shader.
        GLES20.glClearColor(0, 0, 0, 1);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        notifyResume();
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        Log.d(TAG, "onSurfaceChanged " + width + "x" + height);

        mCurrentProgram = Texture2dProgramFactory.getTexture2dProgram(mContextWeakRef.get(), mCurrentFilterType, width, height);
        mFullScreen.changeProgram(mCurrentProgram);
        mFullScreen.getProgram().setTexSize(mIncomingWidth, mIncomingHeight);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        if (VERBOSE) Log.d(TAG, "onDrawFrame tex=" + mTextureId);
        // Latch the latest frame.  If there isn't anything new, we'll just re-use whatever
        // was there before.
        mSurfaceTexture.updateTexImage();

        if (mIncomingWidth <= 0 || mIncomingHeight <= 0) {
            // Texture size isn't set yet.  This is only used for the filters, but to be
            // safe we can just skip drawing while we wait for the various races to resolve.
            // (This seems to happen if you toggle the screen off/on with power button.)
            Log.i(TAG, "Drawing before incoming texture size set; skipping");
            return;
        }
        // Draw the video frame.
        mSurfaceTexture.getTransformMatrix(mSTMatrix);
        mFullScreen.drawFrame(mTextureId, mSTMatrix);
    }
}
