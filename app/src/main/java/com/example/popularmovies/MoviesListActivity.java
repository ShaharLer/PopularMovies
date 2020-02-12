package com.example.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.example.popularmovies.database.AppDatabase;
import com.example.popularmovies.database.Movie;
import com.example.popularmovies.utils.JsonUtils;
import com.example.popularmovies.utils.NetworkUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MoviesListActivity extends AppCompatActivity implements MoviesAdapter.MoviesAdapterOnClickHandler {

    private static final String SAVED_INSTANCE_MOVIES_LIST = "movies list";
    private static final String SAVED_INSTANCE_SORT_CHOICE = "sort choice";
    private static final String SORT_POPULAR = "popular";
    private static final String SORT_TOP_RATED = "top_rated";
    private static final int PORTRAIT_MOVIES_COLUMNS = 2;
    private static final int LANDSCAPE_MOVIES_COLUMNS = 4;

    private static String spinnerPopular;
    private static String spinnerTopRated;
    private static String spinnerFavorites;
    private static String spinnerChosenOption;
    private AppDatabase mDb;
    private MoviesAdapter mMoviesAdapter;
    private ProgressBar mProgressBar;
    private LinearLayout mErrorLayout;
    private Spinner mSpinner;
    private RecyclerView mMoviesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies_list);
        initViews();

        if (savedInstanceState != null &&
                savedInstanceState.containsKey(SAVED_INSTANCE_SORT_CHOICE) &&
                savedInstanceState.containsKey(SAVED_INSTANCE_MOVIES_LIST)) {

            hideData();
            spinnerChosenOption = savedInstanceState.getString(SAVED_INSTANCE_SORT_CHOICE);
            List<Movie> movies = savedInstanceState.getParcelableArrayList(SAVED_INSTANCE_MOVIES_LIST);
            showData(movies);
            return;
        }

        loadMoviesData(SORT_POPULAR);
    }

    // TODO move network calls here
    @Override
    protected void onResume() {
        super.onResume();
        if (spinnerChosenOption.equals(spinnerFavorites)) {
            retrieveFavoriteMovies();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_INSTANCE_SORT_CHOICE, spinnerChosenOption);
        outState.putParcelableArrayList(SAVED_INSTANCE_MOVIES_LIST, new ArrayList<>(mMoviesAdapter.getMoviesData()));
    }

    private void initViews() {
        mDb = AppDatabase.getInstance(getApplicationContext());
        spinnerPopular = getString(R.string.popular);
        spinnerChosenOption = spinnerPopular; // default
        spinnerTopRated = getString(R.string.top_rated);
        spinnerFavorites = getString(R.string.favorites);
        mErrorLayout = findViewById(R.id.error_layout);
        mProgressBar = findViewById(R.id.pb_loading_indicator);
        mMoviesRecyclerView = findViewById(R.id.rv_movies);

        mMoviesRecyclerView.setHasFixedSize(true);

        int gridColumnsNumber = PORTRAIT_MOVIES_COLUMNS; // default
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            gridColumnsNumber = LANDSCAPE_MOVIES_COLUMNS;
        }
        mMoviesRecyclerView.setLayoutManager(new GridLayoutManager(this, gridColumnsNumber));

        mMoviesAdapter = new MoviesAdapter(this, this);
        mMoviesRecyclerView.setAdapter(mMoviesAdapter);
    }

    private void hideData() {
        mMoviesRecyclerView.setVisibility(View.INVISIBLE);
        mMoviesRecyclerView.smoothScrollToPosition(0);
        mErrorLayout.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void showData(List<Movie> movies) {
        if (movies != null) {
            mMoviesAdapter.setMoviesData(movies);
            mMoviesRecyclerView.setVisibility(View.VISIBLE);
        } else {
            showErrorMessage();
        }
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onClick(Movie chosenMovie) {
        Context context = this;
        Class destinationClass = MovieDetailsActivity.class;
        Intent intent = new Intent(context, destinationClass);
        intent.putExtra(Intent.EXTRA_TEXT, chosenMovie);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_layout, menu);
        mSpinner = (Spinner) menu.findItem(R.id.spinner).getActionView();
        SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(getBaseContext(),
                R.array.sort_categories_array, android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(mSpinnerAdapter);
        mSpinner.setSelection(
                Arrays.asList(getResources().getStringArray(R.array.sort_categories_array)).indexOf(spinnerChosenOption), false);
        mSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String category = (String) parent.getItemAtPosition(position);

                        if (category.equals(spinnerPopular)) {
                            loadMoviesData(SORT_POPULAR);
                            spinnerChosenOption = spinnerPopular;
                        } else if (category.equals(spinnerTopRated)) {
                            loadMoviesData(SORT_TOP_RATED);
                            spinnerChosenOption = spinnerTopRated;
                        } else if (category.equals(spinnerFavorites)) {
                            retrieveFavoriteMovies();
                            spinnerChosenOption = spinnerFavorites;
                        } else {
                            showErrorMessage();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                }
        );

        return true;
    }

    private void retrieveFavoriteMovies() {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final List<Movie> favoriteMovies = mDb.movieDao().loadAllMovies();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showData(favoriteMovies);
                    }
                });
            }
        });
    }

    /**
     * This method will execute the method to fetch the movies list according to the given category.
     *
     * @param sortCategory The category that will decide the sort order for the movies list.
     */
    private void loadMoviesData(String sortCategory) {
        hideData();
        new FetchMoviesListTask().execute(sortCategory);
    }

    /**
     * This method will make the error message visible and hide the movies View.
     */
    private void showErrorMessage() {
        mMoviesRecyclerView.setVisibility(View.GONE);
        mErrorLayout.setVisibility(View.VISIBLE);
    }

    /**
     * The method makes the app execute again the loadMoviesData function when the user clicks it,
     * after an error occurs.
     *
     * @param view The view of the REFRESH button.
     */
    public void refreshData(View view) {
        loadMoviesData(mSpinner.getSelectedItem().toString());
    }

    public class FetchMoviesListTask extends AsyncTask<String, Void, List<Movie>> {

        @Override
        protected List<Movie> doInBackground(String... strings) {
            if (strings.length == 0) {
                return null;
            }

            String sortCriteria = strings[0];
            String apiKey = getResources().getString(R.string.api_key);
            URL moviesRequestUrl = NetworkUtils.buildParameterizedUrl(sortCriteria, apiKey);

            try {
                String jsonMoviesResponse = NetworkUtils.getResponseFromHttpUrl(moviesRequestUrl);
                List<Movie> movies = JsonUtils.parseMoviesFromJson(jsonMoviesResponse);
                return movies;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            showData(movies);
        }
    }
}