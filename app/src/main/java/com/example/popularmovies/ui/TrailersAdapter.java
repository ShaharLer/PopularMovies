package com.example.popularmovies.ui;

/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.popularmovies.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

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

        @BindView(R.id.play_trailer)ImageButton playTrailerIb;
        @BindView(R.id.share_trailer)ImageButton shareTrailerIb;
        @BindView(R.id.trailer_number)TextView trailerTitleTv;

        TrailerViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
