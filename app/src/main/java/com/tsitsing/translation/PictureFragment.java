package com.tsitsing.translation;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;

import com.baidu.translate.ocr.OcrClient;
import com.baidu.translate.ocr.OcrClientFactory;
import com.baidu.translate.ocr.entity.OcrResult;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class PictureFragment extends Fragment {

    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int READ_REQUEST_CODE = 2;
    private static final int REQUEST_CROP_IMAGE = 3;
    private static final int WRITE_REQUEST_CODE = 4;
    private static final int RESULT_LOAD_IMAGE = 10;
    private static final int TAKE_PICTURE = 20;
    private File file;
    private Uri photoUri;
    Button button;
    private String path = "";
    PictureCallBack callBack;

    public PictureFragment() {
        // Required empty public constructor
    }

    //判断权限是否已授权
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("request", "success");
                } else {
                    Log.i("request", "failed");
                }
            }
            break;

            case READ_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("request", "success");
                } else {
                    Log.i("request", "failed");
                }
            }
            break;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_picture, container, false);

        if (PermissionUtil.isOwnPermission(this.getActivity(), Manifest.permission.CAMERA)) {
            //如果已经拥有相机权限
            Log.i("camera", "own");
        } else {
            //没有该权限，需要进行请求
            Log.i("camera", "not");
            PermissionUtil.requestPermission(this.getActivity(), Manifest.permission.CAMERA, CAMERA_REQUEST_CODE);
        }

        if (PermissionUtil.isOwnPermission(this.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //如果读已经拥有权限
            Log.i("read", "own");
        } else {
            //没有该权限，需要进行请求
            PermissionUtil.requestPermission(this.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE, READ_REQUEST_CODE);
        }

        if (PermissionUtil.isOwnPermission(this.getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            Log.i("write", "own");
        }
        else {
            Log.i("write", "not");
            PermissionUtil.requestPermission(this.getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_REQUEST_CODE);
        }

        button = view.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPicture();
            }
        });
        return view;
    }

    private void selectPicture() {
        callBack = new PictureCallBack() {
            @Override
            public void doSuccess(Bitmap bitmap) {
                OcrClient client = OcrClientFactory.create(getContext(), "20190211000265342", "P0tTPdrqOvttQqAeRqhF");
                OcrResult result = client.getOcrResult("Language.ZH", "Language.EN", bitmap);
                if (result.getContents() != null) {
                    String content = result.getContents().toString();
                    Log.d("content", content);
//                Log.d("orcResult", result.toString());
                }else{
                    Log.d("content", "is null");
                }
            }

            @Override
            public void doFail() {

            }
        };

        showPopupWindow();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_LOAD_IMAGE:
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        startPhotoZoom(uri);
                    }
                }
                break;
            case REQUEST_CROP_IMAGE:
                if (resultCode == RESULT_OK && data != null) {
                    if (path != null && path.length() != 0) {
                        Bitmap bitmap = BitmapFactory.decodeFile(path);
                        if (callBack != null) {
                            callBack.doSuccess(bitmap);
                        }
                    }
                }
                break;
            case TAKE_PICTURE:
                if (file != null && file.exists())
                    startPhotoZoom(photoUri);
                break;
        }
    }

    public void photo() {

        try {
            Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            String sdcardState = Environment.getExternalStorageState();
            String sdcardPathDir = Environment.getExternalStorageDirectory().getPath() + "/tempImage/";
            file = null;
            if (Environment.MEDIA_MOUNTED.equals(sdcardState)) {
                // 有sd卡，是否有myImage文件夹
                File fileDir = new File(sdcardPathDir);
                if (!fileDir.exists()) {
                    fileDir.mkdirs();
                }
                // 是否有headImg文件
                long l = System.currentTimeMillis();
                file = new File(sdcardPathDir + l + ".JPEG");
            }
            if (file != null) {
                path = file.getPath();

                photoUri = Uri.fromFile(file);
                if (Build.VERSION.SDK_INT >= 25) {
                    photoUri = FileProvider.getUriForFile(getContext(), "com.tsitsing.translation.fileProvider", file);
                } else {
                    photoUri = Uri.fromFile(file);
                }
                openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(openCameraIntent, TAKE_PICTURE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //裁剪方法
    private void startPhotoZoom(Uri uri) {
        String address = new SimpleDateFormat("yyyyMMddhhmmss", Locale.CHINA).format(new Date());
        if (FileUtils.isNotFileExist("")) {
            try {
                FileUtils.createSDDir("");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Uri copyUri = Uri.parse("file:///sdcard/Tsitsing/" + address + ".JPEG");
        final Intent intent = new Intent("com.android.camera.action.CROP");
        //进行裁剪参数传递
        intent.setDataAndType(uri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra("crop", true);
        intent.putExtra("scale", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, copyUri);//输出路径
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());//输出格式
        intent.putExtra("noFaceDetection", false);//不启动人脸识别
        intent.putExtra("return-data", false);
        intent.putExtra("fileurl", FileUtils.SDCARD_PATH + address + ".JPEG");
        path = FileUtils.SDCARD_PATH + address + ".JPEG";
        startActivityForResult(intent, REQUEST_CROP_IMAGE);
    }

    //弹出选择窗口
    private void showPopupWindow() {
        View view = View.inflate(getActivity(), R.layout.pop_window, null);

        Button buttonAlbum = view.findViewById(R.id.button_album);
        Button buttonTakePhoto = view.findViewById(R.id.button_take_photo);
        Button buttonCancel = view.findViewById(R.id.button_cancel);

        //获取屏幕宽高
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels / 3;

        final PopupWindow popupWindow = new PopupWindow(view, width, height);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);//点击外部消失

        buttonAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
                popupWindow.dismiss();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        buttonTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photo();
            }
        });

        //popupWindow消失屏幕变为不透明
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
                lp.alpha = 1.0f;
                getActivity().getWindow().setAttributes(lp);
            }
        });
        //popWindow出现，屏幕变为半透明
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.alpha = 0.5f;
        getActivity().getWindow().setAttributes(lp);
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 50);
    }
}
