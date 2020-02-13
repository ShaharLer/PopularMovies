package com.example.popularmovies;

import com.example.popularmovies.database.AppDatabase;
import com.example.popularmovies.database.Movie;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class MovieDetailsViewModel extends ViewModel {

    private LiveData<Movie> movie;

    public MovieDetailsViewModel(AppDatabase database, String movieId) {
        movie = database.movieDao().loadMovieById(movieId);
    }

    public LiveData<Movie> getMovie() {
        return movie;
    }
}
