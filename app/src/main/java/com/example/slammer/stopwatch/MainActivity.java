package com.example.slammer.stopwatch;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    // define instance variables for our views
    private TextView tv_count = null;
    private Button bt_start = null;
    private Button bt_stop = null;
    private Button bt_reset = null;
    private Button bt_resume = null;
    private Timer t = null;
    private Counter ctr = null;  // TimerTask
    private AudioAttributes aa = null;
    private SoundPool soundPool = null;
    private int bloopSound = 0;
    private int tenths = 0;  // tenths of second
    private int seconds = 0;
    private int minutes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize views
        this.tv_count = findViewById(R.id.tv_count);
        this.bt_start = findViewById(R.id.bt_start);
        this.bt_stop = findViewById(R.id.bt_stop);
        this.bt_reset = findViewById(R.id.bt_reset);
        this.bt_resume = findViewById(R.id.bt_resume);

        this.bt_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bt_start.setEnabled(false);
                bt_stop.setEnabled(true);
                bt_resume.setEnabled(false);
                ctr.running = true;
                t.scheduleAtFixedRate(ctr, 0, 100);
            }
        });

        this.bt_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // stop counter if it's running
                ctr.running = false;
                ctr = new Counter();

                // set everything back to zero
                tenths = 0;
                seconds = 0;
                minutes = 0;
                tv_count.setText("00:00:0");

                // stop and resume shouldn't be clickable now, but start should
                bt_stop.setEnabled(false);
                bt_resume.setEnabled(false);
                bt_start.setEnabled(true);
            }
        });

        this.bt_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctr.running = false;
                ctr = new Counter();

                // resume should be clickable now, but stop shouldn't
                bt_resume.setEnabled(true);
                bt_stop.setEnabled(false);
                bt_start.setEnabled(false);
            }
        });

        this.bt_resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bt_resume.setEnabled(false);
                bt_stop.setEnabled(true);
                bt_start.setEnabled(false);
                ctr.running = true;
                t.scheduleAtFixedRate(ctr, 0, 100);
            }
        });

        // stop and resume shouldn't be clickable on creation
        this.bt_stop.setEnabled(false);
        this.bt_resume.setEnabled(false);

        this.bt_start.setEnabled(true);
        this.bt_reset.setEnabled(true);

        this.aa = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_GAME).build();

        this.soundPool = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(aa).build();
        this.bloopSound = this.soundPool.load(this, R.raw.bloop, 1);

        this.tv_count.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
                soundPool.play(bloopSound, 1f, 1, 0, 0, 1f);
                Animator anim = AnimatorInflater.loadAnimator(MainActivity.this, R.animator.counter);
                anim.setTarget(tv_count);
                anim.start();
           }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // reload the count from a previous run
        // if first time running, start at 0
        // preferences to share state
        int tenths = getPreferences(MODE_PRIVATE).getInt("TENTHS", 0);
        int seconds = getPreferences(MODE_PRIVATE).getInt("SECONDS", 0);
        int minutes = getPreferences(MODE_PRIVATE).getInt("MINUTES", 0);
        if (minutes <= 9 && seconds <= 9) {
            this.tv_count.setText("0" + Integer.toString(minutes) + ":0"
                    + Integer.toString(seconds) + ":" + Integer.toString(tenths));
        } else if (minutes <= 9) {
            this.tv_count.setText("0" + Integer.toString(minutes) + ":"
                    + Integer.toString(seconds) + ":" + Integer.toString(tenths));
        } else if (seconds <= 9) {
            this.tv_count.setText(Integer.toString(minutes) + ":0"
                    + Integer.toString(seconds) + ":" + Integer.toString(tenths));
        } else {
            this.tv_count.setText(Integer.toString(minutes) + ":"
                    + Integer.toString(seconds) + ":" + Integer.toString(tenths));
        }

        // only make a new counter/timer if one doesn't already exist
        if (t == null && ctr == null) {
            this.ctr = new Counter();
            this.t = new Timer();
            if (!ctr.running) {
                if (!(minutes == 0 && seconds == 0 && tenths == 0)) {
                    this.bt_start.setEnabled(false);
                    this.bt_stop.setEnabled(false);
                    this.bt_resume.setEnabled(true);
                }
            }
        }
        this.tenths = tenths;
        this.seconds = seconds;
        this.minutes = minutes;

        // factory method - design pattern
        Toast.makeText(this, "Stopwatch is started", Toast.LENGTH_LONG).show();
    }

    class Counter extends TimerTask {
        boolean running = false;

        @Override
        public void run() {
            MainActivity.this.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        if (running) {
                            if (minutes <= 9 && seconds <= 9) {
                                MainActivity.this.tv_count.setText("0" + Integer.toString(minutes) + ":0"
                                        + Integer.toString(seconds) + ":" + Integer.toString(tenths));
                            } else if (minutes <= 9) {
                                MainActivity.this.tv_count.setText("0" + Integer.toString(minutes) + ":"
                                        + Integer.toString(seconds) + ":" + Integer.toString(tenths));
                            } else if (seconds <= 9) {
                                MainActivity.this.tv_count.setText(Integer.toString(minutes) + ":0"
                                        + Integer.toString(seconds) + ":" + Integer.toString(tenths));
                            } else {
                                MainActivity.this.tv_count.setText(Integer.toString(minutes) + ":"
                                        + Integer.toString(seconds) + ":" + Integer.toString(tenths));
                            }
                            tenths++;
                            if (tenths == 10) {
                                tenths = 0;
                                seconds++;
                            }
                            if (seconds == 60) {
                                seconds = 0;
                                minutes++;
                            }
                        } else {
                            t.cancel();
                            t.purge();
                            t = new Timer();
                        }
                    }
                }
            );
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        getPreferences(MODE_PRIVATE).edit().putInt("TENTHS", this.tenths).apply();
        getPreferences(MODE_PRIVATE).edit().putInt("SECONDS", this.seconds).apply();
        getPreferences(MODE_PRIVATE).edit().putInt("MINUTES", this.minutes).apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getPreferences(MODE_PRIVATE).edit().putInt("TENTHS", this.tenths).apply();
        getPreferences(MODE_PRIVATE).edit().putInt("SECONDS", this.seconds).apply();
        getPreferences(MODE_PRIVATE).edit().putInt("MINUTES", this.minutes).apply();
    }
}
