package com.varteq.catslovers.view.qb.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.makeramen.roundedimageview.RoundedImageView;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.helper.CollectionsUtil;
import com.quickblox.users.model.QBUser;
import com.varteq.catslovers.R;
import com.varteq.catslovers.utils.ChatHelper;
import com.varteq.catslovers.utils.ResourceUtils;
import com.varteq.catslovers.utils.TimeUtils;
import com.varteq.catslovers.utils.qb.PaginationHistoryListener;
import com.varteq.catslovers.utils.qb.QbUsersHolder;
import com.varteq.catslovers.view.qb.AttachmentImageActivity;
import com.varteq.catslovers.view.qb.widget.MaskedImageView;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.util.Collection;
import java.util.List;


public class ChatAdapter extends BaseListAdapter<QBChatMessage> {

    private static final String TAG = ChatAdapter.class.getSimpleName();
    private final QBChatDialog chatDialog;
    private OnItemInfoExpandedListener onItemInfoExpandedListener;
    private PaginationHistoryListener paginationListener;
    private int previousGetCount = 0;
    private Integer currentUserId = ChatHelper.getCurrentUser().getId();

    public ChatAdapter(Context context, QBChatDialog chatDialog, List<QBChatMessage> chatMessages) {
        super(context, chatMessages);
        this.chatDialog = chatDialog;
    }

    public void setOnItemInfoExpandedListener(OnItemInfoExpandedListener onItemInfoExpandedListener) {
        this.onItemInfoExpandedListener = onItemInfoExpandedListener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.list_item_chat_message, parent, false);

            holder.avatarImageView = convertView.findViewById(R.id.avatarImageView);
            holder.messageBodyTextView = (TextView) convertView.findViewById(R.id.text_image_message);
            holder.messageAuthorTextView = (TextView) convertView.findViewById(R.id.text_message_author);
            holder.messageContainerLayout = (LinearLayout) convertView.findViewById(R.id.layout_chat_message_container);
            holder.messageBodyContainerLayout = (RelativeLayout) convertView.findViewById(R.id.layout_message_content_container);
            holder.messageInfoTextView = (TextView) convertView.findViewById(R.id.text_message_info);
            holder.attachmentImageView = (MaskedImageView) convertView.findViewById(R.id.image_message_attachment);
            holder.attachmentProgressBar = (ProgressBar) convertView.findViewById(R.id.progress_message_attachment);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final QBChatMessage chatMessage = getItem(position);

        Integer userId = chatMessage.getSenderId();
        if (userId == null)
            userId = currentUserId;
        QBUser user = QbUsersHolder.getInstance().getUserById(userId);
        boolean isAvatarExist = user != null && user.getCustomData() != null && !user.getCustomData().isEmpty();
        Glide.with(convertView)
                .load(isAvatarExist ? user.getCustomData() : R.drawable.user_avatar_default)
                .apply(new RequestOptions().error(R.drawable.user_avatar_default))
                .into(holder.avatarImageView);

        setIncomingOrOutgoingMessageAttributes(holder, chatMessage);
        setMessageBody(holder, chatMessage);
        setMessageInfo(chatMessage, holder);
        setMessageAuthor(holder, chatMessage);


