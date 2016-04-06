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
import android.widget.Toast;

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

    public static final int EHV = 0;
    public static final int Heeze = 1;
    public static final int RDaal = 2;

    ProgressBar progressBar;
    String response; //set after getting xml from ns, used by
    /**
     * response is used by getNSDepartures and set by the AsyncTask
     */
    String arrivalResponse; //trips from Breda to RDaal for arrival times on EHV-RDaal

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

        npTo.setMinValue(0);
        npTo.setMaxValue(cities.length - 1);
        npTo.setDisplayedValues(cities);
        npTo.setWrapSelectorWheel(true);
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

    public void setArrivalResponse(String arrivalResponse) {
        this.arrivalResponse = arrivalResponse;
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
            case EHV:
                result = "Eindhoven";
                break;
            case Heeze:
                result = "Heeze";
                break;
            case RDaal:
                result = "Roosendaal";
                break;
            default:
                result = "I don't know city";
        }
        return result;
    }

    /**
     * get arrival time of voyage, given departure time
     *
     * @param depTime departure time ns-format
     * @param field do you want delays or arrival times? Should be ns node string
     * @return arrival time
     */
    public String getNSStringByDepartureTime(String depTime, String field) {
        if (depTime == null) {
            return null;
        } else {
            String arrivalTime = "+0";
            if (response == null) {
                response = "No response from NS or first time";
            }
            try {
                //create java DOM xml parser
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder;
                builder = builderFactory.newDocumentBuilder();

                //create XPath object
                XPath xPath = XPathFactory.newInstance().newXPath();

                String arrivalOrDepExpr;


                //parse xml with the DOM parser
                Document xmlDocument = builder.parse(new ByteArrayInputStream(response.getBytes()));

                if (from == EHV && to == RDaal) {
                    arrivalOrDepExpr = "/ReisMogelijkheden/ReisMogelijkheid" +
                            "[ActueleVertrekTijd[text()='" + depTime + "']]/ActueleAankomstTijd";

                    //deptime is departure time in EHV
                    //get new deptime, the first departure time in Breda
                    // which is later than the arrival time
                    NodeList arrivalNodeList = (NodeList) xPath.compile(arrivalOrDepExpr).evaluate(
                            xmlDocument, XPathConstants.NODESET);

                    if (arrivalNodeList.getLength() == 0) {
                        //there is no arrivalTime
                        return "arr: No breda arrival time.";
                    }

                    //arrivalNodeList should contain the (hopefully only one) arrival time in Breda
                    String bredaArrivalTime = arrivalNodeList.item(0).getFirstChild().getNodeValue();

                    //get next departure times
                    //arrivalResponse contains xml Breda-RDaal

                    //if going to RDaal, means arrival time is in arrivalResponse
                    xmlDocument = builder.parse(new ByteArrayInputStream(arrivalResponse.getBytes()));

                    //get all breda departure times
                    String depExpr = "//ActueleVertrekTijd";

                    NodeList BredaDepNodeList = (NodeList) xPath.compile(depExpr).evaluate(
                            xmlDocument, XPathConstants.NODESET);

                    List<String> BredaDepNSTimes = new ArrayList<>();

                    //use dep times from xml
                    for (int i = 0; i < BredaDepNodeList.getLength(); i++) {
                        BredaDepNSTimes.add(i, BredaDepNodeList.item(i).getFirstChild().getNodeValue());
                    }

                    //compare with breda arrival time
                    Date BredaArrivalDate = convertNSToDate(bredaArrivalTime);

                    //find next departure time in List
                    int nextIndex = -1;
                    //convert to date to compare
                    for (int i = 0; i < BredaDepNSTimes.size(); i++) {
                        Date nsDate = convertNSToDate(BredaDepNSTimes.get(i));
                        if (BredaArrivalDate.before(nsDate)) {
                            nextIndex = i; //i is index of next departure time.
                            break;
                        }
                    }

                    if (nextIndex == -1) {
                        Log.e("breda ", "departure time mistake ");
                    } else {
                        //depTime becomes next Breda departure Time
                        depTime = BredaDepNSTimes.get(nextIndex);
                    }
                }

                //now (possibly again) arrival time with depTime.
                // depTime may have been updated to Breda depTime
                arrivalOrDepExpr = "/ReisMogelijkheden" +
                        "/ReisMogelijkheid[ActueleVertrekTijd[text()='" + depTime + "']]/" + field;

                NodeList nodeList = (NodeList) xPath.compile(arrivalOrDepExpr).evaluate(
                        xmlDocument, XPathConstants.NODESET);

                if (nodeList.getLength() == 0) {
                    //there is no arrivalTime
                    return "No delays.";
                }
                //set arrivalTime using the arrivalTime found
                arrivalTime = nodeList.item(0).getFirstChild().getNodeValue();

            } catch (ParserConfigurationException | SAXException |
                    IOException | XPathExpressionException e) {
                e.printStackTrace();
            }

            return arrivalTime;
        }
    }

    /**
     * Communicates with ASyncTask using the instance variables to, from and response
     * also in WidgetProvider
     *
     * @return Calendar[] with five current departures, default is five null objects
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

            //if from EHV to RDaal, select intercities only
            if (from == EHV && to == RDaal) {
                //select all departure times where type is Intercity

                // select all ActueleVertrekTijd where the first Reisdeel
                // has a child VervoerType with text Intercity
                String depTimesICExpr = "//ReisMogelijkheid[ReisDeel[1]/" +
                        "VervoerType = 'Intercity']/ActueleVertrekTijd";
                nodeList = (NodeList) xPath.compile(depTimesICExpr).evaluate(
                        xmlDocument, XPathConstants.NODESET);
            } else if (from == RDaal && to == EHV) {
                String depTimesICExpr = "//ReisMogelijkheid[ReisDeel[last()]/" +
                        "VervoerType = 'Intercity']/ActueleVertrekTijd";
                nodeList = (NodeList) xPath.compile(depTimesICExpr).evaluate(
                        xmlDocument, XPathConstants.NODESET);
            } else {
                //generate list of departure times corresponding to nrpickers
                //just the departure times where status != niet-mogelijk
                String depTimesExpr = "/ReisMogelijkheden/ReisMogelijkheid" +
                        "[Status[not(text()='NIET-MOGELIJK')]]/ActueleVertrekTijd";
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

            if (nsTimes.size() < 5) {
                Log.e("nstimes size is ", Integer.toString(nsTimes.size()));
                Toast.makeText(this, "Warning, due to NS messing up, results may be inaccurate",
                        Toast.LENGTH_LONG).show();
            }

            Calendar[] depTimes = new Calendar[5];

            if (nextIndex < 2) {
                if (nextIndex == -1) {
                    Log.e("nextIndex", "no next departure time!");
                } else {
                    Log.e("nextIndex", "is too small: " + Integer.toString(nextIndex));
                }
                } else {
                //index is index of next dept time of all the xml deptimes in nsTimes
                //get departure times around next time
                for (int i = 0; i < depTimes.length; i++) {
                    if (nextIndex - 2 + i < nsTimes.size()) {
                        //if not out of bounds... (happens when ns returns <5 times total)
                        depTimes[i] = convertNSToCal(nsTimes.get(nextIndex - 2 + i));
                    }
                }
                }

            return depTimes;

        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            e.printStackTrace();
        }
        return new Calendar[5]; //return default, null objects
    }

    /**
     * convert ns-format to HH:mm
     *
     * @param nsTime ns time
     * @return string
     */
    public String convertNSToString(String nsTime) {
        if (nsTime == null) {
            return "No time selected";
        } else {
            Calendar c = convertNSToCal(nsTime);
            if (from == EHV && to == RDaal) { //if going to Rdaal
                //round time to nearest ten minutes
                int unroundedMinutes = c.get(Calendar.MINUTE);
                int mod = unroundedMinutes % 10;
                c.add(Calendar.MINUTE, 20); //add 20 minutes for bike time
                c.add(Calendar.MINUTE, mod < 5 ? -mod : (10 - mod));
            }
            return convertCalendarToString(c);
        }
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

    /**
     * convert calendar object to string object in HH:mm format
     *
     * @param c calendar object
     * @return string object
     */
    public String convertCalendarToString(Calendar c) {
        if (c == null) {
            return "No time selected";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
            return sdf.format(c.getTime());
        }
    }

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

    /**
     * Called from {@link AsyncTask} when there is no internet connection
     */
    public void noInternetConnection() {
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
     * whatsapp a delayed arrival time
     *
     * @param view button sendDelay
     */
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

    public void sendText(View view) {

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
        } else if (from == RDaal) {
            if (to == EHV) {
                message = "ETA " + nsArrivalTime;
            } else if (to == Heeze) {
                message = "ETA " + nsArrivalTime;
            }
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
    public void sendWhatsApp(String text) {
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
            } catch (IOException e) {
                Log.e("IOException", "no internet connection");
                return "no internet";
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
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