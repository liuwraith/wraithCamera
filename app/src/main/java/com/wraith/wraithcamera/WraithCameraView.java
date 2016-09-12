package com.wraith.wraithcamera;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;

import com.wraith.wraithcamera.gpucomponents.GLCameraPhotoTextureView;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by liuzongyang on 16/5/26.
 */
public class WraithCameraView extends GLCameraPhotoTextureView implements SurfaceTexture.OnFrameAvailableListener {
    public static final String TAG = WraithCameraView.class.getSimpleName();
    private Camera mCamera = null;
    private WraithCameraRender mRender = null;
    private Camera.Size mPreviewSize;
    private int mCameraId;
    private CameraOrientationListener mOrientationListener;
    private SurfaceTexture mSurfaceTexture;
    private int mCurrentDisplayOrietation = 0;
    private boolean mHasFrontCamera = true;

    private static class MGLiveCameraRenderCallBack implements WraithCameraRender.RenderCallBack {

        private WeakReference<WraithCameraView> mCameraPreviewView = null;

        MGLiveCameraRenderCallBack(WraithCameraView view) {
            mCameraPreviewView = new WeakReference<WraithCameraView>(view);
        }

        @Override
        public void onSurfaceCreated(SurfaceTexture surfaceTexture) {
            if (mCameraPreviewView.get() != null) {
                mCameraPreviewView.get().handleSetSurfaceTexture(surfaceTexture);
            }
        }
    }

    public WraithCameraView(Context context) {
        super(context);
        init(context);
    }

    public WraithCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        try {
            setEGLContextClientVersion(2);
            mRender = new WraithCameraRender(new MGLiveCameraRenderCallBack(this), context);
            setRenderer(mRender);
            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            mCameraId = getFrontCameraID(context);
            Log.i(TAG, "camera id " + mCameraId);
            getCameraDisplayOrientation(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Setup the camera parameters
     */
    private boolean setupCamera(int cameraId) {
        // Never keep a global parameters
        try {
            mCamera = Camera.open(cameraId);
            if (mCamera != null) {
                Camera.Parameters parameters = mCamera.getParameters();

                final Camera.Size bestPreviewSize = determineBestSize(parameters);
                mPreviewSize = bestPreviewSize;

                parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);

                //设置输入纹理的大小
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mRender.setCameraPreviewSize(mPreviewSize.width, mPreviewSize.height);
                    }
                });

                // Set continuous picture focus, if it's supported
                if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }

                // Lock in the changes
                mCamera.setParameters(parameters);
                setIsCameraReady(true);
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        Log.i("wraith", "setup camera finish");
        return true;
    }

    private Camera.Size determineBestSize(Camera.Parameters parameters) {
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        Camera.Size bestSize = null;
        Camera.Size size;
        float screenWidth = getScreenWidth();
        float screenHeight = getScreenHeight();

        int numOfSizes = sizes.size();
        for (int i = 0; i < numOfSizes; i++) {
            size = sizes.get(i);

            if ((size.width == screenWidth && size.height == screenHeight) ||
                    (size.height == screenWidth && size.width == screenHeight)) {
                bestSize = size;
            }
        }

        if (bestSize == null) {
            return sizes.get(sizes.size() - 1);
        }

        return bestSize;
    }

    private int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    private int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    private int getFrontCameraID(Context context) {
        if (context != null) {
            PackageManager pm = context.getPackageManager();
            if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
                mHasFrontCamera = true;
                Log.i(TAG, "has front camera");
                return Camera.CameraInfo.CAMERA_FACING_FRONT;
            }
            Log.i(TAG, "has not front camera");
            mHasFrontCamera = false;
            return getBackCameraID();
        }
        return -1;
    }

    private int getBackCameraID() {
        return Camera.CameraInfo.CAMERA_FACING_BACK;
    }

    private void stopCameraPreview() {
        try {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mCamera = null;
        }
    }

    private boolean mIsCameraReady = false;

    public boolean resume() {
        mIsCameraReady = setupCamera(mCameraId);
        if (mIsCameraReady) {
            try {
                mOrientationListener = new CameraOrientationListener(getContext());
                mOrientationListener.enable();
            } catch (Exception e) {
                e.printStackTrace();
            }

            onResume();
        }
        return mIsCameraReady;
    }

    public void pause() {
        if (mIsCameraReady) {
            try {
                mOrientationListener.disable();
            } catch (Exception e) {
                e.printStackTrace();
            }

            stopCameraPreview();
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    mRender.notifyPausing();
                }
            });
            onPause();
        }
    }

    /**
     * When orientation changes, onOrientationChanged(int) of the listener will be called
     */
    private static class CameraOrientationListener extends OrientationEventListener {

        private int mCurrentNormalizedOrientation;
        private int mRememberedNormalOrientation;

        public CameraOrientationListener(Context context) {
            super(context, SensorManager.SENSOR_DELAY_NORMAL);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation != ORIENTATION_UNKNOWN) {
                mCurrentNormalizedOrientation = normalize(orientation);
            }
        }

        /**
         * @param degrees Amount of clockwise rotation from the device's natural position
         * @return Normalized degrees to just 0, 90, 180, 270
         */
        private int normalize(int degrees) {
            if (degrees > 315 || degrees <= 45) {
                return 0;
            }

            if (degrees > 45 && degrees <= 135) {
                return 90;
            }

            if (degrees > 135 && degrees <= 225) {
                return 180;
            }

            if (degrees > 225 && degrees <= 315) {
                return 270;
            }

            throw new RuntimeException("The physics as we know them are no more. Watch out for anomalies.");
        }

        public void rememberOrientation() {
            mRememberedNormalOrientation = mCurrentNormalizedOrientation;
        }

        public int getRememberedNormalOrientation() {
            rememberOrientation();
            return mRememberedNormalOrientation;
        }

    }

    /**
     * Connects the SurfaceTexture to the Camera preview output, and starts the preview.
     */
    private void handleSetSurfaceTexture(final SurfaceTexture st) {
        post(new Runnable() {
            @Override
            public void run() {
                Log.i("wraith", "handlerSetSurfaceTexture");
                if (mCamera != null) {
                    synchronized (mCamera) {
                        if (mCamera == null) {
                            return;
                        }

                        if (st != null) {
                            mSurfaceTexture = st;
                            mSurfaceTexture.setOnFrameAvailableListener(WraithCameraView.this);
                        }

                        try {
                            mCamera.setDisplayOrientation(mCurrentDisplayOrietation);
                            mCamera.setPreviewTexture(mSurfaceTexture);
                            mCamera.startPreview();
                            mCamera.cancelAutoFocus();
                            mCamera.getParameters().setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                        } catch (Exception ioe) {
                            ioe.printStackTrace();
                        }
                    }
                }
            }
        });
    }


    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requestRender();
    }


    private void getCameraDisplayOrientation(Context context) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(mCameraId, info);
        int rotation = ((Activity) context).getWindowManager().getDefaultDisplay()
                .getRotation();

        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        mCurrentDisplayOrietation = result - 90;
    }

    public void swapCamera(Context context) {
        stopCameraPreview();
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mCameraId = getBackCameraID();
        } else {
            mCameraId = getFrontCameraID(context);
        }
        setupCamera(mCameraId);
        handleSetSurfaceTexture(null);
    }

    public boolean isFrontCamera() {
        if (mHasFrontCamera == false) {
            return true;
        }

        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return true;
        } else {
            return false;
        }
    }

}

