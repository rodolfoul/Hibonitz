package com.hibonit.app;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class GPSActivity extends Activity implements SensorListener {
	private SensorManager sensorMgr;
	private long lastUpdate = -1;
	private float x, y, z;
	private float last_x, last_y, last_z;
	private static final int SHAKE_THRESHOLD = 800;

	protected final String KML_START = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n" + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">" + "\n"
			+ "<Document>" + "\n" + "<name>Paths</name>" + "<description>KML</description>" + "\n" + "<Style id=\"yellowLineGreenPoly\">" + "\n"
			+ "<LineStyle>" + "\n" + "<color>7f00ffff</color>" + "\n" + "<width>4</width>" + "\n" + "</LineStyle>" + "\n"
			+ "<PolyStyle><color>7f00ff00</color></PolyStyle>" + "\n" + "</Style>" + "\n" + "<Placemark>" + "<name>Absolute Extruded</name>"
			+ "<description>kml</description>" + "\n" + "<styleUrl>#yellowLineGreenPoly</styleUrl>" + "\n" + "<LineString>" + "\n"
			+ "<extrude>1</extrude>" + "\n" + "<tessellate>1</tessellate>" + "\n" + "<altitudeMode>absolute</altitudeMode>" + "\n" + "<coordinates>";

	protected final String KML_END = "</coordinates>" + "\n" + "</LineString>" + "\n" + "</Placemark>" + "\n" + "</Document>" + "\n" + "</kml>";

	public static String BROADCAST_ACTION = "com.hibonit.broadcast";
	public static String BROADCAST_ACTION_END = "com.hibonit.broadcast.end";
	public static String BROADCAST_ACTION_KM = "com.hibonit.broadcast.km";
	public static String LATITUDE = "latitude";
	public static String LONGITUDE = "longitude";
	public static String ALTITUDE = "altitude";
	public static String KM = "km";
	public static String DISTANCE = "distance";
	public static String AVERAGE_SPEED = "avgspeed";
	public static String AVERAGE_SPEED_PER_KM = "avgspeedkm";
	public static String INSTANT_SPEED = "instspeed";
	public static String KML_DATA = "kmldata";

	private MyBroadcastReceiver mBroadcastReceiver;
	private LocationInterface locationInterface;
	private boolean isBound = false;
	public static final String PREFS_NAME = "HibonitPrefs";
	private static Location lastLocation;
	private double latitude;
	private double longitude;
	private double distance;
	private double averageSpeed;
	private Intent serviceIntent;
	private String kmlFile;
	private String speedString = "";
	private int maxGraphX = 0, maxGraphY = 0;;

	private TextView textAverageSpeed;
	private TextView textDistance;
	private TextView textKml;
	private Button buttonStartTracking;
	private Button buttonStopTracking;

	public ArrayList<Double> lat = new ArrayList<Double>();
	public ArrayList<Double> lng = new ArrayList<Double>();

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		lat = new ArrayList<Double>();
		lng = new ArrayList<Double>();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gps_layout);
		serviceIntent = new Intent(this, LocationService.class);
		mBroadcastReceiver = new MyBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION);
		this.registerReceiver(mBroadcastReceiver, intentFilter);
		kmlFile = KML_START;

		textAverageSpeed = (TextView) findViewById(R.id.avg_speed_textview);
		textDistance = (TextView) findViewById(R.id.distance_textview);
		textKml = (TextView) findViewById(R.id.kml_textview);
		buttonStartTracking = (Button) findViewById(R.id.button_start_tracking);
		buttonStartTracking.setOnClickListener(buttonListenerStartTracking);
		buttonStopTracking = (Button) findViewById(R.id.button_stop_tracking);
		buttonStopTracking.setOnClickListener(buttonListenerStopTracking);

		sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
		boolean accelSupported = sensorMgr.registerListener(this, SensorManager.SENSOR_ACCELEROMETER, SensorManager.SENSOR_DELAY_GAME);

		if (!accelSupported) {
			sensorMgr.unregisterListener(this, SensorManager.SENSOR_ACCELEROMETER);
		}
	}

	private OnClickListener buttonListenerStartTracking = new OnClickListener() {
		public void onClick(View v) {
			startTracking();
		}
	};

	private OnClickListener buttonListenerStopTracking = new OnClickListener() {
		public void onClick(View v) {
			stopTracking();
			principal();
		}
	};

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			locationInterface = LocationInterface.Stub.asInterface(service);
		}

		public void onServiceDisconnected(ComponentName className) {
			locationInterface = null;
		}
	};

	void doBindService() {
		bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
		isBound = true;
	}

	void doUnbindService() {
		unbindService(mConnection);
		isBound = false;
	}

	public void onPause() {
		super.onPause();
		SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean("serviceIsBound", isBound);
		editor.commit();
		if (isBound) {
			doUnbindService();
		}

		// if (sensorMgr != null) {
		// sensorMgr.unregisterListener(this,
		// SensorManager.SENSOR_ACCELEROMETER);
		// sensorMgr = null;
		// }
	}

	public void onResume() {
		super.onResume();
		SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
		isBound = sharedPreferences.getBoolean("serviceIsBound", false);

		if (isBound) { // if service is running
			doBindService();
		}
	}

	public void tryStartService() {
		if (!isBound) {
			startService(serviceIntent);
			doBindService();
			LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) { // perguntar
																			// se
																			// quer
																			// habilitar
				Toast toast = Toast.makeText(getApplicationContext(), "GPS is disabled.", Toast.LENGTH_SHORT);
				toast.show();
			}
		}
		if (sensorMgr != null) {
			sensorMgr.unregisterListener(this, SensorManager.SENSOR_ACCELEROMETER);
			sensorMgr = null;
		}
	}

	public void tryStopService() {
		if (isBound) {
			doUnbindService();
			stopService(serviceIntent);
		}
	}

	public void onDestroy() {
		this.unregisterReceiver(mBroadcastReceiver);

		if (sensorMgr != null) {
			sensorMgr.unregisterListener(this, SensorManager.SENSOR_ACCELEROMETER);
			sensorMgr = null;
		}

		super.onDestroy();

	}
	
	public class MyBroadcastReceiver extends BroadcastReceiver {
		public void onReceive(Context arg0, Intent intent) { // conferir a
																// action (se
																// tiver mais de
																// 1)
			if (intent.getAction().equals(BROADCAST_ACTION)) {
				// latitude = intent.getDoubleExtra(LATITUDE, 0);
				// longitude = intent.getDoubleExtra(LONGITUDE, 0);
				Log.v("hibonit", "broadcast");
				kmlFile += intent.getStringExtra(KML_DATA);
//				textKml.setText(intent.getStringExtra(KML_DATA));
				lat.add(intent.getDoubleExtra(LATITUDE,0));
				lng.add(intent.getDoubleExtra(LONGITUDE,0));
				Log.v("hibonit", kmlFile);
			} else if (intent.getAction().equals(BROADCAST_ACTION_END)) {
				Log.v("hibonit", "broadcast end");
				averageSpeed = intent.getDoubleExtra(AVERAGE_SPEED, 0);
				textAverageSpeed.setText(String.valueOf(averageSpeed));
				distance = intent.getDoubleExtra(DISTANCE, 0);
				textDistance.setText(String.valueOf(distance));
			} else if (intent.getAction().equals(BROADCAST_ACTION_KM)) {
				double tmp = intent.getDoubleExtra(AVERAGE_SPEED_PER_KM, 0);
				if (speedString.length() == 0)
					speedString = String.valueOf(tmp);
				else
					speedString += "," + String.valueOf(tmp);
				if (tmp > maxGraphY)
					maxGraphY = (int) tmp;
				maxGraphX++;

			}

			// pegar os outros tb
		}
	}

	public void startTracking() {
		tryStartService();
		// SpeedChart tmp = new SpeedChart();
		// ArrayList<double[]> values = new ArrayList<double[]>();
		// values.add(new double[] { 5, 10, 18 , 22, 30});
		// ArrayList<double[]> time = new ArrayList<double[]>();
		// time.add(new double[] { 1, 2, 3, 4, 5});
		// tmp.setData(time, values);
		// Intent chartIntent = tmp.execute(this);
		// startActivity(chartIntent);
	}

	public void stopTracking() {
		kmlFile += KML_END;
		// textKml.setText(kmlFile);
		Log.v("hibonit", kmlFile);

		// textAverageSpeed.setText(String.valueOf(averageSpeed));

		if (locationInterface != null) {
			try {
				distance = locationInterface.getDistance();
				Log.v("hibonit", "distance in act:" + distance);
//				textDistance.setText(String.valueOf(distance));
				// averageSpeed = positionInterface.getAverageSpeed();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		// textDistance.setText(String.valueOf(distance));

		// textAverageSpeed.setText(String.valueOf(averageSpeed));

		SumarioCorrida sumarioCorrida = new SumarioCorrida("teste");
		sumarioCorrida.distancia = distance;
		sumarioCorrida.velocidade = averageSpeed;
		sumarioCorrida.rota = kmlFile;
		sumarioCorrida.graficoUrl = getGraphUrl();
		sumarioCorrida.salvar();
		tryStopService();
		Log.v("hibonit", "html: " + sumarioCorrida.toHTML());
	}

	/*
	 * public void updateValues(){ if(locationInterface != null){ try { latitude
	 * = locationInterface.getLatitude(); longitude =
	 * locationInterface.getLongitude(); distance =
	 * locationInterface.getDistance(); altitude =
	 * locationInterface.getAltitude(); averageSpeed =
	 * locationInterface.getAverageSpeed(); } catch (RemoteException e) {
	 * e.printStackTrace(); } } }
	 */

	public String getGraphUrl() {
		// String string =
		// "http://chart.googleapis.com/chart?chxl=1:|Km|3:|Km%2Fh&chxp=1,50|3,60&chxt=x,x,y,y&chbh=a&chs=300x225&cht=bvg&chco=EC871D&chd=t:10,50,60,80,40,60,30&chtt=Velocidade";
		int mediumX = maxGraphX / 2;
		int mediumY = maxGraphY / 2;

		return "http://chart.googleapis.com/chart?chxl=1:|Km|3:|Km%2Fh&chxp=1," + String.valueOf(mediumX) + "|3," + String.valueOf(mediumY)
				+ "&chxt=x,x,y,y&chbh=a&chs=300x225&cht=bvg&chco=EC871D&chd=t:" + speedString + "&chtt=Velocidade";
	}

	public void onAccuracyChanged(int arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	public void onSensorChanged(int sensor, float[] values) {
		if (sensor == SensorManager.SENSOR_ACCELEROMETER) {
			long curTime = System.currentTimeMillis();
			// only allow one update every 100ms.
			if ((curTime - lastUpdate) > 100) {
				long diffTime = (curTime - lastUpdate);
				lastUpdate = curTime;

				x = values[SensorManager.DATA_X];
				y = values[SensorManager.DATA_Y];
				z = values[SensorManager.DATA_Z];

				float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;
				last_x = x;
				last_y = y;
				last_z = z;
				if (speed > SHAKE_THRESHOLD) {
					Log.v("hibonit", "SHAKE");
					MediaPlayer player = MediaPlayer.create(this, Settings.System.DEFAULT_NOTIFICATION_URI);
					player.start();
					startTracking();
				}
			}
		}
	}

	public void principal() {
		double centerLat = 0.0;
		double centerLng = 0.0;
		int i = 0;

	
		File fpw = new File("/mnt/sdcard/polyline.html");
		try {
			PrintWriter pw = new PrintWriter(fpw);
			pw.println("<!DOCTYPE html>");
			pw.println("<html lang='en'>");
			pw.println("  <head>");
			pw.println("    <meta charset='utf-8' />");
			pw.println("    <title>Mapa da corrida</title>");
			pw.println("    <script src='//maps.google.com/maps?file=api&amp;v=2&amp;key=AIzaSyD4iE2xVSpkLLOXoyqT-RuPwURN3ddScAI'");
			pw.println("            type='text/javascript'></script>");
			pw.println("    <script type='text/javascript'>");
			pw.println("    function initialize() {");
			pw.println("      if (GBrowserIsCompatible()) {");
			pw.println("        var map = new GMap2(document.getElementById('map_canvas'));");

			pw.println(String.format("        map.setCenter(new GLatLng(%f, %f), 13);", centerLat, centerLng));

			pw.println("        var polyline = new GPolyline([");

			for (i = 0; i < lat.size() - 1; i++) {
				pw.println(String.format("           new GLatLng(%f, %f),", lat.get(i), lng.get(i)));
			}
			pw.println(String.format("           new GLatLng(%f, %f)", lat.get(i), lng.get(i)));

			pw.println("     ], '#ff0000', 10);");
			pw.println("     map.addOverlay(polyline);");
			pw.println("      }");
			pw.println("    }  ");
			pw.println("    </script>");
			pw.println("  </head>");
			pw.println("  <body onload='initialize()' onunload='GUnload()'>");
			pw.println("    <div id='map_canvas' style='width: 500px; height: 300px'></div>");
			pw.println("    <div id='message'></div>");
			pw.println("  </body>");
			pw.println("</html>");
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
