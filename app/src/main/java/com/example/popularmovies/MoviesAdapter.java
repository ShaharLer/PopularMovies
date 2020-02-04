package com.example.popularmovies;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.popularmovies.model.Movie;
import com.example.popularmovies.utils.NetworkUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MovieViewHolder>  {

    private Movie[] mMoviesData;
    private final MoviesAdapterOnClickHandler mClickHandler;

    public interface MoviesAdapterOnClickHandler {
        void onClick(Movie chosenMovie);
    }

    public MoviesAdapter(MoviesAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
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
        NetworkUtils.downloadImageINtoView(moviePosterPath, holder.image);
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
