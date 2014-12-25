package com.miao.barcodereader;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.SurfaceView;
import android.widget.ImageView;

public class MainActivity extends Activity {

	CameraManage cm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		cm = new CameraManage(this, (ImageView) findViewById(R.id.imageView),
				(SurfaceView) findViewById(R.id.preview_view));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
