package com.example.android.simple_journal.data;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/** Class that prevents the direct interaction of UI code with the database
 *  UI code interacts with the Provider which in turn interacts with the database(JournalDbHelper)
 */
public class JournalProvider extends ContentProvider {


    private static final String LOG_TAG = JournalProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the journals table */
    private static final int JOURNAL = 100;

    /** URI matcher code for the content URI for a single journal in the journals table */
    private static final int JOURNAL_ID = 101;


    /** UriMatcher matches a URI to the corresponding code**/
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /**Set up the Uri Matcher with the Uri pattern and assign corresponding integer code**/

    // Static initializer. This is run the first time anything is called from this class.
    static{
        /** Provides access to multiple rows in the journal table
         *  attach the UriPattern with its code
         * **/
        sUriMatcher.addURI(JournalContract.CONTENT_AUTHORITY,JournalContract.PATH_JOURNALS,JOURNAL);

        /** Provides access to a single row in the journal table
         *  attach this UriPattern with its code
         * **/
        sUriMatcher.addURI(JournalContract.CONTENT_AUTHORITY,JournalContract.PATH_JOURNALS + "/#",JOURNAL_ID);
    }


    /** Database helper object */
    private JournalDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        //To access the database,we instantiate the subclass of SQLiteOpenHelper
        //and pass in the context
        mDbHelper = new JournalDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        //open a database(if it exists) or create a new one by calling onCreate in SQLiteOpenHelper to read from it
        //just like this command (.open inventory.db) in the terminal
        SQLiteDatabase database =  mDbHelper.getReadableDatabase();

        //this cursor holds the result of the query
        Cursor cursor;

        //Figure out if the Uri can match to a specific code
        int match =  sUriMatcher.match(uri);


        switch (match){

            // if it matches PRODUCTS code,query the whole table
            case JOURNAL:
                sortOrder = JournalContract.JournalEntry._ID +" ASC";
                cursor = database.query(JournalContract.JournalEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            //if the code matches PRODUCT_ID code ,query a single row corresponding to the id in the database
            case JOURNAL_ID:

                // Extract out the ID of the requested row from the URI
                selection = JournalContract.JournalEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                //query the products where the _id corresponds to the extracted id

                cursor =   database.query(JournalContract.JournalEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;

            // If the Uri do not match the templates throw an exception
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        //cursors have an attribute called notification uri
        // so we know what content URI the Cursor was created for.
        //once a Uri is setup to be notified,we will know when the data at this uri changes
        //so that know we will need to update the cursor
        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        //return the cursor containing the results from the query
        return cursor;


    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }


    /**
     * Insert Data into the database
     */

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        //figure out what code the Uri matches to.It can only match to PRODUCTS code
        //because we can only insert into the products table
        // and it does not make sense to insert product here it exists already,we can only update
        final  int match = sUriMatcher.match(uri);
        switch(match){
            case JOURNAL:
                // Calls the insertProduct helper method to insert data into the database
                return insertJournal(uri, contentValues);
            default:
                throw  new IllegalArgumentException("Insertion is not supported for this" + uri );

        }

    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case JOURNAL:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(JournalContract.JournalEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case JOURNAL_ID:
                // Delete a single row given by the ID in the URI
                selection = JournalContract.JournalEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(JournalContract.JournalEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {

        //Figure out if the Uri can match to a specific code
        int match =  sUriMatcher.match(uri);


        switch(match) {
            // if it matches PRODUCTS code,update the whole table
            case JOURNAL:

                return updateJournal(uri,contentValues,selection,selectionArgs);
            //if the code matches PRODUCT_ID code ,update a single row corresponding to the id in the table
            case JOURNAL_ID:
                // Extract out the ID of the requested row from the URI
                selection = JournalContract.JournalEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // Now call helper method updateJournal()
                return updateJournal(uri, contentValues, selection, selectionArgs);
            // If the Uri do not match the templates throw an exception
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     *
     * Helper method that inserts data into the database and returns the Uri for the inserted row
     */

    private Uri insertJournal(Uri uri, ContentValues values) {

        // Check that the journal title is not null. I think that extraction data from the input may result in
        // inserting an "empty" string, which is different from null.
        // That is why I also check for an "empty" string: length() == 0
        String journalTitle = values.getAsString(JournalContract.JournalEntry.COLUMN_JOURNAL_TITLE);
        if (journalTitle == null || journalTitle.length() == 0) {
            throw new IllegalArgumentException("Insert Exception! Product requires a product name!");
        }
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new product with the given values
        // Return long with the primary key value of the new row
        long id = database.insert(JournalContract.JournalEntry.TABLE_NAME, null, values);

        // If the the primary key value is -1,
        // then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the product content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the primary key value (id) of the new row in the table,
        // append it to the uri argument and assign the appended uri to a new Uri variable
        Uri returnedUri = ContentUris.withAppendedId(uri, id);

        return returnedUri;
    }

    /**
     * Helper method that inserts data into the database and returns the Uri for the inserted row
     *  * Return the number of rows that were successfully updated.
     */
    private int updateJournal(Uri uri, ContentValues values, String selection, String[] selectionArgs){

        // check that the title value is not null.
        if (values.containsKey(JournalContract.JournalEntry.COLUMN_JOURNAL_TITLE)) {
            String name = values.getAsString(JournalContract.JournalEntry.COLUMN_JOURNAL_TITLE);
            if (name == null) {
                throw new IllegalArgumentException("Journal requires a title");
            }
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(JournalContract.JournalEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

}

