package com.tsitsing.translation;


import android.Manifest;
import android.annotation.SuppressLint;
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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.translate.asr.OnRecognizeListener;
import com.baidu.translate.asr.TransAsrClient;
import com.baidu.translate.asr.TransAsrConfig;
import com.baidu.translate.asr.data.RecognitionResult;
import com.tsitsing.translation.customView.CircleImageWithShadow;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class VoiceFragment extends Fragment {


    public VoiceFragment() {
        // Required empty public constructor
    }

//    MyButton buttonVoiceTranslate;
    private CircleImageWithShadow imgView;
    private TextView textTranslateResult;
    private TextView textRecognizeResult;
    private FiveLine fiveLine;
    private Spinner spinnerVoiceSource;
    private Spinner spinnerVoiceDest;
    ArrayAdapter<String> arrayAdapter;
    ArrayAdapter<String> arrayAdapter2;
    private String from = "en";
    private String to = "zh";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_voice, container, false);


        //用于存放简单数据
        arrayAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item,getData());
        arrayAdapter2 = new ArrayAdapter<>(getContext(), R.layout.spinner_item,getData2());
        spinnerVoiceSource = view.findViewById(R.id.spinner_voice_source);
        spinnerVoiceDest = view.findViewById(R.id.spinner_voice_dest);
        spinnerVoiceSource.setDropDownHorizontalOffset(50);
        spinnerVoiceSource.setDropDownVerticalOffset(110);
        spinnerVoiceDest.setDropDownHorizontalOffset(1030);
        spinnerVoiceDest.setDropDownVerticalOffset(110);

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

        //交换源语言与目标语言
        ImageView imgSwitch = view.findViewById(R.id.img_voice_switch);
        imgSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //先获取当前选中的item
                String srcSelected = (String) spinnerVoiceSource.getSelectedItem();
                String dstSelected = (String) spinnerVoiceDest.getSelectedItem();
                //通过适配器获取对应item的位置
                int srcTarget = arrayAdapter.getPosition(dstSelected);
                int dstTarget = arrayAdapter2.getPosition(srcSelected);
                if (srcTarget != -1 && dstTarget != -1) {//值为-1时说明不存在此item
                    //根据位置重新设定当前spinner的选择
                    spinnerVoiceSource.setSelection(srcTarget, true);
                    spinnerVoiceDest.setSelection(dstTarget, true);
                } else {
                    Toast.makeText(getContext(), R.string.toast_canNotSwitch, Toast.LENGTH_SHORT).show();
                }
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
                    //中间结果
                }else if (resultType == OnRecognizeListener.TYPE_FINAL_RESULT){
                    if (recognitionResult.getError() == 0) {
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
        imgView = view.findViewById(R.id.img_voice_translate);
        imgView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    fiveLine.setVisibility(View.VISIBLE);
                    imgView.setVisibility(View.INVISIBLE);
                    client.startRecognize(from,to);
                }else if ((event.getAction() == MotionEvent.ACTION_UP) || (event.getAction() == MotionEvent.ACTION_CANCEL)){
                    fiveLine.setVisibility(View.INVISIBLE);
                    imgView.setVisibility(View.VISIBLE);
                    client.stopRecognize();
                }
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
