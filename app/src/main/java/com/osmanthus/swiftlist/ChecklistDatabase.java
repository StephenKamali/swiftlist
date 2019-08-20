package com.osmanthus.swiftlist;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities =  {ChecklistItem.class}, version = 1)
public abstract class ChecklistDatabase extends RoomDatabase {

    private static final String DB_NAME = "checklistDatabase.db";
    private static volatile ChecklistDatabase instance;

    static synchronized ChecklistDatabase getInstance(Context context) {
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    }

    private static ChecklistDatabase create(final Context context) {
        //TODO - don't force this to be on the main thread
        return Room.databaseBuilder(context, ChecklistDatabase.class, DB_NAME).build();
    }

    public abstract ChecklistItemDao getChecklistDao();
}
