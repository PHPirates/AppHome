package com.abbyberkers.apphome;

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

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

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

    Context remoteContext;
    AppWidgetManager appWidgetManager;

//    @Override
//    public void onEnabled(Context context){
//        super.onEnabled(context);
//        Log.d(TAG, "onEnabled");
//    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        //set instance variables for the updateButton method to use
        this.remoteContext = context;
        this.appWidgetManager = appWidgetManager;

        setLoading();

        new RetrieveFeedTask().execute(); //get xml from ns
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction().equals(TIME_ONE)) {

            Bundle extras = intent.getExtras();
            if (extras != null) {
                timeOne = extras.getString("text");
            } else {
                timeOne = "time one";
            }

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

    public void setLoading() {
        //set loading on buttons
        String loading = "...";

        RemoteViews remoteViews;
        ComponentName watchWidget;

        remoteViews = new RemoteViews(remoteContext.getPackageName(), R.layout.widget_layout);
        watchWidget = new ComponentName(remoteContext, WidgetProvider.class);

        remoteViews.setTextViewText(R.id.settingsButton, "loading...");
        remoteViews.setTextViewText(R.id.sendTimeOne, loading);
        remoteViews.setTextViewText(R.id.sendTimeTwo, loading);
        remoteViews.setTextViewText(R.id.sendTimeThree, loading);

        doStuff(remoteViews, watchWidget);
    }

    /**
     * called by the ASyncTask after it has finished setting the response
     */
    public void updateButtons() {

        RemoteViews remoteViews;
        ComponentName watchWidget;

        remoteViews = new RemoteViews(remoteContext.getPackageName(), R.layout.widget_layout);
        watchWidget = new ComponentName(remoteContext, WidgetProvider.class);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        int minutes = c.get(Calendar.MINUTE);

        String direction = WidgetSettings.loadDirection(remoteContext);

        if (direction != null) {
            remoteViews.setTextViewText(R.id.settingsButton, direction);
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
            } else if (direction.equals("Heeze - Roosendaal")) {
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


            //get five current departure calendars,
            // because when this method is called response is set
            Calendar[] currentDeps = getNSDepartures();
            remoteViews.setTextViewText(R.id.sendTimeOne, cToString(currentDeps[1]));
            remoteViews.setTextViewText(R.id.sendTimeTwo, cToString(currentDeps[2]));
            remoteViews.setTextViewText(R.id.sendTimeThree, cToString(currentDeps[3]));

            //set time to send to whatsapp
            timeOne = cTravelString(currentDeps[1], travel);
            timeTwo = cTravelString(currentDeps[2], travel);
            timeThree = cTravelString(currentDeps[3], travel);

        }

        doStuff(remoteViews, watchWidget);
    }

    public void doStuff(RemoteViews remoteViews, ComponentName watchWidget) {
        //set all kinds of intents and clicklisteners and stuff???
        Intent intent = new Intent(remoteContext, WidgetSettings.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        PendingIntent pendingIntent = PendingIntent.getActivity(remoteContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.settingsButton, pendingIntent);

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

    /**
     * @param c      calendar object, probably the one chosen by the time nrpicker
     * @param travel optional travel time
     * @return added travel time to calendar object and converted to string
     */
    public String cTravelString(Calendar c, int travel) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        c.add(Calendar.MINUTE, travel);
        String direction = WidgetSettings.loadDirection(remoteContext);
        if (direction.equals("Eindhoven - Roosendaal")) { //if going to Rdaal
            //round time to nearest ten minutes
            int unroundedMinutes = c.get(Calendar.MINUTE);
            int mod = unroundedMinutes % 10;
            c.add(Calendar.MINUTE, mod < 5 ? -mod : (10 - mod));
        }
        return simpleDateFormat.format(c.getTime());
    }

    public Calendar[] getNSDepartures() {
        if (response == null) {
            Log.e("getNSdeps", "no response from ns or first time");
            response = "No response from NS";
        }
        try {
            String direction = WidgetSettings.loadDirection(remoteContext);

            //create java DOM xml parser
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            builder = builderFactory.newDocumentBuilder();

            //parse xml with the DOM parser
            Document xmlDocument = builder.parse(new ByteArrayInputStream(response.getBytes()));

            //create XPath object
            XPath xPath = XPathFactory.newInstance().newXPath();

            NodeList nodeList;

            //if on traject Rdaal/EHV, select intercities only
            if ((direction.equals("Roosendaal - Eindhoven")) || (direction.equals("Eindhoven - Roosendaal"))) {
                //select all departure times where type is Intercity
                String depTimesICExpr = "/ReisMogelijkheden/ReisMogelijkheid[AantalOverstappen<1]/ActueleVertrekTijd";
                nodeList = (NodeList) xPath.compile(depTimesICExpr).evaluate(
                        xmlDocument, XPathConstants.NODESET);
            } else {
                //generate list of departure times corresponding to nrpickers
                String depTimesExpr = "//ActueleVertrekTijd";
                nodeList = (NodeList) xPath.compile(depTimesExpr).evaluate(
                        xmlDocument, XPathConstants.NODESET);
            }
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

            //nstimes contains all ns departure times in ns-text format

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


    class RetrieveFeedTask extends AsyncTask<Void, Void, String> {


        protected void onPreExecute() {
        }

        protected String doInBackground(Void... urls) {
            InputStream in;
            int resCode;

            try {

                //get directions from and to
                String direction = WidgetSettings.loadDirection(remoteContext);
                if (direction.equals("Settings")) { //the first time
                    return null;
                } else {
                    String[] directions = direction.split(" - ");
                    if (directions.length != 2)
                        throw new IllegalArgumentException("String not in correct format");

                    String fromString = directions[0];
                    String toString = directions[1];

                    if (fromString.equals(toString)) {
                        Log.e("asynctask", "from equals to");
                        return "no url possible";
                    } else {
                        //if from EHV to Roosendaal, we need to take the intercity to Breda
                        if (direction.equals("Eindhoven - Roosendaal")) {
                            fromString = directions[0];
                            toString = "Breda";
                        } else if (direction.equals("Roosendaal - Eindhoven")) {
                            fromString = "Breda";
                            toString = directions[1];
                        }

                        URL url = new URL("http://webservices.ns.nl/ns-api-treinplanner?fromStation="
                                + fromString + "&toStation=" + toString);

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
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            setResponse(response);
            updateButtons(); //update buttons
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
}