package com.osmanthus.swiftlist;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class ChecklistItem {
    @PrimaryKey
    public final int num;

    @ColumnInfo
    public int position;
    public String text;
    public boolean isChecked;

    public ChecklistItem(int num, int position, String text, boolean isChecked) {
        this.num = num;
        this.position = position;
        this.text = text;
        this.isChecked = isChecked;
    }
}
