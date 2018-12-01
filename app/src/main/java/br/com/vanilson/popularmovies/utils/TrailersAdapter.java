package br.com.vanilson.popularmovies.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import br.com.vanilson.popularmovies.R;
import br.com.vanilson.popularmovies.model.Trailer;
import br.com.vanilson.popularmovies.network.NetworkUtils;

public class TrailersAdapter extends RecyclerView.Adapter<TrailersAdapter.TrailersAdapterViewHolder> {

    List<Trailer> trailers;
    Context mContext;

    @Override
    public TrailersAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.trailer_list_item, parent, false);
        return new TrailersAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrailersAdapterViewHolder holder, int position) {
        holder.mTrailerName.setText(trailers.get(position).getName());
    }

    @Override
    public int getItemCount() {
        if (null == trailers) return 0;
        return trailers.size();
    }

    public void setTrailers(List<Trailer> trailers) {
        this.trailers = trailers;
        notifyDataSetChanged();
    }

    public List<Trailer> getTrailers() {
        return this.trailers;
    }

    public class TrailersAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView mTrailerName;

        public TrailersAdapterViewHolder(View view) {
            super(view);
            mTrailerName = view.findViewById(R.id.tv_trailer_name);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            try{
                Trailer trailer = trailers.get(getAdapterPosition());
                Uri youtubeUri = Uri.parse(NetworkUtils.YOUTUBE_URL+trailer.getKey());
                Intent intent = new Intent(Intent.ACTION_VIEW, youtubeUri);
                mContext.startActivity(intent);
            }catch (Exception e){
                e.printStackTrace();
            }


        }
    }
}
