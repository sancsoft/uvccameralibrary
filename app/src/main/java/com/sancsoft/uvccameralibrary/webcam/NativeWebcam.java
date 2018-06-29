package com.sancsoft.uvccameralibrary.webcam;

import java.io.File;

import android.graphics.Bitmap;
import android.util.Log;

public class NativeWebcam implements Webcam {

    private static String TAG = "NativeWebcam";
    private static final int DEFAULT_IMAGE_WIDTH = 640;
    private static final int DEFAULT_IMAGE_HEIGHT = 480;

    private Bitmap mBitmap;
    private int mWidth;
    private int mHeight;

    private native int startCamera(String deviceName, int width, int height);
    private native void processCamera();
    private native boolean cameraAttached();
    private native void stopCamera();
    private native void loadNextFrame(Bitmap bitmap);

    static {
        System.loadLibrary("webcam");
    }

    public NativeWebcam(String deviceName, int width, int height) {
        mWidth = width;
        mHeight = height;
        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        connect(deviceName, mWidth, mHeight);
    }

    public NativeWebcam(int width, int height) {
        mWidth = width;
        mHeight = height;
        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        connect(mWidth, mHeight);
    }

    public NativeWebcam(String deviceName) {
        this(deviceName, DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);
    }

    public NativeWebcam() {
        this(DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);
    }

    private void connect(String deviceName, int width, int height) {
        boolean deviceReady = true;

        File deviceFile = new File(deviceName);
        if(deviceFile.exists()) {
            if(!deviceFile.canRead()) {
                Log.d(TAG, "Insufficient permissions on " + deviceName +
                        " -- does the app have the CAMERA permission?");
                deviceReady = false;
            }
        } else {
            Log.w(TAG, deviceName + " does not exist");
            deviceReady = false;
        }

        if(deviceReady) {
            Log.i(TAG, "Preparing camera with device name " + deviceName);
            startCamera(deviceName, width, height);
        }
    }

    private void connect(int width, int height) {
        boolean scanning = true;
        int index = 0;
        File deviceFile;
        while (scanning) {
            String deviceName = "/dev/video" + String.valueOf(index++);
            if (index == 99) {
                index = 0;
            }
            deviceFile = new File(deviceName);
            if (deviceFile.exists()) {
                if (!deviceFile.canRead()) {
                    Log.d(TAG, "Insufficient permissions on " + deviceName +
                            " -- does the app have the CAMERA permission?");
                    continue;
                }
            } else {
                continue;
            }

            Log.i(TAG, "Preparing camera with device name " + deviceName);
            int result = startCamera(deviceName, width, height);
            if (result == 0) {
                scanning = false;
            } else {
                Log.e(TAG, "Error preparing camera: " + String.valueOf(result));
            }
        }

    }

    public Bitmap getFrame() {
        loadNextFrame(mBitmap);
        return mBitmap;
    }

    public void stop() {
        stopCamera();
    }

    public boolean isAttached() {
        return cameraAttached();
    }
}
