package com.example.CatchChuchu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.CatchChuchu.R;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class MainActivity extends AppCompatActivity {

    private FrameLayout gameFrame;
    private int frameHeight;
    private int frameWidth;
    private int  inititalFrameWidth;
    private LinearLayout startLayout;

    private ImageView geoff;
    private ImageView checkstyle;
    private ImageView chuchu;
    private ImageView xyz;
    private Drawable geoffRight;
    private Drawable geoffLeft;

    private int geoffSize;

    private float geoffX;
    private float geoffY;
    private float checkstyleX;
    private float checkstyleY;
    private float chuchuX;
    private float chuchuY;
    private float xyzX;
    private float xyzY;

    private TextView scoreLabel;
    private TextView highScoreLabel;
    private int score;
    private int highScore;
    private int timeCount;
    private SharedPreferences settings;

    private Timer timer;
    private Handler handler = new Handler();
    private SoundPlayer soundplayer;

    private boolean startFlag = false;
    private boolean actionFlag = false;
    private boolean xyzFlag = false;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    33);

        }
        soundplayer = new SoundPlayer(this);

        gameFrame = findViewById(R.id.gameFrame);
        startLayout = findViewById(R.id.startLayout);
        geoff = findViewById(R.id.geoff);
        checkstyle = findViewById(R.id.checkstyle);
        chuchu = findViewById(R.id.chuchu);
        xyz = findViewById(R.id.xyz);
        scoreLabel = findViewById(R.id.scoreLabel);
        highScoreLabel = findViewById(R.id.highScoreLabel);

        geoffLeft = getResources().getDrawable(R.drawable.geoff_left);
        geoffRight = getResources().getDrawable(R.drawable.geoff_right);

        //High score

        settings = getSharedPreferences("GAME_DATA", Context.MODE_PRIVATE);
        highScore = settings.getInt("HIGH_SCORE", 0);
        highScoreLabel.setText("Highscore :" + highScore);


        // CREDIT TO https://medium.com/@juniorbump/pitch-detection-in-android-using-tarsosdsp-a2dd4a3f04e9
        // Tarsos DSP lib used
        AudioDispatcher dispatcher =
                AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);
        PitchDetectionHandler pitchhandler = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult res, AudioEvent e){
                final float pitchInHz = res.getPitch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (startFlag) {
                            if (pitchInHz >= 145) {
                                actionFlag = true;
                            } else {
                                actionFlag = false;
                            }
                        }
                    }
                });
            }
        };
        AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pitchhandler);
        dispatcher.addAudioProcessor(pitchProcessor);

        Thread audioThread = new Thread(dispatcher, "Audio Thread");
        audioThread.start();

    }

    public void changePos() {

        //Timecount

        timeCount = timeCount + 20;

        //Chuchu

        chuchuY = chuchuY + 12;

        float chuchuCenterX = chuchuX + chuchu.getWidth() / 2;
        float chuchuCenterY = chuchuY + chuchu.getHeight() / 2;

        if (hitCheck(chuchuCenterX, chuchuCenterY)) {
            chuchuY = frameHeight + 100;
            score = score + 10;
            soundplayer.playHitChuchuSound();

        }

        if (chuchuY > frameHeight) {
            chuchuY = -100;
            chuchuX = (float) Math.floor(Math.random() * (frameWidth - chuchu.getWidth()));
        }
        chuchu.setX(chuchuX);
        chuchu.setY(chuchuY);

        //XYZ

        if (!xyzFlag && timeCount % 10000 == 0) {
            xyzFlag = true;
            xyzY = -40;
            xyzX = (float) Math.floor(Math.random() * (frameWidth - xyz.getWidth()));
        }

        if (xyzFlag) {
            xyzY = xyzY + 40;

            float pinkCenterX = xyzX + xyz.getWidth() / 2;
            float pinkCenterY = xyzY + xyz.getHeight() / 2;

            if (hitCheck(pinkCenterX, pinkCenterY)) {
                xyzY = frameHeight + 50;
                score = score + 50;


                //Change Framewidth

                if (inititalFrameWidth > frameWidth * 120 / 100) {
                    frameWidth = frameWidth * 120 / 100;
                    changeFrameWidth(frameWidth);
                }
                soundplayer.playHitXYZSound();
            }

            if (xyzY > frameHeight) xyzFlag = false;
            xyz.setX(xyzX);
            xyz.setY(xyzY);
        }


        checkstyleY = checkstyleY + 15;

        float checkstyleCenterX = checkstyleX + checkstyle.getWidth() / 2;
        float checkstyleCenterY = checkstyleY + checkstyle.getHeight() / 2;

        if (hitCheck(checkstyleCenterX, checkstyleCenterY)) {
            checkstyleY = frameHeight + 100;

            //Change FrameWidth
            frameWidth = frameWidth * 85/100;
            changeFrameWidth(frameWidth);
            soundplayer.playHitCheckStyleSound();


            if (frameWidth <= geoffSize) {
                gameOver();


            }
        }

        if (checkstyleY > frameHeight) {
            checkstyleY = -100;
            checkstyleX = (float) Math.floor(Math.random() * (frameWidth - checkstyle.getWidth()));

        }
        checkstyle.setX(checkstyleX);
        checkstyle.setY(checkstyleY);



        //moveChallen
        if (actionFlag) {
            //touching screen
            geoffX = geoffX + 20;
            geoff.setImageDrawable(geoffRight);
        } else {
            //releasing screen
            geoffX = geoffX - 20;
            geoff.setImageDrawable(geoffLeft);
        }
        if (geoffX < 0) {
            geoffX = 0;
            geoff.setImageDrawable(geoffRight);
        }

        //check Challen's position

        if (frameWidth - geoffSize < geoffX) {
            geoffX = frameWidth - geoffSize;
            geoff.setImageDrawable(geoffLeft);
        }
        geoff.setX(geoffX);
        scoreLabel.setText("Score :" + score );
    }

    public boolean hitCheck(float x, float y) {
        if (geoffX <= x && x <= geoffX + geoffSize && geoffY <= y && y <= frameHeight) {
            return true;
        }
        return false;
    }

    public void changeFrameWidth(int frameWidth) {
        ViewGroup.LayoutParams params = gameFrame.getLayoutParams();
        params.width = frameWidth;
        gameFrame.setLayoutParams(params);
    }

    public void gameOver() {
        timer.cancel();
        timer = null;
        startFlag = false;

        //1 sec pause before showing start Layout

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        changeFrameWidth(inititalFrameWidth);

        startLayout.setVisibility(View.VISIBLE);
        geoff.setVisibility(View.INVISIBLE);
        checkstyle.setVisibility(View.INVISIBLE);
        chuchu.setVisibility(View.INVISIBLE);
        xyz.setVisibility(View.INVISIBLE);

        //Update Highscore

        if (score > highScore) {
            highScore = score;
            highScoreLabel.setText("Highscore :" + highScore);

            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("HIGH_SCORE", highScore);
            editor.commit();
        }

    }


    public void startGame(View view) {
        startFlag = true;
        startLayout.setVisibility(View.INVISIBLE);

        if (frameHeight == 0) {
            frameHeight = gameFrame.getHeight();
            frameWidth = gameFrame.getWidth();
            inititalFrameWidth = frameWidth;

            geoffSize = geoff.getHeight();
            geoffX = geoff.getX();
            geoffY = geoff.getY();
        }

        frameWidth = inititalFrameWidth;

        geoff.setX(0.0f);
        checkstyle.setY(3000.0f);
        chuchu.setY(3000.0f);
        xyz.setY(3000.f);

        checkstyleY = checkstyle.getY();
        chuchuY = chuchu.getY();
        xyzY = xyz.getY();

        geoff.setVisibility(View.VISIBLE);
        checkstyle.setVisibility(View.VISIBLE);
        chuchu.setVisibility(View.VISIBLE);
        xyz.setVisibility(View.VISIBLE);

        timeCount = 0;
        score = 0;
        scoreLabel.setText("Score : 0");

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (startFlag) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            changePos();
                        }
                    });
                }

            }
        },0, 20);

    }

    public void quitGame(View view) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask();
        } else {
            finish();
        }

    }
}
