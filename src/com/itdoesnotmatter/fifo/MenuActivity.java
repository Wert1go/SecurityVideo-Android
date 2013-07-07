package com.itdoesnotmatter.fifo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.itdoesnotmatter.fifo.utils.Constants;
import com.itdoesnotmatter.fifo.utils.IntentKeys;

public class MenuActivity extends Activity implements OnClickListener{
	Button cameraButton;
	Button infoButton;
	Button userInfoButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_activity_layout);
		
		cameraButton = (Button) findViewById(R.id.camera_button);
		cameraButton.setOnClickListener(this);
		infoButton  = (Button) findViewById(R.id.provider_button);
		infoButton.setOnClickListener(this);
		
		userInfoButton  = (Button) findViewById(R.id.user_button);
		userInfoButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.camera_button:
			final Intent intent = new Intent (this, CameraActivity.class);
			
			final CharSequence[] items = Constants.videoQualityStrings();
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Video quality");
			builder.setItems(items, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			    	if (item != 8) {
			    		intent.putExtra(IntentKeys.VIDEO_QUALITY, item);
			    	} else {
			    		intent.setClass(getBaseContext(), VideoConfigurationActivity.class);
			    	}
			    	dialog.dismiss();
		    		startActivity(intent);
			    }
			});
			AlertDialog alert = builder.create();
			alert.show();

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
