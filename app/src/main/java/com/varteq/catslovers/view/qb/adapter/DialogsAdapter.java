package com.varteq.catslovers.view.qb.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.makeramen.roundedimageview.RoundedImageView;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.users.model.QBUser;
import com.varteq.catslovers.R;
import com.varteq.catslovers.utils.ChatHelper;
import com.varteq.catslovers.utils.ResourceUtils;
import com.varteq.catslovers.utils.TimeUtils;
import com.varteq.catslovers.utils.UiUtils;
import com.varteq.catslovers.utils.qb.QbDialogUtils;

import java.util.List;

public class DialogsAdapter extends BaseSelectableListAdapter<QBChatDialog> {

    private static final String EMPTY_STRING = "";

    public DialogsAdapter(Context context, List<QBChatDialog> dialogs) {
        super(context, dialogs);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_dialog, parent, false);

            holder = new ViewHolder();
            holder.rootLayout = (ViewGroup) convertView.findViewById(R.id.root);
            holder.nameTextView = (TextView) convertView.findViewById(R.id.text_dialog_name);
            holder.lastMessageTextView = (TextView) convertView.findViewById(R.id.text_dialog_last_message);
            holder.lastMessageTimeTextView = (TextView) convertView.findViewById(R.id.text_dialog_last_message_time);
            holder.dialogImageView = convertView.findViewById(R.id.image_dialog_icon);
            holder.unreadCounterTextView = (TextView) convertView.findViewById(R.id.text_dialog_unread_count);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        QBChatDialog dialog = getItem(position);
        if (dialog.getType().equals(QBDialogType.PRIVATE)) {
            QBUser user = ChatHelper.getInstance().getInterlocutorUserFromDialog(dialog);
            boolean isAvatarExist = user != null && user.getCustomData() != null && !user.getCustomData().isEmpty();
            Glide.with(holder.rootLayout)
                    .load(isAvatarExist ? user.getCustomData() : R.drawable.user_avatar_default)
                    .apply(new RequestOptions().error(R.drawable.user_avatar_default))
                    .into(holder.dialogImageView);

        } else {
            holder.dialogImageView.setBackgroundDrawable(UiUtils.getGreyCircleDrawable());
            holder.dialogImageView.setImageResource(R.drawable.ic_chat_group);
        }

        holder.nameTextView.setText(QbDialogUtils.getDialogName(dialog));
        holder.lastMessageTextView.setText(prepareTextLastMessage(dialog));
        holder.lastMessageTimeTextView.setText(TimeUtils.getDateAsEEEMMddyyyyHHmmaa(dialog.getLastMessageDateSent() * 1000L));

        int unreadMessagesCount = getUnreadMsgCount(dialog);
        if (unreadMessagesCount == 0) {
            holder.unreadCounterTextView.setVisibility(View.GONE);
        } else {
            holder.unreadCounterTextView.setVisibility(View.VISIBLE);
            holder.unreadCounterTextView.setText(String.valueOf(unreadMessagesCount > 99 ? 99 : unreadMessagesCount));
        }

        holder.rootLayout.setBackgroundColor(isItemSelected(position) ? ResourceUtils.getColor(R.color.selected_list_item_color) :
                ResourceUtils.getColor(android.R.color.transparent));

        return convertView;
    }

    private int getUnreadMsgCount(QBChatDialog chatDialog) {
        Integer unreadMessageCount = chatDialog.getUnreadMessageCount();
        if (unreadMessageCount == null) {
            return 0;
        } else {
            return unreadMessageCount;
        }
    }

    private boolean isLastMessageAttachment(QBChatDialog dialog) {
        String lastMessage = dialog.getLastMessage();
        Integer lastMessageSenderId = dialog.getLastMessageUserId();
        return TextUtils.isEmpty(lastMessage) && lastMessageSenderId != null;
    }

    private String prepareTextLastMessage(QBChatDialog chatDialog) {
        if (isLastMessageAttachment(chatDialog)) {
            return context.getString(R.string.chat_attachment);
        } else {
            return chatDialog.getLastMessage();
        }
    }

    private static class ViewHolder {
        ViewGroup rootLayout;
        RoundedImageView dialogImageView;
        TextView nameTextView;
        TextView lastMessageTextView;
        TextView lastMessageTimeTextView;
        TextView unreadCounterTextView;
    }
}
