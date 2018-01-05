package com.varteq.catslovers.view.adapters;

import android.content.Context;
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
import com.varteq.catslovers.model.CatProfile;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CatsListSameLetterAdapter extends RecyclerView.Adapter<CatsListSameLetterAdapter.CatProfileViewHolder> {

    private OnCatClickListener externalClickListener;
    private List<CatProfile> personList;
    private boolean isMyCats;

    private View.OnClickListener internalClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
            int itemPosition = lp.getViewLayoutPosition();
            if (externalClickListener != null) {
                externalClickListener.onCatClicked(personList.get(itemPosition));
            }
        }
    };

    public CatsListSameLetterAdapter(List<CatProfile> personList, OnCatClickListener externalClickListener) {
        this.externalClickListener = externalClickListener;
        this.personList = personList;
    }

    @Override
    public int getItemCount() {
        return personList.size();
    }

    public void setMyCats(boolean myCats) {
        isMyCats = myCats;
    }

    @Override
    public void onBindViewHolder(CatProfileViewHolder viewHolder, int i) {
        CatProfile catProfile = personList.get(i);
        Context context = viewHolder.itemView.getContext();
        if (catProfile.getAvatar() != null) {
            Glide.with(viewHolder.itemView)
                    .load(catProfile.getAvatar().getThumbnail())
                    .apply(new RequestOptions().centerCrop())
                    .into(viewHolder.catAvatarImageView);
            //viewHolder.catAvatarImageView.setImageURI(catProfile.getAvatar());
        }
        else
            viewHolder.catAvatarImageView.setImageResource(R.drawable.ic_person);

        viewHolder.catNameTextView.setText(catProfile.getPetName());
        if (isMyCats) {
            viewHolder.catAvatarImageView.setBorderColor(context.getResources().getColor(R.color.colorPrimary));
            viewHolder.starImageView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.catAvatarImageView.setBorderColor(context.getResources().getColor(R.color.transparent));
            viewHolder.starImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public CatProfileViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_cat_same_letter, viewGroup, false);

        itemView.setOnClickListener(internalClickListener);
        return new CatProfileViewHolder(itemView);
    }

    public List<CatProfile> getPersonList() {
        return personList;
    }

    public class CatProfileViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.cat_avatar_RoundedImageView)
        RoundedImageView catAvatarImageView;
        @BindView(R.id.cat_name_TextView)
        TextView catNameTextView;
        @BindView(R.id.star_ImageView)
        ImageView starImageView;

        public CatProfileViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    public interface OnCatClickListener {
        void onCatClicked(CatProfile catProfile);
    }
}
