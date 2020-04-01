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

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.popularmovies.R;
import com.example.popularmovies.database.Movie;
import com.example.popularmovies.utils.NetworkUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MovieViewHolder>  {

    private static final String STATUS_BAR_HEIGHT = "status_bar_height";
    private static final String TYPE_DIMEN = "dimen";
    private static final String ANDROID_PACKAGE = "android";

    private final MoviesAdapterOnClickHandler mClickHandler;
    private List<Movie> mMoviesData;
    private Context mContext;
    private int imageViewHeight;

    public interface MoviesAdapterOnClickHandler {
        void OnMovieClicked(Movie chosenMovie);
    }

    MoviesAdapter(MoviesAdapterOnClickHandler clickHandler, Context context) {
        mClickHandler = clickHandler;
        mContext = context;
        imageViewHeight = imageViewHeightToSet();
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.movie_item, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        String moviePosterPath = mMoviesData.get(position).getPosterPath();
        holder.image.getLayoutParams().height = imageViewHeight;
        NetworkUtils.downloadImageIntoView(moviePosterPath, holder.image);
    }

    /**
     * This method height that should be set for the poster image view, depending on the screen
     * total height, the status bar height, the action bar height and the current screen
     * orientation.
     *
     * @return The height to set for the image view.
     */
    private int imageViewHeightToSet() {
        // Get the screen total height
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;

        // Get status bar height
        int resource = mContext.getResources().getIdentifier(STATUS_BAR_HEIGHT, TYPE_DIMEN, ANDROID_PACKAGE);
        int statusBarHeight = 0;
        if (resource > 0) {
            statusBarHeight = mContext.getResources().getDimensionPixelSize(resource);
        }

        // Get the action bar height
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (mContext.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, mContext.getResources().getDisplayMetrics());
        }

        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            return (screenHeight - actionBarHeight - statusBarHeight) / 2;
        } else {
            return (screenHeight - actionBarHeight - statusBarHeight);
        }
    }

    @Override
    public int getItemCount() {
        if (mMoviesData == null) {
            return 0;
        }
        return mMoviesData.size();
    }

    void setMoviesData(List<Movie> moviesData) {
        mMoviesData = moviesData;
        notifyDataSetChanged();
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.movie_poster) ImageView image;

        MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Movie chosenMovie = mMoviesData.get(getAdapterPosition());
            mClickHandler.OnMovieClicked(chosenMovie);
        }
    }
}
