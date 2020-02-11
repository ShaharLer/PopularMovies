package com.example.popularmovies;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {

    private ReviewsAdapterOnClickHandler mClickHandler;
    private String[] mReviews;

    public interface ReviewsAdapterOnClickHandler {
        void onClick(String review);
    }

    public ReviewsAdapter(ReviewsAdapterOnClickHandler clickHandler, String[] reviews) {
        mClickHandler = clickHandler;
        mReviews = reviews;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.review_item, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return mReviews.length;
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageButton imageIb;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            imageIb = itemView.findViewById(R.id.review_image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            String chosenReview = mReviews[adapterPosition];
            mClickHandler.onClick(chosenReview);
        }
    }

}
