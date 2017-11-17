package com.varteq.catslovers;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

public class CatPhotosAdapter extends RecyclerView.Adapter<CatPhotosAdapter.PhotoViewHolder> {

    private List<Uri> photoList;

    public CatPhotosAdapter(List<Uri> photoList) {
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

        return new PhotoViewHolder(itemView);
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {
        protected ImageView imageView;

        public PhotoViewHolder(View v) {
            super(v);
            imageView = (ImageView) v.findViewById(R.id.cat_photo_imageView);
        }
    }
}
