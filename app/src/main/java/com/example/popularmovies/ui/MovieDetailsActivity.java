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

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.popularmovies.R;
import com.example.popularmovies.database.AppDatabase;
import com.example.popularmovies.database.AppExecutors;
import com.example.popularmovies.database.Movie;
import com.example.popularmovies.database.MovieDetailsViewModel;
import com.example.popularmovies.database.MovieDetailsViewModelFactory;
import com.example.popularmovies.utils.JsonUtils;
import com.example.popularmovies.utils.NetworkUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieDetailsActivity extends AppCompatActivity
        implements ReviewsAdapter.ReviewsAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<Void>,
        TrailersAdapter.PlayTrailersHandler,
        TrailersAdapter.ShareTrailersHandler {

    private static final String SAVED_INSTANCE_MOVIE_OBJECT = "movie";
    private static final String SAVED_INSTANCE_MOVIE_TRAILERS = "trailers";
    private static final String SAVED_INSTANCE_MOVIE_REVIEWS = "reviews";
    private static final String YOUTUBE_URL = "https://www.youtube.com/watch?v=";
    private static final String MOVIE_TRAILERS_URL_SUFFIX = "/videos";
    private static final String MOVIE_REVIEWS_URL_SUFFIX = "/reviews";
    private static final String SHARING_TRAILER_TITLE = "Sharing a movie trailer";
    private static final String MIME_TYPE_SHARE_TRAILER = "text/plain";
    private static final String KEY_VIDEO_KEY = "key";
    private static final String KEY_REVIEW_CONTENT = "content";
    private static final int REVIEWS_COLUMNS_PORTRAIT = 3;
    private static final int REVIEWS_COLUMNS_LANDSCAPE = 5;
    private static final int MOVIES_DETAILS_LOADER_ID = 8;
    private boolean mMovieInFavorites, mFinishedLoading = false;
    private List<String> mMovieCategoriesList;
    private AppDatabase mDb;
    private Movie mMovie;
    private List<String> mTrailers, mReviews;
    private TrailersAdapter mTrailersAdapter;
    private ReviewsAdapter mReviewsAdapter;
    @BindView(R.id.full_details_layout) LinearLayout mFullDetailsLayout;
    @BindView(R.id.trailer_layout) LinearLayout mTrailerLayout;
    @BindView(R.id.reviews_layout) LinearLayout mReviewsLayout;
    @BindView(R.id.pb_loading_indicator_details_activity) ProgressBar mProgressBar;
    @BindView(R.id.original_title) TextView mOriginalTitleTv;
    @BindView(R.id.release_date) TextView mReleaseDateTv;
    @BindView(R.id.runtime) TextView mRuntimeTv;
    @BindView(R.id.vote_average) TextView mVoteAverageTv;
    @BindView(R.id.overview) TextView mOverviewTv;
    @BindView(R.id.mark_as_favorite) Button mFavoriteButton;
    @BindView(R.id.movie_details_image) ImageView mImageIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        ButterKnife.bind(this);

        initAttributes();

        if (savedInstanceState != null && savedInstanceState.containsKey(SAVED_INSTANCE_MOVIE_OBJECT)) {
            hideData();
            mMovie = savedInstanceState.getParcelable(SAVED_INSTANCE_MOVIE_OBJECT);
            if (savedInstanceState.containsKey(SAVED_INSTANCE_MOVIE_TRAILERS)) {
                mTrailers = savedInstanceState.getStringArrayList(SAVED_INSTANCE_MOVIE_TRAILERS);
            }
            if (savedInstanceState.containsKey(SAVED_INSTANCE_MOVIE_REVIEWS)) {
                mReviews = savedInstanceState.getStringArrayList(SAVED_INSTANCE_MOVIE_REVIEWS);
            }
            showData();
            return;
        }

        Intent intent = getIntent();
        if (intent == null) {
            closeOnError();
            return;
        }

        if (!intent.hasExtra(Intent.EXTRA_TEXT)) {
            closeOnError();
            return;
        }

        mMovie = intent.getParcelableExtra(Intent.EXTRA_TEXT);
        if (mMovie == null) {
            closeOnError();
            return;
        }

        loadMovieExtraData();
    }

    private void initAttributes() {
        mDb = AppDatabase.getInstance(getApplicationContext());
        mMovieCategoriesList = Arrays.asList(getResources().getStringArray(R.array.movie_categories_array));

        RecyclerView mTrailersRecyclerView = findViewById(R.id.rv_trailers);
        mTrailersRecyclerView.setFocusable(false);
        mTrailersRecyclerView.setHasFixedSize(true);
        mTrailersRecyclerView.addItemDecoration(new DividerItemDecorator(
                ContextCompat.getDrawable(mTrailersRecyclerView.getContext(), R.drawable.divider)));
        mTrailersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mTrailersAdapter = new TrailersAdapter(this, this);
        mTrailersRecyclerView.setAdapter(mTrailersAdapter);

        RecyclerView mReviewsRecyclerView = findViewById(R.id.rv_reviews);
        mReviewsRecyclerView.setFocusable(false);
        mReviewsRecyclerView.setHasFixedSize(true);
        int gridColumnsNumber = REVIEWS_COLUMNS_PORTRAIT; // default
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            gridColumnsNumber = REVIEWS_COLUMNS_LANDSCAPE;
        }
        mReviewsRecyclerView.setLayoutManager(new GridLayoutManager(this, gridColumnsNumber));
        mReviewsAdapter = new ReviewsAdapter(this);
        mReviewsRecyclerView.setAdapter(mReviewsAdapter);

        mFullDetailsLayout.requestFocus();
    }

    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.detail_error_message, Toast.LENGTH_SHORT).show();
    }

    private SharedPreferences getAppSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    private String getPrefSortCategory() {
        return getAppSharedPreferences().getString(getString(R.string.pref_sort_category_key),
                getString(R.string.pref_default_sort_category));
    }

    private void loadMovieExtraData() {
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> moviesDetailsLoader = loaderManager.getLoader(MOVIES_DETAILS_LOADER_ID);
        if (moviesDetailsLoader == null) {
            getSupportLoaderManager().initLoader(MOVIES_DETAILS_LOADER_ID, null, this);
        } else {
            getSupportLoaderManager().restartLoader(MOVIES_DETAILS_LOADER_ID, null, this);
        }
    }

    @NonNull
    @Override
    public Loader<Void> onCreateLoader(int id, @Nullable Bundle args) {
        return new AsyncTaskLoader<Void>(this) {

            @Override
            protected void onStartLoading() {
                if (mMovie.getRuntime() != null && mTrailers != null && mReviews != null) {
                    return;
                }
                hideData();
                forceLoad();
            }

            @Override
            public Void loadInBackground() {
                try {
                    String urlPrefix = getResources().getString(R.string.movies_query_base_url) + mMovie.getId();
                    JsonUtils.parseMovieExtraDetails(getJsonFromHttpResponse(urlPrefix), mMovie);
                    mTrailers = JsonUtils.parseResponseToList(
                            getJsonFromHttpResponse(urlPrefix + MOVIE_TRAILERS_URL_SUFFIX), KEY_VIDEO_KEY);
                    mReviews = JsonUtils.parseResponseToList(
                            getJsonFromHttpResponse(urlPrefix + MOVIE_REVIEWS_URL_SUFFIX), KEY_REVIEW_CONTENT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            private String getJsonFromHttpResponse(String urlPrefix) throws Exception {
                URL moviesRequestUrl = NetworkUtils.buildUrl(urlPrefix, getResources().getString(R.string.api_key));
                return NetworkUtils.getResponseFromHttpUrl(moviesRequestUrl);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Void> loader, Void data) {
        showData();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Void> loader) {
    }

    private void hideData() {
        mFullDetailsLayout.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void showData() {
        mFinishedLoading = true;
        mProgressBar.setVisibility(View.GONE);
        populateUI();
        mFullDetailsLayout.setVisibility(View.VISIBLE);
    }

    private void populateUI() {
        String originalTitle = mMovie.getOriginalTitle();
        if (originalTitle != null && !originalTitle.isEmpty()) {
            mOriginalTitleTv.setText(originalTitle);
        } else {
            mOriginalTitleTv.setVisibility(View.GONE);
        }

        String releaseYear = null;
        String releaseDate = mMovie.getReleaseDate();
        if (releaseDate != null && !releaseDate.isEmpty()) {
            releaseYear = releaseDate.substring(0, 4);
        }
        if (releaseYear != null && !releaseYear.isEmpty()) {
            mReleaseDateTv.setText(releaseYear);
        } else {
            mReleaseDateTv.setVisibility(View.GONE);
        }

        String runtime = mMovie.getRuntime();
        if (runtime != null && !runtime.isEmpty()) {
            String runtimeText = runtime + " " + getApplicationContext().getString(R.string.runtime_suffix);
            mRuntimeTv.setText(runtimeText);
        } else {
            mRuntimeTv.setVisibility(View.GONE);
        }

        String voteAverage = mMovie.getVoteAverage();
        if (voteAverage != null && !voteAverage.isEmpty()) {
            String averageOutOfTen = voteAverage + getApplicationContext().getString(R.string.vote_average_suffix);
            mVoteAverageTv.setText(averageOutOfTen);
        } else {
            mVoteAverageTv.setVisibility(View.GONE);
        }

        String overview = mMovie.getOverview();
        if (overview != null && !overview.isEmpty()) {
            mOverviewTv.setText(overview);
        } else {
            mOverviewTv.setVisibility(View.GONE);
        }

        if (mTrailers != null && mTrailers.size() != 0) {
            mTrailersAdapter.setTrailersData(mTrailers);
        } else {
            mTrailerLayout.setVisibility(View.GONE);
        }

        if (mReviews != null && mReviews.size() != 0) {
            mReviewsAdapter.setReviewsData(mReviews);
        } else {
            mReviewsLayout.setVisibility(View.GONE);
        }

        NetworkUtils.downloadImageIntoView(mMovie.getPosterPath(), mImageIv);

        MovieDetailsViewModelFactory factory = new MovieDetailsViewModelFactory(mDb, mMovie.getId());
        final MovieDetailsViewModel viewModel
                = ViewModelProviders.of(this, factory).get(MovieDetailsViewModel.class);
        viewModel.getMovie().observe(this, new Observer<Movie>() {
            @Override
            public void onChanged(Movie movie) {
                mMovieInFavorites = (movie != null);
                setFavoriteBackground();
            }
        });
    }

    private void setFavoriteBackground() {
        if (mMovieInFavorites) {
            mFavoriteButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.btn_star_big_on));
        } else {
            mFavoriteButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.btn_star_big_off));
        }
    }

    public void onFavoriteButtonClicked(View view) {
        if (mMovieInFavorites) {
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    mDb.movieDao().deleteMovie(mMovie);
                }
            });
        } else {
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    mDb.movieDao().insertMovie(mMovie);
                }
            });
        }
    }

    @Override
    public void onPlayViewClicked(String videoKey) {
        String youtubeUrl = YOUTUBE_URL + videoKey;
        Intent trailerIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl));
        if (trailerIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(trailerIntent);
        }
    }

    @Override
    public void onShareViewClicked(String videoKey) {
        String youtubeUrl = YOUTUBE_URL + videoKey;
        ShareCompat.IntentBuilder
                .from(this)
                .setType(MIME_TYPE_SHARE_TRAILER)
                .setChooserTitle(SHARING_TRAILER_TITLE)
                .setText(youtubeUrl)
                .startChooser();
    }

    @SuppressLint("InflateParams")
    @Override
    public void onReviewClicked(String review) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MovieDetailsActivity.this);
        LayoutInflater inflater = MovieDetailsActivity.this.getLayoutInflater();
        View reviewLayout = inflater.inflate(R.layout.review_dialog, null);
        TextView reviewView = reviewLayout.findViewById(R.id.review_content);
        reviewView.setText(review);
        builder.setView(reviewLayout)
                .setNegativeButton(R.string.review_dialog_back_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_layout, menu);
        Spinner mSpinner = (Spinner) menu.findItem(R.id.spinner).getActionView();
        SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(getBaseContext(),
                R.array.movie_categories_array, android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(mSpinnerAdapter);
        mSpinner.setSelection(mMovieCategoriesList.indexOf(getPrefSortCategory()), true);
        mSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String chosenCategory = (String) parent.getItemAtPosition(position);
                        SharedPreferences.Editor editor = getAppSharedPreferences().edit();
                        editor.putString(getString(R.string.pref_sort_category_key), chosenCategory);
                        editor.apply();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                }
        );

        return true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFinishedLoading) {
            outState.putParcelable(SAVED_INSTANCE_MOVIE_OBJECT, mMovie);
            if (mTrailers != null) {
                outState.putStringArrayList(SAVED_INSTANCE_MOVIE_TRAILERS, new ArrayList<>(mTrailers));
            }
            if (mReviews != null) {
                outState.putStringArrayList(SAVED_INSTANCE_MOVIE_REVIEWS, new ArrayList<>(mReviews));
            }
        }
    }

}
