package com.tsitsing.translation.mySpace;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.tsitsing.translation.AboutActivity;
import com.tsitsing.translation.CollectionActivity;
import com.tsitsing.translation.LearnedActivity;
import com.tsitsing.translation.MyApplication;
import com.tsitsing.translation.R;
import com.tsitsing.translation.SignInActivity;
import com.tsitsing.translation.customView.CircleImage;
import com.tsitsing.translation.mySpace.DynamicActivity;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyFragment extends Fragment {

    private MyApplication myApplication;
    private TextView textUserName;
    private int SIGN_IN_REQUEST = 1;

    public MyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my, container, false);

        myApplication = (MyApplication) getActivity().getApplication();
        textUserName = view.findViewById(R.id.textView_userName);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.cat);
        bitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true);

        //点击进行登录
        CircleImage imagePortrait = view.findViewById(R.id.imageView_portrait);
        imagePortrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!myApplication.getIsSignIn()) {
                    Intent intent = new Intent(getContext(), SignInActivity.class);
                    startActivityForResult(intent, 1000);
                } else {
                }
            }
        });
        textUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!myApplication.getIsSignIn()) {
                    Intent intent = new Intent(getContext(), SignInActivity.class);
                    startActivityForResult(intent, 1000);
                } else {
                }
            }
        });

        imagePortrait.setImageBitmap(bitmap);

        //进入动态页面
        Button btnDynamic = view.findViewById(R.id.btn_my_dynamic);
        btnDynamic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myApplication.getIsSignIn()) {
                    Intent intent = new Intent(getContext(), DynamicActivity.class);
                    startActivity(intent);
                }
            }
        });

        //退出登录
        Button btnSignOut = view.findViewById(R.id.btn_my_signOut);
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myApplication.getIsSignIn()) {
                    myApplication.setIsSignIn(false);
                    myApplication.setUserName(null);
                    myApplication.setFirstSignIn(true);
                    Intent intent = new Intent(getContext(), SignInActivity.class);
                    startActivityForResult(intent, SIGN_IN_REQUEST);
                    textUserName.setText(getContext().getResources().getString(R.string.btn_SignIn));
                }
            }
        });

        //跳转至收藏
        Button btnCollection = view.findViewById(R.id.btn_my_collection);
        btnCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CollectionActivity.class);
                startActivity(intent);
            }
        });

        //跳转至已背单词
        Button btnLearned = view.findViewById(R.id.btn_my_learned);
        btnLearned.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), LearnedActivity.class);
                startActivity(intent);
            }
        });

        //跳转至关于
        Button btnAbout = view.findViewById(R.id.btn_my_about);
        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AboutActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 || requestCode == SIGN_IN_REQUEST) {
            //登录完成，设置界面上的用户名
            if (resultCode == RESULT_OK) {
                textUserName.setText(myApplication.getUserName());
            }
        }
    }
}
