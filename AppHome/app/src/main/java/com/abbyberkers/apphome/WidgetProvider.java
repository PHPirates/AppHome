package com.abbyberkers.apphome;

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

import java.util.Random;

public class WidgetProvider extends AppWidgetProvider {

    final String SYNC_CLICKED = "action";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        RemoteViews remoteViews;
        ComponentName watchWidget;

        remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        watchWidget = new ComponentName(context, WidgetProvider.class);

        remoteViews.setOnClickPendingIntent(R.id.sendTimeOne, getPendingSelfIntent(context, SYNC_CLICKED));



        remoteViews.setTextViewText(R.id.testText, "TESTINGupdate");

        appWidgetManager.updateAppWidget(watchWidget, remoteViews);


    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        super.onReceive(context, intent);

        if (intent.getAction().equals(SYNC_CLICKED)) {

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            RemoteViews remoteViews;
            ComponentName watchWidget;

            remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            watchWidget = new ComponentName(context, WidgetProvider.class);

            remoteViews.setTextViewText(R.id.sendTimeOne, "TESTING");
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "watest");
            sendIntent.setType("text/plain");
            sendIntent.setPackage("com.whatsapp");
            sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(sendIntent);

            appWidgetManager.updateAppWidget(watchWidget, remoteViews);

        }
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);

    }
}