package com.varteq.catslovers.view.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.varteq.catslovers.R;
import com.varteq.catslovers.utils.Toaster;

import butterknife.BindView;
import butterknife.ButterKnife;


public class WeightDialog {

    @BindView(R.id.title_TextView)
    TextView textView;
    @BindView(R.id.kg_numberPicker)
    NumberPicker kgNumberPicker;
    @BindView(R.id.g_numberPicker)
    NumberPicker gramsNumberPicker;
    @BindView(R.id.dialog_ok_button)
    Button positiveButton;
    @BindView(R.id.dialog_cancel_button)
    Button negativeButton;

    AlertDialog.Builder builder;
    AlertDialog dialog;

    private int MAX_KG = 49;
    private int MAX_GRAMS = 9;

    public WeightDialog(Context context, Float weight, WeightDialog.OnClickListener onClickListener) {

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_weight, null, false);
        ButterKnife.bind(this, dialogView);

        //setTitleText(titleText);
        positiveButton.setOnClickListener(view -> {
            if (kgNumberPicker.getValue() == 0 && gramsNumberPicker.getValue() == 0) {
                Toaster.shortToast("Min weight is 100 grams");
                gramsNumberPicker.setValue(1);
                return;
            }
            onClickListener.onWeightChanged(getWeight(kgNumberPicker.getValue(), gramsNumberPicker.getValue()));
            dialog.dismiss();
        });
        negativeButton.setOnClickListener(view -> dialog.dismiss());

        kgNumberPicker.setMinValue(0);
        kgNumberPicker.setMaxValue(MAX_KG);
        gramsNumberPicker.setMinValue(0);
        gramsNumberPicker.setMaxValue(MAX_GRAMS);

        initPickers(weight);

        builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        dialog = builder.show();
    }

    private void initPickers(Float weight) {
        if (weight == null || weight <= 0) {
            kgNumberPicker.setValue(0);
            gramsNumberPicker.setValue(1);
            return;
        }

        int kg = Math.min(MAX_KG, Math.max(0, (int) weight.floatValue()));
        kgNumberPicker.setValue(kg);
        int g = (int) ((weight - (int) weight.floatValue()) * 10);
        int grams = Math.min(MAX_GRAMS, Math.max(0, g));
        gramsNumberPicker.setValue(grams);
    }

    private float getWeight(int kgs, int grams) {
        return (float) kgs + (float) grams * 0.1f;
    }

    public void setTitleText(String textViewText) {
        textView.setText(textViewText);
    }

    public void dismiss() {
        dialog.dismiss();
    }

    public void show() {
        dialog = builder.show();
    }

    public interface OnClickListener {

        void onWeightChanged(float weight);
    }

}
