package com.vashidgah.calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent i) {
        String json = c.getSharedPreferences(Notifier.PREF, 0).getString("json", null);
        if (json != null) Notifier.schedule(c, json);
    }
}
