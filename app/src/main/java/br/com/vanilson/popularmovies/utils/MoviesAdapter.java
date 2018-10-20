package br.com.vanilson.popularmovies.utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import java.util.List;

import br.com.vanilson.popularmovies.MovieDetail;
import br.com.vanilson.popularmovies.R;
import br.com.vanilson.popularmovies.model.Movie;

import static br.com.vanilson.popularmovies.network.NetworkUtils.IMG_URL;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesAdapterViewHolder> {

    List<Movie> movies;
    Context mContext;

    @Override
    public MoviesAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.movie_list_item, parent, false);
        return new MoviesAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoviesAdapterViewHolder holder, int position) {
        Picasso.with(mContext).load(IMG_URL + movies.get(position).getPosterPath()).into(holder.mMovieImageView);
    }

    @Override
    public int getItemCount() {
        if (null == movies) return 0;
        return movies.size();
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
        notifyDataSetChanged();
    }

    public class MoviesAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView mMovieImageView;

        public MoviesAdapterViewHolder(View view) {
            super(view);
            mMovieImageView = view.findViewById(R.id.iv_movie_item);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            try{
                Movie movie = movies.get(getAdapterPosition());
                Intent intent = new Intent(mContext, MovieDetail.class);
                intent.putExtra("movie", movie);
                mContext.startActivity(intent);
            }catch (Exception e){
                e.printStackTrace();
            }


        }
    }
}
