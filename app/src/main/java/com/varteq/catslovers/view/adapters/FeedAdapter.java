package com.varteq.catslovers.view.adapters;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;
import com.quickblox.users.model.QBUser;
import com.varteq.catslovers.R;
import com.varteq.catslovers.model.FeedPost;
import com.varteq.catslovers.utils.NetworkUtils;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.utils.TimeUtils;
import com.varteq.catslovers.utils.UiUtils;
import com.varteq.catslovers.utils.Utils;
import com.varteq.catslovers.utils.qb.QbUsersHolder;
import com.varteq.catslovers.view.MediaViewerActivity;

import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    private List<FeedPost> feedList;
    private Context context;
    private Integer currentUserId;
    private OnPostChangedListener postChangedListener;
    private OnLikeChangedListener likeChangedListener = new OnLikeChangedListener() {
        @Override
        public void onLikeChanged(boolean isLiked, int position) {
            FeedPost feed = feedList.get(position);
            if (feed.getIsUserLiked(currentUserId) != isLiked) {
                feed.onUserLiked(currentUserId, isLiked);
                if (postChangedListener != null)
                    postChangedListener.onPostChanged(feed);
            }
        }
    };

    private Handler handler = new Handler();

    public FeedAdapter(List<FeedPost> feedList, Context context, OnPostChangedListener postChangedListener) {
        this.feedList = feedList;
        this.context = context;
        this.postChangedListener = postChangedListener;
        currentUserId = Integer.valueOf(Profile.getUserId(context));
    }

    @Override
    public FeedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.simple_card_feed, parent, false);
        FeedViewHolder vh = new FeedViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(FeedViewHolder holder, int position) {
        FeedPost feed = feedList.get(position);

        holder.nameTextView.setText(feed.getName());
        holder.likeImageButton.setLikes(feed.getIsUserLiked(currentUserId), feed.getLikes() != null ? feed.getLikes().size() : 0, position);

        QBUser user = QbUsersHolder.getInstance().getUserById(feed.getUserId());
        if (user != null && user.getCustomData() != null) {
            Glide.with(holder.itemView)
                    .load(user.getCustomData())
                    .into(holder.avatarImageView);
        } else
            holder.avatarImageView.setImageBitmap(Utils.getBitmapWithColor(UiUtils.getCircleColorForPosition(feed.getUserId())));

        String message = feed.getMessage();
        if (message != null && !message.equals("null"))
            holder.messageTextView.setText(feed.getMessage());
        else holder.messageTextView.setText(null);

        cleanView(holder);

        if (feed.getType().equals(FeedPost.FeedPostType.VIDEO)) {
            // video
            Glide.with(context)
                    .load(NetworkUtils.getFeedPostPreviewGlideUrl(feed.getId()))
                    .into(holder.imageView);
            holder.timeUnderTextView.setVisibility(View.GONE);

            holder.timeTextView.setText(TimeUtils.getDateAsMMMMddHHmm(feed.getDate()));
        } else if (feed.getType().equals(FeedPost.FeedPostType.PICTURE)) {
            // photo
            Glide.with(context)
                    .load(NetworkUtils.getFeedPostPreviewGlideUrl(feed.getId()))
                    .into(holder.imageView);
            holder.timeUnderTextView.setVisibility(View.GONE);
            holder.playImageView.setVisibility(View.INVISIBLE);

            holder.timeTextView.setText(TimeUtils.getDateAsMMMMddHHmm(feed.getDate()));
        } else if (feed.getType().equals(FeedPost.FeedPostType.TEXT)) {
            // no preview
            holder.mediaLayout.setVisibility(View.GONE);
            holder.emptyView.setVisibility(View.GONE);
            holder.timeTextView.setVisibility(View.GONE);

            holder.timeUnderTextView.setText(TimeUtils.getDateAsMMMMddHHmm(feed.getDate()));
        }
    }

    private void cleanView(FeedViewHolder holder) {
        //holder.toolbarGradientView.setVisibility(View.VISIBLE);
        holder.imageView.setImageBitmap(null);
        holder.mediaLayout.setVisibility(View.VISIBLE);
        holder.emptyView.setVisibility(View.VISIBLE);
        holder.timeTextView.setVisibility(View.VISIBLE);
        holder.timeUnderTextView.setVisibility(View.VISIBLE);
        holder.playImageView.setVisibility(View.VISIBLE);
        holder.timeUnderTextView.setText("");
        holder.timeTextView.setText("");
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
        //TextView likesTextView;
        RoundedImageView avatarImageView;
        //ImageButton menuButton;
        TextView messageTextView;
        //ImageButton likeButton;
        //ImageButton goIntoButton;
        CardView cardView;
        //RelativeLayout headerRelativeLayout;
        TextView timeUnderTextView;
        ImageView playImageView;
        RelativeLayout mediaLayout;
        View emptyView;
        LikeImageButton likeImageButton;

        public FeedViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
            toolbarGradientView = itemView.findViewById(R.id.toolbarGradientView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            avatarImageView = itemView.findViewById(R.id.avatarImageView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            //menuButton = itemView.findViewById(R.id.menuButton);
            ImageButton likeButton = itemView.findViewById(R.id.likeButton);
            //goIntoButton = itemView.findViewById(R.id.goIntoButton);
            TextView likesTextView = itemView.findViewById(R.id.likesTextView);
            likeImageButton = new LikeImageButton(likeButton, likesTextView, likeChangedListener, handler);

            cardView = itemView.findViewById(R.id.cardView);
            //headerRelativeLayout = itemView.findViewById(R.id.headerRelativeLayout);
            timeUnderTextView = itemView.findViewById(R.id.timeUnderTextView);
            playImageView = itemView.findViewById(R.id.playImageView);

            mediaLayout = itemView.findViewById(R.id.media_layout);
            emptyView = itemView.findViewById(R.id.empty_view);

            imageView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                FeedPost feed = feedList.get(position);
                MediaViewerActivity.startActivity(context, feed);
            });
        }
    }

    class LikeImageButton {

        private static final long DELAY = 1000;
        private ImageButton likeButton;
        private TextView likesTextView;
        private boolean isUserLiked;
        private int likesCount;
        private OnLikeChangedListener likeChangedListener;
        private Handler handler;
        private Runnable runnable;

        public LikeImageButton(ImageButton likeButton, TextView likesTextView, OnLikeChangedListener likeChangedListener, Handler handler) {
            this.likeButton = likeButton;
            this.likesTextView = likesTextView;
            this.likeChangedListener = likeChangedListener;
            this.handler = handler;

            likeButton.setOnClickListener(view -> {
                isUserLiked = !isUserLiked;
                if (isUserLiked)
                    likesCount++;
                else likesCount--;
                onLikeChanged(isUserLiked, likesCount);
                updatePostLike(isUserLiked, (Integer) likeButton.getTag());
            });
        }

        private void updatePostLike(boolean isUserLiked, int position) {
            if (runnable != null)
                handler.removeCallbacks(runnable);
            else
                runnable = () -> {
                    if (likeChangedListener != null)
                        likeChangedListener.onLikeChanged(isUserLiked, position);
                };
            handler.postDelayed(runnable, DELAY);
        }

        public void setLikes(boolean isUserLiked, int likesCount, int position) {
            runnable = null;
            this.isUserLiked = isUserLiked;
            this.likesCount = likesCount;
            likeButton.setTag(position);
            onLikeChanged(isUserLiked, likesCount);
        }

        private void onLikeChanged(boolean isUserLiked, int likesCount) {
            likesTextView.setText(String.valueOf(likesCount));
            setLikeIcon(isUserLiked);
        }

        private void setLikeIcon(boolean isUserLiked) {
            if (isUserLiked)
                likeButton.setBackground(likeButton.getContext().getResources().getDrawable(R.drawable.ic_liked));
            else
                likeButton.setBackground(likeButton.getContext().getResources().getDrawable(R.drawable.ic_unliked));
        }
    }

    interface OnLikeChangedListener {
        void onLikeChanged(boolean isLiked, int position);
    }

    public interface OnPostChangedListener {
        void onPostChanged(FeedPost feedPost);
    }
}
