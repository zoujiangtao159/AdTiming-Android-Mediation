// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.interactive;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.adtiming.mediationsdk.utils.DensityUtil;

import java.util.concurrent.atomic.AtomicInteger;


public class InteractiveTitleView extends RelativeLayout {

    private static final String backImgBase64Data = "iVBORw0KGgoAAAANSUhEUgAAAEgAAABIBAMAAACnw650AAAAG1BMVEUAAAAzMzM2NjY0NDQzMzM4ODgzMzM0NDQzMzPciteJAAAACXRSTlMA/lHYGQruYighBah6AAAAS0lEQVRIx2MYBaOApoA1gAhFahmE1TAJihNWpChoSoRBwg5EGGQyatDINSgMYRBhRZRbx6A4atSoUSiFGOXFIaJgJVxEj4JRQEsAAOYOEBOOQ7hTAAAAAElFTkSuQmCC";
    private static final String closeImgBase64Data = "iVBORw0KGgoAAAANSUhEUgAAAEgAAABIBAMAAACnw650AAAAMFBMVEUAAAA0NDQ0NDQ5OTkzMzMzMzMzMzMzMzMzMzMzMzMzMzM1NTU2NjY0NDQ2NjYzMzOtBEhrAAAAD3RSTlMAnc4SX6Rv5t+vh2BHQSGi0fSdAAAArUlEQVRIx+3QsQkCQRBG4TlEYyvQEoTLzK4by7ACsQTtQEuwCQs58EwUxuVnuYvGh8bzogmGb5m1LMv+aHee5u0hWNr34zjbDMHSxZdWa/0dLDXej5B3FrQWJehhFlMIiSJIFEKiCBKFkChBQA2CiCoQNXe/GtV6ORAqp3nP0KsciFDXEKU/KhRB+naERBEkCiFRBIn6ChF1ElSpZ7B0FFSp6L37bZoXK8uy7Pc+RU5X+mkNWt0AAAAASUVORK5CYII=";
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
    private TypeEnum mTypeEnum;
    private ImageView imgView;

    public InteractiveTitleView(Context context) {
        super(context);
        imgView = new ImageView(getContext());

        LayoutParams params = new LayoutParams(DensityUtil.dip2px(context, 48),
                DensityUtil.dip2px(context, 48));
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(imgView, params);
    }

    public void setTypeEnum(TypeEnum typeEnum) {
        mTypeEnum = typeEnum;
        int size = DensityUtil.dip2px(getContext(), 36);
        imgView.getLayoutParams().width = size;
        imgView.getLayoutParams().height = size;
        imgView.setImageBitmap(getMarkImg());
    }

    protected Bitmap getMarkImg() {
        byte[] bytes;
        if (mTypeEnum.equals(TypeEnum.BACK)) {
            bytes = Base64.decode(backImgBase64Data, Base64.DEFAULT);
        } else {
            bytes = Base64.decode(closeImgBase64Data, Base64.DEFAULT);
        }

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


    public static int generateViewId() {
        for (; ; ) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    public enum TypeEnum {
        BACK, CLOSE
    }
}
