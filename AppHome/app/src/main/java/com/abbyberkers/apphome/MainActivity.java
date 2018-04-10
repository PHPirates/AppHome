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
import android.widget.CheckBox;
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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import static com.abbyberkers.apphome.converters.CalendarKt.calendarToNS;
import static com.abbyberkers.apphome.converters.CalendarKt.calendarToString;
import static com.abbyberkers.apphome.converters.CalendarKt.nsToCalendar;
import static com.abbyberkers.apphome.translations.CitiesKt.allCityStrings;

public class MainActivity extends AppCompatActivity {

    private BaseClass baseClass;

    private City from; //default from Eindhoven
    private City to;
    private int depart; //departure numberpicker value

    private boolean ASyncTaskIsRunning; //boolean to check if there is an asynctask running

    private Menu mainMenu;

    private ProgressBar progressBar;
    private String response; //set after getting xml from ns, used by

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
        npFrom.setMaxValue(City.values().length - 1);
        npFrom.setDisplayedValues(allCityStrings()); // TODO (Kotlin)
        npFrom.setWrapSelectorWheel(false);
        npFrom.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        setDividerColor(npFrom, ContextCompat.getColor(this, R.color.divider));

        npFrom.setOnValueChangedListener((picker, oldVal, newVal) -> {
            from = City.values()[newVal];
            updateNumberpicker();
        });

        NumberPicker npTo;

        npTo = (NumberPicker) findViewById(R.id.numberPickerTo);

        assert npTo != null;
        npTo.setMinValue(0);
        npTo.setMaxValue(City.values().length - 1);
        npTo.setDisplayedValues(allCityStrings()); // TODO
        npTo.setWrapSelectorWheel(false);
        npTo.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        setDividerColor(npTo, ContextCompat.getColor(this, R.color.divider));


