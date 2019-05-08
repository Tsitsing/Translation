package com.tsitsing.translation;


import android.annotation.SuppressLint;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tsitsing.translation.recite.PlansActivity;
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

    public DictionaryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dictionary, container, false);
        ImageView imgSearch = view.findViewById(R.id.imageView_search_offLine);
        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });
        ImageView imgRecite = view.findViewById(R.id.imageView__reciteWords);
        imgRecite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication application = (MyApplication) getActivity().getApplication();
                if (application.getIsSignIn()) {
                    Intent intent = new Intent(getActivity(), PlansActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), R.string.please_signIn, Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }
}
