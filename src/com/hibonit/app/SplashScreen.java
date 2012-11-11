package com.hibonit.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashScreen extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.splash);

        // thread for displaying the SplashScreen
		final Thread splashThread = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized(this){

                            //wait 5 sec
                            wait(5000);
                    }

                } catch(InterruptedException e) {}
                finally {
                    finish();

                    //start a new activity
                    Intent i = new Intent();
                    i.setClass(getBaseContext(), MainActivity.class);
                    startActivity(i);
                }
            }
        };

        splashThread.start();
	}
}
