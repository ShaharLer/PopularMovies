package com.example.popularmovies.utils;

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

import com.example.popularmovies.database.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonUtils {

    private static final String KEY_RESULTS = "results";
    private static final String KEY_ID = "id";
    private static final String KEY_ORIGINAL_TITLE = "original_title";
    private static final String KEY_POSTER_PATH = "poster_path";
    private static final String KEY_OVERVIEW = "overview";
    private static final String KEY_VOTE_AVERAGE = "vote_average";
    private static final String KEY_RELEASE_DATE = "release_date";
    private static final String KEY_RUNTIME = "runtime";

    public static List<Movie> parseMoviesFromJson(String json) throws JSONException {
        JSONObject jsonMoviesResponse = new JSONObject(json);
        JSONArray results = jsonMoviesResponse.getJSONArray(KEY_RESULTS);

        List<Movie> movies = new ArrayList<>();
        for (int i = 0; i < results.length(); i++) {
            JSONObject jsonMovieObject = (JSONObject) results.get(i);
            movies.add(parseMovie(jsonMovieObject));
        }

        return movies;
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

    public static List<String> parseResponseToList(String json, String key) throws JSONException {
        JSONObject jsonMoviesResponse = new JSONObject(json);
        JSONArray results = jsonMoviesResponse.getJSONArray(KEY_RESULTS);
        List<String> listToFill = new ArrayList<>();

        for (int i = 0; i < results.length(); i++) {
            JSONObject jsonMovieObject = (JSONObject) results.get(i);
            listToFill.add(jsonMovieObject.getString(key));
        }

        return listToFill;
    }
}
