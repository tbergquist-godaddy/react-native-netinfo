/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.reactnativecommunity.netinfo;

import android.os.Build;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;

/** Module that monitors and provides information about the connectivity state of the device. */
@ReactModule(name = NetInfoModule.NAME)
public class NetInfoModule extends ReactContextBaseJavaModule implements AmazonFireDeviceConnectivityPoller.ConnectivityChangedCallback {
    public static final String NAME = "RNCNetInfo";

    private final ConnectivityReceiver mConnectivityReceiver;
    private final AmazonFireDeviceConnectivityPoller mAmazonConnectivityChecker;

    private int numberOfListeners = 0;

    public NetInfoModule(ReactApplicationContext reactContext) {
        super(reactContext);
        // Create the connectivity receiver based on the API level we are running on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mConnectivityReceiver = new NetworkCallbackConnectivityReceiver(reactContext);
        } else {
            mConnectivityReceiver = new BroadcastReceiverConnectivityReceiver(reactContext);
        }

        mAmazonConnectivityChecker = new AmazonFireDeviceConnectivityPoller(reactContext, this);
    }

    @Override
    public void initialize() {
        mConnectivityReceiver.register();
        mAmazonConnectivityChecker.register();
    }

    // the upstream method was removed in react-native 0.74
    // this stub remains for backwards compatibility so that react-native < 0.74
    // (which will still call onCatalystInstanceDestroy) will continue to function
    public void onCatalystInstanceDestroy() {
        invalidate();
    }

    // This should have an `@Override` tag, but the method does not exist until
    // react-native >= 0.74, which would cause linting errors across versions
    // once minimum supported react-native here is 0.74+, add the tag
    public void invalidate() {
        mAmazonConnectivityChecker.unregister();
        mConnectivityReceiver.unregister();
        mConnectivityReceiver.hasListener = false;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @ReactMethod
    public void getCurrentState(final String requestedInterface, final Promise promise) {
        mConnectivityReceiver.getCurrentState(requestedInterface, promise);
    }

    @Override
    public void onAmazonFireDeviceConnectivityChanged(boolean isConnected) {
        mConnectivityReceiver.setIsInternetReachableOverride(isConnected);
    }

    @ReactMethod
    public void addListener(String eventName) {
        numberOfListeners++;
        mConnectivityReceiver.hasListener = true;
    }

    @ReactMethod
    public void removeListeners(Integer count) {
        numberOfListeners -= count;
        if (numberOfListeners == 0) {
            mConnectivityReceiver.hasListener = false;
        }
    }
}
