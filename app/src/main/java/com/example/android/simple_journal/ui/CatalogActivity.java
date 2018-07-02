package com.example.android.simple_journal.ui;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.simple_journal.LoginActivity;
import com.example.android.simple_journal.R;
import com.example.android.simple_journal.data.JournalContract;
import com.example.android.simple_journal.data.JournalDbHelper;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;

public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, JournalCustomCursorAdapter.ListItemClickListener {

    private static final String TAG = CatalogActivity.class.getSimpleName();
    public boolean isProductViewAsList;
    RecyclerView mRecyclerView;
    LinearLayoutManager linearLayoutManager;
    GridLayoutManager gridLayoutManager;
    FirebaseAuth auth;
    GoogleApiClient mGoogleApiClient;
    FirebaseAuth.AuthStateListener authStateListener;
    boolean canAddItem = false;
    // Member variables for the adapter and RecyclerView
    private JournalCustomCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        Toolbar mActionBarToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mActionBarToolbar);
        getSupportActionBar().setTitle("Simple Journal");
        mActionBarToolbar.setTitleTextAppearance(this, R.style.RobotoBoldTextAppearance);

        // Setup FAB to open EditorActivity
        final FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                startActivity(intent);
            }
        });


        // Set the RecyclerView to its corresponding view
        mRecyclerView = findViewById(R.id.recycler_item);

        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(this) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }

        };

        smoothScroller.setTargetPosition(1);

        // Set the layout for the RecyclerView to be a linear layout, which measures and
        // positions items within a RecyclerView into a linear list
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        // mRecyclerView.setScrollingTouchSlop(0);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.smoothScrollToPosition(0);
        linearLayoutManager.startSmoothScroll(smoothScroller);
        gridLayoutManager = new GridLayoutManager(this, 2);
        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new JournalCustomCursorAdapter(this, this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.smoothScrollToPosition(32);
        auth = FirebaseAuth.getInstance();


        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    startActivity(new Intent(CatalogActivity.this, LoginActivity.class));

                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // re-queries for all tasks
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        Log.v(TAG, "TEST:onCreateOptionsMenu called:");
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint("Search notes");
        //Note:
        //MenuItemCompat.OnActionExpandListener interface has a static implementation and
        //is not an instance method so it is called on its class
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(final MenuItem item) {
                CatalogActivity.this.setItemsVisibility(menu, searchItem, false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(final MenuItem item) {
                CatalogActivity.this.setItemsVisibility(menu, searchItem, true);
                MenuItem gridItem = menu.findItem(R.id.change_journal);
                gridItem.setVisible(true);
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Cursor cursor = getProductsListByKeyword(query);
                if (cursor == null) {
                    //   ImageView emptyImage = (ImageView) findViewById(R.id.empty_image);
                    //  emptyImage.setVisibility(View.GONE);
                    //   TextView emptyText = (TextView) findViewById(R.id.empty_state_text);
                    //  emptyText.setText("No items found ");
                } else {
                }
                mAdapter.swapCursor(cursor);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Cursor cursor = getProductsListByKeyword(newText);
                if (cursor != null) {
                    mAdapter.swapCursor(cursor);
                }
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v(TAG, "TEST:onOptionsItemSelected called:");
        switch (item.getItemId()) {
            case R.id.change_journal:
                isProductViewAsList = !isProductViewAsList;
                Log.v(TAG, "TEST:InvalidateOptionsMenu called:");
                mRecyclerView.setLayoutManager(isProductViewAsList ? linearLayoutManager : gridLayoutManager);
                gridLayoutManager.setReverseLayout(true);
                linearLayoutManager.setStackFromEnd(true);
                linearLayoutManager.setReverseLayout(true);
                mRecyclerView.setAdapter(mAdapter);
                mRecyclerView.setHasFixedSize(true);
                invalidateOptionsMenu();
                return true;
            case R.id.delete_all_products:
                showDeleteConfirmationDialog();
                return true;
            case R.id.sign_out:
                auth.signOut();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete all the notes");
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteAllNotes();
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.v(TAG, "TEST:onPrepareOptionsMenu called:");
        if (canAddItem) {
            menu.getItem(0).setIcon(R.drawable.ic_grid_svg);
            canAddItem = false;
        } else {
            menu.getItem(0).setIcon(R.drawable.ic_list);
            canAddItem = true;
        }
        return super.onPrepareOptionsMenu(menu);

    }

    private void deleteAllNotes() {
        int rowsDeleted = getContentResolver().delete(JournalContract.JournalEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from pet database");
    }

    @Override
    public void onListItemClick(long itemId) {

        Uri currentJournalUri = ContentUris.withAppendedId(JournalContract.JournalEntry.CONTENT_URI, itemId);
        Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
        // Set the URI on the data field of the intent
        intent.setData(currentJournalUri);
        startActivity(intent);
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                JournalContract.JournalEntry._ID,
                JournalContract.JournalEntry.COLUMN_JOURNAL_NOTE,
                JournalContract.JournalEntry.COLUMN_JOURNAL_TITLE,
                JournalContract.JournalEntry.COLUMN_DATE,
                JournalContract.JournalEntry.COLUMN_TIME};

        return new android.support.v4.content.CursorLoader(this,
                JournalContract.JournalEntry.CONTENT_URI,
                projection, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        if (data != null) mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

        mAdapter.swapCursor(null);
    }


    public Cursor getProductsListByKeyword(String search) {

        JournalDbHelper dbHelper = new JournalDbHelper(this);
        //Open connection to read only
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT  rowid as " +
                JournalContract.JournalEntry._ID + "," +
                JournalContract.JournalEntry.COLUMN_DATE + "," +
                JournalContract.JournalEntry.COLUMN_TIME + "," +
                JournalContract.JournalEntry.COLUMN_JOURNAL_TITLE + "," +
                JournalContract.JournalEntry.COLUMN_JOURNAL_NOTE +
                " FROM " + JournalContract.JournalEntry.TABLE_NAME +
                " WHERE " + JournalContract.JournalEntry.COLUMN_JOURNAL_TITLE + "  LIKE  '%" + search + "%' ";


        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void setItemsVisibility(final Menu menu, final MenuItem exception,
                                    final boolean visible) {
        for (int i = 0; i < menu.size(); ++i) {
            MenuItem item = menu.getItem(i);
            if (item != exception)
                item.setVisible(visible);
        }
    }
}


