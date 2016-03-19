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
    public static final String TIME_ONE_KEY = "time_one";
    public static final String TIME_TWO_KEY = "time_two";
    public static final String TIME_THREE_KEY = "time_three";
    public static final String DIRECTION_KEY = "direction";


    final Context context = WidgetSettings.this;

    int from = 0; //default from Eindhoven
    int to;
    int depart; //departure numberpicker value

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_settings);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

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
     * Update the time picker when selecting a new destination
     */
//    public void updateDeparturesSettings() {
//        String[] departTimes = currentDeparturesSettings();
//        NumberPicker npDep; //NP for close departure times
//        npDep = (NumberPicker) findViewById(R.id.numberPickerDeparturesSettings);
//        npDep.setDisplayedValues(departTimes);
//        npDep.setValue(2); //set default option
//        depart = 2; //set chosen value to default
//        if (to == from) {
//            setDividerColor(npDep, 0);
//        } else {
//            setDividerColor(npDep, ContextCompat.getColor(this, R.color.divider));
//        }
//    }

    private void setDividerColor(NumberPicker picker, int color) {

        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(picker, colorDrawable);
                } catch (IllegalArgumentException | Resources.NotFoundException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    /**
     * @return string array of current departures, empty strings if to==from
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
     * @return calendar array of current departures, null objects if to==from
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
                    dep[i + 2] = arrivalTimeCal(16, 30 * i);
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



    public void setWidget(View view){

        String direction = direction(from, to);
        saveDirection(context, direction);

        Calendar cal = currentDeparturesCalSettings()[2];
        cal.add(Calendar.MINUTE,1);

        Intent intent = new Intent(context, Receiver.class);
//        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);

        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 180000, pIntent);
        Log.e("setWidget", "alarm set at " + cToString(cal));

//        saveTimeOne(context, cToString(currentDeparturesCalSettings()[1]));
//        saveTimeTwo(context, cToString(currentDeparturesCalSettings()[2]));
//        saveTimeThree(context, cToString(currentDeparturesCalSettings()[3]));

        // We need to broadcast an APPWIDGET_UPDATE to our appWidget
        // so it will update the user name TextView.
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
        // Done with Configure, finish Activity.
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

    static void saveTimeOne(Context context, String timeOne) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(
                PREFS_NAME, 0).edit();
        prefs.putString(TIME_ONE_KEY, timeOne);
        prefs.commit();
    }

    static void saveTimeTwo(Context context, String timeTwo) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(
                PREFS_NAME, 0).edit();
        prefs.putString(TIME_TWO_KEY, timeTwo);
        prefs.commit();
    }

    static void saveTimeThree(Context context, String timeThree) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(
                PREFS_NAME, 0).edit();
        prefs.putString(TIME_THREE_KEY, timeThree);
        prefs.commit();
    }

    static String loadDirection(Context context){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String direction = prefs.getString(DIRECTION_KEY, "Settings");
        return direction;
    }

    static String loadTimeOne(Context context){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String timeOne = prefs.getString(TIME_ONE_KEY,"Time One");
        return timeOne;
    }

    static String loadTimeTwo(Context context){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME,0);
        String timeTwo = prefs.getString(TIME_TWO_KEY,"Time Two");
        return timeTwo;
    }

    static String loadTimeThree(Context context){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME,0);
        String timeThree = prefs.getString(TIME_THREE_KEY, "Time Three");
        return timeThree;
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

