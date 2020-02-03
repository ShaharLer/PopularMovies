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
    private static final String API_KEY_PARAM = "api_key";

    /**
     * This method builds a URL that will be used to query a movies list from themoviedb.org .
     *
     * @param sortCriteria The criteria that will determine the movies kind of sort
     * @param apiKey The api key to work with the themoviedb.org API
     * @return
     */
    public static URL buildUrl(String sortCriteria, String apiKey) {
        Uri uri = Uri.parse(MOVIES_SORT_BASE_URL + sortCriteria).buildUpon()
                .appendQueryParameter(API_KEY_PARAM, apiKey)
                .build();

        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // TODO delete after finishing debug
        Log.v(TAG, "Built URI " + url);

        return url;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
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
    public static void downloadImageINtoView(String path, ImageView view) {
        String apiPrefix = view.getContext().getString(R.string.api_prefix);
        Picasso.get()
                .load(apiPrefix + path)
                .error(R.mipmap.ic_image_not_found_foreground)
                .fit()
                .into(view);
    }
}
