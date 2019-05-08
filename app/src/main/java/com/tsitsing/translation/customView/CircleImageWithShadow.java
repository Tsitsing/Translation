package com.tsitsing.translation.customView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.tsitsing.translation.R;

public class CircleImageWithShadow extends android.support.v7.widget.AppCompatImageView {

    Paint paint;
    public CircleImageWithShadow(Context context) {
        super(context);
        paint = new Paint();
    }

    public CircleImageWithShadow(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint  = new Paint();
    }

    public CircleImageWithShadow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint  = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();//获取原图像的drawable
        if (drawable != null) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            Bitmap b = getCircleBitmap(bitmap);
            paint.reset();
            canvas.drawBitmap(b, 0, 0, paint);
        } else {
            super.onDraw(canvas);
        }
    }

    private Bitmap getCircleBitmap (Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);//颜色存储模式
        Canvas canvas = new Canvas(output);
        Rect rect = new Rect(0,0, bitmap.getWidth(), bitmap.getHeight());
        paint.setAntiAlias(true);//抗锯齿
        //设置模糊样式
        BlurMaskFilter blurMaskFilter = new BlurMaskFilter(12, BlurMaskFilter.Blur.SOLID);
        paint.setColor(getContext().getResources().getColor(R.color.theme, null));
        paint.setMaskFilter(blurMaskFilter);
        canvas.drawCircle(bitmap.getWidth()/(float)2, bitmap.getHeight()/(float)2, bitmap.getHeight()/(float)2.3, paint);
        //设置混合模式
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));//取原图像作为前景的交集
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }
}
