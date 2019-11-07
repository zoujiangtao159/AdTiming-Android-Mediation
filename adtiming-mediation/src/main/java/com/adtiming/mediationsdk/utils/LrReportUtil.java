// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils;

import com.adtiming.mediationsdk.core.AdTimingManager;
import com.adtiming.mediationsdk.core.Instance;

/**
 * 
 */
public final class LrReportUtil {

    private static final int ADT = 0;

    private static final String INSTANCE_LOAD = "1";
    private static final String INSTANCE_READY = "2";
    private static final String ALL_LOAD = "3";
    private static final String ALL_READY = "4";
    private static final String INVALID_REQUEST = "8";
    private static final String ALL_USELESS_REQUEST = "9";
    private static final String INSTANCE_USELESS_REQUEST = "10";
    private static final String IIMPR = "11";//instance impression
    private static final String ICLICK = "12";//instance click

    public static void aUselessReport(String placementId, Instance instance) {
        DeveloperLog.LogD("aUselessReport placementId is : " + placementId + " instances : "
                + instance.toString());
        MediationRequest.httpLr(placementId, 0, AdTimingManager.LOAD_TYPE.MANUAL,
                instance.getId(), instance.getMediationId(), ALL_USELESS_REQUEST);
    }

    /**
     * placementId load success reporting
     *
     * @param placementId 
     */
    public static void aReadyReport(String placementId, AdTimingManager.LOAD_TYPE loadType) {
        DeveloperLog.LogD("aReadyReport placementId is : " + placementId + " load type is : "
                + loadType.name() + " instancesId : " + 0 + " mediation is : " + ADT);
        MediationRequest.httpLr(placementId, 0, loadType, 0, ADT, ALL_READY);
    }

    public static void invalidReport(String placementId) {
        DeveloperLog.LogD("invalidReport placementId is : " + placementId + " instancesId : "
                + 0 + " mediation is : " + ADT);
        MediationRequest.httpLr(placementId, 0, AdTimingManager.LOAD_TYPE.UNKNOWN, 0,
                ADT, INVALID_REQUEST);
    }

    public static void iUselessReport(String placementId, Instance instance) {
        if (instance == null || instance.getMediationId() == ADT) {
            return;
        }
        DeveloperLog.LogD("IUselessReport placementId is : " + placementId + " instances : "
                + instance.toString());
        MediationRequest.httpLr(placementId, 0, AdTimingManager.LOAD_TYPE.MANUAL, instance.getId(),
                instance.getMediationId(), INSTANCE_USELESS_REQUEST);
    }

    /**
     * instance load reporting
     */
    public static void iLoadReport(String placementId, AdTimingManager.LOAD_TYPE loadType, Instance instance) {
        if (instance == null || instance.getMediationId() == ADT) {
            return;
        }
        DeveloperLog.LogD("ILoadReport placementId is : " + placementId + " load type is : "
                + loadType.name() + " instances : " + instance.toString());
        MediationRequest.httpLr(placementId, 0, loadType, instance.getId(), instance.getMediationId(),
                INSTANCE_LOAD);
    }

    /**
     * instance load success reporting
     */
    public static void iReadyReport(String placementId, AdTimingManager.LOAD_TYPE loadType, Instance instance) {
        if (instance == null || instance.getMediationId() == ADT) {
            return;
        }
        DeveloperLog.LogD("IReadyReport placementId is : " + placementId + " load type is : "
                + loadType.name() + " instances : " + instance.toString());
        MediationRequest.httpLr(placementId, 0, loadType, instance.getId(), instance.getMediationId(),
                INSTANCE_READY);
    }

    /**
     * instance impression reporting
     */
    public static void iImpressionReport(String placementId, int sceneId, Instance instance) {
        if (instance == null || instance.getMediationId() == ADT) {
            return;
        }
        DeveloperLog.LogD("insImpReport placementId is : " + placementId + " scene is : "
                + sceneId + " instances : " + instance.toString());
        MediationRequest.httpLr(placementId, sceneId, AdTimingManager.LOAD_TYPE.UNKNOWN, instance.getId(),
                instance.getMediationId(), IIMPR);
    }

    /**
     * instance click reporting
     */
    public static void iClickReport(String placementId, int sceneId, Instance instance) {
        if (instance == null || instance.getMediationId() == ADT) {
            return;
        }
        DeveloperLog.LogD("insClickReport placementId is : " + placementId + " scene is : "
                + sceneId + " instances : " + instance.toString());
        MediationRequest.httpLr(placementId, sceneId, AdTimingManager.LOAD_TYPE.UNKNOWN, instance.getId(),
                instance.getMediationId(), ICLICK);
    }
}
