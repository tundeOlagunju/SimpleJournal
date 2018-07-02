package com.example.android.simple_journal.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class JournalContract {

   public static final String CONTENT_AUTHORITY ="com.example.android.simple_journal";

   public static final Uri BASE_CONTENT_URI= Uri.parse("content://" + CONTENT_AUTHORITY);

   public static final String PATH_JOURNALS ="journals";

    private JournalContract(){}

    public static  abstract class JournalEntry implements  BaseColumns{

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI,PATH_JOURNALS);

        public static final String TABLE_NAME="journals";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_JOURNAL_TITLE= "Title";
        public static final String COLUMN_JOURNAL_NOTE ="Note";
        public static final String COLUMN_DATE ="Date";
        public static final String COLUMN_TIME ="Time";
        public static final String COLUMN_DAY ="Day";

    }
}

