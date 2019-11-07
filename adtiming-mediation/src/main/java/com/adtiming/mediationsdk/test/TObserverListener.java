// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.test;

import com.adtiming.mediationsdk.utils.model.BaseInstance;
import com.adtiming.mediationsdk.utils.model.Placement;

import java.util.List;

public interface TObserverListener {
    void updateLoadInstance(BaseInstance ins);

    void updateReadyInstance(BaseInstance ins);

    void updateFailedInstance(BaseInstance ins);

    void updateShowInstance(BaseInstance ins);

    void updateReadyInstanceInLoading(BaseInstance ins);

    void updateInsConfig(BaseInstance instance, int loadLimit, int currentAvailableAdsCount, int loadCount);

    void updatePlacementConfig(Placement placement);

    void updateCLks(List<String> clks);

    void updateLoadFailed(String placementId, String errorMessage);

    void updateShowFailed(String placementId, String errorMessage);
}
