package com.example.popularmovies.utils;

import com.example.popularmovies.database.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JsonUtils {

    private static final String KEY_RESULTS = "results";
    private static final String KEY_ID = "id";
    private static final String KEY_ORIGINAL_TITLE = "original_title";
    private static final String KEY_POSTER_PATH = "poster_path";
    private static final String KEY_OVERVIEW = "overview";
    private static final String KEY_VOTE_AVERAGE = "vote_average";
    private static final String KEY_RELEASE_DATE = "release_date";
    private static final String KEY_RUNTIME = "runtime";
    private static final String KEY_VIDEO_KEY = "key";
    private static final String KEY_REVIEW_CONTENT = "content";

    public static ArrayList<Movie> parseMoviesFromJson(String json) {
        try {
            JSONObject jsonMoviesResponse = new JSONObject(json);
            JSONArray results = jsonMoviesResponse.getJSONArray(KEY_RESULTS);

            ArrayList<Movie> movies = new ArrayList<>();
            for (int i = 0; i < results.length(); i++) {
                JSONObject jsonMovieObject = (JSONObject) results.get(i);
                movies.add(parseMovie(jsonMovieObject));
            }

            return movies;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Movie parseMovie(JSONObject jsonMovieObject) throws JSONException {
        String id = jsonMovieObject.getString(KEY_ID);
        String originalTitle = jsonMovieObject.getString(KEY_ORIGINAL_TITLE);
        String posterPath = jsonMovieObject.getString(KEY_POSTER_PATH);
        String overview = jsonMovieObject.getString(KEY_OVERVIEW);
        String voteAverage = jsonMovieObject.getString(KEY_VOTE_AVERAGE);
        String releaseDate = jsonMovieObject.getString(KEY_RELEASE_DATE);

        return new Movie(id, originalTitle, posterPath, overview, voteAverage, releaseDate);
    }

    public static void parseMovieExtraDetails(String json, Movie movie) throws JSONException {
        JSONObject jsonMoviesResponse = new JSONObject(json);
        movie.setRuntime(jsonMoviesResponse.getString(KEY_RUNTIME));
    }

    public static void parseMovieVideosNew(String json, ArrayList<String> trailers) throws JSONException {
        JSONObject jsonMoviesResponse = new JSONObject(json);
        JSONArray results = jsonMoviesResponse.getJSONArray(KEY_RESULTS);
        trailers.clear();

        for (int i = 0; i < results.length(); i++) {
            JSONObject jsonMovieObject = (JSONObject) results.get(i);
            trailers.add(jsonMovieObject.getString(KEY_VIDEO_KEY));
        }
    }

    public static void parseMovieReviewsNew(String json, ArrayList<String> reviews) throws JSONException {
        JSONObject jsonMoviesResponse = new JSONObject(json);
        JSONArray results = jsonMoviesResponse.getJSONArray(KEY_RESULTS);
        reviews.clear();

        for (int i = 0; i < results.length(); i++) {
            JSONObject jsonMovieObject = (JSONObject) results.get(i);
            reviews.add(jsonMovieObject.getString(KEY_REVIEW_CONTENT));
        }
    }
}
