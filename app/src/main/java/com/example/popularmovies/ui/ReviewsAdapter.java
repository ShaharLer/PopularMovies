package com.example.popularmovies.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.popularmovies.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {

    private ReviewsAdapterOnClickHandler mClickHandler;
    private List<String> mReviewsData;

    public interface ReviewsAdapterOnClickHandler {
        void onReviewClicked(String review);
    }

    ReviewsAdapter(ReviewsAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
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
        if (mReviewsData == null) {
            return 0;
        }
        return mReviewsData.size();
    }

    void setReviewsData(List<String> reviewsData) {
        mReviewsData = reviewsData;
        notifyDataSetChanged();
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String chosenReview = mReviewsData.get(getAdapterPosition());
            mClickHandler.onReviewClicked(chosenReview);
        }
    }

}
