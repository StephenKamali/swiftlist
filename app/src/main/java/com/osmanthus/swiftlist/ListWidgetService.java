package com.osmanthus.swiftlist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class ListWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListWidgetFactory(this.getApplicationContext(), intent);
    }
}

class ListWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context context;
    private static int lastClicked;

    public ListWidgetFactory(Context context, Intent intent) {
        this.context = context;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        //TODO - should probably cache the list here too
        if (TaskDispatcher.getInstance().getChecklistItems(context) != null) {
            return TaskDispatcher.getInstance().getChecklistItems(context).size();
        } else {
            return 0;
        }
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (TaskDispatcher.getInstance().getChecklistItems(context) != null) {
            lastClicked = position;
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.list_widget_item);

            ChecklistItem item = TaskDispatcher.getInstance().getChecklistItems(context).get(position);

            views.setTextViewText(R.id.widget_textView, item.text);

            //TODO - should cache the list instead
            if (item.isChecked) {
                views.setInt(R.id.widget_imageButton, "setImageResource", R.drawable.checkmark);
                views.setTextColor(R.id.widget_textView, Color.GRAY);
            } else {
                views.setInt(R.id.widget_imageButton, "setImageResource", R.drawable.checkmark_empty);
                views.setTextColor(R.id.widget_textView, Color.parseColor("#323232"));
            }

            Intent fillInIntent = new Intent();
            fillInIntent.putExtra(ListWidget.ITEM_ID, item.id);
            fillInIntent.putExtra(ListWidget.ITEM_POS, position);
            views.setOnClickFillInIntent(R.id.widget_imageButton, fillInIntent);

            Intent launchApp = new Intent();
            launchApp.putExtra(ListWidget.ITEM_ID, (long)(-1));
            views.setOnClickFillInIntent(R.id.widget_textView, launchApp);

            return views;
        } else {
            return null;
        }
    }

    @Override
    public RemoteViews getLoadingView() {
        //TODO - can try to do something here to get rid of widget loading message
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}