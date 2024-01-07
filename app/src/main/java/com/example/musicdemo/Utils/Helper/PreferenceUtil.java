package com.example.musicdemo.Utils.Helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;

public final class PreferenceUtil {

    public static String DEFAULT_APP_RESULT_FOLDER = "CutMusic";
    public static final String VIDEO_CONVERTED = "video_converted";
    private static PreferenceUtil sInstance;
    private final SharedPreferences mPreferences;

    private PreferenceUtil(Context context) {
        this.mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PreferenceUtil getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PreferenceUtil(context.getApplicationContext());
        }
        return sInstance;
    }

    public static String getMusicOutputFolder() {
        String str = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath() + File.separator + DEFAULT_APP_RESULT_FOLDER;
        FileUtils.createDir(str);
        return str;
    }

    public void setVideoConverted() {
        SharedPreferences.Editor edit = this.mPreferences.edit();
        edit.putBoolean(VIDEO_CONVERTED, true);
        edit.commit();
    }
}
