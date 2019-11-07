// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.constant;


import com.adtiming.mediationsdk.utils.device.DeviceUtil;

/**
 * KeyConstants
 * 
 * 
 * 
 */
public class KeyConstants {

    /**
     * Keys for server request body json+gzip+aes
     */
    public interface RequestBody {
        /*@see System.currentTimeMillis() long      app time in mills 1567479919643  must */
        String KEY_TS = "ts";

        /*@see DeviceUtil.getTimeZoneOffset    int       TimeZoneOffset in minutes, e.g. UTC+0800 zo=480 must */
        String KEY_ZO = "zo";

        /*@see DeviceUtil.getTimeZone          String    localTimeZone.name,TimeZone.getDefault().getID()     Asia/Shanghai︎ must*/
        String KEY_TZ = "tz";

        /*String    session ID, UUID generated at App init   BBD6E1CD-8C4B-40CB-8A62-4BBC7AFE07D6  must*/
        String KEY_SESSION = "session";

        /*String    unique user ID by SDK   BBD6E1CD-8C4B-40CB-8A62-4BBC7AFE07D6*/
        String KEY_UID = "uid";

        /*String    device ID, combined with dtype  BBD6E1CD-8C4B-40CB-8A62-4BBC7AFE07D6*/
        String KEY_DID = "did";

        /*int       device type, 1:IDFA, 2:GAID, 3:FBID, 4:HUAWEIID   1*/
        String KEY_DTYPE = "dtype";

        /*int      jailbreak;//  0:normal, 1:jailbreak   1*/
        String KEY_JB = "jb";

        /*@see DeviceUtil.getLanguageCountry String    device language code   en_US*/
        String KEY_LANG = "lang";

        /*@see DeviceUtil.getLanguage String    device language name   English*/
        String KEY_LANG_NAME = "langname";

        /*@see DeviceUtil.getCountry  String   [[NSLocale currentLocale]localeIdentifier],Locale.getCountry()     CN*/
        String KEY_LCOUNTRY = "lcountry";

        /*@see AdtUtil.getApplication().getPackageName String   app bunle name  com.xxx.xxx */
        String KEY_BUNDLE = "bundle";

        /*@see Build.MANUFACTURER   String    device make  samsung*/
        String KEY_MAKE = "make";

        /*@see Build.BRAND    String    device brand  samsung*/
        String KEY_BRAND = "brand";

        /*@see Build.MODEL  String    device model  iPhone10,3*/
        String KEY_MODEL = "model";

        /*@see Build.VERSION.RELEASE  String    OS version   12.1*/
        String KEY_OSV = "osv";

        /*@see Build.DEVICE  String    system build #, Android: ro.build.display.id   16A366*/
        String KEY_BUILD = "build";

        /*APP KEY*/
        String KEY_APPK = "appk";

        /*@see DeviceUtil.getVersionName String    App version   1.0*/
        String KEY_APPV = "appv";

        /*@see DensityUtil.getPhoneWidth int  DisplayMetrics.widthPixels    1028 */
        String KEY_W = "width";

        /*@see DensityUtil.getPhoneHeight int    DisplayMetrics.heightPixels  2094*/
        String KEY_H = "height";

        /*String   LAN ip   192.168.1.101*/
        String KEY_LIP = "lip";

        /*@see NetworkChecker.getNetworkType  int       ConnectionType   4*/
        String KEY_CONT = "contype";

        /*@see NetworkChecker.getNetworkOperator  String    NetworkOperatorName with mccmnc NetworkOperatorName   46002China Mobile*/
        String KEY_CARRIER = "carrier";

        /*@see Density.getFM  int     free storage size in MB     17799*/
        String KEY_FM = "fm";

        /*int       in percentage    52*/
        String KEY_BATTERY = "battery";

        /*int       charging battery, 0:No,1:Yes    1*/
        String KEY_BTCH = "btch";

        /*int       low power, 0:No,1:Yes*/
        String KEY_LOWP = "lowp";

