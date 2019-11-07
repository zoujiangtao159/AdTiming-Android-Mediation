// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.video.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Base64;
import android.widget.ImageView;

import com.adtiming.mediationsdk.utils.DensityUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProgerssView extends ImageView {
    //progress of arc drawing
    private int progress = 0;
    //
    private int lineThick = 6;
    //center's x
    float center;
    //arc's radius
    float radius;

    private static final String imgBase64Data = "iVBORw0KGgoAAAANSUhEUgAAAgAAAAIACAMAAADDpiTIAAAB11BMVEUAAAD///+AgICqqqqAgICZmZmfn5+Ojo6ioqKVlZWdnZ2SkpKZmZmfn5+ZmZmenp6Xl5ebm5uZmZmdnZ2bm5uVlZWZmZmcnJyXl5eWlpaZmZmYmJiampqWlpaZmZmbm5uXl5eZmZmbm5uampqXl5eZmZmYmJiXl5eZmZmbm5uYmJiampqYmJiampqZmZmYmJiZmZmampqYmJiZmZmYmJiampqYmJiZmZmampqYmJiampqYmJiampqampqZmZmZmZmampqZmZmYmJiZmZmampqZmZmZmZmYmJiZmZmampqZmZmYmJiZmZmampqZmZmampqZmZmZmZmZmZmampqampqZmZmZmZmZmZmYmJiampqZmZmYmJiampqZmZmZmZmYmJiampqZmZmampqZmZmZmZmZmZmYmJiampqZmZmZmZmZmZmZmZmYmJiZmZmYmJiampqZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZmZnDSZNpAAAAnHRSTlMAAQIDBAUICQsMDQ4PEBQVFhcZGhwdHh8gIiMlJicoKSwtLjAxMjQ2Nzg+P0NHS01QUVRVV1hZWlteYGFiZWZpamxtbm9wcXJzdHV3eHl6fn+AgoOIiYyPkJKTlZeYmZqcnaGjqKyusLK0t7m9vsLExsvMz9DR0tPV1tna29ze3+Hi4+Tl5ufo6err7O3u7/Dx8vP09ff4+vv8/f6/a21AAAAAAWJLR0QB/wIt3gAACg9JREFUeNrt2/1rlXUch/HvzlnO5lzZTNuyUlemlVrYo9CzWFBhQY8WBGUU4D/QfxH91AizgAKLqAZlVAYgFDZVDDF1M3Bb6TTcfHZubjk3dcrm0e2Ozj7X9Ybo7OC54dzXa/d5QFNyzjnnnHPOOeecc84555xzzjnnnHPOOeecc84555xzzjnnnHPOOeecc84551wxriSlNG9heeeutQciPr2qRTW9+9dvjV7xqmlX5ytKCv+57mNd7a2HLwBQ8trDfTeOrv7h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";
    String color = "#999999";

    //arc's shape and boundaries
    RectF rectF;

    Paint paint;

    //control's width
    float totalWidth;

    public ProgerssView(Context context) {
        super(context);
        totalWidth = dp2px(context, 30);
        init();
    }

    public ProgerssView(Context context, AttributeSet attrs) {
        super(context, attrs);

        Pattern p = Pattern.compile("\\d*");

        Matcher m = p.matcher(attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "layout_width"));

        if (m.find()) {
            totalWidth = Float.valueOf(m.group());
        }

        totalWidth = dp2px(context, totalWidth);
        init();
    }

    public ProgerssView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    public static float dp2px(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        invalidate();
    }

    public int getProgress() {
        return progress;
    }

    void init() {
        int padding = DensityUtil.dip2px(getContext(), 8);
        setPadding(padding, padding, padding, padding);

        setImageBitmap(getMarkImg());

        paint = new Paint();
        //
        paint.setColor(Color.parseColor(color));
        //
        paint.setStrokeWidth(lineThick);
        //
        paint.setStyle(Paint.Style.STROKE);
        //
        paint.setAntiAlias(true);
        //
        center = (totalWidth / 2);
        //
        radius = (totalWidth / 2) - lineThick;

        rectF = new RectF(center - radius,
                center - radius,
                center + radius,
                center + radius);
        setClickable(true);
    }

    protected Bitmap getMarkImg() {
        byte[] bytes = Base64.decode(imgBase64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    //drawing
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //
        canvas.drawArc(rectF, 180, progress, false, paint);
    }

}
