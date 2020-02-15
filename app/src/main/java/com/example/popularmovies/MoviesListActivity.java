package com.example.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.example.popularmovies.database.Movie;
import com.example.popularmovies.utils.JsonUtils;
import com.example.popularmovies.utils.NetworkUtils;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MoviesListActivity extends AppCompatActivity implements MoviesAdapter.MoviesAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<List<Movie>>{

    private static final String SAVED_INSTANCE_SORT_CHOICE = "sort choice";
    private static final String SORT_POPULAR = "popular";
    private static final String SORT_TOP_RATED = "top_rated";
    private static final String SEARCH_MOVIES_CATEGORY = "category";
    private static final int PORTRAIT_MOVIES_COLUMNS = 2;
    private static final int LANDSCAPE_MOVIES_COLUMNS = 4;
    private static final int MOVIES_SEARCH_LOADER_ID = 24;

    private static String spinnerPopular;
    private static String spinnerTopRated;
    private static String spinnerFavorites;
    private static String spinnerChosenOption;

    private MoviesAdapter mMoviesAdapter;
    private ProgressBar mProgressBar;
    private LinearLayout mErrorLayout;
    private RecyclerView mMoviesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("TEST", "Starting onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies_list);
        initViews();

        if (savedInstanceState != null && savedInstanceState.containsKey(SAVED_INSTANCE_SORT_CHOICE)) {
            spinnerChosenOption = savedInstanceState.getString(SAVED_INSTANCE_SORT_CHOICE);
        }

        Log.d("TEST", "Calling initLoader inside: onCreate");
        getSupportLoaderManager().initLoader(MOVIES_SEARCH_LOADER_ID, null, this);
        loadMoviesData();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_INSTANCE_SORT_CHOICE, spinnerChosenOption);
    }

    private void initViews() {
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
        mErrorLayout.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void showData(List<Movie> movies) {
        Log.d("TEST", "Inside showData");
        if (movies != null) {
            mMoviesAdapter.setMoviesData(movies);
            mMoviesRecyclerView.smoothScrollToPosition(0);
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
        Spinner mSpinner = (Spinner) menu.findItem(R.id.spinner).getActionView();
        SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(getBaseContext(),
                R.array.sort_categories_array, android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(mSpinnerAdapter);
        mSpinner.setSelection(
                Arrays.asList(getResources().getStringArray(R.array.sort_categories_array))
                        .indexOf(spinnerChosenOption), false);
        mSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String category = (String) parent.getItemAtPosition(position);

                        if (category.equals(spinnerPopular)) {
                            spinnerChosenOption = spinnerPopular;
                        } else if (category.equals(spinnerTopRated)) {
                            spinnerChosenOption = spinnerTopRated;
                        } else if (category.equals(spinnerFavorites)) {
                            spinnerChosenOption = spinnerFavorites;
                        } else {
                            showErrorMessage();
                            return;
                        }

                        loadMoviesData();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                }
        );

        return true;
    }

    /**
     * This method will execute the method to fetch the movies list according to the given category.
     */
    private void loadMoviesData() {
        if (spinnerChosenOption.equals(spinnerFavorites)) {
            setupViewModel();
        } else {
            Bundle searchBundle = new Bundle();
            searchBundle.putString(SEARCH_MOVIES_CATEGORY, spinnerChosenOption);
            LoaderManager loaderManager = getSupportLoaderManager();
            Loader<String> moviesSearchLoader = loaderManager.getLoader(MOVIES_SEARCH_LOADER_ID);
            if (moviesSearchLoader == null) {
                Log.d("TEST", "Calling initLoader inside: loadMoviesData");
                getSupportLoaderManager().initLoader(MOVIES_SEARCH_LOADER_ID, null, this);
            } else {
                Log.d("TEST", "Calling restartLoader inside: loadMoviesData");
                getSupportLoaderManager().restartLoader(MOVIES_SEARCH_LOADER_ID, searchBundle, this);
            }
        }
    }

    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getMovies().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(List<Movie> movies) {
                if (spinnerChosenOption.equals(spinnerFavorites)) {
                    Log.d("TEST", "Inside onChanged");
                    showData(movies);
                }
            }
        });
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
        loadMoviesData();
    }

    private String mapCategory(String category) {
        if (category.equals(spinnerPopular)) {
            return SORT_POPULAR;
        } else if (category.equals(spinnerTopRated)) {
            return SORT_TOP_RATED;
        } else {
            return category;
        }
    }

    @NonNull
    @Override
    public Loader<List<Movie>> onCreateLoader(int id, final Bundle args) {
        Log.d("TEST", "Inside onCreateLoader");
        return new AsyncTaskLoader<List<Movie>>(this) {

            private List<Movie> movies;

            @Override
            protected void onStartLoading() {
                Log.d("TEST", "Inside onStartLoading");
                if (args == null) {
                    return;
                }
                if (movies != null) {
                    Log.d("TEST", "movies != null -> calling deliverResult(movies)");
                    deliverResult(movies);
                } else {
                    hideData();
                    Log.d("TEST", "calling forceLoad()");
                    forceLoad();
                }
            }

            @Override
            public List<Movie> loadInBackground() {
                Log.d("TEST", "Inside loadInBackground");
                String categoryType = args.getString(SEARCH_MOVIES_CATEGORY);
                if (categoryType == null || categoryType.isEmpty()) {
                    return null;
                }

                try {
                    String apiKey = getResources().getString(R.string.api_key);
                    URL moviesRequestUrl = NetworkUtils.buildParameterizedUrl(mapCategory(categoryType), apiKey);
                    String jsonMoviesResponse = NetworkUtils.getResponseFromHttpUrl(moviesRequestUrl);
                    movies = JsonUtils.parseMoviesFromJson(jsonMoviesResponse);
                    Log.d("TEST", "Returning movies");
                    return movies;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(@Nullable List<Movie> movies) {
                Log.d("TEST", "Inside deliverResult");
                super.deliverResult(movies);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Movie>> loader, List<Movie> data) {
        Log.d("TEST", "Inside onLoadFinished");
        if (data != mMoviesAdapter.getMoviesData() && !spinnerChosenOption.equals(spinnerFavorites)) {
            showData(data);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Movie>> loader) {
        Log.d("TEST", "Inside onLoaderReset");
    }

    public class FetchMoviesListTask extends AsyncTask<String, Void, List<Movie>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hideData();
        }

        @Override
        protected List<Movie> doInBackground(String... strings) {
            if (strings.length == 0) {
                return null;
            }

            String categoryType = strings[0];
            String apiKey = getResources().getString(R.string.api_key);
            URL moviesRequestUrl = NetworkUtils.buildParameterizedUrl(mapCategory(categoryType), apiKey);

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