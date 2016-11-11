package com.abbyberkers.apphome;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;

import java.util.Calendar;

public class WidgetSettings extends AppCompatActivity {
    private BaseClass baseClass;

    private static final String PREFS_NAME = "com.abbyberkers.apphome.WidgetSettings";
//    public static final String DIRECTION_KEY = "direction";

    private final Context context = WidgetSettings.this;

    private int from = 0; //default from Eindhoven
    private int to;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_settings);

        baseClass = new BaseClass();

        //do same nrpicker stuff as in main
        String[] cities = new String[]{"Eindhoven", "Heeze", "Roosendaal"};

        NumberPicker npFrom;
        npFrom = (NumberPicker) findViewById(R.id.numberPickerFromSettings);

        assert npFrom != null;
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

        assert npTo != null;
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

    /**
     * @return next departure time
     */
    private Calendar nextDeparture() {
        int[] direction = {from, to};
        return baseClass.nextDeparture(direction);
    }

    /**
     * on buttonclick to set widget,
     *
     * @param view button setWidget
     */
    @SuppressWarnings({"unused", "UnusedParameters"})
    public void setWidget(View view) {

        Log.e("Widget", "widget set");

        //save direction chose by numberpickers
        saveDirection(context, from, to);

        //duplicate from code in WidgetProvider.onReceive

        Calendar cal = nextDeparture();
        cal.add(Calendar.MINUTE, 1);

        //set alarm to update every half an hour
        Intent intent = new Intent(context, Receiver.class);
        intent.setAction("alarm");
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 60000, pIntent); //1800000 for half an hour

        //standard code block to update widget
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
        // Done with Configure, finish this activity.
        ActivityCompat.finishAffinity(this);
    }

//    /**
//     * @param c calendar object
//     * @return c in a string
//     */
//    public String cToString(Calendar c) {
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
//        return simpleDateFormat.format(c.getTime());
//    }

    /**
     * Save direction into shared prefs
     *
     * @param context context for prefs
     * @param from    from city
     * @param to      to city
     */
    static void saveDirection(Context context, int from, int to) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(
                PREFS_NAME, 0).edit(); //0 instead of MODE_MULTI_PROCESS
        prefs.putInt("from", from);
        prefs.putInt("to", to);
        prefs.apply();
    }

    /**
     * Load direction from shared prefs
     *
     * @param context context for prefs
     * @return int[] with first is from, second is to
     */
    static int[] loadDirection(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        int[] fromto = new int[2];
        fromto[0] = prefs.getInt("from", -1);
        fromto[1] = prefs.getInt("to", -1);
        return fromto;
    }
}