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
import android.widget.TextView;

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

    int from = 0; //default from Eindhoven
    int to;
    int depart; //departure numberpicker value

    TextView responseView;
    ProgressBar progressBar;
    String response; //set after getting xml from ns, used by getNSDepTimes TODO link

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //SETUP NS STUFF
        responseView = (TextView) findViewById(R.id.responseView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        //------


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
                updateDepartures();
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
                updateDepartures();
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

        new RetrieveFeedTask().execute();
    }

    class RetrieveFeedTask extends AsyncTask<Void, Void, String> {

        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            responseView.setText("");
        }

        protected String doInBackground(Void... urls) {
            InputStream in;
            int resCode;

            try {
                //http://t.m.schouten@student.tue.nl:sO-65AZxuErJmmC28eIRB85aos7oGVJ0C6tOZI9YeHDPLXeEv1nfBg@webservices.ns.nl/ns-api-treinplanner?fromStation=Roosendaal&toStation=Eindhoven
                URL url = new URL("http://webservices.ns.nl/ns-api-treinplanner?fromStation=Roosendaal&toStation=Eindhoven");

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
                }
                finally{
                    urlConnection.disconnect();
                }
            }
            catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
//            if(response == null) {
//                response = "THERE WAS AN ERROR";
//            }
            progressBar.setVisibility(View.GONE);
            setResponse(response);
        }
    }

    public void setResponse(String response) {
        this.response = response;
        //debug
        String[] res = getNSDepartures();
    }

    public String[] getNSDepartures() {
        if (response == null) {
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

//            String testexpr = "/ReisMogelijkheden/ReisMogelijkheid[3]/ActueleVertrekTijd";
//            String arrivalTime = xPath.compile(testexpr).evaluate(xmlDocument);
//            Log.e("arrivaltime", arrivalTime);
////            responseView.setText(arrivalTime);

            //generate list of departure times corresponding to nrpickers
            String depTimesExpr = "//ActueleVertrekTijd";
            NodeList nodeList = (NodeList) xPath.compile(depTimesExpr).evaluate(xmlDocument, XPathConstants.NODESET);
            List<String> nsTimes = new ArrayList<>();

            //use dep times from xml
            for (int i = 0; i < nodeList.getLength(); i++) {
                nsTimes.add(i, nodeList.item(i).getFirstChild().getNodeValue());
            }

            //get next departure time
            Calendar nextDepCal = departureTimeCal(20, 0); //uses instance variables to and from
            Date nextDepDate = nextDepCal.getTime();

            //find next departure time in List
            int nextIndex = -1;
            //convert to date to compare
            for (int i = 0; i < nsTimes.size(); i++) {
                Date nsDate = convertNSToDate(nsTimes.get(i));
                if (nextDepDate.before(nsDate)) {
                    nextIndex = i - 1; //index of next time. No idea why -1 is needed
                    break;
                }
            }

            String[] depTimes = new String[5];

            if (nextIndex == -1) {
                Log.e("nextIndex", "no next departure time!");
            } else {
                //index is index of next dept time of all the xml deptimes in nsTimes
                //get departure times around next time
                for (int i = 0; i < depTimes.length; i++) {
                    depTimes[i] = convertNSToString(nsTimes.get(nextIndex - 2 + i));
                }
            }

//            //convert to HH:mm
//            for (int i = 0; i < nsTimes.size(); i++) {
//                String depTime = nsTimes.get(i);
//                depTime = convertNSToString(depTime);
//                nsTimes.set(i,depTime);
//            }

            //test printing
            String res = "";
            for (String depTime : depTimes) {
                res += "\n" + depTime;
            }
            responseView.setText(res);

            return depTimes;
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            e.printStackTrace();
        }
        return new String[]{" ", " ", " ", " ", " "};
    }

    public Date convertNSToDate(String nsTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            return sdf.parse(nsTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date(); //default
    }

    public String convertNSToString(String nsTime) {
        Date date = convertNSToDate(nsTime);
        Calendar c = new GregorianCalendar();
        c.setTime(date);
//        c.add(Calendar.HOUR, 1); //add one hour because +1:00 NS times
        return convertCalendarToString(c);
    }

    public String convertCalendarToString(Calendar c) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        return sdf.format(c.getTime());
    }

    //*****************normal stuff****************

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
            setDividerColor(npDep, ContextCompat.getColor(this,R.color.divider));
        }
    }

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
        Calendar[] calendars = currentDeparturesCal();
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
     * Departure times magik values are in here
     * @return calendar array of current departures, null objects if to==from
     */
    public Calendar[] currentDeparturesCal() {
        int nrDepTimes = 5; //number of departure times
        int lo = -2; //lower and higher bound of for loops
        int hi = 3;

        //initialise calendar with current time and trim
        Calendar c = Calendar.getInstance();
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Calendar[] dep = new Calendar[nrDepTimes];

        int EHV = 0;
        int Heeze = 1;
        int RDaal = 2;

        //arrivalTime(14,0) gives next departure time when departure is :14 each half hour
        if (from == EHV) {
            if (to == Heeze) {
                for (int i = lo; i < hi; i++) {
                    //generate departure times using travel time, back and forth in time
                    dep[i + 2] = departureTimeCal(4, 30 * i);
                }
            } else if (to == RDaal) {
                for (int i = lo; i < hi; i++) {
                    dep[i + 2] = departureTimeCal(1, 30 * i);
                }
            }
        } else if (from == Heeze) {
            if (to == EHV || to == RDaal) {
                for (int i = lo; i < hi; i++) {
                    dep[i + 2] = departureTimeCal(16, 30 * i);
                }
            }
        } else if (from == RDaal) {
            if (to == EHV || to == Heeze) {
                for (int i = lo; i < hi; i++) {
                    dep[i + 2] = departureTimeCal(50, 30 * i);
                }
            }
        }

        return dep;
    }


    /**
     * Sends the text, called on buttonclick
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
                message = "Trein van " + cAddTravel(currentDeparturesCal()[depart], 0);
            } else if (to == RDaal) {
                message = "ETA " + cAddTravel(currentDeparturesCal()[depart], 89);
            }
        } else if (from == Heeze) {
            if (to == EHV) {
                message = "Eindhoven ETA " + cAddTravel(currentDeparturesCal()[depart], 15);
            } else if (to == RDaal) {
                message = "Aiming for the " + cAddTravel(currentDeparturesCal()[depart], 0) + " Eindhoven train.";
            }
        } else if (from == RDaal) {
            if (to == EHV) {
                message = "ETA " + cAddTravel(currentDeparturesCal()[depart], 70);  //20,70
            } else if (to == Heeze) {
                message = "ETA " + cAddTravel(currentDeparturesCal()[depart], 113);
            }
        }

        sendText(message);

    }

    /**
     * @param c calendar object, probably the one chosen by the time nrpicker
     * @param travel optional travel time
     * @return added travel time to calendar object and converted to string
     */
    public String cAddTravel(Calendar c, int travel) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        c.add(Calendar.MINUTE, travel);
        return simpleDateFormat.format(c.getTime());
    }

    /**
     * departure time should be the first departure time in a given hour
     * @param depart Train departure time
     * @param offset Total travel time
     * @return departure time with offset
     */

    public Calendar departureTimeCal(int depart, int offset) {
        //initialise calendar with current arrivalTime and trim
        Calendar c = Calendar.getInstance();
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        //arrivalTime next train
        int minutes = c.get(Calendar.MINUTE);

        if (minutes < depart) {
            minutes = depart; //next train at e.g. :20
        } else if (minutes < depart + 30) {
            minutes = 30 + depart; //add up to the next train departure at e.g. :50
        } else { //minutes>depart+30
            minutes = depart + 60; //train departure in the next hour, :20
        }

        //set calendar
        c.set(Calendar.MINUTE, minutes);
        c.add(Calendar.MINUTE, offset); //add travel time

        return c;
    }

    /**
     * send message to whatsapp
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
}