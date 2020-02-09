package com.example.popularmovies.utils;

import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.example.popularmovies.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();
    private static final String MOVIES_SORT_BASE_URL = "https://api.themoviedb.org/3/movie/";
    private static final String MOVIE_TRAILERS_URL_SUFFIX = "/videos";
    private static final String MOVIE_REVIEWS_URL_SUFFIX = "/reviews";
    private static final String API_KEY_PARAM = "api_key";

    /**
     * This method calls buildUrl to get a URL that will be used to query a list of movies from themoviedb.org .
     *
     * @param parameter The parameter that will be given to decide MOVIES GET command.
     * @param apiKey The api key to work with the themoviedb.org API.
     * @return A URL to query a sorted list of movies by category from themoviedb.org .
     */
    public static URL buildParameterizedUrl(String parameter, String apiKey) {
        return buildUrl(MOVIES_SORT_BASE_URL + parameter, apiKey);
    }

    /**
     * This method calls buildUrl to get a URL that will be used to query movie's trailers from themoviedb.org .
     *
     * @param id The movie id.
     * @param apiKey The api key to work with the themoviedb.org API.
     * @return A URL to query a the trailers of movies by category from themoviedb.org .
     */
    public static URL buildTrailersUrl(String id, String apiKey) {
        return buildUrl(MOVIES_SORT_BASE_URL + id + MOVIE_TRAILERS_URL_SUFFIX, apiKey);
    }

    /**
     * This method calls buildUrl to get a URL that will be used to query movie's reviews from themoviedb.org .
     *
     * @param id The movie id.
     * @param apiKey The api key to work with the themoviedb.org API.
     * @return
     */
    public static URL buildReviewsUrl(int id, String apiKey) {
        return buildUrl(MOVIES_SORT_BASE_URL + id + MOVIE_REVIEWS_URL_SUFFIX, apiKey);
    }

    /**
     * This method builds a URL that will be used to query from themoviedb.org .
     *
     * @param uriPrefix The string that will be the prefix for the final URL.
     * @param apiKey The api key to work with the themoviedb.org API.
     * @return A URL to query from themoviedb.org .
     */
    private static URL buildUrl(String uriPrefix, String apiKey) {
        Uri uri = Uri.parse(uriPrefix).buildUpon()
                .appendQueryParameter(API_KEY_PARAM, apiKey)
                .build();

        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading.
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    /**
     * This method downloads and image to the given image view.
     *
     * @param path The suffix of the path to the full image path.
     * @param view The image view where the image will be rendered to.
     */
    public static void downloadImageIntoView(String path, ImageView view) {
        String apiPrefix = view.getContext().getString(R.string.api_prefix);
        Picasso.get()
                .load(apiPrefix + path)
                .error(R.mipmap.ic_image_not_found_foreground)
                .fit()
                .into(view);
    }
}
