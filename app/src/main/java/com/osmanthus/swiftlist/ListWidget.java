package com.osmanthus.swiftlist;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class ListWidget extends AppWidgetProvider {
    public static final String CHECK_ACTION = "com.osmanthus.simplelist.CHECK_ACTION";
    public static final String EXTRA_ITEM = "com.osmanthus.simplelist.EXTRA_ITEM";

    public static void updateWidgetView(final Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, ListWidget.class);
        //TODO - would be nice to not have to fetch appWidgetIds every single time
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Intent intent = new Intent(context, ListWidgetService.class);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.list_widget);
        views.setRemoteAdapter(R.id.widget_list, intent);

        // Set up pending intent for list view items
        Intent checkboxIntent = new Intent(context, ListWidget.class);
        checkboxIntent.setAction(ListWidget.CHECK_ACTION);
        PendingIntent checkboxPendingIntent = PendingIntent.getBroadcast(context, 0,
                checkboxIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widget_list, checkboxPendingIntent);

        // Set up pending on click for blank list space to launch main app
        Intent launchApp = new Intent(context, MainActivity.class);
        PendingIntent launchAppIntent = PendingIntent.getActivity(context, 0,
                launchApp, 0);
        views.setOnClickPendingIntent(R.id.widget_layout, launchAppIntent);

        //views.setInt(R.id.widget_layout, "setBackgroundColor", Color.parseColor("#80ffffff"));
        //views.setInt(R.id.widget_bg, "setImageAlpha", 0);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        super.onDisabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(CHECK_ACTION)) {
            int viewIndex = intent.getIntExtra(EXTRA_ITEM, 0);
            if (viewIndex == -1) {
                Intent launchApp = new Intent(context, MainActivity.class);
                launchApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchApp);
            } else {
                //TODO - use cached list instead of accessing singleton
                //TaskDispatcher.getInstance().ping();
                //ChecklistItem toChange = TaskDispatcher.getInstance().getChecklistItems(context).get(viewIndex);
                ChecklistItem toChange = ListWidgetFactory.cache.get(viewIndex);
                ChecklistItem changed = new ChecklistItem(toChange.id, toChange.position, toChange.text, toChange.isChecked);
                changed.isChecked = !changed.isChecked;
                //TODO - use a pending intent to stop thread from being killed when broadcast
                //receiver is garbage collected
                TaskDispatcher.getInstance().updateItem(context, changed, viewIndex);
            }
        }
    }
}

