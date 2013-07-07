package com.itdoesnotmatter.fifo;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.format.Time;
import android.widget.TextView;

import com.itdoesnotmatter.fifo.utils.Constants;

public class UserInfoActivity extends Activity{
	TextView udidField;
	TextView timeField;
	TextView latField;
	TextView lonField;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_data_layout);
		
		this.udidField = (TextView) findViewById(R.id.udid_field);
		this.timeField = (TextView) findViewById(R.id.time_field);
		this.latField = (TextView) findViewById(R.id.lat_field);
		this.lonField = (TextView) findViewById(R.id.lon_field);
		
		this.udidField.setText("UDID: " + Constants.getUdid(this));
		
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		
		this.timeField.setText("Device time: " + today.format("%k:%M:%S"));
		
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		// Register the listener with the Location Manager to receive location updates
		// Or use LocationManager.GPS_PROVIDER
		String locationProvider = LocationManager.NETWORK_PROVIDER;
		Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
		setLocationData(lastKnownLocation);
		locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
	}
	
	LocationListener locationListener = new LocationListener() {
	    public void onLocationChanged(Location location) {
	    	// Called when a new location is found by the network location provider.
	    	setLocationData(location);
	    }

	    public void onStatusChanged(String provider, int status, Bundle extras) {}

	    public void onProviderEnabled(String provider) {}

	    public void onProviderDisabled(String provider) {}
	  };
	  
	  public void setLocationData(Location location) {
		  this.latField.setText("Lat: " + location.getLatitude());
		  this.lonField.setText("Lon: " + location.getLongitude());
	  }
}
