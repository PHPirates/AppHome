package com.abbyberkers.apphome;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class WidgetConfigurationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_configuration);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeAppWidget();

        setResult(RESULT_CANCELED);
    }

    int mAppWidgetId;

    private void initializeAppWidget(){
        mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(EXTRA_APPWIDGET_ID,
                    INVALID_APPWIDGET_ID);
//            showProgressDialog();
            saveTheUserValueInPref(selectedCategory, sourceAndLanguage,
                    mAppWidgetId);
            getDataToLoadInWidget = new GetDataToLoadInWidget(
                    ConfigurationActivity.this, selectedSource,
                    selectedLanguage, selectedCategory);
        }
        if (mAppWidgetId == INVALID_APPWIDGET_ID) {
            finish();
        }
    }

}
