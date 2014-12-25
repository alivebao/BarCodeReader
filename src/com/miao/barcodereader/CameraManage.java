package com.miao.barcodereader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

public class CameraManage implements SurfaceHolder.Callback {
	private SurfaceHolder surfaceHolder;
	private Camera camera;

	private Activity activity;
	SurfaceView surfaceView;

	private ImageView imageView;
	private Timer mTimer;
	private TimerTask mTimerTask;

	private Camera.AutoFocusCallback mAutoFocusCallBack;
	private Camera.PreviewCallback previewCallback;

	CameraManage(Activity ac, ImageView iv, SurfaceView sv) {
		activity = ac;

		imageView = iv;
		surfaceView = sv;
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		AutoFocusSet();

		mTimer = new Timer();
		mTimerTask = new CameraTimerTask();
		mTimer.schedule(mTimerTask, 0, 500);
	}

	public void AutoFocusSet() {
		mAutoFocusCallBack = new Camera.AutoFocusCallback() {
			@Override
			public void onAutoFocus(boolean success, Camera camera) {
				if (success) {
					// isAutoFocus = true;
					camera.setOneShotPreviewCallback(previewCallback);
				}
			}
		};

		previewCallback = new Camera.PreviewCallback() {
			@Override
			public void onPreviewFrame(byte[] data, Camera arg1) {
				if (data != null) {
					Camera.Parameters parameters = camera.getParameters();
					int imageFormat = parameters.getPreviewFormat();
					Log.i("map", "Image Format: " + imageFormat);

					Log.i("CameraPreviewCallback", "data length:" + data.length);
					if (imageFormat == ImageFormat.NV21) {
						// get full picture
						Bitmap image = null;
						int w = parameters.getPreviewSize().width;
						int h = parameters.getPreviewSize().height;

						Rect rect = new Rect(0, 0, w, h);
						YuvImage img = new YuvImage(data, ImageFormat.NV21, w,
								h, null);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						if (img.compressToJpeg(rect, 100, baos)) {
							image = BitmapFactory.decodeByteArray(
									baos.toByteArray(), 0, baos.size());
							image = adjustPhotoRotation(image, 90);
							imageView.setImageBitmap(image);

							Drawable d = imageView.getDrawable();

							BitmapDrawable bd = (BitmapDrawable) d;

							Bitmap bm = bd.getBitmap();

							String str = BitmapLuminanceSource.getResult(bm);
							if (!str.equals("empty"))
								Toast.makeText(activity.getApplication(), str,
										Toast.LENGTH_SHORT).show();
						}

					}
				}
			}
		};
	}

	class CameraTimerTask extends TimerTask {
		@Override
		public void run() {
			if (camera != null) {
				camera.autoFocus(mAutoFocusCallBack);
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		initCamera(surfaceHolder);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		if (camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}
		previewCallback = null;
		mAutoFocusCallBack = null;
	}

	public void initCamera(SurfaceHolder surfaceHolder) {
		camera = Camera.open();
		if (camera == null) {
			return;
		}
		Camera.Parameters parameters = camera.getParameters();

		WindowManager wm = (WindowManager) (activity
				.getSystemService(Context.WINDOW_SERVICE)); // 获取当前屏幕管理器对象

		Display display = wm.getDefaultDisplay(); // 获取屏幕信息的描述类
		parameters.setPreviewSize(display.getWidth(), display.getHeight());
		camera.setParameters(parameters);
		try {
			camera.setPreviewDisplay(surfaceHolder);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		camera.setDisplayOrientation(90);
		camera.startPreview();
	}

	public Bitmap adjustPhotoRotation(Bitmap bm, final int orientationDegree) {
		Matrix m = new Matrix();
		m.setRotate(orientationDegree, (float) bm.getWidth() / 2,
				(float) bm.getHeight() / 2);

		try {
			Bitmap bm1 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
					bm.getHeight(), m, true);
			return bm1;
		} catch (OutOfMemoryError ex) {
		}
		return null;
	}

}
