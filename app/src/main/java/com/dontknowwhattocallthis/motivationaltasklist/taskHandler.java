package com.dontknowwhattocallthis.motivationaltasklist;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.dontknowwhattocallthis.motivationaltasklist.model.TaskItemCursorAdapter;
import com.dontknowwhattocallthis.motivationaltasklist.model.TaskItemSQL;
import com.dontknowwhattocallthis.motivationaltasklist.persistence.TaskDBHelper;

import java.util.ArrayList;
import java.util.Calendar;

//import android.text.format.DateFormat;

/**
 * Created by Henry on 1/4/2017.
 */

public class taskHandler {
    private TaskItem task;
    private Context ctx;
    private ArrayList<TaskItem> taskData;
    private TaskItemCursorAdapter adapter;
    private TaskDBHelper tDBHelper;
    private Calendar currCal = Calendar.getInstance();
    private TaskItem undoTask;

    public taskHandler(Context ctx, ArrayList<TaskItem> taskData, TaskDBHelper tDB, TaskItemCursorAdapter adapter){
        this.ctx = ctx;
        this.taskData = taskData;
        this.adapter = adapter;
        this.tDBHelper = tDB;
    }

    //creates dialog box to create task title
    public void addNewTask(){

        // final String taskName;
        //String taskDate = "jsdflkj"
        task = new TaskItem();
        final AlertDialog.Builder getTaskTitleBuilder = new AlertDialog.Builder(ctx);
        getTaskTitleBuilder.setTitle("Create new task");

        //create text field
        final EditText titleInput = new EditText(ctx);
        titleInput.setInputType(InputType.TYPE_CLASS_TEXT);
        titleInput.setHint("Feed the cat");
        //set view in dialog box
        getTaskTitleBuilder.setView(titleInput);
        getTaskTitleBuilder.setPositiveButton("Select date", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id){
                String taskName = titleInput.getText().toString();
                task.setName(taskName);
                setTaskDate();
                dialog.dismiss();
            }
        });
        getTaskTitleBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id){
                dialog.cancel();
            }
        });
        getTaskTitleBuilder.setNeutralButton("Create", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id){
                String taskName = titleInput.getText().toString();
                task.setName(taskName);
                task.setUseDate(false);task.setUseTime(false);
                updateAddData(task);
                dialog.dismiss();
            }
        });
        final AlertDialog getTaskTitleDialog = getTaskTitleBuilder.create();
        getTaskTitleDialog.show();
        getTaskTitleDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        getTaskTitleDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(false);

        //do not allow empty task names
        titleInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length() == 0){
                    getTaskTitleDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    getTaskTitleDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(false);
                }
                else{
                    getTaskTitleDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    getTaskTitleDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    //Launches datepicker dialog to select task date
    private void setTaskDate(){
        //initialize calendar to current date

        int cYear = currCal.get(Calendar.YEAR);
        int cMonth = currCal.get(Calendar.MONTH);
        int cDay = currCal.get(Calendar.DAY_OF_MONTH);

        final DatePickerDialog dpDialog = new DatePickerDialog(ctx,null,cYear,cMonth,cDay);
        dpDialog.getDatePicker().setMinDate(System.currentTimeMillis()-1000);
        dpDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Select time", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                DatePicker dp = dpDialog.getDatePicker();
                int uYear = dp.getYear();
                int uMonth = dp.getMonth(); //remember months go from 0-11
                int uDay = dp.getDayOfMonth();
               // Calendar cal = Calendar.getInstance();
                setTaskTime(new int[]{uYear,uMonth,uDay});
                task.setUseDate(true);
                dialog.dismiss();

            }
        });
        dpDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

            }
        });
        dpDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                DatePicker dp = dpDialog.getDatePicker();
                int uYear = dp.getYear();
                int uMonth = dp.getMonth(); //remember months go from 0-11
                int uDay = dp.getDayOfMonth();
                Calendar cal = Calendar.getInstance();
                cal.set(uYear,uMonth,uDay,23,59,59); //set time to 23:59:59
                task.setDate(cal.getTime());
                task.setUseDate(true);
                task.setUseTime(false);
                updateAddData(task);
                dialog.dismiss();
            }
        });
        dpDialog.show();
    }

    //Creates time picker dialog, with values passed from datepicker to create milli time
    private void setTaskTime(int[] dateParam){
        final int[] dateArr = dateParam;
        final TimePickerDialog tpDialog = new TimePickerDialog(ctx,new TimePickerDialog.OnTimeSetListener(){
            @Override
            public void onTimeSet(TimePicker tP, int uHour, int uMinute){
                Calendar cal = Calendar.getInstance();
                cal.set(dateArr[0], dateArr[1],dateArr[2], uHour, uMinute);
                task.setDate(cal.getTime());
                task.setUseTime(true);
                updateAddData(task);

            }
        }, currCal.get(Calendar.HOUR_OF_DAY), currCal.get(Calendar.MINUTE),false);
        tpDialog.show();
    }

    //Adds a task to database and notifies adapter
    private void updateAddData(TaskItem t) {
        /*
        HashMap<String, String> newTask = new HashMap<String, String>(2);
        newTask.put("task", task.getTitle());

        if (task.hasDate()) { //date, no time
            DateFormat dF = DateFormat.getDateInstance(DateFormat.LONG);

            StringBuilder stringDate = new StringBuilder();
            stringDate.append(dF.format(task.getDueDate()));
            if(task.hasTime()){ //date and time
                DateFormat tF = DateFormat.getTimeInstance(DateFormat.SHORT);
                stringDate.append(", " + tF.format(task.getDueDate())); //maybe adjust this
            }
            newTask.put("date", stringDate.toString());
        }

        else { //task name only
            newTask.put("date", "");
        }
        */
        taskData.add(t);
        t.writeToDataBase(tDBHelper);
        Cursor c = TaskItemSQL.getAllTaskItems(tDBHelper);
        adapter.setCursor(c);
        adapter.notifyDataSetChanged();

    }

    //Removes task and stores it in undo field; deletes from database
    public void updateRemoveData(Long pos){
        undoTask = TaskItemSQL.deleteTaskItem(tDBHelper,pos);
        undoTask.deleteFromDataBase(tDBHelper);
        Cursor c = TaskItemSQL.getAllTaskItems(tDBHelper);
        adapter.setCursor(c);
        adapter.notifyDataSetChanged();

    }

    //Restores recently deleted task to list
    public void addUndoTask(){
        assert(undoTask != null);
        updateAddData(undoTask);
    }

}
