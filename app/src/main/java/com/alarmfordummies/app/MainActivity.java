package com.alarmfordummies.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.AlarmClock;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.util.Calendar;

public class MainActivity extends FragmentActivity implements TimePickerDialog.OnTimeSetListener {

    public static final String TIMEPICKER_TAG = "timepicker";
    public static final String START_TIME_IN_MINUTES_KEY = "startTimeInMinutes";
    public static final String END_TIME_IN_MINUTES_KEY = "endTimeInMinutes";

    private boolean first = true;
    protected int frequency;
    private int tempStartMinute;
    private int tempStartHour;
    private int startTimeInMinutes;
    private int endTimeInMinutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Calendar calendar = Calendar.getInstance();

        final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false, false);

        findViewById(R.id.startTimeButton).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                timePickerDialog.show(getSupportFragmentManager(), TIMEPICKER_TAG);
                first = true;
            }
        });

        findViewById(R.id.endTimeButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerDialog.show(getSupportFragmentManager(), TIMEPICKER_TAG);
                first = false;
            }
        });

        findViewById(R.id.submit_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });

        if (savedInstanceState != null) {
            TimePickerDialog tpd = (TimePickerDialog) getSupportFragmentManager().findFragmentByTag(TIMEPICKER_TAG);
            if (tpd != null) {
                tpd.setOnTimeSetListener(this);
            }
        }
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        if (first) {
            putInt(START_TIME_IN_MINUTES_KEY, hourOfDay * 60 + minute);
            startTimeInMinutes = getInt(START_TIME_IN_MINUTES_KEY, 0);
            first = false;
        } else {
            putInt(END_TIME_IN_MINUTES_KEY, hourOfDay * 60 + minute);
            endTimeInMinutes = getInt(END_TIME_IN_MINUTES_KEY, 0);
            first = true;
        }
    }

    private void submit() {
        EditText frequencyText = (EditText) findViewById(R.id.frequencyEditText);
        try {
            frequency = Integer.parseInt(frequencyText.getText().toString());
        } catch (Exception e) {
        }
        ;
        if (frequencyText.getText() != null && frequency != 0) {
            if (endTimeInMinutes <= startTimeInMinutes) {
                Toast.makeText(MainActivity.this, "End Time must be after Start Time", Toast.LENGTH_LONG).show();
            } else {
                final Thread background = new Thread(new Runnable() {
                    public void run() {
                        while (endTimeInMinutes >= startTimeInMinutes) {
                            tempStartHour = endTimeInMinutes / 60;
                            tempStartMinute = endTimeInMinutes % 60;
                            Intent i = new Intent(AlarmClock.ACTION_SET_ALARM);
                            i.putExtra(AlarmClock.EXTRA_MESSAGE, "Alarm For Dummies");
                            i.putExtra(AlarmClock.EXTRA_HOUR, tempStartHour);
                            i.putExtra(AlarmClock.EXTRA_MINUTES, tempStartMinute);
                            i.putExtra(AlarmClock.EXTRA_SKIP_UI,true);
                            startActivity(i);
                            endTimeInMinutes -= frequency;
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                background.start();

            }
        }else {
            Toast.makeText(MainActivity.this,"You must set the frequency to start Alarm !",Toast.LENGTH_LONG).show();
        }
    }
    public int getInt(String key, int defValue) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        return settings.getInt(key, defValue);
    }

    public void putInt(String key, int value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
        editor.putInt(key, value);
        editor.commit();
    }
}
