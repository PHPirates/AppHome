package com.abbyberkers.apphome;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.NumberPicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    int from = 0; //default from Eindhoven
    int to;
    int depart; //departure numberpicker value
    boolean boxChecked; //check if checkbox checked

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
//                Toast.makeText(getApplicationContext(), Integer.toString(from),
//                        Toast.LENGTH_SHORT).show();
                updateDepartures();
            }
        });


        String[] departTimes = currentDepartures();

        NumberPicker npDep; //NP for close departure times
        npDep = (NumberPicker) findViewById(R.id.numberPickerDepartures);

        npDep.setMinValue(0);
        npDep.setMaxValue(departTimes.length - 1);
        npDep.setValue(2);
        npDep.setDisplayedValues(departTimes);
        npDep.setWrapSelectorWheel(false);
        npDep.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        npDep.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                depart = newVal;
            }
        });
    }

    public void updateDepartures() {
        String[] departTimes = currentDepartures();
        NumberPicker npDep; //NP for close departure times
        npDep = (NumberPicker) findViewById(R.id.numberPickerDepartures);
        npDep.setDisplayedValues(departTimes);
    }

    public String[] currentDepartures() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        Calendar[] calendars = currentDeparturesCal();
        String[] deps = new String[calendars.length];
        for (int i = 0; i < calendars.length; i++) {
            deps[i] = simpleDateFormat.format(calendars[i].getTime());
        }
        return deps;
    }

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

        String[] def = new String[]{"", "", "", "", ""}; //TODO default thing

        //arrivalTime(14,0) gives next departure time when departure is :14 each half hour
        if (from == EHV) {
            if (to == Heeze) {
                for (int i = lo; i < hi; i++) {
                    dep[i + 2] = arrivalTimeCal(4, 30 * i);
                }
            } else if (to == RDaal) {
                for (int i = lo; i < hi; i++) {
                    dep[i + 2] = arrivalTimeCal(1, 30 * i);
                }
            } else {
                //dep = def;
            }
        } else if (from == Heeze) {
            if (to == EHV) {
                for (int i = lo; i < hi; i++) {
                    dep[i + 2] = arrivalTimeCal(16, 30 * i);
                }
            } else if (to == RDaal) {
                for (int i = lo; i < hi; i++) {
                    dep[i + 2] = arrivalTimeCal(16, 30 * i);
                }
            } else {
                //dep = def;
            }
        } else if (from == RDaal) {
            if (to == EHV) {
                for (int i = lo; i < hi; i++) {
                    dep[i + 2] = arrivalTimeCal(50, 30 * i);
                }
            } else if (to == Heeze) {
                for (int i = lo; i < hi; i++) {
                    dep[i + 2] = arrivalTimeCal(50, 30 * i);
                }
            } else {
                //dep = def;
            }
        }

        return dep;
    }



    /**
     * Sends the text, called on buttonclick
     *
     * @param view button Send
     */

    public void sendText(View view) { //TODO should take the chosen time

        String message = "You are here already, you stupid!";

        int EHV = 0;
        int Heeze = 1;
        int RDaal = 2;

        if (from == EHV) {
            if (to == Heeze) {
                message = "Trein van " + arrivalTime(4, 0);
            } else if (to == RDaal) {
//                message = "ETA " + arrivalTime(1, 89);
                message = "ETA " + cAddTravel(currentDeparturesCal()[depart],89);

            }
        } else if (from == Heeze) {
            if (to == EHV) {
                message = "Eindhoven ETA " + arrivalTime(15, 15);
            } else if (to == RDaal) {
                message = "Aiming for the " + arrivalTime(1, 0) + " Eindhoven train.";
            }
        } else if (from == RDaal) {
            if (to == EHV) {
                message = "ETA " + arrivalTime(20, 70);
            } else if (to == Heeze) {
                message = "ETA " + arrivalTime(20, 113);
            }
        }

        sendText(message);

    }

    public String cAddTravel(Calendar c, int travel) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        c.add(Calendar.MINUTE,travel);
        return simpleDateFormat.format(c.getTime());
    }
    /**
     * departure time should be the first departure time in a given hour
     *
     * @param depart Train departure time
     * @param travel Total travel time
     * @return ETA destination, or departure time if travel=0
     */

    public String arrivalTime(int depart, int travel) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        Calendar c = arrivalTimeCal(depart,travel);
        return simpleDateFormat.format(c.getTime());
    }

    public Calendar arrivalTimeCal(int depart, int travel) {
        //initialise calendar with current arrivalTime and trim
        Calendar c = Calendar.getInstance();
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        //arrivalTime next train
        int minutes = c.get(Calendar.MINUTE);
        int hours = c.get(Calendar.HOUR_OF_DAY);
        if (!boxChecked) { //taking next train
            if (minutes < depart) {
                minutes = depart; //next train at e.g. :20
            } else if (minutes < depart + 30) {
                minutes = 30 + depart; //add up to the next train departure at e.g. :50
            } else { //minutes>depart+30
                minutes = depart + 60; //train departure in the next hour, :20
            }
        } else { //took last train
            if (minutes < depart) {
                minutes = depart - 30; //e.g. the :50 of last hour
            } else if (minutes < depart + 30) {
                minutes = depart; //took the :20 train
            } else { //minutes >depart+30
                minutes = depart + 30;
//            hours++;
            }
        }
        c.set(Calendar.MINUTE, minutes);
        c.set(Calendar.HOUR_OF_DAY, hours);

        if (travel != 0) { //if we want to know arrival arrivalTime, otherwise return departure arrivalTime
            c.add(Calendar.MINUTE, travel); //arrivalTime I need to get home
        }
    return c;
    }

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

    /**
     * if checkbox hit
     *
     * @param v view of checkbox
     */
    public void checkedLastTrain(View v) {
        CheckBox checkbox = (CheckBox) findViewById(R.id.lastCheck);
        boxChecked = checkbox.isChecked();
    }

}