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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.example.popularmovies.R;
import com.example.popularmovies.database.MainViewModel;
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
import butterknife.BindView;
import butterknife.ButterKnife;

public class MoviesListActivity extends AppCompatActivity implements MoviesAdapter.MoviesAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<List<Movie>> {

    private static final String SAVED_INSTANCE_CHOSEN_CATEGORY = "chosen category";
    private static final String SAVED_INSTANCE_MOVIES_LIST = "movies list";
    private static final String SAVED_INSTANCE_FIRST_VISIBLE_POSITION = "first visible position";
    private static final String SORT_POPULAR = "popular";
    private static final String SORT_TOP_RATED = "top_rated";
    private static final int PORTRAIT_MOVIES_COLUMNS = 2;
    private static final int LANDSCAPE_MOVIES_COLUMNS = 4;
    private static final int MOVIES_SEARCH_LOADER_ID = 24;
    private boolean mFinishedLoading = false;
    private boolean mSpinnerChoiceTrigger = false;
    private int mAdapterFirstVisiblePosition = 0;
    private int moviesColumns;
    private String mUserCategoryChoice;
    private List<Movie> mMovies;
    private Spinner mSpinner;
    private MoviesAdapter mMoviesAdapter;
    private GridLayoutManager mLayoutManager;
    private MainViewModel viewModel;
    @BindView(R.id.error_layout) LinearLayout mErrorLayout;
    @BindView(R.id.pb_loading_indicator) ProgressBar mProgressBar;
    @BindView(R.id.rv_movies) RecyclerView mMoviesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies_list);
        ButterKnife.bind(this);

        initAttributes();

        if (savedInstanceState != null
                && savedInstanceState.containsKey(SAVED_INSTANCE_CHOSEN_CATEGORY)
                && savedInstanceState.containsKey(SAVED_INSTANCE_MOVIES_LIST)) {

            mUserCategoryChoice = savedInstanceState.getString(SAVED_INSTANCE_CHOSEN_CATEGORY);
            if (savedInstanceState.containsKey(SAVED_INSTANCE_FIRST_VISIBLE_POSITION)) {
                int firstVisibleItemPosition = savedInstanceState.getInt(SAVED_INSTANCE_FIRST_VISIBLE_POSITION);
                mAdapterFirstVisiblePosition = (firstVisibleItemPosition / moviesColumns) * moviesColumns;
            }
            mMovies = savedInstanceState.getParcelableArrayList(SAVED_INSTANCE_MOVIES_LIST);
        }

        loadMoviesData();
    }

    private void initAttributes() {
        // initialize variables
        mUserCategoryChoice = getPrefSortCategory();
        moviesColumns = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) ?
                PORTRAIT_MOVIES_COLUMNS : LANDSCAPE_MOVIES_COLUMNS;
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
        hideData();
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> moviesSearchLoader = loaderManager.getLoader(MOVIES_SEARCH_LOADER_ID);
        if (moviesSearchLoader == null) {
            getSupportLoaderManager().initLoader(MOVIES_SEARCH_LOADER_ID, null, this);
        } else {
            getSupportLoaderManager().restartLoader(MOVIES_SEARCH_LOADER_ID, null, this);
        }
    }

    private void setupViewModel() {
        hideData();
        if (viewModel == null) {
            viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
            viewModel.getMovies().observe(this, new Observer<List<Movie>>() {
                @Override
                public void onChanged(List<Movie> movies) {
                    if (getPrefSortCategory().equals(getString(R.string.favorites))) {
                        mMovies = movies;
                        showData();
                    }
                }
            });
        } else {
            mMovies = viewModel.getMovies().getValue();
            showData();
        }
    }

    @NonNull
    @Override
    public Loader<List<Movie>> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<List<Movie>>(this) {

            @Override
            protected void onStartLoading() {
                /*
                    In this method we understand whether we got here after a spinner choice, screen
                    rotation or back from the movie details activity.
                */

                String prefSortCategory = getPrefSortCategory();

                if (mMovies != null) {
                    // Means we are after screen rotation / back from details activity (or both).
                    // We now check if the user changed its spinner choice or not.
                    if (!prefSortCategory.equals(mUserCategoryChoice)) {
                        if (mSpinner == null) {
                            // Means we are after screen rotation (either back from movie details activity or not)
                            mSpinnerChoiceTrigger = true;
                        } else {
                            // Means we are  back from movie details activity
                            setSpinnerChoice(true);
                        }
                    } else if (!mFinishedLoading) {
                        // Means the user didn't change it choice but a screen rotation was made
                        if (prefSortCategory.equals(getString(R.string.favorites))) {
                            setupViewModel();
                        } else {
                            showData();
                        }
                    } else if (mLayoutManager != null) {
                        mAdapterFirstVisiblePosition = mLayoutManager.findFirstVisibleItemPosition();
                    }
                } else {
                    // Means a new movies list we be fetched
                    mAdapterFirstVisiblePosition = 0;
                    if (prefSortCategory.equals(getString(R.string.favorites))) {
                        setupViewModel();
                    } else {
                        forceLoad();
                    }
                }
            }

            @Override
            public List<Movie> loadInBackground() {
                String categoryType = getPrefSortCategory();
                if (categoryType == null || categoryType.isEmpty()) {
                    return null;
                }

                try {
                    String apiKey = getResources().getString(R.string.api_key);
                    String urlPrefix = getResources().getString(R.string.movies_query_base_url) + mapCategory(categoryType);
                    URL moviesRequestUrl = NetworkUtils.buildUrl(urlPrefix, apiKey);
                    String jsonMoviesResponse = NetworkUtils.getResponseFromHttpUrl(moviesRequestUrl);
                    return JsonUtils.parseMoviesFromJson(jsonMoviesResponse);
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
            mMoviesAdapter.setMoviesData(mMovies);
            mMoviesRecyclerView.smoothScrollToPosition(mAdapterFirstVisiblePosition);
            mMoviesRecyclerView.setVisibility(View.VISIBLE);
            mFinishedLoading = true;
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
            if (mLayoutManager != null && mLayoutManager.findFirstVisibleItemPosition() > 0) {
                outState.putInt(SAVED_INSTANCE_FIRST_VISIBLE_POSITION, mLayoutManager.findFirstVisibleItemPosition());
            }
            if (mMovies != null) {
                outState.putParcelableArrayList(SAVED_INSTANCE_MOVIES_LIST, new ArrayList<>(mMovies));
            }
        }
    }
}