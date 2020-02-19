package com.example.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MoviesListActivity extends AppCompatActivity implements MoviesAdapter.MoviesAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<List<Movie>> {

    private static final String SAVED_INSTANCE_CHOSEN_CATEGORY = "chosen category";
    private static final String SAVED_INSTANCE_MOVIES_LIST = "movies list";
//    private static final String SAVED_INSTANCE_FIRST_VISIBLE_POSITION = "first visible position";
    private static final String SORT_POPULAR = "popular";
    private static final String SORT_TOP_RATED = "top_rated";
    private static final String SEARCH_MOVIES_CATEGORY = "category";
    private static final int PORTRAIT_MOVIES_COLUMNS = 2;
    private static final int LANDSCAPE_MOVIES_COLUMNS = 4;
    private static final int MOVIES_SEARCH_LOADER_ID = 24;
//    private int mAdapterFirstVisiblePosition = 0;
    private int moviesColumns = PORTRAIT_MOVIES_COLUMNS; // default
    private boolean mFinishedLoading = false;
    private boolean mSpinnerChoiceTrigger = false;
    private String mUserCategoryChoice;
    private List<Movie> mMovies;
    private Spinner mSpinner;
    private MoviesAdapter mMoviesAdapter;
    private ProgressBar mProgressBar;
    private LinearLayout mErrorLayout;
    private GridLayoutManager mLayoutManager;
    private RecyclerView mMoviesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies_list);
        Log.d("TEST", "Starting onCreate");

        initAttributes();
        if (savedInstanceState != null
                && savedInstanceState.containsKey(SAVED_INSTANCE_CHOSEN_CATEGORY)
//                && savedInstanceState.containsKey(SAVED_INSTANCE_FIRST_VISIBLE_POSITION)
                && savedInstanceState.containsKey(SAVED_INSTANCE_MOVIES_LIST)) {

            Log.d("TEST", "savedInstanceState != null");

            mUserCategoryChoice = savedInstanceState.getString(SAVED_INSTANCE_CHOSEN_CATEGORY);
//            int firstVisibleItemPosition = savedInstanceState.getInt(SAVED_INSTANCE_FIRST_VISIBLE_POSITION);
//            mAdapterFirstVisiblePosition = (firstVisibleItemPosition / moviesColumns) * moviesColumns;

//            if (getPrefSortCategory().equals(getString(R.string.favorites))) {
//                hideData();
//                setupViewModel();
//                return;
//            } else {
//            if (!getPrefSortCategory().equals(getString(R.string.favorites))) {
                mMovies = savedInstanceState.getParcelableArrayList(SAVED_INSTANCE_MOVIES_LIST);
//            }
//            }
        }


        loadMoviesData();
    }

    private void initAttributes() {
        mUserCategoryChoice = getPrefSortCategory();
        mErrorLayout = findViewById(R.id.error_layout);
        mProgressBar = findViewById(R.id.pb_loading_indicator);
        mMoviesRecyclerView = findViewById(R.id.rv_movies);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            moviesColumns = LANDSCAPE_MOVIES_COLUMNS;
        }
        mMoviesRecyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(this, moviesColumns);
        mMoviesRecyclerView.setLayoutManager(mLayoutManager);
        mMoviesAdapter = new MoviesAdapter(this, this);
        mMoviesRecyclerView.setAdapter(mMoviesAdapter);
    }

    private SharedPreferences getAppSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    private String getPrefSortCategory() {
        return getAppSharedPreferences().getString(getString(R.string.pref_sort_category_key),
                getString(R.string.pref_default_sort_category));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_layout, menu);
        mSpinner = (Spinner) menu.findItem(R.id.spinner).getActionView();
        SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(getBaseContext(),
                R.array.movie_categories_array, android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(mSpinnerAdapter);
        setSpinnerChoice(mSpinnerChoiceTrigger);
        mSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String chosenCategory = (String) parent.getItemAtPosition(position);
                        mUserCategoryChoice = chosenCategory;
                        SharedPreferences.Editor editor = getAppSharedPreferences().edit();
                        editor.putString(getString(R.string.pref_sort_category_key), chosenCategory);
                        editor.apply();
                        mFinishedLoading = false;
                        mMovies = null;
                        loadMoviesData();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                }
        );

        return true;
    }

    private void setSpinnerChoice(boolean triggerListener) {
        int chosenCategoryIndex = Arrays.asList(getResources().getStringArray(R.array.movie_categories_array))
                .indexOf(getPrefSortCategory());

        if (triggerListener) {
            mSpinner.setSelection(chosenCategoryIndex);
        } else {
            mSpinner.setSelection(chosenCategoryIndex, false);
        }
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

    /**
     * This method will execute the method to fetch the movies list according to the given category.
     */
    private void loadMoviesData() {
        /*
        hideData();
        String prefSortCategory = getPrefSortCategory();
        if (prefSortCategory.equals(getString(R.string.favorites))) {

         */
//            mAdapterFirstVisiblePosition = 0;
//            setupViewModel();
//        } else {
//            Bundle searchBundle = new Bundle();
//            searchBundle.putString(SEARCH_MOVIES_CATEGORY, getPrefSortCategory());

            LoaderManager loaderManager = getSupportLoaderManager();
            Loader<String> moviesSearchLoader = loaderManager.getLoader(MOVIES_SEARCH_LOADER_ID);
            if (moviesSearchLoader == null) {
                Log.d("TEST", "Calling initLoader inside: loadMoviesData");
//                getSupportLoaderManager().initLoader(MOVIES_SEARCH_LOADER_ID, searchBundle, this);
                getSupportLoaderManager().initLoader(MOVIES_SEARCH_LOADER_ID, null, this);
            } else {
                Log.d("TEST", "Calling restartLoader inside: loadMoviesData");
//                getSupportLoaderManager().restartLoader(MOVIES_SEARCH_LOADER_ID, searchBundle, this);
                getSupportLoaderManager().restartLoader(MOVIES_SEARCH_LOADER_ID, null, this);
            }
//        }
    }

    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getMovies().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(List<Movie> movies) {
                if (getPrefSortCategory().equals(getString(R.string.favorites))) {
                    Log.d("TEST", "Inside setupViewModel:onChanged");
                    mMovies = movies;
                    showData();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @NonNull
    @Override
    public Loader<List<Movie>> onCreateLoader(int id, final Bundle args) {
        Log.d("TEST", "Inside onCreateLoader");
        return new AsyncTaskLoader<List<Movie>>(this) {

            @Override
            protected void onStartLoading() {
                String prefSortCategory = getPrefSortCategory();

                if (prefSortCategory.equals(getString(R.string.favorites))) {
                    // favorites

                    if (mMovies != null) {
                        if (!prefSortCategory.equals(mUserCategoryChoice)) {
                            if (mSpinner == null) {
                                mSpinnerChoiceTrigger = true;
                            } else {
                                setSpinnerChoice(false);
                            }
                        } else if (!mFinishedLoading) {
                            hideData();
                            setupViewModel();
                        }
                        return;
                    }

                    hideData();
                    setupViewModel();
                } else {
                    // Most popular / Top rated

                    if (mMovies != null) {
                        if (!prefSortCategory.equals(mUserCategoryChoice)) {
                            if (mSpinner == null) {
                                mSpinnerChoiceTrigger = true;
                            } else {
                                setSpinnerChoice(true);
                            }
                        } else if (!mFinishedLoading) {
                            showData();
                        }
                        return;
                    }

                    hideData();
                    forceLoad();
                }

                /*
                if (mMovies != null) {
                    if (!prefSortCategory.equals(mUserCategoryChoice)) {
                        mSpinnerChoiceTrigger = true;
                        if (mSpinner != null) {
                            setSpinnerChoice(true);
                        }
                        return;
                    }

                    if (mSpinner == null) {
                        showData();
                    }
                } else if (args != null) {
                    Log.d("TEST", "calling forceLoad()");
                    forceLoad();
                } else {
                    Log.d("TEST", "args == null");
                    deliverResult(null);
                }
                */

            }

            @Override
            public List<Movie> loadInBackground() {
                Log.d("TEST", "loadInBackground");
//                String categoryType = args.getString(SEARCH_MOVIES_CATEGORY);
                String categoryType = getPrefSortCategory();
                if (categoryType == null || categoryType.isEmpty()) {
                    return null;
                }

                try {
                    String apiKey = getResources().getString(R.string.api_key);
                    String urlPrefix = getResources().getString(R.string.movies_query_base_url) + mapCategory(categoryType);
                    URL moviesRequestUrl = NetworkUtils.buildUrl(urlPrefix, apiKey);
                    String jsonMoviesResponse = NetworkUtils.getResponseFromHttpUrl(moviesRequestUrl);
                    mMovies = JsonUtils.parseMoviesFromJson(jsonMoviesResponse);
                    return mMovies;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            private String mapCategory(String category) {
                if (category.equals(getString(R.string.popular))) {
                    return SORT_POPULAR;
                } else if (category.equals(getString(R.string.top_rated))) {
                    return SORT_TOP_RATED;
                } else {
                    return category;
                }
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Movie>> loader, List<Movie> data) {
        Log.d("TEST", "onLoadFinished");
//        mAdapterFirstVisiblePosition = 0;
        mMovies = data;
        showData();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Movie>> loader) {
    }

    /**
     * This method will hide the data views and show the progress bar.
     */
    private void hideData() {
        mMoviesRecyclerView.setVisibility(View.INVISIBLE);
        mErrorLayout.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * This method will show either the movie list or the error message visible and hide the
     * progress bar.
     */
    private void showData() {
        if (mMovies != null) {
            mFinishedLoading = true;
            mMoviesAdapter.setMoviesData(mMovies);
            mMoviesRecyclerView.smoothScrollToPosition(0 /*mAdapterFirstVisiblePosition*/);
            mMoviesRecyclerView.setVisibility(View.VISIBLE);
        } else {
            showErrorMessage();
        }
        mProgressBar.setVisibility(View.GONE);
    }

    /**
     * This method will make the error message visible and hide the movies View.
     */
    private void showErrorMessage() {
        mMoviesRecyclerView.setVisibility(View.GONE);
        mErrorLayout.setVisibility(View.VISIBLE);
    }

    /**
     * This method is called when a movie item was clicked in the movies list.
     */
    @Override
    public void OnMovieClicked(Movie chosenMovie) {
        Context context = this;
        Class destinationClass = MovieDetailsActivity.class;
        Intent intent = new Intent(context, destinationClass);
        intent.putExtra(Intent.EXTRA_TEXT, chosenMovie);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFinishedLoading) {
            outState.putString(SAVED_INSTANCE_CHOSEN_CATEGORY, getPrefSortCategory());
//            outState.putInt(SAVED_INSTANCE_FIRST_VISIBLE_POSITION, mLayoutManager.findFirstVisibleItemPosition());
            outState.putParcelableArrayList(SAVED_INSTANCE_MOVIES_LIST, new ArrayList<>(mMovies));
        }
    }
}