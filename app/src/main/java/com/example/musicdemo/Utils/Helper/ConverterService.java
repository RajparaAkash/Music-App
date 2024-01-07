package com.example.musicdemo.Utils.Helper;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ConverterService extends Service {

    private FFmpegConverter ffmpegConverter;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int i, int i2) {
        this.ffmpegConverter = App.getInstance().fFmpegConverter;
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent intent) {
        super.onTaskRemoved(intent);
        this.ffmpegConverter.deleteAllNotifications();
        stopSelf();
    }
}
