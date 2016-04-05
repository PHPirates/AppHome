package com.abbyberkers.apphome;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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

    String timeOne;
    String timeTwo;
    String timeThree;
    String response;

    String message = "";
    int travel;

    Context remoteContext;
    AppWidgetManager appWidgetManager;

    int from;
    int to;

    public static final int EHV = 0;
    public static final int Heeze = 1;
    public static final int RDaal = 2;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        //set instance variables for the updateButton method to use
        this.remoteContext = context;
        this.appWidgetManager = appWidgetManager;

        //set instance variables to be used in all kinds of methods
        int[] direction = WidgetSettings.loadDirection(remoteContext);
        this.from = direction[0];
        this.to = direction[1];

        setLoading();

        new RetrieveFeedTask().execute(); //execute the asynctask class
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

        setButtonOnClickListeners(remoteViews, watchWidget);
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

        int[] direction = WidgetSettings.loadDirection(remoteContext);

        if (direction != null) {
            remoteViews.setTextViewText(R.id.settingsButton, getDirection());

            if (from == EHV) {
                if (to == Heeze) {
                    message = "Trein van ";
                    travel = 0;
                } else if (to == RDaal) {
                    message = "ETA ";
                    travel = 89;
                }
            } else if (from == Heeze) {
                if (to == EHV) {
                    message = "ETA ";
                    travel = 15;
                } else if (to == RDaal) {
                    message = "ETA ";
                    travel = 83;
                }
            } else if (from == RDaal) {
                if (to == EHV) {
                    message = "ETA ";
                    travel = 70;
                } else if (to == Heeze) {
                    message = "ETA ";
                    travel = 85;
                }
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

        setButtonOnClickListeners(remoteViews, watchWidget);
    }

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
        String result;
        switch (toFrom) {
            case 0:
                result = "Eindhoven";
                break;
            case 1:
                result = "Heeze";
                break;
            case 2:
                result = "Roosendaal";
                break;
            default:
                result = null;
        }
        return result;
    }

    /**
     * sets the button onClick listeners for the widget
     * 1. start the settings activiy
     * 2. send first time to whatsapp with the time as text
     * 3. send second time to whatsapp "
     * 4. send third time to whatsapp "
     *
     * @param remoteViews
     * @param watchWidget
     */
    public void setButtonOnClickListeners(RemoteViews remoteViews, ComponentName watchWidget) {

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
        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP); //no idea but it works
        context.startActivity(sendIntent);
    }

    public String cToString(Calendar c) {
        if (c == null) {
            return "...";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        return simpleDateFormat.format(c.getTime());
    }

    /**
     * @param c      calendar object, probably the one chosen by the time nrpicker
     * @param travel optional travel time
     * @return added travel time to calendar object and converted to string
     */
    public String cTravelString(Calendar c, int travel) {
        if (c == null) {
            return "I'm lost.";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        c.add(Calendar.MINUTE, travel);
        if ((from == EHV) && (to == RDaal)) { //if going from ehv to Rdaal
            //round time to nearest ten minutes
            int unroundedMinutes = c.get(Calendar.MINUTE);
            int mod = unroundedMinutes % 10;
            c.add(Calendar.MINUTE, mod < 5 ? -mod : (10 - mod));
        }
        return simpleDateFormat.format(c.getTime());
    }

    /**
     * Communicates with ASyncTask using the instance variables to, from and response
     * also in MainActivity
     *
     * @return Calendar[] with five current departures
     */
    public Calendar[] getNSDepartures() {
        if (response == null) {
            response = "No response from NS or first time";
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

            NodeList nodeList;

            //if from EHV to RDaal, to == Breda, select intercities only
            if (from == EHV && to == RDaal) {
                //select all departure times where type is Intercity
                String depTimesICExpr = "/ReisMogelijkheden/ReisMogelijkheid[AantalOverstappen<1]/ActueleVertrekTijd";
                // "//*[not(text()='NIET-MOGELIJK')]"
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
//
//            for (int i = 0; i < nsTimes.size(); i++) {
//                Log.e("forl", convertNSToDate(nsTimes.get(i)).toString());
//            }


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

                if (from == -1 || to == -1) { //the first time
                    return null;
                } else {

                    if (from == to) {
                        Log.e("asynctask", "from equals to");
                        return "no url possible";
                    } else {
                        String fromString;
                        String toString;
                        //if from EHV to Roosendaal, we need to take the intercity
                        if (from == EHV && to == RDaal) {
                            fromString = convertCityToString(from);
                            toString = "Breda";
                        } else {
                            fromString = convertCityToString(from);
                            toString = convertCityToString(to);
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