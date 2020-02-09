package com.example.popularmovies;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

import androidx.appcompat.app.AppCompatActivity;

public class MovieDetailsActivity extends AppCompatActivity {

    private LinearLayout mFullDetailsLayout;
    private ProgressBar mProgressBar;
    private TextView originalTitleTv;
    private TextView releaseDateTv;
    private TextView runtimeTv;
    private TextView voteAverageTv;
    private TextView overviewTv;
    private ImageView imageIv;
    private Movie movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        mFullDetailsLayout = findViewById(R.id.full_details_layout);
        mProgressBar = findViewById(R.id.pb_loading_indicator_details_activity);
        originalTitleTv = findViewById(R.id.original_title);
        releaseDateTv = findViewById(R.id.release_date);
        runtimeTv = findViewById(R.id.runtime);
        voteAverageTv = findViewById(R.id.vote_average);
        overviewTv = findViewById(R.id.overview);
        imageIv = findViewById(R.id.movie_details_image);

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

        loadMovieExtraData();
    }

    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.detail_error_message, Toast.LENGTH_SHORT).show();
    }

    private void loadMovieExtraData() {
        new FetchMovieDetailsTask().execute();
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
    }

    public class FetchMovieDetailsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mFullDetailsLayout.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String apiKey = getResources().getString(R.string.api_key);
            URL moviesRequestUrl = NetworkUtils.buildParameterizedUrl(movie.getId(), apiKey);

            try {
                String jsonMoviesResponse = NetworkUtils.getResponseFromHttpUrl(moviesRequestUrl);
                JsonUtils.parseMovieExtraDetails(jsonMoviesResponse, movie);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgressBar.setVisibility(View.GONE);
            populateUI();
            mFullDetailsLayout.setVisibility(View.VISIBLE);
        }
    }
}
