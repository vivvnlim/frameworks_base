
package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class TorchButton extends ImageView implements OnClickListener {

    protected static final String TAG = "TorchButton";
    private boolean isActive = false;
    Handler mHandler = new Handler();
    private Camera mCamera;

    private Context mContext;

    public TorchButton(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public TorchButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (isActive) {
            mHandler.post(mTorchStopTask);
            isActive = false;
        } else {
            mHandler.post(mTorchStartTask);
            isActive = true;
        }
    }

    private Runnable mTorchStartTask = new Runnable() {
        public void run() {
            try {
                mCamera = Camera.open();
            } catch (RuntimeException e) {
                Log.i(TAG, "Camera won't open!", e);
                return;
            }
            if (mCamera != null) {
                Parameters params = mCamera.getParameters();
                params.setFlashMode(Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(params);
            }
        }
    };

    private Runnable mTorchStopTask = new Runnable() {

        public void run() {

            if (mCamera != null) {
                Parameters params = mCamera.getParameters();
                params.setFlashMode(Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(params);
                mCamera.release();
            }

        }
    };

}
