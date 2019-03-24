package com.tsitsing.translation;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.GeneralParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.WordSimple;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


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
    private static final int MESSAGE_WHAT = 100;

    private static final String TAG = "MyTAG";

    private File file;
    private Uri photoUri;
    private Button button;
    private String path = "";
    private BasicCallBack callBack, translateCallBack;
    private Bitmap bitmap;
    private String ocrResult = "";
    private String translateResult = "hehe";
    private boolean hasGotToken = false;
    private String from = "zh";
    private String to = "en";

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

        if (PermissionUtil.isOwnPermission(this.getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Log.i("write", "own");
        } else {
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

        initAccessToken();

        return view;
    }

    //handler可能会存在内存泄露，稍后解决
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_WHAT:
//                    Log.d(TAG, "gotMsg");
//                    Log.d("handled", msg.obj.toString());
                    try {
                        JSONObject jsonObject = new JSONObject(msg.obj.toString());
                        JSONArray jsonArray  = jsonObject.getJSONArray("words_result");
                        if (jsonArray.length() != 0) {
                            ocrResult = "";//全局变量，先将其置空
                            for (int i = 0 ; i<jsonArray.length() ; i++) {
                                JSONObject extractObject = new JSONObject(jsonArray.getJSONObject(i).toString());
                                ocrResult += (" " + extractObject.getString("words"));
                            }
                        }
                        Log.d("ocrResult", ocrResult);
                        final TranslateText translateText = new TranslateText();
                        //回调方法，translateText 完成时会调用
                        translateCallBack = new BasicCallBack() {
                            @Override
                            public void doSuccess() {

                            }

                            @Override
                            public void doSuccess(String string) {
                                translateResult = string;
                                TextView textView = getView().findViewById(R.id.textView);
                                textView.setText(string);
                                Log.d("translateResult", translateResult);
                            }

                            @Override
                            public void doFail() {

                            }
                        };
                        //其中传入一个回调对象translateCallBack
                        translateText.transRequest(translateCallBack,getContext(), ocrResult, from, to);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };


    //进行图片文字识别
    private void sendOrcResult() {
        Log.d(TAG, "sendORCResult");
        new Thread() {
            @Override
            public void run() {
                Log.d(TAG, "ThreadRun");
//                OcrClient client = OcrClientFactory.create(getContext(), "20190211000265342", "P0tTPdrqOvttQqAeRqhF");
//                OcrResult result = client.getOcrResult("Language.ZH", "Language.EN", bitmap);
                //output information
//                if (result.getSumDst() != null) {
//                    Log.d("src", result.getSumDst());
//                } else {
//                    Log.d("TAG", "dst is null");
//                }
                recognizeChar(path, getContext(), new ServiceListener() {
                    @Override
                    public void onResult(String result) {
                        Log.d(TAG, result);
                        Message message = handler.obtainMessage(MESSAGE_WHAT);
                        message.obj = result;
                        handler.sendMessage(message);
                    }
                });
            }
        }.start();
    }

    private void selectPicture() {
        callBack = new BasicCallBack() {
            @Override
            public void doSuccess() {
//                String picString = imageToBase64(path);
//                Log.d(TAG, picString);
                Log.d(TAG, "doSuccess");
                sendOrcResult();
            }

            @Override
            public void doFail() {

            }

            @Override
            public void doSuccess(String string) {

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
                        bitmap = BitmapFactory.decodeFile(path);
                        if (callBack != null) {
                            callBack.doSuccess();
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

    /**
     * 以license文件方式初始化
     */
    private void initAccessToken() {
        OCR.getInstance(this.getActivity()).initAccessToken(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken accessToken) {
                String token = accessToken.getAccessToken();
                hasGotToken = true;
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
            }
        }, this.getContext());
    }

    /**
     * 自定义license的文件路径和文件名称，以license文件方式初始化
     */
    private void initAccessTokenLicenseFile() {
        OCR.getInstance(this.getActivity()).initAccessToken(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken accessToken) {
                String token = accessToken.getAccessToken();
                hasGotToken = true;
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
            }
        }, "aip.license", this.getContext());
    }

    private boolean checkTokenStatus() {
        if (!hasGotToken) {
            Toast.makeText(this.getActivity(), "token还未成功获取", Toast.LENGTH_LONG).show();
        }
        return hasGotToken;
    }

    /**
     * 将图片转换成Base64编码的字符串
     */
    public static String imageToBase64(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        InputStream is = null;
        byte[] data = null;
        String result = null;
        try {
            is = new FileInputStream(path);
            //创建一个字符流大小的数组。
            data = new byte[is.available()];
            //写入数组
            is.read(data);
            //用默认的编码格式进行编码
            result = Base64.encodeToString(data, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return result;
    }

    //文字识别
    public static void recognizeChar (String path, Context context, final ServiceListener listener) {
        GeneralParams params = new GeneralParams();
        params.setDetectDirection(true);
        params.setDetectDirection(true);
        params.setVertexesLocation(true);
        params.setImageFile(new File(path));

        OCR.getInstance(context).recognizeAccurateBasic(params, new OnResultListener<GeneralResult>() {
            @Override
            public void onResult(GeneralResult generalResult) {
                StringBuilder sb = new StringBuilder();
                for (WordSimple wordSimple : generalResult.getWordList()) {
                    // wordSimple不包含位置信息
                    WordSimple word = wordSimple;
                    sb.append(word.getWords());
                    sb.append("\n");
                }
                listener.onResult(generalResult.getJsonRes());
            }

            @Override
            public void onError(OCRError ocrError) {
                listener.onResult(ocrError.getMessage());
            }
        });
    }

    interface ServiceListener {
        public void onResult(String result);
    }
}
