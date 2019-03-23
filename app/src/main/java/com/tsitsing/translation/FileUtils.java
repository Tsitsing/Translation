package com.tsitsing.translation;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

public class FileUtils {
    public static String SDCARD_PATH = Environment.getExternalStorageDirectory().getPath() + "/Tsitsing/";

    //静态方法，不用创建实例即可调用
    //创建文件夹
    public static File createSDDir (String dirName) throws IOException{
        File dir = new File(SDCARD_PATH + dirName);
        dir.mkdirs();
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

        }
        return dir;
    }

    //文件未存在，创建文件
    public static boolean isNotFileExist (String parseScriptPath) {
        if (parseScriptPath !=null && parseScriptPath.length() != 0){
            File file = new File(parseScriptPath);
            if (file.exists() && file.isFile()){
                return false;
            }
        }
        return  true;
    }
}
