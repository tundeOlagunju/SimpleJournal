package com.example.android.simple_journal.ui;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.simple_journal.R;
import com.example.android.simple_journal.data.JournalContract;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private EditText mTitleEditText;
    private EditText mNoteEditText;
    private TextView mDateText;
    private TextClock mTimeText;
    private Uri mCurrentNoteUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mTitleEditText = findViewById(R.id.titleEdit);
        mNoteEditText =   findViewById(R.id.noteEdit);
        mDateText =   findViewById(R.id.dateView);
        mTimeText =    findViewById(R.id.textClock);


        DateFormat dateF = DateFormat.getDateInstance();
        String date = dateF.format(new Date());
        mDateText.setText(date);

        Intent intent = getIntent();
        mCurrentNoteUri = intent.getData();

        // If the intent DOES NOT contain a pet content URI, then we know that we are
        // creating a new pet.
        if (mCurrentNoteUri == null) {
            // This is a new pet, so change the app bar to say "Add a Pet"
            setTitle("Write New");
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing pet, so change app bar to say "Edit Pet"
            setTitle("Edit Note");
           // LinearLayout linearLayout = findViewById(R.id.linearLayout);
            //linearLayout.setBackgroundColor(ColorUtils
              //     .getViewHolderBackgroundColorFromInstance(this, position));

            // Initialize a loader to read the pet data from the database
            // and display the current values in the editor
                getSupportLoaderManager().initLoader(0,null,this);
        }

/*

        Calendar cal = Calendar.getInstance();
        DateFormat outputFormat = new SimpleDateFormat("KK:mm a");
        String time = outputFormat.format(cal.getTime());
        mTimeText.setText(time);
        */

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.save_journal:
                saveJournal();
                finish();
                return true;
            case R.id.delete_journal:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void saveJournal(){

        String title = mTitleEditText.getText().toString().trim();
        String note = mNoteEditText.getText().toString().trim();
        String currentDate = mDateText.getText().toString().trim();
        String currentTime =(String) mTimeText.getText();


        if (mCurrentNoteUri == null && TextUtils.isEmpty(title) && TextUtils.isEmpty(note)) {
            return;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(JournalContract.JournalEntry.COLUMN_DATE,currentDate);
        contentValues.put(JournalContract.JournalEntry.COLUMN_TIME,currentTime);
        contentValues.put(JournalContract.JournalEntry.COLUMN_JOURNAL_NOTE,note);
        contentValues.put(JournalContract.JournalEntry.COLUMN_JOURNAL_TITLE,title);

        if(mCurrentNoteUri == null) {
            Uri newUri = getContentResolver().insert(JournalContract.JournalEntry.CONTENT_URI, contentValues);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, "Failed to save note",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, "Note saved successfully",
                        Toast.LENGTH_SHORT).show();
            }
        }else{

            int rowsAffected = getContentResolver().update(mCurrentNoteUri, contentValues, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this,"Note Update failed",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this,"Note Updated Succesfully",
                        Toast.LENGTH_SHORT).show();
            }

        }

    }


    /**
     * Prompt the user to confirm that they want to delete this pet.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteJournal();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteJournal() {

        if (mCurrentNoteUri != null) {

            int rowsDeleted = getContentResolver().delete(mCurrentNoteUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this,"Note Deletion Failed",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, "Note deleted",
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String [] projection = {
                JournalContract.JournalEntry._ID,
                JournalContract.JournalEntry.COLUMN_JOURNAL_NOTE,
                JournalContract.JournalEntry.COLUMN_JOURNAL_TITLE,
                JournalContract.JournalEntry.COLUMN_DATE,
                JournalContract.JournalEntry.COLUMN_TIME};

        return new android.support.v4.content.CursorLoader(this,
                mCurrentNoteUri,
                projection,null,null,null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            int dateIndex = cursor.getColumnIndex(JournalContract.JournalEntry.COLUMN_DATE);
            int timeIndex = cursor.getColumnIndex(JournalContract.JournalEntry.COLUMN_TIME);
            int titleIndex = cursor.getColumnIndex(JournalContract.JournalEntry.COLUMN_JOURNAL_TITLE);
            int noteIndex = cursor.getColumnIndex(JournalContract.JournalEntry.COLUMN_JOURNAL_NOTE);

            String date = cursor.getString(dateIndex);
            String time = cursor.getString(timeIndex);
            String title = cursor.getString(titleIndex);
            String note = cursor.getString(noteIndex);
            // Update the views on the screen with the values from the database
            mDateText.setText(date);
            mTimeText.setText(time);
            mTitleEditText.setText(title);
            mNoteEditText.setText(note);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mDateText.setText("");
        mTimeText.setText("");
        mTitleEditText.setText("");
        mNoteEditText.setText("");
    }
}
