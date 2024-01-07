package com.example.musicdemo.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musicdemo.R;
import com.example.musicdemo.Utils.CustomAudioViews.MarkerView;
import com.example.musicdemo.Utils.CustomAudioViews.SamplePlayer;
import com.example.musicdemo.Utils.CustomAudioViews.SoundFile;
import com.example.musicdemo.Utils.CustomAudioViews.WaveformView;
import com.example.musicdemo.Utils.Helper.App;
import com.example.musicdemo.Utils.Helper.FFmpegConverter;
import com.example.musicdemo.Utils.Helper.PreferenceUtil;
import com.example.musicdemo.Utils.Helper.Utility;

import java.io.File;
import java.io.IOException;

public class AudioCutterActivity extends AppCompatActivity implements View.OnClickListener, MarkerView.MarkerListener, WaveformView.WaveformListener {

    private Handler myHandler;
    private WaveformView audioWaveform;
    private ImageView music_extract_img_3;
    private String defaultMusicPath;
    private EditText etAudioOutputTitle;
    private FFmpegConverter ffmpegConverter;
    private long inputMusicDuration;
    private String inputMusicFilename;
    private String inputMusicPath;
    private float mDensity;
    private int mEndPos;
    private boolean mEndVisible;
    private File mFile;
    private int mFlingVelocity;
    private Handler mHandler;
    private boolean mKeyDown;
    private int mLastDisplayedEndPos;
    private int mLastDisplayedStartPos;
    private SoundFile mLoadedSoundFile;
    private boolean mLoadingKeepGoing;
    private long mLoadingLastUpdateTime;
    private int mMarkerBottomOffset;
    private int mMarkerLeftInset;
    private int mMarkerRightInset;
    private int mMarkerTopOffset;
    private int mMaxPos;
    private int mOffset;
    private int mOffsetGoal;
    private int mPlayEndMillSec;
    private SamplePlayer mPlayer;
    private ProgressDialog mProgressDialog;
    private SoundFile mRecordedSoundFile;
    private int mStartPos;
    private boolean mStartVisible;
    private int mTextBottomOffset;
    private int mTextLeftInset;
    private int mTextRightInset;
    private int mTextTopOffset;
    private boolean mTouchDragging;
    private int mTouchInitialEndPos;
    private int mTouchInitialOffset;
    private int mTouchInitialStartPos;
    private float mTouchStart;
    private int mWidth;
    private MarkerView markerEnd;
    private MarkerView markerStart;
    private ImageView musicControllerPlayButton;
    private SeekBar musicControllerPositionBar;

