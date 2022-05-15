package com.example.watchdog.utils;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.example.watchdog.R;

public class EditTextUtils {
    @SuppressLint("ClickableViewAccessibility")
    public static void addRightCancelDrawable(EditText edt) {
        hideRightDrawableIfEmpty(edt.getText(), edt);

        edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hideRightDrawableIfEmpty(s, edt);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (edt.getText().length()==0) return false;
                    if (event.getRawX() >= (edt.getRight() - edt.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        edt.getText().clear();
                        edt.requestFocus();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public static void hideRightDrawableIfEmpty(CharSequence s, EditText edt) {
        if (s.length() > 0) {
            edt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_cancel, 0);
        } else {
            edt.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }
}
