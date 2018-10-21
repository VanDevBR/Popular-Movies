package br.com.vanilson.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import br.com.vanilson.popularmovies.model.Movie;

import static br.com.vanilson.popularmovies.network.NetworkUtils.IMG_POSTER_URL;

public class MovieDetail extends AppCompatActivity {

    private TextView tvTitle;
    private TextView tvRating;
    private TextView tvSynopsis;
    private TextView tvDate;
    private ImageView ivPoster;
    private Movie movie;
    private Button btBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        tvTitle = findViewById(R.id.tv_movie_title);
        tvRating = findViewById(R.id.tv_movie_rating);
        tvSynopsis = findViewById(R.id.tv_movie_synopsis);
        tvDate = findViewById(R.id.tv_movie_date);
        ivPoster = findViewById(R.id.iv_movie_poster);
        btBack = findViewById(R.id.bt_back);

        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btBackClick();
            }
        });

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

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void btBackClick(){
        onBackPressed();
    }
}
