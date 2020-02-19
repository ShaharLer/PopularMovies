package com.example.popularmovies;

import android.app.Application;
import android.util.Log;

import com.example.popularmovies.database.AppDatabase;
import com.example.popularmovies.database.Movie;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class MainViewModel extends AndroidViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();
    private LiveData<List<Movie>> movies;

    public MainViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        movies = database.movieDao().loadAllMovies();
        Log.d("TEST", "loaded movies from database");
    }

    public LiveData<List<Movie>> getMovies() {
        return movies;
    }
}
