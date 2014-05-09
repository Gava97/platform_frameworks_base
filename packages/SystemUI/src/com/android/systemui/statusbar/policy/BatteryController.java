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
import android.os.BatteryManager;

import java.util.ArrayList;

public class BatteryController extends BroadcastReceiver {
    private static final String TAG = "StatusBar.BatteryController";

    private Context mContext;
    private ArrayList<ImageView> mIconViews = new ArrayList<ImageView>();

    private static final int BATTERY_STYLE_NORMAL         = 0;
    private static final int BATTERY_ICON_STYLE_NORMAL      = R.drawable.tw_stat_sys_battery;
    private static final int BATTERY_ICON_STYLE_CHARGE      = R.drawable.tw_stat_sys_battery_charge;
    
    private boolean mBatteryPlugged = false;
    private int mBatteryStyle;
    private int mBatteryIcon = BATTERY_ICON_STYLE_NORMAL;

    Handler mHandler;

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.STATUS_BAR_BATTERY), false, this);
        }

        @Override public void onChange(boolean selfChange) {
            updateSettings();
        }
    }

    private ArrayList<BatteryStateChangeCallback> mChangeCallbacks =
            new ArrayList<BatteryStateChangeCallback>();

    public interface BatteryStateChangeCallback {
        public void onBatteryLevelChanged(int level, boolean pluggedIn);
    }

    public BatteryController(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        context.registerReceiver(this, filter);
    }

    public void addStateChangedCallback(BatteryStateChangeCallback cb) {
        mChangeCallbacks.add(cb);
    }

    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
            final int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            final int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                    BatteryManager.BATTERY_STATUS_UNKNOWN);

            boolean plugged = false;
            switch (status) {
                case BatteryManager.BATTERY_STATUS_CHARGING:
                case BatteryManager.BATTERY_STATUS_FULL:
                    plugged = true;
                    break;
            }

            for (BatteryStateChangeCallback cb : mChangeCallbacks) {
                cb.onBatteryLevelChanged(level, plugged);
            }
            updateBattery();
        }
    }
    
    private void updateBattery() {
        int mIcon = View.VISIBLE;
        int mIconStyle = BATTERY_ICON_STYLE_NORMAL;

        if (mBatteryStyle == BATTERY_STYLE_NORMAL) {
            mIcon = (View.VISIBLE);
            mIconStyle = mBatteryPlugged ? BATTERY_ICON_STYLE_CHARGE
                    : BATTERY_ICON_STYLE_NORMAL;
        }

        int N = mIconViews.size();
        for (int i=0; i<N; i++) {
            ImageView v = mIconViews.get(i);
            v.setVisibility(mIcon);
            v.setImageResource(mIconStyle);
        }
    }

    private void updateSettings() {
        ContentResolver resolver = mContext.getContentResolver();

        updateBattery();
    }
}
