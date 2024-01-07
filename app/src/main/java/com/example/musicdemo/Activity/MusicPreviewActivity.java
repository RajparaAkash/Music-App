package com.example.musicdemo.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.musicdemo.R;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class MusicPreviewActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private TextView tvCurrentTime, tvTotalTime;
    private TextView musicNameTxt;
    private ImageView musicPlayPause;
    private Handler handler;
    private Runnable updateSeekBar;

    String music_path;
    String music_filename;
    String duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_preview);

        findViewById(R.id.back_img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.homeImg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MusicPreviewActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        findViewById(R.id.shareMusicImg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    musicPlayPause.setImageResource(R.drawable.music_play_img);
                    mediaPlayer.pause();
                    handler.removeCallbacks(updateSeekBar);
                    mediaPlayer.seekTo(0);
                }

                try {
                    File file = new File(music_path);

                    Uri screenshotUri = FileProvider.getUriForFile(MusicPreviewActivity.this,
                            getApplicationContext().getPackageName() + ".provider", file);
                    Intent intent = new Intent("android.intent.action.SEND");
                    intent.setType("audio/*");
                    intent.putExtra("android.intent.extra.STREAM", screenshotUri);
                    startActivity(Intent.createChooser(intent, "Share File"));

                } catch (Exception unused) {
                    Log.e("TAG", "" + unused.getMessage());
                }
            }
        });

        mediaPlayer = new MediaPlayer();
        seekBar = findViewById(R.id.seekBar);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        musicNameTxt = findViewById(R.id.musicNameTxt);
        musicPlayPause = findViewById(R.id.musicPlayPause);
        handler = new Handler();

        Intent intent = getIntent();
        if (intent != null) {
            music_path = intent.getStringExtra("music_path");
            music_filename = intent.getStringExtra("music_filename");
            duration = intent.getStringExtra("duration");
        }

        musicNameTxt.setText(music_filename);

        try {
            mediaPlayer.setDataSource(music_path);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        initializeSeekBar();

        musicPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mediaPlayer.isPlaying()) {
                    musicPlayPause.setImageResource(R.drawable.music_pause_img);
                    mediaPlayer.start();
                    handler.post(updateSeekBar);
                    return;
                }
                if (mediaPlayer.isPlaying()) {
                    musicPlayPause.setImageResource(R.drawable.music_play_img);
                    mediaPlayer.pause();
                    handler.removeCallbacks(updateSeekBar);
                    mediaPlayer.seekTo(0);
                }
            }
        });
    }

    private void initializeSeekBar() {
        int duration = mediaPlayer.getDuration();
        seekBar.setMax(duration);
        tvTotalTime.setText(formatTime(duration));

        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    tvCurrentTime.setText(formatTime(currentPosition));
                    handler.postDelayed(this, 1000); // Update seekbar every second
                }
            }
        };

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Remove callbacks to stop updating seekbar while user is dragging
                handler.removeCallbacks(updateSeekBar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Resume updating seekbar after user has finished dragging
                handler.post(updateSeekBar);
            }
        });
    }

    private String formatTime(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        handler.removeCallbacks(updateSeekBar);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        handler.removeCallbacks(updateSeekBar);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}