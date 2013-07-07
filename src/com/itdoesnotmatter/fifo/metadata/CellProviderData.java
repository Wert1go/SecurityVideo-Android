package com.itdoesnotmatter.fifo.metadata;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

import com.itdoesnotmatter.fifo.utils.Constants;
import com.itdoesnotmatter.fifo.utils.ServerRequestTask;
import com.itdoesnotmatter.fifo.utils.XMLParser;

public class CellProviderData {
	private int cellId;
	private int lacId;
	private int mcc;
	private int mnc;
	private String country;
	private Context context;
	private String providerName;
	
	
	public CellProviderData(Context context) {
		this.context = context;
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		GsmCellLocation loc = (GsmCellLocation) tm.getCellLocation();

		this.setCellId(loc.getCid());
		this.setLacId(loc.getLac());
		
		String operatorname = tm.getNetworkOperatorName();
		this.setProviderName(operatorname);
		
		String networkOperator = tm.getNetworkOperator();

	    if (networkOperator != null) {
	        int mcc = Integer.parseInt(networkOperator.substring(0, 3));
	        this.setMcc(mcc);
	        int mnc = Integer.parseInt(networkOperator.substring(3));
	        this.setMnc(mnc);
	    }
		String operatoriso = tm.getNetworkCountryIso();
		this.setCountry(operatoriso);
	}

	public int getCellId() {
		return cellId;
	}

	public void setCellId(int cellId) {
		this.cellId = cellId;
	}

	public int getLacId() {
		return lacId;
	}

	public void setLacId(int lacId) {
		this.lacId = lacId;
	}

	public String getProviderName() {
		return providerName;
	}

	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}
	
	public void requestCoordinates(final MetaDataReciver reciver) {
		//http://www.opencellid.org/cell/get?key=myapikey&mnc=1&mcc=2&lac=200&cellid=234
		String urlString = "http://www.opencellid.org/cell/get?key=" + Constants.OPENCELLID_API_KEY + "&mcc=" + this.getMcc() + "&mnc=" + this.getMnc() + "&lac=" + this.getLacId() + "&cellid=" + this.getCellId();
		URL requestUrl = null;
		
		try {
			requestUrl = new URL(urlString);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		ServerRequestTask requestTask = new ServerRequestTask(requestUrl, this.context);
		requestTask.setOnServerDataArrivedListener(new ServerDataReciver() {

			@Override
			public void onServerDataArrived(String resultString) {

				List<Coordinate> coordinates = XMLParser.parseCoordinates(resultString);
				
				if (reciver != null)
					reciver.onCoordinatesArrived(coordinates.get(0).lat, coordinates.get(0).lon);
			}
			
		});
		requestTask.execute();
	}

	public int getMcc() {
		return mcc;
	}

	public void setMcc(int mcc) {
		this.mcc = mcc;
	}

	public int getMnc() {
		return mnc;
	}

	public void setMnc(int mnc) {
		this.mnc = mnc;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}
}
