package com.example.popularmovies;

import com.example.popularmovies.database.AppDatabase;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class MovieDetailsViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase mDb;
    private final String mMovieId;

    public MovieDetailsViewModelFactory(AppDatabase database, String movieId) {
        mDb = database;
        mMovieId = movieId;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new MovieDetailsViewModel(mDb, mMovieId);
    }
}
