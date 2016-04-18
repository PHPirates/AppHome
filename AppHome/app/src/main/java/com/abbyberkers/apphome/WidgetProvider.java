package com.abbyberkers.apphome;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WidgetProvider extends AppWidgetProvider {
    /**
     * class that does everything in the widget
     */

    BaseClass baseClass;

    //action names of buttons
    final String TIME_ONE = "time_one";
    final String TIME_TWO = "time_two";
    final String TIME_THREE = "time_three";
    final String TURN = "turn";

    //strings for times on buttons
    String timeOne;
    String timeTwo;
    String timeThree;
    //the xml response strings
    String response;
    String arrivalResponse; //xml from trips from Breda to RDaal for arrival times on EHV-RDaal

    //message to be sent to whatsapp
    String message = "";

    //the context of the widget
    Context remoteContext;
    AppWidgetManager appWidgetManager;

    int from;
    int to;
    int[] direction;

    public static final int EHV = 0;
    public static final int Heeze = 1;
    public static final int RDaal = 2;

    /**
     * When alarm goes off, we update the widget
     *
     * @param context          widget context
     * @param appWidgetManager widget manager
     * @param appWidgetIds     in case there are more widgets
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        //set instance variables for the updateButton method to use
        this.remoteContext = context;
        this.appWidgetManager = appWidgetManager;

        //set instance variables to be used in all kinds of methods
        direction = WidgetSettings.loadDirection(remoteContext);
        this.from = direction[0];
        this.to = direction[1];

        //check if this is the first time that the widget is created
        if(from == -1 || to == -1) {
            //set "set widget"
            setLoading("set widget");
        } else {
            //set loading dots on widget in the meantime
            setLoading("loading...");
        }

        new RetrieveFeedTask().execute(); //execute the asynctask class to get stuff from ns
    }

    /**
     * called when a button on the widget is clicked
     *
     * @param context widget
     * @param intent i
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        baseClass = new BaseClass();

        if(intent.getAction().equals(TURN)) {
            //if turn button is clicked

            direction = WidgetSettings.loadDirection(context);
            WidgetSettings.saveDirection(context, direction[1], direction[0]); //swap from and to
            direction = WidgetSettings.loadDirection(context); //update direction

            //calling to BootReceiver to avoid one extra duplicate method...
            Calendar cal = baseClass.nextDeparture(direction);
            cal.add(Calendar.MINUTE, 1);

            //set alarm to update every half an hour
            Intent i = new Intent(context, Receiver.class);
            PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, i, 0);

            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 1800000, pIntent);

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
        } else

            //on time 1 button click
        if (intent.getAction().equals(TIME_ONE)) {

            Bundle extras = intent.getExtras();
            if (extras != null) { //timeOne becomes the text to be sent to whatsapp sent from setOnButtonClickListeners
                timeOne = extras.getString("text");
            } else {
                timeOne = "time one";
            }

            //send whatsapp
            appText(context, timeOne);


        } else if (intent.getAction().equals(TIME_TWO)) {

            Bundle extras = intent.getExtras();
            if (extras != null) {
                timeTwo = extras.getString("text");
            } else {
                timeTwo = "time two";
            }

            appText(context, timeTwo);


        } else if (intent.getAction().equals(TIME_THREE)) {

            Bundle extras = intent.getExtras();
            if (extras != null) {
                timeThree = extras.getString("text");
            } else {
                timeThree = "time three";
            }

            appText(context, timeThree);

        }
    }

    /**
     * set loading... on buttons
     * set set widget on buttons when the widget is first created
     */
    public void setLoading(String top) {
        String loading = "...";

        //get remoteViews and component name to update widget
        RemoteViews remoteViews;
        ComponentName watchWidget;

        remoteViews = new RemoteViews(remoteContext.getPackageName(), R.layout.widget_layout);
        watchWidget = new ComponentName(remoteContext, WidgetProvider.class);

        //set text
        remoteViews.setTextViewText(R.id.settingsButton, top);
        remoteViews.setTextViewText(R.id.sendTimeOne, loading);
        remoteViews.setTextViewText(R.id.sendTimeTwo, loading);
        remoteViews.setTextViewText(R.id.sendTimeThree, loading);

        //add click listeners again
        setButtonOnClickListeners(remoteViews, watchWidget);
    }

    /**
     * called by the ASyncTask after it has finished setting the response
     * updates text on the buttons to right times, and adds rights text in the whatsapp message
     */
    public void updateButtons() {

        RemoteViews remoteViews;
        ComponentName watchWidget;

        remoteViews = new RemoteViews(remoteContext.getPackageName(), R.layout.widget_layout);
        watchWidget = new ComponentName(remoteContext, WidgetProvider.class);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        //get direction again just to check if it exists
        int[] direction = WidgetSettings.loadDirection(remoteContext);

        if (direction != null) {
            //set direction on settings button
            remoteViews.setTextViewText(R.id.settingsButton, getDirection());

            //special case for Abby :)
            if (from == EHV && to == Heeze) {
                message = "Trein van ";
            } else {
                message = "ETA ";
            }

            //get five current departure calendars,
            // because when this method is called response is set
            Calendar[] currentDeps = getNSDepartures();
            remoteViews.setTextViewText(R.id.sendTimeOne, cToString(currentDeps[1]));
            remoteViews.setTextViewText(R.id.sendTimeTwo, cToString(currentDeps[2]));
            remoteViews.setTextViewText(R.id.sendTimeThree, cToString(currentDeps[3]));

            //set time to send to whatsapp
            if (from == EHV && to == Heeze) {
                //if going to heeze, just set the departure time for times to send to whatsapp
                timeOne = convertCalendarToString(currentDeps[1]);
                timeTwo = convertCalendarToString(currentDeps[2]);
                timeThree = convertCalendarToString(currentDeps[3]);
            } else {
                //otherwise, get arrival time
                String[] times = new String[3];
                for (int i = 0; i < times.length; i++) {
                    times[i] = convertNSToString(getNSStringByDepartureTime(
                            convertCalendarToNS(currentDeps[i + 1]), "ActueleAankomstTijd"));
                }
                timeOne = times[0];
                timeTwo = times[1];
                timeThree = times[2];
            }
        }

        setButtonOnClickListeners(remoteViews, watchWidget);
    }

    /**
     * get arrival time of voyage, given departure time
     *
     * @param depTime departure time ns-format
     * @param field   do you want delays or arrival times? Should be ns node string
     * @return arrival time
     */
    public String getNSStringByDepartureTime(String depTime, String field) {
        baseClass.response = this.response;
        String res = baseClass.getNSStringByDepartureTime(depTime, field, arrivalResponse, from, to);
        this.response = baseClass.response;
        return res;
    }

    /**
     * convert ns-format to HH:mm
     *
     * @param nsTime ns time
     * @return string
     */
    public String convertNSToString(String nsTime) {
        return baseClass.convertNSToString(nsTime, from, to);
    }

    /**
     * Convert calendar object to string in ns-format
     *
     * @param c calendar
     * @return string
     */
    public String convertCalendarToNS(Calendar c) {
        if (c == null) {
            return null;
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
            return sdf.format(c.getTime());
        }
    }

    /**
     * convert calendar object to string object in HH:mm format
     *
     * @param c calendar object
     * @return string object
     */
    public String convertCalendarToString(Calendar c) {
        return baseClass.convertCalendarToString(c);
    }

    /**
     * uses instance variables from and to, to:
     * @return string "fromcity - tocity", "Set widget" the first time
     */
    public String getDirection() {
        if (convertCityToString(this.from) == null) { //default, the first time
            return "Set widget";
        }
        return convertCityToString(this.from) + " - " + convertCityToString(this.to);
    }

    /**
     * Convert the instance variables to and from, set by the numberpickers,
     * to a string to be used in {@link AsyncTask()}
     *
     * @param toFrom the to or from variable
     * @return string of city
     */
    public String convertCityToString(int toFrom) {
        return baseClass.convertCityToString(toFrom);
    }

    /**
     * sets the button onClick listeners for the widget
     * 1. start the settings activiy
     * 2. send first time to whatsapp with the time as text
     * 3. send second time to whatsapp "
     * 4. send third time to whatsapp "
     *
     * @param remoteViews to update things on widget
     * @param watchWidget app component
     */
    public void setButtonOnClickListeners(RemoteViews remoteViews, ComponentName watchWidget) {

        Intent intent = new Intent(remoteContext, WidgetSettings.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        PendingIntent pendingIntent = PendingIntent.getActivity(remoteContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.settingsButton, pendingIntent);

        Intent intentTurn = new Intent(remoteContext, getClass());
        intentTurn.setAction(TURN);
        PendingIntent buttonTurn = PendingIntent.getBroadcast(remoteContext, 0, intentTurn, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.turnButton, buttonTurn);

        Intent intentOne = new Intent(remoteContext, getClass());
        intentOne.setAction(TIME_ONE);
        intentOne.putExtra("text", message + timeOne);
        PendingIntent buttonOne = PendingIntent.getBroadcast(remoteContext, 0, intentOne, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.sendTimeOne, buttonOne);

        Intent intentTwo = new Intent(remoteContext, getClass());
        intentTwo.setAction(TIME_TWO);
        intentTwo.putExtra("text", message + timeTwo);
        PendingIntent buttonTwo = PendingIntent.getBroadcast(remoteContext, 0, intentTwo, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.sendTimeTwo, buttonTwo);

        Intent intentThree = new Intent(remoteContext, getClass());
        intentThree.setAction(TIME_THREE);
        intentThree.putExtra("text", message + timeThree);
        PendingIntent buttonThree = PendingIntent.getBroadcast(remoteContext, 0, intentThree, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.sendTimeThree, buttonThree);

        appWidgetManager.updateAppWidget(watchWidget, remoteViews);
    }

    /**
     * send whatsapp with text
     * @param context context
     * @param text message to send
     */
    public void appText(Context context, String text) {

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        sendIntent.setPackage("com.whatsapp");
        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP); //no idea but it works
        context.startActivity(sendIntent);
    }

    /**
     * convert calendar to HH:mm string
     * @param c calendar
     * @return string
     */
    public String cToString(Calendar c) {
        if (c == null) {
            return "...";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        return simpleDateFormat.format(c.getTime());
    }

    /**
     * Communicates with ASyncTask using the instance variables to, from and response
     * also in WidgetProvider
     *
     * @return Calendar[] with five current departures, default is five null objects
     */
    public Calendar[] getNSDepartures() {
        return baseClass.getNSDepartures(this.response, this.from, this.to, remoteContext);
    }

    /**
     * convert a time string in ns format to a date object
     *
     * @param nsTime time in ns format
     * @return date object
     */
    public Date convertNSToDate(String nsTime) {
        return baseClass.convertNSToDate(nsTime);
    }

    /**
     * convert ns time string to calendar object, uses {@link #convertNSToDate(String)}
     *
     * @param nsTime time in ns format
     * @return calendar object
     */
    public Calendar convertNSToCal(String nsTime) {
        return baseClass.convertNSToCal(nsTime);
    }


    /**
     * changes compared to main:
     * no progressbar
     * first time
     * updatebuttons instead of updatedepartures
     */
    class RetrieveFeedTask extends AsyncTask<Void, Void, String> {


        protected void onPreExecute() {
        }

        protected String doInBackground(Void... urls) {
            InputStream in;
            int resCode;

            try {

                if (from == -1 || to == -1) { //the first time
                    return null;
                } else {

                    if (to == from) {
                        return "no url possible";
                    } else {
                        String fromString = convertCityToString(from);
                        String toString = convertCityToString(to);

                        URL url;

                        if (from == EHV && to == RDaal) { //go to Breda to get also the intercity trips
                            url = new URL("http://webservices.ns.nl/ns-api-treinplanner?fromStation="
                                    + fromString + "&toStation=Breda");

                            //*************** also get trips from Breda to RDaal for arrival times *****
                            URL arrivalURL = new URL("http://webservices.ns.nl/ns-api-treinplanner" +
                                    "?fromStation=Breda&toStation=" + toString);
                            String encoding = "dC5tLnNjaG91dGVuQHN0dWRlbnQudHVlLm5sOnNPLTY1QVp4dUVySm1tQzI4ZUlSQjg1YW9zN29HVkowQzZ0T1pJOVllSERQTFhlRXYxbmZCZw==";
                            HttpURLConnection urlConnection = (HttpURLConnection) arrivalURL.openConnection();
                            urlConnection.setRequestProperty("Authorization", "Basic " + encoding);

                            try {
                                resCode = urlConnection.getResponseCode();
                                if (resCode == HttpURLConnection.HTTP_OK) {
                                    in = urlConnection.getInputStream();
                                } else {
                                    Log.e("rescode", "rescode not ok");
                                    in = urlConnection.getErrorStream();
                                }
                                BufferedReader bufferedReader = new BufferedReader(
                                        new InputStreamReader(in));
                                StringBuilder stringBuilder = new StringBuilder();
                                String line;
                                while ((line = bufferedReader.readLine()) != null) {
                                    stringBuilder.append(line).append("\n");
                                }
                                bufferedReader.close();
                                setArrivalResponse(stringBuilder.toString());
                            } finally {
                                urlConnection.disconnect();
                            }

                            //****************************************************************
                        } else {
                            url = new URL("http://webservices.ns.nl/ns-api-treinplanner?fromStation="
                                    + fromString + "&toStation=" + toString);
                        }

//                String userCredentials = "t.m.schouten@student.tue.nl:sO-65AZxuErJmmC28eIRB85aos7oGVJ0C6tOZI9YeHDPLXeEv1nfBg";
//                String encoding = new String(android.util.Base64.encode(userCredentials.getBytes(), Base64.DEFAULT));
//                encoding = encoding.replaceAll("\\s+",""); //because the base64 encoding doesn't work.

                        //encoded userCredentials with online encoder
                        String encoding = "dC5tLnNjaG91dGVuQHN0dWRlbnQudHVlLm5sOnNPLTY1QVp4dUVySm1tQzI4ZUlSQjg1YW9zN29HVkowQzZ0T1pJOVllSERQTFhlRXYxbmZCZw==";
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestProperty("Authorization", "Basic " + encoding);

                        try {
                            resCode = urlConnection.getResponseCode();
                            if (resCode == HttpURLConnection.HTTP_OK) {
                                in = urlConnection.getInputStream();
                            } else {
                                Log.e("rescode", "rescode not ok");
                                in = urlConnection.getErrorStream();
                            }
                            BufferedReader bufferedReader = new BufferedReader(
                                    new InputStreamReader(in));
                            StringBuilder stringBuilder = new StringBuilder();
                            String line;
                            while ((line = bufferedReader.readLine()) != null) {
                                stringBuilder.append(line).append("\n");
                            }
                            bufferedReader.close();
                            return stringBuilder.toString();
                        } finally {
                            urlConnection.disconnect();
                        }
                    }
                }
            } catch (IOException e) {
                Log.e("IOException", "no internet connection");
                return "no internet";
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
                }
            }

        protected void onPostExecute(String response) {
            if (response != null) {
                if (response.equals("no internet")) {
                    noInternetConnection();
                } else {
                    setResponse(response);
                    updateButtons(); //update buttons
                }
            }
        }

    }

    /**
     * Set instance response from the ASyncTask
     *
     * @param response response passed
     */
    public void setResponse(String response) {
        this.response = response;
    }

    public void setArrivalResponse(String arrivalResponse) {
        this.arrivalResponse = arrivalResponse;
    }

    /**
     * Called from {@link AsyncTask} when there is no internet connection
     */
    public void noInternetConnection() {
        //can't toast, do nothing
        Toast.makeText(remoteContext, "toast", Toast.LENGTH_SHORT).show();
    }
}