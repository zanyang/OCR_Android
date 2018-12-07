package com.lzy.ocr;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.theartofdev.edmodo.cropper.CropImageView;
import com.lzy.ocr.camear.CameraPreview;
import com.lzy.ocr.camear.FocusView;
import com.lzy.ocr.utils.RotateSensorUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * 相机页面
 *
 * @author lzy
 * @time 18-11-29 上午11:45
 */
public class TakePhotoActivity extends AppCompatActivity implements CameraPreview.OnCameraStatusListener, SensorEventListener {

    private static final String TAG = "TakePhotoActivity";

    public static final Uri IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

    public static final String PATH = Environment.getExternalStorageDirectory().toString() + "/AndroidMedia/";

    public static final int PICK_IMAGE_CHOOSER_REQUEST_CODE = 201;

    /**
     * 拍照框
     */
    CameraPreview mCameraPreview;

    /**
     * 裁剪框
     */
    CropImageView mCropImageView;

    /**
     * 拍照布局
     */
    RelativeLayout mTakePhotoLayout;

    /**
     * 裁剪布局
     */
    LinearLayout mCropperLayout;

    /**
     * 关闭
     */
    private ImageView btnClose;

    /**
     * 拍照
     */
    private ImageView btnShutter;

    /**
     * 控制闪光灯
     */
    private ImageView btnControlLight;

    /**
     * 系统相册
     */
    private ImageView btnAlbum;

    /**
     * 中间提示文案
     */
    private TextView tvHint;

    /**
     * 裁剪完成
     */
    private ImageView btnStartCropper;

    /**
     * 裁剪关闭
     */
    private ImageView btnCloseCropper;

    /**
     * 旋转图片
     */
    private ImageView btnCropperRotate;

    /**
     * 重力监听
     */
    private RotateSensorUtil sensorUtil;

