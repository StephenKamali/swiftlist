package com.osmanthus.swiftlist;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;
import android.widget.ListView;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskDispatcher {

    private static final TaskDispatcher ourInstance = new TaskDispatcher();

    public static TaskDispatcher getInstance() {
        return ourInstance;
    }

    private ExecutorService manager;
    private List<ChecklistItem> checklistItems;
    private RecyclerViewAdapter adapter;

    private TaskDispatcher() {
        manager = Executors.newSingleThreadExecutor();
    }

    private void updateWidgetView(final Context context) {
        ListWidget.updateWidgetView(context);
    }

    private void verifyChecklistDatabase(final Context context) {
        if (checklistItems == null)
            checklistItems = ChecklistDatabase.getInstance(context).getChecklistDao().getAllChecklistItemsByPosition();
    }

    public List<ChecklistItem> getChecklistItems(final Context context) {
        if (checklistItems == null) {
            manager.submit(new Runnable() {
                @Override
                public void run() {
                    verifyChecklistDatabase(context);
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                    updateWidgetView(context);
                }
            });
        }
        return checklistItems;
    }

    public void addItem(final Context context, final ChecklistItem item, final int pos) {
        manager.submit(new Runnable() {
            @Override
            public void run() {
                verifyChecklistDatabase(context);
                ChecklistDatabase.getInstance(context).getChecklistDao().insert(item);
                checklistItems.add(pos, item);
                if (adapter != null) {
                    adapter.notifyItemChanged(pos);
                }
                updateWidgetView(context);
            }
        });
    }

    public void updateItem(final Context context, final ChecklistItem item, final int pos) {
        Log.d("BOOTY", "inside updateItem");
        manager.submit(new Callable<Integer>() {
            @Override
            public Integer call() {
                Log.d("BOOTY", "thread now running updateItem");
                verifyChecklistDatabase(context);
                Log.d("BOOTY", "database verified");
                ChecklistDatabase.getInstance(context).getChecklistDao().update(item);
                Log.d("BOOTY", "database updated");
                checklistItems.set(pos, item);
                Log.d("BOOTY", "checklist updated");
                /*
                if (adapter != null) {
                    adapter.notifyItemChanged(pos);
                    Log.d("BOOTY", "adapter notified");
                }
                */
                Log.d("BOOTY", "calling updateWidgetView");
                updateWidgetView(context);
                Log.d("BOOTY", "returned from updateWidgetView");

                return 0;
            }
        });
        Log.d("BOOTY", "leaving updateItem");
    }

    public void removeCheckedItems(final Context context) {
        manager.submit(new Runnable() {
            @Override
            public void run() {
                verifyChecklistDatabase(context);
                //TODO - add a callback that main receives so it can display # of deleted items
                ChecklistItem tempItem;
                int totalDeleted = 0;
                int i = 0;
                //TODO - keep track of a separate list of checked items so don't need to go through
                //whole list
                while (i < checklistItems.size()) {
                    tempItem = checklistItems.get(i);
                    if(tempItem.isChecked) {
                        totalDeleted++;
                        ChecklistDatabase.getInstance(context).getChecklistDao().delete(tempItem);
                        checklistItems.remove(i);
                        if (adapter != null) {
                            adapter.notifyItemRemoved(i);
                        }
                    } else {
                        i++;
                    }
                }
                updateWidgetView(context);
            }
        });
    }

    public void swapItems(final Context context, final int index1, final int index2) {
        manager.submit(new Runnable() {
            @Override
            public void run() {
                verifyChecklistDatabase(context);
                ChecklistItem cItem1 = checklistItems.get(index1);
                ChecklistItem cItem2 = checklistItems.get(index2);
                cItem1.position = index2;
                cItem2.position = index1;
                ChecklistDatabase.getInstance(context).getChecklistDao().update(cItem2);
                ChecklistDatabase.getInstance(context).getChecklistDao().update(cItem1);
                Collections.swap(checklistItems, index1, index2);
                adapter.notifyItemMoved(index1, index2);
                updateWidgetView(context);
            }
        });
    }

    public void setAdapter(RecyclerViewAdapter adapter) {
        this.adapter = adapter;
    }
}
