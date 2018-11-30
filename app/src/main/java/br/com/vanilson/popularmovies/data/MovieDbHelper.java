package br.com.vanilson.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import br.com.vanilson.popularmovies.data.MovieContract.MovieEntry;

public class MovieDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "movie.db";

    private static final int DATABASE_VERSION = 2;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_WEATHER_TABLE =

                "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +

                MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieEntry.COLUMN_RELEASE_DATE + " STRING NOT NULL, " +
                MovieEntry.COLUMN_ID + " INTEGER NOT NULL," +
                MovieEntry.COLUMN_TITLE + " STRING NOT NULL, " +
                MovieEntry.COLUMN_AVERAGE + " REAL NOT NULL, " +
                MovieEntry.COLUMN_OVERVIEW + " STRING NOT NULL, " +
                MovieEntry.COLUMN_BACKDROP_PATH + " STRING NOT NULL, " +
                MovieEntry.COLUMN_POSTER_PATH + " STRING NOT NULL, " +
                " UNIQUE (" + MovieEntry.COLUMN_ID + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}