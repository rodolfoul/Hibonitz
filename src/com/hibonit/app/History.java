package com.hibonit.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class History extends Activity {
	// ListView Reference
	private ListView mListView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history);
		this.mListView = (ListView) this.findViewById(android.R.id.list);

		new LoadHistoryTask(this).execute();
	}

	public void setAdapter(SumarioCorridaAdapter result) {
		this.mListView.setAdapter(result);
	}
}
