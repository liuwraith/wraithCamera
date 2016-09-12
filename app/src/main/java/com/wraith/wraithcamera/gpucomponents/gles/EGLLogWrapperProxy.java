package com.wraith.wraithcamera.gpucomponents.gles;

import javax.microedition.khronos.egl.EGL11;

public class EGLLogWrapperProxy  {
    private static String getHex(int value) {
        return "0x" + Integer.toHexString(value);
    }

    public static String getErrorString(int error) {
        switch (error) {
            case EGL11.EGL_SUCCESS:
                return "EGL_SUCCESS";
            case EGL11.EGL_NOT_INITIALIZED:
                return "EGL_NOT_INITIALIZED";
            case EGL11.EGL_BAD_ACCESS:
                return "EGL_BAD_ACCESS";
            case EGL11.EGL_BAD_ALLOC:
                return "EGL_BAD_ALLOC";
            case EGL11.EGL_BAD_ATTRIBUTE:
                return "EGL_BAD_ATTRIBUTE";
            case EGL11.EGL_BAD_CONFIG:
                return "EGL_BAD_CONFIG";
            case EGL11.EGL_BAD_CONTEXT:
                return "EGL_BAD_CONTEXT";
            case EGL11.EGL_BAD_CURRENT_SURFACE:
                return "EGL_BAD_CURRENT_SURFACE";
            case EGL11.EGL_BAD_DISPLAY:
                return "EGL_BAD_DISPLAY";
            case EGL11.EGL_BAD_MATCH:
                return "EGL_BAD_MATCH";
            case EGL11.EGL_BAD_NATIVE_PIXMAP:
                return "EGL_BAD_NATIVE_PIXMAP";
            case EGL11.EGL_BAD_NATIVE_WINDOW:
                return "EGL_BAD_NATIVE_WINDOW";
            case EGL11.EGL_BAD_PARAMETER:
                return "EGL_BAD_PARAMETER";
            case EGL11.EGL_BAD_SURFACE:
                return "EGL_BAD_SURFACE";
            case EGL11.EGL_CONTEXT_LOST:
                return "EGL_CONTEXT_LOST";
            default:
                return getHex(error);
        }
    }
}