        /**********************   for Config(init) server API     ***********************/
        String KEY_ADNS = "adns";           //List<AdNetwork>  mediated AdNetworks
        String KEY_ANDROID = "android";    //Android   for Android-only 

        /************************     for cl,lr server API        ***************************/
        /*int32     placement ID      2345*/
        String KEY_PID = "pid";
        /*int8      load activation type, [1:init,2:interval,3:adclose,4:manual]*/
        String KEY_ACT = "act";

        String KEY_SCENE = "scene";

        /************************     for cl server API       ***************************/
        /*string    AppIDs to be banned, sperated by comma      com.bb,com.ee*/
        String KEY_BA = "ba";
        /*float     IAP, inAppPurchase               1*/
        String KEY_IAP = "iap";
        /*int32      placement Impression Times for the day   5*/
        String KEY_IMPRTIMES = "imprTimes";
        /*int32     noAdt flag, 0:needs ADT, 1:doesn't need ADT   1*/
        String KEY_NA = "na";
        /*int32     noGooglePlay flag, 0 or missing: GP installed, 1:GP not installed   1*/
        String KEY_NG = "ng";

        /************************     for cl server API       ***************************/
        /*AdNetworkID*/
        String KEY_MID = "mid";
        /*InstanceID*/
        String KEY_IID = "iid";
    }

    /**
     * datacache keys
     */
    public interface Storage {
        String KEY_IMP_RECORD = "ImpRecord";
        String KEY_IAP_NUMBER = "c_adt_iap_number";
        String KEY_APP_KEY = "AppKey";
        String KEY_ADT_VERSION = "AdtVersion";
        String KEY_AL = "AL";
        String KEY_GTPID = "GTPid";
        String KEY_DAY_IMP_RECORD = "DayImpRecord";
        String KEY_CONFIG = "Config";
        String KEY_USER_AGENT = "User-Agent";//error:UsageAgent
        String KEY_PACKAGE_NAME = "PackageName";
        String KEY_AD_ID = "AdvertisingId";
        String KEY_CONNECT_TYPE = "ConnectType";
        String KEY_MANUFACTURER = "Manufacturer";
        String KEY_LANG = "Lang";
        String KEY_LANG_CODE = "LangCode";
        String KEY_MODEL = "Model";
        String KEY_BRAND = "Brand";
        String KEY_OS_VERSION = "OSVersion";
        String KEY_NETWORK_OPERATOR = "NetworkOperator";
        String KEY_DEVICE = "Device";
        String KEY_PRODUCT = "Product";
        String KEY_DEVICE_TYPE = "DeviceType";
        String KEY_WIDTH_PIXELS = "WidthPixels";
        String KEY_HEIGHT_PIXELS = "HeightPixels";
        String KEY_SCREEN_SIZE = "ScreenSize";
        String KEY_DENSITY_LEVEL = "DensityLevel";
        String KEY_SDK_VERSION = "SDKVersion";
        String KEY_ANDROID_ID = "AndroidId";
        String KEY_OS_BUILD = "OSBuild";
        String KEY_DISPLAY = "Display";
        String KEY_DISPLAY_ID = "ro.build.display.id";
        String KEY_ARCH = "os.arch";
        String KEY_CPU_ABI = "os.product.cpu.abi";
        String KEY_CPU_ABI2 = "os.product.cpu.abi2";
        String KEY_CPU_TYPE = "CPUType";
        String KEY_NETWORK_ISO = "NetworkIso";
        String KEY_FACEBOOK_ID = "FacebookId";
        String KEY_SCREEN_TYPE = "ScreenType";
        String KEY_XDPI = "xdpi";
        String KEY_YDPI = "ydpi";
        String KEY_DENSITY_DPI = "densityDpi";
        String KEY_SIM_OPERATOR = "SimOperator";
    }

