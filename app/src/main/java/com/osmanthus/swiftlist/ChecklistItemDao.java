package com.osmanthus.swiftlist;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface ChecklistItemDao {
    @Query("SELECT * FROM checklistitem")
    List<ChecklistItem> getAllChecklistItems();

    @Query("SELECT * FROM checklistitem ORDER BY position ASC")
    List<ChecklistItem> getAllChecklistItemsByPosition();

    @Insert
    void insert(ChecklistItem item);

    @Update
    void update(ChecklistItem item);

    @Delete
    void delete(ChecklistItem item);
}
