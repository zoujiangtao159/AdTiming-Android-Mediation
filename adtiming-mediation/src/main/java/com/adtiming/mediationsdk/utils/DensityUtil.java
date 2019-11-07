// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils;


import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;

/**
 * 
 */
public class DensityUtil {
    private DensityUtil() {
    }
    // 

    /**
     * 
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().densityDpi;
        return (int) (dpValue * (scale / 160) + 0.5f);
    }

    /**
     * 
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().densityDpi;
        return (int) ((pxValue * 160) / scale + 0.5f);
    }

    public static int getDirection(Context context) {
        if (context == null) {
            return Configuration.ORIENTATION_UNDEFINED;
        }
        //
        Configuration mConfiguration = context.getResources().getConfiguration();
        //
        return mConfiguration.orientation;
    }

    /***
     * display's dimensions
     *
     * @param context
     */
    public static DisplayMetrics getDisplay(Context context) {
        return context.getResources().getDisplayMetrics();
    }

    public static int getPhoneWidth(Context context) {
        return getDisplay(context).widthPixels;
    }

    public static int getPhoneHeight(Context context) {
        return getDisplay(context).heightPixels;
    }

    public static int getDensityDpi(Context context) {
        return getDisplay(context).densityDpi;
    }

    public static int getScreenSize(Context context) {
        return (int) getDisplay(context).density;
    }

    public static int getXdpi(Context context) {
        return (int) getDisplay(context).xdpi;
    }

    public static int getYdpi(Context context) {
        return (int) getDisplay(context).ydpi;
    }
}
