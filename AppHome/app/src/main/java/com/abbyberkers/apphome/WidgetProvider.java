package com.abbyberkers.apphome;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

public class WidgetProvider extends AppWidgetProvider {

    final String TIME_ONE = "time_one";
    private static final String TAG = "LOG_TAG";

    String timeOne;
    String timeTwo;
    String timeThree;

//    @Override
//    public void onEnabled(Context context){
//        super.onEnabled(context);
//        Log.d(TAG, "onEnabled");
//    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        RemoteViews remoteViews;
        ComponentName watchWidget;

        remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        watchWidget = new ComponentName(context, WidgetProvider.class);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        int minutes = c.get(Calendar.MINUTE);

        String direction = WidgetSettings.loadDirection(context);

        if(direction != null){
            remoteViews.setTextViewText(R.id.settingsButton, direction);
            Log.e("direction", direction);

            if(direction.equals("Eindhoven - Heeze")){
                if(minutes < 4){
                    c.set(Calendar.MINUTE, 4);
                } else if(4 < minutes && minutes < 34){
                    c.set(Calendar.MINUTE, 34);
                } else {
                    c.set(Calendar.MINUTE, 4);
                    c.add(Calendar.MINUTE, 60);
                }
                remoteViews.setTextViewText(R.id.sendTimeTwo, cToString(c));
                c.add(Calendar.MINUTE, -30);
                remoteViews.setTextViewText(R.id.sendTimeOne, cToString(c));
                c.add(Calendar.MINUTE, 60);
                remoteViews.setTextViewText(R.id.sendTimeThree, cToString(c));
            }

//            timeOne = WidgetSettings.loadTimeOne(context);
//            timeTwo = WidgetSettings.loadTimeTwo(context);
//            timeThree = WidgetSettings.loadTimeThree(context);
//        } else {
//            timeOne = "Time One";
//            timeTwo = "Time Two";
//            timeThree = "Time Three";
//        }
//
//        if(timeOne != null){
//            remoteViews.setTextViewText(R.id.sendTimeOne, timeOne);
//        }
//
//        if(timeTwo != null){
//            remoteViews.setTextViewText(R.id.sendTimeTwo, timeTwo);
//        }
//
//        if(timeThree != null){
//            remoteViews.setTextViewText(R.id.sendTimeThree, timeThree);
        }

        Intent intent = new Intent(context, WidgetSettings.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.settingsButton, pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.sendTimeOne, getPendingSelfIntent(context,TIME_ONE));

//        remoteViews.setTextViewText(R.id.testText, "TESTINGupdate");

        appWidgetManager.updateAppWidget(watchWidget, remoteViews);


    }

    @Override
    public void onReceive(Context context, Intent intent) {
//        // TODO Auto-generated method stub
        super.onReceive(context, intent);

        Log.d(TAG, "onReceive");

        if (intent.getAction().equals(TIME_ONE)) {

            Log.e("widget", "time one button clicked");

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            RemoteViews remoteViews;
            ComponentName watchWidget;

            remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            watchWidget = new ComponentName(context, WidgetProvider.class);

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, timeOne);
            sendIntent.setType("text/plain");
            sendIntent.setPackage("com.whatsapp");
            sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(sendIntent);

            appWidgetManager.updateAppWidget(watchWidget, remoteViews);

        }
    }

    public String cToString(Calendar c) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        return simpleDateFormat.format(c.getTime());
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);

    }
}