    private String outputMusicTitle;
    private TextView tvMusicElapsedTime;
    private TextView txtEndPosition;
    private TextView txtStartPosition;
    private boolean threadRunning = false;
    private boolean mIsPlaying = false;
    private String blockCharacterSet = "<>:*./";
    private InputFilter filter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence charSequence, int i, int i2, Spanned spanned, int i3, int i4) {
            if (charSequence == null || !AudioCutterActivity.this.blockCharacterSet.contains("" + ((Object) charSequence))) {
                return null;
            }
            return "";
        }
    };
    private Handler handlerElapsedTime = new Handler() {
        @Override
        public void handleMessage(Message message) {
            int i = message.what;
            AudioCutterActivity.this.musicControllerPositionBar.setMax(AudioCutterActivity.this.audioWaveform.pixelsToMillisecs(AudioCutterActivity.this.mEndPos - AudioCutterActivity.this.mStartPos));
            int pixelsToMillisecs = i - AudioCutterActivity.this.audioWaveform.pixelsToMillisecs(AudioCutterActivity.this.mStartPos);
            AudioCutterActivity.this.musicControllerPositionBar.setProgress(pixelsToMillisecs);
            String createTimeLabel = AudioCutterActivity.this.createTimeLabel(pixelsToMillisecs);
            AudioCutterActivity audioCutterActivity = AudioCutterActivity.this;
            AudioCutterActivity.this.tvMusicElapsedTime.setText(createTimeLabel + " / " + audioCutterActivity.createTimeLabel(audioCutterActivity.audioWaveform.pixelsToMillisecs(AudioCutterActivity.this.mEndPos - AudioCutterActivity.this.mStartPos)));
        }
    };
    private Runnable mTimerRunnable = new Runnable() {
        @Override
        public void run() {
            if (AudioCutterActivity.this.mStartPos != AudioCutterActivity.this.mLastDisplayedStartPos) {
                TextView textView = AudioCutterActivity.this.txtStartPosition;
                AudioCutterActivity audioCutterActivity = AudioCutterActivity.this;
                textView.setText(audioCutterActivity.formatTime(audioCutterActivity.mStartPos));
                AudioCutterActivity audioCutterActivity2 = AudioCutterActivity.this;
                audioCutterActivity2.mLastDisplayedStartPos = audioCutterActivity2.mStartPos;
            }
            if (AudioCutterActivity.this.mEndPos != AudioCutterActivity.this.mLastDisplayedEndPos) {
                TextView textView2 = AudioCutterActivity.this.txtEndPosition;
                AudioCutterActivity audioCutterActivity3 = AudioCutterActivity.this;
                textView2.setText(audioCutterActivity3.formatTime(audioCutterActivity3.mEndPos));
                AudioCutterActivity audioCutterActivity4 = AudioCutterActivity.this;
                audioCutterActivity4.mLastDisplayedEndPos = audioCutterActivity4.mEndPos;
            }
            AudioCutterActivity.this.mHandler.postDelayed(AudioCutterActivity.this.mTimerRunnable, 100L);
        }
    };

    @Override
    public void markerDraw() {
    }

    @Override
    public void markerEnter(MarkerView markerView) {
    }

    public void playBtnClick(View view) {
    }

    @Override
    public void waveformTouchEnd() {
    }

    @Override
    public void waveformZoomIn() {
    }


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_audio_cutter);

        myHandler = new Handler(Looper.getMainLooper());
        this.ffmpegConverter = App.getInstance().fFmpegConverter;
        PreferenceUtil.getInstance(this);
        this.defaultMusicPath = PreferenceUtil.getMusicOutputFolder();
        this.inputMusicFilename = getIntent().getStringExtra("music_filename");
        this.inputMusicPath = getIntent().getStringExtra("music_path");
        this.inputMusicDuration = getIntent().getLongExtra("duration", 1L);
        String str = this.inputMusicFilename;
        this.outputMusicTitle = str;
        this.outputMusicTitle = str.split("\\.")[0];
        this.outputMusicTitle += "_cut";
        this.mHandler = new Handler();
        this.txtStartPosition = (TextView) findViewById(R.id.txtStartPosition);
        this.txtEndPosition = (TextView) findViewById(R.id.txtEndPosition);
        this.markerStart = (MarkerView) findViewById(R.id.markerStart);
        this.markerEnd = (MarkerView) findViewById(R.id.markerEnd);
        this.audioWaveform = (WaveformView) findViewById(R.id.audioWaveform);
        this.etAudioOutputTitle = (EditText) findViewById(R.id.et_music_title);
        this.music_extract_img_3 = (ImageView) findViewById(R.id.music_extract_img_3);
        this.musicControllerPlayButton = (ImageView) findViewById(R.id.musicControllerPlayButton);
        this.tvMusicElapsedTime = (TextView) findViewById(R.id.tvMusicElapsedTime);
        this.musicControllerPositionBar = (SeekBar) findViewById(R.id.positionBarElapsedTime);
        this.musicControllerPlayButton.setImageResource(R.drawable.music_play_img);
        this.musicControllerPositionBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                if (z) {
                    try {
                        int pixelsToMillisecs = AudioCutterActivity.this.audioWaveform.pixelsToMillisecs(AudioCutterActivity.this.mStartPos) + i;
                        if (AudioCutterActivity.this.mPlayer != null) {
                            AudioCutterActivity.this.mPlayer.seekTo(pixelsToMillisecs);
                        }
                        AudioCutterActivity.this.musicControllerPositionBar.setProgress(i);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        this.etAudioOutputTitle.setFilters(new InputFilter[]{this.filter});
        /*this.header_title.setText(this.inputMusicFilename);*/
        String str2 = "";
        for (char c : this.outputMusicTitle.toCharArray()) {
            if (!this.blockCharacterSet.contains("" + c)) {
                str2 = str2 + c;
            }
        }
        this.outputMusicTitle = str2;
        this.etAudioOutputTitle.setText(str2);
        this.music_extract_img_3.setVisibility(View.GONE);
        this.mRecordedSoundFile = null;
        this.mKeyDown = false;
        this.audioWaveform.setListener(this);
        this.markerStart.setListener(this);
        this.markerStart.setAlpha(1.0f);
        this.markerStart.setFocusable(true);
        this.markerStart.setFocusableInTouchMode(true);
        this.mStartVisible = true;
        this.markerEnd.setListener(this);
        this.markerEnd.setAlpha(1.0f);
        this.markerEnd.setFocusable(true);
        this.markerEnd.setFocusableInTouchMode(true);
        this.mEndVisible = true;
        float f = getResources().getDisplayMetrics().density;
        this.mDensity = f;
        this.mMarkerLeftInset = (int) (f * 1.0d);
        this.mMarkerRightInset = (int) (f * 1.0d);
        this.mMarkerTopOffset = (int) ((-50.0f) * f);
        this.mMarkerBottomOffset = (int) (50.0f * f);
        this.mTextLeftInset = (int) (f * 1.0f);
        this.mTextTopOffset = (int) ((-1.0f) * f);
        this.mTextRightInset = (int) (1.0f * f);
        this.mTextBottomOffset = (int) (f * (-40.0f));
        this.music_extract_img_3.setOnClickListener(this);
        this.musicControllerPlayButton.setOnClickListener(this);
        this.mHandler.postDelayed(this.mTimerRunnable, 100L);
        loadFromFile(this.inputMusicPath);

        findViewById(R.id.back_img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }


    @Override
    public void onPause() {
        super.onPause();
        SamplePlayer samplePlayer = this.mPlayer;
        if (samplePlayer != null) {
            samplePlayer.stop();
        }
    }

    public void startMediaPlayerInfoThread() {
        this.threadRunning = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted() && AudioCutterActivity.this.mPlayer != null && AudioCutterActivity.this.mPlayer.isPlaying()) {
                    try {
                        if (AudioCutterActivity.this.mPlayer.isPlaying()) {
                            Message message = new Message();
                            message.what = AudioCutterActivity.this.mPlayer.getCurrentPosition();
                            AudioCutterActivity.this.handlerElapsedTime.sendMessage(message);
                        }
                        Thread.sleep(300L);
                    } catch (InterruptedException unused) {
                    }
                }
                AudioCutterActivity.this.threadRunning = false;
            }
        }).start();
    }

    public String createTimeLabel(int i) {
        int i2 = i / 1000;
        int i3 = i2 / 60;
        int i4 = i2 % 60;
        String str = i3 + ":";
        if (i4 < 10) {
            str = str + "0";
        }
        return str + i4;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        ImageView imageView = this.musicControllerPlayButton;
        if (view == imageView) {
            if (!this.mIsPlaying) {
                imageView.setImageResource(R.drawable.music_pause_img);
            } else {
                imageView.setImageResource(R.drawable.music_play_img);
            }
            onPlay(this.mStartPos);
            startMediaPlayerInfoThread();
        } else if (view == this.music_extract_img_3) {
            double pixelsToSeconds = this.audioWaveform.pixelsToSeconds(this.mStartPos);
            double pixelsToSeconds2 = this.audioWaveform.pixelsToSeconds(this.mEndPos);
            if (pixelsToSeconds2 - pixelsToSeconds <= 0.0d) {
                Toast.makeText(this, "Trim seconds should be greater than 0 seconds", Toast.LENGTH_SHORT).show();
                return;
            }
            if (this.mIsPlaying) {
                handlePause();
            }
            String obj = this.etAudioOutputTitle.getText().toString();
            this.outputMusicTitle = obj;
            if (obj.isEmpty()) {
                return;
            }
            String str = this.defaultMusicPath + System.getProperty("file.separator") + this.outputMusicTitle + getExtensionByStringHandling(this.inputMusicPath);
            if (this.ffmpegConverter.musicFileExists(str)) {
                Toast.makeText(getApplicationContext(), "Music file already exists!", Toast.LENGTH_SHORT).show();
                return;
            }
            this.ffmpegConverter.appendMusicCutterOrder(this.inputMusicPath, str, this.inputMusicDuration, pixelsToSeconds, pixelsToSeconds2);

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Please Wait...");
            progressDialog.show();

            myHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (Utility.isFinished) {
                        progressDialog.dismiss();

                        long duration = getVideoDuration(Uri.parse(str));
                        String fileName = getFileNameFromPath(str);

                        Intent intent2 = new Intent(AudioCutterActivity.this, MusicPreviewActivity.class);
                        intent2.putExtra("music_path", str);
                        intent2.putExtra("music_filename", fileName);
                        intent2.putExtra("duration", duration);
                        startActivity(intent2);

                        myHandler.removeCallbacksAndMessages(null);

                        Utility.isFinished = false;
                        return;
                    }
                    myHandler.postDelayed(this, 1000);
                }
            }, 1000);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myHandler.removeCallbacksAndMessages(null);
    }

    public String getExtensionByStringHandling(String str) {
        try {
            return str.substring(str.lastIndexOf("."), str.length());
        } catch (Exception e) {
            Log.e("Converter", "exception", e);
            return "";
        }
    }

    public void finishOpeningSoundFile(SoundFile soundFile, int i) {
        this.audioWaveform.setVisibility(View.VISIBLE);
        this.audioWaveform.setSoundFile(soundFile);
        this.audioWaveform.recomputeHeights(this.mDensity);
        this.mMaxPos = this.audioWaveform.maxPos();
        this.mLastDisplayedStartPos = -1;
        this.mLastDisplayedEndPos = -1;
        this.mTouchDragging = false;
        this.mOffset = 0;
        this.mOffsetGoal = 0;
        this.mFlingVelocity = 0;
        resetPositions();
        int i2 = this.mEndPos;
        int i3 = this.mMaxPos;
        if (i2 > i3) {
            this.mEndPos = i3;
        }
        if (i == 1) {
            this.mStartPos = this.audioWaveform.secondsToPixels(0.0d);
            WaveformView waveformView = this.audioWaveform;
            this.mEndPos = waveformView.secondsToPixels(waveformView.pixelsToSeconds(this.mMaxPos));
        }
        WaveformView waveformView2 = this.audioWaveform;
        if (waveformView2 != null && waveformView2.isInitialized()) {
            this.audioWaveform.pixelsToSeconds(this.mMaxPos);
        }
        this.music_extract_img_3.setVisibility(View.VISIBLE);
        this.audioWaveform.setBackgroundColor(getResources().getColor(R.color.colorWaveformBg));
        this.markerStart.setVisibility(View.VISIBLE);
        this.markerEnd.setVisibility(View.VISIBLE);
        this.txtStartPosition.setVisibility(View.GONE);
        this.txtEndPosition.setVisibility(View.GONE);
        updateDisplay();
    }


    public synchronized void updateDisplay() {
        int i;
        int i2 = 0;
        if (this.mIsPlaying) {
            int currentPosition = this.mPlayer.getCurrentPosition();
            int millisecsToPixels = this.audioWaveform.millisecsToPixels(currentPosition);
            this.audioWaveform.setPlayback(millisecsToPixels);
            Log.e("mWidth >> ", "" + this.mWidth);
            setOffsetGoalNoUpdate(millisecsToPixels - (this.mWidth / 2));
            if (currentPosition >= this.mPlayEndMillSec) {
                handlePause();
            }
        } else if (this.audioWaveform.isInitialized()) {
            this.musicControllerPositionBar.setProgress(0);
            this.tvMusicElapsedTime.setText(createTimeLabel(0) + " / " + createTimeLabel(this.audioWaveform.pixelsToMillisecs(this.mEndPos - this.mStartPos)));
        }
        if (!this.mTouchDragging) {
            int i3 = this.mFlingVelocity;
            if (i3 != 0) {
                int i4 = i3 / 30;
                if (i3 > 80) {
                    this.mFlingVelocity = i3 - 80;
                } else if (i3 < -80) {
                    this.mFlingVelocity = i3 + 80;
                } else {
                    this.mFlingVelocity = 0;
                }
                int i5 = this.mOffset + i4;
                this.mOffset = i5;
                int i6 = this.mWidth;
                int i7 = i5 + (i6 / 2);
                int i8 = this.mMaxPos;
                if (i7 > i8) {
                    this.mOffset = i8 - (i6 / 2);
                    this.mFlingVelocity = 0;
                }
                if (this.mOffset < 0) {
                    this.mOffset = 0;
                    this.mFlingVelocity = 0;
                }
                this.mOffsetGoal = this.mOffset;
            } else {
                int i9 = this.mOffsetGoal;
                int i10 = this.mOffset;
                int i11 = i9 - i10;
                if (i11 > 10) {
                    i = i11 / 10;
                } else if (i11 > 0) {
                    i = 1;
                } else if (i11 < -10) {
                    i = i11 / 10;
                } else {
                    i = i11 < 0 ? -1 : 0;
                }
                this.mOffset = i10 + i;
            }
        }
        this.audioWaveform.setParameters(this.mStartPos, this.mEndPos, this.mOffset);
        this.audioWaveform.invalidate();
        this.markerStart.setContentDescription(" Start Marker" + formatTime(this.mStartPos));
        this.markerEnd.setContentDescription(" End Marker" + formatTime(this.mEndPos));
        int i12 = (this.mStartPos - this.mOffset) + this.mMarkerLeftInset;
        if (this.markerStart.getWidth() + i12 >= 0) {
            if (!this.mStartVisible) {
                this.mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AudioCutterActivity.this.mStartVisible = true;
                        AudioCutterActivity.this.markerStart.setAlpha(1.0f);
                        AudioCutterActivity.this.txtStartPosition.setAlpha(1.0f);
                    }
                }, 0L);
            }
        } else {
            if (this.mStartVisible) {
                this.markerStart.setAlpha(0.0f);
                this.txtStartPosition.setAlpha(0.0f);
                this.mStartVisible = false;
            }
            i12 = 0;
        }
        int i13 = (this.mStartPos - this.mOffset) - this.mTextLeftInset;
        if (this.markerStart.getWidth() + i13 < 0) {
            i13 = 0;
        }
        int width = ((this.mEndPos - this.mOffset) - this.markerEnd.getWidth()) - this.mMarkerRightInset;
        if (this.markerEnd.getWidth() + width >= 0) {
            if (!this.mEndVisible) {
                this.mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AudioCutterActivity.this.mEndVisible = true;
                        AudioCutterActivity.this.markerEnd.setAlpha(1.0f);
                    }
                }, 0L);
            }
        } else {
            if (this.mEndVisible) {
                this.markerEnd.setAlpha(0.0f);
                this.mEndVisible = false;
            }
            width = 0;
        }
        int width2 = ((this.mEndPos - this.mOffset) - this.txtEndPosition.getWidth()) + this.mTextRightInset;
        if (this.markerEnd.getWidth() + width2 >= 0) {
            i2 = width2;
        }
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-2, -2);
        layoutParams.setMargins(i12, (this.audioWaveform.getMeasuredHeight() / 2) + this.mMarkerTopOffset, -this.markerStart.getWidth(), -this.markerStart.getHeight());
        this.markerStart.setLayoutParams(layoutParams);
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(-2, -2);
        layoutParams2.setMargins(i13, this.mTextTopOffset, -this.txtStartPosition.getWidth(), -this.txtStartPosition.getHeight());
        this.txtStartPosition.setLayoutParams(layoutParams2);
        RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(-2, -2);
        layoutParams3.setMargins(width, (this.audioWaveform.getMeasuredHeight() / 2) + this.mMarkerBottomOffset, -this.markerEnd.getWidth(), -this.markerEnd.getHeight());
        this.markerEnd.setLayoutParams(layoutParams3);
        RelativeLayout.LayoutParams layoutParams4 = new RelativeLayout.LayoutParams(-2, -2);
        layoutParams4.setMargins(i2, (this.audioWaveform.getMeasuredHeight() - this.txtEndPosition.getHeight()) - this.mTextBottomOffset, -this.txtEndPosition.getWidth(), -this.txtEndPosition.getHeight());
        this.txtEndPosition.setLayoutParams(layoutParams4);
    }

    private void resetPositions() {
        this.mStartPos = this.audioWaveform.secondsToPixels(0.0d);
        this.mEndPos = this.audioWaveform.secondsToPixels(15.0d);
    }

    private void setOffsetGoalNoUpdate(int i) {
        if (this.mTouchDragging) {
            return;
        }
        this.mOffsetGoal = i;
        int i2 = this.mWidth;
        int i3 = i + (i2 / 2);
        int i4 = this.mMaxPos;
        if (i3 > i4) {
            this.mOffsetGoal = i4 - (i2 / 2);
        }
        if (this.mOffsetGoal < 0) {
            this.mOffsetGoal = 0;
        }
    }

    public String formatTime(int i) {
        WaveformView waveformView = this.audioWaveform;
        return (waveformView == null || !waveformView.isInitialized()) ? "" : formatDecimal(this.audioWaveform.pixelsToSeconds(i));
    }

    private String formatDecimal(double d) {
        int i = (int) d;
        int i2 = (int) (((d - i) * 100.0d) + 0.5d);
        if (i2 >= 100) {
            i++;
            i2 -= 100;
            if (i2 < 10) {
                i2 *= 10;
            }
        }
        if (i2 < 10) {
            if (i < 10) {
                return "0" + i + ".0" + i2;
            }
            return i + ".0" + i2;
        } else if (i < 10) {
            return "0" + i + "." + i2;
        } else {
            return i + "." + i2;
        }
    }

    private int trap(int i) {
        if (i < 0) {
            return 0;
        }
        int i2 = this.mMaxPos;
        return i > i2 ? i2 : i;
    }

    private void setOffsetGoalStart() {
        setOffsetGoal(this.mStartPos - (this.mWidth / 2));
    }

    private void setOffsetGoalStartNoUpdate() {
        setOffsetGoalNoUpdate(this.mStartPos - (this.mWidth / 2));
    }

    private void setOffsetGoalEnd() {
        setOffsetGoal(this.mEndPos - (this.mWidth / 2));
    }

    private void setOffsetGoalEndNoUpdate() {
        setOffsetGoalNoUpdate(this.mEndPos - (this.mWidth / 2));
    }

    private void setOffsetGoal(int i) {
        setOffsetGoalNoUpdate(i);
        updateDisplay();
    }

    @Override
    public void markerTouchStart(MarkerView markerView, float f) {
        this.mTouchDragging = true;
        this.mTouchStart = f;
        this.mTouchInitialStartPos = this.mStartPos;
        this.mTouchInitialEndPos = this.mEndPos;
        handlePause();
    }

    @Override
    public void markerTouchMove(MarkerView markerView, float f) {
        float f2 = f - this.mTouchStart;
        if (markerView == this.markerStart) {
            int trap = trap((int) (this.mTouchInitialStartPos + f2));
            this.mStartPos = trap;
            int i = this.mEndPos;
            if (trap >= i) {
                this.mStartPos = i;
            }
        } else {
            int trap2 = trap((int) (this.mTouchInitialEndPos + f2));
            this.mEndPos = trap2;
            int i2 = this.mStartPos;
            if (trap2 < i2) {
                this.mEndPos = i2;
            }
        }
        updateDisplay();
    }

    @Override
    public void markerTouchEnd(MarkerView markerView) {
        this.mTouchDragging = false;
        if (markerView == this.markerStart) {
            setOffsetGoalStart();
        } else {
            setOffsetGoalEnd();
        }
    }

    @Override
    public void markerLeft(MarkerView markerView, int i) {
        this.mKeyDown = true;
        if (markerView == this.markerStart) {
            int i2 = this.mStartPos;
            int trap = trap(i2 - i);
            this.mStartPos = trap;
            this.mEndPos = trap(this.mEndPos - (i2 - trap));
            setOffsetGoalStart();
        }
        if (markerView == this.markerEnd) {
            int i3 = this.mEndPos;
            int i4 = this.mStartPos;
            if (i3 == i4) {
                int trap2 = trap(i4 - i);
                this.mStartPos = trap2;
                this.mEndPos = trap2;
            } else {
                this.mEndPos = trap(i3 - i);
            }
            setOffsetGoalEnd();
        }
        updateDisplay();
    }

    @Override
    public void markerRight(MarkerView markerView, int i) {
        this.mKeyDown = true;
        if (markerView == this.markerStart) {
            int i2 = this.mStartPos;
            int i3 = i2 + i;
            this.mStartPos = i3;
            int i4 = this.mMaxPos;
            if (i3 > i4) {
                this.mStartPos = i4;
            }
            int i5 = this.mEndPos + (this.mStartPos - i2);
            this.mEndPos = i5;
            if (i5 > i4) {
                this.mEndPos = i4;
            }
            setOffsetGoalStart();
        }
        if (markerView == this.markerEnd) {
            int i6 = this.mEndPos + i;
            this.mEndPos = i6;
            int i7 = this.mMaxPos;
            if (i6 > i7) {
                this.mEndPos = i7;
            }
            setOffsetGoalEnd();
        }
        updateDisplay();
    }

    @Override
    public void markerKeyUp() {
        this.mKeyDown = false;
        updateDisplay();
    }

    @Override
    public void markerFocus(MarkerView markerView) {
        this.mKeyDown = false;
        if (markerView == this.markerStart) {
            setOffsetGoalStartNoUpdate();
        } else {
            setOffsetGoalEndNoUpdate();
        }
        this.mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                AudioCutterActivity.this.updateDisplay();
            }
        }, 100L);
    }

    @Override
    public void waveformDraw() {
        this.mWidth = this.audioWaveform.getMeasuredWidth();
        if (this.mOffsetGoal != this.mOffset && !this.mKeyDown) {
            updateDisplay();
        } else if (this.mIsPlaying) {
            updateDisplay();
        } else if (this.mFlingVelocity != 0) {
            updateDisplay();
        }
    }

    @Override
    public void waveformTouchStart(float f) {
        this.mTouchDragging = true;
        this.mTouchStart = f;
        this.mTouchInitialOffset = this.mOffset;
        this.mFlingVelocity = 0;
    }

    @Override
    public void waveformTouchMove(float f) {
        this.mOffset = trap((int) (this.mTouchInitialOffset + (this.mTouchStart - f)));
        updateDisplay();
    }


    public synchronized void handlePause() {
        this.musicControllerPlayButton.setImageResource(R.drawable.music_play_img);
        SamplePlayer samplePlayer = this.mPlayer;
        if (samplePlayer != null && samplePlayer.isPlaying()) {
            this.mPlayer.pause();
        }
        this.audioWaveform.setPlayback(-1);
        this.mIsPlaying = false;
    }

    private synchronized void onPlay(int i) {
        if (this.mIsPlaying) {
            handlePause();
        } else if (this.mPlayer == null) {
        } else {
            try {
                int pixelsToMillisecs = this.audioWaveform.pixelsToMillisecs(i);
                int i2 = this.mStartPos;
                if (i < i2) {
                    this.mPlayEndMillSec = this.audioWaveform.pixelsToMillisecs(i2);
                } else {
                    int i3 = this.mEndPos;
                    if (i > i3) {
                        this.mPlayEndMillSec = this.audioWaveform.pixelsToMillisecs(this.mMaxPos);
                    } else {
                        this.mPlayEndMillSec = this.audioWaveform.pixelsToMillisecs(i3);
                    }
                }
                this.mPlayer.setOnCompletionListener(new SamplePlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion() {
                        AudioCutterActivity.this.handlePause();
                    }
                });
                this.mIsPlaying = true;
                this.mPlayer.seekTo(pixelsToMillisecs);
                this.mPlayer.start();
                updateDisplay();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void waveformFling(float f) {
        this.mTouchDragging = false;
        this.mOffsetGoal = this.mOffset;
        this.mFlingVelocity = (int) (-f);
        updateDisplay();
    }

    @Override
    public void waveformZoomOut() {
        this.audioWaveform.zoomOut();
        this.mStartPos = this.audioWaveform.getStart();
        this.mEndPos = this.audioWaveform.getEnd();
        this.mMaxPos = this.audioWaveform.maxPos();
        int offset = this.audioWaveform.getOffset();
        this.mOffset = offset;
        this.mOffsetGoal = offset;
        updateDisplay();
    }

    private void loadFromFile(String str) {
        this.mFile = new File(str);
        this.mLoadingLastUpdateTime = Utility.getCurrentTime();
        this.mLoadingKeepGoing = true;
        ProgressDialog progressDialog = new ProgressDialog(this);
        this.mProgressDialog = progressDialog;
        progressDialog.setProgressStyle(1);
        this.mProgressDialog.setMessage("Please Wait...");
        this.mProgressDialog.show();
        final SoundFile.ProgressListener progressListener = new SoundFile.ProgressListener() {
            @Override
            public boolean reportProgress(double d) {

                long currentTime = Utility.getCurrentTime();
                if (currentTime - AudioCutterActivity.this.mLoadingLastUpdateTime > 100) {
                    mProgressDialog.setProgress((int) (AudioCutterActivity.this.mProgressDialog.getMax() * d));
                    mLoadingLastUpdateTime = currentTime;
                }
                return mLoadingKeepGoing;
            }
        };
        new Thread() {
            @Override
            public void run() {
                try {
                    AudioCutterActivity audioCutterActivity = AudioCutterActivity.this;
                    audioCutterActivity.mLoadedSoundFile = SoundFile.create(audioCutterActivity.mFile.getAbsolutePath(), progressListener);
                    if (AudioCutterActivity.this.mLoadedSoundFile == null) {
                        AudioCutterActivity.this.mProgressDialog.dismiss();
                        Log.e(" >> ", "" + (AudioCutterActivity.this.mFile.getName().toLowerCase().split("\\.").length < 2 ? "No Extension" : "Bad Extension"));
                        return;
                    }
                    AudioCutterActivity.this.mPlayer = new SamplePlayer(AudioCutterActivity.this.mLoadedSoundFile);
                    AudioCutterActivity.this.mProgressDialog.dismiss();
                    if (AudioCutterActivity.this.mLoadingKeepGoing) {
                        AudioCutterActivity.this.mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                AudioCutterActivity.this.audioWaveform.setVisibility(View.INVISIBLE);
                                AudioCutterActivity.this.audioWaveform.setBackgroundColor(AudioCutterActivity.this.getResources().getColor(R.color.waveform_unselected_bkgnd_overlay));
                                AudioCutterActivity.this.finishOpeningSoundFile(AudioCutterActivity.this.mLoadedSoundFile, 1);
                            }
                        });
                    }
                } catch (Exception e) {
                    AudioCutterActivity.this.mProgressDialog.dismiss();
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private long getVideoDuration(Uri videoUri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this, videoUri);
        String durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        try {
            retriever.release();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Convert duration to long
        return Long.parseLong(durationString);
    }

    private String getFileNameFromPath(String filePath) {
        // Use File class to extract the file name from the path
        java.io.File file = new java.io.File(filePath);
        return file.getName();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}