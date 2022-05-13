package com.example.watchdog;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.watchdog.interfaces.DialogCloseListener;
import com.example.watchdog.models.Stock;
import com.example.watchdog.utils.DbHandler;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Objects;

public class AddNewTask extends BottomSheetDialogFragment {

    public static final String TAG = "ActionBottomDialog";
    private EditText newSymbolText;
    private EditText newWarningText;
    private Button newStockSaveButton;

    public static AddNewTask newInstance() {
        return new AddNewTask();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(STYLE_NORMAL, R.style.DialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_task, container, false);
        Objects.requireNonNull(getDialog()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        newSymbolText = requireView().findViewById(R.id.newSymbolText);
        newWarningText = requireView().findViewById(R.id.newWarning);
        newStockSaveButton = requireView().findViewById(R.id.newSaveButton);

        boolean isUpdate = false;
        final Bundle bundle = getArguments();
        if (bundle != null) {
            isUpdate = true;
            Stock stock = (Stock) bundle.get("stock");
            assert stock != null;
            newSymbolText.setText(stock.getSymbol());
            newWarningText.setText(String.valueOf(stock.getWarningPrice()));

            if (stock.getSymbol().length() > 0)
                newStockSaveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark));
        }

        DbHandler db = new DbHandler(getActivity());
        db.openDb();

        final boolean finalIsUpdate = isUpdate;
        newStockSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(newSymbolText.getText().toString().equals("")){
                    newSymbolText.setError("Require");
                    return;
                }

                if(newWarningText.getText().toString().equals("")){
                    newSymbolText.setError("Require");
                    return;
                }

                String symbol = newSymbolText.getText().toString();
                Double warning =Double.parseDouble( newWarningText.getText().toString());
                if (finalIsUpdate) {
                    db.updateStock(bundle.getInt("id"), symbol,warning);
                } else {
                    Stock stock = new Stock();
                    stock.setSymbol(symbol);
                    stock.setWarningPrice(warning);
                    stock.setStatus(0);
                    db.insertStock(stock);
                }
                dismiss();
            }
        });
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        Activity activity = getActivity();
        if (activity instanceof DialogCloseListener) {
            ((DialogCloseListener) activity).handleDialogClose(dialog);
        }
    }
}
