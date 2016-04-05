package com.abbyberkers.apphome;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class WidgetSettings extends AppCompatActivity {

    private static final String PREFS_NAME = "com.abbyberkers.apphome.WidgetSettings";
    public static final String DIRECTION_KEY = "direction";


    final Context context = WidgetSettings.this;

    int EHV = 0;
    int Heeze = 1;
    int RDaal = 2;

    int from = 0; //default from Eindhoven
    int to;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_settings);

        String[] cities = new String[]{"Eindhoven", "Heeze", "Roosendaal"};

        NumberPicker npFrom;
        npFrom = (NumberPicker) findViewById(R.id.numberPickerFromSettings);

        npFrom.setMinValue(0);
        npFrom.setMaxValue(cities.length - 1);
        npFrom.setDisplayedValues(cities);
        npFrom.setWrapSelectorWheel(true);
        npFrom.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        npFrom.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                from = newVal;
            }
        });

        NumberPicker npTo;

        npTo = (NumberPicker) findViewById(R.id.numberPickerToSettings);

        npTo.setMinValue(0);
        npTo.setMaxValue(cities.length - 1);
        npTo.setDisplayedValues(cities);
        npTo.setWrapSelectorWheel(true);
        npTo.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        npTo.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                to = newVal;
            }
        });

    }

    Calendar nextDeparture(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        int minutes = cal.get(Calendar.MINUTE);
        int depart = 0;

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

    public void setWidget(View view){

//        String direction = direction(from, to);
        saveDirection(context, from, to);

        Calendar cal = nextDeparture();
        cal.add(Calendar.MINUTE, 1);

        Intent intent = new Intent(context, Receiver.class);

        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 1800000, pIntent);

        AppWidgetManager appWidgetManager = AppWidgetManager
                .getInstance(context);
        ComponentName thisAppWidget = new ComponentName(context
                .getPackageName(), WidgetProvider.class.getName());
        Intent updateIntent = new Intent(context, WidgetProvider.class);
        int[] appWidgetIds = appWidgetManager
                .getAppWidgetIds(thisAppWidget);
        updateIntent
                .setAction("android.appwidget.action.APPWIDGET_UPDATE");
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                appWidgetIds);
        context.sendBroadcast(updateIntent);
        // Done with Configure, finish all activities.
        finishAffinity();

    }

    /**
     * @param c calendar object
     * @return c in a string
     */
    public String cToString(Calendar c) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        return simpleDateFormat.format(c.getTime());
    }

    static void saveDirection(Context context, String direction){
        SharedPreferences.Editor prefs = context.getSharedPreferences(
                PREFS_NAME,0).edit();
        prefs.putString(DIRECTION_KEY, direction);
        prefs.commit();
    }

    static void saveDirection(Context context, int from, int to) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(
                PREFS_NAME, 0).edit();
        prefs.putInt("from", from);
        prefs.putInt("to", to);
        prefs.apply();
    }

    static int[] loadDirection(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        int[] fromto = new int[2];
        fromto[0] = prefs.getInt("from", -1);
        fromto[1] = prefs.getInt("to", -1);
        return fromto;
    }

    String direction(int from, int to){
        if(from == EHV){
            if(to == Heeze){
                return "Eindhoven - Heeze";
            } else if(to == RDaal){
                return "Eindhoven - Roosendaal";
            }
        } else if(from == Heeze){
            if(to == EHV){
                return "Heeze - Eindhoven";
            } else if(to == RDaal){
                return "Heeze - Roosendaal";
            }
        } else {
            if(to == EHV){
                return "Roosendaal - Eindhoven";
            } else if(to == Heeze){
                return "Roosendaal - Heeze";
            }
        }
        return "from - to";
    }
}

