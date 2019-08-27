package com.osmanthus.swiftlist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
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

    public void addItem(final Context context, final ChecklistItem item) {
        item.position = checklistItems.size();
        checklistItems.add(item);

        //Must copy the item to submit to database, because the item
        //in the list could be changed in the meantime
        final ChecklistItem itemCopy = new ChecklistItem(item);

        if (externalHandler != null) {
            Message msg = new Message();
            msg.what = INSERT_ITEM;
            msg.arg1 = item.position;
            externalHandler.sendMessage(msg);
        }
        updateWidgetView(context);

        manager.submit(new Runnable() {
            @Override
            public void run() {
                long id = ChecklistDatabase.getInstance(context).getChecklistDao().insert(itemCopy);
                item.id = id;
            }
        });
    }

    public void updateItem(final Context context, final ChecklistItem item, final int pos) {
        //TODO - instead of passing a new object to this method, allow specific params
        //to be passed ( so don't need to instantiate entirely new obj)
        checklistItems.set(pos, item);

        final ChecklistItem itemCopy = new ChecklistItem(item);

        if (externalHandler != null) {
            Message msg = new Message();
            msg.what = UPDATE_ITEM;
            msg.arg1 = pos;
            externalHandler.sendMessage(msg);
        }
        updateWidgetView(context);

        manager.submit(new Runnable() {
            @Override
            public void run() {
                ChecklistDatabase.getInstance(context).getChecklistDao().update(itemCopy);
            }
        });
    }

    public void widgetUpdateItem(final Context context, final BroadcastReceiver.PendingResult pendingResult, final int pos) {
        manager.submit(new Runnable() {
            @Override
            public void run() {
                verifyChecklistDatabase(context);

                if (externalHandler != null) {
                    Message msg = new Message();
                    msg.what = UPDATE_ITEM;
                    msg.arg1 = pos;
                    externalHandler.sendMessage(msg);
                }
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
        updateWidgetView(context);

        manager.submit(new Runnable() {
            @Override
            public void run() {
                //TODO - add a callback that main receives so it can display # of deleted items
                ChecklistDatabase.getInstance(context).getChecklistDao().deleteList(removed);
                ChecklistDatabase.getInstance(context).getChecklistDao().updateList(updated);
            }
        });
    }

    public void swapItems(final Context context, final int index1, final int index2) {
        ChecklistItem cItem1 = checklistItems.get(index1);
        ChecklistItem cItem2 = checklistItems.get(index2);
        cItem1.position = index2;
        cItem2.position = index1;
        Collections.swap(checklistItems, index1, index2);

        final ChecklistItem cItem1Copy = new ChecklistItem(cItem1);
        final ChecklistItem cItem2Copy = new ChecklistItem(cItem2);

        if (externalHandler != null) {
            Message msg = new Message();
            msg.what = SWAP_ITEM;
            msg.arg1 = index1;
            msg.arg2 = index2;
            externalHandler.sendMessage(msg);
        }
        updateWidgetView(context);

        manager.submit(new Runnable() {
            @Override
            public void run() {
                ChecklistDatabase.getInstance(context).getChecklistDao().update(cItem1Copy);
                ChecklistDatabase.getInstance(context).getChecklistDao().update(cItem2Copy);
            }
        });
    }

    public void setExternalHandler(Handler handler) {
        externalHandler = handler;
    }
}
