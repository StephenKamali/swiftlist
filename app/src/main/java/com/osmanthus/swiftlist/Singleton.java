package com.osmanthus.swiftlist;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Singleton {
    private static final Singleton ourInstance = new Singleton();

    static Singleton getInstance() {
        return ourInstance;
    }

    private Singleton() {
    }

    // Here's the real stuff
    private static ArrayList<ChecklistItem> listItems;

    //TODO - maybe should move all calls to updateWidgetView to the singleton
    public void updateWidgetView(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, ListWidget.class);
        //TODO - would be nice to not have to fetch appWidgetIds every single time
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
    }

    public List<ChecklistItem> getItemList(Context context) {
        if (listItems == null)
            listItems = (ArrayList<ChecklistItem>)ChecklistDatabase.getInstance(context).getChecklistDao().getAllChecklistItemsByPosition();
        return listItems;
    }

    public void swapChecklistItems(Context context, int index1, int index2) {
        ChecklistItem cItem1 = getItemList(context).get(index1);
        ChecklistItem cItem2 = getItemList(context).get(index2);
        cItem1.position = index2;
        cItem2.position = index1;
        ChecklistDatabase.getInstance(context).getChecklistDao().update(cItem2);
        ChecklistDatabase.getInstance(context).getChecklistDao().update(cItem1);
        Collections.swap(listItems, index1, index2);
    }

    public void databaseUpdate(Context context, int itemIndex) {
        ChecklistDatabase.getInstance(context).getChecklistDao().update(listItems.get(itemIndex));
    }

    public int deleteChecked(Context context) {
        //TODO - use better method to keep track of checked items (maybe have a separate collection for them)
        ChecklistItemDao tempRef = ChecklistDatabase.getInstance(context).getChecklistDao();
        List<ChecklistItem> toDelete = new ArrayList<>();
        int totalDeleted = 0;
        for (ChecklistItem ci : listItems) {
            if (ci.isChecked) {
                totalDeleted++;
                tempRef.delete(ci);
                toDelete.add(ci);
            }
        }
        listItems.removeAll(toDelete);
        toDelete.clear();

        return totalDeleted;
    }
}
