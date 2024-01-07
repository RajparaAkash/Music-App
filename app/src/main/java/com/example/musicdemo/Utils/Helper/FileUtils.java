package com.example.musicdemo.Utils.Helper;

import android.util.Log;

import java.io.File;

public class FileUtils {

    public static void createDir(String str) {
        File file = new File(str);
        if (!file.exists() && !file.isDirectory()) {
            if (file.mkdirs()) {
                Log.e("CreateDir", "App dir created");
                return;
            } else {
                Log.e("CreateDir", "Unable to create app dir!");
                return;
            }
        }
        Log.e("CreateDir", "App dir already exists");
    }
}
