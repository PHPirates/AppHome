package com.abbyberkers.apphome;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class WidgetProvider extends AppWidgetProvider {

    final String TIME_ONE = "time_one";
    final String TIME_TWO = "time_two";
    final String TIME_THREE = "time_three";
    private static final String TAG = "LOG_TAG";

    String timeOne;
    String timeTwo;
    String timeThree;
    String response;

    String message;
    int travel;

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

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        int minutes = c.get(Calendar.MINUTE);

        String direction = WidgetSettings.loadDirection(context);

        if (direction != null) {
            remoteViews.setTextViewText(R.id.settingsButton, direction);
            Log.e("direction", direction);
            int depart = 0;

            if (direction.equals("Eindhoven - Heeze")) {
                depart = 4;
//                Log.e("depart", cToString(getNSDepartures()[2]));
                message = "Trein van ";
                travel = 0;
            } else if (direction.equals("Eindhoven - Roosendaal")) {
                depart = 1;
                message = "ETA ";
                travel = 89;
            } else if (direction.equals("Heeze - Eindhoven")) {
                depart = 15;
                message = "ETA ";
                travel = 15;
            } else if(direction.equals("Heeze - Roosendaal")){
                depart = 15;
                message = "ETA ";
                travel = 113;
            } else if (direction.equals("Roosendaal - Eindhoven")) {
                depart = 20;
                message = "ETA ";
                travel = 70;
            } else if (direction.equals("Roosendaal - Heeze")) {
                depart = 20;
                message = "ETA ";
                travel = 85;
            }

            if (minutes < depart) {
                c.set(Calendar.MINUTE, depart);
            } else if (depart < minutes && minutes < depart + 30) {
                c.set(Calendar.MINUTE, depart + 30);
            } else {
                c.set(Calendar.MINUTE, depart);
                c.add(Calendar.MINUTE, 60);
            }

            remoteViews.setTextViewText(R.id.sendTimeTwo, cToString(c));
            cal.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
            cal.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
            timeTwo = cTravelString(cal, travel);
            c.add(Calendar.MINUTE, -30);
            cal.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
            cal.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
//            Log.e("time two", timeTwo);
            remoteViews.setTextViewText(R.id.sendTimeOne, cToString(c));
            timeOne = cTravelString(cal, travel);
//            Log.e("time one", timeOne);
            c.add(Calendar.MINUTE, 60);
            cal.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
            cal.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
            remoteViews.setTextViewText(R.id.sendTimeThree, cToString(c));
            timeThree = cTravelString(cal, travel);

        }

        Intent intent = new Intent(context, WidgetSettings.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.settingsButton, pendingIntent);

        Intent intentOne = new Intent(context, getClass());
        intentOne.setAction(TIME_ONE);
        intentOne.putExtra("text", message + timeOne);
        PendingIntent buttonOne = PendingIntent.getBroadcast(context, 0, intentOne, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.sendTimeOne, buttonOne);

        Intent intentTwo = new Intent(context, getClass());
        intentTwo.setAction(TIME_TWO);
        intentTwo.putExtra("text", message + timeTwo);
        PendingIntent buttonTwo = PendingIntent.getBroadcast(context, 0, intentTwo, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.sendTimeTwo, buttonTwo);

        Intent intentThree = new Intent(context, getClass());
        intentThree.setAction(TIME_THREE);
        intentThree.putExtra("text", message + timeThree);
        PendingIntent buttonThree = PendingIntent.getBroadcast(context, 0, intentThree, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.sendTimeThree, buttonThree);

        appWidgetManager.updateAppWidget(watchWidget, remoteViews);


    }

    @Override
    public void onReceive(Context context, Intent intent) {
//        // TODO Auto-generated method stub
        super.onReceive(context, intent);

        Log.d(TAG, "onReceive");

        if (intent.getAction().equals(TIME_ONE)) {

            Log.e("widget", "time one button clicked");
            Bundle extras = intent.getExtras();
            if (extras != null) {
                timeOne = extras.getString("text");
            } else {
                timeOne = "time one";
            }

            appText(context, timeOne);


        } else if (intent.getAction().equals(TIME_TWO)) {

            Log.e("widget", "time two button clicked");
            Bundle extras = intent.getExtras();
            if (extras != null) {
                timeTwo = extras.getString("text");
                Log.e("text", timeTwo);
            } else {
                timeTwo = "time two";
            }

            appText(context, timeTwo);


        } else if (intent.getAction().equals(TIME_THREE)) {

            Log.e("widget", "time three button clicked");
            Bundle extras = intent.getExtras();
            if (extras != null) {
                timeThree = extras.getString("text");
            } else {
                timeThree = "time three";
            }

            appText(context, timeThree);

        }
    }

    public void appText(Context context, String text) {

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        sendIntent.setPackage("com.whatsapp");
        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(sendIntent);
    }

    public String cToString(Calendar c) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        return simpleDateFormat.format(c.getTime());
    }

    public String cTravelString(Calendar c, int t) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        c.add(Calendar.MINUTE, t);
        return simpleDateFormat.format(c.getTime());
    }

    public Calendar[] getNSDepartures() {
        if (response == null) {
            Log.e("getNSdeps", "no response from ns");
            response = "No response from NS";
        }
        try {

            //create java DOM xml parser
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            builder = builderFactory.newDocumentBuilder();

            //parse xml with the DOM parser
            Document xmlDocument = builder.parse(new ByteArrayInputStream(response.getBytes()));

            //create XPath object
            XPath xPath = XPathFactory.newInstance().newXPath();

            //generate list of departure times corresponding to nrpickers
            String depTimesExpr = "//ActueleVertrekTijd";
            NodeList nodeList = (NodeList) xPath.compile(depTimesExpr).evaluate(xmlDocument, XPathConstants.NODESET);
            List<String> nsTimes = new ArrayList<>();

            //use dep times from xml
            for (int i = 0; i < nodeList.getLength(); i++) {
                nsTimes.add(i, nodeList.item(i).getFirstChild().getNodeValue());
            }

            //get current time
            Date current = new Date();

            //find next departure time in List
            int nextIndex = -1;
            //convert to date to compare
            for (int i = 0; i < nsTimes.size(); i++) {
                Date nsDate = convertNSToDate(nsTimes.get(i));
                if (current.before(nsDate)) {
                    nextIndex = i; //index of next departure time.
                    break;
                }
            }

            Calendar[] depTimes = new Calendar[5];

            if (nextIndex == -1) {
                Log.e("nextIndex", "no next departure time!");
            } else {
                //index is index of next dept time of all the xml deptimes in nsTimes
                //get departure times around next time
                for (int i = 0; i < depTimes.length; i++) {
                    depTimes[i] = convertNSToCal(nsTimes.get(nextIndex - 2 + i));
                }
            }

//            //convert to HH:mm
//            for (int i = 0; i < nsTimes.size(); i++) {
//                String depTime = nsTimes.get(i);
//                depTime = convertNSToString(depTime);
//                nsTimes.set(i,depTime);
//            }

            //test printing
//            String res = "";
//            for (String depTime : depTimes) {
//                res += "\n" + convertCalendarToString(depTime);
//            }
//            responseView.setText(res);

            return depTimes;
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            e.printStackTrace();
        }
        return new Calendar[5]; //return default, null objects
    }

    /**
     * convert a time string in ns format to a date object
     *
     * @param nsTime time in ns format
     * @return date object
     */
    public Date convertNSToDate(String nsTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            return sdf.parse(nsTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date(); //default
    }

    /**
     * convert ns time string to calendar object, uses {@link #convertNSToDate(String)}
     *
     * @param nsTime time in ns format
     * @return calendar object
     */
    public Calendar convertNSToCal(String nsTime) {
        Date date = convertNSToDate(nsTime);
        Calendar c = new GregorianCalendar();
        c.setTime(date);
        return c;
    }
}