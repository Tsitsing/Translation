package com.tsitsing.translation;


import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.translate.asr.data.Language;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyFragment extends Fragment {
    public MyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my, container, false);
        ImageView imagePortrait = view.findViewById(R.id.imageView_portrait);
        imagePortrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication application = (MyApplication) getActivity().getApplication();
                if (!application.getIsSignIn()) {
                    Intent intent = new Intent(getContext(), SignInActivity.class);
                    startActivityForResult(intent, 1000);
                }
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            //登录完成，设置界面上的用户名
            if (resultCode == RESULT_OK) {
                MyApplication application;
                application = (MyApplication) getActivity().getApplication();
                TextView textUserName = getView().findViewById(R.id.textView_userName);
                textUserName.setText(application.getUserName());
            }
        }
    }
}
