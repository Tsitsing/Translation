package com.tsitsing.translation;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

public class MainActivity extends AppCompatActivity {

    private HomeFragment homeFragment;
    private VoiceFragment voiceFragment;
    private PictureFragment pictureFragment;
    private DictionaryFragment dictionaryFragment;
    private MyFragment myFragment;
    private RadioGroup navRadioGroup;

    private final int REQUEST_CODE = 1;

    //权限请求
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("request", "success");
                } else {
                    Log.i("request", "failed");
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (PermissionUtil.isOwnPermission(this, Manifest.permission.RECORD_AUDIO)) {
            //如果已经拥有改权限
            Log.i("request","own");
        } else {
            //没有改权限，需要进行请求
            PermissionUtil.requestPermission(this, Manifest.permission.RECORD_AUDIO, REQUEST_CODE);
        }

        initHomeFragment();//开启时默认的fragment
        navRadioGroup = findViewById(R.id.nav_radio_group);
        navRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (group.getCheckedRadioButtonId()){
                    case R.id.nav_home:
                        initHomeFragment();
                        break;
                    case R.id.nav_voice:
                        initVoiceFragment();
                        break;
                    case R.id.nav_picture:
                        initPictureFragment();
                        break;
                    case R.id.nav_dictionary:
                        initDictionaryFragment();
                        break;
                    case R.id.nav_my:
                        initMyFragment();
                        break;
                }
            }
        });
    }


    //代码冗余，需要改
    private void initHomeFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(homeFragment ==null){
            homeFragment = new HomeFragment();
            transaction.add(R.id.nav_fragment_layout,homeFragment);
        }
        hideFragment(transaction);
        transaction.show(homeFragment);
        transaction.commit();
    }

    private void initVoiceFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(voiceFragment == null){
            voiceFragment = new VoiceFragment();
            transaction.add(R.id.nav_fragment_layout,voiceFragment);
        }
        hideFragment(transaction);
        transaction.show(voiceFragment);
        transaction.commit();
    }

    private void initPictureFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(pictureFragment ==null){
            pictureFragment = new PictureFragment();
            transaction.add(R.id.nav_fragment_layout,pictureFragment);
        }
        hideFragment(transaction);
        transaction.show(pictureFragment);
        transaction.commit();
    }

    private void initDictionaryFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(dictionaryFragment ==null){
            dictionaryFragment = new DictionaryFragment();
            transaction.add(R.id.nav_fragment_layout,dictionaryFragment);
        }
        hideFragment(transaction);
        transaction.show(dictionaryFragment);
        transaction.commit();
    }

    private void initMyFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(myFragment ==null){
            myFragment = new MyFragment();
            transaction.add(R.id.nav_fragment_layout,myFragment);
        }
        hideFragment(transaction);
        transaction.show(myFragment);
        transaction.commit();
    }

    private void hideFragment(FragmentTransaction transaction){
        if (homeFragment != null){
            transaction.hide(homeFragment);
        }
        if (voiceFragment != null){
            transaction.hide(voiceFragment);
        }
        if (pictureFragment !=null){
            transaction.hide(pictureFragment);
        }
        if (dictionaryFragment != null){
            transaction.hide(dictionaryFragment);
        }
        if (myFragment !=null) {
            transaction.hide(myFragment);
        }
    }
}
