package com.abbyberkers.apphome;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {
    /**
     * when boot is received
     *
     * @param context context
     * @param i       intent
     */
    @Override
    public void onReceive(Context context, Intent i) {
        //load direction from shared preferences
        int[] direction = WidgetSettings.loadDirection(context);

        //update time is one minute after next departure time
        Calendar cal = nextDeparture(direction);
        cal.add(Calendar.MINUTE, 1);

        //TODO intent to ?
        Intent intent = new Intent(context, Receiver.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        //set alarm to update every half an hour
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 1800000, pIntent);
    }

    /**
     * duplicate from nextDeparture in WidgetSettings
     * next departure is used to set alarm to update the widget
     * @param direction direction
     * @return next departure time calendar
     */
    static Calendar nextDeparture(int[] direction) {
        //some initialisation
        int EHV = 0;
        int Heeze = 1;
        int RDaal = 2;
        int from = direction[0];
        int to = direction[1];

        //set update time according to departure times without delay
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        int minutes = cal.get(Calendar.MINUTE);
        int depart = 0;

        //get departure time
        if (from == EHV) {
            if (to == Heeze) {
                depart = 4;
            } else if (to == RDaal) {
                depart = 1;
            }
        } else if (from == Heeze) {
            if (to == EHV || to == RDaal) {
                depart = 15;
            }
        } else if (from == RDaal) {
            if (to == EHV || to == Heeze) {
                depart = 20;
            }
        }

        //set minutes to minutes of next departure
        if (minutes < depart) {
            cal.set(Calendar.MINUTE, depart);
        } else if (depart < minutes && minutes < depart + 30) {
            cal.set(Calendar.MINUTE, depart + 30);
        } else {
            cal.set(Calendar.MINUTE, depart);
            cal.add(Calendar.MINUTE, 60);
        }

        return cal;
    }
}
