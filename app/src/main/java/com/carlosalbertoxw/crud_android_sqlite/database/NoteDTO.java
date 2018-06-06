package com.carlosalbertoxw.crud_android_sqlite.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.carlosalbertoxw.crud_android_sqlite.models.Note;

import java.util.ArrayList;
import java.util.List;

public class NoteDTO {
    private SQLiteDatabase sqliteDataBase;
    private DataBase dataBase;
    private String[] allColumns = { dataBase.COLUMN_ID,
            dataBase.COLUMN_TITLE,
            dataBase.COLUMN_TEXT
    };

    public NoteDTO(Context context){
        dataBase = new DataBase(context);
    }

    public long saveNote(Note note) {
        sqliteDataBase = dataBase.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(DataBase.COLUMN_TITLE, note.getTitle());
        values.put(DataBase.COLUMN_TEXT, note.getText());
        long id = sqliteDataBase.insert(dataBase.TABLE_NOTES, null, values);
        sqliteDataBase.close();
        return id;
    }

    public List<Note> getNotes() {
        List<Note> list = new ArrayList<Note>();
        sqliteDataBase = dataBase.getWritableDatabase();
        Cursor cursor = sqliteDataBase.query(DataBase.TABLE_NOTES, allColumns, null, null, null, null, null);
        while (cursor.moveToNext()) {
            Note item= new Note();
            item.setId(cursor.getLong(0));
            item.setTitle(cursor.getString(1));
            item.setText(cursor.getString(2));
            list.add(item);
        }
        cursor.close();
        sqliteDataBase.close();
        return list;
    }

    public int updateNote(Note note){
        sqliteDataBase = dataBase.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DataBase.COLUMN_TITLE, note.getTitle());
        values.put(DataBase.COLUMN_TEXT, note.getText());
        String where = "id=?";
        String[] whereargs = new String[]{String.valueOf(note.getId())};
        int numRows = sqliteDataBase.update(dataBase.TABLE_NOTES, values, where, whereargs);
        sqliteDataBase.close();
        return numRows;
    }

    public int deleteNote(Note note){
        sqliteDataBase = dataBase.getWritableDatabase();
        String where = "id=?";
        String[] whereargs = new String[]{String.valueOf(note.getId())};
        int numRows = sqliteDataBase.delete(dataBase.TABLE_NOTES,where,whereargs);
        dataBase.close();
        return numRows;
    }
}
