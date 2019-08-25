package com.osmanthus.swiftlist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private Context context;
    private ItemTouchHelper itemTouchHelper;
    private LayoutInflater layoutInflater;
    private List<ChecklistItem> checklistItems;

    public RecyclerViewAdapter(Context context, RecyclerView recyclerView) {
        this.context = context;

        layoutInflater = LayoutInflater.from(context);

        // Set up objects needed for dragging list items
        DragController dragController = new DragController(context, this);
        itemTouchHelper = new ItemTouchHelper(dragController);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = layoutInflater.inflate(R.layout.list_item_layout,
                viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        //TODO - cache list instead of this badness
        //if (checklistItems != null) {
        if (TaskDispatcher.getInstance().getChecklistItems(context) != null) {
            final ChecklistItem item = TaskDispatcher.getInstance().getChecklistItems(context).get(i);
            viewHolder.text.setText(item.text);
            viewHolder.checkBox.setChecked(item.isChecked);
            //TODO - same as checkBox listener below, seems kinda inefficient
            viewHolder.text.setTextColor(item.isChecked ? Color.GRAY : Color.parseColor("#323232"));
            viewHolder.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent editItem = new Intent(context, EditListItem.class);
                    editItem.putExtra(EditListItem.TO_EDIT, item.text);
                    editItem.putExtra(EditListItem.TO_EDIT_INDEX, item.position);
                    context.startActivity(editItem);
                }
            });

            viewHolder.moveButton.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        itemTouchHelper.startDrag(viewHolder);
                    }
                    return false;
                }
            });

            viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChecklistItem editedItem = new ChecklistItem(item);
                    editedItem.isChecked = !editedItem.isChecked;
                    //TODO - this seems kinda messy/ inefficient
                    viewHolder.text.setTextColor(editedItem.isChecked ? Color.GRAY : Color.parseColor("#323232"));
                    TaskDispatcher.getInstance().updateItem(context, editedItem, i);
                }
            });
        } else {
            //TODO - add an "empty list" message here
        }
    }

    @Override
    public int getItemCount() {
        //TODO - can use the cached list later
        if (TaskDispatcher.getInstance().getChecklistItems(context) != null)
            return TaskDispatcher.getInstance().getChecklistItems(context).size();
        else
            return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CheckBox checkBox;
        TextView text;
        Button moveButton;
        ConstraintLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox);
            text = itemView.findViewById(R.id.textitem);
            moveButton = itemView.findViewById(R.id.move_button);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }
}
