package com.example.samsung.p1281_audiofocus;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.IOException;

import static android.media.AudioManager.*;
import static android.media.MediaPlayer.*;

public class MainActivity extends AppCompatActivity implements OnCompletionListener, OnPreparedListener {

    private final String  DATA_STREAM = "http://online.radiorecord.ru:8101/rr_128";
    private AudioManager audioManager;

    private AFListener afListenerMusic, afListenerSound;

    private MediaPlayer mediaPlayerMusic, mediaPlayerSound;
    private String msg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    public void onClickMusic(View view) {

        int requestResult = 0;

        if (mediaPlayerMusic != null) mediaPlayerMusic.release();

        mediaPlayerMusic = new MediaPlayer();
        try {
            mediaPlayerMusic.setDataSource(DATA_STREAM);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayerMusic.setOnPreparedListener(this);
        mediaPlayerMusic.setOnCompletionListener(this);

        afListenerMusic = new AFListener(mediaPlayerMusic, "Music");
        requestResult = audioManager.requestAudioFocus(
                afListenerMusic, STREAM_MUSIC, AUDIOFOCUS_GAIN);
        msg = "Music request focus, result = " + requestResult;
        Messager.sendToAllRecipients(getBaseContext(), msg);
        mediaPlayerMusic.prepareAsync();
    }

    public void onClickSound(View view) {

        int durationHint = 0, requestResult = 0;

        switch (view.getId()) {

           case R.id.btnPlaySoundG:
                durationHint = AudioManager.AUDIOFOCUS_GAIN;
                break;
            case R.id.btnPlaySoundGT:
                durationHint = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
                break;
            case R.id.btnPlaySoundGTD:
                durationHint = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK;
                break;
            case R.id.btnPlaySoundGTE:
                durationHint = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE;
                break;
            default:
                break;
        }
        mediaPlayerSound = MediaPlayer.create(this, R.raw.explosion);
        mediaPlayerSound.setOnCompletionListener(this);

        afListenerSound = new AFListener(mediaPlayerSound, "Sound");
        requestResult = audioManager.requestAudioFocus(
                afListenerSound, AudioManager.STREAM_MUSIC, durationHint);
        msg = "Sound request focus, result = " + requestResult;
        Messager.sendToAllRecipients(getBaseContext(), msg);
        mediaPlayerSound.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        if (mp == mediaPlayerMusic) {
            msg = "Music: abandon focus";
            Messager.sendToAllRecipients(getBaseContext(), msg);
            audioManager.abandonAudioFocus(afListenerMusic);
        } else if (mp == mediaPlayerSound) {
            msg = "Sound: abandon focus";
            Messager.sendToAllRecipients(getBaseContext(), msg);
            audioManager.abandonAudioFocus(afListenerSound);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mediaPlayerMusic != null) {
            mediaPlayerMusic.release();
        }
        if (mediaPlayerSound != null) {
            mediaPlayerSound.release();
        }
        if (afListenerMusic != null) {
            audioManager.abandonAudioFocus(afListenerMusic);
        }
        if (afListenerSound != null) {
            audioManager.abandonAudioFocus(afListenerSound);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mp != null){
            msg = "onPrepared(), mp = " + mp.toString();
            Messager.sendToAllRecipients(getBaseContext(), msg);
            mp.start();
        }
    }

    private class AFListener implements OnAudioFocusChangeListener {

        private String label;
        private MediaPlayer mediaPlayer;

        public AFListener(final MediaPlayer mediaPlayer, final String music) {
            this.mediaPlayer = mediaPlayer;
            this.label = music;
        }

        @Override
        public void onAudioFocusChange(int focusChange) {

            String event = "";

            switch (focusChange) {

                case AudioManager.AUDIOFOCUS_LOSS:
                    event = "AUDIOFOCUS_LOSS";
                    mediaPlayer.pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    event = "AUDIOFOCUS_LOSS_TRANSIENT";
                    mediaPlayer.pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    event = "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK";
                    mediaPlayer.setVolume(0.5f, 0.5f);
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    event = "AUDIOFOCUS_GAIN";
                    if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                    mediaPlayer.setVolume(1.0f, 1.0f);
                    break;
                default:
                    break;
            }
            msg = "onAudioFocusChange(), event = " + event;
            Messager.sendToAllRecipients(getBaseContext(), msg);
        }
    }
}
