package com.osmanthus.swiftlist;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface ChecklistItemDao {

    @Query("SELECT * FROM checklistitem WHERE id IS :itemID LIMIT 1")
    ChecklistItem getItem(long itemID);

    @Query("SELECT position FROM checklistitem WHERE id IS :itemID LIMIT 1")
    int getItemPosition(long itemID);

    @Query("SELECT isChecked FROM checklistitem WHERE id IS :itemID LIMIT 1")
    boolean getChecked(long itemID);

    @Query("SELECT * FROM checklistitem")
    List<ChecklistItem> getAllChecklistItems();

    @Query("SELECT * FROM checklistitem ORDER BY position ASC")
    List<ChecklistItem> getAllChecklistItemsByPosition();

    @Query("UPDATE checklistitem SET text = :newText WHERE id IS :itemID")
    void setItemText(long itemID, String newText);

    @Query("UPDATE checklistitem SET isChecked = :checked WHERE id IS :itemID")
    void setItemChecked(long itemID, int checked);

    @Query("UPDATE checklistitem SET position = :pos WHERE id IS :itemID")
    void setItemPosition(long itemID, int pos);

    //Insert doesn't work with room database
    //@Query("INSERT INTO checklistitem VALUES(NULL, (SELECT COUNT(id) FROM checklistitem), :text, 0)")
    //ChecklistItem insertItem(String text);

    //@Query("SELECT id FROM checklistitem ORDER BY id DESC LIMIT 1")
    //TODO - could use max(position) since just trying to find last position
    @Query("SELECT COUNT(id) FROM checklistitem")
    int getItemCount();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ChecklistItem item);

    @Update
    void update(ChecklistItem item);

    @Update
    void updateList(List<ChecklistItem> items);

    @Delete
    void delete(ChecklistItem item);

    @Delete
    void deleteList(List<ChecklistItem> items);
}