        npTo.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                to = City.values()[newVal];
                updateNumberpicker();
            }
        });

        //first check whether user is already set, then set defaults, then request times

        checkUserConfigured();

        //set a proper default
        String user = getUser();
        if (user.equals("Thomas")) {
            npFrom.setValue(City.EINDHOVEN.ordinal());
            from = City.EINDHOVEN;
            npTo.setValue(City.ROOSENDAAL.ordinal());
            to = City.ROOSENDAAL;
        } else if (user.equals("Abby")) {
            from = City.EINDHOVEN;
            npFrom.setValue(City.EINDHOVEN.ordinal());
            to = City.OVERLOON;
            npTo.setValue(City.OVERLOON.ordinal());
        }

        NumberPicker npDep; //NP for close departure times
        npDep = (NumberPicker) findViewById(R.id.numberPickerDepartures);

        assert npDep != null;
        npDep.setMinValue(0);
        npDep.setMaxValue(1);
        npDep.setWrapSelectorWheel(false);
        npDep.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); //todo ?

        npDep.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                depart = newVal;
            }
        });

        updateNumberpicker(); //update departures
    }

    /**
     * Called when opening app again, to refresh the times in the departure numberpicker (npDep).
     */

    @Override
    public void onResume(){
        super.onResume();
        updateNumberpicker(); //get xml from ns
        checkUserConfigured();
    }

    public void checkUserConfigured() {
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
        if (mainMenu != null) {
            final MenuItem item = mainMenu.findItem(R.id.change_user);

            new android.support.v7.app.AlertDialog.Builder(this)
                    .setTitle(R.string.change_user_title)
                    .setMessage(R.string.change_user_message)
                    .setPositiveButton("Abby", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                            SharedPreferences.Editor edit = prefs.edit();
                            edit.putString(getString(R.string.pref_user), "Abby");
                            edit.apply();
                            //update menu item
                            item.setTitle("Abby");
                        }
                    })
                    .setNegativeButton("Thomas", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                            SharedPreferences.Editor edit = prefs.edit();
                            edit.putString(getString(R.string.pref_user), "Thomas");
                            edit.apply();
                            item.setTitle("Thomas");

                        }
                    })
                    .show();
        }
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
     * @return True if the plural checkbox is checked, false otherwise.
     */
    public boolean getPlural() {
        CheckBox pluralBox = (CheckBox) findViewById(R.id.pluralBox);
        assert pluralBox != null;
        return pluralBox.isChecked();
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

    /**
     * get arrival time of voyage, given departure time
     *
     * @param depTime departure time ns-format
     * @param field do you want delays or arrival times? Should be ns node string
     * @return arrival time
     */
    private String getNSStringByDepartureTime(String depTime, String field) {
        baseClass.response = this.response;
        String res = baseClass.getNSStringByDepartureTime(depTime, field);
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
        return baseClass.getNSDepartures(this.response);
    }

    /**
     * convert ns-format to HH:mm
     *
     * @param nsTime ns time
     * @return string
     */
    private String convertNSToString(String nsTime) throws ParseException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String user = prefs.getString(getString(R.string.pref_user),"none");
        return baseClass.convertNSToString(nsTime, to, user);
    }

    /**
     * just convert ns-format to HH:mm
     *
     * @param nsTime ns time
     * @return string
     */
    public String convertNSToString_Bare(String nsTime) throws ParseException {

        if (nsTime == null) {
            return "No time selected";
        } else {
            Calendar c = nsToCalendar(nsTime);
            return calendarToString(c);
        }
    }

    /**
     * find the delayed departure times in response
     * @return hashmap, key=departure time HH:mm, value is delay +x
     */
    private Map<String, String> getDepartureDelays(String response) {
        if (response == null) {
            Log.e("getDepartureDelays","null passed");
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
                //select the corresponding vertrekvertragingen
                String delayExpr =
                        "//ReisMogelijkheid[VertrekVertraging]/VertrekVertraging";
                    depNodeList = (NodeList) xPath.compile(depExpr).evaluate(
                        xmlDocument, XPathConstants.NODESET);
                delayNodeList = (NodeList) xPath.compile(delayExpr).evaluate(
                        xmlDocument, XPathConstants.NODESET);


                //delayNodeList.getLength() == depNodeList.getLength()
                Map<String, String> map = new HashMap<>();

                //add everything to the map, key=departure time HH:mm, value is delay +x
                for (int i = 0; i < depNodeList.getLength(); i++) {
                    try {
                        //get node and convert to HH:mm
                        String dep = convertNSToString_Bare(depNodeList.item(i).getFirstChild().getNodeValue());
                        //get node and remove " min"
                        String delay = delayNodeList.item(i).getFirstChild()
                                .getNodeValue().split(" ")[0];
                        map.put(dep, delay);
                    } catch (ParseException e) {
                        Log.e("getDepartureDelays","convertNSToString failed");
                    }
                }

                return map;

            } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * updates numberpicker with fresh data from NS.
     */
    private void updateNumberpicker() {
        new RetrieveFeedTask().execute();
    }

    /**
     * Update the time picker GIVEN new data from NS
     */
    private void processNSDataForNumberpicker() {

        Map<String, String> mapDelay = getDepartureDelays(response);

        String[] departTimes = currentDepartures();

        //sometimes (first times) departTimes[i] is "  " for all i
        if (departTimes[0].trim().length() > 0 ) {

        if (departTimes[0] != null) {
            //remove duplicates
            departTimes = new LinkedHashSet<>(Arrays.asList(departTimes)).toArray(new String[0]);
        }

        boolean delayedDepTime; //to add align spaces

        //for every time, if there is a match with delayed departures, add delay

            for (int i = 0; i < departTimes.length; i++) {
                delayedDepTime = false;
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
                if (!delayedDepTime) { //align a tiny bit better
                    departTimes[i] += "     ";
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
            npDep.setWrapSelectorWheel(false);
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
     * @return string array of current departures, empty allCityStrings if to==from
     */
    private String[] currentDepartures() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        Calendar[] calendars = getNSDepartures();
        String[] deps = new String[calendars.length];
        for (int i = 0; i < calendars.length; i++) {
            if (calendars[i] != null) { //if from == to, add empty allCityStrings
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
        if (departures[depart-1] == null) {
            Toast.makeText(this, "Delay not possible", Toast.LENGTH_LONG).show();
        } else {
            //convert calendar to ns format and select the chosen departure time
            String nsDep = calendarToNS(departures[depart-1]);
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

        try {

            // 1. get five departure times in calendar format
            // 2. convert the chosen calendar to ns-format string
            // 3. get the corresponding arrival time
            // 4. convert it to HH:mm format
            Calendar nsDepartureCal = getNSDepartures()[depart-1]; //numberpicker values start at one instead of zero
            String nsDepartureString = calendarToNS(nsDepartureCal);
            String nsString = getNSStringByDepartureTime(
                    nsDepartureString, "ActueleAankomstTijd");
            String nsArrivalTime = convertNSToString(nsString);

            String user = getUser();
            boolean plural = getPlural();
            String prefix = "ETA ";
            String postfix = ".";

            message = prefix + nsArrivalTime + postfix;

            // Few special cases to overrule the default message.
            if (user.equals("Thomas")) {
                if (to == City.ROOSENDAAL) {
                    if (plural) {
                        message = "We zijn rond " + nsArrivalTime + " thuis.";
                    } else {
                        message = "Ik ben rond " + nsArrivalTime + " thuis.";
                    }
                } else if (to == City.HEEZE || to == City.OVERLOON) {
                    message = "Yay at " + nsArrivalTime + ".";
                }
            }

            if (user.equals("Abby")) {
                if (from == City.EINDHOVEN && to == City.HEEZE) {
                    // take the chosen calendar object of the current departures,
                    // and add optionally travel time to that and convert to string with cAddTravel
                    message = "Trein van " + calendarToString(nsDepartureCal);
                } else if (from == City.HEEZE && to == City.EINDHOVEN) {
                        message = "Eindhoven ETA " + nsArrivalTime;

                } else if (to == City.ROOSENDAAL) {
                    message = "Yay at " + nsArrivalTime + ".";
                } else if (to == City.OVERLOON) {
                    message = "Ik ben rond " + nsArrivalTime + " thuis.";
                }
            }

        } catch (ParseException e) {
            Log.e("sendText", "parsing to nsArrivalTime failed: " + e.getMessage());
            message = "ParseException thrown";
        }

        sendWhatsApp(message);

    }

    /**
     * send message to whatsapp
     *
     * @param text the message
     */
    private void sendWhatsApp(String text) {
        Toast.makeText(getBaseContext(),text,Toast.LENGTH_SHORT).show();

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        sendIntent.setPackage("com.whatsapp");
        startActivity(sendIntent);
    }


    /**
     * Requests data from NS and updates numberpicker (processNSDataForNumberpicker())
     */
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
                    String fromString = from.getStation();
                    String toString = to.getStation();

                    URL url;
                        url = new URL("http://webservices.ns.nl/ns-api-treinplanner?fromStation="
                                + fromString + "&toStation=" + toString);
//                        url = new URL("http://hollandpirates.bitbucket.org/ehv-hz.xml"); //test url

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
                    Log.e("Response", response);
                    setResponse(response);
                    processNSDataForNumberpicker(); //update nrpicker
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.mainMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu,menu);
        MenuItem item = mainMenu.findItem(R.id.change_user);
        String user = getUser();
        if (user != null && !user.equals("none")) {
            item.setTitle(getUser());
        } else {
            displaySetting();
        }
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