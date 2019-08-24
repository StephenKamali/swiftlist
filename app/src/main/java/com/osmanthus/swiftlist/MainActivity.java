package com.osmanthus.swiftlist;

import android.content.Intent;
import android.os.Bundle;
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

//TODO - add settings for transparency, color, add position (top or bot), text size
//TODO - drag to delete items
//TODO - make icons in main activity look good (add and delete icons)
//TODO - update checkmark sprite
//TODO - update widget preview image
//TODO - add custom app icon
//TODO - cleanup old code and imports
//TODO - make clickable widget area better
//TODO - don't force this to be on the main thread

public class MainActivity extends AppCompatActivity {

    private RecyclerViewAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editListItem = new Intent(view.getContext(), EditListItem.class);
                startActivityForResult(editListItem, 0);
            }
        });
        FloatingActionButton fab_delete = findViewById(R.id.fab_delete);
        fab_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TaskDispatcher.getInstance().removeCheckedItems(getApplicationContext());
                Toast.makeText(getApplicationContext(),
                        "unknown # of" + " items deleted.",
                        Toast.LENGTH_SHORT).show();
                //adapter.notifyDataSetChanged();
            }
        });

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
        TaskDispatcher.getInstance().setAdapter(adapter);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onDestroy() {
        TaskDispatcher.getInstance().setAdapter(null);
        super.onDestroy();
    }
}
