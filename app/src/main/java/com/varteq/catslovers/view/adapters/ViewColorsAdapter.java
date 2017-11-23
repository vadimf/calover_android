package com.varteq.catslovers.view.adapters;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.makeramen.roundedimageview.RoundedImageView;
import com.varteq.catslovers.R;
import com.varteq.catslovers.Utils;

import java.util.List;

public class ViewColorsAdapter extends RecyclerView.Adapter<ViewColorsAdapter.PhotoViewHolder> {

    private List<Integer> colorsList;

    public ViewColorsAdapter(List<Integer> colorsList) {
        this.colorsList = colorsList;
    }

    @Override
    public int getItemCount() {
        return colorsList.size();
    }

    @Override
    public void onBindViewHolder(PhotoViewHolder photoViewHolder, int i) {
        Bitmap bitmapWithColor = Utils.getBitmapWithColor(colorsList.get(i));
        photoViewHolder.imageView.setImageBitmap(bitmapWithColor);
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_view_color, viewGroup, false);

        return new PhotoViewHolder(itemView);
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {
        protected RoundedImageView imageView;

        public PhotoViewHolder(View v) {
            super(v);
            imageView = v.findViewById(R.id.view_color_roundedImageView);
        }
    }
}
