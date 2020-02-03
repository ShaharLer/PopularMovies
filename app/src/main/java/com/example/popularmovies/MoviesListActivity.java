package com.example.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.example.popularmovies.model.Movie;
import com.example.popularmovies.utils.JsonUtils;
import com.example.popularmovies.utils.NetworkUtils;

import java.net.URL;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MoviesListActivity extends AppCompatActivity implements MoviesAdapter.MoviesAdapterOnClickHandler {

    private static final String TAG = MoviesListActivity.class.getSimpleName();
    private static final String SPINNER_TOP_RATED = "highest rated";
    private static final String SORT_POPULAR = "popular";
    private static final String SORT_TOP_RATED = "top_rated";
    private static final int NUMBER_OF_MOVIES_COLUMNS = 2;

    private static String spinnerPopular;
    private static String spinnerTopRated;
    private String selectedCategory = "";
    private RecyclerView mMoviesRecyclerView;
    private MoviesAdapter mMoviesAdapter;
    private ProgressBar mProgressBar;
    private Spinner mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies_list);

        spinnerPopular = getString(R.string.popular);
        spinnerTopRated = getString(R.string.top_rated);
        mMoviesRecyclerView = findViewById(R.id.rv_movies);
        mProgressBar = findViewById(R.id.pb_loading_indicator);

        GridLayoutManager layoutManager = new GridLayoutManager(this, NUMBER_OF_MOVIES_COLUMNS);
        mMoviesRecyclerView.setLayoutManager(layoutManager);
        mMoviesRecyclerView.setHasFixedSize(true);
        mMoviesAdapter = new MoviesAdapter(this);
        mMoviesRecyclerView.setAdapter(mMoviesAdapter);
    }

    @Override
    public void onClick(Movie chosenMovie) {
        Context context = this;
        Class destinationClass = MovieDetailsActivity.class;
        Intent intent = new Intent(context, destinationClass);
        intent.putExtra(Intent.EXTRA_TEXT, chosenMovie.getPosterPath());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_layout, menu);
        mSpinner = (Spinner) menu.findItem(R.id.spinner).getActionView();
        SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(getBaseContext(),
                R.array.sort_categories_array, android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(mSpinnerAdapter);
        mSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Object item = parent.getItemAtPosition(position);
                        String category = item.toString();

                        if (category.equals(spinnerPopular)) {
                            if (!selectedCategory.equals(spinnerPopular)) {
                                new FetchMoviesTask().execute(SORT_POPULAR);
                                selectedCategory = spinnerPopular;
                            }
                        } else if (category.equals(spinnerTopRated)) {
                            if (!selectedCategory.equals(spinnerTopRated)) {
                                new FetchMoviesTask().execute(SORT_TOP_RATED);
                                selectedCategory = spinnerTopRated;
                            }
                        } else {
                            Log.d(TAG, "Failed to parse the selected spinner item");
                            // Error message
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                }
        );

        return true;
    }

    /**
     * This method will make the View for the movies data visible and
     * hide the error message.
     */
    private void showMoviesData() {
        mMoviesRecyclerView.setVisibility(View.VISIBLE);
    }


    /**
     * This method will make the error message visible and hide the movies View.
     */
    private void showErrorMessage() {
        mMoviesRecyclerView.setVisibility(View.INVISIBLE);
//        mErrorMessageDisplay.setVisibility(View.VISIBLE);
//        mSpinner.getSelectedItem().toString();
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mMoviesRecyclerView.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Movie[] doInBackground(String... strings) {
            if (strings.length == 0) {
                return null;
            }

            String sortCriteria = strings[0];
            String apiKey = getResources().getString(R.string.api_key);
            URL moviesRequestUrl = NetworkUtils.buildUrl(sortCriteria, apiKey);

            try {
                String jsonMoviesResponse = NetworkUtils.getResponseFromHttpUrl(moviesRequestUrl);
                Movie[] movies = JsonUtils.parseMoviesFromJson(jsonMoviesResponse);
                return movies;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Movie[] movies) {
            mProgressBar.setVisibility(View.INVISIBLE);
            if (movies != null) {
                showMoviesData();
                mMoviesAdapter.setMoviesData(movies);
            } else {
                Log.d(TAG, "movies object is null");
                showErrorMessage();
            }
        }
    }
}