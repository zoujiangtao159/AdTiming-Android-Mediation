// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.event;


/**
 * 
 */
public interface EventId {

    int INIT_START = 100;
    int INIT_COMPLETE = 101;
    int LOAD = 102;
    int DESTROY = 103;
    int REFRESH_INTERVAL = 110;
    int ATTEMPT_TO_BRING_NEW_FEED = 111;
    int NO_MORE_OFFERS = 112;
    int AVAILABLE_FROM_CACHE = 113;
    int LOAD_BLOCKED = 114;


    /****************************************/


    int INSTANCE_NOT_FOUND = 200;
    int INSTANCE_INIT_START = 201;
    int INSTANCE_INIT_FAILED = 202;
    int INSTANCE_INIT_SUCCESS = 203;
    int INSTANCE_DESTROY = 204;
    int INSTANCE_LOAD = 205;
    int INSTANCE_LOAD_ERROR = 206;
    int INSTANCE_LOAD_NO_FILL = 207;
    int INSTANCE_LOAD_SUCCESS = 208;
    int INSTANCE_READY_TRUE = 209;
    int INSTANCE_READY_FALSE = 210;
    int INSTANCE_LOAD_TIMEOUT = 211;


    /****************************************/


    int INSTANCE_RELOAD = 260;
    int INSTANCE_RELOAD_ERROR = 261;
    int INSTANCE_RELOAD_NO_FILL = 262;
    int INSTANCE_RELOAD_SUCCESS = 263;


    /****************************************/


    int INSTANCE_OPENED = 300;
    int INSTANCE_CLOSED = 301;
    int INSTANCE_SHOW = 302;
    int INSTANCE_SHOW_FAILED = 303;
    int INSTANCE_SHOW_SUCCESS = 304;
    int INSTANCE_VISIBLE = 305;
    int INSTANCE_CLICKED = 306;
    int INSTANCE_VIDEO_START = 307;
    int INSTANCE_VIDEO_CLICK = 308;
    int INSTANCE_VIDEO_COMPLETED = 309;
    int INSTANCE_VIDEO_REWARDED = 310;
    int INSTANCE_END_CARD_DISPLAYED = 311;
    int INSTANCE_CLICK_DOWNLOAD_NOW = 312;
    int INSTANCE_PRESENT_SCREEN = 313;
    int INSTANCE_DISMISS_SCREEN = 314;


    /****************************************/


    int SCENE_CAPPED = 400;
    int INSTANCE_CAPPED = 401;
    int PLACEMENT_CAPPED = 402;
    int SESSION_CAPPED = 403;


    /****************************************/


    int CALLED_LOAD = 500;
    int CALLED_SHOW = 501;
    int CALLED_IS_READY_TRUE = 502;
    int CALLED_IS_READY_FALSE = 503;
    int CALLED_IS_CAPPED_TRUE = 504;
    int CALLED_IS_CAPPED_FALSE = 505;


    /****************************************/


    int CALLBACK_LOAD_SUCCESS = 600;
    int CALLBACK_LOAD_ERROR = 601;
    int CALLBACK_SHOW_FAILED = 602;
    int CALLBACK_CLICK = 603;
    int CALLBACK_LEAVE_APP = 604;
    int CALLBACK_PRESENT_SCREEN = 605;
    int CALLBACK_DISMISS_SCREEN = 606;
    int CALLBACK_SCENE_CAPPED = 607;
}
