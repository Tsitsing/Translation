package com.tsitsing.translation;


import org.json.JSONException;
import org.json.JSONObject;

class SelectLanguage {
    String getLangCode(String language){
        String langCode = "auto";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("自动", "auto");
            jsonObject.put("中文", "zh");
            jsonObject.put("繁体", "cht");
            jsonObject.put("英文", "en");
            jsonObject.put("日语", "jp");
            jsonObject.put("韩语", "kor");
            jsonObject.put("法语", "fra");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            langCode = jsonObject.getString(language);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return langCode;
    }
}
