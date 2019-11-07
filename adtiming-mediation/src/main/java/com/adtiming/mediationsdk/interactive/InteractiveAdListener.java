// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.interactive;

import com.adtiming.mediationsdk.utils.error.AdTimingError;

/**
 * Listener for interactive events. Implementers of this interface will receive events for interactive ads
 *
 * 
 */
public interface InteractiveAdListener {

    /**
     * called when interactiveAd availability changed
     *
     * @param available represent interactiveAd available status
     */
    void onInteractiveAdAvailabilityChanged(boolean available);

    /**
     * called when interactiveAd is shown
     */
    void onInteractiveAdShowed();

    /**
     * called when interactiveAd show failed
     *
     * @param error Interactive ads show failed reason
     */
    void onInteractiveAdShowFailed(AdTimingError error);

    /**
     * called when interactiveAd closes
     */
    void onInteractiveAdClosed();

}
