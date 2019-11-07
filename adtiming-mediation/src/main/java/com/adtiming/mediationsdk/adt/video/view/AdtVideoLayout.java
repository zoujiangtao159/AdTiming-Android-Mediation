// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.video.view;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adtiming.mediationsdk.utils.DensityUtil;
import com.adtiming.mediationsdk.adt.view.DrawCrossMarkView;

import java.util.Locale;

public class AdtVideoLayout extends RelativeLayout implements View.OnClickListener {
    private int mVideoProgress = 360;

    private CustomVideoView mVideoView;
    private TextView mTxtSkip;
    private ProgerssView mPrgVideo;
    private DrawCrossMarkView mDrawCrossMarkView;
    private PlayINButton mPlayINButton;

    private OnViewEventListener mListener;

    public AdtVideoLayout(Context context) {
        this(context, null);
    }

    public AdtVideoLayout(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public AdtVideoLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initMediaView((Activity) context);
        initSkipView((Activity) context);
        initProgressView((Activity) context);
        initCloseView((Activity) context);
        initPlayINButton((Activity) context);
    }

    public void setEventListener(OnViewEventListener listener) {
        mListener = listener;
    }

    public void addToLayout(RelativeLayout layout) {
        if (getParent() != null) {
            ViewGroup group = (ViewGroup) getParent();
            group.removeView(this);
        }

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        layout.addView(this, params);
    }

    public void setLayoutAllClick(int vpc) {
        if (vpc == 1) {
            setOnClickListener(this);
        }
    }

    public void setViewVisibilityWhenSkip(int skip) {
        if (skip == 1) {
            mTxtSkip.setVisibility(View.GONE);
            mPrgVideo.setVisibility(View.VISIBLE);
        } else {
            mTxtSkip.setVisibility(View.VISIBLE);
            mPrgVideo.setVisibility(View.GONE);
        }
    }

    public void updateViewVisibilityWhenVideoComplete() {
        mTxtSkip.setVisibility(View.GONE);
        mPrgVideo.setVisibility(View.GONE);
    }

