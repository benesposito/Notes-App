package com.notes.benesposito.notes;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static android.R.attr.name;

public class MainActivity extends AppCompatActivity
{
    DatabaseManager dbManager;
    SQLiteDatabase db;

    private Button save;
    private Button clear;
    private Button delete;
    private Button newNote;
    private TextView debugLabel;
    private EditText editMainLabel;
    private EditText newNoteName;
    private Spinner noteSelector;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Instantiating
        dbManager = new DatabaseManager(this);
        db = dbManager.getWritableDatabase();
        save = (Button) findViewById(R.id.save);
        clear = (Button) findViewById(R.id.clear);
        delete = (Button) findViewById(R.id.delete);
        newNote = (Button) findViewById(R.id.newNote);
        debugLabel = (TextView) findViewById(R.id.debugLabel);
        editMainLabel = (EditText) findViewById(R.id.editMainLabel);
        newNoteName = (EditText) findViewById(R.id.newNoteName);
        noteSelector = (Spinner) findViewById(R.id.noteSelector);

        editMainLabel.setText(getText());

        ArrayList<Note> notes = new ArrayList<Note>();



        save.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                saveText();
                /*
                String text = editMainLabel.toString();
                debugLabel.setText("Size: " + text.length() + "\n" +
                                    "lastIndex() of \\n: " + text.lastIndexOf("\n") + "\n" +
                                    "charAt(.length - 1): " + text.charAt(text.length() - 1));
                */
            }
        });

        clear.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                editMainLabel.setText("");
            }
        });

        newNote.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                String name = newNoteName.getText().toString();

                boolean exists = rowExists(name);

                if(name != null && !name.equals("") && !exists)
                {
                    ContentValues values = new ContentValues();
                    values.put(DatabaseManager.ROW_NAME, name);
                    values.put(DatabaseManager.ROW_CONTENT, "");

                    db.insert(DatabaseManager.TABLE_NAME, null, values);
                    updateNotesList();
                    noteSelector.setSelection(noteSelector.getCount() - 1);
                }
                else
                    newNoteName.setHint("[Invalid Name]");
            }
        });

        delete.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                db.delete(DatabaseManager.TABLE_NAME, DatabaseManager.ROW_NAME + " LIKE ?", new String[]{noteSelector.getSelectedItem().toString()});
                updateNotesList();
            }
        });

        noteSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Cursor cursor = db.rawQuery("SELECT " + DatabaseManager.ROW_CONTENT + " FROM " + DatabaseManager.TABLE_NAME +
                            " WHERE " + DatabaseManager.ROW_CONTENT + "=" + noteSelector.getSelectedItem().toString(), null);

                    if(cursor.moveToFirst())
                    {
                        while(!cursor.isAfterLast())
                        {
                            editMainLabel.setText(cursor.toString());
                            cursor.moveToNext();
                        }
                    }
                } catch (Exception e) {
                    editMainLabel.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        updateNotesList();
    }

    protected void updateNotesList()
    {
        ArrayList<String> options = new ArrayList<String>();

        Cursor cursor = db.rawQuery("SELECT " + DatabaseManager.ROW_NAME + " FROM " + DatabaseManager.TABLE_NAME, null);

        if(cursor.moveToFirst())
        {
            while(!cursor.isAfterLast())
            {
                options.add(cursor.getString(cursor.getColumnIndex(DatabaseManager.ROW_NAME)));
                cursor.moveToNext();
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, options);
        noteSelector.setAdapter(adapter);
    }

    protected boolean rowExists(String name)
    {
        boolean exists = false;

        if(noteSelector.getCount() > 0) {
            Cursor cursor = db.query(
                    DatabaseManager.TABLE_NAME,
                    new String[]{DatabaseManager.ROW_NAME},
                    DatabaseManager.ROW_NAME + " = ?",
                    new String[]{noteSelector.getSelectedItem().toString()},
                    null,
                    null,
                    DatabaseManager.ROW_ID + " DESC"
            );

            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    if (name.equals(cursor.toString()))
                        exists = true;
                    cursor.moveToNext();
                }
            }
        }

        return exists;
    }

    protected void saveText()
    {
        ContentValues values = new ContentValues();
        values.put(DatabaseManager.ROW_NAME, noteSelector.getSelectedItem().toString());
        values.put(DatabaseManager.ROW_CONTENT, editMainLabel.getText().toString());

        if(rowExists(noteSelector.getSelectedItem().toString()))
            db.replace(DatabaseManager.TABLE_NAME, null, values);
        else
            db.insert(DatabaseManager.TABLE_NAME, null, values);
    }

    protected String getText()
    {
        String text = "";

        try{
            Cursor cursor = db.rawQuery("SELECT " + DatabaseManager.ROW_CONTENT + " FROM " + DatabaseManager.TABLE_NAME +
                    " WHERE " + DatabaseManager.ROW_NAME + "=" + noteSelector.getSelectedItem().toString(), null);

            if(cursor.moveToFirst())
            {
                while(!cursor.isAfterLast())
                {
                    text = cursor.toString();
                    cursor.moveToNext();
                }
            }
        } catch (Exception e)
        {

        }

        return text;
    }

    public class DatabaseManager extends SQLiteOpenHelper
    {
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_FILENAME = "Notes.db";

        public static final String TABLE_NAME = "notes";
        public static final String ROW_ID = "_id";
        public static final String ROW_NAME = "name";
        public static final String ROW_CONTENT = "content";

        public DatabaseManager(Context context)
        {
            super(context, DATABASE_FILENAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + "(" +
                    ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ROW_NAME + " TEXT NOT NULL," +
                    ROW_CONTENT + " TEXT NOT NULL)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}
