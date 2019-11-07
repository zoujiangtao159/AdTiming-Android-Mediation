# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
#-keepattributes SourceFile,LineNumberTable

# SDK API
-keep class com.adtiming.mediationsdk.AdTimingAds{*;}
-keep class com.adtiming.mediationsdk.AdTimingAds$AD_TYPE{*;}
-keep class com.adtiming.mediationsdk.InitCallback{*;}
-keep class com.adtiming.mediationsdk.utils.Constants{*;}
-keep class com.adtiming.mediationsdk.core.AdTimingManager{*;}
-keep class com.adtiming.mediationsdk.banner.**{*;}
-keep class com.adtiming.mediationsdk.interactive.**{*;}
-keep class com.adtiming.mediationsdk.interstitial.**{*;}
-keep class com.adtiming.mediationsdk.nativead.**{*;}
-keep class com.adtiming.mediationsdk.video.**{*;}
-keep class com.adtiming.mediationsdk.mediation.**{*;}
-keep class com.adtiming.mediationsdk.utils.**{*;}

# Mediation interface
-keep class com.adtiming.mediationsdk.mobileads.**{*;}

# Manifest Activity
-keep class com.adtiming.mediationsdk.adt.BaseActivity{*;}
-keep class com.adtiming.mediationsdk.adt.AdtActivity{*;}
-keepattributes *Annotation*

# AdTiming Test
-keep class com.adtiming.mediationsdk.test.**{*;}
-keep class com.adtiming.mediationsdk.core.Instance{*;}
