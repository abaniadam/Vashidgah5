package com.vashidgah.calendar;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import org.json.JSONArray;
import org.json.JSONObject;

public class Notifier {
    static final String CH = "reminders";
    static final String PREF = "notif";

    static void createChannel(Context c) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(CH, "یادآوری‌ها", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.createNotificationChannel(ch);
        }
    }

    private static void cancelAll(Context c, AlarmManager am) {
        int n = c.getSharedPreferences(PREF, 0).getInt("n", 0);
        for (int i = 0; i < n; i++) {
            PendingIntent pi = PendingIntent.getBroadcast(c, i, new Intent(c, ReminderReceiver.class),
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
            if (pi != null) {
                am.cancel(pi);
                pi.cancel();
            }
        }
    }

    // json: [{"t": epochMillis, "title": "...", "text": "..."}, ...]
    public static void schedule(Context c, String json) {
        try {
            AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
            cancelAll(c, am);
            JSONArray a = new JSONArray(json);
            long now = System.currentTimeMillis();
            int k = 0;
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                long t = o.getLong("t");
                if (t <= now) continue;
                Intent it = new Intent(c, ReminderReceiver.class);
                it.putExtra("title", o.optString("title"));
                it.putExtra("text", o.optString("text"));
                it.putExtra("id", k);
                PendingIntent pi = PendingIntent.getBroadcast(c, k, it,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, pi);
                k++;
            }
            c.getSharedPreferences(PREF, 0).edit().putString("json", json).putInt("n", k).apply();
        } catch (Exception e) {
            // ignore malformed data
        }
    }
}
