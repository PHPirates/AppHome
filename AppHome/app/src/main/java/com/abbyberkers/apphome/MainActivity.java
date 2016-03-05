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
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    /**
     * Buttons.
     */

    public void nextTrainRoosendaal(View view) {
        sendText("Roosendaal", true);
    }

    public void lastTrainRoosendaal(View view) {
        sendText("Roosendaal", false);
    }

    public void nextTrainHeeze(View view) {
        sendText("Heeze", true);
    }

    public void lastTrainHeeze(View view) {
        sendText("Heeze", false);
    }

    /**
     * Sending of text
     *
     * @param dest      destination of you.
     * @param nextTrain taking next or took last train
     */

    public void sendText(String dest, boolean nextTrain) {
        //initialise calendar with current time, trimmed
        Calendar c = Calendar.getInstance();
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        //add the minutes until the next train
        int minutes = c.get(Calendar.MINUTE);

        int trainTime = 0;
        switch (dest) {
            case "Roosendaal":
                trainTime = 31; //train departs at :31
                break;
            case "Heeze":
                trainTime = 34; //:34
                break;
            default:
                Toast.makeText(this, "I don't know the destination!", Toast.LENGTH_SHORT).show();
                break;
        }

        if (nextTrain) { //taking next train
            if (minutes < trainTime) {
                minutes = trainTime; //next train at e.g. :31
            } else {
                minutes = 30 + trainTime; //add up to the next train departure at e.g. :01
            }
        } else { //took last train
            if (minutes < trainTime) {
                minutes = trainTime - 30;
            } else {
                minutes = trainTime;
            }
        }

        c.set(Calendar.MINUTE, minutes); //set time of next train
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                "HH:mm", java.util.Locale.getDefault());

        String message = "hi.";
        switch (dest) {
            case "Roosendaal":
                c.add(Calendar.MINUTE, 89); //add time I need to get home
                message = "ETA " + simpleDateFormat.format(c.getTime());
                break;
            case "Heeze":
                message = "Trein van " + simpleDateFormat.format(c.getTime());
                break;
            default:
                Toast.makeText(this, "I don't know the destination!", Toast.LENGTH_SHORT).show();
                break;
        }

        //send to whatsapp
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.setType("text/plain");
        sendIntent.setPackage("com.whatsapp");
        startActivity(sendIntent);
    }
}
