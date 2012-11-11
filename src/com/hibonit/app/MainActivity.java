package com.hibonit.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	private Button historicButton;
	private Button newjogButton;
	private Button syncButton;

	private boolean splashViewed = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			Class.forName("com.hibonit.app.LoadHistoryTask");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		setContentView(R.layout.main);

		historicButton = (Button) findViewById(R.id.history);
		historicButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), History.class);
				startActivity(i);
			}
		});

		newjogButton = (Button) findViewById(R.id.new_jog);
		newjogButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Intent i = new Intent(getApplicationContext(), NewJog.class);
				Intent i = new Intent(getApplicationContext(), GPSActivity.class);
				startActivity(i);
			}
		});

		syncButton = (Button) findViewById(R.id.sync);
		syncButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), Synchronize.class);
				startActivity(i);
			}
		});

//		if (splashViewed == false) {
//			splashViewed = true;
//			Intent i = new Intent(getApplicationContext(), SplashScreen.class);
//			startActivity(i);
//		}

		Evernote.init(this);
		Evernote.login();
	}

	@Override
	public void onResume() {
		super.onResume();
		Evernote.terminarLogin();
	}
}