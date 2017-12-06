package com.varteq.catslovers.view.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.varteq.catslovers.R;
import com.varteq.catslovers.model.Feed;
import com.varteq.catslovers.utils.Utils;
import com.varteq.catslovers.view.MediaViewerActivity;

import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {


    List<Feed> feedList;
    Context context;

    public FeedAdapter(List<Feed> feedList, Context context) {
        this.feedList = feedList;
        this.context = context;
    }

    @Override
    public FeedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_feed, parent, false);
        FeedViewHolder vh = new FeedViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(FeedViewHolder holder, int position) {
        Feed feed = feedList.get(position);

        holder.nameTextView.setText(feed.getName());
        holder.likesTextView.setText(String.valueOf(feed.getLikes()));
        holder.messageTextView.setText(feed.getMessage());
        Glide.with(context)
                .load(feed.getAvatarUri())
                .apply(new RequestOptions().centerCrop())
                .into(holder.avatarImageView);


        if (feed.getMediaType() == 2) {
            // video
            Glide.with(context)
                    .load(feed.getPreviewUri())
                    .apply(new RequestOptions().centerCrop())
                    .into(holder.imageView);
            holder.timeTextView.setVisibility(View.VISIBLE);
            holder.timeUnderTextView.setVisibility(View.INVISIBLE);
            holder.playImageView.setVisibility(View.VISIBLE);
        } else if (feed.getMediaType() == 1) {
            // photo
            Glide.with(context)
                    .load(feed.getPreviewUri())
                    .apply(new RequestOptions().centerCrop())
                    .into(holder.imageView);
            holder.timeTextView.setVisibility(View.VISIBLE);
            holder.timeUnderTextView.setVisibility(View.INVISIBLE);
            holder.playImageView.setVisibility(View.INVISIBLE);
        } else if (feed.getMediaType() == 0) {
            // no preview
            ViewGroup.LayoutParams imageViewLayoutParams = holder.imageView.getLayoutParams();
            imageViewLayoutParams.height = Utils.convertDpToPx(0, context);
            holder.imageView.setLayoutParams(imageViewLayoutParams);

            ViewGroup.LayoutParams playLayoutParams = holder.playImageView.getLayoutParams();
            playLayoutParams.height = Utils.convertDpToPx(0, context);
            holder.playImageView.setLayoutParams(playLayoutParams);

            RelativeLayout.LayoutParams headerLayoutParams = (RelativeLayout.LayoutParams) holder.headerRelativeLayout.getLayoutParams();
            headerLayoutParams.topMargin = Utils.convertDpToPx(8, context);
            holder.headerRelativeLayout.setLayoutParams(headerLayoutParams);

            RelativeLayout.LayoutParams nameLayoutParams = (RelativeLayout.LayoutParams) holder.nameTextView.getLayoutParams();
            nameLayoutParams.topMargin = Utils.convertDpToPx(0, context);
            holder.nameTextView.setLayoutParams(nameLayoutParams);

            LinearLayout.LayoutParams messageLayoutParams = (LinearLayout.LayoutParams) holder.messageTextView.getLayoutParams();
            messageLayoutParams.topMargin = Utils.convertDpToPx(0, context);
            holder.messageTextView.setLayoutParams(messageLayoutParams);

            holder.toolbarGradientView.setVisibility(View.INVISIBLE);
            holder.timeTextView.setVisibility(View.INVISIBLE);
            holder.timeUnderTextView.setVisibility(View.VISIBLE);

        }
    }

    @Override
    public int getItemCount() {
        if (feedList == null)
            return 0;
        else
            return feedList.size();
    }

    class FeedViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        View toolbarGradientView;
        TextView nameTextView;
        TextView timeTextView;
        TextView likesTextView;
        ImageView avatarImageView;
        ImageButton menuButton;
        TextView messageTextView;
        ImageButton likeButton;
        ImageButton goIntoButton;
        CardView cardView;
        RelativeLayout headerRelativeLayout;
        TextView timeUnderTextView;
        ImageView playImageView;

        public FeedViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
            toolbarGradientView = itemView.findViewById(R.id.toolbarGradientView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            avatarImageView = itemView.findViewById(R.id.avatarImageView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            menuButton = itemView.findViewById(R.id.menuButton);
            likeButton = itemView.findViewById(R.id.likeButton);
            goIntoButton = itemView.findViewById(R.id.goIntoButton);
            likesTextView = itemView.findViewById(R.id.likesTextView);
            cardView = itemView.findViewById(R.id.cardView);
            headerRelativeLayout = itemView.findViewById(R.id.headerRelativeLayout);
            timeUnderTextView = itemView.findViewById(R.id.timeUnderTextView);
            playImageView = itemView.findViewById(R.id.playImageView);

            imageView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                Feed feed = feedList.get(position);
                Intent intent = new Intent(context, MediaViewerActivity.class);
                intent.putExtra("mediaType", feed.getMediaType());
                intent.putExtra("mediaUri", feed.getMediaUri().toString());
                context.startActivity(intent);
            });
        }


    }

}
