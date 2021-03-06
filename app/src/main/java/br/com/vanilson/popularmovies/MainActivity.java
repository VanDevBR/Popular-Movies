package br.com.vanilson.popularmovies;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import br.com.vanilson.popularmovies.data.MovieContract;
import br.com.vanilson.popularmovies.model.Movie;
import br.com.vanilson.popularmovies.network.NetworkUtils;
import br.com.vanilson.popularmovies.utils.MoviesAdapter;

import static br.com.vanilson.popularmovies.network.NetworkUtils.API_KEY;
import static br.com.vanilson.popularmovies.network.NetworkUtils.MOVIES_URL;

public class MainActivity extends AppCompatActivity {

    private static final int NUMBER_OF_COLUMNS = 2;
    private static final String POPULAR_SORTING = "popular";
    private static final String TOPRATED_SORTING = "top_rated";

    private RecyclerView mRecyclerView;
    private MoviesAdapter mMoviesAdapter;

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;

    private boolean showingFavorites = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.movies_recyclerview);
        mErrorMessageDisplay = findViewById(R.id.tv_error_message_display);
        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);
        mMoviesAdapter = new MoviesAdapter();

        GridLayoutManager layoutManager = new GridLayoutManager(this, NUMBER_OF_COLUMNS);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mMoviesAdapter);

        if (savedInstanceState != null) {
            List<Movie> movies = new ArrayList<>();
            Parcelable[] moviesParcelable = savedInstanceState.getParcelableArray("movies");
            for(Parcelable parcel : moviesParcelable) {
                movies.add((Movie) parcel);
            }
            mMoviesAdapter.setMovies(movies);
        } else {
            loadMovies(POPULAR_SORTING);
        }

    }

    private void loadMovies(String sortMode) {
        if (NetworkUtils.isNetworkAvailable(this)) {
            new MoviesNetworkTask().execute(sortMode);
        } else {
            Toast.makeText(this, R.string.offline_toast, Toast.LENGTH_SHORT).show();
        }
    }

    private void showErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    private void showDataView() {
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sort_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.miTopRated) {
            showingFavorites = false;
            mMoviesAdapter.setMovies(null);
            loadMovies(TOPRATED_SORTING);
            return true;
        }

        if (id == R.id.miPopular) {
            showingFavorites = false;
            mMoviesAdapter.setMovies(null);
            loadMovies(POPULAR_SORTING);
            return true;
        }

        if (id == R.id.miFavorites) {
            showingFavorites = true;
            mMoviesAdapter.setMovies(null);
            new FavoriteMoviesTask().execute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(showingFavorites){
            new FavoriteMoviesTask().execute();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        List<Movie> movies = mMoviesAdapter.getMovies();

        outState.putParcelableArray("movies", movies.toArray(new Parcelable[movies.size()]));

        super.onSaveInstanceState(outState);
    }

    public class MoviesNetworkTask extends AsyncTask<String, Void, List<Movie>> {

        @Override
        protected List<Movie> doInBackground(String... params) {

            List<Movie> movies = new ArrayList<>();

            try {

                JSONObject responseObj;
                String sortMode = POPULAR_SORTING;

                if (params.length > 0) {
                    sortMode = params[0];
                }

                URL moviesURL = new URL(MOVIES_URL + sortMode + "?api_key=" + API_KEY);
                String resp = NetworkUtils.requestHttpUrl(moviesURL);

                if (resp != null && resp.isEmpty()) {
                    return null;
                }

                responseObj = new JSONObject(resp);
                if (responseObj.has("results")) {

                    String results = responseObj.getJSONArray("results").toString();
                    movies.addAll((List<Movie>) new Gson().fromJson(results, new TypeToken<List<Movie>>() {
                    }.getType()));

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return movies;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(List<Movie> o) {
            super.onPostExecute(o);
            if (o.isEmpty()) {
                showErrorMessage();
            } else {
                mMoviesAdapter.setMovies(o);
                showDataView();
            }

            mLoadingIndicator.setVisibility(View.INVISIBLE);
        }
    }


    public class FavoriteMoviesTask extends AsyncTask<Void, Void, List<Movie>> {

        @Override
        protected List<Movie> doInBackground(Void... voids) {

            List<Movie> movies = new ArrayList<>();

            try {

                Uri uri = MovieContract.MovieEntry.CONTENT_URI;
                Cursor cursor = getContentResolver().query(
                        uri,
                        null,
                        null,
                        null,
                        null);
                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()){
                        Movie movie = new Movie();
                        movie.setId(cursor.getLong(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_ID)));
                        movie.setTitle(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE)));
                        movie.setVoteAverage(cursor.getDouble(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_AVERAGE)));
                        movie.setOverview(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_OVERVIEW)));
                        movie.setReleaseDate(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE)));
                        movie.setBackdropPath(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH)));
                        movie.setPosterPath(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH)));
                        movies.add(movie);
                    }
                }

                cursor.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return movies;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(List<Movie> o) {
            super.onPostExecute(o);
            if (o.isEmpty()) {
                showErrorMessage();
            } else {
                mMoviesAdapter.setMovies(o);
                showDataView();
            }

            mLoadingIndicator.setVisibility(View.INVISIBLE);
        }
    }


}
