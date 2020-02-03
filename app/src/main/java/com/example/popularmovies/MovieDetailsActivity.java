package com.example.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.popularmovies.model.Movie;
import com.squareup.picasso.Picasso;

import androidx.appcompat.app.AppCompatActivity;

public class MovieDetailsActivity extends AppCompatActivity {

    private TextView originalTitle;
    private RelativeLayout imageAndDetailsLayout;
    private LinearLayout detailsLayout;
    private TextView releaseDateTv;
    private TextView voteAverageTv;
    private TextView overviewTv;
    private ImageView imageIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        // Getting the IDs of the layouts
        originalTitle = findViewById(R.id.original_title);
        imageAndDetailsLayout = findViewById(R.id.image_and_details_layout);
        detailsLayout = findViewById(R.id.details_layout);

        // Getting the IDs of the Text/Image Views
        releaseDateTv = findViewById(R.id.release_date);
        voteAverageTv = findViewById(R.id.vote_average);
        overviewTv = findViewById(R.id.overview);
        imageIv = findViewById(R.id.movie_details_image);

        Intent intent = getIntent();
        if (intent == null) {
            closeOnError();
            return;
        }

        Picasso.get()
                .load("https://image.tmdb.org/t/p/w185//nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg")
                .error(R.mipmap.ic_image_not_found_foreground)
                .into(imageIv);

//        if (intent.hasExtra(Intent.EXTRA_TEXT)) {
//            String apiPrefix = getString(R.string.api_prefix);
//            Picasso.get()
//                    .load(apiPrefix + intent.getStringExtra(Intent.EXTRA_TEXT))
//                    .fit()
//                    .into(image);
//        }

    }

    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.detail_error_message, Toast.LENGTH_SHORT).show();
    }

    private void populateUI(Movie movie) {

    }
}