    public static interface AdNetwork {
        String KEY_MID = "mid";             // int       AdNetwork ID       1
        String KEY_ADAPTER_V = "adapterv";//String      AdNetwork Adapter Version      1.0.1
        String KEY_MSDKV = "msdkv";        //String     AdNetwork SDK Version             3.2.1
    }

    public interface Sensor {
        String KEY_ST = "sT";                           //int32             sensor type   2
        String KEY_SV = "sV";                           //string            Invensense Inc.
        String KEY_SVE = "sVE";                         //Array of float   [43.411255, 4.421997, 33.135986]
        String KEY_SVS = "sVS";                         //Array of float   [43.411255, 4.421997, 33.135986]
        String KEY_SN = "sN";                           //string           Invensense Magnetometer
    }

    /**
     * Keys for server request body json+gzip+aes, server init API
     */
    public interface Android {
        /*@see Build.Device     string     Build.DEVICE                           lteub*/
        String KEY_DEVICE = "device";

        /*@see Build.PRODUCT     string     Build.PRODUCT                          a6plteub*/
        String KEY_PRODUCE = "product";

        /* int        [0,1,2]                      2*/
        String KEY_SD = "screen_density";

        /* int       [1,2,3,4]                     2*/
        String KEY_SS = "screen_size";

        /**
         * @see DeviceUtil#getSystemPropertyCPUABI;
         * @see KeyConstants.Device#KEY_RO_CPU_ABI
         * string     ro.product.cpu.abi       armeabi-v7a
         */
        String KEY_CPU_ABI = "cpu_abi";

        /**
         * @see DeviceUtil#getSystemPropertyCPUABI;
         * @see KeyConstants.Device#KEY_RO_CPU_ABI2
         * string     ro.product.cpu.abi2                    armeabi
         */
        String KEY_CPU_ABI2 = "cpu_abi2";

        /**
         * @see DeviceUtil#getSystemPropertyCPUABI;
         * @see KeyConstants.Device#KEY_RO_CPU_ABI_LIST
         * string     ro.product.cpu.abilist
         */
        String KEY_CPU_ABI_LIST = "cpu_abilist";

        /**
         * @see DeviceUtil#getSystemPropertyCPUABI;
         * @see KeyConstants.Device#KEY_RO_CPU_ABI_LIST_32
         * string    ro.product.cpu.abilist32
         */
        String KEY_CPU_ABI_LIST_32 = "cpu_abilist32";

        /**
         * @see DeviceUtil#getSystemPropertyCPUABI;
         * @see KeyConstants.Device#KEY_RO_CPU_ABI_LIST_64
         * string    ro.product.cpu.abilist64
         */
        String KEY_CPU_ABI_LIST_64 = "cpu_abilist64";

        /**
         * @see DeviceUtil#getSystemPropertyCPUABI;
         * @see KeyConstants.Device#KEY_RO_CPU_ABI_LIST_64
         * int32     Android API Level                       26
         */
        String KEY_API_LEVEL = "api_level";

        /*@see getDensityDpi int32      DisplayMetrics.densityDpi              420*/
        String KEY_D_DPI = "d_dpi";

        /*int32     WebViewBridge.getScreenMetrics().size   2*/
        String KEY_DIM_SIZE = "dim_size";

        /*@see DensityUtil.getXDpi  string    DisplayMetrics.xdpi          268.941*/
        String KEY_XDP = "xdp";

        /*@see DensityUtil.getYDpi  string     DisplayMetrics.ydpi         268.694*/
        String KEY_YDP = "ydp";

        /*@see DeviceUtil.getUniquePsuedoID     string     deviceFingerPrintId, getUniquePsuedoID*/
        String KEY_DFPID = "dfpid";

        /*@see DeviceUtil.getTimeZone string     TimeZone.getDefault().getID()         Asia/Shanghai*/
        String KEY_TIME_ZONE = "time_zone";

        /**
         * @see DeviceUtil#getSystemPropertyCPUABI;
         * @see KeyConstants.Device#KEY_RO_ARCH
         * @see Device#KEY_RO_ARCH    string  ro.arch
         */
        String KEY_ARCH = "arch";

