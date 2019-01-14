package com.sancsoft.uvccameralibrary.activities;

import android.os.Bundle;
import android.view.Surface;
import android.widget.ImageButton;
import android.widget.Toast;

import com.sancsoft.uvccameralibrary.R;
import com.sancsoft.uvccameralibrary.common.BaseActivity;
import com.sancsoft.uvccameralibrary.usb.CameraDialog;
import com.sancsoft.uvccameralibrary.usb.USBMonitor;
import com.sancsoft.uvccameralibrary.usb.UVCCamera;

public class CameraActivity extends BaseActivity implements CameraDialog.CameraDialogParent {

    protected final Object mSync = new Object();
    // for accessing USB and USB camera
    protected USBMonitor mUSBMonitor;
    protected UVCCamera mUVCCamera;
    // for open&start / stop&close camera preview
    protected ImageButton mCameraButton;
    protected Surface mPreviewSurface;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUSBMonitor.register();
        synchronized (mSync) {
            if (mUVCCamera != null) {
                mUVCCamera.startPreview();
            }
        }
    }

    @Override
    protected void onStop() {
        synchronized (mSync) {
            if (mUVCCamera != null) {
                mUVCCamera.stopPreview();
            }
            if (mUSBMonitor != null) {
                mUSBMonitor.unregister();
            }
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        synchronized (mSync) {
            if (mToast != null) {
                mToast.cancel();
                mToast = null;
            }
        }
        mCameraButton = null;
        super.onDestroy();
    }

    private Toast mToast;

    private synchronized void releaseCamera() {
        synchronized (mSync) {
            if (mUVCCamera != null) {
                try {
                    mUVCCamera.setStatusCallback(null);
                    mUVCCamera.setButtonCallback(null);
                    mUVCCamera.close();
                    mUVCCamera.destroy();
                } catch (final Exception e) {
                    //
                }
                mUVCCamera = null;
            }
            if (mPreviewSurface != null) {
                mPreviewSurface.release();
                mPreviewSurface = null;
            }
        }
    }

    /**
     * to access from CameraDialog
     * @return
     */
    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (canceled) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // FIXME
                }
            }, 0);
        }
    }

    // if you need frame data as byte array on Java side, you can use this callback method with UVCCamera#setFrameCallback
    // if you need to create Bitmap in IFrameCallback, please refer following snippet.
/*	final Bitmap bitmap = Bitmap.createBitmap(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, Bitmap.Config.RGB_565);
	private final IFrameCallback mIFrameCallback = new IFrameCallback() {
		@Override
		public void onFrame(final ByteBuffer frame) {
			frame.clear();
			synchronized (bitmap) {
				bitmap.copyPixelsFromBuffer(frame);
			}
			mImageView.post(mUpdateImageTask);
		}
	};

	private final Runnable mUpdateImageTask = new Runnable() {
		@Override
		public void run() {
			synchronized (bitmap) {
				mImageView.setImageBitmap(bitmap);
			}
		}
	}; */
}
