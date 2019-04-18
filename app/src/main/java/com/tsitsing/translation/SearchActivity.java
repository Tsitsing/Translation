package com.tsitsing.translation;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tsitsing.translation.utility.FileUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SearchActivity extends AppCompatActivity {

    String path = "data/data/com.tsitsing.translation/databases/words.db";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        FileUtil fileUtil = new FileUtil();
        //如果数据库文件未导入，需要先导入文件
        if (!fileUtil.fileIsExists("data/data/com.tsitsing.translation/databases/words.db")) {
            //导入SQLite文件
            try {
                copySqliteFileFromRawToDatabases(getApplicationContext(), "words.db");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        final EditText edWord = findViewById(R.id.ed_search_word);
        final TextView tvResult = findViewById(R.id.tv_search_result);
        final TextView tvPronunciation = findViewById(R.id.tv_search_pronunciation);
        Button btnQuery = findViewById(R.id.btn_search_query);

        btnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonObject = queryWord(edWord.getText().toString());
                if (jsonObject.has("word")) {
                    try {
                        if (jsonObject.has("pronunciation")) {
                            String string = "[ " + jsonObject.getString("pronunciation") + " ]";
                            tvPronunciation.setText(string);
                        }
                        String translation = jsonObject.getString("translation").replace("\\n", "; ");
                        tvResult.setText(translation);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.no_such_word, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /**
     * 查询数据库中的单词
     *
     * @param word 单词
     * @return 返回JSONObject格式的单词信息
     */
    JSONObject queryWord(String word) {
        JSONObject jsonObject = new JSONObject();
        String[] strings = new String[]{word};
        SQLiteDatabase database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        Cursor cursor = database.rawQuery("select * from bnc where word = ?", strings);
        if (cursor.moveToFirst()) {
            try {
                jsonObject.put("word", cursor.getString(cursor.getColumnIndex("word")));
                jsonObject.put("pronunciation", cursor.getString(cursor.getColumnIndex("pronunciation")));
                jsonObject.put("translation", cursor.getString(cursor.getColumnIndex("translation")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        return jsonObject;
    }

    /**
     * 将assets目录下的文件拷贝到sd上
     *
     * @return 存储数据库的地址
     */
    public static String copySqliteFileFromRawToDatabases(Context context, String SqliteFileName) throws IOException {
        // 第一次运行应用程序时，加载数据库到data/data/当前包的名称/database/<db_name>
        File dir = new File("data/data/com.tsitsing.translation/databases");

        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdir();
        }

        File file = new File(dir, SqliteFileName);
        InputStream inputStream = null;
        OutputStream outputStream = null;
        //通过IO流的方式，将assets目录下的数据库文件，写入到SD卡中。
        if (!file.exists()) {
            try {
                file.createNewFile();

                inputStream = context.getClass().getClassLoader().getResourceAsStream("assets/" + SqliteFileName);
                outputStream = new FileOutputStream(file);

                byte[] buffer = new byte[1024];
                int len;

                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }
        return file.getPath();
    }
}