        /**
         * @see DeviceUtil#getSystemPropertyCPUABI;
         * @see KeyConstants.Device#KEY_RO_CHIPNAME
         * string     ro.chipname
         */
        String KEY_CHIPNAME = "chipname";

        /**
         * @see DeviceUtil#getSystemPropertyCPUABI;
         * @see KeyConstants.Device#KEY_RO_BRIDGE
         * string     ro.dalvik.vm.native.bridge
         */
        String KEY_BRIDGE = "bridge";

        /**
         * @see DeviceUtil#getSystemPropertyCPUABI;
         * @see KeyConstants.Device#KEY_RO_BRIDGE_EXEC
         * string     ro.enable.native.bridge.exec
         */
        String KEY_BRIDGE_EXEC = "bridge_exec";

        /**
         * @see DeviceUtil#getSystemPropertyCPUABI;
         * @see KeyConstants.Device#KEY_RO_ZYGOTE
         * string     ro.zygote
         */
        String KEY_ZYGOTE = "zygote";

        /**
         * @see DeviceUtil#getSystemPropertyCPUABI;
         * @see KeyConstants.Device#KEY_RO_MOCK_LOCATION
         * string     ro.allow.mock.location
         */
        String KEY_MOCK_LOCATION = "mock_location";

        /**
         * @see DeviceUtil#getSystemPropertyCPUABI;
         * @see KeyConstants.Device#KEY_RO_ISA_ARM
         * string     ro.dalvik.vm.isa.arm
         */
        String KEY_ISA_ARM = "isa_arm";

        /**
         * @see DeviceUtil#getSystemPropertyCPUABI;
         * @see KeyConstants.Device#KEY_RO_BUILD_USER
         * string     ro.build.user
         */
        String KEY_BUILD_USER = "build_user";

        /**
         * @see DeviceUtil#getSystemPropertyCPUABI;
         * @see KeyConstants.Device#KEY_RO_KERNEL_QEMU
         * string     ro.kernel.qemu
         */
        String KEY_KERNEL_QEMU = "kernel_qemu";

        /**
         * @see DeviceUtil#getSystemPropertyCPUABI;
         * @see KeyConstants.Device#KEY_RO_HARDWARE
         * string     ro.hardware
         */
        String KEY_HARDWARE = "hardware";

        /**
         * @see DeviceUtil#getSystemPropertyCPUABI;
         * @see KeyConstants.Device#KEY_NATIVE_BRIDGE
         * string     persist.sys.nativebridge
         */
        String KEY_NATIVE_BRIDGE = "nativebridge";

        /*string     dalvik.vm.isa.x86.features*/
        String KEY_ISA_X86_FEATURES = "isax86_features";

        /*string     dalvik.vm.isa.x86.variant*/
        String KEY_ISA_X86_VARIANT = "isa_x86_variant";

        /*string     dalvik.vm.isa.arm.features*/
        String KEY_ISA_ARM_FEATURES = "isa_arm_features";

        /*string     dalvik.vm.isa.arm.variant*/
        String KEY_ISA_ARM_VARIANT = "isa_arm_variant";

        /*string     dalvik.vm.isa.arm64.features*/
        String KEY_ISA_ARM64_FEATURES = "isa_arm64_features";

        /*string     dalvik.vm.isa.arm64.variant*/
        String KEY_ISA_ARM64_VARIANT = "isa_arm64_variant";

        /*@see DeviceUtil.getSensorSize     int32                                    18*/
        String KEY_SENSOR_SIZE = "sensor_size";

        /*@see DeviceUtil.getSensorList     Array of Sensors              */
        String KEY_SENSORS = "sensors";

        /*@see DeviceUtil.getFacebookId     string     FacebookID*/
        String KEY_FB_ID = "fb_id";

        String KEY_AS = "as";
    }

