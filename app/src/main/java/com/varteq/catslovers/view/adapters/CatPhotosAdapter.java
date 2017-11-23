package com.varteq.catslovers.view.adapters;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.varteq.catslovers.R;

import java.util.List;

public class CatPhotosAdapter extends RecyclerView.Adapter<CatPhotosAdapter.PhotoViewHolder> {

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

    public CatPhotosAdapter(List<Uri> photoList, OnImageClickListener externalClickListener) {
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
                inflate(R.layout.card_cat_photo, viewGroup, false);

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
