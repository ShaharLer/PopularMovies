# PopularMovies
PopularMovies is a mobile app for Android, allowing its users discover the **most-popular** / **top-rated** movies playing.
Upon launch, the user is presented with a grid arrangement of movie posters.
The user can change the sort order via a spinner: the sort order can be by **most-popular** or by **top-rated**.
Tapping a movie poster will transition to a details screen with additional information such as
trailers and reviews.

## Extras
The app uses Android Architecture Components (Room, LiveData, ViewModel and Lifecycle) in order 
to create a robust an efficient application.
Is allows a user to mark a movie as a **favorite** movie by tapping a button (star).
A favorites sort criteria is also possible in the spinner of app main screen (retrieves the data from the device database even when offline).

## Isseus
The app uses the API of themoviedb.org, with a unique API key.
In order to keep it safe (and not breaking the Terms of Service), after cloning the repo, an API key should be entered inside **string.xml** file:
`<string name="api_key">enter_api_key</string>`

## License
Licensed under the Apache License, Version 2.0
