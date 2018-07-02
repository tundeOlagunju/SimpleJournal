package com.example.android.simple_journal.ui;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.simple_journal.R;
import com.example.android.simple_journal.data.JournalContract;

import java.util.ArrayList;

public class JournalCustomCursorAdapter extends RecyclerView.Adapter<JournalCustomCursorAdapter.NoteViewHolder>{

    // Class variables for the Cursor that holds task data and the Context
    private Cursor mCursor;
    private Context mContext;
    public boolean isProductViewAsList;
    final private ListItemClickListener mOnClickListener;





    public interface ListItemClickListener {
        void onListItemClick(long itemId);
    }

    /**
     * Constructor for the CustomCursorAdapter that initializes the Context.
     *
     * @param mContext the current Context
     */
    public JournalCustomCursorAdapter(Context mContext,ListItemClickListener listener) {
        this.mContext = mContext;
        this.mOnClickListener = listener;

    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the task_layout to a view
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.list_item, parent, false);
      //  View v = LayoutInflater.from(parent.getContext()).inflate(isProductViewAsList ? R.layout.list_item : R.layout.grid_item, null);

        NoteViewHolder viewHolder = new NoteViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {

        int dateIndex = mCursor.getColumnIndex(JournalContract.JournalEntry.COLUMN_DATE);
        int timeIndex = mCursor.getColumnIndex(JournalContract.JournalEntry.COLUMN_TIME);
        int titleIndex = mCursor.getColumnIndex(JournalContract.JournalEntry.COLUMN_JOURNAL_TITLE);
        int noteIndex = mCursor.getColumnIndex(JournalContract.JournalEntry.COLUMN_JOURNAL_NOTE);


        mCursor.moveToPosition(position); // get to the right location in the cursor


        String date = mCursor.getString(dateIndex);
        String time = mCursor.getString(timeIndex);
        String title = mCursor.getString(titleIndex);
        String note = mCursor.getString(noteIndex);

        holder.dateView.setText(date);
        holder.titleView.setText(title);
        holder.noteView.setText(note);
        holder.timeView.setText(time);


        int backgroundColorForViewHolder = ColorUtils
                .getViewHolderBackgroundColorFromInstance(mContext, position);
        holder.itemView.setBackgroundColor(backgroundColorForViewHolder);



    }
    /**
     * Returns the number of items to display.
     */
    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    /**
     * When data changes and a re-query occurs, this function swaps the old Cursor
     * with a newly updated Cursor (Cursor c) that is passed in.
     */
    public Cursor swapCursor(Cursor c) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == c) {
            return null; // bc nothing has changed
        }
        Cursor temp = mCursor;
        this.mCursor = c; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (c != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }





    // Inner class for creating ViewHolders
    class NoteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Class variables for the task description and priority TextViews
        TextView dateView;
        TextView titleView;
        TextView noteView;
        TextView timeView;


        /**
         * Constructor for the TaskViewHolders.
         *
         * @param itemView The view inflated in onCreateViewHolder
         */
        public NoteViewHolder(View itemView) {
            super(itemView);

            dateView =  itemView.findViewById(R.id.date) ;
            titleView =  itemView.findViewById(R.id.title);
            noteView=  itemView.findViewById(R.id.note);
            timeView =  itemView.findViewById(R.id.time);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {

            long clickedPosition =  getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPosition + 1);

        }
    }
}
