package com.abbyberkers.apphome;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
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

import com.abbyberkers.apphome.ns.UseAPIKt;
import com.abbyberkers.apphome.ns.xml.ReisMogelijkheid;
import com.abbyberkers.apphome.util.ToastKt;

import java.text.ParseException;
import java.util.List;

import kotlin.reflect.KMutableProperty;

import static com.abbyberkers.apphome.converters.CalendarKt.fromNs;
import static com.abbyberkers.apphome.translations.CitiesKt.allCityStrings;

public class MainActivity extends AppCompatActivity {

    private BaseClass baseClass;


    private City from = City.EINDHOVEN; // Default from Eindhoven.
    private City to = City.EINDHOVEN; // Default to Eindhoven, not null.
    public int depart; // departure numberpicker value

    public List<ReisMogelijkheid> journeys;

    private Menu mainMenu;

    public ProgressBar progressBar;

    /**
     * OnCreate
     *
     * @param savedInstanceState bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        baseClass = new BaseClass();

        //progressbar when getting response from slow NS
        progressBar = findViewById(R.id.progressBar);

        NumberPicker npFrom;
        npFrom = findViewById(R.id.numberPickerFrom);

        assert npFrom != null;
        npFrom.setMinValue(0);
        npFrom.setMaxValue(City.values().length - 1);
        npFrom.setDisplayedValues(allCityStrings()); // TODO (Kotlin) https://github.com/PHPirates/AppHome/issues/12 id:1
        npFrom.setWrapSelectorWheel(false);
        npFrom.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        setDividerColor(npFrom, ContextCompat.getColor(this, R.color.divider));

        npFrom.setOnValueChangedListener((picker, oldVal, newVal) -> {
            from = City.values()[newVal];
            getNewJourneys();
        });

        NumberPicker npTo;

        npTo = findViewById(R.id.numberPickerTo);

        assert npTo != null;
        npTo.setMinValue(0);
        npTo.setMaxValue(City.values().length - 1);
        npTo.setDisplayedValues(allCityStrings()); // TODO https://github.com/PHPirates/AppHome/issues/13 id:2
        npTo.setWrapSelectorWheel(false);
        npTo.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        setDividerColor(npTo, ContextCompat.getColor(this, R.color.divider));


        npTo.setOnValueChangedListener((picker, oldVal, newVal) -> {
            to = City.values()[newVal];
            getNewJourneys();
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

        NumberPicker npDep = findViewById(R.id.numberPickerDepartures); //NP for close departure times

        assert npDep != null;
        npDep.setMinValue(0);
        npDep.setMaxValue(1);
        npDep.setWrapSelectorWheel(false);
        npDep.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); //todo ?

        npDep.setOnValueChangedListener((picker, oldVal, newVal) -> depart = newVal);

        getNewJourneys(); //update departures
    }

    /**
     * Called when opening app again, to refresh the times in the departure numberpicker (npDep).
     */

    @Override
    public void onResume(){
        super.onResume();
        getNewJourneys(); // Get and parse xml from ns.
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
                    .setPositiveButton("Abby", (dialogInterface, i) -> {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putString(getString(R.string.pref_user), "Abby");
                        edit.apply();
                        //update menu item
                        item.setTitle("Abby");
                    })
                    .setNegativeButton("Thomas", (dialogInterface, i) -> {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putString(getString(R.string.pref_user), "Thomas");
                        edit.apply();
                        item.setTitle("Thomas");

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
        CheckBox pluralBox = findViewById(R.id.pluralBox);
        assert pluralBox != null;
        return pluralBox.isChecked();
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
     * updates numberpicker with fresh data from NS.
     */
    private void getNewJourneys() {
        // Get the new data.
        NumberPicker departureNP = findViewById(R.id.numberPickerDepartures);
        UseAPIKt.updateNumberPicker(departureNP, from, to, this);
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
     * TODO https://github.com/TODO https://github.com/TODO https://github.com/PHPirates/AppHome/issues/14 id:3
     * whatsapp a delayed arrival time
     *
     * @param view button sendDelay
     */
    @SuppressWarnings("unused") // Actually injected by sendDelayButton
    public void sendDelay(View view) {
//        String delay = journeys.get(depart-1).departureDelay;
        String delay = journeys.get(depart-1).delay();
        if(delay != null) {
//            delay = delay.replace(" min", "");
            sendWhatsApp(delay);
        } else {
            ToastKt.toast(this, "No delay :)");
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
            // Currently: convertNSToString instead of nsToString because this adds bike time
            String nsArrivalTime = convertNSToString(journeys.get(depart - 1).arrivalTime);

            String user = getUser();
            boolean plural = getPlural();
            String prefix = "ETA ";
            String postfix = ".";

            // Set a default message.
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
                    message = "Trein van " + fromNs(journeys.get(depart-1).departureTime);
                } else if (from == City.HEEZE && to == City.EINDHOVEN) {
                        message = "Eindhoven ETA " + nsArrivalTime;

                } else if (to == City.ROOSENDAAL) {
                    message = "Yay at " + nsArrivalTime + ".";
                } else if (to == City.OVERLOON) {
                    if (plural) {
                        message = "We zijn rond " + nsArrivalTime + " thuis.";
                    } else {
                        message = "Ik ben rond " + nsArrivalTime + " thuis.";
                    }
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