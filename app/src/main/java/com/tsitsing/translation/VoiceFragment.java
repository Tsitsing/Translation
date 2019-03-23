package com.tsitsing.translation;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.translate.asr.OnRecognizeListener;
import com.baidu.translate.asr.TransAsrClient;
import com.baidu.translate.asr.TransAsrConfig;
import com.baidu.translate.asr.data.RecognitionResult;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class VoiceFragment extends Fragment {


    public VoiceFragment() {
        // Required empty public constructor
    }

    MyButton buttonVoiceTranslate;
    TextView textTranslateResult;
    TextView textRecognizeResult;
    FiveLine fiveLine;

    private Spinner spinnerVoiceSource,spinnerVoiceDest;
    private String from = "en";
    private String to = "zh";

//    //申请录音权限
//    private static final int GET_RECORD_AUDIO = 1;
//    private static String[] PERMISSION_AUDIO = {
//            Manifest.permission.RECORD_AUDIO
//    };
//    public static void verifyAudioPermissions(Activity activity){
//        int permission = ActivityCompat.checkSelfPermission(activity,Manifest.permission.RECORD_AUDIO);
//        if (permission != PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(activity,PERMISSION_AUDIO,GET_RECORD_AUDIO);
//        }
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_voice, container, false);


        //用于存放简单数据
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item,getData());
        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item,getData2());

        spinnerVoiceSource = view.findViewById(R.id.spinner_voice_source);
        spinnerVoiceDest = view.findViewById(R.id.spinner_voice_dest);

        //设置spinner的内容
        spinnerVoiceSource.setAdapter(arrayAdapter);
        spinnerVoiceDest.setAdapter(arrayAdapter2);

        //设置源语言选择监听事件
        spinnerVoiceSource.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SelectLanguage selectVoiceFrom = new SelectLanguage();
                from = selectVoiceFrom.getLangCode(parent.getItemAtPosition(position).toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //设置目标语言选择监听事件
        spinnerVoiceDest.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SelectLanguage selectVoiceTo = new SelectLanguage();
                to = selectVoiceTo.getLangCode(parent.getItemAtPosition(position).toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //初始化语音翻译相关类
        TransAsrConfig config = new TransAsrConfig("20190211000265342","P0tTPdrqOvttQqAeRqhF");
        final TransAsrClient client = new TransAsrClient(getContext(),config);

        textTranslateResult = view.findViewById(R.id.text_translate_result);
        textRecognizeResult = view.findViewById(R.id.text_recognize_result);
        client.setRecognizeListener(new OnRecognizeListener() {
            @Override
            public void onRecognized(int resultType, RecognitionResult recognitionResult) {
                if(resultType == OnRecognizeListener.TYPE_PARTIAL_RESULT){
//                    中间结果
//                    Log.d("----------","中间识别结果:"+recognitionResult.getAsrResult());
                }else if (resultType == OnRecognizeListener.TYPE_FINAL_RESULT){
                    if (recognitionResult.getError() == 0) {
//                        Log.d("----------", "最终识别结果:" +recognitionResult.getAsrResult());
//                        Log.d("----------","翻译结果:"+recognitionResult.getTransResult());
                        textRecognizeResult.setText(recognitionResult.getAsrResult());
                        textTranslateResult.setText(recognitionResult.getTransResult());
                    }else{
                        Log.d("----------","语音翻译出错："+recognitionResult.getError()+"错误信息："+recognitionResult.getErrorMsg());
                    }
                }
            }
        });

        fiveLine = view.findViewById(R.id.fiveLine);
        //设置语音按钮监听事件
        buttonVoiceTranslate = view.findViewById(R.id.button_voice_translate);
        buttonVoiceTranslate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    Log.d("++++++", "down");
                    fiveLine.setVisibility(View.VISIBLE);
                    buttonVoiceTranslate.setVisibility(View.INVISIBLE);
                    client.startRecognize(from,to);
                }else if ((event.getAction() == MotionEvent.ACTION_UP) || (event.getAction() == MotionEvent.ACTION_CANCEL)){
                    Log.d("++++++", "up");
                    fiveLine.setVisibility(View.INVISIBLE);
                    buttonVoiceTranslate.setVisibility(View.VISIBLE);
                    client.stopRecognize();
                }
                buttonVoiceTranslate.performClick();
                return true;
            }
        });


        return view;
    }

    private List<String> getData(){
        List<String> list = new ArrayList<String>();
        list.add("英文");
        list.add("中文");
        list.add("日语");
        return list;
    }
    private List<String> getData2(){
        List<String> list = new ArrayList<String>();
        list.add("中文");
        list.add("英文");
        list.add("日语");
        list.add("韩语");
        list.add("法语");
        return list;
    }
}
