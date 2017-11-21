package com.varteq.catslovers;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

public class GroupPartnersAdapter extends RecyclerView.Adapter<GroupPartnersAdapter.PhotoViewHolder> {

    private OnImageClickListener externalClickListener;
    private List<Uri> photoList;
    private View.OnClickListener internalClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
            int itemPosition = lp.getViewLayoutPosition();
            if (externalClickListener != null)
                externalClickListener.onImageClicked(photoList.get(itemPosition));
        }
    };

    public GroupPartnersAdapter(List<Uri> photoList, OnImageClickListener externalClickListener) {
        this.externalClickListener = externalClickListener;
        this.photoList = photoList;
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    @Override
    public void onBindViewHolder(PhotoViewHolder photoViewHolder, int i) {
        Uri imageURI = photoList.get(i);
        photoViewHolder.imageView.setImageURI(imageURI);
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_group_partner, viewGroup, false);

        itemView.setOnClickListener(internalClickListener);
        return new PhotoViewHolder(itemView);
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {
        protected ImageView imageView;

        public PhotoViewHolder(View v) {
            super(v);
            imageView = v.findViewById(R.id.cat_photo_imageView);
        }
    }

    public interface OnImageClickListener {
        void onImageClicked(Uri imageUri);
    }
}
