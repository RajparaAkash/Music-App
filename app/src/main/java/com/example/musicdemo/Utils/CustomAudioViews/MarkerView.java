package com.example.musicdemo.Utils.CustomAudioViews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ImageView;


public class MarkerView extends ImageView {
    private MarkerListener mListener;
    private int mVelocity;

    
    public interface MarkerListener {
        void markerDraw();

        void markerEnter(MarkerView markerView);

        void markerFocus(MarkerView markerView);

        void markerKeyUp();

        void markerLeft(MarkerView markerView, int i);

        void markerRight(MarkerView markerView, int i);

        void markerTouchEnd(MarkerView markerView);

        void markerTouchMove(MarkerView markerView, float f);

        void markerTouchStart(MarkerView markerView, float f);
    }

    public MarkerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setFocusable(true);
        this.mVelocity = 0;
        this.mListener = null;
    }

    public void setListener(MarkerListener markerListener) {
        this.mListener = markerListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        MarkerListener markerListener;
        int action = motionEvent.getAction();
        if (action == 0) {
            requestFocus();
            MarkerListener markerListener2 = this.mListener;
            if (markerListener2 != null) {
                markerListener2.markerTouchStart(this, motionEvent.getRawX());
            }
        } else if (action == 1) {
            MarkerListener markerListener3 = this.mListener;
            if (markerListener3 != null) {
                markerListener3.markerTouchEnd(this);
            }
        } else if (action == 2 && (markerListener = this.mListener) != null) {
            markerListener.markerTouchMove(this, motionEvent.getRawX());
        }
        return true;
    }

    @Override
    protected void onFocusChanged(boolean z, int i, Rect rect) {
        MarkerListener markerListener;
        if (z && (markerListener = this.mListener) != null) {
            markerListener.markerFocus(this);
        }
        super.onFocusChanged(z, i, rect);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        MarkerListener markerListener = this.mListener;
        if (markerListener != null) {
            markerListener.markerDraw();
        }
    }

    @Override
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        int i2 = this.mVelocity + 1;
        this.mVelocity = i2;
        int sqrt = (int) Math.sqrt((i2 / 2) + 1);
        MarkerListener markerListener = this.mListener;
        if (markerListener != null) {
            if (i == 21) {
                markerListener.markerLeft(this, sqrt);
                return true;
            } else if (i == 22) {
                markerListener.markerRight(this, sqrt);
                return true;
            } else if (i == 23) {
                markerListener.markerEnter(this);
                return true;
            }
        }
        return super.onKeyDown(i, keyEvent);
    }

    @Override
    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        this.mVelocity = 0;
        MarkerListener markerListener = this.mListener;
        if (markerListener != null) {
            markerListener.markerKeyUp();
        }
        return super.onKeyDown(i, keyEvent);
    }
}
