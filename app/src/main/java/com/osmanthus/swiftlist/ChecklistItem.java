package com.osmanthus.swiftlist;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class ChecklistItem {

    @PrimaryKey(autoGenerate = true)
    public int num;

    @ColumnInfo
    public int position;
    public String text;
    public boolean isChecked;

    public ChecklistItem(int position, String text, boolean isChecked) {
        this.position = position;
        this.text = text;
        this.isChecked = isChecked;
    }

    public ChecklistItem(ChecklistItem item) {
        num = item.num;
        position = item.position;
        text = item.text;
        isChecked = item.isChecked;
    }
}
