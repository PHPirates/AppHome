package com.abbyberkers.apphome;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.NumberPicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    NumberPicker npFrom;
    NumberPicker npTo;
    int from = 0; //default from Eindhoven
    int to;

    String[] cities = new String[]{"Eindhoven", "Heeze", "Roosendaal"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        npFrom = (NumberPicker) findViewById(R.id.numberPickerFrom);

        npFrom.setMinValue(0);
        npFrom.setMaxValue(cities.length - 1);
        npFrom.setDisplayedValues(cities);
        npFrom.setWrapSelectorWheel(true);

        npFrom.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                from = newVal;
            }
        });

        npTo = (NumberPicker) findViewById(R.id.numberPickerTo);

        npTo.setMinValue(0);
        npTo.setMaxValue(cities.length - 1);
        npTo.setDisplayedValues(cities);
        npTo.setWrapSelectorWheel(true);

        npTo.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                to = newVal;
            }
        });


    }

    /**
     * buttons ------------------------------------------------------
     */

    public void sendText(View view){

        String message = "Yay! I am default.";

        if(from == 0){
            if(to == 1){
                message = "Trein van " + time(34, 0);
            } else if(to == 2){
                message = "ETA " + time(31, 89);

            }
        } else if(from == 1){
            if(to == 0){
                message = "Eindhoven ETA " + time(45,15);
            } else if(to == 2){
                message = "Aiming for the " + time(31, 0) + " Eindhoven train.";
            }
        } else if(from == 2) {
            if(to == 0){
                message = "ETA " + time(50, 100);
            } else if(to == 1){
                message = "ETA " + time(50, 113);
            }
        }

        sendText(message);

    }

    String time(int depart, int travel){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        //time next train
        int minutes = c.get(Calendar.MINUTE);
        if (minutes < depart) {
            minutes = depart; //next train at :31
        } else {
            minutes = depart + 30; //add up to the next train departure at :01
        }
        c.set(Calendar.MINUTE, minutes);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        if(travel > 0) {
            c.add(Calendar.MINUTE, travel); //time I need to get home
        }

        return simpleDateFormat.format(c.getTime());
    }

    public void sendText(String text){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        sendIntent.setPackage("com.whatsapp");
        startActivity(sendIntent);
    }

}