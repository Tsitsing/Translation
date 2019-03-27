package com.tsitsing.translation;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.LoginFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.youdao.sdk.app.Language;
import com.youdao.sdk.app.LanguageUtils;
import com.youdao.sdk.app.YouDaoApplication;
import com.youdao.sdk.ydtranslate.EnWordTranslator;
import com.youdao.sdk.ydtranslate.Translate;
import com.youdao.sdk.ydtranslate.TranslateErrorCode;
import com.youdao.sdk.ydtranslate.TranslateListener;
import com.youdao.sdk.ydtranslate.TranslateParameters;
import com.youdao.sdk.ydtranslate.TranslatorOffline;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class DictionaryFragment extends Fragment {

    private String queryContent;
    private static final String REQUEST_CODE = "qid";
    private static final int MASSAGE_WHAT = 1;
    private TranslatorOffline translatorOffline;
    private Button buttonQuery;
    private TextView textViewResult;
    private EditText editTextContent;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MASSAGE_WHAT :
                    Log.d("handled result", msg.obj.toString());
            }
        }
    };

    public DictionaryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        YouDaoApplication.init(getContext(), "52e3f438b3ed7fa8");
        translatorOffline = new TranslatorOffline();
        View view = inflater.inflate(R.layout.fragment_dictionary, container, false);

        editTextContent = view.findViewById(R.id.editText_content);
        textViewResult = view.findViewById(R.id.textView_off_result);
        buttonQuery = view.findViewById(R.id.button_query);

        buttonQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryContent = editTextContent.getText().toString();
                translateOffLine();
            }
        });
        return view;
    }

    private void translateOffLine () {
        //初始化词库
        translatorOffline.initWordDict("dict", true, new EnWordTranslator.EnWordInitListener() {
            @Override
            public void success() {
                Log.d("translatorOffline", "init success");
            }

            @Override
            public void fail(TranslateErrorCode translateErrorCode) {
                Log.d("translatorOffline", "init fail " + translateErrorCode);
            }
        });

        String from = "中文";
        String to = "英文";
        Language languageFrom = LanguageUtils.getLangByName(from);
        Language languageTo = LanguageUtils.getLangByName(to);
        final TranslateParameters translateParameters = new TranslateParameters.Builder().from(languageFrom).to(languageTo)
                .useAutoConvertWord(true).useAutoConvertLine(false).build();
        new Thread() {
            @Override
            public void run() {
                Log.d("new thread", "success");
                translatorOffline.lookupNative(queryContent, translateParameters, REQUEST_CODE,
                        new TranslateListener() {
                            @Override
                            public void onError(TranslateErrorCode translateErrorCode, String s) {
                                Log.d("query error", translateErrorCode.toString());
                            }

                            @Override
                            public void onResult(Translate translate, String s, String s1) {
                                Log.d("query error", "onResult achieve");
                                String result = translate.toString();
                                Message message = handler.obtainMessage(MASSAGE_WHAT);
                                message.obj = result;
                                handler.sendMessage(message);
                            }

                            @Override
                            public void onResult(List<Translate> list, List<String> list1, List<TranslateErrorCode> list2, String s) {

                            }
                        });
            }
        }.start();
    }

}
