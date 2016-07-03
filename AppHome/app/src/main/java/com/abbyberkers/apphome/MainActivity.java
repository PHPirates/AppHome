package com.abbyberkers.apphome;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private BaseClass baseClass;

    private int from; //default from Eindhoven
    private int to;
    private int depart; //departure numberpicker value

    private boolean ASyncTaskIsRunning; //boolean to check if there is an asynctask running

    private static final int EHV = 0;
    private static final int Heeze = 1;
    private static final int RDaal = 2;

    private ProgressBar progressBar;
    private String response; //set after getting xml from ns, used by
    /**
     * response is used by getNSDepartures and set by the AsyncTask
     */
    private String arrivalResponse; //trips from Breda to RDaal for arrival times on EHV-RDaal

    /**
     * OnCreate
     *
     * @param savedInstanceState bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        baseClass = new BaseClass();

        //progressbar when getting response from slow NS
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        NumberPicker npFrom;
        npFrom = (NumberPicker) findViewById(R.id.numberPickerFrom);

        assert npFrom != null;
        npFrom.setMinValue(0);
        npFrom.setMaxValue(BaseClass.cities.length - 1);
        npFrom.setDisplayedValues(BaseClass.cities);
        npFrom.setWrapSelectorWheel(false);
        npFrom.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        setDividerColor(npFrom, ContextCompat.getColor(this, R.color.divider));

        npFrom.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                from = newVal;
                new RetrieveFeedTask().execute(); //get xml from ns
            }
        });

        NumberPicker npTo;

        npTo = (NumberPicker) findViewById(R.id.numberPickerTo);

        assert npTo != null;
        npTo.setMinValue(0);
        npTo.setMaxValue(BaseClass.cities.length - 1);
        npTo.setDisplayedValues(BaseClass.cities);
        npTo.setWrapSelectorWheel(false);
        npTo.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        setDividerColor(npTo, ContextCompat.getColor(this, R.color.divider));


        npTo.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                to = newVal;
                new RetrieveFeedTask().execute(); //get xml from ns
            }
        });

        //get all the current departures to show in numberpicker
        String[] departTimes = currentDepartures();

        NumberPicker npDep; //NP for close departure times
        npDep = (NumberPicker) findViewById(R.id.numberPickerDepartures);

        assert npDep != null;
        npDep.setMinValue(0);
        npDep.setMaxValue(departTimes.length - 1);
        npDep.setWrapSelectorWheel(false);
        npDep.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        updateDepartures();

        npDep.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                depart = newVal;
            }
        });
    }

    /**
     * Called when opening app again, to refresh the times in the departure numberpicker (npDep).
     */

    @Override
    public void onResume(){
        super.onResume();
        new RetrieveFeedTask().execute(); //get xml from ns
    }

    /**
     * Set instance response from the ASyncTask
     *
     * @param response response passed
     */
    private void setResponse(String response) {
        this.response = response;
    }

    private void setArrivalResponse(String arrivalResponse) {
        this.arrivalResponse = arrivalResponse;
    }

    /**
     * Convert the instance variables to and from, set by the numberpickers,
     * to a string to be used in {@link AsyncTask()}
     *
     * @param toFrom the to or from variable
     * @return string of city
     */
    private String convertCityToString(int toFrom) {
        return baseClass.convertCityToString(toFrom);
    }

    /**
     * get arrival time of voyage, given departure time
     *
     * @param depTime departure time ns-format
     * @param field do you want delays or arrival times? Should be ns node string
     * @return arrival time
     */
    private String getNSStringByDepartureTime(String depTime, String field) {
        baseClass.response = this.response;
        String res = baseClass.getNSStringByDepartureTime(depTime, field, arrivalResponse, from, to);
        this.response = baseClass.response;
        return res;
    }

    /**
     * Communicates with ASyncTask using the instance variables to, from and response
     * also in WidgetProvider
     *
     * @return Calendar[] with five current departures, default is five null objects
     */
    private Calendar[] getNSDepartures() {
        return baseClass.getNSDepartures(this.response, this.from, this.to, this);
    }

    /**
     * convert ns-format to HH:mm
     *
     * @param nsTime ns time
     * @return string
     */
    private String convertNSToString(String nsTime) {
        return baseClass.convertNSToString(nsTime, from, to);
    }

// --Commented out by Inspection START (15-6-2016 16:41):
//    /**
//     * convert a time string in ns format to a date object
//     *
//     * @param nsTime time in ns format
//     * @return date object
//     */
//    public Date convertNSToDate(String nsTime) {
//        return baseClass.convertNSToDate(nsTime);
//    }
// --Commented out by Inspection STOP (15-6-2016 16:41)

    /**
     * Convert calendar object to string in ns-format
     *
     * @param c calendar
     * @return string
     */
    private String convertCalendarToNS(Calendar c) {
        if (c == null) {
            return null;
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
            return sdf.format(c.getTime());
        }
    }

