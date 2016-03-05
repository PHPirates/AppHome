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

    public void sendText(View view){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.SECOND,0);
        c.set(Calendar.MILLISECOND,0);

        //time next train
        int minutes = c.get(Calendar.MINUTE);
        if (minutes<31) {
            minutes=31; //next train at :31
        } else {
            minutes = 61; //add up to the next train departure at :01
        }
        c.set(Calendar.MINUTE,minutes);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm",java.util.Locale.getDefault());
        //String nextTrain = simpleDateFormat.format(c.getTime());
        c.add(Calendar.MINUTE,89); //time I need to get home
        String ETA = "ETA "+simpleDateFormat.format(c.getTime());
        
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, ETA);
        sendIntent.setType("text/plain");
        sendIntent.setPackage("com.whatsapp");
        startActivity(sendIntent);
    }
}
