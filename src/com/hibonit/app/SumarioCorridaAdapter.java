package com.hibonit.app;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SumarioCorridaAdapter extends ArrayAdapter<SumarioCorrida> {

	private int resource;
	private LayoutInflater inflater;

	public SumarioCorridaAdapter(Context ctx, int resourceId, List<SumarioCorrida> objects) {

		super(ctx, resourceId, objects);
		resource = resourceId;
		inflater = LayoutInflater.from(ctx);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		/* create a new view of my layout and inflate it in the row */
		convertView = (RelativeLayout) inflater.inflate(resource, null);

		/* Extract the city's object to show */
		SumarioCorrida sumarioCorrida = getItem(position);

		TextView duration_text = (TextView) convertView.findViewById(R.id.stopwatch_gera);
		duration_text.setText(sumarioCorrida.tempo);

		TextView flag_text = (TextView) convertView.findViewById(R.id.flag_gera);
		flag_text.setText(String.valueOf(sumarioCorrida.distancia));

		TextView calories_text = (TextView) convertView.findViewById(R.id.runner_gera);
		calories_text.setText(sumarioCorrida.calorias);

		return convertView;
	}

}
