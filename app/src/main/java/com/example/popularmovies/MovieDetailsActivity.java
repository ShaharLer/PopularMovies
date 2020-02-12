package com.example.popularmovies;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.popularmovies.database.Movie;
import com.example.popularmovies.utils.JsonUtils;
import com.example.popularmovies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MovieDetailsActivity extends AppCompatActivity
        implements ReviewsAdapter.ReviewsAdapterOnClickHandler,
        TrailersAdapter.PlayTrailersHandler,
        TrailersAdapter.ShareTrailersHandler {

    private static final String SAVED_INSTANCE_MOVIE_OBJECT = "movie";
    private static final String SAVED_INSTANCE_MOVIE_TRAILERS = "trailers";
    private static final String SAVED_INSTANCE_MOVIE_REVIEWS = "reviews";
    private static final String YOUTUBE_URL = "https://www.youtube.com/watch?v=";
    private static final String SHARING_TRAILER_TITLE = "Sharing a movie trailer";
    private static final String MIME_TYPE_SHARE_TRAILER = "text/plain";
    private static final String GET_RUNTIME = "runtime";
    private static final String GET_TRAILERS = "trailers";
    private static final String GET_REVIEWS = "reviews";
    private static final int REVIEWS_COLUMNS = 3;

    //    private AppDatabase mDb;

    private Movie mMovie;
    private boolean mMovieInFavorites = false;
    private ArrayList<String> mReviews = new ArrayList<>();
    private ArrayList<String> mTrailers = new ArrayList<>();
    private LinearLayout mFullDetailsLayout;
    private LinearLayout mTrailerLayout;
    private LinearLayout mReviewsLayout;
    private ProgressBar mProgressBar;
    private TextView mOriginalTitleTv;
    private TextView mReleaseDateTv;
    private TextView mRuntimeTv;
    private TextView mVoteAverageTv;
    private TextView mOverviewTv;
    private Button mFavoriteButton;
    private ImageView mImageIv;
    private RecyclerView mTrailersRecyclerView;
    private RecyclerView mReviewsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        initViews();

//        mDb = AppDatabase.getInstance(getApplicationContext());

        if (savedInstanceState != null &&
                savedInstanceState.containsKey(SAVED_INSTANCE_MOVIE_OBJECT) &&
                savedInstanceState.containsKey(SAVED_INSTANCE_MOVIE_TRAILERS) &&
                savedInstanceState.containsKey(SAVED_INSTANCE_MOVIE_REVIEWS)) {

            hideData();
            mMovie = savedInstanceState.getParcelable(SAVED_INSTANCE_MOVIE_OBJECT);
            mTrailers = savedInstanceState.getStringArrayList(SAVED_INSTANCE_MOVIE_TRAILERS);
            mReviews = savedInstanceState.getStringArrayList(SAVED_INSTANCE_MOVIE_REVIEWS);
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

        loadMovieExtraData(GET_RUNTIME);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVED_INSTANCE_MOVIE_OBJECT, mMovie);
        outState.putStringArrayList(SAVED_INSTANCE_MOVIE_TRAILERS, mTrailers);
        outState.putStringArrayList(SAVED_INSTANCE_MOVIE_REVIEWS, mReviews);
    }

    private void initViews() {
        mFullDetailsLayout = findViewById(R.id.full_details_layout);
        mTrailerLayout = findViewById(R.id.trailer_layout);
        mReviewsLayout = findViewById(R.id.reviews_layout);
        mProgressBar = findViewById(R.id.pb_loading_indicator_details_activity);
        mOriginalTitleTv = findViewById(R.id.original_title);
        mReleaseDateTv = findViewById(R.id.release_date);
        mRuntimeTv = findViewById(R.id.runtime);
        mVoteAverageTv = findViewById(R.id.vote_average);
        mOverviewTv = findViewById(R.id.overview);
        mFavoriteButton = findViewById(R.id.mark_as_favorite);
        mImageIv = findViewById(R.id.movie_details_image);
        mTrailersRecyclerView = findViewById(R.id.rv_trailers);
        mReviewsRecyclerView = findViewById(R.id.rv_reviews);
    }

    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.detail_error_message, Toast.LENGTH_SHORT).show();
    }

    private void loadMovieExtraData(String getCommand) {
        new FetchMovieDetailsTask().execute(getCommand);
    }

    private void hideData() {
        mFullDetailsLayout.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void showData() {
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

        Picasso.get()
                .load(mMovie.getPosterPath())
                .error(R.mipmap.ic_image_not_found_foreground)
                .into(mImageIv);

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

        NetworkUtils.downloadImageIntoView(mMovie.getPosterPath(), mImageIv);

        if (mTrailers != null && mTrailers.size() != 0) {
            RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecorator(
                    ContextCompat.getDrawable(mTrailersRecyclerView.getContext(), R.drawable.divider));
            mTrailersRecyclerView.setHasFixedSize(true);
            mTrailersRecyclerView.addItemDecoration(dividerItemDecoration);
            mTrailersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mTrailersRecyclerView.setAdapter(new TrailersAdapter(this, this, mTrailers));
        } else {
            mTrailerLayout.setVisibility(View.GONE);
        }

        if (mReviews != null && mReviews.size() != 0) {
            mReviewsRecyclerView.setHasFixedSize(true);
            mReviewsRecyclerView.setLayoutManager(new GridLayoutManager(this, REVIEWS_COLUMNS));
            mReviewsRecyclerView.setAdapter(new ReviewsAdapter(this, mReviews));
        } else {
            mReviewsLayout.setVisibility(View.GONE);
        }
    }

    public void onFavoriteButtonClicked(View view) {
        if (mMovieInFavorites) {
            mFavoriteButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.btn_star_big_off));
        } else {
            mFavoriteButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.btn_star_big_on));
        }

        mMovieInFavorites = !mMovieInFavorites;
    }


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

    public class FetchMovieDetailsTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hideData();
        }

        @Override
        protected String doInBackground(String... strings) {
            if (strings.length == 0) {
                return null;
            }

            String getCommand = strings[0];
            String apiKey = getResources().getString(R.string.api_key);
            URL moviesRequestUrl;
            String jsonMoviesResponse;

            try {
                switch (getCommand) {

                    case GET_RUNTIME:
                        moviesRequestUrl = NetworkUtils.buildParameterizedUrl(mMovie.getId(), apiKey);
                        jsonMoviesResponse = NetworkUtils.getResponseFromHttpUrl(moviesRequestUrl);
                        JsonUtils.parseMovieExtraDetails(jsonMoviesResponse, mMovie);
                        break;

                    case GET_TRAILERS:
                        moviesRequestUrl = NetworkUtils.buildVideosUrl(mMovie.getId(), apiKey);
                        jsonMoviesResponse = NetworkUtils.getResponseFromHttpUrl(moviesRequestUrl);
                        JsonUtils.parseMovieVideosNew(jsonMoviesResponse, mTrailers);
                        break;

                    case GET_REVIEWS:
                        moviesRequestUrl = NetworkUtils.buildReviewsUrl(mMovie.getId(), apiKey);
                        jsonMoviesResponse = NetworkUtils.getResponseFromHttpUrl(moviesRequestUrl);
                        JsonUtils.parseMovieReviewsNew(jsonMoviesResponse, mReviews);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return getCommand;
        }

        @Override
        protected void onPostExecute(String getCommand) {
            switch (getCommand) {

                case GET_RUNTIME:
                    loadMovieExtraData(GET_TRAILERS);
                    break;

                case GET_TRAILERS:
                    loadMovieExtraData(GET_REVIEWS);
                    break;

                default:
                    showData();
                    break;
            }
        }
    }
}
