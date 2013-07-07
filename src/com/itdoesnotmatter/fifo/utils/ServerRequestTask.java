package com.itdoesnotmatter.fifo.utils;

import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.itdoesnotmatter.fifo.metadata.ServerDataReciver;

public class ServerRequestTask extends AsyncTask<Void, Void, Void> {
	private URL requestUrl;
	private String requestResult;
	private ServerDataReciver reciver;
	private Context context;
	
	public void setOnServerDataArrivedListener(ServerDataReciver reciver) {
		this.reciver = reciver;
	}
	
	public ServerRequestTask(URL requestUrl, Context context) {
		this.requestUrl = requestUrl;
		this.context = context;
	}

	public String doGetRequest(URL requestUrl) {
		String responseString = null;
		
		HttpParams httpParams = new BasicHttpParams();
		DefaultHttpClient httpclient = new DefaultHttpClient(httpParams);

		HttpGet httpGet = null;
		
		if(connectedToInternet()) {
			try {
				httpGet = new HttpGet(requestUrl.toURI());

				HttpResponse response = null;
				response = httpclient.execute(httpGet);
				responseString = EntityUtils.toString(response
						.getEntity());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return responseString;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected Void doInBackground(Void... params) {
		Log.e("makeRequest to", "url: " + requestUrl.toString());
		requestResult = doGetRequest(requestUrl);
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		
		if (reciver != null) {
			reciver.onServerDataArrived(requestResult);
		}
	}
	
	public boolean connectedToInternet() {
		if (context == null)
			return false;
		
		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connManager.getActiveNetworkInfo();
		if (info == null) {
			return false;
		} else {
			return true;
		}
	}
}