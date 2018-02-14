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
import com.bumptech.glide.request.RequestOptions;
import com.makeramen.roundedimageview.RoundedImageView;
import com.varteq.catslovers.R;
import com.varteq.catslovers.model.FeedPost;
import com.varteq.catslovers.utils.NetworkUtils;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.utils.TimeUtils;
import com.varteq.catslovers.view.MediaViewerActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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
        holder.commentsTextView.setText(String.valueOf(feed.getCommentsCount()));

        boolean isAvatarExist = feed.getAvatar() != null && !feed.getAvatar().isEmpty();
        Glide.with(holder.itemView)
                .load(isAvatarExist ? feed.getAvatar() : R.drawable.user_avatar_default)
                .apply(new RequestOptions().error(R.drawable.user_avatar_default))
                .into(holder.avatarImageView);

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
        @BindView(R.id.imageView)
        ImageView imageView;
        @BindView(R.id.toolbarGradientView)
        View toolbarGradientView;
        @BindView(R.id.nameTextView)
        TextView nameTextView;
        @BindView(R.id.timeTextView)
        TextView timeTextView;
        @BindView(R.id.likesTextView)
        TextView likesTextView;
        @BindView(R.id.likeButton)
        ImageButton likeButton;
        @BindView(R.id.comentsTextView)
        TextView commentsTextView;
        @BindView(R.id.comentsButton)
        ImageButton commentsButton;
        @BindView(R.id.avatarImageView)
        RoundedImageView avatarImageView;
        @BindView(R.id.messageTextView)
        TextView messageTextView;
        @BindView(R.id.cardView)
        CardView cardView;
        @BindView(R.id.timeUnderTextView)
        TextView timeUnderTextView;
        @BindView(R.id.playImageView)
        ImageView playImageView;
        @BindView(R.id.media_layout)
        RelativeLayout mediaLayout;
        @BindView(R.id.empty_view)
        View emptyView;
        LikeImageButton likeImageButton;
        //R.id.menuButton
        //R.id.goIntoButton
        //ImageButton goIntoButton;
        //ImageButton menuButton;

        public FeedViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
            likeImageButton = new LikeImageButton(likeButton, likesTextView, likeChangedListener, handler);

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
