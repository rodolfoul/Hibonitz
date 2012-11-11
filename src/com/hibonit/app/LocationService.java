package com.hibonit.app;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class LocationService extends Service{
	
	protected double latitude;
    protected double longitude;
    protected double altitude;
    protected float instantSpeed;
    
    protected LocationManager locationManager;
    protected LocationListener locationListener;
	protected int minTime= 0;  
	protected int minDistance = 0;	
	protected Location lastLocation;
	protected double totalDistance = 0; //in meters
	protected double currentKm = 1;
	protected long startTime;
	protected long kmStartTime;
	protected long currentKmSpeed = 0;
	
//	protected String gpxString;
	protected String kmlString;
//	protected final String GPX_START = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n"+
//			"<gpx version=\"1.1\" creator=\"GPSBabel - http://www.gpsbabel.org\" " + "\n" +
//			"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " + "\n" +
//			"xmlns=\"http://www.topografix.com/GPX/1/1\" " + "\n" +
//			"xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">" + "\n" +
//			"<metadata><time>2009-04-20T19:43:14Z</time><bounds minlat=\"48.469001667\" minlon=\"9.495335000\" maxlat=\"48.530370000\" maxlon=\"9.547900000\"/>" + "\n" +
//			"</metadata><trk><trkseg>" + "\n\n";
	
	
//	protected final String KML_START = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n"+
//	"<kml xmlns=\"http://www.opengis.net/kml/2.2\">" + "\n" + "<Document>" + "\n" +
//	"<name>Paths</name>" + "<description>KML</description>" + "\n" + "<Style id=\"yellowLineGreenPoly\">"
//	+ "\n" +  "<LineStyle>" + "\n" + "<color>7f00ffff</color>"+ "\n" + "<width>4</width>" + "\n" +
//	"</LineStyle>" + "\n" + "<PolyStyle><color>7f00ff00</color></PolyStyle>" + "\n" + "</Style>"
//	+ "\n" + "<Placemark>" + "<name>Absolute Extruded</name>" + "<description>kml</description>"
//	+ "\n" + "<styleUrl>#yellowLineGreenPoly</styleUrl>" + "\n" + "<LineString>" + "\n" +
//	"<extrude>1</extrude>" + "\n" + "<tessellate>1</tessellate>" + "\n" + "<altitudeMode>absolute</altitudeMode>" + "\n" +  "<coordinates>";
//	
//	protected final String KML_END = "</coordinates>" + "\n" + "</LineString>" + "\n" + "</Placemark>" + "\n" + "</Document>" + "\n" + "</kml>";

//	protected final String GPX_END = "</trkseg></trk></gpx>";
		
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public void onCreate() { 
		super.onCreate();
//		gpxString = GPX_START; // colocar o fim quando apropriado
//		kmlString = KML_START;
		locationManager = (LocationManager)
        		getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();
        locationManager.requestLocationUpdates( //usar network para testar em cel sem data plan
        		LocationManager.NETWORK_PROVIDER,//LocationManager.GPS_PROVIDER,
    			minTime,
    			minDistance,
    			locationListener);
        Log.v("hibonit", "onCreate service");
	}  
	
	private final LocationInterface.Stub mBinder = new LocationInterface.Stub() {
		// isso OU broadcastreceiver
//		public void stop() throws RemoteException{
////			kmlString += KML_END;
//			Intent broadcastIntent = new Intent();
//			broadcastIntent.setAction(MainActivity.BROADCAST_ACTION);
//			broadcastIntent.putExtra(MainActivity.AVERAGE_SPEED, totalDistance/(1000*getHours(startTime, System.nanoTime())));
//	        sendBroadcast(broadcastIntent);
//		}
		/*public double getLatitude() throws RemoteException {
			return latitude;
		}
		public double getLongitude() throws RemoteException {
			return longitude;
		}
		public double getAltitude() throws RemoteException {
			return altitude;
		}*/
		public double getDistance() throws RemoteException {
			return totalDistance;
		}
		public double getAverageSpeed() throws RemoteException {// totalDistance em metros
			return totalDistance/(1000*getHours(startTime, System.nanoTime()));
		}
		/*public double getInstantSpeed() throws RemoteException {
			return instantSpeed;
		}*/
	};
	
	public void onDestroy() {
		Log.v("hibonit", "onDestroy service");
		System.out.println("onDestroy service");
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(GPSActivity.BROADCAST_ACTION_END);
		broadcastIntent.putExtra(GPSActivity.AVERAGE_SPEED, totalDistance/(1000*getHours(startTime, System.nanoTime())));
		broadcastIntent.putExtra(GPSActivity.DISTANCE, totalDistance/1000);
		sendBroadcast(broadcastIntent);
		Log.v("hibonit", "dist: "+String.valueOf(totalDistance));
		locationManager.removeUpdates(locationListener); 
		super.onDestroy();
	}
	
	public class MyLocationListener implements LocationListener {
			
		public void onLocationChanged(Location location) {
			
			latitude = location.getLatitude();
			longitude = location.getLongitude();
			altitude = location.getAltitude();
			instantSpeed = location.getSpeed();
			
			if(lastLocation != null){
				totalDistance = totalDistance + location.distanceTo(lastLocation);	
				if(totalDistance > currentKm*1000){
					currentKm += 1;
					currentKmSpeed = (1/getHours(kmStartTime, System.nanoTime()));
					kmStartTime = System.nanoTime();
					Intent broadcastIntent = new Intent();
					broadcastIntent.setAction(GPSActivity.BROADCAST_ACTION_KM);
					broadcastIntent.putExtra(GPSActivity.AVERAGE_SPEED_PER_KM, currentKmSpeed);
					sendBroadcast(broadcastIntent);
				}
			}
			lastLocation = location;			
			
			Intent broadcastIntent = new Intent();
	        broadcastIntent.setAction(GPSActivity.BROADCAST_ACTION);
//			broadcastIntent.putExtra(GPSActivity.LATITUDE, latitude);
//			broadcastIntent.putExtra(GPSActivity.LONGITUDE, longitude);
//			broadcastIntent.putExtra(GPSActivity.ALTITUDE, altitude);
//			broadcastIntent.putExtra(GPSActivity.KM, currentKm);
//			broadcastIntent.putExtra(GPSActivity.AVERAGE_SPEED, totalDistance/(1000*getHours(startTime, System.nanoTime())));
//			broadcastIntent.putExtra(GPSActivity.AVERAGE_SPEED_PER_KM, currentKmSpeed);
//			broadcastIntent.putExtra(GPSActivity.INSTANT_SPEED, instantSpeed);
	        
//	        Date date = new Date();
	        kmlString = String.valueOf(latitude) + "," + String.valueOf(longitude) + "," +
	        			String.valueOf(altitude) + "\n";
	        broadcastIntent.putExtra(GPSActivity.KML_DATA, kmlString);
	        broadcastIntent.putExtra(GPSActivity.LATITUDE, latitude);

	        broadcastIntent.putExtra(GPSActivity.LONGITUDE, longitude);

//			gpxString += "<trkpt lat=\"" + String.valueOf(latitude)+ "\"";
//			gpxString += " lon=\"" + String.valueOf(longitude) + "\">\n";
//			gpxString += "  <time>" + String.valueOf(date) + "</time>\n</trkpt>\n";	
	        sendBroadcast(broadcastIntent);
		}
		
		public void onProviderDisabled(String provider) {
		}
		public void onProviderEnabled(String provider) {
		}
		public void onStatusChanged(String provider, int status, Bundle extras) {			
		}		
	}
	
	public long getHours(long start, long end){			
		return (end - start)/(1000000000*3600);
	}
	
	/*public long getKm (long start, long end){
		return (end-start)/1000;
	}*/
} 
