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
    public static final String CHECK_ACTION = "com.osmanthus.swiftlist.CHECK_ACTION";
    public static final String ITEM_ID = "com.osmanthus.swiftlist.ITEM_ID";
    public static final String ITEM_POS = "com.osmanthus.swiftlist.ITEM_POS";

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
            //TODO - error check here to make sure never actually go through with 0
            long itemID = intent.getLongExtra(ITEM_ID, 0);
            if (itemID == -1) {
                Intent launchApp = new Intent(context, MainActivity.class);
                launchApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchApp);
            } else {
                //TODO - add error check to make sure this val is never 0
                int pos = intent.getIntExtra(ITEM_POS, 0);
                PendingResult pendingResult = goAsync();
                TaskDispatcher.getInstance().toggleItemChecked(context, pendingResult, pos, itemID);
            }
        }
    }
}

