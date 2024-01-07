package com.example.musicdemo.Utils.CustomAudioViews;

import android.media.AudioTrack;

import java.nio.ShortBuffer;


public class SamplePlayer {
    private AudioTrack mAudioTrack;
    private short[] mBuffer;
    private int mChannels;
    private boolean mKeepPlaying;
    private OnCompletionListener mListener;
    private int mNumSamples;
    private Thread mPlayThread;
    private int mPlaybackStart;
    private int mSampleRate;
    private ShortBuffer mSamples;

    
    public interface OnCompletionListener {
        void onCompletion();
    }

    public SamplePlayer(ShortBuffer shortBuffer, int i, int i2, int i3) {
        this.mSamples = shortBuffer;
        this.mSampleRate = i;
        this.mChannels = i2;
        this.mNumSamples = i3;
        this.mPlaybackStart = 0;
        int minBufferSize = AudioTrack.getMinBufferSize(i, i2 == 1 ? 4 : 12, 2);
        int i4 = this.mChannels;
        int i5 = this.mSampleRate;
        this.mBuffer = new short[(minBufferSize < (i4 * i5) * 2 ? (i4 * i5) * 2 : minBufferSize) / 2];
        AudioTrack audioTrack = new AudioTrack(3, this.mSampleRate, this.mChannels == 1 ? 4 : 12, 2, this.mBuffer.length * 2, 1);
        this.mAudioTrack = audioTrack;
        audioTrack.setNotificationMarkerPosition(this.mNumSamples - 1);
        this.mAudioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
            @Override
            public void onPeriodicNotification(AudioTrack audioTrack2) {
            }

            @Override
            public void onMarkerReached(AudioTrack audioTrack2) {
                SamplePlayer.this.stop();
                if (SamplePlayer.this.mListener != null) {
                    SamplePlayer.this.mListener.onCompletion();
                }
            }
        });
        this.mPlayThread = null;
        this.mKeepPlaying = true;
        this.mListener = null;
    }

    public SamplePlayer(SoundFile soundFile) {
        this(soundFile.getSamples(), soundFile.getSampleRate(), soundFile.getChannels(), soundFile.getNumSamples());
    }

    public void setOnCompletionListener(OnCompletionListener onCompletionListener) {
        this.mListener = onCompletionListener;
    }

    public boolean isPlaying() {
        return this.mAudioTrack.getPlayState() == 3;
    }

    public boolean isPaused() {
        return this.mAudioTrack.getPlayState() == 2;
    }

    public void start() {
        if (isPlaying()) {
            return;
        }
        this.mKeepPlaying = true;
        this.mAudioTrack.flush();
        this.mAudioTrack.play();
        Thread thread = new Thread() {
            @Override
            public void run() {
                SamplePlayer.this.mSamples.position(SamplePlayer.this.mPlaybackStart * SamplePlayer.this.mChannels);
                int i = SamplePlayer.this.mNumSamples * SamplePlayer.this.mChannels;
                while (SamplePlayer.this.mSamples.position() < i && SamplePlayer.this.mKeepPlaying) {
                    int position = i - SamplePlayer.this.mSamples.position();
                    if (position >= SamplePlayer.this.mBuffer.length) {
                        SamplePlayer.this.mSamples.get(SamplePlayer.this.mBuffer);
                    } else {
                        for (int i2 = position; i2 < SamplePlayer.this.mBuffer.length; i2++) {
                            SamplePlayer.this.mBuffer[i2] = 0;
                        }
                        SamplePlayer.this.mSamples.get(SamplePlayer.this.mBuffer, 0, position);
                    }
                    SamplePlayer.this.mAudioTrack.write(SamplePlayer.this.mBuffer, 0, SamplePlayer.this.mBuffer.length);
                }
            }
        };
        this.mPlayThread = thread;
        thread.start();
    }

    public void pause() {
        if (isPlaying()) {
            this.mAudioTrack.pause();
        }
    }

    public void stop() {
        if (isPlaying() || isPaused()) {
            this.mKeepPlaying = false;
            this.mAudioTrack.pause();
            this.mAudioTrack.stop();
            Thread thread = this.mPlayThread;
            if (thread != null) {
                try {
                    thread.join();
                } catch (InterruptedException unused) {
                }
                this.mPlayThread = null;
            }
            this.mAudioTrack.flush();
        }
    }

    public void release() {
        stop();
        this.mAudioTrack.release();
    }

    public void seekTo(int i) {
        boolean isPlaying = isPlaying();
        stop();
        int i2 = (int) (i * (this.mSampleRate / 1000.0d));
        this.mPlaybackStart = i2;
        int i3 = this.mNumSamples;
        if (i2 > i3) {
            this.mPlaybackStart = i3;
        }
        this.mAudioTrack.setNotificationMarkerPosition((i3 - 1) - this.mPlaybackStart);
        if (isPlaying) {
            start();
        }
    }

    public int getCurrentPosition() {
        return (int) ((this.mPlaybackStart + this.mAudioTrack.getPlaybackHeadPosition()) * (1000.0d / this.mSampleRate));
    }
}
