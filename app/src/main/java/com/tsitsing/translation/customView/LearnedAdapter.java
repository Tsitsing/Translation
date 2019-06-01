package com.tsitsing.translation.customView;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tsitsing.translation.R;

import org.json.JSONArray;
import org.json.JSONException;

public class LearnedAdapter extends RecyclerView.Adapter<LearnedAdapter.MyHolder>{
    // 设置点击事件的接口，利用接口回调，来完成点击事件
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position, String word, String plan);
    }
    private OnItemClickListener mOnItemClickListener;
    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    private Context context;
    private JSONArray jsonArray;

    public LearnedAdapter(Context context, JSONArray jsonArray) {
        this.context = context;
        this.jsonArray = jsonArray;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //绑定item
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_recite, viewGroup, false);
        MyHolder myHolder = new MyHolder(view);
        return myHolder;
    }

    //绑定数据
    @Override
    public void onBindViewHolder(@NonNull final MyHolder myHolder, int i) {
        try {
            myHolder.tvWord.setText(jsonArray.getJSONObject(i).getString("word"));
            myHolder.tvDate.setText(jsonArray.getJSONObject(i).getString("date"));
            myHolder.tvPlan.setText(jsonArray.getJSONObject(i).getString("plan"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        myHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int pos = myHolder.getLayoutPosition();
                mOnItemClickListener.onItemLongClick(myHolder.itemView, pos, myHolder.tvWord.getText().toString(),
                        myHolder.tvPlan.getText().toString());
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return jsonArray.length();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        TextView tvWord;
        TextView tvDate;
        TextView tvPlan;
        //        LinearLayout linearLayout;
        public MyHolder (View itemView) {
            super(itemView);
            tvWord = itemView.findViewById(R.id.tv_learned_word);
            tvDate = itemView.findViewById(R.id.tv_learned_date);
            tvPlan = itemView.findViewById(R.id.tv_learned_plan);
//            linearLayout = itemView.findViewById(R.id.linear_collection);
        }
    }
}
