# AdTiming Mediation SDK for Android
![AdTiming](https://github.com/AdTiming/AdTiming-Android-Mediation/blob/master/AdTimingLogo.jpg?raw=true)
---

Thanks for taking a look at AdTiming! We committed to providing flexible and easy-to-use monetization solution that works across multiple platforms.

Sign up for an account at [http://www.adtiming.com](http://www.adtiming.com).

## **Table of Contents**
---
- Introduction
- Pull Request
- Mediation
- More Help
- Requirements
- New in this Version

## **Introduction**
---
AdTiming Mediation Platform offers diversified and competitive monetization services and supports a variety of Ad formats including Native Ad, Interstitial Ad, Banner Ad, and Rewarded Video Ad. The AdTiming Mediation Platform works with multiple ad networks include AdMob, Facebook, UnityAds, Vungle, Tapjoy, AppLovin, AdColony, Chartboost and MoPub etc.

The newly designed AdTiming Mediation SDK (Version 6.0.0 and above) equipped with smart ad inventory mechanism and optimized SDK initialization pre-loading mechanism provides the best mediation solution for Mobile app developers. For Rewarded Video and Interstitial Ad, the SDK will regularly check inventory and automatically load ads, including pre-loading, loading after ad served, timing loading and more mechanisms, to maintain ad inventory and developers do not need to invoke load() method themselves. The new APIs are easier to use.

## **Pull Request**

Thank you for submitting pull requests to the AdTiming Mediation SDK Android GitHub repository. Our team regularly monitors and investigates all submissions for inclusion in our official SDK releases. Please note that AdTiming does not directly merge these pull requests at this time. Please reach out to your account team or support@adtiming.com if you have further questions.


For integration instructions, please see the [Android SDK Integration](https://support.adtiming.com/hc/en-us/articles/360033763553-Android-SDK-Integration) and [Add Mediation Networks](https://support.adtiming.com/hc/en-us/articles/360033233554-Add-Mediation-Networks) guide.

## **Cloned GitHub repository**

Alternatively, you can obtain the AdTiming Mediation SDK source by cloning the git repository:

```bash
git clone git@github.com:AdTiming/AdTiming-Android-Mediation.git
```

For additional integration instructions, please see the  [Android SDK Integration](https://support.adtiming.com/hc/en-us/articles/360033763553-Android-SDK-Integration) guide.

## **Mediation**
---
**Interested in Mediation?** After Integrate our Rewarded Video, Interstitial Ads in your app and follow our [Add Mediation Networks](https://support.adtiming.com/hc/en-us/articles/360033233554-Add-Mediation-Networks) and [Mediation Integration Guides](https://support.adtiming.com/hc/en-us/articles/360033590914-Mediation-Network-Guides) to mediation networks. 


## **Proguard rules**

```
 -dontwarn com.adtiming.mediationsdk.**.*
 
 -dontwarn com.mopub.**.*
 
 -dontoptimize
 
 -dontskipnonpubliclibraryclasses
 
 #adtiming
 
 -keep class com.adtiming.mediationsdk.mediation.**{ *; }
 
 -keep class com.adtiming.mediationsdk.mobileads.**{ *; }
 
 -keep class com.mopub.**{ *; }
 
 #R
 
 -keepclassmembers class **.R$* {
     public static <fields>;
 }
 
 -keepattributes *Annotation *,InnerClasses
 
 -keepnames class * implements android.os.Parcelable {
     public static final ** CREATOR;
}
```

## **More Help**
---
You can find more help on our [developer help site](https://support.adtiming.com/hc/en-us/categories/360001950234-SDK-Integration) for SDK integration, ad units and ad network mediation guide.

To file an issue with our team [Open Ticket](https://support.adtiming.com/hc/en-us/requests/new) or email [support@adtiming.com](mailto:support@adtiming.com).

## **Requirements**
We support Android Operating Systems Version 4.1 (API Level 16) and up. Be sure to:

-     Use Android Studio 2.0 and up
-     Target Android API level 28
-     MinSdkVersion level 16 and up

## **New in this Version**
- Features
   - Smart ad inventroy to cache more than one ad in inventory
   - Automaticly ad load without invoke loadAd() yourself
   - With new init method has ad unit type as parameter, you can init ad unit at any checkpoint in app





