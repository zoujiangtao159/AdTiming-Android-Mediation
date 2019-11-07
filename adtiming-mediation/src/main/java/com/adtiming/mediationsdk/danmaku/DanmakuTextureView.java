// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.danmaku;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.adtiming.mediationsdk.utils.DensityUtil;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class DanmakuTextureView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private float mFontSize;
    private int mLineSpacing;
    private int mDanmakuSpacing;
    private float mDanmakuHeight;
    private int mSpeed;
    private int mMaxLine = 10;
    private int mType = 1;
    private LinkedList<String> mDanmakus = null;
    private int[] mFontColors = new int[]{Color.RED, Color.WHITE, Color.BLUE, Color.YELLOW};
    private List<DanmakuItem> danmakuItems = new ArrayList<>();
    private DanmakuCallback mDanmakuCallback;
    private SurfaceHolder mSurfaceHolder;
    private Canvas mCanvas;
    private int mCanvasHeight;
    private int mCanvasWidth;
    private Paint mPaint;
    private boolean isRun = true;
    private Map<Integer, Float> mLineInfo = new HashMap<>();
    private float mLineBottom = -1;
    private float mDanmakuDelete = 0.33f;
    private boolean isStop = false;

    public DanmakuTextureView(Context context) {
        super(context);
        init();
    }

    public DanmakuTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DanmakuTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mFontSize = DensityUtil.dip2px(getContext(), 16f);
        mSpeed = DensityUtil.dip2px(getContext(), 2f);
        mLineSpacing = DensityUtil.dip2px(getContext(), 10f);
        mDanmakuSpacing = DensityUtil.dip2px(getContext(), 20f);
        setWillNotCacheDrawing(true);
        setDrawingCacheEnabled(false);
        setZOrderMediaOverlay(true);
        setZOrderOnTop(true);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mFontSize);
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        mDanmakuHeight = fontMetrics.descent - fontMetrics.ascent;
        mSurfaceHolder = getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        mSurfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        DeveloperLog.LogD("DanmakuTextureView", "surfaceCreated");
        isStop = false;
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        DeveloperLog.LogD("DanmakuTextureView", "surfaceChanged");
        isStop = false;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        DeveloperLog.LogD("DanmakuTextureView", "surfaceDestroyed");
        isStop = true;
        mDanmakus.clear();
        danmakuItems.clear();
    }

    public void setType(int type) {
        this.mType = type;
    }

    public void setData(LinkedList<String> danmakus) {
        this.mDanmakus = danmakus;
    }

    public void setFontColors(int[] colors) {
        this.mFontColors = colors;
    }

    public void setCallback(DanmakuCallback danmakuCallback) {
        this.mDanmakuCallback = danmakuCallback;
    }

    @Override
    public void run() {
        while (isFinished() && !isStop) {
            try {
                draw();
                Thread.sleep(1);
            } catch (Exception e) {
                DeveloperLog.LogD("DanmakuTextureView", e);
                CrashUtil.getSingleton().saveException(e);
            }
        }
    }

    /**
     * 绘制
     * 如需优化弹幕,降低此方法时间复杂度即可
     */
    private void draw() {
        try {
            if (mSurfaceHolder == null || isStop) {
                return;
            }
            mCanvas = mSurfaceHolder.lockCanvas();

            if (mCanvas == null) {
                return;
            }

            if (mCanvasHeight == 0) {
                mCanvasHeight = mCanvas.getHeight();
            }
            if (mCanvasWidth == 0) {
                mCanvasWidth = mCanvas.getWidth();
//                mDanmakuSpacing = mCanvasWidth;
            }

            if (danmakuItems.isEmpty()) {
                initDanmakuItems();
            }
            deleteDanmakuItems(mType);
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            int size = danmakuItems.size();
            if (size == 0) {
                return;
            }
            for (int i = 0; i < size; i++) {
                DanmakuItem danmakuItem = danmakuItems.get(i);
                if (danmakuItem == null) {
                    continue;
                }
                if (mType == 1) {
                    if (!danmakuItem.isShow()) {
                        if (mLineInfo.isEmpty() || danmakuItem.getLine() >= mLineInfo.size()) {
                            continue;
                        }
                        float lineX = mLineInfo.get(danmakuItem.getLine());
                        if (lineX == -1 || lineX + mDanmakuSpacing < mCanvasWidth) {
                            danmakuItem.setX(mCanvasWidth);
                            danmakuItem.setY(danmakuItem.getLine() * (mDanmakuHeight + mLineSpacing));
                            danmakuItem.setShow(true);
                        }
                    }
                    if (danmakuItem.isShow()) {
                        float x = danmakuItem.getX() - mSpeed;
                        mLineInfo.put(danmakuItem.getLine(), x + danmakuItem.getContentLength());
                        danmakuItem.setX(x);
                    }
                }
                if (mType == 2) {
                    if (!danmakuItem.isShow()) {
                        if (mLineBottom == -1 || mLineBottom < mCanvasHeight) {
                            danmakuItem.setX(0f + mDanmakuSpacing / 2f);
                            danmakuItem.setY(mCanvasHeight + mLineSpacing + mDanmakuHeight);
                            danmakuItem.setShow(true);
                        }
                    }
                    if (danmakuItem.isShow()) {
                        float y = danmakuItem.getY() - mSpeed;
                        mLineBottom = y;
                        danmakuItem.setY(y);
                    }
                }
                if (danmakuItem.isShow()) {
                    mPaint.setColor(danmakuItem.getColor());
                    mCanvas.drawText(danmakuItem.getContent(), danmakuItem.getX(), danmakuItem.getY(), mPaint);
                }
            }
        } catch (Exception e) {
            DeveloperLog.LogD("DanmakuTextureView", e);
            CrashUtil.getSingleton().saveException(e);
        } finally {
            if (mCanvas != null && mSurfaceHolder != null && mSurfaceHolder.getSurface().isValid()) {
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    private boolean isFinished() {
        if ((mDanmakus == null || mDanmakus.size() == 0) && danmakuItems.size() == 0) {
            isRun = false;
            if (mDanmakuCallback != null) {
                mDanmakuCallback.onFinish();
            }
        }
        return isRun;
    }

    private void initDanmakuItems() {
        int index = 0;
        int colorIndex = mFontColors.length;
        for (String danmakuText : mDanmakus) {
            DanmakuItem danmakuItem = new DanmakuItem();
            danmakuItem.setContent(danmakuText);
            danmakuItem.setContentLength(mPaint.measureText(danmakuText));
            danmakuItem.setColor(mFontColors[index % colorIndex]);
            danmakuItem.setLine(index % mMaxLine + 1);
            danmakuItem.setX(0);
            danmakuItem.setY(0);
            danmakuItem.setShow(false);
            danmakuItems.add(danmakuItem);
            index++;
        }
        mDanmakus.clear();
        for (int i = 1; i <= mMaxLine; i++) {
            mLineInfo.put(i, -1f);
        }
    }

    private void deleteDanmakuItems(int type) {
        Iterator<DanmakuItem> iter = danmakuItems.iterator();
        if (type == 1) {
            while (iter.hasNext()) {
                DanmakuItem danmakuItem = iter.next();
                if (!danmakuItem.isShow()) {
                    continue;
                }
                if (danmakuItem.getX() < 0 - danmakuItem.getContentLength()) {
                    iter.remove();
                }
            }
        }
        if (type == 2) {
            float danmakuRemoveLine = mCanvasHeight * mDanmakuDelete;
            while (iter.hasNext()) {
                DanmakuItem danmakuItem = iter.next();
                if (!danmakuItem.isShow()) {
                    continue;
                }
                if (danmakuItem.getY() != 0 && danmakuItem.getY() < danmakuRemoveLine) {
                    iter.remove();
                } else {
                    float dif = (danmakuItem.getY() - danmakuRemoveLine) / 255;
                    float alpha = dif < 1 ? dif * 255 : 255;
                    danmakuItem.setColor(changeAlpha(danmakuItem.getColor(), Math.round(alpha)));
                }
            }
        }
    }

    public int changeAlpha(int color, int alpha) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
    }
}
