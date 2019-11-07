# coding=utf-8
# -*- coding: utf-8 -*-
import os, shutil

mAssembleRelease = '{} {}:assembleRelease --daemon'
mClean = "{} {}:clean --daemon"
mBuildAARPath = '{}/{}/build/outputs/aar/{}.aar'
mSavePath = '{}/AdTimingSDK'
mSaveAARPath = '{}/{}.aar'
mGradlewPath = None

VERSION_ADTIMING = '6.0.0'
ADAPTER_VERSION_ADMOB = '3.1.2'
ADAPTER_VERSION_FACEBOOK = '3.2.0'
ADAPTER_VERSION_UNITY = '3.2.2'
ADAPTER_VERSION_VUNGLE = '3.2.0'
ADAPTER_VERSION_ADCOLONY = '3.1.1'
ADAPTER_VERSION_APPLOVIN = '3.1.1'
ADAPTER_VERSION_MOPUB = '3.2.0'
ADAPTER_VERSION_CHARTBOOST = '3.1.2'
ADAPTER_VERSION_TAPJOY = '3.1.1'

FILE_NAME_ADTIMING_SDK = 'AdTimingSDK' + '-' + VERSION_ADTIMING
FILE_NAME_ADAPTER_ADMOB = 'AdmobAdapter' + '-' + ADAPTER_VERSION_ADMOB
FILE_NAME_ADAPTER_FACEBOOK = 'FacebookAdapter' + '-' + ADAPTER_VERSION_FACEBOOK
FILENAME_ADAPTER_UNITY = 'UnityAdapter' + '-' + ADAPTER_VERSION_UNITY
FILENAME_ADAPTER_VUNGLE = 'VungleAdapter' + '-' + ADAPTER_VERSION_VUNGLE
FILENAME_ADAPTER_ADCOLONY = 'AdColonyAdapter' + '-' + ADAPTER_VERSION_ADCOLONY
FILENAME_ADAPTER_APPLOVIN = 'AppLovinAdapter' + '-' + ADAPTER_VERSION_APPLOVIN
FILENAME_ADAPTER_CHARTBOOST = 'ChartboostAdapter' + '-' + ADAPTER_VERSION_CHARTBOOST
FILENAME_ADAPTER_TAPJOY = 'TapjoyAdapter' + '-' + ADAPTER_VERSION_TAPJOY
FILENAME_ADAPTER_MOPUB = 'MopubAdapter' + '-' + ADAPTER_VERSION_MOPUB

mBuildModelMap = {'adtiming-mediation': FILE_NAME_ADTIMING_SDK,
                  'adcolony': FILENAME_ADAPTER_ADCOLONY,
                  'admob': FILE_NAME_ADAPTER_ADMOB,
                  'mopub': FILENAME_ADAPTER_MOPUB,
                  'tapjoy': FILENAME_ADAPTER_TAPJOY,
                  'applovin': FILENAME_ADAPTER_APPLOVIN,
                  'chartboost': FILENAME_ADAPTER_CHARTBOOST,
                  'facebook': FILE_NAME_ADAPTER_FACEBOOK,
                  'unity': FILENAME_ADAPTER_UNITY,
                  'vungle': FILENAME_ADAPTER_VUNGLE}


def runCMD(cmd=None):
    if cmd == None:
        return
    print(cmd)
    popen = os.popen(cmd)
    report = popen.read()
    print(report)


def findfile(start, name):
    for relpath, dirs, files in os.walk(start):
        if name in files:
            full_path = os.path.join(start, relpath, name)
            return os.path.normpath(os.path.abspath(full_path))


def finddir(start, name):
    for relpath, dirs, files in os.walk(start):
        if name in dirs:
            full_path = os.path.join(start, relpath, name)
            return os.path.normpath(os.path.abspath(full_path))


if __name__ == '__main__':
    thisPath = os.path.abspath(os.path.dirname(__file__))
    upPath = os.path.abspath(os.path.join(os.path.dirname(__file__), os.path.pardir))
    mGradlewPath = findfile(thisPath, "gradlew")
    if mGradlewPath == None:
        mGradlewPath = findfile(upPath, "gradlew")
        if mGradlewPath == None:
            print ("Gradlew file does not exist")
            exit(0)
        else:
            thisPath = upPath
    print("GradlewPath:{}".format(mGradlewPath))

    pathMap = {}
    for modelName in mBuildModelMap:
        modelPath = finddir(thisPath, modelName)
        if modelPath == None:
            print ("Path does not exist:{}".format(modelName))
            exit(0)
        else:
            pathMap[modelName] = modelPath.replace(thisPath, '').replace('\\', ':').replace('/',
                                                                                            ':')

    mSavePath = mSavePath.format(thisPath)
    if os.path.exists(mSavePath):
        shutil.rmtree(mSavePath)

    print("CleanRuning...")
    for modelName in pathMap:
        runCMD(mClean.format(mGradlewPath, pathMap[modelName]))

    print("BuildRuning...")
    for modelName in pathMap:
        runCMD(mAssembleRelease.format(mGradlewPath, pathMap[modelName]))
        aarPath = findfile(thisPath, modelName + ".aar")
        if aarPath == None:
            print ("{} build Fail!".format(modelName))
        else:
            if not os.path.exists(mSavePath):
                os.makedirs(mSavePath)
            outputPath = mSaveAARPath.format(mSavePath, mBuildModelMap[modelName])
            shutil.copyfile(aarPath, outputPath)
            print ("{} build Success:{}".format(modelName, outputPath))

    print("BuildFinished!")
