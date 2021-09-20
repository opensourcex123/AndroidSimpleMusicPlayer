package com.example.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    private static SeekBar musicProgressBar;
    @SuppressLint("StaticFieldLeak")
    private static TextView currentTime;
    @SuppressLint("StaticFieldLeak")
    private static TextView totalTime;
    private Button start, stop, lastSong, nextSong, randStart, orderStart;

    ArrayList<Song> songList = new ArrayList<>();
    ListView songListView;
    public String path = "android.resource://com.example.musicplayer/raw/";
    Bundle bundle = new Bundle();
    boolean RANDOM_START = false;
    boolean ORDER_START = true;

    private static MyMediaPlayer.MusicControl control;

    private final ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            control = (MyMediaPlayer.MusicControl) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        for (int i = 0; i <= 2; i++) {
            Song song = new Song();
            song.setName("music" + i);
            songList.add(song);
        }

        songListView.setAdapter(new SongAdapter(getApplicationContext(), songList));
        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                control.play(path + "music" + i);
                bundle.putInt("position", i);
            }
        });
    }

    @Override
    protected void onDestroy() {
        unbindService(conn);
        control.stop();
        super.onDestroy();
    }

    public void init() {
        musicProgressBar = findViewById(R.id.sb);
        currentTime = findViewById(R.id.music_progress);
        totalTime = findViewById(R.id.total);
        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        lastSong = findViewById(R.id.lastSong);
        nextSong = findViewById(R.id.nextSong);
        randStart = findViewById(R.id.randStart);
        orderStart = findViewById(R.id.orderStart);
        songListView = findViewById(R.id.songList);

        OnClick onClick = new OnClick();
        start.setOnClickListener(onClick);
        stop.setOnClickListener(onClick);
        lastSong.setOnClickListener(onClick);
        nextSong.setOnClickListener(onClick);
        randStart.setOnClickListener(onClick);
        orderStart.setOnClickListener(onClick);

        Intent intent = new Intent(getApplicationContext(), MyMediaPlayer.class);
        bindService(intent, conn, BIND_AUTO_CREATE);
    }

    class OnClick implements View.OnClickListener {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.start:
                    int position = bundle.getInt("position");
                    control.play(path + "music" + position);
                    break;
                case R.id.stop:
                    control.pause();
                    break;
                case R.id.lastSong:
                    if (ORDER_START) {
                        position = bundle.getInt("position");
                        if (position == 0) {
                            Toast.makeText(MainActivity.this, "已经是第一首了", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            control.play(path + "music" + (position - 1) % songList.size());
                            bundle.putInt("position", (position - 1) % songList.size());
                        }
                        break;
                    } else if (RANDOM_START) {
                        int random = new Random().nextInt(songList.size());
                        bundle.putInt("position", random);
                        control.play(path + "music" + random);
                        break;
                    }

                case R.id.nextSong:
                    if (ORDER_START) {
                        position = bundle.getInt("position");
                        if (position == songList.size() - 1) {
                            Toast.makeText(MainActivity.this, "已经是最后一首了", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            control.play(path + "music" + (position + 1) % songList.size());
                            bundle.putInt("position", (position + 1) % songList.size());
                        }
                        break;
                    } else if (RANDOM_START) {
                        int random = new Random().nextInt(songList.size());
                        bundle.putInt("position", random);
                        control.play(path + "music" + random);
                        break;
                    }

                case R.id.randStart:
                    RANDOM_START = true;
                    ORDER_START = false;
                    Toast.makeText(MainActivity.this, "随机播放", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.orderStart:
                    RANDOM_START = false;
                    ORDER_START = true;
                    Toast.makeText(MainActivity.this, "顺序播放", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    public static Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            int duration = bundle.getInt("duration", 0);
            int currentPosition = bundle.getInt("currentPosition", 0);
            musicProgressBar.setMax(duration);
            musicProgressBar.setProgress(currentPosition);
            String total = msToMinSec(duration);
            String current = msToMinSec(currentPosition);
            totalTime.setText(total);
            currentTime.setText(current);
            musicProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if (b) {
                        control.seekTo(i);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    control.pause();
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    control.resume();
                }
            });
        }
    };

    @SuppressLint("DefaultLocale")
    private static String msToMinSec(int ms) {
        int sec = ms / 1000;
        int min = sec / 60;
        sec -= min * 60;
        return String.format("%02d:%02d", min, sec);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add: {
                InsertDialog();
            }
            break;
            case R.id.delete: {
                DeleteDialog();
            }
            break;
        }
        return true;
    }

    private void DeleteDialog() {
        final TableLayout tableLayout = (TableLayout) getLayoutInflater().inflate(R.layout.insert, null);
        new AlertDialog.Builder(this)
                .setTitle("删除歌曲")
                .setView(tableLayout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String songName = ((EditText) tableLayout.findViewById(R.id.songAddName)).getText().toString();
                        Delete(songName);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .create()
                .show();
    }

    private void InsertDialog() {
        final TableLayout tableLayout = (TableLayout) getLayoutInflater().inflate(R.layout.insert, null);
        new AlertDialog.Builder(this)
                .setTitle("新增歌曲")
                .setView(tableLayout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String songName = ((EditText) tableLayout.findViewById(R.id.songAddName)).getText().toString();
                        Insert(songName);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .create()
                .show();
    }

    private void Insert(String songName) {
        Song song = new Song();
        song.setName(songName);
        songList.add(song);
        songListView.setAdapter(new SongAdapter(getApplicationContext(), songList));
    }

    private void Delete(String songName) {
        int flag = 0;
        for (int i = 0; i < songList.size(); i++) {
            if (songName.equals(songList.get(i).getName())) {
                flag = i;
                break; //i就是索引
            }
        }
        songList.remove(flag);
        songListView.setAdapter(new SongAdapter(getApplicationContext(), songList));
    }
}