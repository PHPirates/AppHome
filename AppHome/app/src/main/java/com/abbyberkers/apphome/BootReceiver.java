package com.abbyberkers.apphome;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {
    private BaseClass baseClass;

    /**
     * when boot is received
     *
     * @param context context
     * @param i       intent
     */
    @Override
    public void onReceive(Context context, Intent i) {
        if (i.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            baseClass = new BaseClass();

            //load direction from shared preferences
            int[] direction = WidgetSettings.loadDirection(context);

            //update time is one minute after next departure time
            Calendar cal = nextDeparture(direction);
            cal.add(Calendar.MINUTE, 1);

            // intent to start Receiver which updates the alarm in its onReceive()
            Intent intent = new Intent(context, Receiver.class);
            PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

            //set alarm to update every half an hour
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 1800000, pIntent);
        }
    }

    /**
     * duplicate from nextDeparture in WidgetSettings
     * next departure is used to set alarm to update the widget
     * @param direction direction
     * @return next departure time calendar
     */
    private Calendar nextDeparture(int[] direction) {
        return baseClass.nextDeparture(direction);
    }
}
