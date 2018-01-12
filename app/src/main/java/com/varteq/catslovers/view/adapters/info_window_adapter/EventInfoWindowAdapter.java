package com.varteq.catslovers.view.adapters.info_window_adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;
import com.varteq.catslovers.R;
import com.varteq.catslovers.model.Event;


public class EventInfoWindowAdapter implements InfoWindowAdapter {

    private View eventMarkerDialogView;
    private TextView addressTextView;
    private TextView eventTypeNameTextView;
    private TextView dateTextView;
    private boolean show;
    private Event.Type type;
    private Context context;

    public EventInfoWindowAdapter(Context context) {
        this.context = context;
        eventMarkerDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_event_marker, null);
        addressTextView = eventMarkerDialogView.findViewById(R.id.textView_dialog_address);
        dateTextView = eventMarkerDialogView.findViewById(R.id.textView_dialog_date);
        eventTypeNameTextView = eventMarkerDialogView.findViewById(R.id.textView_dialog_event);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        if (show) {
            switch (type) {
                case WARNING:
                    eventTypeNameTextView.setTextColor(context.getResources().getColor(R.color.orange));
                    break;
                case EMERGENCY:
                    eventTypeNameTextView.setTextColor(context.getResources().getColor(R.color.rusty_red));
                    break;
            }
            return eventMarkerDialogView;
        } else
            return null;
    }

    public void setShowWindow(boolean show) {
        this.show = show;
    }

    public void setValues(String address, String date, String eventTypeName, Event.Type type) {
        setAddress(address);
        setDate(date);
        setEventTypeName(eventTypeName);
        this.type = type;
    }

    public void setAddress(String address) {
        addressTextView.setText(address);
    }

    public void setDate(String date) {
        dateTextView.setText(date);
    }

    public void setEventTypeName(String eventTypeName) {
        eventTypeNameTextView.setText(eventTypeName);
    }

    public View getEventMarkerDialogView() {
        return eventMarkerDialogView;
    }

    public void setEventMarkerDialogView(View eventMarkerDialogView) {
        this.eventMarkerDialogView = eventMarkerDialogView;
    }
}
