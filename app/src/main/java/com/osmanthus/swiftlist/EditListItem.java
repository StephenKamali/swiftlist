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
                    saveEdit(textView.getText().toString(), editIndex);
                }
            }
        });
    }

    private void saveEdit(String text, int index) {
        int size = TaskDispatcher.getInstance().getChecklistItems(this).size();
        if (!isEdit) {
            ChecklistItem newItem = new ChecklistItem(size, text, false);
            TaskDispatcher.getInstance().addItem(this, newItem, size);
        } else {
            ChecklistItem toUpdate = TaskDispatcher.getInstance().getChecklistItems(this).get(index);
            ChecklistItem updatedItem = new ChecklistItem(toUpdate);
            updatedItem.text = text;
            TaskDispatcher.getInstance().updateItem(this, updatedItem, index);
        }

        finish();
    }
}
