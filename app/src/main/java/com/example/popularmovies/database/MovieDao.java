package com.example.popularmovies.database;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface MovieDao {

    @Query("SELECT * FROM movie")
    List<Movie> loadAllTasks();

    @Insert
    void insertTask(Movie taskEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateTask(Movie taskEntry);

    @Delete
    void deleteTask(Movie taskEntry);
}
