package com.example.popularmovies;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TrailersAdapter extends RecyclerView.Adapter<TrailersAdapter.TrailerViewHolder> {

    private static final String TRAILER_TITLE = "Trailer";

    private String[] mTrailersKeys;

    public TrailersAdapter(String[] trailersKeys) {
        mTrailersKeys = trailersKeys;
    }

    @NonNull
    @Override
    public TrailerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.trailer_item, parent, false);
        return new TrailerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrailerViewHolder holder, int position) {
        String trailerTitle = TRAILER_TITLE + " " + (position + 1);
        holder.trailerTitleTv.setText(trailerTitle);
    }

    @Override
    public int getItemCount() {
        return mTrailersKeys.length;
    }

    class TrailerViewHolder extends RecyclerView.ViewHolder {

        TextView trailerTitleTv;

        TrailerViewHolder(@NonNull View itemView) {
            super(itemView);
            trailerTitleTv = itemView.findViewById(R.id.trailer_name);
        }
    }

}
