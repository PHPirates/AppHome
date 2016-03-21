package com.abbyberkers.apphome;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class WidgetSettings extends AppCompatActivity {

    private static final String PREFS_NAME = "com.abbyberkers.apphome.WidgetSettings";
    public static final String DIRECTION_KEY = "direction";


    final Context context = WidgetSettings.this;

    int from = 0; //default from Eindhoven
    int to;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_settings);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        Log.e("settings onCreate", "log?");

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
//                updateDeparturesSettings();
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
//                updateDeparturesSettings();
            }
        });

        //get all the current departures to show in numberpicker
        String[] departTimes = currentDeparturesSettings();

    }

    /**
     * @return string array of current departures, empty strings if from == to
     */
    public String[] currentDeparturesSettings() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        Calendar[] calendars = currentDeparturesCalSettings();
        String[] deps = new String[calendars.length];
        for (int i = 0; i < calendars.length; i++) {
            if (calendars[i] != null) { //if from == to, add empty strings
                deps[i] = simpleDateFormat.format(calendars[i].getTime());
            } else {
                deps[i] = " ";
            }
        }
        return deps;
    }

    /**
     * Departure times magik values are in here
     * @return calendar array of current departures, null objects if from == to
     */
    public Calendar[] currentDeparturesCalSettings() {
        int nrDepTimes = 5; //number of departure times
        int lo = -2; //lower and higher bound of for loops
        int hi = 3;

        //initialise calendar with current time and trim
        Calendar c = Calendar.getInstance();
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Calendar[] dep = new Calendar[nrDepTimes];

        int EHV = 0;
        int Heeze = 1;
        int RDaal = 2;

        //arrivalTime(14,0) gives next departure time when departure is :14 each half hour
        if (from == EHV) {
            if (to == Heeze) {
                for (int i = lo; i < hi; i++) {
                    //generate departure times using travel time, back and forth in time
                    dep[i + 2] = arrivalTimeCal(4, 30 * i);
                }
            } else if (to == RDaal) {
                for (int i = lo; i < hi; i++) {
                    dep[i + 2] = arrivalTimeCal(1, 30 * i);
                }
            }
        } else if (from == Heeze) {
            if (to == EHV || to == RDaal) {
                for (int i = lo; i < hi; i++) {
                    dep[i + 2] = arrivalTimeCal(15, 30 * i);
                }
            }
        } else if (from == RDaal) {
            if (to == EHV || to == Heeze) {
                for (int i = lo; i < hi; i++) {
                    dep[i + 2] = arrivalTimeCal(50, 30 * i);
                }
            }
        }

        return dep;
    }


    Calendar nextDeparture(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        int minutes = cal.get(Calendar.MINUTE);
        int depart = 0;

        int EHV = 0;
        int Heeze = 1;
        int RDaal = 2;

        //arrivalTime(14,0) gives next departure time when departure is :14 each half hour
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

        String direction = direction(from, to);
        saveDirection(context, direction);

        Calendar cal = nextDeparture();
        cal.add(Calendar.MINUTE,1);

        Intent intent = new Intent(context, Receiver.class);

        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 180000, pIntent);
        Log.e("setWidget", "alarm set at " + cToString(cal));

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
     * @param c calendar object, probably the one chosen by the time nrpicker
     * @return added travel time to calendar object and converted to string
     */
    public String cToString(Calendar c) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        return simpleDateFormat.format(c.getTime());
    }

    /**
     * departure time should be the first departure time in a given hour
     * @param depart Train departure time
     * @param offset Total travel time
     * @return departure time with offset
     */

    public Calendar arrivalTimeCal(int depart, int offset) {
        //initialise calendar with current arrivalTime and trim
        Calendar c = Calendar.getInstance();
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        //arrivalTime next train
        int minutes = c.get(Calendar.MINUTE);

        if (minutes < depart) {
            minutes = depart; //next train at e.g. :20
        } else if (minutes < depart + 30) {
            minutes = 30 + depart; //add up to the next train departure at e.g. :50
        } else { //minutes>depart+30
            minutes = depart + 60; //train departure in the next hour, :20
        }

        //set calendar
        c.set(Calendar.MINUTE, minutes);
        c.add(Calendar.MINUTE, offset); //add travel time

        return c;
    }

    static void saveDirection(Context context, String direction){
        SharedPreferences.Editor prefs = context.getSharedPreferences(
                PREFS_NAME,0).edit();
        prefs.putString(DIRECTION_KEY, direction);
        prefs.commit();
    }

    static String loadDirection(Context context){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String direction = prefs.getString(DIRECTION_KEY, "Settings");
        return direction;
    }

    String direction(int from, int to){
        if(from == 0){
            if(to == 1){
                return "Eindhoven - Heeze";
            } else if(to == 2){
                return "Eindhoven - Roosendaal";
            }
        } else if(from == 1){
            if(to == 0){
                return "Heeze - Eindhoven";
            } else if(to == 2){
                return "Heeze - Roosendaal";
            }
        } else {
            if(to == 0){
                return "Roosendaal - Heeze";
            } else if(to == 1){
                return "Roosendaal - Eindhoven";
            }
        }
        return "from - to";
    }
}

