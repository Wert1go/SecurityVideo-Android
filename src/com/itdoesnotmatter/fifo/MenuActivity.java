package com.itdoesnotmatter.fifo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MenuActivity extends Activity implements OnClickListener{
	Button videoRegButton;
	Button reportButton;
	Button infoButton;
	Button userInfoButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_activity_layout);
		
		videoRegButton = (Button) findViewById(R.id.videoreg_button);
		videoRegButton.setOnClickListener(this);
		reportButton = (Button) findViewById(R.id.report_button);
		reportButton.setOnClickListener(this);
		infoButton  = (Button) findViewById(R.id.provider_button);
		infoButton.setOnClickListener(this);
		
		userInfoButton  = (Button) findViewById(R.id.user_button);
		userInfoButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.videoreg_button:
			Intent intentVideoReg = new Intent (this, CameraActivity.class);
			intentVideoReg.putExtra(VideoManager.RECORD_MODE, VideoManager.MODE_VIDEO_REG);
			startActivity(intentVideoReg);
			break;
		case R.id.report_button:
			Intent intentReport = new Intent (this, CameraActivity.class);
			intentReport.putExtra(VideoManager.RECORD_MODE, VideoManager.MODE_REPORT);
			startActivity(intentReport);
			break;
		case R.id.provider_button:
			Intent intent1 = new Intent (this, ProviderInfoActvity.class);
			this.startActivity(intent1);
			break;
		case R.id.user_button:
			Intent intent2 = new Intent (this, UserInfoActivity.class);
			this.startActivity(intent2);
			break;
		}
	}
}
