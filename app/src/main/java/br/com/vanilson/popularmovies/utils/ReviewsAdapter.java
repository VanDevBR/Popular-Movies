package br.com.vanilson.popularmovies.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import br.com.vanilson.popularmovies.R;
import br.com.vanilson.popularmovies.model.Review;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewsAdapterViewHolder> {

    List<Review> reviews;
    Context mContext;

    @Override
    public ReviewsAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.review_list_item, parent, false);
        return new ReviewsAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewsAdapterViewHolder holder, int position) {
        holder.mAuthor.setText(reviews.get(position).getAuthor());
        holder.mContent.setText(reviews.get(position).getContent());
    }

    @Override
    public int getItemCount() {
        if (null == reviews) return 0;
        return reviews.size();
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
        notifyDataSetChanged();
    }

    public List<Review> getReviews() {
        return this.reviews;
    }

    public class ReviewsAdapterViewHolder extends RecyclerView.ViewHolder {

        public final TextView mAuthor;
        public final TextView mContent;

        public ReviewsAdapterViewHolder(View view) {
            super(view);
            mAuthor = view.findViewById(R.id.tv_review_author);
            mContent = view.findViewById(R.id.tv_review_content);
        }
    }
}
