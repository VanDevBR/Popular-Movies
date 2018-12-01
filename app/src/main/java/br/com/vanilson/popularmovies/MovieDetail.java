package br.com.vanilson.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import br.com.vanilson.popularmovies.data.MovieContract;
import br.com.vanilson.popularmovies.model.Movie;
import br.com.vanilson.popularmovies.model.Trailer;
import br.com.vanilson.popularmovies.network.NetworkUtils;
import br.com.vanilson.popularmovies.utils.TrailersAdapter;

import static br.com.vanilson.popularmovies.network.NetworkUtils.API_KEY;
import static br.com.vanilson.popularmovies.network.NetworkUtils.IMG_POSTER_URL;
import static br.com.vanilson.popularmovies.network.NetworkUtils.MOVIES_URL;
import static br.com.vanilson.popularmovies.network.NetworkUtils.isNetworkAvailable;

public class MovieDetail extends AppCompatActivity {

    private TextView tvTitle;
    private TextView tvRating;
    private TextView tvSynopsis;
    private TextView tvDate;
    private ImageView ivPoster;
    private CheckBox cbFavorite;
    private Movie movie;
    private ProgressBar mLoadingIndicator;
    private View vContainer;
    private RecyclerView rvTrailers;
    private TrailersAdapter mTrailersAdapter;

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
        mLoadingIndicator = findViewById(R.id.pb_detail_loading_indicator);
        vContainer = findViewById(R.id.detail_container);
        rvTrailers = findViewById(R.id.rvTrailers);
        mTrailersAdapter = new TrailersAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvTrailers.setHasFixedSize(true);
        rvTrailers.setLayoutManager(layoutManager);
        rvTrailers.setAdapter(mTrailersAdapter);

        Intent selfIntent = getIntent();

        if (!selfIntent.hasExtra("movie")) {
            onBackPressed();
        }

        movie = selfIntent.getParcelableExtra("movie");

        tvTitle.setText(movie.getTitle());
        tvRating.setText(String.valueOf(movie.getVoteAverage()));
        tvSynopsis.setText(movie.getOverview());
        tvDate.setText(movie.getReleaseDate());
        Picasso.with(this).load(IMG_POSTER_URL + movie.getBackdropPath()).into(ivPoster);
        cbFavorite.setChecked(isFavorite());

        if(savedInstanceState != null){
            List<Trailer> trailers = new ArrayList<>();
            Parcelable[] moviesParcelable = savedInstanceState.getParcelableArray("trailers");
            for(Parcelable parcel : moviesParcelable) {
                trailers.add((Trailer) parcel);
            }
            mTrailersAdapter.setTrailers(trailers);
            showData();
        }else{
            loadTrailers();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        List<Trailer> trailers = mTrailersAdapter.getTrailers();

        outState.putParcelableArray("trailers", trailers.toArray(new Parcelable[trailers.size()]));

        super.onSaveInstanceState(outState);
    }

    private void loadTrailers() {
        if (isNetworkAvailable(this)) {
            new TrailersNetworkTask().execute();
        } else {
            showData();
        }
    }

    public void toggleFavorite(View view) {
        if (cbFavorite.isChecked()) {
            addToFavorites();
        } else {
            removeFromFavorites();
        }
    }

    private boolean isFavorite() {
        String stringId = movie.getId().toString();
        Uri uri = MovieContract.MovieEntry.CONTENT_URI;
        uri = uri.buildUpon().appendPath(stringId).build();

        Cursor cursor = getContentResolver().query(uri,
                null,
                "id = ?",
                new String[]{movie.getId().toString()},
                null);
        if (cursor != null && cursor.getCount() > 0) {
            return true;
        }

        return false;
    }

    private void addToFavorites() {

        ContentValues contentValues = new ContentValues();

        contentValues.put(MovieContract.MovieEntry.COLUMN_ID, movie.getId());
        contentValues.put(MovieContract.MovieEntry.COLUMN_TITLE, movie.getTitle());
        contentValues.put(MovieContract.MovieEntry.COLUMN_AVERAGE, movie.getVoteAverage());
        contentValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, movie.getOverview());
        contentValues.put(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH, movie.getBackdropPath());
        contentValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
        contentValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, movie.getPosterPath());

        Uri uri = getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, contentValues);

        if (uri != null) {
            Toast.makeText(getBaseContext(), getString(R.string.favorite_add_success, movie.getTitle()), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getBaseContext(), getString(R.string.error_message), Toast.LENGTH_LONG).show();
        }

    }

    private void removeFromFavorites() {

        String stringId = movie.getId().toString();
        Uri uri = MovieContract.MovieEntry.CONTENT_URI;
        uri = uri.buildUpon().appendPath(stringId).build();

        getContentResolver().delete(uri, null, null);

        if (uri != null) {
            Toast.makeText(getBaseContext(), getString(R.string.favorite_remove_success, movie.getTitle()), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getBaseContext(), getString(R.string.error_message), Toast.LENGTH_LONG).show();
        }
    }


    public class TrailersNetworkTask extends AsyncTask<String, Void, List<Trailer>> {

        @Override
        protected List<Trailer> doInBackground(String... params) {

            List<Trailer> trailers = new ArrayList<>();

            try {

                JSONObject responseObj;

                URL moviesURL = new URL(MOVIES_URL + movie.getId() + "/videos?api_key=" + API_KEY);
                String resp = NetworkUtils.requestHttpUrl(moviesURL);

                if (resp != null && resp.isEmpty()) {
                    return null;
                }

                responseObj = new JSONObject(resp);
                if (responseObj.has("results")) {

                    String results = responseObj.getJSONArray("results").toString();
                    trailers.addAll((List<Trailer>) new Gson().fromJson(results, new TypeToken<List<Trailer>>() {
                    }.getType()));

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return trailers;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(List<Trailer> o) {
            super.onPostExecute(o);
            if (!o.isEmpty()) {
                mTrailersAdapter.setTrailers(o);
            }
            showData();
        }
    }

    private void showData() {
        vContainer.setVisibility(View.VISIBLE);
        mLoadingIndicator.setVisibility(View.GONE);
    }

}
