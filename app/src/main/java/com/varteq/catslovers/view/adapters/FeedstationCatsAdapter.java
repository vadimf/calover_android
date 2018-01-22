package com.varteq.catslovers.view.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.makeramen.roundedimageview.RoundedImageView;
import com.varteq.catslovers.R;
import com.varteq.catslovers.model.CatProfile;
import com.varteq.catslovers.model.PhotoWithPreview;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FeedstationCatsAdapter extends RecyclerView.Adapter<FeedstationCatsAdapter.FeedstationCatsViewHolder> {

    private FeedstationCatsAdapter.OnCatClickListener externalClickListener;
    private List<CatProfile> catsList;
    private View.OnClickListener internalClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
            int itemPosition = lp.getViewLayoutPosition();
            if (externalClickListener != null) {
                externalClickListener.onCatClicked(catsList.get(itemPosition));
            }
        }
    };

    public FeedstationCatsAdapter(List<CatProfile> catsList, OnCatClickListener externalClickListener) {
        this.externalClickListener = externalClickListener;
        this.catsList = catsList;
    }

    @Override
    public FeedstationCatsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.card_feedstation_cat, parent, false);

        itemView.setOnClickListener(internalClickListener);
        return new FeedstationCatsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FeedstationCatsViewHolder holder, int position) {
        CatProfile catProfile = catsList.get(position);
        PhotoWithPreview photoWithPreview = catProfile.getAvatar();
        if (photoWithPreview != null)
            Glide.with(holder.itemView)
                    .load(catProfile.getAvatar().getThumbnail())
                    .apply(new RequestOptions().centerCrop())
                    .into(holder.catAvatarImageView);
        else
            holder.catAvatarImageView.setImageResource(R.drawable.ic_person);

        holder.catNameTextView.setText(catProfile.getPetName());
        holder.catAvatarImageView.setBorderColor(
                holder.itemView.getContext().getResources().getColor(R.color.colorPrimary));
    }

    @Override
    public int getItemCount() {
        if (catsList != null)
            return catsList.size();
        else return 0;
    }

    public class FeedstationCatsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.cat_avatar_RoundedImageView)
        RoundedImageView catAvatarImageView;
        @BindView(R.id.cat_name_TextView)
        TextView catNameTextView;

        public FeedstationCatsViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    public interface OnCatClickListener {
        void onCatClicked(CatProfile catProfile);
    }

}
