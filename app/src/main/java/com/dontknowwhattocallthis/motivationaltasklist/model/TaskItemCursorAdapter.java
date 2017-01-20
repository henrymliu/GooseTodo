package com.dontknowwhattocallthis.motivationaltasklist.model;

import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dontknowwhattocallthis.motivationaltasklist.R;
import com.dontknowwhattocallthis.motivationaltasklist.TaskItem;
import com.woxthebox.draglistview.DragItemAdapter;

import java.text.DateFormat;
import java.util.ArrayList;

/**
 * Created by Cheng on 03/01/2017
 */

public class TaskItemCursorAdapter extends DragItemAdapter<TaskItem, TaskItemCursorAdapter.ViewHolder> {

    /*
    public TaskItemCursorAdapter(Context context, int layout, Cursor cursor, int flags){
        super(context, layout, cursor, flags);
    }*/
    private int mLayoutId;
    private int mGrabHandleId;
    private boolean mDragOnLongPress;

    public TaskItemCursorAdapter(Cursor mCursor, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        mDragOnLongPress = dragOnLongPress;
        setHasStableIds(true);
        setItemList(parseCursor(mCursor));
    }

    private ArrayList<TaskItem> parseCursor(Cursor mCursor){
        ArrayList<TaskItem> newList = new ArrayList<TaskItem>();
        try{
            while(mCursor.moveToNext()){
                TaskItem temp = new TaskItem(mCursor);
                newList.add(temp);
            }
        }
        finally{
            mCursor.close();
        }
        return newList;
    }

    public void setCursor(Cursor mCursor){
        // create a TaskItem Arraylist from the cursor
        setItemList(parseCursor(mCursor));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        String text = mItemList.get(position).getTitle();
        //String order = String.valueOf(mItemList.get(position).getOrder());
        holder.mText.setText(text);
        holder.itemView.setTag(mItemList.get(position).getID());

        if (mItemList.get(position).hasDate()) { //date, no time
            DateFormat dF = DateFormat.getDateInstance(DateFormat.LONG);

            StringBuilder stringDate = new StringBuilder();
            stringDate.append(dF.format(mItemList.get(position).getDueDate()));
            if(mItemList.get(position).hasTime()){ //date and time
                DateFormat tF = DateFormat.getTimeInstance(DateFormat.SHORT);
                stringDate.append(", " + tF.format(mItemList.get(position).getDueDate())); //maybe adjust this
            }
            if(mItemList.get(position).isOverdue()){ //TODO: update current time
                holder.dateText.setTextColor(Color.RED);
            }
            holder.dateText.setText(stringDate.toString());
        }

        else { //task name only
            //newTask.put("date", "");
            holder.dateText.setText("");
        }
    }

    @Override
    public long getItemId(int position) {
        return mItemList.get(position).getID();
    }

    public class ViewHolder extends DragItemAdapter.ViewHolder {
        public TextView mText;
        public TextView dateText;

        public ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId, mDragOnLongPress);
            mText = (TextView) itemView.findViewById(R.id.task_item_task_desc);
            dateText = (TextView) itemView.findViewById(R.id.task_item_task_date);
        }

        @Override
        public void onItemClicked(View view) {
            //Toast.makeText(view.getContext(), "Item clicked", Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onItemLongClicked(View view) {
            //Toast.makeText(view.getContext(), "Item long clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    /*
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Create a new TaskItem using the cursor
        TaskItem taskitem = new TaskItem(cursor);

        // Assign the relevant fields
        TextView title = (TextView) view.findViewById(R.id.task_item_task_desc);
        title.setText(taskitem.getTitle());

        TextView date = (TextView) view.findViewById(R.id.task_item_task_date);
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
        date.setText(dateFormat.format(taskitem.getDueDate()));
    }*/
}