        holder.messageContainerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasAttachments(chatMessage)) {
                    Collection<QBAttachment> attachments = chatMessage.getAttachments();
                    QBAttachment attachment = attachments.iterator().next();
                    AttachmentImageActivity.start(context, attachment.getUrl());
                } else {
                    //toggleItemInfo(holder, position);
                }
            }
        });
        holder.messageContainerLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (hasAttachments(chatMessage)) {
                    //toggleItemInfo(holder, position);
                    return true;
                }

                return false;
            }
        });
        holder.messageInfoTextView.setVisibility(View.VISIBLE);

        if (isIncoming(chatMessage) && !isRead(chatMessage)) {
            readMessage(chatMessage);
        }

        downloadMore(position);

        return convertView;
    }

    private void downloadMore(int position) {
        if (position == 0) {
            if (getCount() != previousGetCount) {
                paginationListener.downloadMore();
                previousGetCount = getCount();
            }
        }
    }

    public void setPaginationHistoryListener(PaginationHistoryListener paginationListener) {
        this.paginationListener = paginationListener;
    }

    private void toggleItemInfo(ViewHolder holder, int position) {
        boolean isMessageInfoVisible = holder.messageInfoTextView.getVisibility() == View.VISIBLE;
        holder.messageInfoTextView.setVisibility(isMessageInfoVisible ? View.GONE : View.VISIBLE);

        if (onItemInfoExpandedListener != null) {
            onItemInfoExpandedListener.onItemInfoExpanded(position);
        }
    }

    /*@Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = inflater.inflate(R.layout.view_chat_message_header, parent, false);
            holder.dateTextView = (TextView) convertView.findViewById(R.id.header_date_textview);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        QBChatMessage chatMessage = getItem(position);
        holder.dateTextView.setText(TimeUtils.getDate(chatMessage.getDateSent() * 1000));

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) holder.dateTextView.getLayoutParams();
        if (position == 0) {
            lp.topMargin = ResourceUtils.getDimen(R.dimen.chat_date_header_top_margin);
        } else {
            lp.topMargin = 0;
        }
        holder.dateTextView.setLayoutParams(lp);

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        QBChatMessage chatMessage = getItem(position);
        return TimeUtils.getDateAsHeaderId(chatMessage.getDateSent() * 1000);
    }*/

    private void setMessageBody(final ViewHolder holder, QBChatMessage chatMessage) {
        if (hasAttachments(chatMessage)) {
            Collection<QBAttachment> attachments = chatMessage.getAttachments();
            QBAttachment attachment = attachments.iterator().next();

            holder.messageBodyTextView.setVisibility(View.GONE);
            holder.attachmentImageView.setVisibility(View.VISIBLE);
            holder.attachmentProgressBar.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(attachment.getUrl())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            holder.attachmentImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                            holder.attachmentProgressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.attachmentImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            holder.attachmentProgressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    //.override(Consts.PREFERRED_IMAGE_SIZE_PREVIEW, Consts.PREFERRED_IMAGE_SIZE_PREVIEW)
                    //.dontTransform()
                    //.error(R.drawable.ic_error)
                    .into(holder.attachmentImageView);
        } else {
            holder.messageBodyTextView.setText(chatMessage.getBody());
            holder.messageBodyTextView.setVisibility(View.VISIBLE);
            holder.attachmentImageView.setVisibility(View.GONE);
            holder.attachmentProgressBar.setVisibility(View.GONE);
        }
    }

    private void setMessageAuthor(ViewHolder holder, QBChatMessage chatMessage) {
        if (isIncoming(chatMessage)) {
            QBUser sender = QbUsersHolder.getInstance().getUserById(chatMessage.getSenderId());
            if (sender != null) {
                holder.messageAuthorTextView.setText(sender.getFullName());
                holder.messageAuthorTextView.setVisibility(View.VISIBLE);
            }

            if (hasAttachments(chatMessage)) {
                holder.messageAuthorTextView.setBackgroundResource(R.drawable.shape_rectangle_semi_transparent);
                //holder.messageAuthorTextView.setTextColor(ResourceUtils.getColor(R.color.text_color_white));
            } else {
                holder.messageAuthorTextView.setBackgroundResource(0);
                //holder.messageAuthorTextView.setTextColor(ResourceUtils.getColor(R.color.text_color_white));
            }
        } else {
            holder.messageAuthorTextView.setVisibility(View.GONE);
        }
    }

    private void setMessageInfo(QBChatMessage chatMessage, ViewHolder holder) {
        holder.messageInfoTextView.setText(TimeUtils.getTime(chatMessage.getDateSent() * 1000));
    }

    @SuppressLint("RtlHardcoded")
    private void setIncomingOrOutgoingMessageAttributes(ViewHolder holder, QBChatMessage chatMessage) {
        boolean isIncoming = isIncoming(chatMessage);
        int gravity = isIncoming ? Gravity.LEFT : Gravity.RIGHT;
        holder.messageContainerLayout.setGravity(gravity);
        holder.messageInfoTextView.setGravity(gravity);

        int messageBodyContainerBgResource = isIncoming
                ? R.drawable.incoming_messages_shape
                : R.drawable.outgoing_messages_shape;
        if (hasAttachments(chatMessage)) {
            holder.messageBodyContainerLayout.setBackgroundResource(0);
            holder.messageBodyContainerLayout.setPadding(0, 0, 0, 0);
            holder.attachmentImageView.setMaskResourceId(messageBodyContainerBgResource);
        } else {
            holder.messageBodyContainerLayout.setBackgroundResource(messageBodyContainerBgResource);
        }

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.messageAuthorTextView.getLayoutParams();
        if (isIncoming && hasAttachments(chatMessage)) {
            lp.leftMargin = ResourceUtils.getDimen(R.dimen.chat_message_attachment_username_margin);
            lp.topMargin = ResourceUtils.getDimen(R.dimen.chat_message_attachment_username_top_margin);
        } else if (isIncoming) {
            lp.leftMargin = ResourceUtils.getDimen(R.dimen.chat_message_username_margin);
            lp.topMargin = 0;
        }
        holder.messageAuthorTextView.setLayoutParams(lp);

        int textColorResource = isIncoming
                ? R.color.text_color_white
                : R.color.colorPrimary;
        holder.messageBodyTextView.setTextColor(ResourceUtils.getColor(textColorResource));

        RelativeLayout.LayoutParams avatarParams = (RelativeLayout.LayoutParams) holder.avatarImageView.getLayoutParams();
        RelativeLayout.LayoutParams messageBodyContainerParams = (RelativeLayout.LayoutParams) holder.messageBodyContainerLayout.getLayoutParams();
        if (isIncoming) {
            avatarParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            avatarParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            messageBodyContainerParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            messageBodyContainerParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        } else {
            avatarParams.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
            avatarParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            messageBodyContainerParams.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
            messageBodyContainerParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        }
        holder.avatarImageView.setLayoutParams(avatarParams);
        holder.messageBodyContainerLayout.setLayoutParams(messageBodyContainerParams);
    }

    private boolean hasAttachments(QBChatMessage chatMessage) {
        Collection<QBAttachment> attachments = chatMessage.getAttachments();
        return attachments != null && !attachments.isEmpty();
    }

    private boolean isIncoming(QBChatMessage chatMessage) {
        QBUser currentUser = ChatHelper.getCurrentUser();
        return chatMessage.getSenderId() != null && !chatMessage.getSenderId().equals(currentUser.getId());
    }

    private boolean isRead(QBChatMessage chatMessage) {
        Integer currentUserId = ChatHelper.getCurrentUser().getId();
        return !CollectionsUtil.isEmpty(chatMessage.getReadIds()) && chatMessage.getReadIds().contains(currentUserId);
    }

    private void readMessage(QBChatMessage chatMessage) {
        try {
            chatDialog.readMessage(chatMessage);
        } catch (XMPPException | SmackException.NotConnectedException e) {
            Log.w(TAG, e);
        }
    }

    private static class HeaderViewHolder {
        public TextView dateTextView;
    }

    private static class ViewHolder {
        public RoundedImageView avatarImageView;
        public TextView messageBodyTextView;
        public TextView messageAuthorTextView;
        public TextView messageInfoTextView;
        public LinearLayout messageContainerLayout;
        public RelativeLayout messageBodyContainerLayout;
        public MaskedImageView attachmentImageView;
        public ProgressBar attachmentProgressBar;
    }

    public interface OnItemInfoExpandedListener {
        void onItemInfoExpanded(int position);
    }
}
