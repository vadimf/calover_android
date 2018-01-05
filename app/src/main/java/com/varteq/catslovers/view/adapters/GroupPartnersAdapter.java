package com.varteq.catslovers.view.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.makeramen.roundedimageview.RoundedImageView;
import com.varteq.catslovers.R;
import com.varteq.catslovers.model.GroupPartner;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GroupPartnersAdapter extends RecyclerView.Adapter<GroupPartnersAdapter.GroupPartnerViewHolder> {

    private OnPersonClickListener externalClickListener;
    private List<GroupPartner> personList;
    private String addNewPartnerString = "addNewPartnerString";
    private final GroupPartner addNewPartnerView = new GroupPartner(addNewPartnerString, "", GroupPartner.Status.JOINED, false);
    private View.OnClickListener internalClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
            int itemPosition = lp.getViewLayoutPosition();
            if (externalClickListener != null) {
                if (personList.get(itemPosition).getAvatar() != null && personList.get(itemPosition).getAvatar().equals(addNewPartnerString))
                    externalClickListener.onAddPerson();
                else
                    externalClickListener.onPersonClicked(personList.get(itemPosition));
            }
        }
    };

    public GroupPartnersAdapter(List<GroupPartner> personList, boolean isEditMode, OnPersonClickListener externalClickListener) {
        this.externalClickListener = externalClickListener;
        this.personList = personList;

        if (isEditMode)
            switchToEditMode();
    }

    public void switchToEditMode() {
        int lastPos = personList.size() - 1;
        if (personList.size() < 1 || personList.get(lastPos) != addNewPartnerView) {
            personList.add(lastPos + 1, addNewPartnerView);
            notifyItemInserted(lastPos + 1);
        }
    }

    public void switchToViewMode() {
        if (personList.size() < 1) return;
        int lastPos = personList.size() - 1;
        if (personList.get(lastPos) == addNewPartnerView) {
            personList.remove(lastPos);
            notifyItemRemoved(lastPos);
        }
    }

    @Override
    public int getItemCount() {
        return personList.size();
    }

    @Override
    public void onBindViewHolder(GroupPartnerViewHolder viewHolder, int i) {
        GroupPartner person = personList.get(i);
        if (person != addNewPartnerView) {
            if (person.getAvatar() != null)
                Glide.with(viewHolder.itemView)
                        .load(person.getAvatar())
                        .apply(new RequestOptions().centerCrop())
                        .into(viewHolder.partnerAvatarImageView);
            else
                viewHolder.partnerAvatarImageView.setImageResource(R.drawable.ic_person);
        }
        else
            viewHolder.partnerAvatarImageView.setImageResource(R.drawable.ic_add_group_partner);
        viewHolder.partnerNameTextView.setText(person.getName());
        viewHolder.isAdminImageView.setVisibility(person.isAdmin() ? View.VISIBLE : View.INVISIBLE);

        if (person.getStatus().equals(GroupPartner.Status.JOINED))
            viewHolder.partnerAvatarImageView.setBorderColor(
                    viewHolder.itemView.getContext().getResources().getColor(R.color.colorPrimary));
        else
            viewHolder.partnerAvatarImageView.setBorderColor(
                    viewHolder.itemView.getContext().getResources().getColor(R.color.colorPrimaryLight));
    }

    @Override
    public GroupPartnerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_group_partner, viewGroup, false);

        itemView.setOnClickListener(internalClickListener);
        return new GroupPartnerViewHolder(itemView);
    }

    public class GroupPartnerViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.partner_avatar_RoundedImageView)
        RoundedImageView partnerAvatarImageView;
        @BindView(R.id.is_admin_ImageView)
        ImageView isAdminImageView;
        @BindView(R.id.partner_name_TextView)
        TextView partnerNameTextView;

        public GroupPartnerViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    public interface OnPersonClickListener {
        void onPersonClicked(GroupPartner groupPartner);
        void onAddPerson();
    }
}
