package com.osmanthus.swiftlist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import android.util.Log;

public class DragController extends Callback {

    private Context context;

    public DragController(Context context) {
        this.context = context;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                ItemTouchHelper.DOWN | ItemTouchHelper.UP);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
        //recyclerView.getAdapter();
        TaskDispatcher.getInstance().swapItems(context, viewHolder.getAdapterPosition(), viewHolder1.getAdapterPosition());
        return true;
    }

    /*
    @Override
    public void onMoved (RecyclerView recyclerView,
                         RecyclerView.ViewHolder viewHolder,
                         int fromPos,
                         RecyclerView.ViewHolder target,
                         int toPos,
                         int x,
                         int y) {
        Log.d("SEFI", "fromPos = " + fromPos);
        if (fromPos == 0 && toPos <= 5)
            recyclerView.scrollToPosition(0);
    }
    */

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

    }


    //TODO - customize the feel of scroll drags
    //Also note, negative val = scroll up, positive val = scroll down
    @Override
    public int interpolateOutOfBoundsScroll(RecyclerView recyclerView,
                                            int viewSize,
                                            int viewSizeOutOfBounds,
                                            int totalSize,
                                            long msSinceStartScroll) {

        //Log.d("SEFI", "viewSize = " + viewSize + " and viewSizeOutOfBounds = " + viewSizeOutOfBounds);
        //return -100;
        //Log.d("SEFI", "returning " + viewSizeOutOfBounds / 2);
        //return (int)(viewSizeOutOfBounds * min(msSinceStartScroll / 500.0f, 1.0f));
        return (viewSizeOutOfBounds / 2);
    }

    private float min (float num1, float num2) {
        if (num1 <= num2) {
            return num1;
        }
        return num2;
    }
}
