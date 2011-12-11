/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.CompoundButton;

/**
 * TODO: Listen for changes to the setting.
 */
public class WifiController extends BroadcastReceiver implements
        CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "StatusBar.WifiController";

    private Context mContext;
    private CompoundButton mCheckBox;

    private boolean mWifi;

    public WifiController(Context context, CompoundButton checkbox) {
        mContext = context;
        mWifi = isWifiOn(context);
        mCheckBox = checkbox;
        checkbox.setChecked(mWifi);
        checkbox.setOnCheckedChangeListener(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        context.registerReceiver(this, filter);
    }

    public void onCheckedChanged(CompoundButton view, boolean checked) {
        if (checked != mWifi) {
            changeWifiState(checked);
        }
    }

    public boolean isWifiOn(Context context) {
        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            switch (wifiManager.getWifiState()) {
                case WifiManager.WIFI_STATE_ENABLED:
                case WifiManager.WIFI_STATE_ENABLING:
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    private void changeWifiState(final boolean desiredState) {
        mWifi = desiredState;
        final WifiManager wifiManager = (WifiManager) mContext
                .getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            Log.d("WifiButton", "No wifiManager.");
            return;
        }

        AsyncTask.execute(new Runnable() {
            public void run() {
                int wifiApState = wifiManager.getWifiApState();
                if (desiredState
                        && ((wifiApState == WifiManager.WIFI_AP_STATE_ENABLING) || (wifiApState == WifiManager.WIFI_AP_STATE_ENABLED))) {
                    wifiManager.setWifiApEnabled(null, false);
                }

                wifiManager.setWifiEnabled(desiredState);
                return;
            }
        });
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent
                .getAction())) {
            return;
        }
        int wifiState = intent
                .getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);

        switch (wifiState) {
            case WifiManager.WIFI_STATE_DISABLED:
            case WifiManager.WIFI_STATE_ENABLED:
                mCheckBox.setEnabled(true);
                break;
            case WifiManager.WIFI_STATE_DISABLING:
            case WifiManager.WIFI_STATE_ENABLING:
            case WifiManager.WIFI_STATE_UNKNOWN:
            default:
                mCheckBox.setEnabled(false);
                break;
        }

    }
}
