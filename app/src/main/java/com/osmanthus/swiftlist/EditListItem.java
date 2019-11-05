package com.osmanthus.swiftlist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class EditListItem extends AppCompatActivity {

    public static final String TO_EDIT = "com.osmanthus.swiftlist.TO_EDIT";
    public static final String TO_EDIT_ID = "com.osmanthus.swiftlist.TO_EDIT_ID";
    public static final String TO_EDIT_POS = "com.osmanthus.swiftlist.TO_EDIT_POS";

    private TextView textView;
    private Button saveButton;
    private Intent passedIntent;

    private boolean isEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_list_item);

        textView = findViewById(R.id.editText);

        passedIntent = getIntent();

        String editText = passedIntent.getStringExtra(TO_EDIT);
        final long editID = passedIntent.getLongExtra(TO_EDIT_ID, 0);
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
                    saveEdit(textView.getText().toString(), editID);
                }
            }
        });
    }

    private void saveEdit(String text, long id) {
        if (!isEdit) {
            TaskDispatcher.getInstance().addItem(this, text);
        } else {
            int pos = passedIntent.getIntExtra(TO_EDIT_POS, 0);
            TaskDispatcher.getInstance().updateItemText(this, pos, id, text);
        }

        finish();
    }
}
