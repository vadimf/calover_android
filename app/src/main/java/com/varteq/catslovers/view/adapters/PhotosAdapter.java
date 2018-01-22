package com.varteq.catslovers.view.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.varteq.catslovers.R;
import com.varteq.catslovers.model.PhotoWithPreview;

import java.util.List;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.PhotoViewHolder> {

    private final int THUMBSIZE = 250;
    private OnImageClickListener externalClickListener;
    private OnImageLongClickListener externalLongClickListener;
    private List<PhotoWithPreview> photoList;
    private View.OnClickListener internalClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
            int itemPosition = lp.getViewLayoutPosition();
            if (externalClickListener != null)
                externalClickListener.onImageClicked(photoList.get(itemPosition).getPhoto());
        }
    };
    private View.OnLongClickListener internalLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
            int itemPosition = lp.getViewLayoutPosition();
            if (externalLongClickListener != null && isEditMode)
                externalLongClickListener.onImageClicked(itemPosition);
            return true;
        }
    };
    private boolean isEditMode;

    public PhotosAdapter(List<PhotoWithPreview> photoList, OnImageClickListener externalClickListener,
                         OnImageLongClickListener externalLongClickListener) {
        this.externalClickListener = externalClickListener;
        this.externalLongClickListener = externalLongClickListener;
        this.photoList = photoList;
    }

    public void switchToEditMode() {
        isEditMode = true;
    }

    public void switchToViewMode() {
        isEditMode = false;
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    @Override
    public void onBindViewHolder(PhotoViewHolder photoViewHolder, int i) {
        Glide.with(photoViewHolder.itemView)
                //.asBitmap()
                .load(photoList.get(i).getThumbnail())
                .apply(new RequestOptions().override(THUMBSIZE, THUMBSIZE))
                .into(photoViewHolder.imageView);
                /*.into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        if (resource.getWidth() > 300)
                            photoViewHolder.imageView.setImageBitmap(ThumbnailUtils.extractThumbnail(resource,
                                    THUMBSIZE, THUMBSIZE));
                        else
                            photoViewHolder.imageView.setImageBitmap(resource);
                    }
                });*/

        //Uri imageURI = photoList.get(i);
        //photoViewHolder.imageView.setImageURI(imageURI);
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_cat_photo, viewGroup, false);

        itemView.setOnClickListener(internalClickListener);
        itemView.setOnLongClickListener(internalLongClickListener);
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
        void onImageClicked(String imagePath);
    }

    public interface OnImageLongClickListener {
        void onImageClicked(int position);
    }

    public List<PhotoWithPreview> getPhotoList(){
        return photoList;
    }

}
