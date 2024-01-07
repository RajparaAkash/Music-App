package com.example.musicdemo.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.musicdemo.Activity.AudioCutterActivity;
import com.example.musicdemo.Model.Music;
import com.example.musicdemo.R;

import java.util.Arrays;
import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {

    private Context context;
    private List<Music> musicList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int pos);
    }

    public MusicAdapter(List<Music> musicList, Context context, OnItemClickListener onItemClickListener) {
        this.musicList = musicList;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.itemview_music, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Music music = musicList.get(position);
        holder.titleTextView.setText(music.getTitle());
        holder.artistTextView.setText(music.getArtist());
        holder.durationTextView.setText(formatDuration(music.getDuration()));

        char firstLetter = music.getTitle().charAt(0);
        holder.iconTextView.setText(String.valueOf(firstLetter));

        if (music.isPlaying()) {
            holder.iconTextView.setVisibility(View.GONE);
            holder.musicLottie.setVisibility(View.VISIBLE);
            holder.playPauseImageView.setImageResource(R.drawable.ic_pause);
        } else {
            holder.musicLottie.setVisibility(View.GONE);
            holder.iconTextView.setVisibility(View.VISIBLE);
            holder.playPauseImageView.setImageResource(R.drawable.ic_play);
        }

        holder.playPauseImageView.setOnClickListener(v -> {
            onItemClickListener.onItemClick(position);
        });

        if (position % 7 == 0) {
            holder.iconbgRel.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.list_color1)));
        } else if (position % 7 == 1) {
            holder.iconbgRel.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.list_color2)));
        } else if (position % 7 == 2) {
            holder.iconbgRel.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.list_color3)));
        } else if (position % 7 == 3) {
            holder.iconbgRel.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.list_color4)));
        } else if (position % 7 == 4) {
            holder.iconbgRel.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.list_color5)));
        } else if (position % 7 == 5) {
            holder.iconbgRel.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.list_color6)));
        } else if (position % 7 == 6) {
            holder.iconbgRel.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.list_color7)));
        }

        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(context, AudioCutterActivity.class);
            intent.putExtra("music_path", music.getFilePath());
            intent.putExtra("music_filename", music.getTitle());
            intent.putExtra("duration", music.getDuration());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;
        TextView artistTextView;
        TextView durationTextView;
        TextView iconTextView;
        ImageView playPauseImageView;
        LottieAnimationView musicLottie;
        RelativeLayout iconbgRel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            artistTextView = itemView.findViewById(R.id.artistTextView);
            durationTextView = itemView.findViewById(R.id.durationTextView);
            iconTextView = itemView.findViewById(R.id.iconTextView);
            playPauseImageView = itemView.findViewById(R.id.playPauseImageView);
            musicLottie = itemView.findViewById(R.id.musicLottie);
            iconbgRel = itemView.findViewById(R.id.iconbgRel);
        }
    }

    private String formatDuration(long duration) {
        long minutes = duration / 60000;
        long seconds = (duration % 60000) / 1000;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