    private ArrayList<View> rotateViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_take_photo);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        btnClose = (ImageView) findViewById(R.id.btn_close);
        btnShutter = (ImageView) findViewById(R.id.btn_shutter);
        btnAlbum = (ImageView) findViewById(R.id.btn_album);
        tvHint = (TextView) findViewById(R.id.hint);
        btnStartCropper = (ImageView) findViewById(R.id.btn_startcropper);
        btnCloseCropper = (ImageView) findViewById(R.id.btn_closecropper);
        btnCropperRotate = (ImageView) findViewById(R.id.btn_cropper_rotate);
        mTakePhotoLayout = (RelativeLayout) findViewById(R.id.take_photo_layout);
        btnControlLight = (ImageView) findViewById(R.id.contorl_light);

        btnClose.setOnClickListener(onClickListener);
        btnControlLight.setOnClickListener(onClickListener);
        btnShutter.setOnClickListener(onClickListener);
        btnAlbum.setOnClickListener(onClickListener);
        btnStartCropper.setOnClickListener(onCropperListener);
        btnCloseCropper.setOnClickListener(onCropperListener);
        btnCloseCropper.setOnClickListener(onCropperListener);
        btnCropperRotate.setOnClickListener(onCropperListener);

        mCropperLayout = (LinearLayout) findViewById(R.id.cropper_layout);
        mCropImageView = (CropImageView) findViewById(R.id.CropImageView);
        mCropImageView.setGuidelines(CropImageView.Guidelines.ON);

        FocusView focusView = (FocusView) findViewById(R.id.view_focus);
        mCameraPreview = (CameraPreview) findViewById(R.id.cameraPreview);
        mCameraPreview.setFocusView(focusView);
        mCameraPreview.setOnCameraStatusListener(this);

        addViews();
        startAnim();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void startAnim() {
        sensorUtil = new RotateSensorUtil(this, rotateViews);
    }

    /**
     * 添加需要旋转的view
     *
     * @author lzy
     * @time 18-11-29 下午12:02
     */
    private void addViews() {
        rotateViews.add(btnShutter);
        rotateViews.add(btnControlLight);
        rotateViews.add(btnClose);
        rotateViews.add(btnAlbum);
        rotateViews.add(tvHint);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorUtil != null) {
            sensorUtil.unregisterSensor();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * 监听
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_close:
                    finish();
                    break;
                case R.id.btn_shutter:
                    if (mCameraPreview != null) {
                        mCameraPreview.takePicture();
                    }
                    break;
                case R.id.btn_album:
                    Intent intent = new Intent();
                    // 开启Pictures画面Type设定为image
                    intent.setType("image/*");
                    // 使用Intent.ACTION_GET_CONTENT这个Action
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    // 取得相片后返回本画面
                    startActivityForResult(intent, PICK_IMAGE_CHOOSER_REQUEST_CODE);
                    break;
                case R.id.contorl_light:
                    if (mCameraPreview != null) {
                        boolean isFlashOn = mCameraPreview.switchFlashLight();
                        btnControlLight.setImageResource(isFlashOn ? R.drawable.icon_btn_kqzm : R.drawable.icon_btn_kqzm);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 图片裁剪监听
     */
    private View.OnClickListener onCropperListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_closecropper:
                    showTakePhotoLayout();
                    break;
                case R.id.btn_startcropper:
                    Bitmap cropperBitmap = mCropImageView.getCroppedImage();
                    // 系统时间
                    long dateTaken = System.currentTimeMillis();
                    // 图像名称
                    String filename = DateFormat.format("yyyy-MM-dd kk.mm.ss", dateTaken).toString() + ".jpg";
                    Uri uri = insertImage(getContentResolver(), filename, dateTaken, PATH, filename, cropperBitmap, null);

                    Intent intent = new Intent(TakePhotoActivity.this, ShowCropperActivity.class);
                    intent.setData(uri);
                    intent.putExtra("path", PATH + filename);
                    intent.putExtra("width", cropperBitmap.getWidth());
                    intent.putExtra("height", cropperBitmap.getHeight());
                    startActivity(intent);
                    cropperBitmap.recycle();
                    finish();
                    break;
                case R.id.btn_cropper_rotate:
                    mCropImageView.rotateImage(90);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 拍照成功后回调
     * 存储图片并显示截图界面
     *
     * @param data
     */
    @Override
    public void onCameraStopped(byte[] data) {
        Log.i(TAG, "==onCameraStopped==");
        // 创建图像
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        // 系统时间
        long dateTaken = System.currentTimeMillis();
        // 图像名称
        String filename = DateFormat.format("yyyy-MM-dd kk.mm.ss", dateTaken).toString() + ".jpg";
        // 存储图像（PATH目录）
        Uri source = insertImage(getContentResolver(), filename, dateTaken, PATH, filename, bitmap, data);
        // 是否需要对照片旋转
//        bitmap = Utils.rotate(bitmap, 90);
        mCropImageView.setImageUriAsync(source);
//        mCropImageView.setImageBitmap(bitmap);
        showCropperLayout();
    }

    /**
     * 选择相册
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE_CHOOSER_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                if (uri != null) {
                    Log.i(TAG, "uri = " + uri.toString());
                    ContentResolver cr = this.getContentResolver();
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                        mCropImageView.setImageBitmap(bitmap);
                        showCropperLayout();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("Exception", e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * 存储图像并将信息添加入媒体数据库
     */
    private Uri insertImage(ContentResolver cr, String name, long dateTaken,
                            String directory, String filename, Bitmap source, byte[] jpegData) {
        OutputStream outputStream = null;
        String filePath = directory + filename;
        try {
            File dir = new File(directory);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(directory, filename);
            if (file.createNewFile()) {
                outputStream = new FileOutputStream(file);
                if (source != null) {
                    source.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                } else {
                    outputStream.write(jpegData);
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
            return null;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
        ContentValues values = new ContentValues(7);
        values.put(MediaStore.Images.Media.TITLE, name);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
        values.put(MediaStore.Images.Media.DATE_TAKEN, dateTaken);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATA, filePath);
        return cr.insert(IMAGE_URI, values);
    }

    private void showTakePhotoLayout() {
        mTakePhotoLayout.setVisibility(View.VISIBLE);
        mCropperLayout.setVisibility(View.GONE);
    }

    private void showCropperLayout() {
        mTakePhotoLayout.setVisibility(View.GONE);
        mCropperLayout.setVisibility(View.VISIBLE);
        mCameraPreview.start();   //继续启动摄像头
    }


    private float mLastX = 0;

    private float mLastY = 0;

    private float mLastZ = 0;

    private boolean mInitialized = false;


    /**
     * 位移 自动对焦
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        if (!mInitialized) {
            mLastX = x;
            mLastY = y;
            mLastZ = z;
            mInitialized = true;
        }
        float deltaX = Math.abs(mLastX - x);
        float deltaY = Math.abs(mLastY - y);
        float deltaZ = Math.abs(mLastZ - z);

        if (deltaX > 0.8 || deltaY > 0.8 || deltaZ > 0.8) {
            mCameraPreview.setFocus();
        }
        mLastX = x;
        mLastY = y;
        mLastZ = z;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
