package com.abbyberkers.apphome;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Receiver extends BroadcastReceiver {
    /**
     * Class to update widget when alarm is received
     *
     * @param context widget
     * @param intent  i
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e("Receiver", "received");
        //same standard block for updating widget
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

    }
}
