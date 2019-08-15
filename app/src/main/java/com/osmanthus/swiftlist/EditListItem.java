package com.osmanthus.swiftlist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class EditListItem extends AppCompatActivity {

    public static final String TO_EDIT = "com.osmanthus.simplelist.TO_EDIT";
    public static final String TO_EDIT_INDEX = "com.osmanthus.simplelist.TO_EDIT_INDEX";

    private TextView textView;
    private Button saveButton;

    private boolean isEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_list_item);

        textView = findViewById(R.id.editText);

        String editText = getIntent().getStringExtra(TO_EDIT);
        final int editIndex = getIntent().getIntExtra(TO_EDIT_INDEX, 0);
        if (editText != null) {
            isEdit = true;
            textView.setText(editText);
        }

        textView.requestFocus();
        saveButton = findViewById(R.id.button_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textView.getText().toString().equals("")) {
                    finish();
                } else {
                    //Save the edit
                    /*
                    Intent data = new Intent();
                    data.setData(Uri.parse(textView.getText().toString()));
                    setResult(RESULT_OK, data);
                    finish();
                    */
                    saveEdit(textView.getText().toString(), editIndex);
                }
            }
        });
    }

    private void saveEdit(String text, int index) {
        List<ChecklistItem> tempList = Singleton.getInstance().getItemList(this);
        int size = tempList.size();
        //TODO - cleanup what is needed to make a new checklist item (don't need to pass size twice, false can be set by default)
        if (!isEdit) {
            ChecklistItem newItem = new ChecklistItem(size, size, text, false);
            tempList.add(newItem);
            //adapter.notifyItemInserted(size);
            ChecklistDatabase.getInstance(this).getChecklistDao().insert(newItem);
        } else {
            tempList.get(index).text = text;
            ChecklistDatabase.getInstance(this).getChecklistDao().update(tempList.get(index));
        }

        Singleton.getInstance().updateWidgetView(this);
        finish();
    }
}
