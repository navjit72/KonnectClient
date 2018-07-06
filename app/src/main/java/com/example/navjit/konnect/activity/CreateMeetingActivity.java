package com.example.navjit.konnect.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.navjit.konnect.R;
import com.example.navjit.konnect.util.CalendarEventsUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class CreateMeetingActivity extends AppCompatActivity {

    private TextView mTitleTextView;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private TimePickerDialog.OnTimeSetListener mTimeSetListenerFrom;
    private TimePickerDialog.OnTimeSetListener mTimeSetListenerTo;

    private EditText mMeetingDateEditText;
    private EditText mMeetingTimeFromEditText;
    private EditText mMeetingTimeToEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_meeting);
        mTitleTextView = findViewById(R.id.meeting_invite_title);
        if (getIntent() != null) {
            mTitleTextView.setText("Meeting with " + getIntent().getStringExtra("User"));
        }

        mMeetingDateEditText = findViewById(R.id.meeting_invite_date);
        mMeetingTimeFromEditText = findViewById(R.id.meeting_invite_time_from);
        mMeetingTimeToEditText = findViewById(R.id.meeting_invite_time_to);

        mDateSetListener = (datePicker, year, month, dayOfMonth) -> setSelectedDate(year, month, dayOfMonth);

        mTimeSetListenerFrom = (TimePicker timePicker, int hourOfDay, int minute) -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm",
                    getResources().getConfiguration().locale);
            Calendar calendar = new GregorianCalendar(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, hourOfDay, minute);
            String timString = dateFormat.format(calendar.getTime());
            mMeetingTimeFromEditText.setText(timString);

        };

        mTimeSetListenerTo = (TimePicker timePicker, int hourOfDay, int minute) -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm",
                    getResources().getConfiguration().locale);
            Calendar calendar = new GregorianCalendar(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, hourOfDay, minute);
            String timString = dateFormat.format(calendar.getTime());
            mMeetingTimeToEditText.setText(timString);
        };
    }

    private void setSelectedDate(int year, int month, int dayOfMonth) {
        Calendar calendar = new GregorianCalendar(year, month, dayOfMonth);
        Date meetingDate = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyy",
                getResources().getConfiguration().locale);
        String dateString = dateFormat.format(meetingDate);
        mMeetingDateEditText.setText(dateString);
    }

    public static void launch(Context context) {
        Intent intent = new Intent(context, CreateMeetingActivity.class);
        context.startActivity(intent);
    }

    public static void launch(Context context, Bundle data) {
        Intent intent = new Intent(context, CreateMeetingActivity.class);
        intent.putExtras(data);
        context.startActivity(intent);
    }

    public void showDatePicker(View view) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this,
                R.style.Theme_AppCompat_Light_Dialog_Alert,
                mDateSetListener,
                year, month, dayOfMonth);
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    public void showTimePicker(View view) {
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog dialog;

        switch (view.getId()) {

            case R.id.btn_meeting_invite_time_from:
                dialog = new TimePickerDialog(this,
                        R.style.Theme_AppCompat_Light_Dialog_Alert,
                        mTimeSetListenerFrom,
                        hourOfDay, minute,
                        false);
                break;
            case R.id.btn_meeting_invite_time_to:
            default:
                dialog = new TimePickerDialog(this,
                        R.style.Theme_AppCompat_Light_Dialog_Alert,
                        mTimeSetListenerTo,
                        hourOfDay, minute,
                        false);
        }
        dialog.show();
    }

    public void createMeeting(View view) {
        CalendarEventsUtil.addEventToCalendar(this);
    }
}
