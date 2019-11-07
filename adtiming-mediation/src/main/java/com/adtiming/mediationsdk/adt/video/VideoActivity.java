// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.video;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.adtiming.mediationsdk.adt.BaseActivity;
import com.adtiming.mediationsdk.adt.report.AdReport;
import com.adtiming.mediationsdk.adt.utils.PUtils;
import com.adtiming.mediationsdk.adt.video.view.AdtVideoLayout;
import com.adtiming.mediationsdk.adt.video.view.CustomVideoView;
import com.adtiming.mediationsdk.adt.video.view.PlayINView;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.HandlerUtil;
import com.adtiming.mediationsdk.utils.cache.Cache;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;
import com.adtiming.mediationsdk.utils.request.network.Request;
import com.adtiming.mediationsdk.utils.request.network.Response;
import com.adtiming.mediationsdk.utils.webview.AdJSInterface;
import com.adtiming.mediationsdk.utils.webview.AdWebView;
import com.adtiming.mediationsdk.utils.webview.AdtWebView;
import com.adtiming.mediationsdk.utils.webview.VideoJsCallback;

import org.json.JSONObject;

/**
 * 
 */
public class VideoActivity extends BaseActivity implements MediaPlayer.OnPreparedListener, AdtVideoLayout.OnViewEventListener,
        MediaPlayer.OnCompletionListener, VideoJsCallback, VolumeListener, Request.OnRequestCallback {
    private static final String TYPE_PLAYIN_TERMINATE = "playInTerminate";
    private static final String TYPE_PLAYIN_CLOSE = "playInCloseAction";
    private static final String TYPE_PLAYIN_CLICK = "playInInstallAction";
    private static final String TYPE_PLAYIN_ERROR = "playInError";

    private CustomVideoView mVideoView;
    private VideoListener mListener;
    private AdWebView mPlayINView;
    private AdtVideoLayout mLytVideo;

    private boolean isVideoCompletion = false;
    private boolean isFullyWatched = true;
    private boolean isBackEnable = true;
    private int mVideoDuration = 0;
    private int mVideoSkip = 0;
    private boolean isPause = false;
    private int mVideoStopPosition = 0;
    private HandlerUtil.HandlerHolder mHandler;
    private UIRunnable mRunnable;
    private AdJSInterface mJsInterface;
    private VolumeBroadcastReceiver mReceiver;

    private int mFirstQuartile;
    private int mMid;
    private int mThirdQuartile;
    private int mPiCheckPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (mAdListener.get() != null) {
                mListener = (VideoListener) mAdListener.get();
            }
            Intent intent = getIntent();
            mVideoDuration = intent != null ? intent.getIntExtra("vd", 0) : 0;
            mVideoSkip = intent != null ? intent.getIntExtra("vskp", 0) : 0;

            mHandler = new HandlerUtil.HandlerHolder(null);
            mRunnable = new UIRunnable();
            if (mListener != null) {
                mListener.onVideoAdShowed(mPlacementId);
            }

            registerVolumeChangeReceiver();

        } catch (Throwable e) {
            DeveloperLog.LogD("VideoActivity", e);
            CrashUtil.getSingleton().saveException(e);
            onShowError(e.getLocalizedMessage());
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        try {
            int currentPosition = mVideoView.getCurrentPosition();
            int duration = mVideoView.getDuration();
            int left = (duration - currentPosition) / 1000;
            if (mVideoDuration > left) {
                mVideoDuration = left;
            }
            mFirstQuartile = mVideoDuration / 4;
            mMid = mVideoDuration / 2;
            mThirdQuartile = mVideoDuration * 3 / 4;
            //
            mPiCheckPoint = mVideoDuration * mAdBean.getPiCp() / 100;

            mLytVideo.updateProgressTxt(isPause, mVideoSkip, mVideoDuration);
            mHandler.post(mRunnable);

            mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mp) {
                    if (isPause && mVideoView != null) {
                        mVideoView.start();
                        isPause = false;
                    }
                }
            });
        } catch (Exception e) {
            DeveloperLog.LogD("VideoActivity", e);
            CrashUtil.getSingleton().saveException(e);
            onShowError(e.getLocalizedMessage());
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        isFullyWatched = true;
        setVideoCompletion(true);
        mLytVideo.updateViewVisibilityWhenVideoComplete();
    }

    @Override
    protected void onPause() {
        if (mVideoView != null) {
            mVideoStopPosition = mVideoView.getCurrentPosition();
            mVideoView.pause();
            VideoUtil.reportVideoEvents(this, mPlacementId, mAdBean, VideoEvents.PAUSE);
            isPause = true;
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mVideoView != null && !isVideoCompletion && mVideoStopPosition != 0) {
            mVideoView.resume();
            VideoUtil.reportVideoEvents(this, mPlacementId, mAdBean, VideoEvents.RESUME);
            mVideoView.seekTo(mVideoStopPosition);
        }
    }

    @Override
    public void onBackPressed() {
        if (isVideoCompletion) {
            VideoUtil.reportVideoEvents(this, mPlacementId, mAdBean, VideoEvents.CLOSE);
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        //adds adClose callback
        callbackAdCloseOnUIThread();

        if (mLytVideo != null) {
            mLytVideo.destroy();
        }
        if (mLytAd != null) {
            mLytAd.removeAllViews();
        }

        if (mJsInterface != null) {
            mJsInterface.onDestroy();
            mJsInterface = null;
        }

        if (mAdView != null) {
            mAdView.clearHistory();
            AdtWebView.getInstance().destroy(mAdView, "sdk");
        }

        if (mPlayINView != null) {
            PlayINView.getInstance().destroy("sdk");
        }

        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
            mRunnable = null;
            mHandler = null;
        }
        if (mReceiver != null) {
            mReceiver.unregisterReceiver(this);
        }
        super.onDestroy();
    }

    @Override
    protected void initViewAndLoad(String impUrl) {
        super.initViewAndLoad(impUrl);
        mLytVideo = new AdtVideoLayout(this);
        mLytVideo.addToLayout(mLytAd);
        mLytVideo.setEventListener(this);
        Uri uri = Uri.fromFile(Cache.getCacheFile(this, mAdBean.getVideoUrl(), null));
        if (uri == null) {
            onShowError("Cann't find video path to display");
            return;
        }
        setUpMediaView(uri);
        VideoUtil.reportVideoEvents(this, mPlacementId, mAdBean, VideoEvents.START);

        mLytVideo.setLayoutAllClick(mAdBean.getVpc());
        mLytVideo.setViewVisibilityWhenSkip(mVideoSkip);

        mAdView.setVisibility(View.GONE);

        if (mJsInterface == null) {
            mJsInterface = new AdJSInterface(mPlacementId,
                    mAdBean.getOriData(), this);
        }
        AdtWebView.getInstance().addJsInterface(mAdView, mJsInterface, "sdk");

        mAdView.loadUrl(impUrl);

        AdReport.impReport(this, mPlacementId, mAdBean, false);
    }

    @Override
    protected void callbackWhenClose() {
        super.callbackWhenClose();
        if (mListener == null) {
            return;
        }
        if (isFullyWatched) {
            mListener.onVideoAdRewarded(mPlacementId);
        }
        mListener.onVideoAdClose(mPlacementId, isFullyWatched);
    }

    @Override
    protected void callbackWhenError(String error) {
        super.callbackWhenError(error);
        if (mListener != null) {
            mListener.onVideoAdFailed(mPlacementId, error);
        }
    }

    @Override
    protected void callbackWhenClick() {
        super.callbackWhenClick();
        if (mListener != null) {
            mListener.onVideoAdClicked(mPlacementId);
        }
    }

    @Override
    public void close() {
        finish();
    }

    @Override
    public void click() {
        callbackAdClickOnUIThread();
        AdReport.CLKReport(this, mPlacementId, mAdBean);
        PUtils.doClick(this, mPlacementId, mAdBean);
    }

    @Override
    public void showClose() {
        isBackEnable = true;
        mLytVideo.updateCloseBtnVisibility(isBackEnable);
    }

    @Override
    public void hideClose() {
        isBackEnable = false;
        mLytVideo.updateCloseBtnVisibility(isBackEnable);
    }

    @Override
    public void addEvent(String event) {
        if (mListener != null) {
            mListener.onVideoAdEvent(mPlacementId, event);
        }
    }

    @Override
    public void wvClick() {
        callbackAdClickOnUIThread();
        AdReport.CLKReport(this, mPlacementId, mAdBean);
    }

    @Override
    public void postMessage(String msg) {
        //TODO
        try {
            JSONObject object = new JSONObject(msg);
            String type = object.optString("type");
            if (TextUtils.isEmpty(type)) {
                return;
            }
            if (TextUtils.equals(type, TYPE_PLAYIN_TERMINATE) || TextUtils.equals(type, TYPE_PLAYIN_ERROR)) {
                mPlayINView.setVisibility(View.GONE);
                mAdView.setVisibility(View.VISIBLE);
            } else if (TextUtils.equals(type, TYPE_PLAYIN_CLICK)) {
                click();
            } else if (TextUtils.equals(type, TYPE_PLAYIN_CLOSE)) {
                onBackPressed();
            }
        } catch (Exception e) {
            DeveloperLog.LogE("PlayIN postMessage ", e);
        }
    }

    private void initPlayIN() {
        mPlayINView = PlayINView.getInstance().getAdView();
        if (mPlayINView.getParent() != null) {
            ViewGroup group = (ViewGroup) mPlayINView.getParent();
            group.removeView(mPlayINView);
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        mLytAd.addView(mPlayINView, params);

        if (mJsInterface == null) {
            mJsInterface = new AdJSInterface(mPlacementId,
                    mAdBean.getOriData(), this);
        }
        PlayINView.getInstance().addJsInterface(mJsInterface, "sdk");

//        //TODO:url的拼接
//        mPlayINView.setWebViewClient(new PlayINWebClient(mPlayINView, mAdBean.getPiHs()));
//        mPlayINView.setVisibility(View.GONE);
//
//        mPlayINView.loadUrl("https://www.baidu.com/");
    }

    private void setUpMediaView(Uri path) {
        mVideoView = mLytVideo.getMediaView();
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnCompletionListener(this);

        mVideoView.setVideoURI(path);
        mVideoView.start();
    }

    private void registerVolumeChangeReceiver() {
        if (mAdBean.getVes() == null) {
            return;
        }

        if (mReceiver == null) {
            mReceiver = new VolumeBroadcastReceiver(this);
        }
        mReceiver.registerReceiver(this);
    }

    /**
     * sets completion except for PlayIn buttons
     */
    private void setVideoCompletion(boolean isComplete) {
        if (isComplete) {
            VideoUtil.reportVideoEvents(this, mPlacementId, mAdBean, VideoEvents.COMPLETE);
            mAdView.setVisibility(View.VISIBLE);
            mAdView.loadUrl("javascript:sdk_show()");
        } else {
            mVideoView.stopPlayback();
            mPlayINView.setVisibility(View.VISIBLE);
            mPlayINView.loadUrl("javascript:sdk_show()");
            VideoUtil.reportVideoEvents(this, mPlacementId, mAdBean, VideoEvents.SKIP);
        }
        isVideoCompletion = true;
        mVideoView.setVisibility(View.GONE);
        mHandler.removeCallbacks(mRunnable);
        mLytVideo.updatePlayINButtonVisibility(View.GONE);
        mLytVideo.updateCloseBtnVisibility(isBackEnable);
    }

    private void showLpWhenPlayINError() {
        if (mAdView != null) {
            mAdView.setVisibility(View.VISIBLE);
        }
    }

    private void onShowError(String error) {
        callbackAdShowFailedOnUIThread(error);
        callbackAdCloseOnUIThread();
        finish();
    }

    @Override
    public void onMuted() {
        VideoUtil.reportVideoEvents(this, mPlacementId, mAdBean, VideoEvents.MUTE);
    }

    @Override
    public void onUnMuted() {
        VideoUtil.reportVideoEvents(this, mPlacementId, mAdBean, VideoEvents.UNMUTE);
    }

    @Override
    public void onRequestSuccess(Response response) {
        if (response == null || response.code() != 200) {
            //PlayIN not available
            return;
        }
        //draws a clickable button
        if (mLytVideo != null) {
            mLytVideo.updatePlayINButtonVisibility(View.VISIBLE);
        }
        PlayINView.getInstance().init();
    }

    @Override
    public void onRequestFailed(String error) {
        //PlayIN not available
    }

    @Override
    public void onProgressViewClick() {
        isFullyWatched = false;
        setVideoCompletion(true);
    }

    @Override
    public void onCloseViewClick() {
        onBackPressed();
    }

    @Override
    public void onAdLayoutClick() {
        callbackAdClickOnUIThread();
        AdReport.CLKReport(this, mPlacementId, mAdBean);
        PUtils.doClick(this, mPlacementId, mAdBean);
    }

    @Override
    public void onPlayINButtonClick() {
        initPlayIN();
    }

    @Override
    public void onVideoCompletion() {
        setVideoCompletion(true);
    }

    private class UIRunnable implements Runnable {
        private int index = 0;

        @Override
        public void run() {
            mHandler.postDelayed(this, 1000);
            if (mAdView == null) {
                return;
            }
            if (mAdView.getVisibility() == View.VISIBLE) {
                return;
            }
            if (mPiCheckPoint != 0 && mPiCheckPoint == index) {
                //TODO:check PlayIN available
//                VideoUtil.checkPlayINAvailable(mAdBean.getPiHs(), VideoActivity.this);
//                if (mLytVideo != null) {
//                    mLytVideo.updatePlayINButtonVisibility(View.VISIBLE);
//                    PlayINView.getInstance().init();
//                }
            }
            if (index == mFirstQuartile) {
                VideoUtil.reportVideoEvents(VideoActivity.this, mPlacementId, mAdBean, VideoEvents.FIRST_QUARTILE);
            } else if (index == mMid) {
                VideoUtil.reportVideoEvents(VideoActivity.this, mPlacementId, mAdBean, VideoEvents.MID_POINT);
            } else if (index == mThirdQuartile) {
                VideoUtil.reportVideoEvents(VideoActivity.this, mPlacementId, mAdBean, VideoEvents.THIRD_QUARTILE);
            }
            index++;
            if (mLytVideo != null) {
                mLytVideo.updateProgressTxt(isPause, mVideoSkip, mVideoDuration);
            }
        }
    }

    private class PlayINWebClient extends WebViewClient {
        private String mOriginUrl;
        private boolean isPageFinish;

        PlayINWebClient(WebView webView, String url) {
            this.mOriginUrl = url;
            webView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isPageFinish) {
                        showLpWhenPlayINError();
                    }
                }
            }, 3000);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            isPageFinish = true;
            if (mAdView != null && mAdView.getVisibility() == View.VISIBLE) {
                return;
            }
            setVideoCompletion(false);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if (TextUtils.equals(view.getUrl(), mOriginUrl)) {
                showLpWhenPlayINError();
            }
        }
    }
}
