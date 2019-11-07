// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt;

import java.util.HashMap;
import java.util.Map;

public final class ListenerMap {
    private static Map<String, BaseAdListener> mListeners = new HashMap<>();

    public static void addListenerToMap(String placementId, BaseAdListener listener) {
        mListeners.put(placementId, listener);
    }

    public static BaseAdListener getListener(String placementId) {
        return mListeners.get(placementId);
    }

    public static void removeListenerFromMap(String placementId) {
        mListeners.remove(placementId);
    }
}
