package com.example.popularmovies;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.popularmovies.model.Movie;
import com.example.popularmovies.utils.JsonUtils;
import com.example.popularmovies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.net.URL;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MovieDetailsActivity extends AppCompatActivity implements ReviewsAdapter.ReviewsAdapterOnClickHandler {

    private static final String GET_RUNTIME = "runtime";
    private static final String GET_VIDEOS = "videos";
    private static final String GET_REVIEWS = "reviews";
    private static final int REVIEWS_COLUMNS = 3;

    private LinearLayout mFullDetailsLayout;
    private LinearLayout mTrailerLayout;
    private LinearLayout mReviewsLayout;
    private ProgressBar mProgressBar;
    private TextView originalTitleTv;
    private TextView releaseDateTv;
    private TextView runtimeTv;
    private TextView voteAverageTv;
    private TextView overviewTv;
    private ImageView imageIv;
    private Movie movie;
    private RecyclerView mTrailersRecyclerView;
    private RecyclerView mReviewsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        mFullDetailsLayout = findViewById(R.id.full_details_layout);
        mTrailerLayout = findViewById(R.id.trailer_layout);
        mReviewsLayout = findViewById(R.id.reviews_layout);
        mProgressBar = findViewById(R.id.pb_loading_indicator_details_activity);
        originalTitleTv = findViewById(R.id.original_title);
        releaseDateTv = findViewById(R.id.release_date);
        runtimeTv = findViewById(R.id.runtime);
        voteAverageTv = findViewById(R.id.vote_average);
        overviewTv = findViewById(R.id.overview);
        imageIv = findViewById(R.id.movie_details_image);
        mTrailersRecyclerView = findViewById(R.id.rv_trailers);
        mReviewsRecyclerView = findViewById(R.id.rv_reviews);

        Intent intent = getIntent();
        if (intent == null) {
            closeOnError();
            return;
        }

        if (!intent.hasExtra(Intent.EXTRA_TEXT)) {
            closeOnError();
            return;
        }

        movie = intent.getParcelableExtra(Intent.EXTRA_TEXT);
        if (movie == null) {
            closeOnError();
            return;
        }

        loadMovieExtraData(GET_RUNTIME);
    }

    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.detail_error_message, Toast.LENGTH_SHORT).show();
    }

    private void loadMovieExtraData(String getCommand) {
        new FetchMovieDetailsTask().execute(getCommand);
    }

    private void showData() {
        mProgressBar.setVisibility(View.GONE);
        populateUI();
        mFullDetailsLayout.setVisibility(View.VISIBLE);
    }

    private void populateUI() {
        String originalTitle = movie.getOriginalTitle();
        if (originalTitle != null && !originalTitle.isEmpty()) {
            originalTitleTv.setText(originalTitle);
        } else {
            originalTitleTv.setVisibility(View.GONE);
        }

        Picasso.get()
                .load(movie.getPosterPath())
                .error(R.mipmap.ic_image_not_found_foreground)
                .into(imageIv);

        String releaseYear = null;
        String releaseDate = movie.getReleaseDate();
        if (releaseDate != null && !releaseDate.isEmpty()) {
            releaseYear = releaseDate.substring(0,4);
        }

        if (releaseYear != null && !releaseYear.isEmpty()) {
            releaseDateTv.setText(releaseYear);
        } else {
            releaseDateTv.setVisibility(View.GONE);
        }

        String runtime = movie.getRuntime();
        if (runtime != null && !runtime.isEmpty()) {
            String runtimeText = runtime + " " + getApplicationContext().getString(R.string.runtime_suffix);
            runtimeTv.setText(runtimeText);
        } else {
            runtimeTv.setVisibility(View.GONE);
        }

        String voteAverage = movie.getVoteAverage();
        if (voteAverage != null && !voteAverage.isEmpty()) {
            String averageOutOfTen = voteAverage + getApplicationContext().getString(R.string.vote_average_suffix);
            voteAverageTv.setText(averageOutOfTen);
        } else {
            voteAverageTv.setVisibility(View.GONE);
        }

        String overview = movie.getOverview();
        if (overview != null && !overview.isEmpty()) {
            overviewTv.setText(overview);
        } else {
            overviewTv.setVisibility(View.GONE);
        }

        NetworkUtils.downloadImageIntoView(movie.getPosterPath(), imageIv);

        String[] videosKeys = movie.getVideosKeys();
        if (videosKeys != null && videosKeys.length != 0) {
            RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecorator(
                    ContextCompat.getDrawable(mTrailersRecyclerView.getContext(), R.drawable.divider));
            mTrailersRecyclerView.setHasFixedSize(true);
            mTrailersRecyclerView.addItemDecoration(dividerItemDecoration);
            mTrailersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mTrailersRecyclerView.setAdapter(new TrailersAdapter(videosKeys));
        } else {
            mTrailerLayout.setVisibility(View.GONE);
        }

        String[] reviews = movie.getReviews();
        if (reviews != null && reviews.length != 0) {
            mReviewsRecyclerView.setHasFixedSize(true);
            mReviewsRecyclerView.setLayoutManager(new GridLayoutManager(this, REVIEWS_COLUMNS));
            mReviewsRecyclerView.setAdapter(new ReviewsAdapter(this, reviews));
        } else {
            mReviewsLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(String review) {
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

    public class FetchMovieDetailsTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mFullDetailsLayout.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
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
                        moviesRequestUrl = NetworkUtils.buildParameterizedUrl(movie.getId(), apiKey);
                        jsonMoviesResponse = NetworkUtils.getResponseFromHttpUrl(moviesRequestUrl);
                        JsonUtils.parseMovieExtraDetails(jsonMoviesResponse, movie);
                        break;

                    case GET_VIDEOS:
                        moviesRequestUrl = NetworkUtils.buildVideosUrl(movie.getId(), apiKey);
                        jsonMoviesResponse = NetworkUtils.getResponseFromHttpUrl(moviesRequestUrl);
                        JsonUtils.parseMovieVideos(jsonMoviesResponse, movie);
                        break;

                    case GET_REVIEWS:
                        moviesRequestUrl = NetworkUtils.buildReviewsUrl(movie.getId(), apiKey);
                        jsonMoviesResponse = NetworkUtils.getResponseFromHttpUrl(moviesRequestUrl);
                        JsonUtils.parseMovieReviews(jsonMoviesResponse, movie);
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
                    loadMovieExtraData(GET_VIDEOS);
                    break;

                case GET_VIDEOS:
                    loadMovieExtraData(GET_REVIEWS);
                    break;

                default:
                    showData();
                    break;
            }
        }
    }
}
