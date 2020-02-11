package com.example.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.popularmovies.model.Movie;
import com.example.popularmovies.utils.NetworkUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MovieViewHolder>  {

    private static final String STATUS_BAR_HEIGHT = "status_bar_height";
    private static final String TYPE_DIMEN = "dimen";
    private static final String ANDROID_PACKAGE = "android";

    private final MoviesAdapterOnClickHandler mClickHandler;
    private Movie[] mMoviesData;
    private Context mContext;
    private int imageViewHeight;

    public interface MoviesAdapterOnClickHandler {
        void onClick(Movie chosenMovie);
    }

    public MoviesAdapter(MoviesAdapterOnClickHandler clickHandler, Context context) {
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
        String moviePosterPath = mMoviesData[position].getPosterPath();
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
        if (mMoviesData == null) return 0;
        return mMoviesData.length;
    }

    public void setMoviesData(Movie[] moviesData) {
        mMoviesData = moviesData;
        notifyDataSetChanged();
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView image;

        MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.movie_poster);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            Movie chosenMovie = mMoviesData[adapterPosition];
            mClickHandler.onClick(chosenMovie);
        }
    }
}
