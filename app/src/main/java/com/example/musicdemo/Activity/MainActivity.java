package com.example.musicdemo.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaPlayer;
import android.os.Bundle;

import com.example.musicdemo.Adapter.MusicAdapter;
import com.example.musicdemo.Model.Music;
import com.example.musicdemo.Utils.MusicRetriever;
import com.example.musicdemo.R;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView musicListRV;
    List<Music> musicList;
    MusicAdapter musicAdapter;

    Music myMusic;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        idBinding();

        mediaPlayer = new MediaPlayer();
        musicListRV.setLayoutManager(new LinearLayoutManager(this));
        musicList = MusicRetriever.getAllMusic(this);
        Collections.sort(musicList, (music1, music2) -> {
            return music1.getTitle().compareToIgnoreCase(music2.getTitle());
        });

        if (musicList != null) {
            musicAdapter = new MusicAdapter(musicList, getApplicationContext(), new MusicAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int pos) {

                    myMusic = musicList.get(pos);
                    if (myMusic != null) {
                        if (myMusic.isPlaying()) {

                            mediaPlayer.stop();
                            musicList.get(pos).setPlaying(false);

                        } else {
                            try {
                                mediaPlayer.reset();
                                mediaPlayer.setDataSource(musicList.get(pos).getFilePath());
                                mediaPlayer.prepare();
                                mediaPlayer.start();
                                musicList.get(pos).setPlaying(true);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                    for (int i = 0; i < musicList.size(); i++) {
                        musicList.get(i).setPlaying(false);
                    }
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        musicList.get(pos).setPlaying(true);
                    }
                    musicAdapter.notifyDataSetChanged();
                }
            });
            musicListRV.setAdapter(musicAdapter);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void onPause() {
        super.onPause();
        for (int i = 0; i < this.musicList.size(); i++) {
            this.musicList.get(i).setPlaying(false);
        }

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        musicAdapter.notifyDataSetChanged();
    }

    private void idBinding() {
        musicListRV = findViewById(R.id.musicListRV);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        System.exit(0);
    }
}