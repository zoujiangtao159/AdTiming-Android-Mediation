// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.video;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

import com.adtiming.mediationsdk.utils.DeveloperLog;

/**
 * 
 */
public final class VolumeBroadcastReceiver extends BroadcastReceiver {
    private VolumeListener mListener;
    private boolean isMuted;

    VolumeBroadcastReceiver(VolumeListener listener) {
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            return;
        }
        String action = intent.getAction();
        if (VideoEvents.VOLUME_CHANGED_ACTION.equals(action) &&
                intent.getIntExtra(VideoEvents.EXTRA_VOLUME_STREAM_TYPE, -1) == AudioManager.STREAM_MUSIC) {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            int volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            DeveloperLog.LogD("volume changed : " + volume);
            if (volume == 0) {
                if (isMuted) {
                    return;
                }
                isMuted = true;
                if (mListener != null) {
                    mListener.onMuted();
                }
            } else {
                if (isMuted) {
                    isMuted = false;
                    if (mListener != null) {
                        mListener.onMuted();
                    }
                }
            }
        } else if (AudioManager.RINGER_MODE_CHANGED_ACTION.equals(action)) {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            final int ringerMode = am.getRingerMode();
            switch (ringerMode) {
                case AudioManager.RINGER_MODE_NORMAL:
                    DeveloperLog.LogD("ringerMode changed : RINGER_MODE_NORMAL");
                    if (isMuted) {
                        isMuted = false;
                        if (mListener != null) {
                            mListener.onMuted();
                        }
                    }
                    //normal
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    DeveloperLog.LogD("ringerMode changed : RINGER_MODE_VIBRATE");
                    if (isMuted) {
                        isMuted = false;
                        if (mListener != null) {
                            mListener.onMuted();
                        }
                    }
                    //vibrate
                    break;
                case AudioManager.RINGER_MODE_SILENT:
                    DeveloperLog.LogD("ringerMode changed : RINGER_MODE_SILENT");
                    if (isMuted) {
                        return;
                    }
                    isMuted = true;
                    if (mListener != null) {
                        mListener.onMuted();
                    }
                    //silent
                    break;
                default:
                    break;
            }
        }
    }

    public void registerReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(VideoEvents.VOLUME_CHANGED_ACTION);
        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        context.registerReceiver(this, filter);
    }

    public void unregisterReceiver(Context context) {
        context.unregisterReceiver(this);
    }
}
