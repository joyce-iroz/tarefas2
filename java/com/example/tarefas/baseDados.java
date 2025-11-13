package com.example.tarefas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class baseDados extends SQLiteOpenHelper {

    private static final String TAG = "baseDados"; // Tag para logs

    private static final String DATABASE_NAME = "tarefas.db";
    private static final int DATABASE_VERSION = 2;

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
        try {
            String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + " (" +
                    COLUNA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUNA_DATA + " TEXT, " +
                    COLUNA_HORA + " TEXT, " +
                    COLUNA_DESCRICAO + " TEXT, " +
                    COLUMN_IS_COMPLETED + " INTEGER NOT NULL DEFAULT 0" +
                    ")";
            db.execSQL(CREATE_TASKS_TABLE);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao criar a tabela: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            // Adiciona a coluna 'is_completed' apenas se estiver atualizando da versão 1 para a 2
            if (oldVersion < 2) {
                db.execSQL("ALTER TABLE " + TABLE_TASKS +
                        " ADD COLUMN " + COLUMN_IS_COMPLETED + " INTEGER NOT NULL DEFAULT 0;");
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar o banco de dados: " + e.getMessage());
        }
    }



    public long addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = -1;
        try {
            ContentValues values = new ContentValues();
            values.put(COLUNA_DATA, task.getDate());
            values.put(COLUNA_HORA, task.getTime());
            values.put(COLUNA_DESCRICAO, task.getDescription());
            values.put(COLUMN_IS_COMPLETED, task.isCompleted() ? 1 : 0);

            id = db.insert(TABLE_TASKS, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao adicionar tarefa: " + e.getMessage());
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return id;
    }

    public List<Task> getTasksByDate(String date) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    TABLE_TASKS,
                    null, // todas colunas
                    COLUNA_DATA + " = ?",
                    new String[]{date},
                    null,
                    null,
                    COLUNA_HORA // ordena por tempo
            );

            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndexOrThrow(COLUNA_ID);
                int dataIndex = cursor.getColumnIndexOrThrow(COLUNA_DATA);
                int horaIndex = cursor.getColumnIndexOrThrow(COLUNA_HORA);
                int descricaoIndex = cursor.getColumnIndexOrThrow(COLUNA_DESCRICAO);
                int completedIndex = cursor.getColumnIndexOrThrow(COLUMN_IS_COMPLETED);

                do {
                    Task task = new Task(
                            cursor.getInt(idIndex),
                            cursor.getString(dataIndex),
                            cursor.getString(horaIndex),
                            cursor.getString(descricaoIndex)
                    );
                    task.setCompleted(cursor.getInt(completedIndex) == 1);
                    taskList.add(task);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao buscar tarefas por data: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return taskList;
    }

    public int updateTaskCompletionStatus(int taskId, boolean isCompleted) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = 0;
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_IS_COMPLETED, isCompleted ? 1 : 0);
            rows = db.update(TABLE_TASKS, values, COLUNA_ID + " = ?", new String[]{String.valueOf(taskId)});
        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar status da tarefa: " + e.getMessage());
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return rows;
    }

    public void deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(TABLE_TASKS, COLUNA_ID + " = ?", new String[]{String.valueOf(id)});
        } catch (Exception e) {
            Log.e(TAG, "Erro ao deletar tarefa: " + e.getMessage());
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public int getTasksCount(String date, Boolean isCompleted) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int count = 0;
        try {
            String selection = COLUNA_DATA + " = ?";
            List<String> selectionArgs = new ArrayList<>();
            selectionArgs.add(date);

            if (isCompleted != null) {
                selection += " AND " + COLUMN_IS_COMPLETED + " = ?";
                selectionArgs.add(isCompleted ? "1" : "0");
            }

            cursor = db.query(
                    TABLE_TASKS,
                    new String[]{COLUNA_ID},
                    selection,
                    selectionArgs.toArray(new String[0]),
                    null,
                    null,
                    null
            );
            count = cursor.getCount();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao contar tarefas: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return count;
    }

    public int getTotalCompletedTasks() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = db.query(
                    TABLE_TASKS,
                    new String[]{COLUNA_ID},
                    COLUMN_IS_COMPLETED + " = 1",
                    null,
                    null,
                    null,
                    null
            );
            count = cursor.getCount();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao contar tarefas concluídas: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return count;
    }
}