package lcf.clock;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

public class NavBarHider {
	private static final String TAG = "NavBarHider";
	private final Activity mActivity;
	private final Handler mHandler = new Handler();
	private static final int HIDE_TIMEOUT_MS = 1000;
	private static final int RESTORE_HIDE_TIMEOUT_MS = 3500;
	private static boolean hasNavigationBar = false;
	private boolean isRunning = true;
	private final Runnable mWorkRunnable = new Runnable() {

		@SuppressLint("NewApi")
		@Override
		public void run() {
			if (!isRunning) {
				return;
			}
			Log.d(TAG, "hiding: hasNavigationBar=" + hasNavigationBar);
			if (hasNavigationBar && android.os.Build.VERSION.SDK_INT >= 11) {
				mActivity
						.getWindow()
						.getDecorView()
						.setSystemUiVisibility(
								mActivity.getWindow().getDecorView()
										.getSystemUiVisibility()
										| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			}
		}
	};

	@SuppressLint("NewApi")
	public NavBarHider(Activity activity, final View rootView) {
		mActivity = activity;

		if (android.os.Build.VERSION.SDK_INT >= 11) {
			rootView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
				@Override
				public void onSystemUiVisibilityChange(int visibility) {
					Log.d(TAG, "onSystemUiVisibilityChange");
					if (!isRunning
							|| (visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) {
						return;
					}
					hide(RESTORE_HIDE_TIMEOUT_MS);
				}
			});
		}
		rootView.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					private final View content = rootView;

					@SuppressWarnings("deprecation")
					@Override
					public void onGlobalLayout() {
						Log.d(TAG, "onGlobalLayout");
						if (android.os.Build.VERSION.SDK_INT >= 16) {
							content.getViewTreeObserver()
									.removeOnGlobalLayoutListener(this);
						} else {
							content.getViewTreeObserver()
									.removeGlobalOnLayoutListener(this);
						}
						hasNavigationBar = hasNavigationBar();
						if (hasNavigationBar && isRunning) {
							hide();
						}
					}

					private boolean hasNavigationBar() {
						DisplayMetrics displayMetrics = Style.getDisplayMetrics();
						return content.getHeight() < displayMetrics.heightPixels
								|| content.getWidth() < displayMetrics.widthPixels;
					}
				});
	}

	private void hide(int ms) {
		if (!isRunning) {
			return;
		}
		mHandler.removeCallbacks(mWorkRunnable);
		mHandler.postDelayed(mWorkRunnable, ms);
	}

	public void hide() {
		hide(HIDE_TIMEOUT_MS);
	}

	public void hideDelayed() {
		hide(RESTORE_HIDE_TIMEOUT_MS);
	}

	@SuppressLint("NewApi")
	public void show() {
		Log.d(TAG, "show");
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			mActivity
					.getWindow()
					.getDecorView()
					.setSystemUiVisibility(
							mActivity.getWindow().getDecorView()
									.getSystemUiVisibility()
									& (~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION));
		}
	}

	public void stop() {
		isRunning = false;
		show();
	}

	public void start() {
		isRunning = true;
		hide();
	}
}
