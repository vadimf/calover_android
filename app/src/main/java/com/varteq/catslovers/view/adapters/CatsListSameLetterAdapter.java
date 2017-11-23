package com.varteq.catslovers.view.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.varteq.catslovers.R;
import com.varteq.catslovers.model.CatProfile;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CatsListSameLetterAdapter extends RecyclerView.Adapter<CatsListSameLetterAdapter.CatProfileViewHolder> {

    private OnCatClickListener externalClickListener;
    private List<CatProfile> personList;

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

    @Override
    public void onBindViewHolder(CatProfileViewHolder viewHolder, int i) {
        CatProfile catProfile = personList.get(i);
        if (catProfile.getAvatarUri() != null)
            viewHolder.catAvatarImageView.setImageURI(catProfile.getAvatarUri());
        else
            viewHolder.catAvatarImageView.setImageResource(R.drawable.ic_person);

        viewHolder.catNameTextView.setText(catProfile.getPetName());
    }

    @Override
    public CatProfileViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_cat_same_letter, viewGroup, false);

        itemView.setOnClickListener(internalClickListener);
        return new CatProfileViewHolder(itemView);
    }

    public class CatProfileViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.cat_avatar_RoundedImageView)
        RoundedImageView catAvatarImageView;
        @BindView(R.id.cat_name_TextView)
        TextView catNameTextView;

        public CatProfileViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    public interface OnCatClickListener {
        void onCatClicked(CatProfile catProfile);
    }
}
