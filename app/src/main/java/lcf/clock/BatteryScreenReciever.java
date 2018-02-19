package lcf.clock;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;

public class BatteryScreenReciever {
	private final Activity mActivity;
	private BroadcastReceiver mCharger = null;
	private final float mBatterySaveLevel;
	private final boolean mAutoClose;

	final int STATUS_BATTERY_LEVEL = 0;			// уровень батареи
	final int STATUS_BATTERY_DISCHARGING = 1;	// батарея разряжается
	final int STATUS_BATTERY_CHARGING = 2;		// батарея заряжается

	public BatteryScreenReciever(Activity activity, int levelPercent,
								 boolean autoClose, final Handler handler) {
		mActivity = activity;
		mAutoClose = autoClose;
		mBatterySaveLevel = levelPercent / 100.0f;
		IntentFilter batteryFilter = null;
		if (mBatterySaveLevel >= 0.0f) {
			mActivity.getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		}
		if (mAutoClose) {
			batteryFilter = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
		}
		if (batteryFilter != null) {
			mActivity.registerReceiver(mCharger = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					if (intent.getAction()
							.equals(Intent.ACTION_BATTERY_CHANGED)) {
						if (intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ==
								BatteryManager.BATTERY_STATUS_CHARGING)	{
							handler.sendEmptyMessage(STATUS_BATTERY_CHARGING);
						}
						if (intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0) !=
								BatteryManager.BATTERY_STATUS_CHARGING)	{
							handler.sendEmptyMessage(STATUS_BATTERY_DISCHARGING);
						}
						int batLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
						Message msg;
						msg = handler.obtainMessage(STATUS_BATTERY_LEVEL, batLevel, 0);
						handler.sendMessage(msg);

						if (intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0)
								* mBatterySaveLevel > intent.getIntExtra(
								BatteryManager.EXTRA_LEVEL, -1)
								&& intent.getIntExtra(
										BatteryManager.EXTRA_PLUGGED, -1) == 0
								&& intent.getIntExtra(
										BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_DISCHARGING) {
							mActivity
									.getWindow()
									.clearFlags(
											WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
							/*Log.i("tag",
									"NOT keep screen "
											+ intent.getIntExtra(
													BatteryManager.EXTRA_LEVEL, 0)
											+ "/"
											+ intent.getIntExtra(
													BatteryManager.EXTRA_SCALE, 0));*/
						} else {
							/*Log.i("tag",
									"keep screen "
											+ intent.getIntExtra(
													BatteryManager.EXTRA_LEVEL, 0)
											+ "/"
											+ intent.getIntExtra(
													BatteryManager.EXTRA_SCALE, 0));*/
							mActivity
									.getWindow()
									.addFlags(
											WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
						}
					} else {
						mActivity.finish();
					}
				}
			}, batteryFilter);
		}
	}

	public void free() {
		mActivity.getWindow().clearFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		if (mCharger != null) {
			mActivity.unregisterReceiver(mCharger);
			mCharger = null;
		}
	}

}
