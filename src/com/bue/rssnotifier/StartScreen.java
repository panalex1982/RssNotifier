package com.bue.rssnotifier;

import java.util.Calendar;

import com.bue.rssnotifier.services.RssNotificationListenerService;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class StartScreen extends Activity {
	private AlarmManager alarm;
	private PendingIntent pintent;

	private Button startNotificationsButton, cancelNotificationsbutton,updateButton;
	private TextView titleTextView, contentTextView;
	private EditText linkEditText;	
	
	private SharedPreferences spUpdate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Initialize gui
		setContentView(R.layout.activity_start_screen);
		startNotificationsButton = (Button) findViewById(R.id.startNotificationsButton);
		cancelNotificationsbutton = (Button) findViewById(R.id.cancelNotificationsbutton);
		updateButton= (Button) findViewById(R.id.updateButton);
		
		titleTextView = (TextView) findViewById(R.id.titleTextView);
		contentTextView = (TextView) findViewById(R.id.contentTextView);	
		
		linkEditText = (EditText) findViewById(R.id.linkEditText);
	
		
		//Get Extras If exist
		Intent intent=getIntent();
		if(intent.hasExtra("title")){
			titleTextView.setText(intent.getStringExtra("title"));
			contentTextView.setText(intent.getStringExtra("summary"));
		}
		

		//Listeners
		startNotificationsButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startNotifications();
			}
		});

		cancelNotificationsbutton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				stopNotifications();
			}
		});
		
		updateButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Editor editor=spUpdate.edit();
				editor.putString("feedLink",linkEditText.getText().toString());
				editor.commit();
			}
		});
		
		//Initialize Service Intent
		Intent serviceIntent = new Intent(this, RssNotificationListenerService.class);
		pintent = PendingIntent.getService(this, 0, serviceIntent, 0);
		
		//Initialize Shared Preferences
		spUpdate = getSharedPreferences("rssUpdate", 0);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.start_screen, menu);
		return true;
	}

	private void startNotifications() {
		Calendar cal = Calendar.getInstance();
		
		alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		// Start every 30 seconds
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
				60 * 1000, pintent);
	}

	private void stopNotifications() {
		alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarm.cancel(pintent);		
	}

}