    public void updateCloseBtnVisibility(final boolean isBackEnable) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (isBackEnable) {
                    if (mDrawCrossMarkView != null) {
                        mDrawCrossMarkView.setVisibility(View.VISIBLE);
                        ObjectAnimator animator = ObjectAnimator.ofFloat(mDrawCrossMarkView,
                                "alpha", 0f, 1f);
                        animator.setDuration(500);
                        animator.start();
                    }
                } else {
                    if (mDrawCrossMarkView != null) {
                        mDrawCrossMarkView.setVisibility(View.GONE);
                    }
                }
            }
        };
        postDelayed(runnable, 3000);
    }

    public void updateProgressTxt(boolean isPause, int skip, int d) {
        if (!isPause) {
            if (skip == 0) {
                if (d == 0) {
                    int currentPosition = mVideoView.getCurrentPosition();
                    int duration = mVideoView.getDuration();
                    int left = duration - currentPosition;
                    mTxtSkip.setText(String.format(Locale.getDefault(), "%d", left / 1000));
                } else {
                    String text = mTxtSkip.getText().toString();
                    if (TextUtils.isEmpty(text)) {
                        mTxtSkip.setText(String.format(Locale.getDefault(), "%d", d));
                        return;
                    }

                    int videoLeft = Integer.parseInt(mTxtSkip.getText().toString());
                    if (videoLeft == 0) {
                        mVideoView.stopPlayback();
                        updateViewVisibilityWhenVideoComplete();
                        if (mListener != null) {
                            mListener.onVideoCompletion();
                        }
                        return;
                    }

                    videoLeft = videoLeft - 1;
                    mTxtSkip.setText(String.format(Locale.getDefault(), "%d", videoLeft));
                }
            } else {
                if (mPrgVideo != null) {
                    mPrgVideo.setProgress(mVideoProgress);
                    if (mVideoProgress != 0) {
                        mVideoProgress = mPrgVideo.getProgress() - 72;
                    }
                }
            }

        }
    }

    public void updatePlayINButtonVisibility(int visibility) {
        if (mPlayINButton != null) {
            mPlayINButton.setVisibility(visibility);
        }
    }

    public CustomVideoView getMediaView() {
        return mVideoView;
    }

    public void destroy() {
        if (mVideoView != null) {
            mVideoView.setOnCompletionListener(null);
            mVideoView.setOnPreparedListener(null);
            mVideoView.setMediaController(null);
            mVideoView = null;
        }

        removeView(mVideoView);
        removeView(mDrawCrossMarkView);
        removeView(mTxtSkip);
        removeView(mPrgVideo);
    }

    private void initMediaView(Activity activity) {
        mVideoView = new CustomVideoView(activity);
        //if not the activity's context， will result in error: unable to add window -- token null is not valid
        MediaController controller = new MediaController(activity);
        controller.setVisibility(View.GONE);
        mVideoView.setMediaController(controller);
        addView(mVideoView);
        int orientation = DensityUtil.getDirection(activity);
        if (Configuration.ORIENTATION_LANDSCAPE == orientation) {
            mVideoView.getLayoutParams().height = LayoutParams.MATCH_PARENT;
            mVideoView.getLayoutParams().width = LayoutParams.WRAP_CONTENT;
            //forces landscape
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            mVideoView.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
            mVideoView.getLayoutParams().width = LayoutParams.MATCH_PARENT;
            //forces portrait
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        ((LayoutParams) mVideoView.getLayoutParams()).addRule(RelativeLayout.CENTER_IN_PARENT);
    }

    private void initSkipView(Activity activity) {
        mTxtSkip = new TextView(activity);
        mTxtSkip.setTextColor(Color.WHITE);
        mTxtSkip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        mTxtSkip.setPadding(0, 30, 30, 0);
        addView(mTxtSkip);
        LayoutParams txtParams = (LayoutParams) mTxtSkip.getLayoutParams();
        txtParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mTxtSkip.setLayoutParams(txtParams);
    }

    private void initProgressView(Activity activity) {
        mPrgVideo = new ProgerssView(activity);
        int size = DensityUtil.dip2px(activity, 30);
        mPrgVideo.setProgress(360);
        addView(mPrgVideo);
        mPrgVideo.getLayoutParams().width = size;
        mPrgVideo.getLayoutParams().height = size;
        ((LayoutParams) mPrgVideo.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        ((LayoutParams) mPrgVideo.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_TOP);
        ((LayoutParams) mPrgVideo.getLayoutParams()).setMargins(0, 30, 30, 0);
        mPrgVideo.bringToFront();
        mPrgVideo.setOnClickListener(this);
    }

    private void initCloseView(Activity activity) {
        //back button
        mDrawCrossMarkView = new DrawCrossMarkView(activity, Color.GRAY);
        mDrawCrossMarkView.setOnClickListener(this);

        int size2 = DensityUtil.dip2px(activity, 20);
        LayoutParams p = new LayoutParams(size2, size2);
        p.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        p.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        p.setMargins(30, 30, 30, 30);
        mDrawCrossMarkView.setLayoutParams(p);
        mDrawCrossMarkView.setVisibility(View.GONE);
        addView(mDrawCrossMarkView);
    }

    private void initPlayINButton(Activity activity) {
        mPlayINButton = new PlayINButton(activity);
        int size2 = DensityUtil.dip2px(activity, 60);
        LayoutParams p = new LayoutParams(size2, size2);
        p.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        p.addRule(RelativeLayout.CENTER_VERTICAL);
        p.setMargins(30, 30, 30, 30);
        mPlayINButton.setLayoutParams(p);
        mPlayINButton.setVisibility(GONE);
        mPlayINButton.setOnClickListener(this);
        addView(mPlayINButton);
    }

    @Override
    public void onClick(View v) {
        if (v == mPrgVideo) {
            if (mPrgVideo.getProgress() == 0) {
                mVideoView.stopPlayback();
                updateViewVisibilityWhenVideoComplete();
                if (mListener != null) {
                    mListener.onProgressViewClick();
                }
            }
        } else if (v == mDrawCrossMarkView) {
            if (mListener != null) {
                mListener.onCloseViewClick();
            }
        } else if (v == this) {
            if (mListener != null) {
                mListener.onAdLayoutClick();
            }
        } else if (v == mPlayINButton) {
            updateViewVisibilityWhenVideoComplete();
            if (mListener != null) {
                mListener.onPlayINButtonClick();
            }
        }
    }

    public interface OnViewEventListener {
        void onProgressViewClick();

        void onCloseViewClick();

        void onAdLayoutClick();

        void onPlayINButtonClick();

        void onVideoCompletion();
    }
}
