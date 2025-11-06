package com.example.tarefas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tarefas.db";
    private static final int DATABASE_VERSION = 2; // Incremented version

    private static final String TABLE_TASKS = "tasks";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_IS_COMPLETED = "is_completed"; // New column

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_TIME + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_IS_COMPLETED + " INTEGER NOT NULL DEFAULT 0"
                + ")";
        db.execSQL(CREATE_TASKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + COLUMN_IS_COMPLETED + " INTEGER NOT NULL DEFAULT 0;");
        }
    }

    public long addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, task.getDate());
        values.put(COLUMN_TIME, task.getTime());
        values.put(COLUMN_DESCRIPTION, task.getDescription());
        values.put(COLUMN_IS_COMPLETED, task.isCompleted() ? 1 : 0);

        long id = db.insert(TABLE_TASKS, null, values);
        db.close();
        return id;
    }

    public List<Task> getTasksByDate(String date) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_TASKS, null, COLUMN_DATE + " = ?", new String[]{date}, null, null, COLUMN_TIME);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Task task = new Task(
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
                    );
                    task.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_COMPLETED)) == 1);
                    taskList.add(task);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error while trying to get tasks from database", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return taskList;
    }

    public int updateTaskCompletionStatus(int taskId, boolean isCompleted) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_COMPLETED, isCompleted ? 1 : 0);
        int rows = db.update(TABLE_TASKS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();
        return rows;
    }

    public void deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteAllTasks() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, null, null);
        db.close();
    }

    public void markAllTasksAsCompleted() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_COMPLETED, 1);
        db.update(TABLE_TASKS, values, null, null);
        db.close();
    }
    
    public int getTasksCount(String date, Boolean isCompleted) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_DATE + " = ?";
        List<String> selectionArgs = new ArrayList<>();
        selectionArgs.add(date);

        if (isCompleted != null) {
            selection += " AND " + COLUMN_IS_COMPLETED + " = ?";
            selectionArgs.add(isCompleted ? "1" : "0");
        }
        
        Cursor cursor = db.query(TABLE_TASKS, new String[]{COLUMN_ID}, selection, selectionArgs.toArray(new String[0]), null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    public int getTotalCompletedTasks() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, new String[]{COLUMN_ID}, COLUMN_IS_COMPLETED + " = 1", null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }
}
