package com.example.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

public class MyMediaPlayer extends Service {

    private MediaPlayer mediaPlayer;
    private Timer timer;

    public MyMediaPlayer() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new MusicControl();
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {
        return super.onStartCommand(intent, flag, startId);
    }

    public void addTimer() {
        if (timer == null) {
            timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    int duration = mediaPlayer.getDuration();
                    int currentPos = mediaPlayer.getCurrentPosition();
                    Message message = MainActivity.handler.obtainMessage();

                    Bundle bundle = new Bundle();
                    bundle.putInt("duration", duration);
                    bundle.putInt("currentPosition", currentPos);
                    message.setData(bundle);
                    MainActivity.handler.sendMessage(message);
                }
            };
            timer.schedule(task, 5, 500);
        }
    }

    class MusicControl extends Binder {
        public void play(String path) {
            mediaPlayer.reset();
            Uri uri = Uri.parse(path);
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.start();
            addTimer();
        }

        public void pause() {
            mediaPlayer.pause();
        }

        public void stop() {
            mediaPlayer.stop();
            mediaPlayer.release();
            timer.cancel();
        }

        public void resume() {
            mediaPlayer.start();
        }

        public void seekTo(int ms) {
            mediaPlayer.seekTo(ms);
        }
    }
}