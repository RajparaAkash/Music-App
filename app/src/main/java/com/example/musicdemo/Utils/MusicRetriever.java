package com.example.musicdemo.Utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.musicdemo.Model.Music;

import java.util.ArrayList;
import java.util.List;

public class MusicRetriever {

    public static List<Music> getAllMusic(Context context) {

        List<Music> musicList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
        };

        Cursor cursor = contentResolver.query(musicUri, projection, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));

                Music music = new Music(id, title, artist, duration, filePath);
                musicList.add(music);
            } while (cursor.moveToNext());

            cursor.close();
        }

        return musicList;
    }
}
