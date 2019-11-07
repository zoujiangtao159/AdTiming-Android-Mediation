// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.adtiming.mediationsdk.utils.DensityUtil;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;


/**
 * 
 */
public class AdMarketView extends RelativeLayout {
    private static String TAG = "AdMarketView";
    private static final String AD_LOGO_URL = "http://www.adtiming.com/en/privacypolicy.html";
    private static final String imgBase64Data = "iVBORw0KGgoAAAANSUhEUgAAACQAAAAkCAMAAADW3miqAAAAolBMVEUAAAAAAAD9/f3s7OzKysr4+PiEhIT8/Pzz8/Otra0zMzMZGRn+/v7g4ODY2Ni3t7eZmZlgYGDw8PDW1taQkJBVVVX+/v719fXp6enNzc2Xl5fu7u7q6uoAAAD5+fn19fXU1NTJycmmpqaHh4clJSUAAADi4uLc3NzIyMi7u7u5ubl9fX1GRkYAAAAAAAAAAADl5eXQ0NDBwcFtbW0AAAD///+0S2RyAAAANXRSTlNNAPnare949+eRWlP+yLyahWjiuoBk/OrVr4Pb10Px67erjXpVG8nBq56cdV9KNCvNs6RuGTtgcKgAAAFiSURBVDjLzZPpdoIwEIVDCIJCReuKiPtS1673/V+tDXPMJFIP/dn7h5M5X8gsd8S/V0NNAymDqWo8RJoRjKLmr8jABzD0e0ot/CEAf1Bl1jNgnrbo0ErnwKzyZl8iUTGfY5VA9l2mG0J23FBHIuw6ER+YiDtNAN+pC1DlDxe73RPpIIQC7BojjGKdSAjWz3GEyOohkOrvChg9l5prSKQAV6iQlLWPEcbmmu5EAmWgMWV4BtqCdAKonrGBAvQE/X19az9BPQQGkljSvZk479tabwS9QNoQ3V6IDUgMuc8dylo+O1of/JybeG5FBpy43QIdf2dPUAuGUG4z28CJ58bNdMdSEY+FB3wnHjBbJalaJUEuSBdjupXLrCzTFca+yDaMbDJY9r3eTB4CwfG2CMcACNniW2PznFZqme3Llcotg3vFH5bT29prntGaZ9RDhrxXUSuPqFrI+yrqoFLba3F5zHwDIdYwMTtbGdcAAAAASUVORK5CYII=";

    private String mActUrl;

    public AdMarketView(Context context, Bitmap logo, String act) {
        super(context);
        mActUrl = TextUtils.isEmpty(act) ? AD_LOGO_URL : act;
        DeveloperLog.LogD("adt-log url : " + mActUrl);

        ImageView adLogoImgView = new ImageView(getContext());
        addView(adLogoImgView);

        int size = DensityUtil.dip2px(getContext(), 15);
        adLogoImgView.getLayoutParams().width = size;
        adLogoImgView.getLayoutParams().height = size;
        if (logo == null) {
            DeveloperLog.LogD("adt-log default bm");
            adLogoImgView.setImageBitmap(getMarkImg());
        } else {
            DeveloperLog.LogD("adt-log custom bm");
            adLogoImgView.setImageBitmap(logo);
        }
        adLogoImgView.bringToFront();


        adLogoImgView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    Uri url = Uri.parse(mActUrl);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(url);
                    getContext().startActivity(intent);
                } catch (Throwable e) {
                    DeveloperLog.LogD(TAG, e);
                    CrashUtil.getSingleton().saveException(e);
                }
            }
        });
    }

    protected Bitmap getMarkImg() {
        byte[] bytes = Base64.decode(imgBase64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
