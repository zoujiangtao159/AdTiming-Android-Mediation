// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.adt.demo;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adtiming.adt.demo.utils.NewApiUtils;
import com.adtiming.mediationsdk.AdTimingAds;
import com.adtiming.mediationsdk.InitCallback;
import com.adtiming.mediationsdk.banner.BannerAd;
import com.adtiming.mediationsdk.banner.BannerAdListener;
import com.adtiming.mediationsdk.interactive.AdTimingInteractiveAd;
import com.adtiming.mediationsdk.interactive.InteractiveAdListener;
import com.adtiming.mediationsdk.interstitial.AdTimingInterstitialAd;
import com.adtiming.mediationsdk.interstitial.InterstitialAdListener;
import com.adtiming.mediationsdk.nativead.AdIconView;
import com.adtiming.mediationsdk.nativead.AdInfo;
import com.adtiming.mediationsdk.nativead.MediaView;
import com.adtiming.mediationsdk.nativead.NativeAd;
import com.adtiming.mediationsdk.nativead.NativeAdListener;
import com.adtiming.mediationsdk.nativead.NativeAdView;
import com.adtiming.mediationsdk.utils.error.AdTimingError;
import com.adtiming.mediationsdk.video.AdTimingRewardedVideo;
import com.adtiming.mediationsdk.video.RewardedVideoListener;

public class MainActivity extends Activity {

    private Button rewardVideoButton;
    private Button interstitialButton;
    private Button interactiveButton;
    private Button bannerButton;
    private Button nativeButton;


    private LinearLayout adContainer;
    private View adView;
    private NativeAdView nativeAdView;


