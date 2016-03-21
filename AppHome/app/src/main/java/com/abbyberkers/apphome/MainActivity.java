package com.abbyberkers.apphome;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.ProgressBar;

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


public class MainActivity extends AppCompatActivity {

    int from; //default from Eindhoven
    int to;
    int depart; //departure numberpicker value

    ProgressBar progressBar;
    String response; //set after getting xml from ns, used by
    /**
     * response is used by {@link #getNSDepartures()} and set by the AsyncTask
     */

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

        //progressbar when getting response from slow NS
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        String[] cities = new String[]{"Eindhoven", "Heeze", "Roosendaal"};

        NumberPicker npFrom;
        npFrom = (NumberPicker) findViewById(R.id.numberPickerFrom);

        npFrom.setMinValue(0);
        npFrom.setMaxValue(cities.length - 1);
        npFrom.setDisplayedValues(cities);
        npFrom.setWrapSelectorWheel(true);
        npFrom.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        npFrom.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                from = newVal;
                new RetrieveFeedTask().execute(); //get xml from ns
            }
        });

        NumberPicker npTo;

        npTo = (NumberPicker) findViewById(R.id.numberPickerTo);

        npTo.setMinValue(0);
        npTo.setMaxValue(cities.length - 1);
        npTo.setDisplayedValues(cities);
        npTo.setWrapSelectorWheel(true);
        npTo.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

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
     * Set instance response from the ASyncTask
     *
     * @param response response passed
     */
    public void setResponse(String response) {
        this.response = response;
    }

    /**
     * Convert the instance variables to and from, set by the numberpickers,
     * to a string to be used in {@link AsyncTask()}
     *
     * @param toFrom the to or from variable
     * @return string of city
     */
    public String convertToFromToString(int toFrom) {
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
                result = "I don't know city";
        }
        return result;
    }

    /**
     * Communicates with ASyncTask using the instance variables to, from and response
     *
     * @return Calendar[] with five current departures
     */
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

//    /**
//     * convert calendar object to string object in HH:mm format
//     * @param c calendar object
//     * @return string object
//     */
//    public String convertCalendarToString(Calendar c) {
//        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
//        return sdf.format(c.getTime());
//    }

    /**
     * Update the time picker when selecting a new destination
     */
    public void updateDepartures() {
        String[] departTimes = currentDepartures();
        NumberPicker npDep; //NP for close departure times
        npDep = (NumberPicker) findViewById(R.id.numberPickerDepartures);
        npDep.setDisplayedValues(departTimes);
        npDep.setValue(2); //set default option
        depart = 2; //set chosen value to default
        if (to == from) {
            setDividerColor(npDep, 0);
        } else {
            setDividerColor(npDep, ContextCompat.getColor(this, R.color.divider));
        }
    }

    //*****************normal stuff****************

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
    public String[] currentDepartures() {
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
     * Sends the text, called on buttonclick
     *
     * @param view button Send
     */

    public void sendText(View view) {

        String message = "You are here already, you stupid!";

        int EHV = 0;
        int Heeze = 1;
        int RDaal = 2;

        if (from == EHV) {
            if (to == Heeze) {
                //take the chosen calendar object of the current departures,
                // and add optionally travel time to that and convert to string with cAddTravel
                message = "Trein van " + cAddTravel(getNSDepartures()[depart], 0);
            } else if (to == RDaal) {
                message = "ETA " + cAddTravel(getNSDepartures()[depart], 90);
            }
        } else if (from == Heeze) {
            if (to == EHV) {
                message = "Eindhoven ETA " + cAddTravel(getNSDepartures()[depart], 15);
            } else if (to == RDaal) {
                message = "Yay at " + cAddTravel(getNSDepartures()[depart], 114) + ".";
            }
        } else if (from == RDaal) {
            if (to == EHV) {
                message = "ETA " + cAddTravel(getNSDepartures()[depart], 70);  //20,70
            } else if (to == Heeze) {
                message = "ETA " + cAddTravel(getNSDepartures()[depart], 113);
            }
        }

        sendText(message);

    }

    /**
     * @param c      calendar object, probably the one chosen by the time nrpicker
     * @param travel optional travel time
     * @return added travel time to calendar object and converted to string
     */
    public String cAddTravel(Calendar c, int travel) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        c.add(Calendar.MINUTE, travel);
        if (from == 0 && to == 2) { //if going to Rdaal
            //round time to nearest ten minutes
            int unroundedMinutes = c.get(Calendar.MINUTE);
            int mod = unroundedMinutes % 10;
            c.add(Calendar.MINUTE, mod < 5 ? -mod : (10 - mod));
        }
        return simpleDateFormat.format(c.getTime());
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
    public void sendText(String text) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        sendIntent.setPackage("com.whatsapp");
        startActivity(sendIntent);
    }


    class RetrieveFeedTask extends AsyncTask<Void, Void, String> {


        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        protected String doInBackground(Void... urls) {
            InputStream in;
            int resCode;

            try {
                //http://t.m.schouten@student.tue.nl:sO-65AZxuErJmmC28eIRB85aos7oGVJ0C6tOZI9YeHDPLXeEv1nfBg@webservices.ns.nl/ns-api-treinplanner?fromStation=Roosendaal&toStation=Eindhoven

                //build right url using to and from
//                int from = getThisFrom();
//                int to = getThisTo();

                if (to == from) {
                    return "no url possible";
                } else {
                    String fromString = convertToFromToString(from);
                    String toString = convertToFromToString(to);
                    URL url = new URL("http://webservices.ns.nl/ns-api-treinplanner?fromStation="
                            + fromString + "&toStation=" + toString);
//                    URL url = new URL("http://webservices.ns.nl/ns-api-treinplanner?fromStation=Roosendaal&toStation=Eindhoven");

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
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            progressBar.setVisibility(View.GONE);
            setResponse(response);
            updateDepartures(); //update nrpicker
        }
    }
}