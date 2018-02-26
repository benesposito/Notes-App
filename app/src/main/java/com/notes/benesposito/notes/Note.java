package com.notes.benesposito.notes;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import static com.notes.benesposito.notes.R.id.editMainLabel;
import static com.notes.benesposito.notes.R.id.noteSelector;

/**
 * Created by benesposito on 2/19/2017.
 */

public class Note
{
    private String title;
    private String content;

    public Note(String title, String content)
    {
        this.title = title;
        this.content = content;
    }

    //Get/Set methods
    public String getTitle()
    {
        return title;
    }

    public String getContent()
    {
        return content;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setContent(String content)
    {
        this.content = content;
    }
}
