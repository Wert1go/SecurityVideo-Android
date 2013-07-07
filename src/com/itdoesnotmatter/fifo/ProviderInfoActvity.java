package com.itdoesnotmatter.fifo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.itdoesnotmatter.fifo.metadata.CellProviderData;
import com.itdoesnotmatter.fifo.metadata.MetaDataReciver;

public class ProviderInfoActvity extends Activity implements MetaDataReciver{
	TextView cellIdField;
	TextView cellIdLatField;
	TextView cellIdLonField;
	
	TextView lacField;
	TextView providerNameField;
	
	TextView mccField;
	TextView mncField;
	TextView countryField;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.provider_info_layout);
		
		this.cellIdField = (TextView) findViewById(R.id.cell_id_field);
		this.cellIdLatField = (TextView) findViewById(R.id.cellid_lat_field);
		this.cellIdLonField = (TextView) findViewById(R.id.cellid_lon_field);
		this.lacField = (TextView) findViewById(R.id.lac_field);
		this.providerNameField = (TextView) findViewById(R.id.provider_name_field);
		this.mccField = (TextView) findViewById(R.id.mcc_field);
		this.mncField = (TextView) findViewById(R.id.mnc_field);
		this.countryField = (TextView) findViewById(R.id.country_field);
		
		CellProviderData data = new CellProviderData(this);
		
		this.cellIdField.setText("GSM Cell ID (CID): " + data.getCellId());
		this.lacField.setText("GSM Local Area Code (LAC): " + data.getLacId());
		this.providerNameField.setText("Operator: " + data.getProviderName());
		
		this.mccField.setText("Mobile Country Code (MCC): " + data.getMcc());
		this.mncField.setText("Mobile Network Code (MNC): " + data.getMnc());
		
		this.countryField.setText("Country: " + data.getCountry());
		
		data.requestCoordinates(this);
	}

	@Override
	public void onCoordinatesArrived(double lat, double lon) {
		Log.e("onCoordinatesArrived", "lat " + lat + " lon " + lon);
		this.cellIdLatField.setText("Cell ID Latitude: " + lat);
		this.cellIdLonField.setText("Cell ID Longitude: " + lon);
	}
}
