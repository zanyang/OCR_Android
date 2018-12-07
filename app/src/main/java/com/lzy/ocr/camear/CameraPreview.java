package com.lzy.ocr.camear;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.lzy.ocr.R;
import com.lzy.ocr.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义相机
 *
 * @author lzy
 * @time 18-11-29 上午11:02
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.AutoFocusCallback {

    private int mViewWidth = 0;

    private int mViewHeight = 0;

    /**
     * 照相机状态改变的监听
     */
    private OnCameraStatusListener listener;

    private SurfaceHolder holder;

    private Camera mCamera;

    private FocusView mFocusView;

    /**
     * 创建一个PictureCallback对象，并实现其中的onPictureTaken方法
     */
    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {

        /**
         * 该方法用于处理拍摄后的照片数据
         * @param data 图片的字节码
         * @param camera 摄像头实例
         */
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // 停止照片拍摄
            try {
                camera.stopPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 调用结束事件
            if (null != listener) {
                listener.onCameraStopped(data);
            }
        }
    };

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        // 获得SurfaceHolder对象
        holder = getHolder();
        // 指定用于捕捉拍照事件的SurfaceHolder.Callback对象
        holder.addCallback(this);
        // 设置SurfaceHolder对象的类型
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // 设置固定屏幕
        holder.setKeepScreenOn(true);

        setOnTouchListener(onTouchListener);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!Utils.checkCameraHardware(getContext())) {
            Toast.makeText(getContext(), R.string.open_camera_fail, Toast.LENGTH_SHORT).show();
            return;
        }
        // 获取相机实例
        mCamera = getCameraInstance();
        setParameters(holder);

    }

    /**
     * 设置相机参数
     *
     * @param holder
     * @author lzy
     * @time 18-11-29 上午10:56
     */
    private void setParameters(SurfaceHolder holder) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(holder);

                Camera.Parameters parameters = mCamera.getParameters();
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    //竖屏拍照时，需要设置旋转90度，否者看到的相机预览方向和界面方向不相同
                    mCamera.setDisplayOrientation(90);
                    parameters.setRotation(90);
                } else {
                    mCamera.setDisplayOrientation(0);
                    parameters.setRotation(0);
                }
                Camera.Size bestSize = getBestSize(parameters.getSupportedPreviewSizes());
                if (bestSize != null) {
                    parameters.setPreviewSize(bestSize.width, bestSize.height);
                    parameters.setPictureSize(bestSize.width, bestSize.height);
                } else {
                    parameters.setPreviewSize(1920, 1080);
                    parameters.setPictureSize(1920, 1080);
                }
                mCamera.setParameters(parameters);
                mCamera.startPreview();
                setFocus();//首次对焦
            } catch (Exception e) {
                try {
                    Camera.Parameters parameters = mCamera.getParameters();
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        //竖屏拍照时，需要设置旋转90度，否者看到的相机预览方向和界面方向不相同
                        mCamera.setDisplayOrientation(90);
                        parameters.setRotation(90);
                    } else {
                        mCamera.setDisplayOrientation(0);
                        parameters.setRotation(0);
                    }
                    mCamera.setParameters(parameters);
                    mCamera.startPreview();
                    setFocus();//首次对焦
                } catch (Exception e1) {
                    e.printStackTrace();
                    mCamera = null;
                }
            }
        }
    }

    /**
     * Android相机的预览尺寸都是4:3或者16:9，这里遍历所有支持的预览尺寸，得到16:9的最大尺寸，保证成像清晰度
     *
     * @return 最佳尺寸
     */
    private Camera.Size getBestSize(List<Camera.Size> sizes) {
        Camera.Size bestSize = null;
        for (Camera.Size size : sizes) {
            if ((float) size.width / (float) size.height == 16.0f / 9.0f) {
                if (bestSize == null) {
                    bestSize = size;
                } else {
                    if (size.width > bestSize.width) {
                        bestSize = size;
                    }
                }
            }
        }
        return bestSize;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // 释放手机摄像头
        stop();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }


    /**
     * 因为设置了固定屏幕方向，所以在实际使用中不会触发这个方法
     */
    @Override
    public void surfaceChanged(final SurfaceHolder holder, int format, int w, int h) {

    }


    /**
     * 点击显示焦点区域并对焦.
     */
    OnTouchListener onTouchListener = new OnTouchListener() {
        @SuppressWarnings("deprecation")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int width = mFocusView.getWidth();
                int height = mFocusView.getHeight();
                mFocusView.setX(event.getX() - (width / 2));
                mFocusView.setY(event.getY() - (height / 2));
                mFocusView.beginFocus();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                focusOnTouch(event);
            }
            return true;
        }
    };


    /**
     * 获取摄像头实例
     */
    private Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;

    }


    /**
     * 进行拍照，并将拍摄的照片传入PictureCallback接口的onPictureTaken方法
     */
    public void takePicture() {
        if (mCamera != null) {
            try {
                mCamera.takePicture(null, null, pictureCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 设置监听事件
     */
    public void setOnCameraStatusListener(OnCameraStatusListener listener) {
        this.listener = listener;
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {

    }

    public void start() {
        if (mCamera != null) {
            mCamera.startPreview();
        }
    }

    public void stop() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }


    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        mViewWidth = MeasureSpec.getSize(widthSpec);
        mViewHeight = MeasureSpec.getSize(heightSpec);
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(mViewWidth, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(mViewHeight, MeasureSpec.EXACTLY));
    }


    /**
     * 设置焦点和测光区域
     */
    public void focusOnTouch(MotionEvent event) {

        int[] location = new int[2];
        RelativeLayout relativeLayout = (RelativeLayout) getParent();
        relativeLayout.getLocationOnScreen(location);

        Rect focusRect = Utils.calculateTapArea(mFocusView.getWidth(),
            mFocusView.getHeight(), 1f, event.getRawX(), event.getRawY(),
            location[0], location[0] + relativeLayout.getWidth(), location[1],
            location[1] + relativeLayout.getHeight());
        Rect meteringRect = Utils.calculateTapArea(mFocusView.getWidth(),
            mFocusView.getHeight(), 1.5f, event.getRawX(), event.getRawY(),
            location[0], location[0] + relativeLayout.getWidth(), location[1],
            location[1] + relativeLayout.getHeight());

        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

        if (parameters.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
            focusAreas.add(new Camera.Area(focusRect, 1000));

            parameters.setFocusAreas(focusAreas);
        }

        if (parameters.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
            meteringAreas.add(new Camera.Area(meteringRect, 1000));

            parameters.setMeteringAreas(meteringAreas);
        }

        try {
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mCamera.autoFocus(this);
    }


    /**
     * 设置聚焦的图片
     *
     * @param focusView 圆形聚焦框
     */
    public void setFocusView(FocusView focusView) {
        this.mFocusView = focusView;
    }


    /**
     * 设置自动聚焦，并且聚焦的圈圈显示在屏幕中间位置
     */
    public void setFocus() {
        if (!mFocusView.isFocusing()) {
            try {
                mCamera.autoFocus(this);
                mFocusView.setX((Utils.getWidthInPx(getContext()) - mFocusView.getWidth()) / 2);
                mFocusView.setY((Utils.getHeightInPx(getContext()) - mFocusView.getHeight()) / 2);
                mFocusView.beginFocus();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 开关闪光灯
     */
    public boolean switchFlashLight() {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
                return true;
            } else {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(parameters);
                return false;
            }
        }
        return false;
    }


    /**
     * 相机拍照监听接口
     */
    public interface OnCameraStatusListener {
        /**
         * 相机拍照结束事件
         *
         * @param data 拍照后的回调
         */
        void onCameraStopped(byte[] data);
    }
}
