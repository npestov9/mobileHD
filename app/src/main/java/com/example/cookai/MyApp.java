package com.example.cookai;

import android.app.Application;
import androidx.room.Room;

import com.example.cookai.Model.AppDatabase;

public class MyApp extends Application {
    private static AppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "my-database")
                .allowMainThreadQueries() // For simplicity in this example, avoid in production
                .build();
    }

    public static AppDatabase getDatabase() {
        return database;
    }
}
