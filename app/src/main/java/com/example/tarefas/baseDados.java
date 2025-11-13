package com.example.tarefas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;


public class baseDados extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tarefas.db";
    private static final int DATABASE_VERSION = 2; // Nova versão do app

    private static final String TABLE_TASKS = "tasks";
    private static final String COLUNA_ID = "id";
    private static final String COLUNA_DATA = "data";
    private static final String COLUNA_HORA = "hora";
    private static final String COLUNA_DESCRICAO = "descricao";
    private static final String COLUMN_IS_COMPLETED = "is_completed";

    public baseDados(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + " ("
                + COLUNA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUNA_DATA + " TEXT, "
                + COLUNA_HORA + " TEXT, "
                + COLUNA_DESCRICAO + " TEXT, "
                + COLUMN_IS_COMPLETED + " INTEGER NOT NULL DEFAULT 0"
                + ")";
        db.execSQL(CREATE_TASKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            db.execSQL("ALTER TABLE " + TABLE_TASKS +
                    " ADD COLUMN " + COLUMN_IS_COMPLETED + " INTEGER NOT NULL DEFAULT 0;");
        }
    }

    // Adiciona uma nova tarefa
    public long addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUNA_DATA, task.getDate());
        values.put(COLUNA_HORA, task.getTime());
        values.put(COLUNA_DESCRICAO, task.getDescription());
        values.put(COLUMN_IS_COMPLETED, task.isCompleted() ? 1 : 0);

        long id = db.insert(TABLE_TASKS, null, values);
        db.close();
        return id;
    }

    // Retorna todas as tarefas de uma data específica
    public List<Task> getTasksByDate(String date) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_TASKS,
                null,
                COLUNA_DATA + " = ?",
                new String[]{date},
                null,
                null,
                COLUNA_DESCRICAO
        );

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUNA_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUNA_DATA)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUNA_HORA)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUNA_DESCRICAO))
                );
                task.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_COMPLETED)) == 1);
                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return taskList;
    }

    // Atualiza o status de conclusão de uma tarefa
    public int updateTaskCompletionStatus(int taskId, boolean isCompleted) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_COMPLETED, isCompleted ? 1 : 0);
        int rows = db.update(TABLE_TASKS, values, COLUNA_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();
        return rows;
    }


    public void deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, COLUNA_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Retorna a contagem de tarefas por data e status de conclusão
    public int getTasksCount(String date, Boolean isCompleted) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUNA_DATA + " = ?";
        List<String> selectionArgs = new ArrayList<>();
        selectionArgs.add(date);

        if (isCompleted != null) {
            selection += " AND " + COLUMN_IS_COMPLETED + " = ?";
            selectionArgs.add(isCompleted ? "1" : "0");
        }

        Cursor cursor = db.query(
                TABLE_TASKS,
                new String[]{COLUNA_ID},
                selection,
                selectionArgs.toArray(new String[0]),
                null,
                null,
                null
        );
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    // Retorna a contagem total de tarefas concluídas
    public int getTotalCompletedTasks() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_TASKS,
                new String[]{COLUNA_ID},
                COLUMN_IS_COMPLETED + " = 1",
                null,
                null,
                null,
                null
        );
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }
}
