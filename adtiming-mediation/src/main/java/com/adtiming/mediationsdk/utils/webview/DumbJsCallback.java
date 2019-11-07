// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.webview;

/**
 * 
 */
public class DumbJsCallback implements IAJsCallback {

	private static DumbJsCallback instance = null;

	public static DumbJsCallback getSingleton() {
		if (instance == null) {
			synchronized (DumbJsCallback.class) {
				if (instance == null) {
					instance = new DumbJsCallback();
				}
			}
		}
		return instance;
	}

	private DumbJsCallback() {

	}

	@Override
	public boolean isVideoReady() {
		return false;
	}

	@Override
	public boolean playVideo() {
		return false;
	}

	@Override
	public void loadVideo() {

	}

	/**
	 * callback when ad close
	 */
	@Override
	public void close() {

	}

	/**
	 * callback when ad click
	 */
	@Override
	public void click() {

	}

	/**
	 * callback when close btn can be showed
	 */
	@Override
	public void showClose() {

	}

	/**
	 * callback when close btn should be closed
	 */
	@Override
	public void hideClose() {

	}

	@Override
	public void addEvent(String event) {

	}

	@Override
	public void wvClick() {

	}
}
