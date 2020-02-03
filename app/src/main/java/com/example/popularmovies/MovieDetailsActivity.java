package com.example.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.popularmovies.model.Movie;
import com.example.popularmovies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import androidx.appcompat.app.AppCompatActivity;

public class MovieDetailsActivity extends AppCompatActivity {

    private TextView originalTitleTv;
    private TextView releaseDateTv;
    private TextView voteAverageTv;
    private TextView overviewTv;
    private ImageView imageIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        originalTitleTv = findViewById(R.id.original_title);
        releaseDateTv = findViewById(R.id.release_date);
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

        Movie movie = intent.getParcelableExtra(Intent.EXTRA_TEXT);
        populateUI(movie);
        NetworkUtils.downloadImageINtoView(movie.getPosterPath(), imageIv);
    }

    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.detail_error_message, Toast.LENGTH_SHORT).show();
    }

    private void populateUI(Movie movie) {
        if (movie == null) {
            closeOnError();
            return;
        }

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
    }
}
