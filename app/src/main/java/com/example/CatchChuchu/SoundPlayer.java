package com.example.CatchChuchu;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import com.example.CatchChuchu.R;

public class SoundPlayer {
    private AudioAttributes audioAttributes;
    final int SOUND_POOL_MAX = 3;
    private static SoundPool soundpool;
    private static int hitChuchuSound;
    private static int hitXYZSound;
    private static int hitCheckStyleSound;

    public SoundPlayer(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();

            soundpool = new SoundPool.Builder().setAudioAttributes(audioAttributes).setMaxStreams(SOUND_POOL_MAX).build();
        } else {
            soundpool = new SoundPool(SOUND_POOL_MAX, AudioManager.STREAM_MUSIC, 0);
        }

        hitChuchuSound = soundpool.load(context, R.raw.chuchu, 1);
        hitXYZSound = soundpool.load(context, R.raw.xyz,1);
        hitCheckStyleSound = soundpool.load(context,R.raw.checkstyle, 1);

    }
    public void playHitChuchuSound() {
        soundpool.play(hitChuchuSound, 1.0f,1.0f,1,0,1.0f);
    }

    public void playHitXYZSound() {
        soundpool.play(hitXYZSound, 1.0f,1.0f,1,0,1.0f);
    }

    public void playHitCheckStyleSound() {
        soundpool.play(hitCheckStyleSound, 1.0f,1.0f,1,0,1.0f);
    }



}