    /**
     * Keys for SystemProperties
     */
    public interface Device {
        /*@see DeviceUtil.getSystemPropertyCPUABI  string     ro.product.cpu.abi                     armeabi-v7a*/
        String KEY_RO_CPU_ABI = "ro.product.cpu.abi";

        /*@see DeviceUtil.getSystemPropertyCPUABI  string     ro.product.cpu.abi2                    armeabi*/
        String KEY_RO_CPU_ABI2 = "ro.product.cpu.abi2";

        /*@see DeviceUtil.getSystemPropertyCPUABI  string     ro.product.cpu.abilist*/
        String KEY_RO_CPU_ABI_LIST = "ro.product.cpu.abilist";

        /*@see DeviceUtil.getSystemPropertyCPUABI  string    ro.product.cpu.abilist32*/
        String KEY_RO_CPU_ABI_LIST_32 = "ro.product.cpu.abilist";

        /*@see DeviceUtil.getSystemPropertyCPUABI  string    ro.product.cpu.abilist64*/
        String KEY_RO_CPU_ABI_LIST_64 = "ro.product.cpu.abilist64";

        /*@see DeviceUtil.getSystemPropertyCPUABI  string    ro.arch*/
        String KEY_RO_ARCH = "ro.arch";

        /*@see DeviceUtil.getSystemPropertyCPUABI  string    ro.chipname*/
        String KEY_RO_CHIPNAME = "ro.chipname";

        /*@see DeviceUtil.getSystemPropertyCPUABI  string    ro.dalvik.vm.native.bridge*/
        String KEY_RO_BRIDGE = "ro.dalvik.vm.native.bridge";

        /*@see DeviceUtil.getSystemPropertyCPUABI  string    ro.enable.native.bridge.exec*/
        String KEY_RO_BRIDGE_EXEC = "ro.enable.native.bridge.exec";

        /*@see DeviceUtil.getSystemPropertyCPUABI  string    ro.zygote*/
        String KEY_RO_ZYGOTE = "ro.zygote";

        /*@see DeviceUtil.getSystemPropertyCPUABI  string    ro.allow.mock.location*/
        String KEY_RO_MOCK_LOCATION = "ro.allow.mock.location";

        /*@see DeviceUtil.getSystemPropertyCPUABI  string    ro.dalvik.vm.isa.arm*/
        String KEY_RO_ISA_ARM = "ro.dalvik.vm.isa.arm";

        /*@see DeviceUtil.getSystemPropertyCPUABI  string    ro.build.user*/
        String KEY_RO_BUILD_USER = "ro.build.user";

        /*@see DeviceUtil.getSystemPropertyCPUABI  string    ro.kernel.qemu*/
        String KEY_RO_KERNEL_QEMU = "ro.kernel.qemu";

        /*@see DeviceUtil.getSystemPropertyCPUABI  string    ro.hardware*/
        String KEY_RO_HARDWARE = "ro.hardware";

        /*@see DeviceUtil.getSystemPropertyCPUABI  string    persist.sys.nativebridge*/
        String KEY_NATIVE_BRIDGE = "persist.sys.nativebridge";
    }

    public interface Response {
        /*String AppName */
        String KEY_APP_NAME = "app_name";
        /*int32     AppFileSize     0*/
        String KEY_APP_SIZE = "app_size";
        /*int32     AppRating count     1393613*/
        String KEY_RATING_COUNT = "rating_count";
        /*string    adt video event reporting basic param*/
        String KEY_VQ = "vq";
        /*Object of VideoEvents to report*/
        String KEY_VES = "ves";
        /*Array of string carousel images URLs       */
        String KEY_IMGS = "imgs";
        /*int8      shows adt mark, 0:hide,1:show*/
        String KEY_MK = "mk";
        /*int32    in seconds*/
        String KEY_EXPIRE = "expire";
        /*Object of PlayIn          PlayIn*/
        String KEY_PI = "pi";
        /*Array of HotApp           HotApp*/
        String KEY_RT = "rt";
    }
}
