/*************************************************************************
 *     Author : Harshdeep Singh
 *
 *     This activity aids the instructor in creating the meeting invite.
 **************************************************************************/

package com.example.navjit.konnect.activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.navjit.konnect.R;
import com.example.navjit.konnect.model.MeetingInvite;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import cz.msebera.android.httpclient.HttpHeaders;
import cz.msebera.android.httpclient.entity.StringEntity;

import static android.provider.CalendarContract.Events.CONTENT_URI;

public class CreateMeetingActivity extends AppCompatActivity {

    private TextView mTitleTextView;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private TimePickerDialog.OnTimeSetListener mTimeSetListenerFrom;
    private TimePickerDialog.OnTimeSetListener mTimeSetListenerTo;

    private EditText mMeetingDateEditText;
    private EditText mMeetingTimeFromEditText;
    private EditText mMeetingTimeToEditText;
    private EditText mMeetingVenueEditText;
    private MeetingInvite meetingInvite;
    private String otherUserToken;
    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_meeting);
        meetingInvite = new MeetingInvite();
        mTitleTextView = findViewById(R.id.meeting_invite_title);
        if (getIntent() != null) {
            mTitleTextView.setText("Meeting with " + getIntent().getStringExtra("User"));
        }
        otherUserToken = getIntent().getStringExtra("Other User Token");
        currentUser = getIntent().getStringExtra("Current User");
        mMeetingDateEditText = findViewById(R.id.meeting_invite_date);
        mMeetingTimeFromEditText = findViewById(R.id.meeting_invite_time_from);
        mMeetingTimeToEditText = findViewById(R.id.meeting_invite_time_to);
        mMeetingVenueEditText = findViewById(R.id.meeting_invite_venue);

        mDateSetListener = (datePicker, year, month, dayOfMonth) -> setSelectedDate(year, month, dayOfMonth);

        mTimeSetListenerFrom = (TimePicker timePicker, int hourOfDay, int minute) -> {
            mMeetingTimeFromEditText.setText(getTimeString(hourOfDay, minute));
            meetingInvite.setStartHour(hourOfDay);
            meetingInvite.setStartMinute(minute);

        };

        mTimeSetListenerTo = (TimePicker timePicker, int hourOfDay, int minute) -> {
            mMeetingTimeToEditText.setText(getTimeString(hourOfDay, minute));
            meetingInvite.setEndHour(hourOfDay);
            meetingInvite.setEndMinute(minute);
        };
    }

    private String getTimeString(int hourOfDay, int minute) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm",
                getResources().getConfiguration().locale);
        Calendar calendar = new GregorianCalendar(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, hourOfDay, minute);
        return dateFormat.format(calendar.getTime());
    }

    private void setSelectedDate(int year, int month, int dayOfMonth) {
        meetingInvite.setDay(dayOfMonth);
        meetingInvite.setMonth(month);
        meetingInvite.setYear(year);
        String dateString = getDateString(year, month, dayOfMonth);
        mMeetingDateEditText.setText(dateString);
    }

    private String getDateString(int year, int month, int dayOfMonth) {
        Calendar calendar = new GregorianCalendar(year, month, dayOfMonth);
        Date meetingDate = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyy",
                getResources().getConfiguration().locale);
        return dateFormat.format(meetingDate);
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
        if (checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CALENDAR}, 0);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.
            Calendar beginTime = Calendar.getInstance();
            beginTime.set(meetingInvite.getYear(), meetingInvite.getMonth(), meetingInvite.getDay(),
                    meetingInvite.getStartHour(), meetingInvite.getStartMinute());

            Calendar endTime = Calendar.getInstance();
            endTime.set(meetingInvite.getYear(), meetingInvite.getMonth(), meetingInvite.getDay(),
                    meetingInvite.getEndHour(), meetingInvite.getEndMinute());

            Intent intent = new Intent(Intent.ACTION_INSERT)
                    .setData(CONTENT_URI)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                    .putExtra(CalendarContract.Events.TITLE, "Meeting with " + getIntent().getStringExtra("User"))
                    .putExtra(CalendarContract.Events.DESCRIPTION, mMeetingVenueEditText.getText().toString());

            startActivityForResult(intent, 1);
        }
//        CalendarEventsUtil.addEventToCalendar(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (isEventAdded(meetingInvite)) {
            String url = "https://fcm.googleapis.com/fcm/send";
            AsyncHttpClient client = new AsyncHttpClient();
            client.addHeader(HttpHeaders.AUTHORIZATION, "key=AIzaSyAiVsW00ommP7msOLZyiIrRvwMjfAeMs0A");
            client.addHeader(HttpHeaders.CONTENT_TYPE, RequestParams.APPLICATION_JSON);

            try {
                JSONArray registrationTokens = new JSONArray();

                registrationTokens.put(otherUserToken);

                JSONObject message = new JSONObject();
                message.put("registration_ids", registrationTokens);

                JSONObject notification = new JSONObject();
                notification.put("title", currentUser + " would like to meet you");
                notification.put("body", "on " + getDateString(meetingInvite.getYear(), meetingInvite.getMonth(), meetingInvite.getDay()) +
                        " from " + getTimeString(meetingInvite.getStartHour(), meetingInvite.getStartMinute()) + " to " + getTimeString(meetingInvite.getEndHour(), meetingInvite.getEndMinute()));

                JSONObject dataLoad = new JSONObject();
                dataLoad.put("startTime", meetingInvite.getMeetingStartTime().getTimeInMillis());
                dataLoad.put("endTime", meetingInvite.getMeetingEndTime().getTimeInMillis());
                dataLoad.put("venue", mMeetingVenueEditText.getText().toString());
                dataLoad.put("meetingTitle", "Meeting with " + currentUser);

                message.put("notification", notification);
                message.put("data", dataLoad);


                StringEntity entity = new StringEntity(message.toString());

                client.post(getApplicationContext(), url, entity, RequestParams.APPLICATION_JSON, new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {

                        Log.i("Create", responseString);
                    }

                    @Override
                    public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {

                        Log.i("CREATE", responseString);
                    }
                });
            } catch (Exception e) {

            }
        } else {

        }
    }

    private boolean isEventAdded(MeetingInvite meetingInvite) {
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(meetingInvite.getYear(), meetingInvite.getMonth(), meetingInvite.getDay(),
                meetingInvite.getStartHour(), meetingInvite.getStartMinute());

        Calendar endTime = Calendar.getInstance();
        endTime.set(meetingInvite.getYear(), meetingInvite.getMonth(), meetingInvite.getDay(),
                meetingInvite.getEndHour(), meetingInvite.getEndMinute());
        final String[] projection = new String[]{CalendarContract.Instances.BEGIN, CalendarContract.Instances.END, CalendarContract.Instances.TITLE};
        Cursor cursor = CalendarContract.Instances.query(getContentResolver(), projection, beginTime.getTimeInMillis(), endTime.getTimeInMillis());
        return cursor != null && cursor.moveToFirst() && cursor.getString(cursor.getColumnIndex(CalendarContract.Instances.TITLE)).equalsIgnoreCase("Meeting with " + getIntent().getStringExtra("User"));
    }
}
