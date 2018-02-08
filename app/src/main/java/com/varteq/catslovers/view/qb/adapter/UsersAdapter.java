package com.varteq.catslovers.view.qb.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.makeramen.roundedimageview.RoundedImageView;
import com.quickblox.chat.QBChatService;
import com.quickblox.users.model.QBUser;
import com.varteq.catslovers.R;
import com.varteq.catslovers.utils.ResourceUtils;

import java.util.List;

public class UsersAdapter extends BaseListAdapter<QBUser> {

    protected QBUser currentUser;

    public UsersAdapter(Context context, List<QBUser> users) {
        super(context, users);
        currentUser = QBChatService.getInstance().getUser();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        QBUser user = getItem(position);

        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_user, parent, false);
            holder = new ViewHolder();
            holder.userImageView = convertView.findViewById(R.id.image_user);
            holder.loginTextView = (TextView) convertView.findViewById(R.id.text_user_login);
            holder.userCheckBox = (CheckBox) convertView.findViewById(R.id.checkbox_user);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (isUserMe(user)) {
            holder.loginTextView.setText(context.getString(R.string.placeholder_username_you, user.getFullName()));
        } else {
            holder.loginTextView.setText(user.getFullName());
        }

        if (isAvailableForSelection(user)) {
            holder.loginTextView.setTextColor(ResourceUtils.getColor(R.color.text_color_black));
        } else {
            holder.loginTextView.setTextColor(ResourceUtils.getColor(R.color.text_color_medium_grey));
        }

        boolean isAvatarExist = user.getCustomData() != null && !user.getCustomData().isEmpty();
        Glide.with(convertView)
                .load(isAvatarExist ? user.getCustomData() : R.drawable.user_avatar_default)
                .apply(new RequestOptions().error(R.drawable.user_avatar_default))
                .into(holder.userImageView);
        holder.userCheckBox.setVisibility(View.GONE);

        return convertView;
    }

    protected boolean isUserMe(QBUser user) {
        return currentUser != null && currentUser.getId().equals(user.getId());
    }

    protected boolean isAvailableForSelection(QBUser user) {
        return currentUser == null || !currentUser.getId().equals(user.getId());
    }

    protected static class ViewHolder {
        RoundedImageView userImageView;
        TextView loginTextView;
        CheckBox userCheckBox;
    }
}
