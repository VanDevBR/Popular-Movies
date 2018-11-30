package br.com.vanilson.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import br.com.vanilson.popularmovies.data.MovieContract;
import br.com.vanilson.popularmovies.model.Movie;

import static br.com.vanilson.popularmovies.network.NetworkUtils.IMG_POSTER_URL;

public class MovieDetail extends AppCompatActivity {

    private TextView tvTitle;
    private TextView tvRating;
    private TextView tvSynopsis;
    private TextView tvDate;
    private ImageView ivPoster;
    private CheckBox cbFavorite;
    private Movie movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        tvTitle = findViewById(R.id.tv_movie_title);
        tvRating = findViewById(R.id.tv_movie_rating);
        tvSynopsis = findViewById(R.id.tv_movie_synopsis);
        tvDate = findViewById(R.id.tv_movie_date);
        ivPoster = findViewById(R.id.iv_movie_poster);
        cbFavorite = findViewById(R.id.cb_favorite);

        Intent selfIntent = getIntent();

        if(!selfIntent.hasExtra("movie")){
            onBackPressed();
        }

        movie = selfIntent.getParcelableExtra("movie");

        tvTitle.setText(movie.getTitle());
        tvRating.setText(String.valueOf(movie.getVoteAverage()));
        tvSynopsis.setText(movie.getOverview());
        tvDate.setText(movie.getReleaseDate());
        Picasso.with(this).load(IMG_POSTER_URL + movie.getBackdropPath()).into(ivPoster);
        cbFavorite.setChecked(isFavorite());

    }

    public void toggleFavorite(View view) {
        if(cbFavorite.isChecked()){
            addToFavorites();
        }else{
            removeFromFavorites();
        }
    }

    private boolean isFavorite(){
        String stringId = movie.getId().toString();
        Uri uri = MovieContract.MovieEntry.CONTENT_URI;
        uri = uri.buildUpon().appendPath(stringId).build();

        Cursor cursor = getContentResolver().query(uri,
                null,
                "id = ?",
                new String[]{movie.getId().toString()},
                null);
        if(cursor != null && cursor.getCount() > 0){
            return true;
        }

        return false;
    }

    private void addToFavorites(){

        ContentValues contentValues = new ContentValues();

        contentValues.put(MovieContract.MovieEntry.COLUMN_ID, movie.getId());
        contentValues.put(MovieContract.MovieEntry.COLUMN_TITLE, movie.getTitle());
        contentValues.put(MovieContract.MovieEntry.COLUMN_AVERAGE, movie.getVoteAverage());
        contentValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, movie.getOverview());
        contentValues.put(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH, movie.getBackdropPath());
        contentValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
        contentValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, movie.getPosterPath());

        Uri uri = getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, contentValues);

        if(uri != null) {
            Toast.makeText(getBaseContext(), getString(R.string.favorite_add_success, movie.getTitle()), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getBaseContext(), getString(R.string.error_message), Toast.LENGTH_LONG).show();
        }

    }

    private void removeFromFavorites(){

        String stringId = movie.getId().toString();
        Uri uri = MovieContract.MovieEntry.CONTENT_URI;
        uri = uri.buildUpon().appendPath(stringId).build();

        getContentResolver().delete(uri, null, null);

        if(uri != null) {
            Toast.makeText(getBaseContext(), getString(R.string.favorite_remove_success, movie.getTitle()), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getBaseContext(), getString(R.string.error_message), Toast.LENGTH_LONG).show();
        }
    }

}