    private BannerAd bannerAd;
    private NativeAd nativeAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NewApiUtils.ENABLE_LOG = true;
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorStatuBar));
        }
        rewardVideoButton = findViewById(R.id.btn_reward_video);
        interstitialButton = findViewById(R.id.btn_interstitial);
        interactiveButton = findViewById(R.id.btn_interactive);
        bannerButton = findViewById(R.id.btn_banner);
        nativeButton = findViewById(R.id.btn_native);
        adContainer = findViewById(R.id.ad_container);
        initSDK();
        if (AdTimingRewardedVideo.isReady()) {
            setRewardVideoButtonStat(true);
        }
        if (AdTimingInterstitialAd.isReady()) {
            setInterstitialButtonStat(true);
        }
        if (AdTimingInteractiveAd.isReady()) {
            setInteractiveButtonStat(true);
        }

    }


    private void initSDK() {
        NewApiUtils.printLog("start init sdk");
        AdTimingAds.init(this, NewApiUtils.APPKEY, new InitCallback() {
            @Override
            public void onSuccess() {
                NewApiUtils.printLog("init success");
                setVideoListener();
                setInterstitialListener();
                setInteractiveListener();
            }

            @Override
            public void onError(AdTimingError result) {
                NewApiUtils.printLog("init failed " + result.toString());

            }
        });
    }

    private void setVideoListener() {
        AdTimingRewardedVideo.setAdListener(new RewardedVideoListener() {
            @Override
            public void onRewardedVideoAvailabilityChanged(boolean available) {
                if (available) {
                    setRewardVideoButtonStat(true);
                }
            }

            @Override
            public void onRewardedVideoAdShowed() {

            }

            @Override
            public void onRewardedVideoAdShowFailed(AdTimingError error) {

            }

            @Override
            public void onRewardedVideoAdClicked() {

            }

            @Override
            public void onRewardedVideoAdClosed() {

            }

            @Override
            public void onRewardedVideoAdStarted() {

            }

            @Override
            public void onRewardedVideoAdEnded() {

            }

            @Override
            public void onRewardedVideoAdRewarded() {

            }
        });
    }


    private void setInterstitialListener() {
        AdTimingInterstitialAd.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialAdAvailabilityChanged(boolean available) {
                if (available) {
                    setInterstitialButtonStat(true);
                }
            }

            @Override
            public void onInterstitialAdShowed() {

            }

            @Override
            public void onInterstitialAdShowFailed(AdTimingError error) {

            }

            @Override
            public void onInterstitialAdClosed() {

            }

            @Override
            public void onInterstitialAdClicked() {

            }
        });
    }

    private void setInteractiveListener() {
        AdTimingInteractiveAd.setAdListener(new InteractiveAdListener() {
            @Override
            public void onInteractiveAdAvailabilityChanged(boolean available) {
                if (available) {
                    setInteractiveButtonStat(true);
                }
            }

            @Override
            public void onInteractiveAdShowed() {

            }

            @Override
            public void onInteractiveAdShowFailed(AdTimingError error) {

            }

            @Override
            public void onInteractiveAdClosed() {

            }
        });
    }


    public void showRewardVideo(View view) {
        AdTimingRewardedVideo.showAd();
        setRewardVideoButtonStat(false);
    }

    public void showInterstitial(View view) {
        AdTimingInterstitialAd.showAd();
        setInterstitialButtonStat(false);
    }

    public void showInteractive(View view) {
        AdTimingInteractiveAd.showAd();
        setInteractiveButtonStat(false);

    }

    public void loadAndShowBanner(View view) {
        adContainer.removeAllViews();
        bannerButton.setEnabled(false);
        bannerButton.setText("Banner Ad Loading...");
        if (bannerAd != null) {
            bannerAd.destroy();
        }
        bannerAd = new BannerAd(this, NewApiUtils.P_BANNER, new BannerAdListener() {
            @Override
            public void onAdReady(View view) {
                try {
                    if (null != view.getParent()) {
                        ((ViewGroup) view.getParent()).removeView(view);
                    }
                    adContainer.removeAllViews();
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                    adContainer.addView(view, layoutParams);
                } catch (Exception e) {
                    Log.e("AdtDebug", e.getLocalizedMessage());
                }
                bannerButton.setEnabled(true);
                bannerButton.setText("Load And Show Banner Ad");
            }

            @Override
            public void onAdFailed(String error) {
                bannerButton.setEnabled(true);
                bannerButton.setText("Banner Load Failed, Try Again");

            }

            @Override
            public void onAdClicked() {

            }
        });
        bannerAd.loadAd();
    }

    public void loadAndShowNative(View view) {
        nativeButton.setEnabled(false);
        nativeButton.setText("Native Ad Loading...");
        if (nativeAd != null) {
            nativeAd.destroy();
        }
        adContainer.removeAllViews();
        nativeAd = new NativeAd(this, NewApiUtils.P_NATIVE, new NativeAdListener() {
            @Override
            public void onAdFailed(String msg) {
                nativeButton.setEnabled(true);
                nativeButton.setText("Native Load Failed, Try Again");
            }

            @Override
            public void onAdReady(AdInfo info) {
                adContainer.removeAllViews();
                adView = LayoutInflater.from(MainActivity.this).inflate(R.layout.native_ad_layout, null);


                TextView title = adView.findViewById(R.id.ad_title);
                title.setText(info.getTitle());

                TextView desc = adView.findViewById(R.id.ad_desc);
                desc.setText(info.getDesc());

                Button btn = adView.findViewById(R.id.ad_btn);
                btn.setText(info.getCallToActionText());


                MediaView mediaView = adView.findViewById(R.id.ad_media);

                nativeAdView = new NativeAdView(MainActivity.this);


                AdIconView adIconView = adView.findViewById(R.id.ad_icon_media);


                DisplayMetrics displayMetrics = MainActivity.this.getResources().getDisplayMetrics();
                mediaView.getLayoutParams().height = (int) (displayMetrics.widthPixels / (1200.0 / 627.0));

                nativeAdView.addView(adView);

                nativeAdView.setTitleView(title);
                nativeAdView.setDescView(desc);
                nativeAdView.setAdIconView(adIconView);
                nativeAdView.setCallToActionView(btn);
                nativeAdView.setMediaView(mediaView);


                nativeAd.registerNativeAdView(nativeAdView);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                adContainer.addView(nativeAdView, layoutParams);
                nativeButton.setEnabled(true);
                nativeButton.setText("Load And Show Native Ad");
            }

            @Override
            public void onAdClicked() {

            }
        });

        nativeAd.loadAd();

    }

    private void setRewardVideoButtonStat(boolean isEnable) {
        rewardVideoButton.setEnabled(isEnable);
        if (isEnable) {
            rewardVideoButton.setText("Show Reward Video Ad");
        } else {
            rewardVideoButton.setText("Reward Video Ad Loading...");
        }
    }


    private void setInterstitialButtonStat(boolean isEnable) {
        interstitialButton.setEnabled(isEnable);
        if (isEnable) {
            interstitialButton.setText("Show Interstitial Ad");
        } else {
            interstitialButton.setText("Interstitial Ad Loading...");
        }
    }

    private void setInteractiveButtonStat(boolean isEnable) {
        interactiveButton.setEnabled(isEnable);
        if (isEnable) {
            interactiveButton.setText("Show Interactive Ad");
        } else {
            interactiveButton.setText("Interactive Ad Loading...");
        }
    }

}
