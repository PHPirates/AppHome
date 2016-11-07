package com.abbyberkers.apphome;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class MainActivity extends AppCompatActivity {

    private BaseClass baseClass;

    private int from; //default from Eindhoven
    private int to;
    private int depart; //departure numberpicker value

    private boolean ASyncTaskIsRunning; //boolean to check if there is an asynctask running

    private Menu mainMenu;

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

        //check for first run, if it is, display setting
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean previouslyStarted = prefs.getBoolean(getString(R.string.pref_previously_started), false);
        if (!previouslyStarted) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(getString(R.string.pref_previously_started),Boolean.TRUE);
            edit.apply();
            displaySetting();
        }
    }

    /**
     * Display 'choose user' dialog
     */
    public void displaySetting() {
        final MenuItem item = mainMenu.findItem(R.id.change_user);

        new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle(R.string.change_user_title)
                .setMessage(R.string.change_user_message)
                .setPositiveButton("Abby", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putString(getString(R.string.pref_user),"Abby");
                        edit.apply();
                        Log.e("updating","menu");
                        //update menu item
                        item.setTitle("Abby");
                    }
                })
                .setNegativeButton("Thomas", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putString(getString(R.string.pref_user),"Thomas");
                        edit.apply();
                        item.setTitle("Thomas");

                    }
                })
                .show();
    }

    /**
     * @return string user from shared prefs
     */
    public String getUser() {
        //find the user
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        return prefs.getString(getString(R.string.pref_user),"none");
    }

    /**
     * Set instance response from the ASyncTask
     *
     * @param response response passed
     */
    private void setResponse(String response) {
        //set responses in both classes
        this.response = response;
        baseClass.response = response;
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String user = prefs.getString(getString(R.string.pref_user),"none");

        return baseClass.convertNSToString(nsTime, from, to, user);
    }

    /**
     * @param time string in HH:mm format
     * @return time in NS format
     */
    private String convertStringToNS(String time) {
        if (  time == null || time.contains(" ") ) { //assume correct HH:mm times
            return "Time not formatted correctly";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
            Date date = null;
            try {
                date = sdf.parse(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (date == null) return "convertStringToNS: date is null";
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(date); //changes date to 1 jan 1970
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int min = calendar.get(Calendar.MINUTE);
            Calendar c = new GregorianCalendar(); //now with correct date
            c.set(Calendar.HOUR_OF_DAY,hour);
            c.set(Calendar.MINUTE,min);
            c.set(Calendar.SECOND,0); //you'd wish trains are this precise
            return convertCalendarToNS(c);
        }
    }

    /**
     * convert ns time string to calendar object
     *
     * @param nsTime time in ns format
     * @return calendar object
     */
    private Calendar convertNSToCal(String nsTime) {
        return baseClass.convertNSToCal(nsTime);
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

    /**
     * just convert ns-format to HH:mm
     *
     * @param nsTime ns time
     * @return string
     */
    public String convertNSToString_Bare(String nsTime) {
        if (nsTime == null) {
            return "No time selected";
        } else {
            Calendar c = convertNSToCal(nsTime);
            return convertCalendarToString(c);
        }
    }

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
     * find the delayed departure times in response
     * @return hashmap, key=departure time HH:mm, value is delay +x
     */
    private Map<String, String> getDepartureDelays(String response) {
        if (response == null) {
            return null;
        } else {
            try {

                //find vertrekvertragingen

                //setup parsing
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document xmlDocument = builder.parse(new ByteArrayInputStream(response.getBytes()));


                NodeList depNodeList;
                NodeList delayNodeList;

                //select all geplandevertrektijd where the reismogelijkheid has a vertrekvertraging
                //and vervoertype of first reisdeel is intercity
                String depExpr =
                        "//ReisMogelijkheid[VertrekVertraging]/GeplandeVertrekTijd";
                //would be for breda:
//                "//ReisMogelijkheid[VertrekVertraging and ReisDeel[1]/VervoerType = 'Intercity']/GeplandeVertrekTijd";
                //select the corresponding vertrekvertragingen
                String delayExpr =
                        "//ReisMogelijkheid[VertrekVertraging]/VertrekVertraging";
                //would be for breda:
//                    "//ReisMogelijkheid[VertrekVertraging and ReisDeel[1]/VervoerType = 'Intercity']/VertrekVertraging";
                    depNodeList = (NodeList) xPath.compile(depExpr).evaluate(
                        xmlDocument, XPathConstants.NODESET);
                delayNodeList = (NodeList) xPath.compile(delayExpr).evaluate(
                        xmlDocument, XPathConstants.NODESET);


                //delayNodeList.getLength() == depNodeList.getLength()
                Map<String, String> map = new HashMap<>();

                //add everything to the map, key=departure time HH:mm, value is delay +x
                for (int i = 0; i < depNodeList.getLength(); i++) {
                    //get node and convert to HH:mm
                    String dep = convertNSToString_Bare(
                            depNodeList.item(i).getFirstChild().getNodeValue());
                    //get node and remove " min"
                    String delay = delayNodeList.item(i).getFirstChild()
                            .getNodeValue().split(" ")[0];
                    map.put(dep,delay);
                }

                return map;

            } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Update the time picker when selecting a new destination
     */
    private void updateDepartures() {
        //find out if going via breda (to look for breda delays or not)
        boolean viaBreda = to == EHV && from == RDaal || to == RDaal && from == EHV;

        Map<String, String> mapDelay = getDepartureDelays(response);
        //get delays in breda as well by arrivalResponse
        Map<String, String> mapBredaDelay = getDepartureDelays(arrivalResponse);

        String[] departTimes = currentDepartures();

        //sometimes (first times) departTimes[i] is "  " for all i
        if (departTimes[0].trim().length() > 0 ) {

        if (departTimes[0] != null) {
            //remove duplicates
            departTimes = new LinkedHashSet<>(Arrays.asList(departTimes)).toArray(new String[0]);
        }

        boolean delayedDepTime; //to add align spaces
        boolean delayedBredaDepTime;

        //for every time, if there is a match with delayed departures, add delay

            for (int i = 0; i < departTimes.length; i++) {
                delayedDepTime = false;
                delayedBredaDepTime = false;
                if (mapDelay != null) {
                    for (Map.Entry<String, String> entry : mapDelay.entrySet()) {
                        if (entry.getKey().equals(departTimes[i])) {
                            delayedDepTime = true;
                            //add the delay to it
                            if (!departTimes[i].contains(" ")) { //watch out for empty ones
                                departTimes[i] += " " + entry.getValue();
                            }
                        }
                    }
                }
                //now for breda
                //for every departure time, check also if getBredaDepTime(departTime, arrivalResponse)
                //matches with any departure time in the breda hashmap
                //if so, add bd +x
                if (viaBreda && mapBredaDelay != null) {
                    //convert to ns for getBredaDepTime, removing the +x for that
                    //only if it does contain a +x
                    String nsdep = departTimes[i];
                    if (departTimes[i].contains("+")) {
                        nsdep = departTimes[i].split(" ")[0];
                    }
                    nsdep = convertStringToNS(nsdep);

                    String nsBredaDepTime = baseClass.getBredaDepTime(nsdep, arrivalResponse);
                    if (nsBredaDepTime == null) {
                        break;
                    }
                    String bredaDepTime = convertNSToString_Bare(
                            nsBredaDepTime);
                    if (bredaDepTime == null) break;

                    for (Map.Entry<String, String> entry : mapBredaDelay.entrySet()) {
                        if (entry.getKey().equals(bredaDepTime)) {
                            delayedBredaDepTime = true;
                            departTimes[i] += " bd " + entry.getValue();
                            break;
                        }

                    }
                }
                if (!delayedDepTime) { //align a tiny bit better
                    departTimes[i] += "     ";
                }
                if (!delayedBredaDepTime) {
                    departTimes[i] += "            ";
                }
            }


        }

        NumberPicker npDep; //NP for close departure times
        npDep = (NumberPicker) findViewById(R.id.numberPickerDepartures);
        if (npDep != null) {
            //update max and min values, in case of duplicates length could change
            npDep.setDisplayedValues(null);
            npDep.setMinValue(1);
            npDep.setMaxValue(departTimes.length-1);
            npDep.setDisplayedValues(departTimes);
            int middle = departTimes.length / 2 +1;
            npDep.setValue(middle); //set default option
            depart = middle; //set chosen value to default
            if (to == from) {
                setDividerColor(npDep, 0); //set divider color to invisible
            } else { //set to default color
                setDividerColor(npDep, ContextCompat.getColor(this, R.color.divider));
            }
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

        String user = getUser();
        String prefix = "ETA ";

        //few special cases first
        if (from == EHV) {
            if (to == Heeze) {
                //take the chosen calendar object of the current departures,
                // and add optionally travel time to that and convert to string with cAddTravel
                if (user.equals("Abby")) {
                    message = "Trein van " + convertCalendarToString(getNSDepartures()[depart]);
                } else {
                    message = prefix + nsArrivalTime;
                }
            } else if (to == RDaal) {
                message = prefix + nsArrivalTime;
            }
        } else if (from == Heeze) {
            if (to == EHV) {
                if (user.equals("Abby")) {
                    message = "Eindhoven ETA " + nsArrivalTime;
                } else {
                    message = prefix + nsArrivalTime;
                }
            } else if (to == RDaal) {
                if (user.equals("Abby")) {
                    message = "Yay at " + nsArrivalTime + ".";
                } else {
                    message = prefix + nsArrivalTime;
                }
            }
        } else {
            message = prefix + nsArrivalTime;
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

                    boolean viaBreda = (from == EHV && to == RDaal) || from == RDaal && to == EHV;

                    if (viaBreda) {
                        HttpURLConnection urlConnection;
                        if (from == EHV) { //go to Breda to get also the intercity trips
                            url = new URL("http://webservices.ns.nl/ns-api-treinplanner?fromStation="
                                    + fromString + "&toStation=Breda");
                            // also get trips from Breda to RDaal for arrival times and breda delays
                        URL arrivalURL = new URL("http://webservices.ns.nl/ns-api-treinplanner" +
                                "?fromStation=Breda&toStation=" + toString);
//                            URL arrivalURL = new URL("http://hollandpirates.bitbucket.org/bd-rdaal.xml"); //debug url
                            String encoding = "dC5tLnNjaG91dGVuQHN0dWRlbnQudHVlLm5sOnNPLTY1QVp4dUVySm1tQzI4ZUlSQjg1YW9zN29HVkowQzZ0T1pJOVllSERQTFhlRXYxbmZCZw==";
                            urlConnection = (HttpURLConnection) arrivalURL.openConnection();
                        urlConnection.setRequestProperty("Authorization", "Basic " + encoding);

                        } else { // going from Rdaal
                            url = new URL("http://webservices.ns.nl/ns-api-treinplanner?fromStation="
                                    + fromString + "&toStation=Breda");
                            // also get trips from Breda to Eindhoven for arrival times and breda delays
                        URL arrivalURL = new URL("http://webservices.ns.nl/ns-api-treinplanner" +
                                "?fromStation=Breda&toStation=" + toString);
//                            URL arrivalURL = new URL("http://hollandpirates.bitbucket.org/bd-ehv.xml");
                            String encoding = "dC5tLnNjaG91dGVuQHN0dWRlbnQudHVlLm5sOnNPLTY1QVp4dUVySm1tQzI4ZUlSQjg1YW9zN29HVkowQzZ0T1pJOVllSERQTFhlRXYxbmZCZw==";
                            urlConnection = (HttpURLConnection) arrivalURL.openConnection();
                        urlConnection.setRequestProperty("Authorization", "Basic " + encoding);

                        }

                        try {
                            resCode = urlConnection.getResponseCode();
                            if (resCode == HttpURLConnection.HTTP_OK) {
                                in = urlConnection.getInputStream();
                            } else {
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

                    } else { //if not via breda
                        url = new URL("http://webservices.ns.nl/ns-api-treinplanner?fromStation="
                                + fromString + "&toStation=" + toString);
//                        url = new URL("http://hollandpirates.bitbucket.org/ehv-hz.xml"); //test url
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.mainMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.change_user:
                Toast.makeText(getBaseContext(),"Identity crisis?",Toast.LENGTH_SHORT).show();
                displaySetting();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}