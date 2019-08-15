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
        return Singleton.getInstance().getItemList(context).size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        lastClicked = position;
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.list_widget_item);
        views.setTextViewText(R.id.widget_textView, Singleton.getInstance().getItemList(context).get(position).text);

        if (Singleton.getInstance().getItemList(context).get(position).isChecked) {
            views.setInt(R.id.widget_imageButton, "setImageResource", R.drawable.checkmark);
            views.setTextColor(R.id.widget_textView, Color.GRAY);
        } else {
            views.setInt(R.id.widget_imageButton, "setImageResource", R.drawable.checkmark_empty);
            views.setTextColor(R.id.widget_textView, Color.parseColor("#323232"));
        }

        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(ListWidget.EXTRA_ITEM, position);
        views.setOnClickFillInIntent(R.id.widget_imageButton, fillInIntent);

        Intent launchApp = new Intent();
        launchApp.putExtra(ListWidget.EXTRA_ITEM, -1);
        views.setOnClickFillInIntent(R.id.widget_textView, launchApp);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        //TODO - this seems kind of hacky, and it doesn't work when deletions occur
        /*
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.list_widget_item);
        views.setTextViewText(R.id.widget_textView, Singleton.getInstance().getItemList(context).get(lastClicked).text);

        return views;
        */
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