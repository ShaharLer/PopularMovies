package com.example.popularmovies.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.popularmovies.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TrailersAdapter extends RecyclerView.Adapter<TrailersAdapter.TrailerViewHolder> {

    private static final String TRAILER_TITLE = "Trailer";

    private PlayTrailersHandler mPlayTrailerHandler;
    private ShareTrailersHandler mShareTrailerHandler;
    private List<String> mTrailersData;

    public interface PlayTrailersHandler {
        void onPlayViewClicked(String videoKey);
    }

    public interface ShareTrailersHandler {
        void onShareViewClicked(String videoKey);
    }

    TrailersAdapter(PlayTrailersHandler playTrailerHandler, ShareTrailersHandler shareTrailersHandler) {
        mPlayTrailerHandler = playTrailerHandler;
        mShareTrailerHandler = shareTrailersHandler;
    }

    @NonNull
    @Override
    public TrailerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.trailer_item, parent, false);
        return new TrailerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrailerViewHolder holder, final int position) {
        String trailerTitle = TRAILER_TITLE + " " + (position + 1);
        holder.trailerTitleTv.setText(trailerTitle);
        holder.playTrailerIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPlayTrailerHandler != null) {
                    mPlayTrailerHandler.onPlayViewClicked(mTrailersData.get(position));
                }
            }
        });
        holder.shareTrailerIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mShareTrailerHandler != null) {
                    mShareTrailerHandler.onShareViewClicked(mTrailersData.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mTrailersData == null) {
            return 0;
        }
        return mTrailersData.size();
    }

    void setTrailersData(List<String> trailersData) {
        mTrailersData = trailersData;
        notifyDataSetChanged();
    }

    class TrailerViewHolder extends RecyclerView.ViewHolder {

        ImageButton playTrailerIb;
        ImageButton shareTrailerIb;
        TextView trailerTitleTv;

        TrailerViewHolder(@NonNull View itemView) {
            super(itemView);
            playTrailerIb = itemView.findViewById(R.id.play_trailer);
            shareTrailerIb = itemView.findViewById(R.id.share_trailer);
            trailerTitleTv = itemView.findViewById(R.id.trailer_number);
        }
    }

}
