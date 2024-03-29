package com.standalone.watchdog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.standalone.watchdog.adapter.StockSearchAdapter;
import com.standalone.watchdog.interfaces.DialogCloseListener;
import com.standalone.watchdog.models.Stock;
import com.standalone.watchdog.models.StockInfo;
import com.standalone.watchdog.utils.DbHandler;

import java.util.List;
import java.util.Objects;

public class AlertForm extends BottomSheetDialogFragment {

    public static final String TAG = AlertForm.class.getSimpleName();
    private AutoCompleteTextView newSymbolText;
    private EditText newWarningText;
    private int stockType;
    private final List<StockInfo> stockDex;

    public AlertForm(List<StockInfo> stockDex) {
        this.stockDex = stockDex;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(STYLE_NORMAL, R.style.DialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.alert_form, container, false);
        Objects.requireNonNull(getDialog()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        StockSearchAdapter adapter = new StockSearchAdapter(requireContext(), R.layout.suggestion_item, stockDex);
        newSymbolText = view.findViewById(R.id.newSymbolText);
        newSymbolText.setAdapter(adapter);

        newWarningText = view.findViewById(R.id.newWarning);

        newSymbolText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                newWarningText.requestFocus();
                newWarningText.setSelection(newWarningText.getText().length());
            }
        });
        addCancelButton(newSymbolText, R.id.fromNewSymbol);
        addCancelButton(newWarningText, R.id.fromNewWarning);

        Button newStockSaveButton = view.findViewById(R.id.newSaveButton);
        RadioGroup radioGroup = view.findViewById(R.id.radio_grp);
        radioGroup.check(R.id.radio_less);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_less:
                        stockType = Stock.LESS_THAN;
                        break;
                    case R.id.radio_greater:
                        stockType = Stock.GREATER_THAN;
                        break;
                }
            }
        });

        boolean isUpdate = false;
        final Bundle bundle = getArguments();
        if (bundle != null) {
            isUpdate = true;
            Stock stock = (Stock) bundle.get("stock");
            assert stock != null;
            newSymbolText.setText(stock.getSymbol());
            newWarningText.setText(String.valueOf(stock.getWarningPrice()));
            int res;
            switch (stock.getType()) {
                case Stock.GREATER_THAN:
                    res = R.id.radio_greater;
                    break;
                case Stock.LESS_THAN:
                default:
                    res = R.id.radio_less;
                    break;
            }
            radioGroup.check(res);
        } else {
            newSymbolText.requestFocus();
        }

        DbHandler db = new DbHandler(getActivity());
        db.openDb();

        final boolean finalIsUpdate = isUpdate;
        newStockSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check and get SYMBOL from the text field
                if (newSymbolText.getText().toString().equals("")) {
                    newSymbolText.setError(Constant.REQUIRE);
                    return;
                }

                String symbol = newSymbolText.getText().toString();
                StockInfo stockInfo = stockDex.stream().filter(s -> symbol.equals(s.getSymbol())).findFirst().orElse(null);
                if (stockInfo == null) {
                    newSymbolText.setError(Constant.INVALID_SYMBOL);
                    return;
                }

                // check and get PRICE from the text field
                if (newWarningText.getText().toString().equals("")) {
                    newWarningText.setError(Constant.REQUIRE);
                    return;
                }

                double warning = 0;
                try {
                    warning = Double.parseDouble(newWarningText.getText().toString());
                } catch (NumberFormatException e) {
                    warning = 0.0;
                }

                // get SHORTNAME and STOCK_NO
                String shortName = stockInfo.getShortName();
                String stockNo = stockInfo.getStockNo();

                // Submit
                if (finalIsUpdate) {
                    db.updateStock(bundle.getInt("id"), stockNo, symbol, shortName, warning, stockType);
                } else {
                    Stock stock = new Stock();
                    stock.setSymbol(symbol);
                    stock.setStockNo(stockNo);
                    stock.setShortName(shortName);
                    stock.setWarningPrice(warning);
                    stock.setType(stockType);
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

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        Log.e(TAG, "Dismiss when pressing outside");
        dismiss();
    }

    private void addCancelButton(EditText edt, int id) {
        ImageButton btn = requireView().findViewById(id);

        btn.setVisibility(ImageButton.GONE);
        edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                btn.setVisibility(s.length() > 0 ? ImageButton.VISIBLE : ImageButton.GONE);
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edt.getText().clear();
                edt.requestFocus();

                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(edt, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }
}
