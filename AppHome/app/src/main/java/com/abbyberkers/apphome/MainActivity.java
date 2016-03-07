package com.abbyberkers.apphome;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    int from = 0; //default from Eindhoven
    int to;
    int depart; //departure numberpicker value

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
                    dep[i + 2] = arrivalTimeCal(4, 30 * i);
                }
            } else if (to == RDaal) {
                for (int i = lo; i < hi; i++) {
                    dep[i + 2] = arrivalTimeCal(1, 30 * i);
                }
            }
        } else if (from == Heeze) {
            if (to == EHV || to == RDaal) {
                for (int i = lo; i < hi; i++) {
                    dep[i + 2] = arrivalTimeCal(16, 30 * i);
                }
            }
        } else if (from == RDaal) {
            if (to == EHV || to == Heeze) {
                for (int i = lo; i < hi; i++) {
                    dep[i + 2] = arrivalTimeCal(50, 30 * i);
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

    public Calendar arrivalTimeCal(int depart, int offset) {
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