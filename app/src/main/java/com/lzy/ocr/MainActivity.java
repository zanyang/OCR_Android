package com.lzy.ocr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSIONS_REQUEST_CAMERA = 454;

    static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;

    private Button imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (Button) findViewById(R.id.btn_camera);
        imageView.setOnClickListener(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                deepFile("tessdata");
            }
        }).start();

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_camera:
                checkSelfPermission();
                break;
        }
    }

    /**
     * 将assets中的文件复制出
     *
     * @param path
     */
    public void deepFile(String path) {
        String newPath = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
            newPath = getExternalFilesDir(null) + "/";
        }
        try {
            String str[] = getAssets().list(path);
            if (str.length > 0) {//如果是目录
                File file = new File(newPath + path);
                file.mkdirs();
                for (String string : str) {
                    path = path + "/" + string;
                    deepFile(path);
                    path = path.substring(0, path.lastIndexOf('/'));//回到原来的path
                }
            } else {//如果是文件
                InputStream is = getAssets().open(path);
                FileOutputStream fos = new FileOutputStream(new File(newPath + path));
                byte[] buffer = new byte[1024];
                int count = 0;
                while (true) {
                    count++;
                    int len = is.read(buffer);
                    if (len == -1) {
                        break;
                    }
                    fos.write(buffer, 0, len);
                }
                is.close();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(this, TakePhotoActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "请开启摄像头权限", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    /**
     * 检查权限
     */
    void checkSelfPermission() {
        if (ContextCompat.checkSelfPermission(this, PERMISSION_CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{PERMISSION_CAMERA}, PERMISSIONS_REQUEST_CAMERA);
        } else {
            Intent intent = new Intent(this, TakePhotoActivity.class);
            startActivity(intent);
        }
    }
}
