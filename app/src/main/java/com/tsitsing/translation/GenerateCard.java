package com.tsitsing.translation;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

class GenerateCard {
    private Context context;
    GenerateCard (Context context) {
        this.context = context;
    }

    /**
     * 获取组合后的组件
     * @param wordAndPronunciation 单词和音标
     * @param translation 翻译后的文本
     * @return 返回组件
     */
    LinearLayout getCar (String wordAndPronunciation, String translation) {
        //最外层Layout
        LinearLayout layoutShell = new LinearLayout(context);
        layoutShell.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams shellParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        shellParams.setMargins(10, 10, 10, 10);
        layoutShell.setLayoutParams(shellParams);
        //放置图片与单词、音标的Layout
        LinearLayout layoutSub = new LinearLayout(context);
        layoutSub.setOrientation(LinearLayout.HORIZONTAL);
        //放置收藏图标的view
        ImageView collectView = new ImageView(context);
        collectView.setBackground(context.getDrawable(R.mipmap.collection));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(40, 40);
        collectView.setLayoutParams(params);
        //放置单词和音标TextView
        TextView wordView = new TextView(context);
        wordView.setText(wordAndPronunciation);
        //放置翻译的TextView
        TextView transView = new TextView(context);
        transView.setText(translation);
        //组合各组件
        layoutSub.addView(collectView);
        layoutSub.addView(wordView);
        layoutShell.addView(layoutSub);
        layoutShell.addView(transView);
        return layoutShell;
    }
}
