package com.example.musicdemo.Utils.Helper;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;


public class MediaStoreObserver extends ContentObserver {
    private List<EventListener> eventListeners;

    
    public interface EventListener {
        void mediaStoreChanged(Uri uri);
    }

    public MediaStoreObserver(Handler handler) {
        super(handler);
        this.eventListeners = new ArrayList();
    }

    @Override
    public void onChange(boolean z) {
        onChange(z, null);
    }

    @Override
    public void onChange(boolean z, Uri uri) {
        notifyMediaStoreChange(uri);
    }

    private void notifyMediaStoreChange(Uri uri) {
        for (EventListener eventListener : this.eventListeners) {
            eventListener.mediaStoreChanged(uri);
        }
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListeners.add(eventListener);
    }
}
