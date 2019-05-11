package com.tsitsing.translation;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.GeneralParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.WordSimple;
import com.tsitsing.translation.animation.PPTVLoading;
import com.tsitsing.translation.interfaces.BasicCallBack;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 */
public class PictureFragment extends Fragment {

    private static final int CAMERA_AND_WRITE_REQUEST = 2;
    private static final int REQUEST_CROP_IMAGE = 3;
    private static final int RESULT_LOAD_IMAGE = 10;
    private static final int TAKE_PHOTO = 20;
    private static final int MESSAGE_WHAT = 100;
    private final String[] permissionArray = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private Spinner spinnerSource,spinnerDest;

    private static final String TAG = "MyTAG";

    private File file;
    private Uri photoUri;
    private String path = "";
    private BasicCallBack callBack;
    private String ocrResult = "";
    private boolean hasGotToken = false;
    private String from;
    private String to;
    private Bitmap bitmap;
    private PPTVLoading pptvLoading;


    public PictureFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_picture, container, false);
        spinnerSource = view.findViewById(R.id.spinner_pic_source);
        spinnerDest = view.findViewById(R.id.spinner_pic_dest);

        //用于存放简单数据
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, getData());
        final ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<>(getContext(),R.layout.spinner_item,getData2());
        spinnerSource.setAdapter(arrayAdapter);
        spinnerDest.setAdapter(arrayAdapter2);
        //在指定位置弹出
        spinnerSource.setDropDownHorizontalOffset(50);
        spinnerSource.setDropDownVerticalOffset(110);
        spinnerDest.setDropDownHorizontalOffset(1030);
        spinnerDest.setDropDownVerticalOffset(110);

        //设置源语言选择监听事件
        spinnerSource.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SelectLanguage selectFrom = new SelectLanguage();
                from = selectFrom.getLangCode(parent.getItemAtPosition(position).toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        //设置目标语言选择监听事件
        spinnerDest.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SelectLanguage selectTo = new SelectLanguage();
                to = selectTo.getLangCode(parent.getItemAtPosition(position).toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        //交换源语言与目标语言
        ImageView imgSwitch = view.findViewById(R.id.img_pic_switch);
        imgSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //先获取当前选中的item
                String srcSelected = (String) spinnerSource.getSelectedItem();
                String dstSelected = (String) spinnerDest.getSelectedItem();
                //通过适配器获取对应item的位置
                int srcTarget = arrayAdapter.getPosition(dstSelected);
                int dstTarget = arrayAdapter2.getPosition(srcSelected);
                if (srcTarget != -1 && dstTarget != -1) {//值为-1时说明不存在此item
                    //根据位置重新设定当前spinner的选择
                    spinnerSource.setSelection(srcTarget, true);
                    spinnerDest.setSelection(dstTarget, true);
                } else {
                    Toast.makeText(getContext(), R.string.toast_canNotSwitch, Toast.LENGTH_SHORT).show();
                }
            }
        });
        //申请权限
        if (!(PermissionUtil.isOwnPermission(getActivity(), Manifest.permission.CAMERA)) ||
                !(PermissionUtil.isOwnPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
            PermissionUtil.requestPermission(getActivity(), permissionArray, CAMERA_AND_WRITE_REQUEST);
        }
        ImageView imgPicUpload = view.findViewById(R.id.im_pic_upload);
        imgPicUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPicture();
            }
        });
        //配置OCR接口token
        initAccessToken();
        return view;
    }

    private List<String> getData(){
        List<String> list = new ArrayList<>();
        list.add("英文");
        list.add("中文");
        list.add("日语");
        list.add("法语");
        list.add("韩语");
        list.add("繁体");
        return list;
    }

    private List<String> getData2(){
        List<String> list = new ArrayList<String>();
        list.add("中文");
        list.add("英文");
        list.add("日语");
        list.add("法语");
        list.add("韩语");
        list.add("繁体");
        return list;
    }

    //handler可能会存在内存泄露，稍后解决
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //接受handler返回的信息
                case MESSAGE_WHAT:
                    try {
                        JSONObject jsonObject = new JSONObject(msg.obj.toString());
                        JSONArray jsonArray = jsonObject.getJSONArray("words_result");
                        if (jsonArray.length() != 0) {
                            ocrResult = "";//全局变量，先将其置空
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject extractObject = new JSONObject(jsonArray.getJSONObject(i).toString());
                                //创建识别结果字符串
                                ocrResult += (" " + extractObject.getString("words"));
                            }
                        }
                        Log.d("ocrResult", ocrResult);
                        final TranslateText translateText = new TranslateText();
                        //回调方法，translateText 翻译完成时会调用
                        BasicCallBack translateCallBack = new BasicCallBack() {
                            @Override
                            public void doSuccess() {

                            }
                            //执行回调操作
                            @Override
                            public void doSuccess(String translated) {
                                TextView textViewOCRResult = getView().findViewById(R.id.edit_OCR_result);
                                TextView textViewTranslateResult = getView().findViewById(R.id.edit_translate_result);
                                ImageView imageView = getView().findViewById(R.id.im_pic_upload);
                                pptvLoading.setVisibility(View.INVISIBLE);
                                //翻译完成，设置背景图片
                                imageView.setImageBitmap(bitmap);
                                TextView tvPicUpload = getView().findViewById(R.id.tv_pic_upload);
                                tvPicUpload.setVisibility(View.INVISIBLE);
                                textViewOCRResult.setText(ocrResult);
                                textViewTranslateResult.setText(translated);
                            }

                            @Override
                            public void doFail() {

                            }
                        };
                        //其中传入一个回调对象translateCallBack
                        translateText.transRequest(translateCallBack, getContext(), ocrResult, from, to);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };


    //进行图片文字识别
    private void sendToOCR() {
        //创建一个新线程进行文字识别
        new Thread() {
            @Override
            public void run() {
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
        pptvLoading = getView().findViewById(R.id.PPTVLoading);
        //裁剪完成调用得到回调方法
        callBack = new BasicCallBack() {
            @Override
            public void doSuccess() {
                //设置等待动画
                pptvLoading.setVisibility(View.VISIBLE);
                //开始识别
                sendToOCR();
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
            //从相册选择
            case RESULT_LOAD_IMAGE:
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        startPhotoZoom(uri);
                    }
                }
                break;
            //拍照选择
            case TAKE_PHOTO:
                if (file != null && file.exists())
                    startPhotoZoom(photoUri);
                break;
            //裁剪
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
        }
    }

    //通过拍照选择图片
    public void takePhoto() {
        try {
            Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            String sdcardState = Environment.getExternalStorageState();
            String sdcardPathDir = Environment.getExternalStorageDirectory().getPath() + "/Tsitsing/";
            Log.d("sdcardPath", sdcardPathDir);
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
                startActivityForResult(openCameraIntent, TAKE_PHOTO);
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
        int height = getResources().getDisplayMetrics().heightPixels;

        final PopupWindow popupWindow = new PopupWindow(view, width, height);
        popupWindow.setFocusable(true);
        //popupWindow.setOutsideTouchable(true);//点击外部消失


        buttonAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
                popupWindow.dismiss();
            }
        });

        buttonTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
                popupWindow.dismiss();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
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

    //文字识别
    public static void recognizeChar(String path, Context context, final ServiceListener listener) {
        GeneralParams params = new GeneralParams();
        params.setDetectDirection(true);
        params.setDetectDirection(true);
        params.setVertexesLocation(true);
        params.setImageFile(new File(path));

        //获取实例
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

    //文字识别监听接口
    interface ServiceListener {
        void onResult(String result);
    }
}
