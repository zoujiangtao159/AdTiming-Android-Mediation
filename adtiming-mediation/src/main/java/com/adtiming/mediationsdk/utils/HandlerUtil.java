// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

public class HandlerUtil {
    private HandlerUtil() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public interface OnReceiveMessageListener {
        void handlerMessage(Message msg);
    }

    public static class HandlerHolder extends Handler {
        WeakReference<OnReceiveMessageListener> mListenerWeakReference;

        /**
         * Attention: implement this in Activity or classes within Activity. Do NOT use anonymous class for it may be GC'ed.
         *
         * @param listener 
         */
        public HandlerHolder(OnReceiveMessageListener listener) {
            this(listener, Looper.myLooper());
        }

        public HandlerHolder(OnReceiveMessageListener listener, Looper looper) {
            super(looper);
            if (listener != null) {
                mListenerWeakReference = new WeakReference<>(listener);
            }
        }

        @Override
        public void handleMessage(Message msg) {
            if (mListenerWeakReference != null) {
                OnReceiveMessageListener listener = mListenerWeakReference.get();
                if (listener != null) {
                    listener.handlerMessage(msg);
                }
            }
        }
    }

    public static void runOnUiThread(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            new HandlerHolder(null, Looper.getMainLooper()).postDelayed(runnable, 0);
        }
    }
}
