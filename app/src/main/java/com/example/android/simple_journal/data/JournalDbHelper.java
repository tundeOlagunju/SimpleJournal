package com.example.android.simple_journal.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



/** This class helps to create the database in sQLite
 * Helps to create open and manage database connection
 * **/
public class JournalDbHelper extends SQLiteOpenHelper {

    /** Name of the database file**/
    public static final String DATA_BASE_NAME = "journal.db" ;


    /**
     * Database version.
     */
    public static final int DATA_BASE_VERSION = 1;

    public JournalDbHelper(Context context){
        super(context,DATA_BASE_NAME,null,DATA_BASE_VERSION);
    }



    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {


        String SQL_CREATE_ENTRIES = "CREATE TABLE " + JournalContract.JournalEntry.TABLE_NAME + "("
                + JournalContract.JournalEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + JournalContract.JournalEntry.COLUMN_JOURNAL_TITLE + " TEXT NOT NULL,"
                + JournalContract.JournalEntry.COLUMN_JOURNAL_NOTE + " TEXT,"
                + JournalContract.JournalEntry.COLUMN_DATE + " DEFAULT CURRENT_TIME,"
                + JournalContract.JournalEntry.COLUMN_TIME+ " DEFAULT CURRENT_DATE,"
                + JournalContract.JournalEntry.COLUMN_DAY + " TEXT );";

        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + JournalContract.JournalEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
