package com.osmanthus.swiftlist;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

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
                    verifyChecklistDatabase(context);
                    if (externalHandler != null) {
                        Message msg = new Message();
                        msg.what = DATA_CHANGED;
                        externalHandler.sendMessage(msg);
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
                if (externalHandler != null) {
                    Message msg = new Message();
                    msg.what = INSERT_ITEM;
                    msg.arg1 = pos;
                    externalHandler.sendMessage(msg);
                }
                updateWidgetView(context);
            }
        });
    }

    public void updateItem(final Context context, final ChecklistItem item, final int pos) {
        manager.submit(new Callable<Integer>() {
            @Override
            public Integer call() {
                verifyChecklistDatabase(context);
                ChecklistDatabase.getInstance(context).getChecklistDao().update(item);
                checklistItems.set(pos, item);
                if (externalHandler != null) {
                    Message msg = new Message();
                    msg.what = UPDATE_ITEM;
                    msg.arg1 = pos;
                    externalHandler.sendMessage(msg);
                }
                updateWidgetView(context);

                return 0;
            }
        });
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
                        //TODO - should keep track of all deleted then send them all to adapter
                        //at once at the end
                    } else {
                        i++;
                    }
                }
                if (externalHandler != null) {
                    Message msg = new Message();
                    msg.what = DATA_CHANGED;
                    //msg.arg1 = i;
                    externalHandler.sendMessage(msg);
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
                if (externalHandler != null) {
                    Message msg = new Message();
                    msg.what = SWAP_ITEM;
                    msg.arg1 = index1;
                    msg.arg2 = index2;
                    externalHandler.sendMessage(msg);
                }
                updateWidgetView(context);
            }
        });
    }

    public void setExternalHandler(Handler handler) {
        externalHandler = handler;
    }
}
