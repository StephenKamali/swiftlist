package com.osmanthus.swiftlist;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class ChecklistItem {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo
    public int position;
    public String text;
    public boolean isChecked;

    public ChecklistItem(long id, int position, String text, boolean isChecked) {
        this.id = id;
        this.position = position;
        this.text = text;
        this.isChecked = isChecked;
    }

    public ChecklistItem(ChecklistItem item) {
        this(item.id, item.position, item.text, item.isChecked);
    }
}
