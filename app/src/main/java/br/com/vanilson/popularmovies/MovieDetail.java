package br.com.vanilson.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
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
import br.com.vanilson.popularmovies.model.Review;
import br.com.vanilson.popularmovies.model.Trailer;
import br.com.vanilson.popularmovies.network.NetworkUtils;
import br.com.vanilson.popularmovies.utils.ReviewsAdapter;
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
    private RecyclerView rvReviews;
    private TrailersAdapter mTrailersAdapter;
    private ReviewsAdapter mReviewsAdapter;

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
        rvReviews = findViewById(R.id.rvReviews);
        mTrailersAdapter = new TrailersAdapter();
        mReviewsAdapter = new ReviewsAdapter();
        LinearLayoutManager trailersLayoutManager = new LinearLayoutManager(this);
        LinearLayoutManager reviewsLayoutManager = new LinearLayoutManager(this);

        trailersLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvTrailers.setHasFixedSize(true);
        rvTrailers.setLayoutManager(trailersLayoutManager);
        rvTrailers.setAdapter(mTrailersAdapter);
        rvTrailers.setNestedScrollingEnabled(false);
        rvTrailers.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        reviewsLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvReviews.setHasFixedSize(true);
        rvReviews.setLayoutManager(reviewsLayoutManager);
        rvReviews.setNestedScrollingEnabled(false);
        rvReviews.setAdapter(mReviewsAdapter);

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
            List<Trailer> trailers = handleStateTrailers(savedInstanceState);
            mTrailersAdapter.setTrailers(trailers);

            List<Review> reviews = handleStateReviews(savedInstanceState);
            mReviewsAdapter.setReviews(reviews);

            showData();
        }else{
            loadTrailersAndReviews();
        }

    }

    private List<Trailer> handleStateTrailers(Bundle savedInstanceState) {
        List<Trailer> trailers = new ArrayList<>();
        Parcelable[] moviesParcelable = savedInstanceState.getParcelableArray("trailers");
        for(Parcelable parcel : moviesParcelable) {
            trailers.add((Trailer) parcel);
        }
        return trailers;
    }


    private List<Review> handleStateReviews(Bundle savedInstanceState) {
        List<Review> reviews = new ArrayList<>();
        Parcelable[] moviesParcelable = savedInstanceState.getParcelableArray("reviews");
        if (moviesParcelable != null){
            for(Parcelable parcel : moviesParcelable) {
                reviews.add((Review) parcel);
            }
        }

        return reviews;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        List<Trailer> trailers = mTrailersAdapter.getTrailers();
        if(trailers != null && !trailers.isEmpty())
            outState.putParcelableArray("trailers", trailers.toArray(new Parcelable[trailers.size()]));

        List<Review> reviews = mReviewsAdapter.getReviews();
        if (reviews != null && !reviews.isEmpty())
            outState.putParcelableArray("reviews", reviews.toArray(new Parcelable[reviews.size()]));

        super.onSaveInstanceState(outState);
    }

    private void loadTrailersAndReviews() {
        if (isNetworkAvailable(this)) {
            new TrailersNetworkTask().execute();
            new ReviewsNetworkTask().execute();
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
        boolean result = false;
        String stringId = movie.getId().toString();
        Uri uri = MovieContract.MovieEntry.CONTENT_URI;
        uri = uri.buildUpon().appendPath(stringId).build();

        Cursor cursor = getContentResolver().query(uri,
                null,
                "id = ?",
                new String[]{movie.getId().toString()},
                null);
        if (cursor != null && cursor.getCount() > 0) {
            result = true;
        }

        try {
            cursor.close();
        } catch (Exception e){
            e.printStackTrace();
        }

        return result;
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

        Toast.makeText(getBaseContext(), getString(R.string.favorite_remove_success, movie.getTitle()), Toast.LENGTH_LONG).show();
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

    public class ReviewsNetworkTask extends AsyncTask<String, Void, List<Review>> {

        @Override
        protected List<Review> doInBackground(String... params) {

            List<Review> reviews = new ArrayList<>();

            try {

                JSONObject responseObj;

                URL moviesURL = new URL(MOVIES_URL + movie.getId() + "/reviews?api_key=" + API_KEY);
                String resp = NetworkUtils.requestHttpUrl(moviesURL);

                if (resp != null && resp.isEmpty()) {
                    return null;
                }

                responseObj = new JSONObject(resp);
                if (responseObj.has("results")) {

                    String results = responseObj.getJSONArray("results").toString();
                    reviews.addAll((List<Review>) new Gson().fromJson(results, new TypeToken<List<Review>>() {
                    }.getType()));

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return reviews;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(List<Review> o) {
            super.onPostExecute(o);
            if (!o.isEmpty()) {
                mReviewsAdapter.setReviews(o);
            }
            showData();
        }
    }

    private void showData() {
        vContainer.setVisibility(View.VISIBLE);
        mLoadingIndicator.setVisibility(View.GONE);
    }

}
