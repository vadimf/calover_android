package com.varteq.catslovers.view.adapters.info_window_adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.varteq.catslovers.R;
import com.varteq.catslovers.utils.Utils;

public class BusinessInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private View businessMarkerDialogView;
    private TextView nameTextView;
    private TextView descriptionTextView;
    private TextView addressTextView;
    private boolean show;

    public BusinessInfoWindowAdapter(Context context) {
        businessMarkerDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_business_marker, null);
        nameTextView = businessMarkerDialogView.findViewById(R.id.textView_dialog_name);
        addressTextView = businessMarkerDialogView.findViewById(R.id.textView_dialog_address);
        descriptionTextView = businessMarkerDialogView.findViewById(R.id.textView_dialog_description);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        if (show)
            return businessMarkerDialogView;
         else
            return null;
    }

    public void setShowWindow(boolean show) {
        this.show = show;
    }

    public void setValues(String name, String address, String description) {
        setName(name);
        setAddress(Utils.splitAddress(address, 3));
        setDescription(description);
    }

    public void setName(String name) {
        nameTextView.setText(name);
    }

    public void setAddress(String adress) {
        addressTextView.setText(adress);
    }

    public void setDescription(String description) {
        descriptionTextView.setText(description);
    }

}
