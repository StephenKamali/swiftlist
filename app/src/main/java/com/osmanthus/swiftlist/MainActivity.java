package com.osmanthus.swiftlist;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

//DONE - add divider to last item in widget list
//DONE - change curve radius of widget list
//DONE - improve look of widget border
//DONE - reduce checkmark size of widget list
//DONE - dull out checked widget and list items
//DONE - don't force this to be on the main thread
//DONE - cleanup old code and imports
//DONE - make clickable widget area better

//TODO - add settings for transparency, color, add position (top or bot), text size
//TODO - add setting for dark mode
//TODO - drag to delete items
//TODO - add undo toast for deleting items
//TODO - make icons in main activity look good (add and delete icons)
//TODO - update checkmark sprite
//TODO - update widget preview image
//TODO - add custom app icon

/*
Could use position as primary key

When editing would have to set the position to something like 0, then change the
other item's position, then change it back

This would allow storing items in a hashmap yet still be useful for RecyclerAdapter
 */

public class MainActivity extends AppCompatActivity {

    private RecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    private static Handler adapterHandler;
    private static LinearLayoutManager layoutManager;

    private static FloatingActionButton fabAdd;
    private static FloatingActionButton fabDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Add item button
        fabAdd = findViewById(R.id.fab);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editListItem = new Intent(view.getContext(), EditListItem.class);
                startActivity(editListItem);
            }
        });

        //Delete item(s) button
        fabDelete = findViewById(R.id.fab_delete);
        fabDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TaskDispatcher.getInstance().removeCheckedItems(getApplicationContext());
            }
        });

        //Disable buttons until list is retrieved from database
        //As far as I understand, onCreate must finish before messages can be handled,
        //so there shouldn't be any threading problems with this
        //(messages for the handler are queued on the main thread, and onCreate should be first)
        /*
        if (TaskDispatcher.getInstance().getChecklistItems(this) == null) {
            fabAdd.setAlpha(0.5f);
            fabDelete.setAlpha(0.5f);
            fabAdd.setEnabled(false);
            fabDelete.setEnabled(false);
        }
        */

        initRecyclerView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //Log.d("BOOTY", "option pressed");
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            //return true;
        }

        return true;

        //return super.onOptionsItemSelected(item);
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recyler_view);
        adapter = new RecyclerViewAdapter(this, recyclerView);
        recyclerView.setAdapter(adapter);

        adapterHandler = new AdapterHandler(getApplication(), adapter);
        TaskDispatcher.getInstance().setExternalHandler(adapterHandler);

        layoutManager = new LinearLayoutManager(this);
        //layoutManager.setStackFromEnd(true); //TODO - do I really need to do this?
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public void onPause() {
        TaskDispatcher.updateWidgetView(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        TaskDispatcher.getInstance().setExternalHandler(null);
        super.onDestroy();
    }

    static class AdapterHandler extends Handler {
        private RecyclerViewAdapter adapter;
        private Application application;

        AdapterHandler(Application application, RecyclerViewAdapter adapter) {
            this.application = application;
            this.adapter = adapter;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TaskDispatcher.UPDATE_ITEM:
                    adapter.notifyItemChanged(msg.arg1);
                    break;
                case TaskDispatcher.INSERT_ITEM:
                    adapter.notifyItemInserted(msg.arg1);
                    break;
                case TaskDispatcher.REMOVE_ITEM:
                    //TODO - would like to eventually remove items more efficiently
                    adapter.notifyDataSetChanged();
                    Toast.makeText(application.getApplicationContext(),
                            msg.arg1 + " items deleted.",
                            Toast.LENGTH_SHORT).show();
                    break;
                case TaskDispatcher.DATA_CHANGED:
                    adapter.notifyDataSetChanged();
                    break;
                case TaskDispatcher.DATA_LOADED:
                    adapter.notifyDataSetChanged();
                    /*
                    fabAdd.setAlpha(1.0f);
                    fabDelete.setAlpha(1.0f);
                    fabAdd.setEnabled(true);
                    fabDelete.setEnabled(true);
                    */
                    break;
                case TaskDispatcher.SWAP_ITEM:

                    //Starting from here
                    int firstPos = layoutManager.findFirstCompletelyVisibleItemPosition();
                    int offsetTop = 0;

                    if(firstPos >= 0){
                        View firstView = layoutManager.findViewByPosition(firstPos);
                        offsetTop = layoutManager.getDecoratedTop(firstView) - layoutManager.getTopDecorationHeight(firstView);
                    }

                    adapter.notifyItemMoved(msg.arg1, msg.arg2);

                    if(firstPos >= 0) {
                        layoutManager.scrollToPositionWithOffset(firstPos, offsetTop);
                    }
                    //and ending here is from
                    //https://stackoverflow.com/questions/27992427/recyclerview-adapter-notifyitemmoved0-1-scrolls-screen?answertab=votes#tab-top


                    //adapter.notifyItemMoved(msg.arg1, msg.arg2);
                    ChecklistItem test1 = TaskDispatcher.getInstance().getChecklistItems(application.getApplicationContext()).get(msg.arg1);
                    ChecklistItem test2 = TaskDispatcher.getInstance().getChecklistItems(application.getApplicationContext()).get(msg.arg2);
                    adapter.notifyItemChanged(msg.arg1, test2);
                    adapter.notifyItemChanged(msg.arg2, test1);



                    //adapter.notifyDataSetChanged();
                    break;
                default:
                    //Shouldn't happen
                    break;
            }
            super.handleMessage(msg);
        }
    }
}
