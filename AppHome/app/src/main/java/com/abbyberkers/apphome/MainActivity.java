package com.abbyberkers.apphome;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

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
                Toast.makeText(getApplicationContext(), Integer.toString(from), Toast.LENGTH_SHORT).show();

//                Log.e("update","updatelisten");
//                Toast.makeText(getApplicationContext(),"updatelisten",Toast.LENGTH_SHORT).show();
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
//        Log.e("update","update");
//        Toast.makeText(this,"update",Toast.LENGTH_SHORT).show();
        int nrDepTimes = 5; //number of departure times
        int lo = -2; //lower and higher bound of for loops
        int hi = 3;

    //initialise calendar with current time and trim
        Calendar c = Calendar.getInstance();
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        String[] dep = new String[nrDepTimes];


        int EHV = 0;
        int Heeze = 1;
        int RDaal = 2;

    //time(14,0) gives next departure time when departure is :14 each half hour
        if (from == EHV) {
            if (to == Heeze) {
                Log.e("cD","ehv to heeze");
                Toast.makeText(this,"ehv to heeze",Toast.LENGTH_SHORT).show();
                for (int i = lo; i < hi; i++) {
                    dep[i+2] = time(4,30*i);
                }
            } else if (to == RDaal) {
                for (int i = lo; i < hi; i++) {
                    dep[i+2] = time(1,30*i);
                }
            } else {
                for (int i = lo; i < hi; i++) {
                    dep[i+2] = time(0,30*i);
                }
            }
        } else if (from == Heeze) { //TODO make for loops for these times
            if (to == EHV) {
                dep = new String[]{"Time one", "Time two", "Time three", "Time four", "Time five"};
            } else if (to == RDaal) {
                dep = new String[]{"Time one", "Time two", "Time three", "Time four", "Time five"};

            }
        } else if (from == RDaal) {
            dep = new String[]{"Time one", "Time two", "Time three", "Time four", "Time five"};

            if (to == EHV) {
            } else if (to == Heeze) {
                dep = new String[]{"Time one", "Time two", "Time three", "Time four", "Time five"};
            }
        }

        return dep;
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
                message = "Trein van " + time(4, 0);
            } else if (to == RDaal) {
                message = "ETA " + time(1, 89);

            }
        } else if (from == Heeze) {
            if (to == EHV) {
                message = "Eindhoven ETA " + time(15, 15);
            } else if (to == RDaal) {
                message = "Aiming for the " + time(1, 0) + " Eindhoven train.";
            }
        } else if (from == RDaal) {
            if (to == EHV) {
                message = "ETA " + time(20, 70);
            } else if (to == Heeze) {
                message = "ETA " + time(20, 113);
            }
        }

        sendText(message);

    }

    /**
     * departure time should be the first departure time in a given hour
     *
     * @param depart Train departure time
     * @param travel Total travel time
     * @return ETA destination, or departure time if travel=0
     */

    String time(int depart, int travel) {
        //initialise calendar with current time and trim
        Calendar c = Calendar.getInstance();
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        //time next train
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
            minutes = depart + 30; //took the :20 train
        } else { //minutes >depart+30
            minutes = depart;
            hours++;
        }
        }
        c.set(Calendar.MINUTE, minutes);
        c.set(Calendar.HOUR_OF_DAY, hours);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        if (travel != 0) { //if we want to know arrival time, otherwise return departure time
            c.add(Calendar.MINUTE, travel); //time I need to get home
        }

        return simpleDateFormat.format(c.getTime());
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