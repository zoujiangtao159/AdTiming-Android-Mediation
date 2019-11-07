// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.video.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;

/**
 * 
 */
public class PlayINButton extends Button {

    private AlphaAnimation mAlphaAnimation;

    public PlayINButton(Context context) {
        this(context, null);
    }

    public PlayINButton(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public PlayINButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initStyle();
    }

    private void initStyle() {
        float[] outerRadian = new float[]{50, 50, 50, 50, 50, 50, 50, 50};
        RectF insetDistance = new RectF(0, 0, 0, 0);
        float[] insideRadian = new float[]{20, 20, 20, 20, 20, 20, 20, 20};
        ShapeDrawable drawable = new ShapeDrawable(new RoundRectShape(outerRadian, insetDistance, insideRadian));
        drawable.getPaint().setColor(Color.rgb(102, 153, 255));
        drawable.getPaint().setStyle(Paint.Style.FILL);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            setBackgroundDrawable(drawable);
        } else {
            setBackground(drawable);
        }
        setTextColor(Color.WHITE);
        super.setVisibility(View.GONE);
    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility == getVisibility()) {
            return;
        }
        if (visibility == View.VISIBLE) {
            mAlphaAnimation = new AlphaAnimation(0.0f, 0.9f);
        }
        if (visibility == View.GONE || visibility == View.INVISIBLE) {
            mAlphaAnimation = new AlphaAnimation(0.9f, 0.0f);
        }
        mAlphaAnimation.setDuration(1000);
        mAlphaAnimation.setFillAfter(true);
        startAnimation(mAlphaAnimation);
        super.setVisibility(visibility);
    }
}
