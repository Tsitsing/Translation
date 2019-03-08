package com.tsitsing.translation;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

//权限请求封装类
public class PermissionUtil {

    public static boolean isOwnPermission(Activity activity, String permission){
        return ContextCompat.checkSelfPermission(activity,
                permission)
                == PackageManager.PERMISSION_GRANTED;
    }
    public static void requestPermission(Activity activity, String permission,int requestCode) {
        if (ContextCompat.checkSelfPermission(activity,
                permission)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    permission)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity,
                        new String[]{permission},//需要请求多个权限，可以在这里添加
                        requestCode);

            }
        }
    }
}