// --Commented out by Inspection START (15-6-2016 16:41):
//    /**
//     * convert ns time string to calendar object, uses {@link #convertNSToDate(String)}
//     *
//     * @param nsTime time in ns format
//     * @return calendar object
//     */
//    public Calendar convertNSToCal(String nsTime) {
//        return baseClass.convertNSToCal(nsTime);
//    }
// --Commented out by Inspection STOP (15-6-2016 16:41)

    /**
     * convert calendar object to string object in HH:mm format
     *
     * @param c calendar object
     * @return string object
     */
    private String convertCalendarToString(Calendar c) {
        return baseClass.convertCalendarToString(c);
    }

    /**
     * Update the time picker when selecting a new destination
     */
    private void updateDepartures() {
        String[] departTimes = currentDepartures();
        NumberPicker npDep; //NP for close departure times
        npDep = (NumberPicker) findViewById(R.id.numberPickerDepartures);
        assert npDep != null;
        npDep.setDisplayedValues(departTimes);
        int middle = departTimes.length / 2;
        npDep.setValue(middle); //set default option
        depart = middle; //set chosen value to default
        if (to == from) {
            setDividerColor(npDep, 0); //set divider color to invisible
        } else { //set to default color
            setDividerColor(npDep, ContextCompat.getColor(this, R.color.divider));
        }
    }

    /**
     * Called from {@link AsyncTask} when there is no internet connection
     */
    private void noInternetConnection() {
        //remove select dividers
        NumberPicker npDep; //NP for close departure times
        npDep = (NumberPicker) findViewById(R.id.numberPickerDepartures);
        setDividerColor(npDep, 0);
        Toast.makeText(this, "No Internet connection available", Toast.LENGTH_LONG).show();
    }

    /**
     * set divider color of departure time picker to make it invisible when
     * to == from
     * @param picker the numberpicker
     * @param color colour to make it
     */
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
    private String[] currentDepartures() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        Calendar[] calendars = getNSDepartures();
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
     * whatsapp a delayed arrival time
     *
     * @param view button sendDelay
     */
    @SuppressWarnings("unused") // Actually injected by sendDelayButton
    public void sendDelay(View view) {
        //get the five current departure times in calendar format
        Calendar[] departures = getNSDepartures();
        if (departures[depart] == null) {
            Toast.makeText(this, "Delay not possible", Toast.LENGTH_LONG).show();
        } else {
            //convert calendar to ns format and select the chosen departure time
            String nsDep = convertCalendarToNS(departures[depart]);
            String delay = getNSStringByDepartureTime(nsDep, "AankomstVertraging");
            sendWhatsApp(delay);
        }
    }


    /**
     * Sends the text, called on buttonclick
     *
     * @param view button Send
     */

    @SuppressWarnings("unused")
    public void sendText(@SuppressWarnings("UnusedParameters") View view) {

        String message = "You are here already, you stupid!";

        //1. get five departure times in calendar format
        //2. convert the chosen calendar to ns-format string
        //3. get the corresponding arrival time
        //4. convert it to HH:mm format
        String nsArrivalTime = convertNSToString(getNSStringByDepartureTime(
                convertCalendarToNS(getNSDepartures()[depart]), "ActueleAankomstTijd"));

        if (from == EHV) {
            if (to == Heeze) {
                //take the chosen calendar object of the current departures,
                // and add optionally travel time to that and convert to string with cAddTravel
                message = "Trein van " + convertCalendarToString(getNSDepartures()[depart]);
            } else if (to == RDaal) {
                message = "ETA " + nsArrivalTime;
            }
        } else if (from == Heeze) {
            if (to == EHV) {
                message = "Eindhoven ETA " + nsArrivalTime;
            } else if (to == RDaal) {
                message = "Yay at " + nsArrivalTime + ".";
            }
        } else {
            message = "ETA " + nsArrivalTime;
        }

        sendWhatsApp(message);

    }

    /**
     * departure time should be the first departure time in a given hour
     *
     * @param depart Train departure time
     * @param offset Total travel time
     * @return departure time with offset
     */


    /**
     * send message to whatsapp
     *
     * @param text the message
     */
    private void sendWhatsApp(String text) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        sendIntent.setPackage("com.whatsapp");
        startActivity(sendIntent);
    }


    private class RetrieveFeedTask extends AsyncTask<Void, Void, String> {


        protected void onPreExecute() {
            //change flag value, from false to true if first asynctask, from true to false
            //if there is already one task running
            ASyncTaskIsRunning = !ASyncTaskIsRunning;
            progressBar.setVisibility(View.VISIBLE);
        }

        protected String doInBackground(Void... urls) {
            InputStream in;
            int resCode;

            try {
                //http://t.m.schouten@student.tue.nl:sO-65AZxuErJmmC28eIRB85aos7oGVJ0C6tOZI9YeHDPLXeEv1nfBg@webservices.ns.nl/ns-api-treinplanner?fromStation=Roosendaal&toStation=Eindhoven

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
//                                Log.e("rescode", "rescode not ok");
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
//                            Log.e("rescode", "rescode not ok");
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
            } catch (IOException e) {
//                Log.e("IOException", "no internet connection");
                return "no internet";
            } catch (Exception e) {
//                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            if (!ASyncTaskIsRunning) {
                //there has an other asynctask started while this one was running
                ASyncTaskIsRunning = true; //set to true, because the other one is still running
            } else {
                ASyncTaskIsRunning = false; //reset
                progressBar.setVisibility(View.GONE);
                if (response.equals("no internet")) {
                    noInternetConnection();
                } else {
                    setResponse(response);
                    updateDepartures(); //update nrpicker
                }
            }
        }
    }
}