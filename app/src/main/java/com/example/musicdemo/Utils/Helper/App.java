package com.example.musicdemo.Utils.Helper;

import android.app.Application;
import android.os.Handler;
import android.provider.MediaStore;

public class App extends Application {

    private static App app;
    public DatabaseHelper databaseHelper;
    public FFmpegConverter fFmpegConverter;
    public MediaStoreObserver mediaStoreObserver;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        this.databaseHelper = new DatabaseHelper(getApplicationContext());
        this.fFmpegConverter = new FFmpegConverter(getApplicationContext());
        this.mediaStoreObserver = new MediaStoreObserver(new Handler());
        getContentResolver().registerContentObserver(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true, this.mediaStoreObserver);
        getContentResolver().registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, this.mediaStoreObserver);
        getContentResolver().registerContentObserver(MediaStore.Video.Media.INTERNAL_CONTENT_URI, true, this.mediaStoreObserver);
        getContentResolver().registerContentObserver(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, true, this.mediaStoreObserver);
    }

    public static App getInstance() {
        return app;
    }
}
