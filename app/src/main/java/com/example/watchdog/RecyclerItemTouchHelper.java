package com.example.watchdog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watchdog.adapter.StockAdapter;

import java.util.Objects;

public class RecyclerItemTouchHelper extends ItemTouchHelper.SimpleCallback {

    private final StockAdapter adapter;
    private final  MainActivity activity;

    public RecyclerItemTouchHelper(StockAdapter adapter,MainActivity activity) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
        this.activity=activity;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getAdapterPosition();
        if (direction == ItemTouchHelper.LEFT) {
            // Remove Item
            AlertDialog.Builder builder = new AlertDialog.Builder((adapter.getContext()));
            builder.setTitle("Delete Task");
            builder.setMessage("Are you sure you want to delete this Task");
            builder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    adapter.deleteItem(position);
                }
            }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    adapter.notifyItemChanged(Objects.requireNonNull(viewHolder).getAdapterPosition());
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

        } else {
            // Edit Item
            adapter.editItem(position);
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        Drawable icon;
        ColorDrawable background;

        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 20;

        // Initialize icon and background
        if (dX > 0) {
            // Swiping left
            icon = ContextCompat.getDrawable(adapter.getContext(), R.drawable.ic_baseline_edit);
            background = new ColorDrawable(ContextCompat.getColor(adapter.getContext(), R.color.colorPrimaryDark));
        } else {
            // Swiping right
            icon = ContextCompat.getDrawable(adapter.getContext(), R.drawable.ic_baseline_delete);
            background = new ColorDrawable(Color.RED);
        }

        // align icon
        assert icon != null;
        int icMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int icTop = itemView.getTop() + icMargin;
        int icBottom = icTop + icon.getIntrinsicHeight();

        if (dX > 0) {// Swiping to the right
            int icLeft = itemView.getLeft() + icMargin;
            int icRight = icLeft + icon.getIntrinsicWidth();
            icon.setBounds(icLeft, icTop, icRight, icBottom);

            int bgRight = itemView.getLeft() + ((int) dX) + backgroundCornerOffset;

            background.setBounds(itemView.getLeft(), itemView.getTop(), bgRight, itemView.getBottom());

        } else if (dX < 0) { // Swiping to the left
            int icRight = itemView.getRight() - icMargin;
            int icLeft = icRight - icon.getIntrinsicWidth();
            icon.setBounds(icLeft, icTop, icRight, icBottom);

            int bgLeft = itemView.getRight() + ((int) dX) - backgroundCornerOffset;
            background.setBounds(bgLeft,
                    itemView.getTop(), itemView.getRight(), itemView.getBottom());
        } else {// view is unSwiped
            background.setBounds(0, 0, 0, 0);
        }

        background.draw(c);
        icon.draw(c);
    }
}
