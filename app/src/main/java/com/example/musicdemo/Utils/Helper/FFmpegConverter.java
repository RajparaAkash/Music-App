package com.example.musicdemo.Utils.Helper;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.LogCallback;
import com.arthenica.mobileffmpeg.LogMessage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FFmpegConverter {

    public static final String AUDIO_FORMAT_AAC = "aac";
    public static final String AUDIO_FORMAT_AAC_FROM_VIDEO = "aac_from_video";
    public static final String AUDIO_FORMAT_MP3 = "mp3";
    private static final String TAG = "FFmpegConverter";
    public CallbackOrderListChange callbackOrderListChange;
    public CallbackProgress callbackProgress;
    private Context context;
    private ConversionNotification conversionNotification;
    public String musicPath;
    public ConversionOrder orderInProcess;
    private boolean ffmegIsSupported = false;
    private boolean ffmpegRunning = false;
    private List<ConversionOrder> conversionOrderList = new ArrayList();
    private DatabaseHelper databaseHelper = App.getInstance().databaseHelper;

    
    public interface CallbackOrderListChange {
        void callbackOrderListChange();
    }

    
    public interface CallbackProgress {
        void callbackProgress(int i);
    }

    public FFmpegConverter(Context context) {
        this.context = context;
        this.conversionNotification = new ConversionNotification(context);
        PreferenceUtil.getInstance(context);
        this.musicPath = PreferenceUtil.getMusicOutputFolder();
        init();
    }

    public void init() {
        this.ffmegIsSupported = true;
    }

    public void deleteAllNotifications() {
        this.conversionNotification.deleteAllNotifications();
    }

    public void appendConversionOrder(String str, String str2, String str3, String str4, long j) {
        ConversionOrder conversionOrder = new ConversionOrder(str, str2, str3, str4, j);
        conversionOrder.status = 2;
        this.conversionOrderList.add(0, conversionOrder);
        this.databaseHelper.saveOrder(conversionOrder);
        CallbackOrderListChange callbackOrderListChange = this.callbackOrderListChange;
        if (callbackOrderListChange != null) {
            callbackOrderListChange.callbackOrderListChange();
        }
        startConversion();
    }

    public void appendConversionOrderList(List<ConversionOrder> list) {
        for (int i = 0; i < list.size(); i++) {
            ConversionOrder conversionOrder = list.get(i);
            Log.e("progress", "o: " + conversionOrder.duration);
            conversionOrder.status = 2;
            this.conversionOrderList.add(0, conversionOrder);
            this.databaseHelper.saveOrder(conversionOrder);
            int i2 = conversionOrder.type;
        }
        CallbackOrderListChange callbackOrderListChange = this.callbackOrderListChange;
        if (callbackOrderListChange != null) {
            callbackOrderListChange.callbackOrderListChange();
        }
        startConversion();
    }

    public void appendMusicCutterOrder(String str, String str2, long j, double d, double d2) {
        ConversionOrder conversionOrder = new ConversionOrder(str, str2, j, d, d2);
        conversionOrder.status = 2;
        this.conversionOrderList.add(0, conversionOrder);
        this.databaseHelper.saveOrder(conversionOrder);
        CallbackOrderListChange callbackOrderListChange = this.callbackOrderListChange;
        if (callbackOrderListChange != null) {
            callbackOrderListChange.callbackOrderListChange();
        }
        startConversion();
    }

    public List<ConversionOrder> getOrdersList() {
        return this.conversionOrderList;
    }

    public void removeConversionOrder(ConversionOrder conversionOrder) {
        String str = conversionOrder.musicResultPath;
        conversionOrder.status = 3;
        this.databaseHelper.saveOrder(conversionOrder);
        for (int i = 0; i < this.conversionOrderList.size(); i++) {
            ConversionOrder conversionOrder2 = this.conversionOrderList.get(i);
            if (conversionOrder2.musicResultPath.equals(str)) {
                this.conversionOrderList.remove(conversionOrder2);
                return;
            }
        }
    }

    public void startConversion() {
        List<ConversionOrder> list = this.conversionOrderList;
        if (list == null || list.size() == 0) {
            Log.e(TAG, "conversionOrderList empty!");
            return;
        }
        List<ConversionOrder> list2 = this.conversionOrderList;
        executeConversionOrder(list2.get(list2.size() - 1));
    }

    public boolean musicFileExists(String str) {
        for (int i = 0; i < this.conversionOrderList.size(); i++) {
            if (this.conversionOrderList.get(i).musicResultPath.equals(str)) {
                return true;
            }
        }
        return new File(str).exists();
    }

    public List<ConversionOrder> getOrdersToProcess() {
        ArrayList arrayList = new ArrayList();
        for (ConversionOrder conversionOrder : this.conversionOrderList) {
            arrayList.add(0, conversionOrder);
        }
        ConversionOrder conversionOrder2 = this.orderInProcess;
        if (conversionOrder2 != null) {
            arrayList.add(conversionOrder2);
        }
        return arrayList;
    }

    public void setCallbackProgressListener(CallbackProgress callbackProgress) {
        this.callbackProgress = callbackProgress;
    }

    public void setCallbackOrderListChange(CallbackOrderListChange callbackOrderListChange) {
        this.callbackOrderListChange = callbackOrderListChange;
    }

    
    public static class ConversionOrder {
        public String[] cmd;
        public long duration;
        private String durationStr;
        public String filename;
        public String inputFilePath;
        public String musicResultPath;
        public int status;
        public int type;

        public ConversionOrder() {
            this.durationStr = "";
        }

        public ConversionOrder(String str, String str2, String str3, String str4, long j) {
            this.durationStr = "";
            this.type = 0;
            if (str3.equals(FFmpegConverter.AUDIO_FORMAT_AAC_FROM_VIDEO)) {
                this.cmd = new String[]{"-i", str, "-vn", "-acodec", "copy", str2};
            } else if (str3.equals(FFmpegConverter.AUDIO_FORMAT_AAC)) {
                this.cmd = new String[]{"-i", str, "-b:a", str4, str2};
            } else {
                this.cmd = new String[]{"-i", str, "-b:a", str4, "-f", str3, str2};
            }
            this.inputFilePath = str;
            getResultDuration();
            this.duration = j;
            this.musicResultPath = str2;
            this.filename = new File(str2).getName();
        }

        public ConversionOrder(String str) {
            this.durationStr = "";
            this.type = 3;
            this.inputFilePath = "";
            this.status = 0;
            this.duration = 0L;
            this.musicResultPath = str;
            this.filename = new File(str).getName();
        }

        public ConversionOrder(String str, String str2, long j, double d, double d2) {
            this.durationStr = "";
            this.type = 1;
            this.duration = j;
            this.inputFilePath = "";
            this.inputFilePath = str;
            this.musicResultPath = str2;
            this.filename = new File(str2).getName();
            this.cmd = new String[]{"-i", str, "-ss", floatToTime(d), "-to", floatToTime(d2), "-c", "copy", str2};
        }

        private String floatToTime(double d) {
            return String.format("%02d:%02d:%05.2f", Integer.valueOf(((int) (d / 3600.0d)) % 24), Integer.valueOf(((int) (d / 60.0d)) % 60), Float.valueOf(((float) d) % 60.0f)).replace(',', '.');
        }

        public String getResultDuration() {
            if (!this.durationStr.equals("")) {
                return this.durationStr;
            }
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            try {
                mediaMetadataRetriever.setDataSource(this.musicResultPath);
                this.duration = Long.parseLong(mediaMetadataRetriever.extractMetadata(9));
                convertMillieToHMmSs();
            } catch (Exception unused) {
                this.duration = 0L;
                convertMillieToHMmSs();
            }
            try {
                mediaMetadataRetriever.release();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this.durationStr;
        }

        private void convertMillieToHMmSs() {
            long j = this.duration / 1000;
            long j2 = j % 60;
            long j3 = (j / 60) % 60;
            long j4 = (j / 3600) % 24;
            if (j4 > 0) {
                this.durationStr = String.format("%02d:%02d:%02d", Long.valueOf(j4), Long.valueOf(j3), Long.valueOf(j2));
            } else {
                this.durationStr = String.format("%02d:%02d", Long.valueOf(j3), Long.valueOf(j2));
            }
        }
    }

    public void notifyOutputFolderChange() {
        PreferenceUtil.getInstance(this.context);
        this.musicPath = PreferenceUtil.getMusicOutputFolder();
    }

    
    public void executeConversionOrder(final ConversionOrder conversionOrder) {
        if (this.ffmpegRunning) {
            return;
        }
        if (new File(conversionOrder.musicResultPath).exists()) {
            this.conversionNotification.conversionError(conversionOrder.filename, "File already exists");
            removeOrder(conversionOrder);
            return;
        }
        this.ffmpegRunning = true;
        this.orderInProcess = conversionOrder;
        final long j = conversionOrder.duration;
        this.conversionNotification.startConvertion(conversionOrder.filename, this.conversionOrderList.size());
        conversionOrder.status = 1;
        CallbackOrderListChange callbackOrderListChange = this.callbackOrderListChange;
        if (callbackOrderListChange != null) {
            callbackOrderListChange.callbackOrderListChange();
        }
        this.ffmpegRunning = true;
        this.databaseHelper.saveOrder(conversionOrder);
        createAppDir();
        CallbackProgress callbackProgress = this.callbackProgress;
        if (callbackProgress != null) {
            callbackProgress.callbackProgress(0);
        }
        try {
            FFmpeg.executeAsync(conversionOrder.cmd, new ExecuteCallback() {
                @Override
                public void apply(long j2, int i) {
                    if (i == 0) {

                        Utility.isFinished = true;
                        Log.e(TAG, "Successfully Saved" );

                        MediaScannerConnection.scanFile(FFmpegConverter.this.context, new String[]{conversionOrder.musicResultPath}, null, new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String str, Uri uri) {
                                System.out.println("SCAN COMPLETED: " + str);
                            }
                        });
                        conversionOrder.status = 0;
                        FFmpegConverter.this.conversionNotification.conversionSucess();
                        PreferenceUtil.getInstance(FFmpegConverter.this.context).setVideoConverted();
                        FFmpegConverter.this.databaseHelper.saveOrder(conversionOrder);
                    } else if (i != 255) {
                        FFmpegConverter.this.ffmpegRunning = false;
                        Log.e(FFmpegConverter.TAG, String.format("Async command execution failed with returnCode=%d.", Integer.valueOf(i)));
                        FFmpegConverter.this.conversionNotification.conversionError(conversionOrder.filename);
                        conversionOrder.status = -1;
                    } else {
                        Log.e(FFmpegConverter.TAG, "Async command execution cancelled by user.");
                        Log.e(FFmpegConverter.TAG, String.format("Async command execution failed with returnCode=%d.", Integer.valueOf(i)));
                        FFmpegConverter.this.conversionNotification.conversionError(conversionOrder.filename);
                        conversionOrder.status = -1;
                    }
                    FFmpegConverter.this.databaseHelper.saveOrder(conversionOrder);
                    if (FFmpegConverter.this.callbackProgress != null) {
                        FFmpegConverter.this.callbackProgress.callbackProgress(100);
                    }
                    FFmpegConverter.this.ffmpegRunning = false;
                    FFmpegConverter.this.removeOrder(conversionOrder);
                    if (FFmpegConverter.this.conversionOrderList.size() > 0) {
                        FFmpegConverter fFmpegConverter = FFmpegConverter.this;
                        fFmpegConverter.executeConversionOrder((ConversionOrder) fFmpegConverter.conversionOrderList.get(FFmpegConverter.this.conversionOrderList.size() - 1));
                    }
                }
            });
        } catch (Exception e) {
            this.ffmpegRunning = false;
            Log.e(TAG, "executeConversionOrder: ", e);
            this.conversionNotification.conversionError(conversionOrder.filename);
            CallbackProgress callbackProgress2 = this.callbackProgress;
            if (callbackProgress2 != null) {
                callbackProgress2.callbackProgress(100);
            }
            conversionOrder.status = -1;
            this.databaseHelper.saveOrder(conversionOrder);
        }
        Config.enableLogCallback(new LogCallback() {
            @Override
            public void apply(LogMessage logMessage) {
                String text = logMessage.getText();
                Log.d(FFmpegConverter.TAG, "LogCallback: " + text);
                try {
                    int indexOf = text.indexOf("time=");
                    if (indexOf != -1) {
                        String[] split = text.substring(indexOf + 5, indexOf + 16).split(":");
                        int round = (int) Math.round(((((float) ((Integer.parseInt(split[0]) * 216000000) + (Integer.parseInt(split[1]) * 60000))) + (Float.parseFloat(split[2]) * 1000.0f)) * 100.0d) / j);
                        if (FFmpegConverter.this.callbackProgress != null) {
                            FFmpegConverter.this.callbackProgress.callbackProgress(round);
                        }
                        FFmpegConverter.this.conversionNotification.setProgress(round);
                    }
                } catch (Exception unused) {
                    FFmpegConverter.this.ffmpegRunning = false;
                    PreferenceUtil.getInstance(FFmpegConverter.this.context).setVideoConverted();
                    FFmpegConverter.this.removeOrder(conversionOrder);
                    if (FFmpegConverter.this.conversionOrderList.size() > 0) {
                        FFmpegConverter fFmpegConverter = FFmpegConverter.this;
                        fFmpegConverter.executeConversionOrder((ConversionOrder) fFmpegConverter.conversionOrderList.get(FFmpegConverter.this.conversionOrderList.size() - 1));
                    }
                }
            }
        });
        this.orderInProcess = null;
    }

    private void createAppDir() {
        File file = new File(this.musicPath);
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        if (!file.exists() && !file.isDirectory()) {
            if (file.mkdirs()) {
                Log.i("CreateDir", "App dir created");
                return;
            } else {
                Log.w("CreateDir", "Unable to create app dir!");
                return;
            }
        }
        Log.i("CreateDir", "App dir already exists");
    }

    public void removeOrder(ConversionOrder conversionOrder) {
        this.databaseHelper.saveOrder(conversionOrder);
        this.conversionOrderList.remove(conversionOrder);
    }
}
