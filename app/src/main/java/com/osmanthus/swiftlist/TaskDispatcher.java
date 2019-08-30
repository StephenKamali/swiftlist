package com.osmanthus.swiftlist;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskDispatcher {

    public static final int UPDATE_ITEM = 0;
    public static final int INSERT_ITEM = 1;
    public static final int REMOVE_ITEM = 2;
    public static final int SWAP_ITEM = 3;
    public static final int DATA_CHANGED = 4;

    private static final TaskDispatcher ourInstance = new TaskDispatcher();

    public static TaskDispatcher getInstance() {
        return ourInstance;
    }

    private ExecutorService manager;
    private List<ChecklistItem> checklistItems;
    private static Handler externalHandler;

    private TaskDispatcher() {
        manager = Executors.newSingleThreadExecutor();
    }

    public static void updateWidgetView(final Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, ListWidget.class);
        //TODO - would be nice to not have to fetch appWidgetIds every single time
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
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
                    if (checklistItems == null) {
                        checklistItems = ChecklistDatabase.getInstance(context).getChecklistDao().getAllChecklistItemsByPosition();
                        if (externalHandler != null) {
                            Message msg = new Message();
                            msg.what = DATA_CHANGED;
                            externalHandler.sendMessage(msg);
                        }
                        updateWidgetView(context);
                    }
                }
            });
        }
        //This will return null if the list has not yet been initialized
        //so adapters must check for null return
        return checklistItems;
    }

    public void addItem(final Context context, final String itemText) {

        //TODO - disable add button before taskdispatcher has tried to get the info from database
        //TODO - disable delete button when list is empty
        final ChecklistItem toInsert;
        toInsert = new ChecklistItem(0, checklistItems.size(), itemText, false);
        checklistItems.add(toInsert);

        if (externalHandler != null) {
            Message msg = new Message();
            msg.what = INSERT_ITEM;
            msg.arg1 = toInsert.position;
            externalHandler.sendMessage(msg);
        }

        manager.submit(new Runnable() {
            @Override
            public void run() {
                long id = ChecklistDatabase.getInstance(context).getChecklistDao().insert(toInsert);
                toInsert.id = id;
            }
        });
    }

    public void updateItemText(final Context context,
                               int initPos,
                               final long itemID,
                               final String itemText) {
        checklistItems.get(initPos).text = itemText;

        if (externalHandler != null) {
            Message msg = new Message();
            msg.what = UPDATE_ITEM;
            msg.arg1 = initPos;
            externalHandler.sendMessage(msg);
        }

        manager.submit(new Runnable() {
            @Override
            public void run() {
                ChecklistDatabase.getInstance(context).getChecklistDao().setItemText(itemID, itemText);
            }
        });
    }

    public void updateItemChecked(final Context context,
                                  int initPos,
                                  final long itemID,
                                  final boolean isChecked) {
        checklistItems.get(initPos).isChecked = isChecked;

        manager.submit(new Runnable() {
            @Override
            public void run() {
                ChecklistDatabase.getInstance(context).getChecklistDao().setItemChecked(itemID,
                        isChecked ? 1 : 0);
            }
        });
    }

    //Updates an item's checked status without being passed the new value
    //Slightly slower (I think), but less info needed (for widget use)
    public void toggleItemChecked(final Context context,
                                  final BroadcastReceiver.PendingResult pendingResult,
                                  int initPos,
                                  final long itemID) {
        //TODO - what if main activity gets closed after this is checked, but before item is set?
        //Could result in a null pointer exception. Should use a JobScheduler instead so that
        //TaskDispatcher can outlive the activity (if it still has pending jobs, that is)
        if (checklistItems != null) {
            checklistItems.get(initPos).isChecked = !checklistItems.get(initPos).isChecked;

            if (externalHandler != null) {
                Message msg = new Message();
                msg.what = UPDATE_ITEM;
                msg.arg1 = initPos;
                externalHandler.sendMessage(msg);
            }
        }

        manager.submit(new Runnable() {
            @Override
            public void run() {
                boolean isChecked = !ChecklistDatabase.getInstance(context).getChecklistDao().getChecked(itemID);
                ChecklistDatabase.getInstance(context).getChecklistDao().setItemChecked(itemID,
                        isChecked ? 1 : 0);
                updateWidgetView(context);
                pendingResult.finish();
            }
        });
    }

    public void removeCheckedItems(final Context context) {
        final List<ChecklistItem> removed = new ArrayList<>();
        final List<ChecklistItem> updated = new ArrayList<>();

        ChecklistItem tempItem;
        int totalDeleted = 0;
        int i = 0;
        //TODO - when/if i implement a custom list, this logic may no longer work
        //TODO - keep track of a separate list of checked items so don't need to go through
        //whole list
        while (i < checklistItems.size()) {
            tempItem = checklistItems.get(i);
            if(tempItem.isChecked) {
                totalDeleted++;
                removed.add(checklistItems.remove(i));

                //Check if i is now outside of bounds
                if (i < checklistItems.size()) {
                    //Must update position of item that took this one's place
                    tempItem = checklistItems.get(i);
                    tempItem.position = i;
                    updated.add(tempItem);
                }
                //TODO - should keep track of all deleted then send them all to adapter
                //at once at the end
            } else {
                if ((totalDeleted != 0)) {
                    tempItem.position = i;
                    updated.add(tempItem);
                }
                i++;
            }
        }
        if (externalHandler != null) {
            Message msg = new Message();
            msg.what = REMOVE_ITEM;
            msg.arg1 = totalDeleted;
            externalHandler.sendMessage(msg);
        }
        manager.submit(new Runnable() {
            @Override
            public void run() {
                //TODO - add a callback that main receives so it can display # of deleted items
                ChecklistDatabase.getInstance(context).getChecklistDao().deleteList(removed);
                ChecklistDatabase.getInstance(context).getChecklistDao().updateList(updated);
            }
        });
    }

    //TODO - haven't converted this func
    public void swapItems(final Context context, final int index1, final int index2) {
        final ChecklistItem cItem1 = checklistItems.get(index1);
        final ChecklistItem cItem2 = checklistItems.get(index2);
        cItem1.position = index2;
        cItem2.position = index1;
        Collections.swap(checklistItems, index1, index2);

        //final long item1ID = cItem1.id;
        //final long item2ID = cItem2.id;

        if (externalHandler != null) {
            Message msg = new Message();
            msg.what = SWAP_ITEM;
            msg.arg1 = index1;
            msg.arg2 = index2;
            externalHandler.sendMessage(msg);
        }

        manager.submit(new Runnable() {
            @Override
            public void run() {
                //int pos1 = ChecklistDatabase.getInstance(context).getChecklistDao().getItemPosition(item1ID);
                //int pos2 = ChecklistDatabase.getInstance(context).getChecklistDao().getItemPosition(item2ID);

                //Using ref to real obj here b/c after adding an element, the id could still be 0
                //But once at the point of running another task, item is guaranteed to have had its id set
                ChecklistDatabase.getInstance(context).getChecklistDao().setItemPosition(cItem1.id, index2);
                ChecklistDatabase.getInstance(context).getChecklistDao().setItemPosition(cItem2.id, index1);
            }
        });
    }

    public void setExternalHandler(Handler handler) {
        externalHandler = handler;
    }
}
