package com.varteq.catslovers.view.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.ValueBar;
import com.varteq.catslovers.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ColorPickerDialog extends AlertDialog {

    @BindView(R.id.color_picker)
    ColorPicker colorPickerView;
    @BindView(R.id.opacitybar)
    OpacityBar opacityBar;
    @BindView(R.id.valuebar)
    ValueBar valuebar;
    @BindView(R.id.color_picker_ok_button)
    Button okButton;
    @BindView(R.id.color_picker_cancel_button)
    Button cancelButton;

    private final ColorPicker.OnColorSelectedListener onColorSelectedListener;

    public ColorPickerDialog(Context context, int initialColor, ColorPicker.OnColorSelectedListener onColorSelectedListener) {
        super(context, R.style.PrimaryDialog);

        this.onColorSelectedListener = onColorSelectedListener;

        /*RelativeLayout relativeLayout = new RelativeLayout(context);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        colorPickerView = new ColorPicker(context);
        colorPickerView.setColor(initialColor);

        relativeLayout.addView(colorPickerView, layoutParams);

        setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok), onClickListener);
        setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel), onClickListener);

        setView(relativeLayout);*/

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_color_picker);
        ButterKnife.bind(this);
        colorPickerView.setColor(Color.rgb(76, 47, 0));
        colorPickerView.setOldCenterColor(Color.rgb(76, 47, 0));
        colorPickerView.addOpacityBar(opacityBar);
        colorPickerView.addValueBar(valuebar);
        okButton.setOnClickListener(view -> {
            int selectedColor = colorPickerView.getColor();
            onColorSelectedListener.onColorSelected(selectedColor);
            dismiss();
        });

        cancelButton.setOnClickListener(view -> dismiss());
    }

    public int getCurrentColor() {
        return colorPickerView.getColor();
    }

    private OnClickListener onClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case BUTTON_POSITIVE:
                    int selectedColor = colorPickerView.getColor();
                    onColorSelectedListener.onColorSelected(selectedColor);
                    break;
                case BUTTON_NEGATIVE:
                    dialog.dismiss();
                    break;
            }
        }
    };
}
